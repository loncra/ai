package io.github.loncra.ai.turbo.seedance.video.domian.body;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建资源组请求。
 *
 * @author maurice.chen
 */
public class TurboSeedanceVideoResourceAssetGroupRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private String name;

	private String description;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
