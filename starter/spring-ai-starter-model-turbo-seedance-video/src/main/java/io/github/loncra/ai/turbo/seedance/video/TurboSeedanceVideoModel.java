package io.github.loncra.ai.turbo.seedance.video;

import io.github.loncra.ai.turbo.seedance.video.domian.body.TurboSeedanceVideoResponse;
import io.github.loncra.ai.turbo.seedance.video.domian.body.TurboSeedanceVideoSubmitResponse;
import io.github.loncra.ai.turbo.seedance.video.domian.metadata.TurboSeedanceVideoResponseMetadata;
import io.github.loncra.ai.turbo.seedance.video.enumerate.TurboSeedanceVideoStatus;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.Model;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Map;

/**
 * Turbo 渠道 Seedance 视频生成 {@link Model} 实现。
 * <p>
 * 本实现遵循 Spring AI {@link Model} 规范：{@link #call(TurboSeedanceVideoPrompt)} 负责提交任务并返回
 * {@code task_id}；由于 Seedance 属于异步任务，结果获取通过 {@link #query(String)} 与 {@link #content(String)}
 * 单独暴露，便于调用方按业务节奏轮询。
 *
 * @author maurice.chen
 */
public class TurboSeedanceVideoModel implements Model<TurboSeedanceVideoPrompt, TurboSeedanceVideoResponse> {

	private static final Logger logger = LoggerFactory.getLogger(TurboSeedanceVideoModel.class);

	public static final String PROVIDER_NAME = "turbo-seedance";

	private final TurboSeedanceVideoApiClient apiClient;

	private final TurboSeedanceVideoOptions defaultOptions;

	private final RetryTemplate retryTemplate;

	public TurboSeedanceVideoModel(TurboSeedanceVideoApiClient apiClient) {
		this(apiClient, TurboSeedanceVideoOptions.builder().build(), new RetryTemplate());
	}

	public TurboSeedanceVideoModel(TurboSeedanceVideoApiClient apiClient, TurboSeedanceVideoOptions defaultOptions,
			RetryTemplate retryTemplate) {
		Assert.notNull(apiClient, "apiClient 不能为 null");
		Assert.notNull(defaultOptions, "defaultOptions 不能为 null");
		Assert.notNull(retryTemplate, "retryTemplate 不能为 null");
		this.apiClient = apiClient;
		this.defaultOptions = defaultOptions;
		this.retryTemplate = retryTemplate;
	}

	/**
	 * 提交一次视频生成任务，仅返回含有 {@code task_id} 的响应；请用 {@link #query(String)} 与
	 * {@link #content(String)} 继续跟踪结果。
	 */
	@Override
	public TurboSeedanceVideoResponse call(TurboSeedanceVideoPrompt prompt) {
		Assert.notNull(prompt, "prompt 不能为 null");
		String instructions = prompt.getInstructions();
		Assert.hasText(instructions, "prompt 文本不能为空");

		TurboSeedanceVideoOptions runtime = mergeOptions(prompt.getOptions());
		runtime.setMode(runtime.inferMode());
		runtime.validate(instructions);

		TurboSeedanceVideoSubmitResponse raw = this.retryTemplate
			.execute(ctx -> this.apiClient.submit(runtime, instructions));
		logger.debug("提交 Turbo Seedance 视频任务成功：taskId={}, status={}", raw.getTaskId(), raw.getStatus());
		return toResponse(raw);
	}

	/**
	 * 查询视频任务状态。
	 *
	 * @param taskId 任务 ID
	 */
	public TurboSeedanceVideoResponse query(String taskId) {
		Assert.hasText(taskId, "taskId 不能为空");
		TurboSeedanceVideoSubmitResponse raw = this.retryTemplate.execute(ctx -> this.apiClient.query(taskId));
		return toResponse(raw);
	}

	/**
	 * 获取视频代理地址（通常在 {@link TurboSeedanceVideoStatus#SUCCEEDED} 后调用）。
	 *
	 * @param taskId 任务 ID
	 */
	public TurboSeedanceVideoResponse content(String taskId) {
		Assert.hasText(taskId, "taskId 不能为空");
		TurboSeedanceVideoSubmitResponse raw = this.retryTemplate.execute(ctx -> this.apiClient.content(taskId));
		return toResponse(raw);
	}

	private TurboSeedanceVideoOptions mergeOptions(TurboSeedanceVideoOptions runtimeOptions) {
		TurboSeedanceVideoOptions merged = TurboSeedanceVideoOptions.fromOptions(this.defaultOptions);
		if (runtimeOptions == null) {
			return merged;
		}
		merged.setModel(ModelOptionsUtils.mergeOption(runtimeOptions.getModel(), merged.getModel()));
		merged.setDuration(ModelOptionsUtils.mergeOption(runtimeOptions.getDuration(), merged.getDuration()));
		merged.setMode(ModelOptionsUtils.mergeOption(runtimeOptions.getMode(), merged.getMode()));
		merged.setRatio(ModelOptionsUtils.mergeOption(runtimeOptions.getRatio(), merged.getRatio()));
		merged.setImage(ModelOptionsUtils.mergeOption(runtimeOptions.getImage(), merged.getImage()));
		merged.setImages(ModelOptionsUtils.mergeOption(runtimeOptions.getImages(), merged.getImages()));
		merged.setMetadata(mergeMetadata(runtimeOptions.getMetadata(), merged.getMetadata()));
		return merged;
	}

	private static TurboSeedanceVideoOptions.Metadata mergeMetadata(TurboSeedanceVideoOptions.Metadata runtime,
			TurboSeedanceVideoOptions.Metadata defaults) {
		if (runtime == null && defaults == null) {
			return null;
		}
		TurboSeedanceVideoOptions.Metadata merged = defaults != null ? TurboSeedanceVideoOptions.Metadata
			.fromMetadata(defaults) : new TurboSeedanceVideoOptions.Metadata();
		if (runtime == null) {
			return merged;
		}
		merged.setResolution(ModelOptionsUtils.mergeOption(runtime.getResolution(), merged.getResolution()));
		merged.setRatio(ModelOptionsUtils.mergeOption(runtime.getRatio(), merged.getRatio()));
		merged.setWebhookUrl(ModelOptionsUtils.mergeOption(runtime.getWebhookUrl(), merged.getWebhookUrl()));
		merged.setCallbackUrl(ModelOptionsUtils.mergeOption(runtime.getCallbackUrl(), merged.getCallbackUrl()));
		merged.setContent(ModelOptionsUtils.mergeOption(runtime.getContent(), merged.getContent()));
		merged.setGenerateAudio(ModelOptionsUtils.mergeOption(runtime.getGenerateAudio(), merged.getGenerateAudio()));
		merged.setWatermark(ModelOptionsUtils.mergeOption(runtime.getWatermark(), merged.getWatermark()));
		merged.setExtra(ModelOptionsUtils.mergeOption(runtime.getExtra(), merged.getExtra()));
		return merged;
	}

	private TurboSeedanceVideoResponse toResponse(TurboSeedanceVideoSubmitResponse raw) {
		if (raw == null) {
			return new TurboSeedanceVideoResponse(null, new TurboSeedanceVideoResponseMetadata());
		}
		TurboSeedanceVideoStatus status = ValueEnum.ofEnum(TurboSeedanceVideoStatus.class, raw.getStatus());
		Map<String, Object> metadata = raw.getMetadata();
		String videoUrl = metadata != null && metadata.get("url") instanceof String s ? s : null;
		String mode = metadata != null && metadata.get("mode") instanceof String s ? s : null;
		Integer creditsUsed = toInteger(metadata != null ? metadata.get("credits_used") : null);
		Integer creditsLeft = toInteger(metadata != null ? metadata.get("credits_left") : null);
		String errorMessage = raw.getError() != null ? raw.getError().toString() : null;

		TurboSeedanceVideoGeneration generation = TurboSeedanceVideoGeneration.builder()
			.taskId(raw.getTaskId() != null ? raw.getTaskId() : raw.getId())
			.status(status)
			.rawStatus(raw.getStatus())
			.mode(mode)
			.progress(raw.getProgress())
			.videoUrl(videoUrl)
			.creditsUsed(creditsUsed)
			.creditsLeft(creditsLeft)
			.errorMessage(errorMessage)
			.raw(metadata)
			.build();

		Instant createdAt = raw.getCreatedAt() != null ? Instant.ofEpochSecond(raw.getCreatedAt()) : null;
		TurboSeedanceVideoResponseMetadata responseMetadata = new TurboSeedanceVideoResponseMetadata(raw.getId(),
				raw.getTaskId() != null ? raw.getTaskId() : raw.getId(), raw.getModel(), status, raw.getStatus(),
				createdAt);
		return new TurboSeedanceVideoResponse(generation, responseMetadata);
	}

	private static Integer toInteger(Object value) {
		if (value instanceof Number n) {
			return n.intValue();
		}
		if (value instanceof String s && !s.isBlank()) {
			try {
				return Integer.parseInt(s.trim());
			}
			catch (NumberFormatException ignored) {
				return null;
			}
		}
		return null;
	}

	public TurboSeedanceVideoOptions getDefaultOptions() {
		return this.defaultOptions;
	}

	public TurboSeedanceVideoApiClient getApiClient() {
		return this.apiClient;
	}

}
