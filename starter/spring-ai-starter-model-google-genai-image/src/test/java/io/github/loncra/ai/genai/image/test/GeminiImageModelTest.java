package io.github.loncra.ai.genai.image.test;

import io.github.loncra.ai.genai.image.enumerate.GeminiImageModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 仅校验三个官方模型 ID 与枚举一致，且 {@link io.github.loncra.ai.genai.image.enumerate.GeminiImageModel#fromModelId(String)} 可解析。
 *
 * @author maurice.chen
 */
class GeminiImageModelTest {

	@Test
	void threeOfficialModelIdsMatchDocumentation() {
		assertEquals("gemini-2.5-flash-image", GeminiImageModel.NANO_BANANA.getValue());
		assertEquals("gemini-3.1-flash-image-preview", GeminiImageModel.NANO_BANANA_2.getValue());
		assertEquals("gemini-3-pro-image-preview", GeminiImageModel.NANO_BANANA_PRO.getValue());
	}

	@ParameterizedTest
	@EnumSource(GeminiImageModel.class)
	void fromModelIdResolvesEachOfficialId(GeminiImageModel expected) {
		assertEquals(Optional.of(expected), GeminiImageModel.fromModelId(expected.getValue()));
		assertEquals(Optional.of(expected),
				GeminiImageModel.fromModelId("  " + expected.getValue().toUpperCase(Locale.ROOT) + "  "));
	}

}
