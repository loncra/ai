package io.github.loncra.ai.genai.image;

import com.google.genai.Client;
import com.google.genai.types.*;
import io.github.loncra.ai.genai.image.domain.metadata.GoogleGenAiImageGenerationMetadata;
import io.github.loncra.ai.genai.image.enumerate.GeminiImageModel;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import io.micrometer.observation.ObservationRegistry;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.*;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.observation.DefaultImageModelObservationConvention;
import org.springframework.ai.image.observation.ImageModelObservationContext;
import org.springframework.ai.image.observation.ImageModelObservationConvention;
import org.springframework.ai.image.observation.ImageModelObservationDocumentation;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Matcher.quoteReplacement;

/**
 * Google Gemini 原生图像生成（Nano Banana / Flash Image）的 {@link ImageModel} 实现。
 *
 * @see GoogleGenAiImageOptions#DEFAULT_MODEL
 * @author maurice.chen
 */
public class GoogleGenAiImageModel implements ImageModel {

	private static final Logger logger = LoggerFactory.getLogger(GoogleGenAiImageModel.class);

	/**
	 * 部分渠道/网关在文本里返回 Markdown 内嵌图：{@code ![alt](data:image/png;base64,...)}，而非 {@link Part#inlineData()}。
	 */
	private static final Pattern MARKDOWN_DATA_URL_IMAGE = Pattern.compile(
			"!\\[[^]]*]\\(\\s*data:([^;]+);base64,([^)]+)\\s*\\)");

	/** Micrometer 观测中使用的提供商标识。 */
	public static final String PROVIDER_NAME = "google-genai";

	/**
	 * {@link ImageResponse#getMetadata()} 中聚合文本（含将 Markdown data URL 替换为 {@code [n]}）的键，
	 * 与 {@link ImageGeneration} 列表下标对应；无文本时可能不存在该键。
	 */
	public static final String METADATA_DOCUMENT_TEXT_KEY = "io.github.loncra.ai.google-genai-image.documentText";

	/**
	 * 一次 {@link GenerateContentResponse} 解析结果：图像列表与可读文档文本。
	 *
	 * @param generations 按应答顺序解析的图像
	 * @param documentText 各文本 Part 拼接后的正文；内嵌图已替换为 {@code [index]}，与 {@code generations} 下标一致
	 */
	public record GoogleGenAiImageParseResult(List<ImageGeneration> generations, String documentText) {
		public GoogleGenAiImageParseResult {
			generations = List.copyOf(generations);
			documentText = documentText == null ? StringUtils.EMPTY : documentText;
		}
	}

	private static final ImageModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultImageModelObservationConvention();

	private final Client genAiClient;

	private final GoogleGenAiImageOptions defaultOptions;

	private final RetryTemplate retryTemplate;

	private final ObservationRegistry observationRegistry;

	private ImageModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

	public GoogleGenAiImageModel(Client genAiClient) {
		this(genAiClient, GoogleGenAiImageOptions.builder().build());
	}

	public GoogleGenAiImageModel(Client genAiClient, GoogleGenAiImageOptions defaultOptions) {
		this(genAiClient, defaultOptions, new RetryTemplate());
	}

	public GoogleGenAiImageModel(Client genAiClient, GoogleGenAiImageOptions defaultOptions,
	                             RetryTemplate retryTemplate) {
		this(genAiClient, defaultOptions, retryTemplate, ObservationRegistry.NOOP);
	}

	public GoogleGenAiImageModel(Client genAiClient, GoogleGenAiImageOptions defaultOptions,
	                             RetryTemplate retryTemplate, ObservationRegistry observationRegistry) {
		Assert.notNull(genAiClient, "GenAI Client 不能为 null");
		Assert.notNull(defaultOptions, "默认选项不能为 null");
		Assert.notNull(retryTemplate, "retryTemplate 不能为 null");
		Assert.notNull(observationRegistry, "observationRegistry 不能为 null");
		this.genAiClient = genAiClient;
		this.defaultOptions = defaultOptions;
		this.retryTemplate = retryTemplate;
		this.observationRegistry = observationRegistry;
	}

	@Override
	public @Nullable ImageResponse call(ImagePrompt imagePrompt) {
		ImagePrompt requestImagePrompt = buildRequestImagePrompt(imagePrompt);
		GoogleGenAiImageOptions opts = CastUtils.cast(requestImagePrompt.getOptions());
		Assert.notNull(opts, "合并后的图像选项不能为 null");
		String instructions = requestImagePrompt.getInstructions().getFirst().getText();
		Assert.hasText(instructions, "图像提示词文本不能为空");

		String modelName = opts.getModel() != null ? opts.getModel() : GoogleGenAiImageOptions.DEFAULT_MODEL;
		int n = opts.getN() != null && opts.getN() > 0 ? opts.getN() : 1;

		var observationContext = ImageModelObservationContext.builder()
				.imagePrompt(imagePrompt)
				.provider(PROVIDER_NAME)
				.build();

		return ImageModelObservationDocumentation.IMAGE_MODEL_OPERATION
				.observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
						this.observationRegistry)
				.observe(() -> {
					GenerateContentConfig config = buildGenerateContentConfig(opts);
					List<ImageGeneration> generations = new ArrayList<>();
					List<String> text = new LinkedList<>();
					for (int i = 0; i < n; i++) {
						GenerateContentResponse raw = this.retryTemplate
								.execute(ctx -> invokeGenerateContent(modelName, instructions, config, opts));
						GoogleGenAiImageParseResult chunk = parseGenerateContentResponse(raw);
						generations.addAll(chunk.generations());
						if (chunk.generations().isEmpty()) {
							raw.promptFeedback().ifPresent(pf -> logger.warn("Gemini 提示反馈：{}", pf));
							throw new SystemException("模型 " + modelName + " 的 Gemini 图像响应中未解析到 inline 图像数据");
						}
						if (StringUtils.isNotEmpty(chunk.documentText())) {
							text.add(chunk.documentText());
						}
					}
					ImageResponseMetadata responseMetadata = new ImageResponseMetadata();
					if (CollectionUtils.isNotEmpty(text)) {
						responseMetadata.put(METADATA_DOCUMENT_TEXT_KEY, text);
					}
					ImageResponse response = new ImageResponse(generations, new ImageResponseMetadata());
					observationContext.setResponse(response);
					return response;
				});
	}

	private GenerateContentResponse invokeGenerateContent(String modelName, String instructions,
	                                                      GenerateContentConfig config, GoogleGenAiImageOptions opts) {
		List<Part> refs = opts.getReferenceImages();
		if (refs == null || refs.isEmpty()) {
			return this.genAiClient.models.generateContent(modelName, instructions, config);
		}
		int max = GeminiImageModel.maxReferenceImagesForModelId(modelName);
		if (refs.size() > max) {
			throw new IllegalArgumentException(String.format(
					"模型 %s 最多允许 %d 张参考图，当前为 %d 张", modelName, max, refs.size()));
		}
		List<Part> parts = new ArrayList<>();
		parts.add(Part.fromText(instructions));
		parts.addAll(refs);
		Content content = Content.fromParts(parts.toArray(Part[]::new));
		return this.genAiClient.models.generateContent(modelName, content, config);
	}

	private GenerateContentConfig buildGenerateContentConfig(GoogleGenAiImageOptions opt) {
		GenerateContentConfig.Builder b = GenerateContentConfig.builder();
		// 官方 Java 示例与 REST 均要求声明响应模态，否则可能无法返回 inline 图像；见
		// https://ai.google.dev/gemini-api/docs/image-generation
		if (opt.getResponseModalities() != null && !opt.getResponseModalities().isEmpty()) {
			b.responseModalities(opt.getResponseModalities());
		}
		else {
			b.responseModalities("TEXT", "IMAGE");
		}
		ImageConfig.Builder ic = ImageConfig.builder();
		boolean hasImage = false;
		if (StringUtils.isNotEmpty(opt.getAspectRatio())) {
			ic.aspectRatio(opt.getAspectRatio());
			hasImage = true;
		}
		if (StringUtils.isNotEmpty(opt.getImageSize())) {
			ic.imageSize(opt.getImageSize());
			hasImage = true;
		}
		if (StringUtils.isNotEmpty(opt.getPersonGeneration())) {
			ic.personGeneration(opt.getPersonGeneration());
			hasImage = true;
		}
		if (hasImage) {
			b.imageConfig(ic.build());
		}
		if (CollectionUtils.isNotEmpty(opt.getSafetySettings())) {
			b.safetySettings(opt.getSafetySettings());
		}

		return b.build();
	}

	/**
	 * 将 {@link GenerateContentResponse} 解析为图像列表与文档正文（详见 {@link GoogleGenAiImageParseResult}）。
	 */
	public static GoogleGenAiImageParseResult parseGenerateContentResponse(GenerateContentResponse raw) {
		List<ImageGeneration> generations = new ArrayList<>();
		StringBuilder document = new StringBuilder();
		if (!Objects.requireNonNull(raw.parts()).isEmpty()) {
			for (Part p : raw.parts()) {
				consumePart(generations, document, p);
			}
			return new GoogleGenAiImageParseResult(generations, document.toString());
		}
		raw.candidates().ifPresent(candidates -> {
			for (Candidate c : candidates) {
				c.content().flatMap(Content::parts).ifPresent(parts -> {
					for (Part p : parts) {
						consumePart(generations, document, p);
					}
				});
			}
		});
		return new GoogleGenAiImageParseResult(generations, document.toString());
	}

	/**
	 * 将 {@link GenerateContentResponse} 中的 inline 图像解析为 Spring AI {@link ImageGeneration} 列表。
	 * <p>
	 * 等价于 {@link #parseGenerateContentResponse(GenerateContentResponse)}{@code .generations()}。
	 */
	public static List<ImageGeneration> toImageGenerations(GenerateContentResponse raw) {
		return parseGenerateContentResponse(raw).generations();
	}

	private static void appendDocumentSection(StringBuilder document, String section) {
		if (StringUtils.isBlank(section)) {
			return;
		}
		if (!document.isEmpty()) {
			document.append(System.lineSeparator());
		}
		document.append(section);
	}

	private static void consumePart(List<ImageGeneration> generations, StringBuilder document, Part p) {
		if (p.inlineData().isPresent()) {
			Blob blob = p.inlineData().get();
			if (blob.data().filter(b -> b.length > 0).isPresent()) {
				generations.add(blobToGeneration(blob));
				return;
			}
		}
		p.text().filter(StringUtils::isNotEmpty).ifPresent(text -> appendTextPartWithMarkdownImages(generations, document, text));
	}

	/**
	 * 从 Markdown 内嵌 {@code data:image/...;base64,...} 解析图像；将匹配段替换为 {@code [index]}（与 {@code generations} 中下标一致），
	 * 并把替换后的文本写入 {@code document}。
	 */
	private static void appendTextPartWithMarkdownImages(List<ImageGeneration> generations, StringBuilder document,
	                                                     String text) {
		int baseIndex = generations.size();
		StringBuilder redacted = new StringBuilder();
		Matcher m = MARKDOWN_DATA_URL_IMAGE.matcher(text);
		int ordinal = 0;
		while (m.find()) {
			String mime = m.group(1).trim();
			String b64 = m.group(2).trim().replaceAll("\\s+", StringUtils.EMPTY);
			if (!mime.startsWith("image/") || StringUtils.isEmpty(b64)) {
				m.appendReplacement(redacted, quoteReplacement(m.group(0)));
				continue;
			}
			try {
				MediaType mediaType = MediaType.valueOf(mime);
				ImageGenerationMetadata meta = new GoogleGenAiImageGenerationMetadata(mediaType);
				generations.add(new ImageGeneration(new Image(null, b64), meta));
				int idx = baseIndex + ordinal;
				ordinal++;
				m.appendReplacement(redacted, quoteReplacement("[" + idx + "]"));
			}
			catch (IllegalArgumentException ex) {
				m.appendReplacement(redacted, quoteReplacement(m.group(0)));
			}
		}
		m.appendTail(redacted);
		appendDocumentSection(document, redacted.toString());
	}

	private static ImageGeneration blobToGeneration(Blob blob) {
		byte[] data = blob.data().orElse(new byte[0]);
		String b64 = Base64.getEncoder().encodeToString(data);
		MediaType mime = blob.mimeType()
				.filter(StringUtils::isNotEmpty)
				.map(MediaType::valueOf)
				.orElseGet(() -> detectImageMimeFromBytes(data));
		ImageGenerationMetadata meta = new GoogleGenAiImageGenerationMetadata(mime);
		return new ImageGeneration(new Image(null, b64), meta);
	}

	/**
	 * 在 {@link Blob#mimeType()} 为空时根据常见图像魔数推断 MIME。
	 */
	// FIXME 这个方法应该独立成一个工具方法。
	private static MediaType detectImageMimeFromBytes(byte[] data) {
		if (data == null || data.length < 3) {
			return MediaType.APPLICATION_OCTET_STREAM;
		}
		if (data.length >= 4 && data[0] == (byte) 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47) {
			return MediaType.IMAGE_PNG;
		}
		if ((data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) {
			return MediaType.IMAGE_JPEG;
		}
		if (data.length >= 6 && data[0] == 'G' && data[1] == 'I' && data[2] == 'F' && data[3] == '8'
				&& (data[4] == '7' || data[4] == '9') && data[5] == 'a') {
			return MediaType.IMAGE_GIF;
		}
		if (data.length >= 12 && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F') {
			return MediaType.valueOf("image/webp");
		}
		return MediaType.APPLICATION_OCTET_STREAM;
	}

	private ImagePrompt buildRequestImagePrompt(ImagePrompt imagePrompt) {
		GoogleGenAiImageOptions runtimeOptions;
		if (imagePrompt.getOptions() instanceof GoogleGenAiImageOptions g) {
			runtimeOptions = GoogleGenAiImageOptions.fromOptions(g);
		}
		else {
			runtimeOptions = ModelOptionsUtils.copyToTarget(imagePrompt.getOptions(), ImageOptions.class,
					GoogleGenAiImageOptions.class);
		}

		GoogleGenAiImageOptions requestOptions;
		requestOptions = GoogleGenAiImageOptions.builder()
				.N(ModelOptionsUtils.mergeOption(runtimeOptions.getN(), this.defaultOptions.getN()))
				.model(ModelOptionsUtils.mergeOption(runtimeOptions.getModel(), this.defaultOptions.getModel()))
				.width(ModelOptionsUtils.mergeOption(runtimeOptions.getWidth(), this.defaultOptions.getWidth()))
				.height(ModelOptionsUtils.mergeOption(runtimeOptions.getHeight(), this.defaultOptions.getHeight()))
				.responseFormat(ModelOptionsUtils.mergeOption(
						runtimeOptions.getResponseFormat(),
						this.defaultOptions.getResponseFormat()
				))
				.style(ModelOptionsUtils.mergeOption(runtimeOptions.getStyle(), this.defaultOptions.getStyle()))
				.aspectRatio(ModelOptionsUtils.mergeOption(
						runtimeOptions.getAspectRatio(),
						this.defaultOptions.getAspectRatio()
				))
				.imageSize(ModelOptionsUtils.mergeOption(
						runtimeOptions.getImageSize(),
						this.defaultOptions.getImageSize()
				))
				.personGeneration(ModelOptionsUtils.mergeOption(
						runtimeOptions.getPersonGeneration(),
						this.defaultOptions.getPersonGeneration()
				))
				.safetySettings(ModelOptionsUtils.mergeOption(
						runtimeOptions.getSafetySettings(),
						this.defaultOptions.getSafetySettings()
				))
				.build();
		requestOptions.setResponseModalities(mergeResponseModalities(runtimeOptions));

		requestOptions.setReferenceImages(GoogleGenAiImageOptions.mergeReferenceImages(runtimeOptions,
				this.defaultOptions));

		if (requestOptions.getModel() == null) {
			requestOptions.setModel(GoogleGenAiImageOptions.DEFAULT_MODEL);
		}
		return new ImagePrompt(imagePrompt.getInstructions(), requestOptions);
	}

	public void setObservationConvention(ImageModelObservationConvention observationConvention) {
		Assert.notNull(observationConvention, "observationConvention 不能为 null");
		this.observationConvention = observationConvention;
	}

	/**
	 * 运行时显式配置了非空列表则采用；否则采用默认选项中的列表；均为空则返回 {@code null}（请求侧使用 TEXT+IMAGE）。
	 */
	private List<String> mergeResponseModalities(GoogleGenAiImageOptions runtime) {
		if (runtime != null && runtime.getResponseModalities() != null && !runtime.getResponseModalities().isEmpty()) {
			return new ArrayList<>(runtime.getResponseModalities());
		}
		if (this.defaultOptions.getResponseModalities() != null && !this.defaultOptions.getResponseModalities().isEmpty()) {
			return new ArrayList<>(this.defaultOptions.getResponseModalities());
		}
		return null;
	}

}
