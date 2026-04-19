package io.github.loncra.ai.genai.image.domain.metadata;

import com.google.genai.types.Blob;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageGenerationMetadata;
import org.springframework.http.MediaType;

import java.util.Objects;

/**
 * Google Gemini 原生图像（Nano Banana）单张结果的元数据，供 {@link ImageGeneration#getMetadata()} 使用。
 * <p>
 * MIME 来自 API 的 {@link Blob#mimeType()}，或 Markdown {@code data:&lt;mime&gt;;base64,...}；
 * 若均缺失则根据字节魔数推断，否则为 {@code application/octet-stream}。
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
