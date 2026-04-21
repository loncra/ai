package io.github.loncra.ai.turbo.seedance.video.domian.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 资源组信息。
 *
 * @author maurice.chen
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TurboSeedanceVideoResourceAssetGroup implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private String officialId;

	@JsonProperty("id")
	private Long internalId;

	private String name;

	private String region;

	private String customerId;

	private Instant createdAt;

	public TurboSeedanceVideoResourceAssetGroup() {
	}

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

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
}
