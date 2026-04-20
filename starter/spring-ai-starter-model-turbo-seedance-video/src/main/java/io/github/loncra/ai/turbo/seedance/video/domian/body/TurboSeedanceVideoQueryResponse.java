package io.github.loncra.ai.turbo.seedance.video.domian.body;

import io.github.loncra.ai.turbo.seedance.video.domian.metadata.TurboSeedanceVideoContentMetadata;
import io.github.loncra.ai.video.QueryVideoResponse;

public class TurboSeedanceVideoQueryResponse extends TurboSeedanceVideoSubmitResponse implements QueryVideoResponse {

    private String progress;

    private TurboSeedanceVideoContentMetadata metadata;

    public TurboSeedanceVideoQueryResponse() {
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public TurboSeedanceVideoContentMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(TurboSeedanceVideoContentMetadata metadata) {
        this.metadata = metadata;
    }
}
