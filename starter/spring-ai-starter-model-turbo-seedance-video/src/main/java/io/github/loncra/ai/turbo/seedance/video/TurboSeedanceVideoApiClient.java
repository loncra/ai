package io.github.loncra.ai.turbo.seedance.video;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.loncra.ai.turbo.seedance.video.domian.body.TurboSeedanceVideoErrorResponse;
import io.github.loncra.ai.turbo.seedance.video.domian.body.TurboSeedanceVideoQueryResponse;
import io.github.loncra.ai.turbo.seedance.video.domian.body.TurboSeedanceVideoSubmitResponse;
import io.github.loncra.ai.turbo.seedance.video.domian.exception.TurboSeedanceVideoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Turbo 渠道 Seedance 视频底层 HTTP 客户端，封装如下三段式接口：
 * <ul>
 * <li>{@code POST /v1/videos}（multipart/form-data）—— 提交任务</li>
 * <li>{@code GET  /v1/videos/{task_id}}            —— 查询任务状态</li>
 * <li>{@code GET  /v1/videos/{task_id}/content}    —— 获取视频代理地址</li>
 * </ul>
 * 全部请求自动附加 {@code Authorization: Bearer <api-key>} 以及可选的自定义请求头。
 *
 * @author maurice.chen
 */
public class TurboSeedanceVideoApiClient {

	private static final Logger logger = LoggerFactory.getLogger(TurboSeedanceVideoApiClient.class);

	/** 文档示例中的占位路径，用于替换任务 ID。 */
	private static final String TASK_ID_PLACEHOLDER = "{task_id}";

	private final RestClient restClient;

	private final ObjectMapper objectMapper;

	private final String apiKey;

	private final String baseUrl;

	private final String submitPath;

	private final String queryPath;

	private final String contentPath;

	private final boolean useAuthorizationBearer;

	private final Map<String, String> extraHeaders;

	public TurboSeedanceVideoApiClient(String apiKey, String baseUrl, String submitPath, String queryPath,
			String contentPath, Duration connectTimeout, Duration readTimeout, boolean useAuthorizationBearer,
			Map<String, String> extraHeaders, RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
		Assert.hasText(baseUrl, "baseUrl 不能为空");
		Assert.notNull(objectMapper, "objectMapper 不能为 null");
		this.apiKey = apiKey;
		this.baseUrl = stripTrailingSlash(baseUrl);
		this.submitPath = defaultIfBlank(submitPath, "/v1/videos");
		this.queryPath = defaultIfBlank(queryPath, "/v1/videos/{task_id}");
		this.contentPath = defaultIfBlank(contentPath, "/v1/videos/{task_id}/content");
		this.useAuthorizationBearer = useAuthorizationBearer;
		this.extraHeaders = extraHeaders;
		this.objectMapper = objectMapper;
		RestClient.Builder builder = restClientBuilder != null ? restClientBuilder : RestClient.builder();
		this.restClient = builder.baseUrl(this.baseUrl)
			.requestFactory(buildRequestFactory(connectTimeout, readTimeout))
			.build();
	}

	/**
	 * 提交视频生成任务。
	 *
	 * @param options 生成选项（已完成 merge / 推断 / 校验）
	 * @param prompt 视频描述
	 * @return 接口响应，对应文档 7.1
	 */
	public TurboSeedanceVideoSubmitResponse submit(TurboSeedanceVideoOptions options, String prompt) {
		Assert.notNull(options, "options 不能为 null");
		Assert.hasText(prompt, "prompt 不能为空");
		MultiValueMap<String, Object> form = buildSubmitForm(options, prompt);
		try {
			ResponseEntity<String> response = this.restClient.post()
				.uri(this.submitPath)
				.headers(this::applyAuthHeaders)
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(form)
				.retrieve()
				.toEntity(String.class);
			return parseResponse(response, null);
		}
		catch (TurboSeedanceVideoException e) {
			throw e;
		}
		catch (Exception e) {
			throw new TurboSeedanceVideoException("提交 Turbo Seedance 视频任务失败：" + e.getMessage(), e);
		}
	}

	/**
	 * 查询任务状态。
	 *
	 * @param taskId 任务 ID
	 * @return 接口响应
	 */
	public TurboSeedanceVideoQueryResponse query(String taskId) {
		Assert.hasText(taskId, "taskId 不能为空");
		String path = this.queryPath.replace(TASK_ID_PLACEHOLDER,
				URLEncoder.encode(taskId, StandardCharsets.UTF_8));
		return doGet(path, taskId);
	}

	/**
	 * 拉取任务生成后的视频代理地址。
	 *
	 * @param taskId 任务 ID
	 * @return 接口响应
	 */
	public TurboSeedanceVideoQueryResponse content(String taskId) {
		Assert.hasText(taskId, "taskId 不能为空");
		String path = this.contentPath.replace(TASK_ID_PLACEHOLDER,
				URLEncoder.encode(taskId, StandardCharsets.UTF_8));
		return doGet(path, taskId);
	}

	private TurboSeedanceVideoQueryResponse doGet(String path, String taskId) {
		try {
			ResponseEntity<String> response = this.restClient.get()
				.uri(path)
				.headers(this::applyAuthHeaders)
				.retrieve()
				.toEntity(String.class);
			return parseResponse(response, taskId);
		}
		catch (TurboSeedanceVideoException e) {
			throw e;
		}
		catch (Exception e) {
			throw new TurboSeedanceVideoException("请求 Turbo Seedance 视频任务失败：" + e.getMessage(), e);
		}
	}

	private MultiValueMap<String, Object> buildSubmitForm(TurboSeedanceVideoOptions options, String prompt) {
		MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
		form.add("model", options.getModel());
		form.add("prompt", prompt);
		if (options.getDuration() != null) {
			form.add("duration", options.getDuration());
		}
		if (options.getMode() != null) {
			form.add("mode", options.getMode().getName());
		}
		if (StringUtils.hasText(options.getRatio())) {
			form.add("ratio", options.getRatio());
		}
		/*if (StringUtils.hasText(options.getImage())) {
			form.add("image", options.getImage());
		}
		List<String> images = options.getImages();*/
		/*if (images != null) {
			for (String url : images) {
				if (StringUtils.hasText(url)) {
					form.add("images", url);
				}
			}
		}*/
		TurboSeedanceVideoOptions.Metadata metadata = options.getMetadata();
		if (metadata != null) {
			try {
				String json = this.objectMapper.writeValueAsString(metadata);
				if (StringUtils.hasText(json) && !"{}".equals(json)) {
					form.add("metadata", json);
				}
			}
			catch (JsonProcessingException e) {
				throw new TurboSeedanceVideoException("序列化 metadata 失败：" + e.getMessage(), e);
			}
		}
		return form;
	}

	private TurboSeedanceVideoQueryResponse parseResponse(ResponseEntity<String> response, String taskId) {
		HttpStatusCode status = response.getStatusCode();
		String body = response.getBody();
		if (!status.is2xxSuccessful()) {
			throw buildErrorException(status, body, taskId);
		}
		if (!StringUtils.hasText(body)) {
			return new TurboSeedanceVideoQueryResponse();
		}
		try {
			return this.objectMapper.readValue(body, TurboSeedanceVideoQueryResponse.class);
		}
		catch (JsonProcessingException e) {
			throw new TurboSeedanceVideoException(
					"解析 Turbo Seedance 视频响应失败：" + e.getMessage() + "；原始响应：" + body, e, status, taskId, body);
		}
	}

	private TurboSeedanceVideoException buildErrorException(HttpStatusCode status, String body, String taskId) {
		String message = "Turbo Seedance 视频接口返回非 2xx 状态：" + status.value();
		if (StringUtils.hasText(body)) {
			try {
				TurboSeedanceVideoErrorResponse error = this.objectMapper.readValue(body,
						TurboSeedanceVideoErrorResponse.class);
				String resolved = error.resolveMessage();
				if (StringUtils.hasText(resolved)) {
					message = message + "，" + resolved;
				}
			}
			catch (Exception ignored) {
				message = message + "，响应：" + body;
			}
		}
		logger.warn("Turbo Seedance 视频接口失败：status={}, taskId={}, body={}", status, taskId, body);
		return new TurboSeedanceVideoException(message, null, status, taskId, body);
	}

	private void applyAuthHeaders(HttpHeaders headers) {
		if (this.useAuthorizationBearer && StringUtils.hasText(this.apiKey)
				&& !headers.containsKey(HttpHeaders.AUTHORIZATION)) {
			headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + this.apiKey);
		}
		if (this.extraHeaders != null) {
			this.extraHeaders.forEach(headers::set);
		}
	}

	private static ClientHttpRequestFactory buildRequestFactory(Duration connectTimeout, Duration readTimeout) {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		if (connectTimeout != null) {
			factory.setConnectTimeout((int) connectTimeout.toMillis());
		}
		if (readTimeout != null) {
			factory.setReadTimeout((int) readTimeout.toMillis());
		}
		return factory;
	}

	private static String stripTrailingSlash(String url) {
		if (url == null || url.isEmpty()) {
			return url;
		}
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}

	private static String defaultIfBlank(String value, String fallback) {
		return StringUtils.hasText(value) ? value : fallback;
	}

	public String getBaseUrl() {
		return this.baseUrl;
	}

	public String getSubmitPath() {
		return this.submitPath;
	}

	public String getQueryPath() {
		return this.queryPath;
	}

	public String getContentPath() {
		return this.contentPath;
	}

}
