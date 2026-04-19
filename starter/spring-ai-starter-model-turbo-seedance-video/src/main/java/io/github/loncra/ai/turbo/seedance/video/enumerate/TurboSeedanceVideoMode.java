package io.github.loncra.ai.turbo.seedance.video.enumerate;

import io.github.loncra.framework.commons.enumerate.NameEnum;

/**
 * Turbo 渠道 Seedance 视频生成模式。
 * <p>
 * 对应文档（{@code src/test/resources/readme.html} 第 3 节）：
 * <ul>
 * <li>{@link #TEXT_TO_VIDEO} 纯文字生成</li>
 * <li>{@link #IMAGE_TO_VIDEO} 单张图片驱动生成</li>
 * <li>{@link #FIRST_LAST_FRAME} 首尾帧插值生成（恰好 2 张图片）</li>
 * <li>{@link #MULTI_REF} 多参考素材生成（图片 / 视频 / 音频至少一类）</li>
 * </ul>
 * <p>
 * 不传时平台会按第 3 节自动推断。
 *
 * @author maurice.chen
 */
public enum TurboSeedanceVideoMode implements NameEnum {

	TEXT_TO_VIDEO("text_to_video"),

	IMAGE_TO_VIDEO("image_to_video"),

	FIRST_LAST_FRAME("first_last_frame"),

	MULTI_REF("multi_ref");

	private final String name;

	TurboSeedanceVideoMode(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

}
