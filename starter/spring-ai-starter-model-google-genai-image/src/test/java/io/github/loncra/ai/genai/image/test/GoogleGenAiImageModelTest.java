package io.github.loncra.ai.genai.image.test;

import com.google.genai.types.*;
import io.github.loncra.ai.genai.image.GoogleGenAiImageModel;
import io.github.loncra.ai.genai.image.domain.metadata.GoogleGenAiImageGenerationMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageGenerationMetadata;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GoogleGenAiImageModelTest {

	@Test
	void toImageGenerationsReadsCandidateInlineData() {
		byte[] png = new byte[] { (byte) 0x89, 0x50 };
		GenerateContentResponse raw = GenerateContentResponse.builder()
			.candidates(List.of(Candidate.builder()
				.content(Content.fromParts(Part.builder()
					.inlineData(Blob.builder().data(png).mimeType("image/png").build())
					.build()))
				.build()))
			.build();

		List<ImageGeneration> generations = GoogleGenAiImageModel.toImageGenerations(raw);
		assertEquals(1, generations.size());
		assertNotNull(generations.getFirst().getOutput().getB64Json());
		assertNull(generations.getFirst().getOutput().getUrl());
		ImageGenerationMetadata meta = generations.getFirst().getMetadata();
		assertInstanceOf(GoogleGenAiImageGenerationMetadata.class, meta);
		assertEquals("image/png", ((GoogleGenAiImageGenerationMetadata) meta).getMimeType().toString());
	}

	@Test
	void toImageGenerationsInfersMimeFromPngMagicWhenBlobMimeMissing() {
		byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
		GenerateContentResponse raw = GenerateContentResponse.builder()
			.candidates(List.of(Candidate.builder()
				.content(Content.fromParts(Part.builder()
					.inlineData(Blob.builder().data(pngMagic).build())
					.build()))
				.build()))
			.build();

		List<ImageGeneration> generations = GoogleGenAiImageModel.toImageGenerations(raw);
		assertEquals(1, generations.size());
		ImageGenerationMetadata meta = generations.getFirst().getMetadata();
		assertInstanceOf(GoogleGenAiImageGenerationMetadata.class, meta);
		assertEquals("image/png", ((GoogleGenAiImageGenerationMetadata) meta).getMimeType().toString());
	}

	@Test
	void toImageGenerationsReadsMarkdownDataUrlInTextWhenNoInlineData() {
		String text = "说明文字\n\n![image](data:image/png;base64,QUJDREVG)";
		GenerateContentResponse raw = GenerateContentResponse.builder()
			.candidates(List.of(Candidate.builder()
				.content(Content.fromParts(Part.fromText(text)))
				.build()))
			.build();

		List<ImageGeneration> generations = GoogleGenAiImageModel.toImageGenerations(raw);
		assertEquals(1, generations.size());
		assertEquals("QUJDREVG", generations.getFirst().getOutput().getB64Json());
		assertNull(generations.getFirst().getOutput().getUrl());
		ImageGenerationMetadata meta = generations.getFirst().getMetadata();
		assertInstanceOf(GoogleGenAiImageGenerationMetadata.class, meta);
		assertEquals("image/png", ((GoogleGenAiImageGenerationMetadata) meta).getMimeType().toString());
	}

}
