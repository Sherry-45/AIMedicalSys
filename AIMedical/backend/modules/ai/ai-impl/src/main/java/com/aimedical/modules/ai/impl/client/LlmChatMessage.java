package com.aimedical.modules.ai.impl.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LlmChatMessage {

    private final LlmChatMessageRole role;
    private final String content;

    public LlmChatMessage() {
        this.role = null;
        this.content = null;
    }

    public LlmChatMessage(@JsonProperty("role") LlmChatMessageRole role,
                          @JsonProperty("content") String content) {
        this.role = role;
        this.content = content;
    }

    public LlmChatMessageRole getRole() { return role; }
    public String getContent() { return content; }
}
