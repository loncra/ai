package io.github.loncra.ai.turbo.seedance.video;

import io.github.loncra.ai.turbo.seedance.video.domian.body.TurboSeedanceVideoQueryResponse;
import io.github.loncra.ai.turbo.seedance.video.domian.body.TurboSeedanceVideoSubmitResponse;
import io.github.loncra.ai.video.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.Model;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Turbo 渠道 Seedance 视频生成 {@link Model} 实现。
 *
 * @author maurice.chen
 */
public class TurboSeedanceVideoModel implements VideoModel<String> {

    private static final Logger logger = LoggerFactory.getLogger(TurboSeedanceVideoModel.class);

    public static final String PROVIDER_NAME = "turbo-seedance";

    public static final String SUCCEEDED_STATUS_VALUE = "succeeded";

    public static final String SUBMITTED_STATUS_VALUE = "submitted";

    public static final String FAILED_STATUS_VALUE = "failed";

    public static final String COMPLETED_STATUS_VALUE = "completed";

    public static final List<String> SUCCEEDED_STATUS = List.of(SUCCEEDED_STATUS_VALUE, COMPLETED_STATUS_VALUE);

    private final TurboSeedanceVideoApiClient apiClient;

    private final TurboSeedanceVideoOptions defaultOptions;

    private final RetryTemplate retryTemplate;

    public TurboSeedanceVideoModel(TurboSeedanceVideoApiClient apiClient) {
        this(apiClient, TurboSeedanceVideoOptions.builder().build(), new RetryTemplate());
    }

    public TurboSeedanceVideoModel(
            TurboSeedanceVideoApiClient apiClient,
            TurboSeedanceVideoOptions defaultOptions,
            RetryTemplate retryTemplate
    ) {
        Assert.notNull(apiClient, "apiClient 不能为 null");
        Assert.notNull(defaultOptions, "defaultOptions 不能为 null");
        Assert.notNull(retryTemplate, "retryTemplate 不能为 null");
        this.apiClient = apiClient;
        this.defaultOptions = defaultOptions;
        this.retryTemplate = retryTemplate;
    }


    public TurboSeedanceVideoOptions getDefaultOptions() {
        return this.defaultOptions;
    }

    public TurboSeedanceVideoApiClient getApiClient() {
        return this.apiClient;
    }

    @Override
    public QueryVideoResponse query(String id) {
        Assert.hasText(id, "id 不能为空");
        /*TurboSeedanceVideoQueryResponse response = this.retryTemplate.execute(ctx -> this.apiClient.query(id));
        if (SUCCEEDED_STATUS.contains(response.getStatus())) {
            return this.retryTemplate.execute(ctx -> this.apiClient.content(id));
        }*/

        return this.retryTemplate.execute(ctx -> this.apiClient.query(id));
    }

    @Override
    public TaskVideoResponse<String> call(VideoPrompt request) {
        Assert.notNull(request.getInstructions(), "prompt 不能为 null");
        String instructions = request.getInstructions();
        Assert.hasText(instructions, "prompt 文本不能为空");

        TurboSeedanceVideoOptions runtimeOptions;
        if (request.getOptions() instanceof TurboSeedanceVideoOptions g) {
            runtimeOptions = TurboSeedanceVideoOptions.fromOptions(g);
        }
        else {
            runtimeOptions = ModelOptionsUtils.copyToTarget(request.getOptions(), VideoOptions.class,
                    TurboSeedanceVideoOptions.class);
        }

        runtimeOptions.setMode(runtimeOptions.inferMode());

        TurboSeedanceVideoSubmitResponse submitResponse = this.retryTemplate
                .execute(ctx -> this.apiClient.submit(runtimeOptions, instructions));
        logger.debug("提交 Turbo Seedance 视频任务成功：id={}, status={}", submitResponse.getId(), submitResponse.getStatus());

        TaskVideoGeneration<String> generation = new TaskVideoGeneration<>(submitResponse);
        return new TaskVideoResponse<>(generation);
    }
}
