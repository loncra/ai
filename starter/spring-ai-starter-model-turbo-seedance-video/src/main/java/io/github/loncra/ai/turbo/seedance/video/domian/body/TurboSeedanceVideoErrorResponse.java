package io.github.loncra.ai.turbo.seedance.video.domian.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Turbo 渠道 Seedance 视频失败响应；文档未给出严格规范，这里兼容常见 OpenAI 风格的错误结构：
 * <pre>
 * { "error": { "message": "...", "type": "...", "code": "..." } }
 * </pre>
 * 也兼容平铺的 {@code { "message": "..." }}。
 *
 * @author maurice.chen
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TurboSeedanceVideoErrorResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private ErrorBody error;

	private String message;

	@JsonProperty("task_id")
	private String taskId;

	public ErrorBody getError() {
		return this.error;
	}

	public void setError(ErrorBody error) {
		this.error = error;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTaskId() {
		return this.taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	/**
	 * 解析出一个用于日志与异常信息的可读消息，依次取 {@code error.message} → {@code error.code} → 顶层 {@code message}。
	 */
	public String resolveMessage() {
		if (this.error != null) {
			if (this.error.getMessage() != null && !this.error.getMessage().isBlank()) {
				return this.error.getMessage();
			}
			if (this.error.getCode() != null && !this.error.getCode().isBlank()) {
				return this.error.getCode();
			}
		}
		return Objects.toString(this.message, "");
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ErrorBody implements Serializable {

		@Serial
		private static final long serialVersionUID = 1L;

		private String message;

		private String type;

		private String code;

		private String param;

		public String getMessage() {
			return this.message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getType() {
			return this.type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getCode() {
			return this.code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getParam() {
			return this.param;
		}

		public void setParam(String param) {
			this.param = param;
		}

	}

}
