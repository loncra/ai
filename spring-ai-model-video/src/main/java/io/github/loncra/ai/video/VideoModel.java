package io.github.loncra.ai.video;

import org.springframework.ai.model.Model;

public interface VideoModel<T> extends Model<VideoPrompt, TaskVideoResponse<T>> {

    QueryVideoResponse query(T id);
}
