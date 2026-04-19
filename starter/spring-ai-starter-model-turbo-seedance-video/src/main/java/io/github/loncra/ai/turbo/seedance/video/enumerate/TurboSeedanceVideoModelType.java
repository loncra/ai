package io.github.loncra.ai.turbo.seedance.video.enumerate;

import io.github.loncra.framework.commons.enumerate.NameEnum;

/**
 * Turbo 渠道 Seedance 视频生成支持的模型枚举。
 * <p>
 * 参考文档（{@code src/test/resources/readme.html} 第 2.1 节）：{@code seedance-2} 与 {@code seedance-2-fast}。
 *
 * @author maurice.chen
 */
public enum TurboSeedanceVideoModelType implements NameEnum {

	/**
	 * 画面质量更高，适合精细场景。
	 */
	SEEDANCE_2("seedance-2"),

	/**
	 * 出片更快，适合快速预览或大批量需求。
	 */
	SEEDANCE_2_FAST("seedance-2-fast");

	private final String name;

	TurboSeedanceVideoModelType(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
