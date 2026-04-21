package io.github.loncra.ai.turbo.seedance.video.domian.body;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.loncra.framework.commons.id.BasicIdentification;

import java.io.Serial;
import java.time.Instant;

/**
 * Turbo 渠道 Seedance 视频任务通用响应体；提交 / 查询 / 拉取内容均复用此结构。
 *
 * 提交成功：{@code id, task_id, model, status=submitted, created_at}
 *
 * @author maurice.chen
 */
public class TurboSeedanceVideoSubmitResponse implements BasicIdentification<String> {

	@Serial
	private static final long serialVersionUID = 1L;

	private String id;

	private String model;

	private String status;

	@JsonProperty("created_at")
	private Instant creationTime;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
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

	public Instant getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Instant creationTime) {
		this.creationTime = creationTime;
	}
}
