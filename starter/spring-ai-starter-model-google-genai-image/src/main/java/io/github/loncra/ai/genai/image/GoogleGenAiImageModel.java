package io.github.loncra.ai.genai.image;

import com.google.genai.Client;
import com.google.genai.types.*;
import io.github.loncra.ai.genai.image.domain.metadata.GoogleGenAiImageGenerationMetadata;
import io.github.loncra.ai.genai.image.enumerate.GeminiImageModel;
import io.github.loncra.framework.commons.CastUtils;
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

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
				for (int i = 0; i < n; i++) {
					GenerateContentResponse raw = this.retryTemplate
						.execute(ctx -> invokeGenerateContent(modelName, instructions, config, opts));
					List<ImageGeneration> chunk = toImageGenerations(raw);
					generations.addAll(chunk);
					if (chunk.isEmpty()) {
						raw.promptFeedback().ifPresent(pf -> logger.warn("Gemini 提示反馈：{}", pf));
						logger.warn("模型 {} 的 Gemini 图像响应中未解析到 inline 图像数据", modelName);
					}
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
	 * 将 {@link GenerateContentResponse} 中的 inline 图像解析为 Spring AI {@link ImageGeneration} 列表。
	 */
	public static List<ImageGeneration> toImageGenerations(GenerateContentResponse raw) {
		List<ImageGeneration> generations = new ArrayList<>();
		if (!Objects.requireNonNull(raw.parts()).isEmpty()) {
			for (Part p : raw.parts()) {
				addInlineImage(generations, p);
			}
			return generations;
		}
		raw.candidates().ifPresent(candidates -> {
			for (Candidate c : candidates) {
				c.content().flatMap(Content::parts).ifPresent(parts -> {
                    for (Part p : parts) {
                        addInlineImage(generations, p);
                    }
                });
			}
		});
		return generations;
	}

	private static void addInlineImage(List<ImageGeneration> generations, Part p) {
		if (p.inlineData().isPresent()) {
			Blob blob = p.inlineData().get();
			if (blob.data().filter(b -> b.length > 0).isPresent()) {
				generations.add(blobToGeneration(blob));
				return;
			}
		}
		p.text()
                .filter(StringUtils::isNotEmpty)
                .ifPresent(text -> appendImagesFromMarkdownDataUrls(text, generations));
	}

	/**
	 * 从 Markdown 图片语法中解析 {@code data:image/...;base64,...}，写入 Spring AI {@link ImageGeneration}（b64 串与 inline 路径一致）。
	 */
	private static void appendImagesFromMarkdownDataUrls(String text, List<ImageGeneration> generations) {
		Matcher m = MARKDOWN_DATA_URL_IMAGE.matcher(text);
		while (m.find()) {
			String mime = m.group(1).trim();
			String b64 = m.group(2).trim().replaceAll("\\s+", StringUtils.EMPTY);
			if (!mime.startsWith("image/") || !StringUtils.isNotEmpty(b64)) {
				continue;
			}
			ImageGenerationMetadata meta = new GoogleGenAiImageGenerationMetadata(MediaType.valueOf(mime));
			generations.add(new ImageGeneration(new Image(null, b64), meta));
		}
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
