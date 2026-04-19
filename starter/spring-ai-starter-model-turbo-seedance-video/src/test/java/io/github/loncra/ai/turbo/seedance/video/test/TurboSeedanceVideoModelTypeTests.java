package io.github.loncra.ai.turbo.seedance.video.test;

import io.github.loncra.ai.turbo.seedance.video.enumerate.TurboSeedanceVideoMode;
import io.github.loncra.ai.turbo.seedance.video.enumerate.TurboSeedanceVideoModelType;
import io.github.loncra.ai.turbo.seedance.video.enumerate.TurboSeedanceVideoStatus;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import io.github.loncra.framework.commons.exception.NameEnumNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TurboSeedanceVideoModelTypeTests {

	@Test
	void getValueMatchesDocValue() {
		assertEquals("seedance-2", TurboSeedanceVideoModelType.SEEDANCE_2.getName());
		assertEquals("seedance-2-fast", TurboSeedanceVideoModelType.SEEDANCE_2_FAST.getName());
	}

	@Test
	void ofRoundTrip() {
		for (TurboSeedanceVideoModelType type : TurboSeedanceVideoModelType.values()) {
			assertEquals(type, NameEnum.ofEnum(TurboSeedanceVideoModelType.class, type.getName()));
		}
	}

	@Test
	void ofUnknownThrows() {
		assertThrows(
				NameEnumNotFoundException.class,
				() -> NameEnum.ofEnum(TurboSeedanceVideoModelType.class, "seedance-3"));
	}

	@Test
	void modeAndStatusRoundTrip() {
		for (TurboSeedanceVideoMode m : TurboSeedanceVideoMode.values()) {
			assertEquals(m, NameEnum.ofEnum(TurboSeedanceVideoMode.class, m.getName()));
		}
		for (TurboSeedanceVideoStatus s : TurboSeedanceVideoStatus.values()) {
			assertEquals(s, ValueEnum.ofEnum(TurboSeedanceVideoStatus.class, s.getValue()));
		}
	}

}
