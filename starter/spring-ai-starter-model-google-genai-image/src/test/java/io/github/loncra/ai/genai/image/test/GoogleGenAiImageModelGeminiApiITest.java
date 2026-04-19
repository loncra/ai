package io.github.loncra.ai.genai.image.test;

import io.github.loncra.ai.genai.image.GoogleGenAiImageModel;
import io.github.loncra.ai.genai.image.GoogleGenAiImageOptions;
import io.github.loncra.ai.genai.image.enumerate.GeminiImageModel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 使用真实 Gemini 开发者 API 的联调测试：依赖 {@code spring.ai.google.genai.api-key}（见
 * {@code src/test/resources/application.yml} 或环境变量 {@code GEMINI_API_KEY}）；未填写时整类跳过（不启动 Spring 容器）。
 * <p>
 * 本地运行：在 {@code application.yml} 填入密钥后执行；对 {@link GeminiImageModel} 三个模型各调用一次。
 * {@code mvn test -pl ai/spring-ai-starter-google-genai-nano-banana-image -Dtest=GoogleGenAiImageModelGeminiApiIT}。
 *
 * @author maurice.chen
 */
@Tag("integration")
@ExtendWith(ApiKeyExecutionCondition.class)
@SpringBootTest(classes = GoogleGenaiImageApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class GoogleGenAiImageModelGeminiApiITest {

	@Autowired
	private GoogleGenAiImageModel googleGenAiImageModel;

	@ParameterizedTest
	@EnumSource(GeminiImageModel.class)
	void textToImageReturnsAtLeastOneBase64Image(GeminiImageModel modelPreset) {
		var options = GoogleGenAiImageOptions.builder().model(modelPreset).build();
		ImageResponse response = this.googleGenAiImageModel
			.call(new ImagePrompt("极简图标：一个蓝色小方块，白底，无文字", options));
		assertNotNull(response);
		assertFalse(response.getResults().isEmpty(),
				"模型 " + modelPreset.getValue() + " 应至少返回一张图；若失败请检查配额、模型可用区与密钥权限");
		ImageGeneration first = response.getResults().getFirst();
		assertNotNull(first.getOutput());
		assertTrue(StringUtils.hasText(first.getOutput().getB64Json()), "响应应为 Base64 图像数据");
	}

	/*@Test
	void safetyImage(*//*GeminiNativeImageModel modelPreset*//*) throws IOException {
		var options = GoogleGenAiImageOptions.builder().model(GeminiNativeImageModel.NANO_BANANA_PRO).build();
		options.setSafetySettings(
				List.of(
						SafetySetting.builder()
								.category(HarmCategory.Known.HARM_CATEGORY_HARASSMENT)
								.threshold(HarmBlockThreshold.Known.OFF)
								.build(),
						SafetySetting.builder()
								.category(HarmCategory.Known.HARM_CATEGORY_HATE_SPEECH)
								.threshold(HarmBlockThreshold.Known.OFF)
								.build(),
						SafetySetting.builder()
								.category(HarmCategory.Known.HARM_CATEGORY_SEXUALLY_EXPLICIT)
								.threshold(HarmBlockThreshold.Known.OFF)
								.build(),
						SafetySetting.builder()
								.category(HarmCategory.Known.HARM_CATEGORY_DANGEROUS_CONTENT)
								.threshold(HarmBlockThreshold.Known.OFF)
								.build(),
						SafetySetting.builder()
								.category(HarmCategory.Known.HARM_CATEGORY_IMAGE_DANGEROUS_CONTENT)
								.threshold(HarmBlockThreshold.Known.OFF)
								.build(),
						SafetySetting.builder()
								.category(HarmCategory.Known.HARM_CATEGORY_IMAGE_HARASSMENT)
								.threshold(HarmBlockThreshold.Known.OFF)
								.build()
				)
		);

		options.setReferenceImages(List.of(
				Part.fromBytes(new ClassPathResource("图1.png").getContentAsByteArray(), MediaType.IMAGE_PNG_VALUE),
				Part.fromBytes(new ClassPathResource("图2.png").getContentAsByteArray(), MediaType.IMAGE_PNG_VALUE),
				Part.fromBytes(new ClassPathResource("图3.jpg").getContentAsByteArray(), MediaType.IMAGE_JPEG_VALUE),
				Part.fromBytes(new ClassPathResource("17aa7bbb22dc92f75856f499a20fcb79.png").getContentAsByteArray(), MediaType.IMAGE_PNG_VALUE)
		));
		ClassPathResource prompt = new ClassPathResource("prompt.text");
		ImageResponse response = this.googleGenAiImageModel
				.call(new ImagePrompt(prompt.getContentAsString(Charset.defaultCharset()), options));
		assertNotNull(response);
		assertFalse(response.getResults().isEmpty(),
				"模型 " + GeminiNativeImageModel.NANO_BANANA_PRO.getValue() + " 应至少返回一张图；若失败请检查配额、模型可用区与密钥权限");
		ImageGeneration first = response.getResults().getFirst();
		assertNotNull(first.getOutput());
		assertTrue(StringUtils.hasText(first.getOutput().getB64Json()), "响应应为 Base64 图像数据");
	}*/
}
