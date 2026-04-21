package io.github.loncra.ai.turbo.seedance.video.domian.metadata;

import org.springframework.ai.model.AbstractResponseMetadata;
import org.springframework.ai.model.ResponseMetadata;

import java.time.Instant;

/**
 * Turbo 渠道 Seedance 视频响应的元数据；承载任务提交返回的顶层字段（id / task_id / model / created_at 等）。
 *
 * @author maurice.chen
 */
public class TurboSeedanceVideoResponseMetadata extends AbstractResponseMetadata implements ResponseMetadata {

    private static final String KEY_ID = "id";

    private static final String KEY_TASK_ID = "taskId";

    private static final String KEY_MODEL = "model";

    private static final String KEY_STATUS = "status";

    private static final String KEY_RAW_STATUS = "rawStatus";

    private static final String KEY_CREATED_AT = "createdAt";

    public TurboSeedanceVideoResponseMetadata() {
    }

    public TurboSeedanceVideoResponseMetadata(
            String id,
            String taskId,
            String model,
            String status,
            String rawStatus,
            Instant createdAt
    ) {
        if (id != null) {
            this.map.put(KEY_ID, id);
        }
        if (taskId != null) {
            this.map.put(KEY_TASK_ID, taskId);
        }
        if (model != null) {
            this.map.put(KEY_MODEL, model);
        }
        if (status != null) {
            this.map.put(KEY_STATUS, status);
        }
        if (rawStatus != null) {
            this.map.put(KEY_RAW_STATUS, rawStatus);
        }
        if (createdAt != null) {
            this.map.put(KEY_CREATED_AT, createdAt);
        }
    }

    public String getId() {
        return this.get(KEY_ID);
    }

    public String getTaskId() {
        return this.get(KEY_TASK_ID);
    }

    public String getModel() {
        return this.get(KEY_MODEL);
    }

    public String getStatus() {
        return this.get(KEY_STATUS);
    }

    public String getRawStatus() {
        return this.get(KEY_RAW_STATUS);
    }

    public Instant getCreatedAt() {
        return this.get(KEY_CREATED_AT);
    }

}
