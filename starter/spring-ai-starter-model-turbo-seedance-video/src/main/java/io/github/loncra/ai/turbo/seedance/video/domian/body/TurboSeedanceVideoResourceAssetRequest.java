package io.github.loncra.ai.turbo.seedance.video.domian.body;

import java.io.Serial;
import java.io.Serializable;

/**
 * 单素材创建请求。
 *
 * @author maurice.chen
 */
public class TurboSeedanceVideoResourceAssetRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private String groupId;

	private String url;

	private String name;

	public String getGroupId() {
		return this.groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
