package io.github.loncra.ai.genai.image.enumerate;


import io.github.loncra.ai.genai.image.GoogleGenAiImageOptions;
import io.github.loncra.framework.commons.enumerate.NameValueEnum;

import java.util.Locale;
import java.util.Optional;

/**
 * Google Gemini <strong>原生图像生成</strong>（文档中的 Nano Banana 系列）常用模型 ID。
 *
 * <p>名称与 ID 以
 * <a href="https://ai.google.dev/gemini-api/docs/models">Gemini API 模型列表</a>
 * 为准；预览类模型可能变更或下线，生产环境请固定具体 {@link #getValue()} 并关注官方弃用公告。
 *
 * <p>另有不属于本枚举命名空间的模型（例如 Imagen 系列走不同能力），请直接使用 {@link GoogleGenAiImageOptions#getModel()}  传入官方字符串。
 *
 * @author maurice.chen
 */
public enum GeminiImageModel implements NameValueEnum<String> {

	/**
	 * Nano Banana（Gemini 2.5 Flash Image）—— 默认推荐，偏速度与成本。
	 */
	NANO_BANANA("gemini-2.5-flash-image", "Nano Banana"),

	/**
	 * Nano Banana 2（Gemini 3.1 Flash Image 预览）—— 偏高效与批量场景。
	 */
	NANO_BANANA_2("gemini-3.1-flash-image-preview", "Nano Banana 2"),

	/**
	 * Nano Banana Pro（Gemini 3 Pro Image 预览）—— 偏质量、复杂版式与高精度文字等。
	 */
	NANO_BANANA_PRO("gemini-3-pro-image-preview", "Nano Banana Pro"),

	;

	private final String value;

	private final String name;

	GeminiImageModel(String value, String name) {
		this.value = value;
		this.name = name;
	}

	/**
	 * 调用 {@code generateContent} 时使用的模型名字符串（与 {@link NameValueEnum} 的 value 一致）。
	 */
	@Override
	public String getValue() {
		return this.value;
	}

	/**
	 * 文档/营销中的称呼（与 {@link NameValueEnum} 的 name 一致）。
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * 根据任意模型 ID 给出参考图数量上限（用于自定义 {@code model} 字符串时的预检）。
	 */
	public static int maxReferenceImagesForModelId(String modelId) {
		if (modelId == null || modelId.isBlank()) {
			return 0;
		}
		String m = modelId.toLowerCase(Locale.ROOT);
		if (m.contains(NANO_BANANA_2.getValue())) {
			return 14;
		} else if (m.contains(NANO_BANANA_PRO.getValue())) {
			return 11;
		}

		return 0;
	}

	/**
	 * 按官方模型 ID 解析（忽略大小写、首尾空白）；未知 ID 返回 empty（可再配合自定义 {@code model} 字符串）。
	 */
	public static Optional<GeminiImageModel> fromModelId(String id) {
		if (id == null) {
			return Optional.empty();
		}
		String t = id.trim();
		if (t.isEmpty()) {
			return Optional.empty();
		}
		for (GeminiImageModel m : values()) {
			if (m.getValue().equalsIgnoreCase(t)) {
				return Optional.of(m);
			}
		}
		return Optional.empty();
	}

	/**
	 * 按枚举常量名解析（如 {@code NANO_BANANA_2}），忽略大小写。
	 */
	public static Optional<GeminiImageModel> fromEnumName(String enumConstantName) {
		if (enumConstantName == null || enumConstantName.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(GeminiImageModel.valueOf(enumConstantName.trim().toUpperCase(Locale.ROOT)));
		}
		catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
	}

}
