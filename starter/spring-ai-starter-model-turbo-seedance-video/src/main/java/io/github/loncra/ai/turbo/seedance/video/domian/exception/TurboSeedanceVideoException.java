package io.github.loncra.ai.turbo.seedance.video.domian.exception;

import org.springframework.http.HttpStatusCode;

import java.io.Serial;

/**
 * Turbo 渠道 Seedance 视频对接异常。
 *
 * @author maurice.chen
 */
public class TurboSeedanceVideoException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	private final HttpStatusCode httpStatus;

	private final String taskId;

	private final String rawBody;

	public TurboSeedanceVideoException(String message) {
		this(message, null, null, null, null);
	}

	public TurboSeedanceVideoException(String message, Throwable cause) {
		this(message, cause, null, null, null);
	}

	public TurboSeedanceVideoException(String message, Throwable cause, HttpStatusCode httpStatus, String taskId,
			String rawBody) {
		super(message, cause);
		this.httpStatus = httpStatus;
		this.taskId = taskId;
		this.rawBody = rawBody;
	}

	public HttpStatusCode getHttpStatus() {
		return this.httpStatus;
	}

	public String getTaskId() {
		return this.taskId;
	}

	public String getRawBody() {
		return this.rawBody;
	}

}
