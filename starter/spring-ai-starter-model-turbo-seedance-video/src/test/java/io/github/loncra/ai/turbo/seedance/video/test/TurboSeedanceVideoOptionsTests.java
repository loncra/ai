package io.github.loncra.ai.turbo.seedance.video.test;

import io.github.loncra.ai.turbo.seedance.video.TurboSeedanceVideoOptions;
import io.github.loncra.ai.turbo.seedance.video.domian.metadata.TurboSeedanceVideoContent;
import io.github.loncra.ai.turbo.seedance.video.enumerate.TurboSeedanceVideoMode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TurboSeedanceVideoOptionsTests {

	@Test
	void inferModeTextToVideoWhenNoAssets() {
		TurboSeedanceVideoOptions opts = TurboSeedanceVideoOptions.builder().model("seedance-2").build();
		assertEquals(TurboSeedanceVideoMode.TEXT_TO_VIDEO, opts.inferMode());
	}

	@Test
	void inferModeImageToVideoWithSingleImage() {
		TurboSeedanceVideoOptions opts = TurboSeedanceVideoOptions.builder().model("seedance-2")
			.image("https://example.com/a.png")
			.build();
		assertEquals(TurboSeedanceVideoMode.IMAGE_TO_VIDEO, opts.inferMode());
	}

	@Test
	void inferModeFirstLastFrameWithTwoImages() {
		TurboSeedanceVideoOptions opts = TurboSeedanceVideoOptions.builder()
			.model("seedance-2")
			.images(List.of("https://example.com/a.png", "https://example.com/b.png"))
			.build();
		assertEquals(TurboSeedanceVideoMode.FIRST_LAST_FRAME, opts.inferMode());
	}

	@Test
	void inferModeMultiRefWithThreeImages() {
		TurboSeedanceVideoOptions opts = TurboSeedanceVideoOptions.builder()
			.model("seedance-2")
			.images(List.of("https://example.com/a.png", "https://example.com/b.png", "https://example.com/c.png"))
			.build();
		assertEquals(TurboSeedanceVideoMode.MULTI_REF, opts.inferMode());
	}

	@Test
	void inferModeMultiRefWhenAudioPresent() {
		TurboSeedanceVideoOptions opts = TurboSeedanceVideoOptions.builder()
			.model("seedance-2")
			.image("https://example.com/a.png")
			.content(List.of(TurboSeedanceVideoContent.audioUrl("https://example.com/a.mp3")))
			.build();
		assertEquals(TurboSeedanceVideoMode.MULTI_REF, opts.inferMode());
	}


	@Test
	void validateAllowsDocSampleTextToVideo() {
		TurboSeedanceVideoOptions opts = TurboSeedanceVideoOptions.builder()
			.model("seedance-2")
			.duration(4)
			.ratio("16:9")
			.resolution("720")
			.build();
		opts.setMode(TurboSeedanceVideoMode.TEXT_TO_VIDEO);
		opts.validate("夕阳下奔跑的猎豹，电影级镜头感，慢动作");
	}

	@Test
	void validateRejectsEmptyPrompt() {
		TurboSeedanceVideoOptions opts = TurboSeedanceVideoOptions.builder().model("seedance-2").build();
		assertThrows(IllegalArgumentException.class, () -> opts.validate(""));
	}

	@Test
	void validateRejectsOutOfRangeDuration() {
		TurboSeedanceVideoOptions opts = TurboSeedanceVideoOptions.builder().model("seedance-2").duration(3).build();
		assertThrows(IllegalArgumentException.class, () -> opts.validate("测试"));
	}

	@Test
	void validateRejectsUnknownModel() {
		TurboSeedanceVideoOptions opts = TurboSeedanceVideoOptions.builder().model("seedance-3").build();
		assertThrows(IllegalArgumentException.class, () -> opts.validate("测试"));
	}

	@Test
	void firstLastFrameRequiresExactlyTwoImages() {
		TurboSeedanceVideoOptions opts = TurboSeedanceVideoOptions.builder()
			.model("seedance-2")
			.image("https://example.com/a.png")
			.build();
		opts.setMode(TurboSeedanceVideoMode.FIRST_LAST_FRAME);
		assertThrows(IllegalArgumentException.class, () -> opts.validate("测试"));
	}

}
