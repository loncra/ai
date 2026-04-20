package io.github.loncra.ai.video;

import org.springframework.ai.model.ModelOptions;
import org.springframework.ai.model.ModelRequest;

public class VideoPrompt implements ModelRequest<String> {

    private final String text;

    private final VideoOptions videoOptions;

    public VideoPrompt(
            String text,
            VideoOptions videoOptions
    ) {
        this.text = text;
        this.videoOptions = videoOptions;
    }

    @Override
    public String getInstructions() {
        return text;
    }

    @Override
    public VideoOptions getOptions() {
        return videoOptions;
    }
}
