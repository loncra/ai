package io.github.loncra.ai.turbo.seedance.video.config;

import io.github.loncra.ai.turbo.seedance.video.TurboSeedanceVideoOptions;
import io.github.loncra.ai.turbo.seedance.video.enumerate.TurboSeedanceVideoMode;
import io.github.loncra.ai.turbo.seedance.video.enumerate.TurboSeedanceVideoModelType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;

/**
 * Turbo 渠道 Seedance 视频生成的配置属性，前缀 {@code spring.ai.turbo.seedance.video}。
 * <p>
 * 示例（见 {@code src/test/resources/application.yml}）：
 * <pre>
 * spring:
 *   ai:
 *     turbo:
 *       seedance:
 *         video:
 *           api-key: sk-xxxxx
 * </pre>
 *
 * @author maurice.chen
 */
@ConfigurationProperties(prefix = "spring.ai.turbo.seedance.video")
public class TurboSeedanceVideoProperties {

	/** 默认接入根地址。 */
	public static final String DEFAULT_BASE_URL = "https://turbo.yoofang.com";

	public static final String DEFAULT_SUBMIT_PATH = "/v1/videos";

	public static final String DEFAULT_QUERY_PATH = "/v1/videos/{task_id}";

	public static final String DEFAULT_CONTENT_PATH = "/v1/videos/{task_id}/content";

	public static final String DEFAULT_ASSET_GROUP_PATH = "/turbo_data/asset-groups";

	public static final String DEFAULT_ASSET_PATH = "/turbo_data/assets";

	public static final String DEFAULT_ASSET_BATCH_PATH = "/turbo_data/assets/batch";

	/**
	 * 是否启用 Turbo Seedance 视频自动配置。
	 */
	private boolean enabled = true;

	/**
	 * 接口 API Key，将作为 {@code Authorization: Bearer ...} 发送。
	 */
	private String apiKey;

	/**
	 * HTTP 根地址，默认 {@value #DEFAULT_BASE_URL}。
	 */
	private String baseUrl = DEFAULT_BASE_URL;

	/**
	 * 提交任务路径，默认 {@value #DEFAULT_SUBMIT_PATH}。
	 */
	private String submitPath = DEFAULT_SUBMIT_PATH;

	/**
	 * 查询任务路径，默认 {@value #DEFAULT_QUERY_PATH}。
	 */
	private String queryPath = DEFAULT_QUERY_PATH;

	/**
	 * 拉取内容路径，默认 {@value #DEFAULT_CONTENT_PATH}。
	 */
	private String contentPath = DEFAULT_CONTENT_PATH;

	/**
	 * 资源组接口路径，默认 {@value #DEFAULT_ASSET_GROUP_PATH}。
	 */
	private String assetGroupPath = DEFAULT_ASSET_GROUP_PATH;

	/**
	 * 单素材与素材列表接口路径，默认 {@value #DEFAULT_ASSET_PATH}。
	 */
	private String assetPath = DEFAULT_ASSET_PATH;

	/**
	 * 批量素材接口路径，默认 {@value #DEFAULT_ASSET_BATCH_PATH}。
	 */
	private String assetBatchPath = DEFAULT_ASSET_BATCH_PATH;

	/**
	 * 连接超时，默认 10 秒。
	 */
	private Duration connectTimeout = Duration.ofSeconds(10);

	/**
	 * 读取超时，默认 120 秒；Seedance 提交接口偶发长耗时，保持较宽裕的默认。
	 */
	private Duration readTimeout = Duration.ofSeconds(120);

	/**
	 * 默认模型字符串；若同时配置 {@link #modelType}，后者优先。
	 */
	private String model;

	/**
	 * 模型枚举，优先级高于 {@link #model}。
	 */
	private TurboSeedanceVideoModelType modelType;

	/**
	 * 默认生成模式；不设置时由运行时按素材自动推断。
	 */
	private TurboSeedanceVideoMode mode;

	/**
	 * 默认视频时长（秒，4-15）。
	 */
	private Integer duration;

	/**
	 * 默认宽高比。
	 */
	private String ratio;

	/**
	 * 默认分辨率（480p / 720p）。
	 */
	private String resolution;

	/**
	 * 默认是否生成配音。
	 */
	private Boolean generateAudio;

	/**
	 * 默认是否添加水印。
	 */
	private Boolean watermark;

	/**
	 * 默认 callback 地址（兼容别名）。
	 */
	private String callbackUrl;

	/**
	 * HTTP 相关扩展配置（鉴权头、自定义头等）。
	 */
	private Http http = new Http();

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getApiKey() {
		return this.apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getBaseUrl() {
		return this.baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getSubmitPath() {
		return this.submitPath;
	}

	public void setSubmitPath(String submitPath) {
		this.submitPath = submitPath;
	}

	public String getQueryPath() {
		return this.queryPath;
	}

	public void setQueryPath(String queryPath) {
		this.queryPath = queryPath;
	}

	public String getContentPath() {
		return this.contentPath;
	}

	public void setContentPath(String contentPath) {
		this.contentPath = contentPath;
	}

	public String getAssetGroupPath() {
		return this.assetGroupPath;
	}

	public void setAssetGroupPath(String assetGroupPath) {
		this.assetGroupPath = assetGroupPath;
	}

	public String getAssetPath() {
		return this.assetPath;
	}

	public void setAssetPath(String assetPath) {
		this.assetPath = assetPath;
	}

	public String getAssetBatchPath() {
		return this.assetBatchPath;
	}

	public void setAssetBatchPath(String assetBatchPath) {
		this.assetBatchPath = assetBatchPath;
	}

	public Duration getConnectTimeout() {
		return this.connectTimeout;
	}

	public void setConnectTimeout(Duration connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public Duration getReadTimeout() {
		return this.readTimeout;
	}

	public void setReadTimeout(Duration readTimeout) {
		this.readTimeout = readTimeout;
	}

	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public TurboSeedanceVideoModelType getModelType() {
		return this.modelType;
	}

	public void setModelType(TurboSeedanceVideoModelType modelType) {
		this.modelType = modelType;
	}

	public TurboSeedanceVideoMode getMode() {
		return this.mode;
	}

	public void setMode(TurboSeedanceVideoMode mode) {
		this.mode = mode;
	}

	public Integer getDuration() {
		return this.duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public String getRatio() {
		return this.ratio;
	}

	public void setRatio(String ratio) {
		this.ratio = ratio;
	}

	public String getResolution() {
		return this.resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public Boolean getGenerateAudio() {
		return this.generateAudio;
	}

	public void setGenerateAudio(Boolean generateAudio) {
		this.generateAudio = generateAudio;
	}

	public Boolean getWatermark() {
		return this.watermark;
	}

	public void setWatermark(Boolean watermark) {
		this.watermark = watermark;
	}

	public String getCallbackUrl() {
		return this.callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	public Http getHttp() {
		return this.http;
	}

	public void setHttp(Http http) {
		this.http = http != null ? http : new Http();
	}

	/**
	 * 基于当前配置构造运行时默认 {@link TurboSeedanceVideoOptions}。
	 *
	 * @return 已设置好默认值的 options
	 */
	public TurboSeedanceVideoOptions toOptions() {
		String resolvedModel = this.modelType != null ? this.modelType.getName() : this.model;
		TurboSeedanceVideoOptions.Builder builder = TurboSeedanceVideoOptions.builder()
			.model(resolvedModel)
			.duration(this.duration)
			.mode(this.mode)
			.ratio(this.ratio);
		boolean hasMetadata = false;
		if (this.resolution != null) {
			builder.resolution(this.resolution);
			hasMetadata = true;
		}
		if (this.generateAudio != null) {
			builder.generateAudio(this.generateAudio);
			hasMetadata = true;
		}
		if (this.watermark != null) {
			builder.watermark(this.watermark);
			hasMetadata = true;
		}
		if (this.callbackUrl != null && !this.callbackUrl.isBlank()) {
			builder.callbackUrl(this.callbackUrl);
			hasMetadata = true;
		}
		TurboSeedanceVideoOptions options = builder.build();
		if (!hasMetadata && options.getMetadata() != null) {
			options.setMetadata(null);
		}
		return options;
	}

	/**
	 * HTTP 相关扩展配置。
	 */
	public static class Http {

		/**
		 * 是否自动附加 {@code Authorization: Bearer &lt;api-key&gt;}，默认 {@code true}。
		 */
		private boolean useAuthorizationBearer = true;

		/**
		 * 自定义附加的 HTTP 请求头。
		 */
		private Map<String, String> headers;

		public boolean isUseAuthorizationBearer() {
			return this.useAuthorizationBearer;
		}

		public void setUseAuthorizationBearer(boolean useAuthorizationBearer) {
			this.useAuthorizationBearer = useAuthorizationBearer;
		}

		public Map<String, String> getHeaders() {
			return this.headers;
		}

		public void setHeaders(Map<String, String> headers) {
			this.headers = headers;
		}

	}

}
