package io.github.loncra.ai.genai.image.domain.metadata;

import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageGenerationMetadata;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;

import java.util.Objects;

/**
 * Google Gemini 原生图像（Nano Banana）单张结果的元数据，供 {@link ImageGeneration#getMetadata()} 使用。
 *
 * @author maurice.chen
 */
public class GoogleGenAiImageGenerationMetadata implements ImageGenerationMetadata {
	private final MediaType mimeType;

	public GoogleGenAiImageGenerationMetadata(MediaType mimeType) {
		this.mimeType = Objects.requireNonNullElse(mimeType, MediaType.APPLICATION_OCTET_STREAM);
	}

	/**
	 * @return 图像 MIME，例如 {@code image/png}、{@code image/jpeg}
	 */
	public MediaType getMimeType() {
		return this.mimeType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof GoogleGenAiImageGenerationMetadata)) {
			return false;
		}
		return Objects.equals(this.mimeType, ((GoogleGenAiImageGenerationMetadata) o).getMimeType());
	}

	@Override
	public @NonNull String toString() {
		return "GoogleGenAiNanoBananaImageGenerationMetadata{mimeType='" + this.mimeType + '\'' + '}';
	}

}
