package io.github.loncra.ai.turbo.seedance.video.domian.body;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 资源组列表响应。
 *
 * @author maurice.chen
 */
public class TurboSeedanceVideoResourceAssetGroupListResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private List<TurboSeedanceVideoResourceAssetGroup> value = new ArrayList<>();

	@JsonProperty("Count")
	private Integer count = 0;

	public List<TurboSeedanceVideoResourceAssetGroup> getValue() {
		return this.value;
	}

	public TurboSeedanceVideoResourceAssetGroupListResponse() {
	}

	public void setValue(List<TurboSeedanceVideoResourceAssetGroup> value) {
		this.value = value != null ? value : new ArrayList<>();
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
}
