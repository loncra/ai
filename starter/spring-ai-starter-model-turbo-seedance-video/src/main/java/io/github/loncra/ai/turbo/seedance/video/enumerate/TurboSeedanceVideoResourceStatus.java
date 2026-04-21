package io.github.loncra.ai.turbo.seedance.video.enumerate;

import io.github.loncra.framework.commons.enumerate.NameEnum;

/**
 * Turbo Seedance 素材库资源状态。
 *
 * @author maurice.chen
 */
public enum TurboSeedanceVideoResourceStatus implements NameEnum {

	PROCESSING("Processing"),

	ACTIVE("Active"),

	FAILED("Failed");

	private final String name;

	TurboSeedanceVideoResourceStatus(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

}
