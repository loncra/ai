package io.github.loncra.ai.turbo.seedance.video.domian.body;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turbo 渠道 Seedance 视频任务通用响应体；提交 / 查询 / 拉取内容均复用此结构，差异字段通过 {@link #metadata} 透传。
 * <p>
 * 对应文档（{@code src/test/resources/readme.html} 第 7 节）：
 * <ul>
 * <li>7.1 提交成功：{@code id, task_id, model, status=submitted, created_at}</li>
 * <li>7.2 进行中：{@code status=processing, progress}</li>
 * <li>7.3 完成：{@code status=succeeded, metadata: { url, mode, credits_used, credits_left }}</li>
 * </ul>
 *
 * @author maurice.chen
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TurboSeedanceVideoSubmitResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private String id;

	@JsonProperty("task_id")
	private String taskId;

	private String model;

	private String status;

	private String progress;

	@JsonProperty("created_at")
	private Long createdAt;

	/**
	 * 完成态的元数据字段（{@code url / mode / credits_used / credits_left ...}），直接以 Map 保留，
	 * 便于对接文档未列出的扩展字段。
	 */
	private Map<String, Object> metadata;

	/**
	 * 错误信息，不同渠道可能返回 {@code error} / {@code message} 等字段；这里统一拿 {@code error}。
	 */
	private Object error;

	/**
	 * 其它未匹配字段，经由 {@link JsonAnySetter} 兜底收集。
	 */
	private final Map<String, Object> additional = new LinkedHashMap<>();

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTaskId() {
		return this.taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getProgress() {
		return this.progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

	public Long getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Long createdAt) {
		this.createdAt = createdAt;
	}

	public Map<String, Object> getMetadata() {
		return this.metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public Object getError() {
		return this.error;
	}

	public void setError(Object error) {
		this.error = error;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditional() {
		return this.additional;
	}

	@JsonAnySetter
	public void add(String name, Object value) {
		this.additional.put(name, value);
	}

}
