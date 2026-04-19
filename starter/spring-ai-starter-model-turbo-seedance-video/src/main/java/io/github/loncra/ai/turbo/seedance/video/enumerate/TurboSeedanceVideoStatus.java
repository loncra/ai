package io.github.loncra.ai.turbo.seedance.video.enumerate;


import io.github.loncra.framework.commons.enumerate.NameValueEnum;

/**
 * Turbo 渠道 Seedance 视频任务状态。
 * <p>
 * 对应文档（{@code src/test/resources/readme.html} 第 7.4 节）：submitted / processing / succeeded / failed。
 *
 * @author maurice.chen
 */
public enum TurboSeedanceVideoStatus implements NameValueEnum<String> {

	/**
	 * 已提交，等待处理。
	 */
	SUBMITTED("已提交", "submitted"),

	/**
	 * 生成中。
	 */
	PROCESSING("生成中", "processing"),

	/**
	 * 生成成功。
	 */
	SUCCEEDED("生成成功", "succeeded"),

	/**
	 * 生成失败。
	 */
	FAILED("生成失败", "failed");

	private final String name;

	private final String value;

	TurboSeedanceVideoStatus(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * 判断当前状态是否为终态（成功或失败）。
	 */
	public boolean isTerminal() {
		return this == SUCCEEDED || this == FAILED;
	}

}
