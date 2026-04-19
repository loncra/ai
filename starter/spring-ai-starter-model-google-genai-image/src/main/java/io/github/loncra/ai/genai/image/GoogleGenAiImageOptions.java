package io.github.loncra.ai.genai.image;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.genai.types.Part;
import com.google.genai.types.SafetySetting;
import io.github.loncra.ai.genai.image.enumerate.GeminiImageModel;
import org.springframework.ai.image.ImageOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Google Gemini 原生图像生成（Nano Banana 系列）的选项。
 *
 * <p>默认模型见 {@link GeminiImageModel#NANO_BANANA}；也可通过 {@link Builder#model(GeminiImageModel)} 传参选用其它枚举值，
 * 或直接 {@link #setModel(String)} 传入官方完整模型 ID（含预览/未来新型号）。
 *
 * <p>需要<strong>参考图 / 图生图 / 编辑</strong>时，设置 {@link #setReferenceImages(List)}（见 {@link Part}），
 * 请求将使用多模态 {@code Content}（文本 + 图片 parts）调用 API；无参考图时仍为纯文本 {@code generateContent}。
 *
 * @author maurice.chen
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoogleGenAiImageOptions implements ImageOptions {

	/** 与 {@link GeminiImageModel#NANO_BANANA} 一致。 */
	public static final String DEFAULT_MODEL = GeminiImageModel.NANO_BANANA.getValue();

	@JsonProperty("n")
	private Integer n;

	@JsonProperty("model")
	private String model;

	@JsonProperty("size_width")
	private Integer width;

	@JsonProperty("size_height")
	private Integer height;

	@JsonProperty("response_format")
	private String responseFormat;

	@JsonProperty("style")
	private String style;

	/** 宽高比，对应 {@link com.google.genai.types.ImageConfig}，例如 {@code "16:9"}。 */
	@JsonProperty("aspect_ratio")
	private String aspectRatio;

	/** 图像尺寸预设，对应 {@link com.google.genai.types.ImageConfig}（以 API 约定为准）。 */
	@JsonProperty("image_size")
	private String imageSize;

	@JsonProperty("person_generation")
	private String personGeneration;

	/**
	 * 与官方文档一致：图像生成需在 {@link com.google.genai.types.GenerateContentConfig} 中声明响应模态。
	 * 为 {@code null} 或空列表时，由 {@link GeminiImageModel} 使用默认 {@code TEXT} + {@code IMAGE}。
	 */
	@JsonProperty("response_modalities")
	private List<String> responseModalities;

	/**
	 * 参考图（与提示词一并发送）；非空时走多模态请求。上限见 {@link GeminiImageModel#maxReferenceImagesForModelId(String)}。
	 */
	private List<Part> referenceImages;

	/**
	 * 安全拦截设置
	 */
	private List<SafetySetting> safetySettings;

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * 合并参考图：运行时显式传入非 null 的列表时采用运行时（含空列表表示不传参考图）；否则采用默认选项中的列表。
	 */
	static List<Part> mergeReferenceImages(GoogleGenAiImageOptions runtime,
	                                       GoogleGenAiImageOptions defaults) {
		if (runtime != null && runtime.referenceImages != null) {
			return new ArrayList<>(runtime.referenceImages);
		}
		if (defaults != null && defaults.referenceImages != null) {
			return new ArrayList<>(defaults.referenceImages);
		}
		return null;
	}

	public static GoogleGenAiImageOptions fromOptions(GoogleGenAiImageOptions from) {
		GoogleGenAiImageOptions o = new GoogleGenAiImageOptions();
		o.n = from.n;
		o.model = from.model;
		o.width = from.width;
		o.height = from.height;
		o.responseFormat = from.responseFormat;
		o.style = from.style;
		o.aspectRatio = from.aspectRatio;
		o.imageSize = from.imageSize;
		o.personGeneration = from.personGeneration;
		if (from.responseModalities != null) {
			o.responseModalities = new ArrayList<>(from.responseModalities);
		}
		if (from.referenceImages != null) {
			o.referenceImages = new ArrayList<>(from.referenceImages);
		}
		if (from.safetySettings != null) {
			o.safetySettings = new ArrayList<>(from.safetySettings);
		}
		return o;
	}

	@Override
	public Integer getN() {
		return this.n;
	}

	public void setN(Integer n) {
		this.n = n;
	}

	@Override
	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Override
	public Integer getWidth() {
		return this.width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	@Override
	public Integer getHeight() {
		return this.height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	@Override
	public String getResponseFormat() {
		return this.responseFormat;
	}

	public void setResponseFormat(String responseFormat) {
		this.responseFormat = responseFormat;
	}

	@Override
	public String getStyle() {
		return this.style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getAspectRatio() {
		return this.aspectRatio;
	}

	public void setAspectRatio(String aspectRatio) {
		this.aspectRatio = aspectRatio;
	}

	public String getImageSize() {
		return this.imageSize;
	}

	public void setImageSize(String imageSize) {
		this.imageSize = imageSize;
	}

	public String getPersonGeneration() {
		return this.personGeneration;
	}

	public void setPersonGeneration(String personGeneration) {
		this.personGeneration = personGeneration;
	}

	/**
	 * @return 显式配置的响应模态；{@code null} 表示使用模型内置默认（本 Starter 在请求中会填入 {@code TEXT}+{@code IMAGE}）
	 */
	public List<String> getResponseModalities() {
		return this.responseModalities;
	}

	public void setResponseModalities(List<String> responseModalities) {
		this.responseModalities = responseModalities == null ? null : new ArrayList<>(responseModalities);
	}

	public List<Part> getReferenceImages() {
		return this.referenceImages == null ? Collections.emptyList() : Collections.unmodifiableList(this.referenceImages);
	}

	public void setReferenceImages(List<Part> referenceImages) {
		this.referenceImages = referenceImages == null ? null : new ArrayList<>(referenceImages);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof GoogleGenAiImageOptions that)) {
			return false;
		}
		return Objects.equals(this.n, that.n) && Objects.equals(this.model, that.model)
				&& Objects.equals(this.width, that.width) && Objects.equals(this.height, that.height)
				&& Objects.equals(this.responseFormat, that.responseFormat) && Objects.equals(this.style, that.style)
				&& Objects.equals(this.aspectRatio, that.aspectRatio) && Objects.equals(this.imageSize, that.imageSize)
				&& Objects.equals(this.personGeneration, that.personGeneration)
				&& Objects.equals(this.responseModalities, that.responseModalities)
				&& Objects.equals(this.referenceImages, that.referenceImages);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.n, this.model, this.width, this.height, this.responseFormat, this.style,
				this.aspectRatio, this.imageSize, this.personGeneration, this.responseModalities, this.referenceImages);
	}

	@Override
	public String toString() {
		return "GoogleGenAiImageOptions{" + "n=" + this.n + ", model='" + this.model + '\'' + ", width="
				+ this.width + ", height=" + this.height + ", responseFormat='" + this.responseFormat + '\''
				+ ", style='" + this.style + '\'' + ", aspectRatio='" + this.aspectRatio + '\'' + ", imageSize='"
				+ this.imageSize + '\'' + ", personGeneration='" + this.personGeneration + '\''
				+ ", responseModalities=" + this.responseModalities
				+ ", referenceImages=" + (this.referenceImages == null ? 0 : this.referenceImages.size()) + " refs}";
	}

	/**
	 * {@link GoogleGenAiImageOptions} 的构建器。
	 *
	 * @author maurice.chen
	 */
	public static final class Builder {

		private final GoogleGenAiImageOptions options = new GoogleGenAiImageOptions();

		public Builder() {
		}

		public Builder(GoogleGenAiImageOptions options) {
			this.options.n = options.n;
			this.options.model = options.model;
			this.options.width = options.width;
			this.options.height = options.height;
			this.options.responseFormat = options.responseFormat;
			this.options.style = options.style;
			this.options.aspectRatio = options.aspectRatio;
			this.options.imageSize = options.imageSize;
			this.options.personGeneration = options.personGeneration;
			this.options.responseModalities = options.responseModalities == null ? null
					: new ArrayList<>(options.responseModalities);
			this.options.referenceImages = options.referenceImages == null ? null : new ArrayList<>(options.referenceImages);
		}

		public Builder N(Integer n) {
			this.options.setN(n);
			return this;
		}

		public Builder model(String model) {
			this.options.setModel(model);
			return this;
		}

		/**
		 * 使用预置 {@link GeminiImageModel}，等价于 {@link #model(String)} 传入对应 {@link GeminiImageModel#getValue()}。
		 */
		public Builder model(GeminiImageModel preset) {
			this.options.setModel(preset != null ? preset.getValue() : null);
			return this;
		}

		public Builder width(Integer width) {
			this.options.setWidth(width);
			return this;
		}

		public Builder height(Integer height) {
			this.options.setHeight(height);
			return this;
		}

		public Builder responseFormat(String responseFormat) {
			this.options.setResponseFormat(responseFormat);
			return this;
		}

		public Builder style(String style) {
			this.options.setStyle(style);
			return this;
		}

		public Builder aspectRatio(String aspectRatio) {
			this.options.setAspectRatio(aspectRatio);
			return this;
		}

		public Builder imageSize(String imageSize) {
			this.options.setImageSize(imageSize);
			return this;
		}

		public Builder personGeneration(String personGeneration) {
			this.options.setPersonGeneration(personGeneration);
			return this;
		}

		/**
		 * 覆盖默认的 {@code TEXT}+{@code IMAGE}；部分场景仅需 {@code IMAGE}，参见官方文档。
		 */
		public Builder responseModalities(List<String> responseModalities) {
			this.options.setResponseModalities(responseModalities);
			return this;
		}

		public Builder referenceImages(List<Part> referenceImages) {
			this.options.setReferenceImages(referenceImages);
			return this;
		}

		public Builder safetySettings(List<SafetySetting> safetySettings) {
			this.options.setSafetySettings(safetySettings);
			return this;
		}

		public Builder addSafetySetting(SafetySetting safetySetting) {
			if (safetySetting == null) {
				return this;
			}
			if (this.options.safetySettings == null) {
				this.options.safetySettings = new ArrayList<>();
			}
			this.options.safetySettings.add(safetySetting);
			return this;
		}

		public Builder addReferenceImage(Part image) {
			if (image == null) {
				return this;
			}
			if (this.options.referenceImages == null) {
				this.options.referenceImages = new ArrayList<>();
			}
			this.options.referenceImages.add(image);
			return this;
		}

		public GoogleGenAiImageOptions build() {
			return this.options;
		}

	}

	public List<SafetySetting> getSafetySettings() {
		return safetySettings;
	}

	public void setSafetySettings(List<SafetySetting> safetySettings) {
		this.safetySettings = safetySettings;
	}
}
