package io.github.loncra.ai.video;

import org.springframework.ai.model.ModelOptions;
import org.springframework.lang.Nullable;

public interface VideoOptions extends ModelOptions {

    String getModel();

    Integer getDuration();

    String getResolution();

    String getRatio();
}
