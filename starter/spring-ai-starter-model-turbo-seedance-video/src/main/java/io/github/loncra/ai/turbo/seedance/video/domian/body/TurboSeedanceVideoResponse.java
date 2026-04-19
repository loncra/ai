package io.github.loncra.ai.turbo.seedance.video.domian.body;

import io.github.loncra.ai.turbo.seedance.video.TurboSeedanceVideoGeneration;
import io.github.loncra.ai.turbo.seedance.video.domian.metadata.TurboSeedanceVideoResponseMetadata;
import org.springframework.ai.model.ModelResponse;

import java.util.Collections;
import java.util.List;

/**
 * Turbo 渠道 Seedance 视频响应（{@link ModelResponse}）。
 * <p>
 * 目前每次请求（提交 / 查询 / 拉取内容）均对应一条 {@link TurboSeedanceVideoGeneration}。
 *
 * @author maurice.chen
 */
public class TurboSeedanceVideoResponse implements ModelResponse<TurboSeedanceVideoGeneration> {

	private final TurboSeedanceVideoGeneration result;

	private final TurboSeedanceVideoResponseMetadata metadata;

	public TurboSeedanceVideoResponse(TurboSeedanceVideoGeneration result,
			TurboSeedanceVideoResponseMetadata metadata) {
		this.result = result;
		this.metadata = metadata != null ? metadata : new TurboSeedanceVideoResponseMetadata();
	}

	@Override
	public TurboSeedanceVideoGeneration getResult() {
		return this.result;
	}

	@Override
	public List<TurboSeedanceVideoGeneration> getResults() {
		return this.result != null ? Collections.singletonList(this.result) : Collections.emptyList();
	}

	@Override
	public TurboSeedanceVideoResponseMetadata getMetadata() {
		return this.metadata;
	}

}
