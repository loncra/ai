package io.github.loncra.ai.turbo.seedance.video;

import io.github.loncra.ai.turbo.seedance.video.enumerate.TurboSeedanceVideoMode;
import org.springframework.ai.model.ModelRequest;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Turbo 渠道 Seedance 视频生成 {@link ModelRequest}。
 * <p>
 * 指令为视频描述（{@code prompt}）；选项通过 {@link TurboSeedanceVideoOptions} 承载模型、时长、素材等参数。
 *
 * @author maurice.chen
 */
public class TurboSeedanceVideoPrompt implements ModelRequest<String> {

	private final String instructions;

	private final TurboSeedanceVideoOptions options;

	public TurboSeedanceVideoPrompt(String instructions) {
		this(instructions, null);
	}

	public TurboSeedanceVideoPrompt(String instructions, TurboSeedanceVideoOptions options) {
		Assert.hasText(instructions, "prompt 不能为空");
		this.instructions = instructions;
		this.options = options;
	}

	/**
	 * 便捷构造：文本 + 单张图片，适合 {@link TurboSeedanceVideoMode#IMAGE_TO_VIDEO}。
	 */
	/*public static TurboSeedanceVideoPrompt ofImage(String instructions, String imageUrl) {
		TurboSeedanceVideoOptions opts = TurboSeedanceVideoOptions.builder().image(imageUrl).build();
		return new TurboSeedanceVideoPrompt(instructions, opts);
	}*/

	/**
	 * 便捷构造：文本 + 多张图片，适合 {@link TurboSeedanceVideoMode#FIRST_LAST_FRAME} 或 {@link TurboSeedanceVideoMode#MULTI_REF}。
	 */
	/*public static TurboSeedanceVideoPrompt ofImages(String instructions, String... imageUrls) {
		List<String> list = imageUrls != null ? new ArrayList<>(Arrays.asList(imageUrls)) : Collections.emptyList();
		TurboSeedanceVideoOptions opts = TurboSeedanceVideoOptions.builder().images(list).build();
		return new TurboSeedanceVideoPrompt(instructions, opts);
	}*/

	@Override
	public String getInstructions() {
		return this.instructions;
	}

	@Override
	public TurboSeedanceVideoOptions getOptions() {
		return this.options;
	}

}
