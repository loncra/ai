package io.github.loncra.ai.video;

import io.github.loncra.framework.commons.id.BasicIdentification;
import org.springframework.ai.model.ModelResult;
import org.springframework.ai.model.ResultMetadata;

public class TaskVideoGeneration<T> implements ModelResult<BasicIdentification<T>> {

    private final BasicIdentification<T> output;

    private VideoGenerationMetadata resultMetadata;

    public TaskVideoGeneration(BasicIdentification<T> output) {
        this.output = output;
    }

    public TaskVideoGeneration(
            BasicIdentification<T> output,
            VideoGenerationMetadata resultMetadata
    ) {
        this.output = output;
        this.resultMetadata = resultMetadata;
    }

    @Override
    public BasicIdentification<T> getOutput() {
        return output;
    }

    @Override
    public ResultMetadata getMetadata() {
        return resultMetadata;
    }
}
