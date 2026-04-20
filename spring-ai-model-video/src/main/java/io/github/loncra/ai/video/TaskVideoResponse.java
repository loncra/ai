package io.github.loncra.ai.video;

import org.springframework.ai.model.ModelResponse;
import org.springframework.ai.model.ResponseMetadata;

import java.util.List;

public class TaskVideoResponse<T> implements ModelResponse<TaskVideoGeneration<T>> {
    private final TaskVideoGeneration<T> result;

    private VideoResponseMetadata responseMetadata;

    public TaskVideoResponse(TaskVideoGeneration<T> result) {
        this.result = result;
    }

    public TaskVideoResponse(
            TaskVideoGeneration<T> result,
            VideoResponseMetadata responseMetadata
    ) {
        this.result = result;
        this.responseMetadata = responseMetadata;
    }

    @Override
    public TaskVideoGeneration<T> getResult() {
        return result;
    }

    @Override
    public List<TaskVideoGeneration<T>> getResults() {
        return List.of(result);
    }

    @Override
    public ResponseMetadata getMetadata() {
        return responseMetadata;
    }
}
