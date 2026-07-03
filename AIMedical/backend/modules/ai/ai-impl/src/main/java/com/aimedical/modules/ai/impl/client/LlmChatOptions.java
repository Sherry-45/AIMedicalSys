package com.aimedical.modules.ai.impl.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LlmChatOptions {

    private final String modelId;
    private final Double temperature;
    private final Integer maxTokens;
    private final List<String> stopSequences;
    private final Double topP;
    private final Double frequencyPenalty;
    private final Double presencePenalty;

    public LlmChatOptions() {
        this(null, null, null, null, null, null, null);
    }

    public LlmChatOptions(
            @JsonProperty("modelId") String modelId,
            @JsonProperty("temperature") Double temperature,
            @JsonProperty("maxTokens") Integer maxTokens,
            @JsonProperty("stopSequences") List<String> stopSequences,
            @JsonProperty("topP") Double topP,
            @JsonProperty("frequencyPenalty") Double frequencyPenalty,
            @JsonProperty("presencePenalty") Double presencePenalty) {
        this.modelId = modelId;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.stopSequences = stopSequences;
        this.topP = topP;
        this.frequencyPenalty = frequencyPenalty;
        this.presencePenalty = presencePenalty;
    }

    public String getModelId() { return modelId; }

    public Double getTemperature() { return temperature; }

    public Integer getMaxTokens() { return maxTokens; }

    public List<String> getStopSequences() { return stopSequences; }

    public Double getTopP() { return topP; }

    public Double getFrequencyPenalty() { return frequencyPenalty; }

    public Double getPresencePenalty() { return presencePenalty; }
}
