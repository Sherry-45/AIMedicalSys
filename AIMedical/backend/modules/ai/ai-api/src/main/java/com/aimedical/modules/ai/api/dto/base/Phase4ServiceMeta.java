package com.aimedical.modules.ai.api.dto.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class Phase4ServiceMeta {

    private final String modelId;
    private final Integer promptVersion;
    private final int retryCount;

    @JsonCreator
    public Phase4ServiceMeta(
            @JsonProperty("model_id") String modelId,
            @JsonProperty("prompt_version") Integer promptVersion,
            @JsonProperty("retry_count") int retryCount) {
        this.modelId = modelId;
        this.promptVersion = promptVersion;
        this.retryCount = retryCount;
    }

    public String getModelId() { return modelId; }
    public Integer getPromptVersion() { return promptVersion; }
    public int getRetryCount() { return retryCount; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Phase4ServiceMeta that)) return false;
        return retryCount == that.retryCount
                && Objects.equals(modelId, that.modelId)
                && Objects.equals(promptVersion, that.promptVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelId, promptVersion, retryCount);
    }

    @Override
    public String toString() {
        return "Phase4ServiceMeta{modelId=" + modelId
                + ", promptVersion=" + promptVersion
                + ", retryCount=" + retryCount + "}";
    }
}
