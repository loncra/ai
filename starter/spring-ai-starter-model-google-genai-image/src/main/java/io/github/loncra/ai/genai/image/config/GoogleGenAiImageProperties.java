package io.github.loncra.ai.genai.image.config;

import io.github.loncra.ai.genai.image.GoogleGenAiImageModel;
import io.github.loncra.ai.genai.image.GoogleGenAiImageOptions;
import io.github.loncra.ai.genai.image.enumerate.GeminiImageModel;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * {@link GoogleGenAiImageModel} 的配置属性。
 *
 * @author maurice.chen
 */
@ConfigurationProperties(prefix = "spring.ai.google.genai.image")
public class GoogleGenAiImageProperties {

	private boolean enabled;

	private String model = GoogleGenAiImageOptions.DEFAULT_MODEL;

	/**
	 * 可选：预置模型枚举；非空时优先于 {@link #model}（字符串）使用。
	 */
	private GeminiImageModel modelPreset;

	private Integer n;

	private Integer width;

	private Integer height;

	private String responseFormat;

	private String style;

	private String aspectRatio;

	private String imageSize;

	private String personGeneration;

	/**
	 * 可选：覆盖默认的 TEXT+IMAGE；与 {@link com.google.genai.types.GenerateContentConfig#responseModalities} 一致。
	 */
	private List<String> responseModalities;

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public GeminiImageModel getModelPreset() {
		return this.modelPreset;
	}

	public void setModelPreset(GeminiImageModel modelPreset) {
		this.modelPreset = modelPreset;
	}

	public Integer getN() {
		return this.n;
	}

	public void setN(Integer n) {
		this.n = n;
	}

	public Integer getWidth() {
		return this.width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return this.height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public String getResponseFormat() {
		return this.responseFormat;
	}

	public void setResponseFormat(String responseFormat) {
		this.responseFormat = responseFormat;
	}

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

	public List<String> getResponseModalities() {
		return this.responseModalities;
	}

	public void setResponseModalities(List<String> responseModalities) {
		this.responseModalities = responseModalities;
	}

	/**
	 * 转为运行时使用的 {@link GoogleGenAiImageOptions}（含模型、参考图等）。
	 *
	 * @return 图像模型选项
	 */
	public GoogleGenAiImageOptions toImageOptions() {
		String modelId = this.modelPreset != null ? this.modelPreset.getValue() : this.model;
		GoogleGenAiImageOptions.Builder b = GoogleGenAiImageOptions.builder()
			.model(modelId)
			.N(this.n)
			.width(this.width)
			.height(this.height)
			.responseFormat(this.responseFormat)
			.style(this.style)
			.aspectRatio(this.aspectRatio)
			.imageSize(this.imageSize)
			.personGeneration(this.personGeneration)
			.responseModalities(this.responseModalities);

		return b.build();
	}

}
