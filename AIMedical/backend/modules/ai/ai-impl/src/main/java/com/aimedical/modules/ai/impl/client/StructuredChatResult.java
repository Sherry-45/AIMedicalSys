package com.aimedical.modules.ai.impl.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StructuredChatResult<T> {

    private final T data;
    private final int retryCount;
    private final LlmChatResponse.LlmChatUsage usage;

    public StructuredChatResult() {
        this.data = null;
        this.retryCount = 0;
        this.usage = null;
    }

    public StructuredChatResult(@JsonProperty("data") T data,
                                @JsonProperty("retryCount") int retryCount,
                                @JsonProperty("usage") LlmChatResponse.LlmChatUsage usage) {
        this.data = data;
        this.retryCount = retryCount;
        this.usage = usage;
    }

    public T getData() { return data; }
    public int getRetryCount() { return retryCount; }
    public LlmChatResponse.LlmChatUsage getUsage() { return usage; }
}
