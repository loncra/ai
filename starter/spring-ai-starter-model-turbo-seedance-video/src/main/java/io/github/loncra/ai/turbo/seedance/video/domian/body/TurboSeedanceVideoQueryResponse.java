package io.github.loncra.ai.turbo.seedance.video.domian.body;

import io.github.loncra.ai.turbo.seedance.video.domian.metadata.TurboSeedanceVideoContentMetadata;
import io.github.loncra.ai.video.QueryVideoResponse;

import java.util.LinkedHashMap;
import java.util.Map;

public class TurboSeedanceVideoQueryResponse extends TurboSeedanceVideoSubmitResponse implements QueryVideoResponse {

    private String progress;

    private TurboSeedanceVideoContentMetadata metadata;

    private Map<String, Object> error = new LinkedHashMap<>();

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

    public Map<String, Object> getError() {
        return error;
    }

    public void setError(Map<String, Object> error) {
        this.error = error;
    }
}
