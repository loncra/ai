package io.github.loncra.ai.turbo.seedance.video.domian.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.loncra.ai.turbo.seedance.video.enumerate.TurboSeedanceVideoResourceStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 素材详情。
 *
 * @author maurice.chen
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TurboSeedanceVideoResourceAsset implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private String officialId;

	@JsonProperty("id")
	private Long internalId;

	private String groupId;

	private String name;

	private String url;

	private String imageUrl;

	private TurboSeedanceVideoResourceStatus status;

	private Instant createdAt;

	public void setOfficialId(String officialId) {
		this.officialId = officialId;
	}

	public String getOfficialId() {
		return this.officialId;
	}

	public Long getInternalId() {
		return this.internalId;
	}

	public void setInternalId(Long internalId) {
		this.internalId = internalId;
	}

	public String getGroupId() {
		return this.groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getImageUrl() {
		return this.imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public TurboSeedanceVideoResourceStatus getStatus() {
		return this.status;
	}

	public void setStatus(TurboSeedanceVideoResourceStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

}
