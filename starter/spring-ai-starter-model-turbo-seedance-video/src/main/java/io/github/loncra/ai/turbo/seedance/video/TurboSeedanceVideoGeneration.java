package io.github.loncra.ai.turbo.seedance.video;

import org.springframework.ai.model.ModelResult;
import org.springframework.ai.model.ResultMetadata;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turbo 渠道 Seedance 视频生成的单条结果，承载任务标识与可选的视频地址。
 *
 * @author maurice.chen
 */
public class TurboSeedanceVideoGeneration implements ModelResult<String> {

	/**
	 * 任务 ID（对应文档 7.x 响应中的 {@code task_id}），提交后即可获得。
	 */
	private final String taskId;

	/**
	 * 状态；非终态时 {@link #videoUrl} 通常为 {@code null}。
	 */
	private final String status;

	/**
	 * 原始状态字符串，保留以便对接未知扩展值。
	 */
	private final String rawStatus;

	/**
	 * 生成模式，{@code metadata.mode}。
	 */
	private final String mode;

	/**
	 * 进度字符串，例如 {@code "50%"}。
	 */
	private final String progress;

	/**
	 * 成功态下的视频代理地址；来自 {@code metadata.url}。
	 */
	private final String videoUrl;

	/**
	 * 本次消费的积分。
	 */
	private final Integer creditsUsed;

	/**
	 * 剩余积分。
	 */
	private final Integer creditsLeft;

	/**
	 * 错误信息（失败态时存在）。
	 */
	private final String errorMessage;

	private final TurboSeedanceResultMetadata metadata;

	private TurboSeedanceVideoGeneration(Builder b) {
		this.taskId = b.taskId;
		this.status = b.status;
		this.rawStatus = b.rawStatus;
		this.mode = b.mode;
		this.progress = b.progress;
		this.videoUrl = b.videoUrl;
		this.creditsUsed = b.creditsUsed;
		this.creditsLeft = b.creditsLeft;
		this.errorMessage = b.errorMessage;
		this.metadata = new TurboSeedanceResultMetadata(b.raw);
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public String getOutput() {
		return this.videoUrl != null ? this.videoUrl : this.taskId;
	}

	@Override
	public ResultMetadata getMetadata() {
		return this.metadata;
	}

	public String getTaskId() {
		return this.taskId;
	}

	public String getStatus() {
		return this.status;
	}

	public String getRawStatus() {
		return this.rawStatus;
	}

	public String getMode() {
		return this.mode;
	}

	public String getProgress() {
		return this.progress;
	}

	public String getVideoUrl() {
		return this.videoUrl;
	}

	public Integer getCreditsUsed() {
		return this.creditsUsed;
	}

	public Integer getCreditsLeft() {
		return this.creditsLeft;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	/**
	 * 基于 {@link ResultMetadata} 的最小实现；透传原始 {@code metadata} JSON 字段便于上层取值。
	 */
	public static class TurboSeedanceResultMetadata implements ResultMetadata {

		private final Map<String, Object> raw;

		public TurboSeedanceResultMetadata(Map<String, Object> raw) {
			this.raw = raw != null ? Collections.unmodifiableMap(new LinkedHashMap<>(raw)) : Collections.emptyMap();
		}

		/**
		 * 返回任务响应 {@code metadata} 字段的原始 Map（不可变）；可能为空。
		 */
		public Map<String, Object> getRaw() {
			return this.raw;
		}

	}

	public static class Builder {

		private String taskId;

		private String status;

		private String rawStatus;

		private String mode;

		private String progress;

		private String videoUrl;

		private Integer creditsUsed;

		private Integer creditsLeft;

		private String errorMessage;

		private Map<String, Object> raw;

		public Builder taskId(String taskId) {
			this.taskId = taskId;
			return this;
		}

		public Builder status(String status) {
			this.status = status;
			return this;
		}

		public Builder rawStatus(String rawStatus) {
			this.rawStatus = rawStatus;
			return this;
		}

		public Builder mode(String mode) {
			this.mode = mode;
			return this;
		}

		public Builder progress(String progress) {
			this.progress = progress;
			return this;
		}

		public Builder videoUrl(String videoUrl) {
			this.videoUrl = videoUrl;
			return this;
		}

		public Builder creditsUsed(Integer creditsUsed) {
			this.creditsUsed = creditsUsed;
			return this;
		}

		public Builder creditsLeft(Integer creditsLeft) {
			this.creditsLeft = creditsLeft;
			return this;
		}

		public Builder errorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
			return this;
		}

		public Builder raw(Map<String, Object> raw) {
			this.raw = raw;
			return this;
		}

		public TurboSeedanceVideoGeneration build() {
			return new TurboSeedanceVideoGeneration(this);
		}

	}

}
