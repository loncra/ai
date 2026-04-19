package io.github.loncra.ai.turbo.seedance.video.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loncra.ai.turbo.seedance.video")
public class TurboSeedanceVideoProperties {
    private String apiKey;
    private String baseUrl;

    public TurboSeedanceVideoProperties() {
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
