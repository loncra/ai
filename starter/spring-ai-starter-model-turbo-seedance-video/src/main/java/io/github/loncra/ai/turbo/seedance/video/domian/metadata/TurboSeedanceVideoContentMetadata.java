package io.github.loncra.ai.turbo.seedance.video.domian.metadata;

import io.github.loncra.ai.turbo.seedance.video.enumerate.TurboSeedanceVideoMode;

import java.io.Serializable;

public class TurboSeedanceVideoContentMetadata implements Serializable {

    private String url;

    private TurboSeedanceVideoMode mode;

    private Integer creditsUsed;

    private Integer creditsLeft;

    public TurboSeedanceVideoContentMetadata() {

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public TurboSeedanceVideoMode getMode() {
        return mode;
    }

    public void setMode(TurboSeedanceVideoMode mode) {
        this.mode = mode;
    }

    public Integer getCreditsUsed() {
        return creditsUsed;
    }

    public void setCreditsUsed(Integer creditsUsed) {
        this.creditsUsed = creditsUsed;
    }

    public Integer getCreditsLeft() {
        return creditsLeft;
    }

    public void setCreditsLeft(Integer creditsLeft) {
        this.creditsLeft = creditsLeft;
    }
}
