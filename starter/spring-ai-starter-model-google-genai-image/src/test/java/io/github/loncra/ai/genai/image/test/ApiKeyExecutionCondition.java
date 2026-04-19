package io.github.loncra.ai.genai.image.test;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 从 classpath {@code /application.yml} 读取 {@code spring.ai.google.genai.api-key}（与
 * {@link org.springframework.ai.model.google.genai.autoconfigure.chat.GoogleGenAiConnectionProperties} 一致），
 * 未填写或占位符时跳过联调类，避免 Spring 上下文因缺少密钥启动失败。
 *
 * @author maurice.chen
 */
public final class ApiKeyExecutionCondition implements ExecutionCondition {

	private static final List<String> PLACEHOLDERS = List.of("CHANGE_ME", "YOUR_API_KEY_HERE", "YOUR_GEMINI_API_KEY", "FILL_ME");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		String key = resolveApiKeyForCondition();
		if (!StringUtils.hasText(key)) {
			return ConditionEvaluationResult.disabled(
					"请在 src/test/resources/application.yml 中配置 spring.ai.google.genai.api-key，或设置环境变量 GEMINI_API_KEY（或 SPRING_AI_GOOGLE_GENAI_API_KEY）");
		}
		String t = key.trim();
		if (PLACEHOLDERS.contains(t.toUpperCase(Locale.ROOT))) {
			return ConditionEvaluationResult.disabled("请将 api-key 从占位符改为真实密钥后再运行联调测试");
		}
		return ConditionEvaluationResult.enabled("已检测到 api-key");
	}

	/**
	 * YAML 由 SnakeYaml 直接解析，不会展开 {@code ${...}}；若占位则回退到与 Spring Boot 一致的环境变量。
	 */
	static String resolveApiKeyForCondition() {
		String key = readApiKeyFromClasspathApplicationYml();
		if (StringUtils.hasText(key) && !looksLikeUnresolvedPropertyPlaceholder(key)) {
			return key;
		}
		String fromEnv = firstNonEmptyEnv("GEMINI_API_KEY", "SPRING_AI_GOOGLE_GENAI_API_KEY");
		return StringUtils.hasText(fromEnv) ? fromEnv : null;
	}

	private static String firstNonEmptyEnv(String... names) {
		for (String n : names) {
			String v = System.getenv(n);
			if (StringUtils.hasText(v)) {
				return v.trim();
			}
		}
		return null;
	}

	private static boolean looksLikeUnresolvedPropertyPlaceholder(String raw) {
		if (raw == null) {
			return false;
		}
		String t = raw.trim();
		return t.startsWith("${") && t.contains("}");
	}

	@SuppressWarnings("unchecked")
	static String readApiKeyFromClasspathApplicationYml() {
		try (InputStream is = ApiKeyExecutionCondition.class.getResourceAsStream("/application.yml")) {
			if (is == null) {
				return null;
			}
			Map<String, Object> root = new Yaml().load(is);
			if (root == null) {
				return null;
			}
			Object spring = root.get("spring");
			if (!(spring instanceof Map)) {
				return null;
			}
			Object ai = ((Map<String, Object>) spring).get("ai");
			if (!(ai instanceof Map)) {
				return null;
			}
			Object google = ((Map<String, Object>) ai).get("google");
			if (!(google instanceof Map)) {
				return null;
			}
			Object genai = ((Map<String, Object>) google).get("genai");
			if (!(genai instanceof Map)) {
				return null;
			}
			Map<String, Object> genaiMap = (Map<String, Object>) genai;
			Object apiKey = genaiMap.get("api-key");
			if (apiKey != null) {
				return String.valueOf(apiKey).trim();
			}
			// 兼容旧写法：spring.ai.google.genai.image.nano-banana.api-key
			Object image = genaiMap.get("image");
			if (image instanceof Map) {
				Object nanoBanana = ((Map<String, Object>) image).get("nano-banana");
				if (nanoBanana instanceof Map) {
					Object legacy = ((Map<String, Object>) nanoBanana).get("api-key");
					return legacy == null ? null : String.valueOf(legacy).trim();
				}
			}
			return null;
		}
		catch (Exception ex) {
			return null;
		}
	}

}
