package com.aimedical.modules.ai.impl.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LlmChatResponse {

    private final String content;
    private final LlmChatUsage usage;
    private final String modelId;
    private final int retryCount;

    public LlmChatResponse() {
        this.content = null;
        this.usage = null;
        this.modelId = null;
        this.retryCount = 0;
    }

    public LlmChatResponse(@JsonProperty("content") String content,
                           @JsonProperty("usage") LlmChatUsage usage,
                           @JsonProperty("modelId") String modelId,
                           @JsonProperty("retryCount") int retryCount) {
        this.content = content;
        this.usage = usage;
        this.modelId = modelId;
        this.retryCount = retryCount;
    }

    public String getContent() { return content; }
    public LlmChatUsage getUsage() { return usage; }
    public String getModelId() { return modelId; }
    public int getRetryCount() { return retryCount; }

    public static class LlmChatUsage {

        private final int promptTokens;
        private final int completionTokens;
        private final int totalTokens;

        public LlmChatUsage() {
            this.promptTokens = 0;
            this.completionTokens = 0;
            this.totalTokens = 0;
        }

        public LlmChatUsage(@JsonProperty("promptTokens") int promptTokens,
                            @JsonProperty("completionTokens") int completionTokens,
                            @JsonProperty("totalTokens") int totalTokens) {
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.totalTokens = totalTokens;
        }

        public int getPromptTokens() { return promptTokens; }
        public int getCompletionTokens() { return completionTokens; }
        public int getTotalTokens() { return totalTokens; }
    }
}
