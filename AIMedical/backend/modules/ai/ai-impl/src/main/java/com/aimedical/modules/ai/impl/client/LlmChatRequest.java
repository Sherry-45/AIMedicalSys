package com.aimedical.modules.ai.impl.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class LlmChatRequest {

    private final List<LlmChatMessage> messages;
    private final LlmChatOptions options;
    private final ClientType clientType;
    private final List<ChatToolDefinition> tools;
    private final String endpointId;
    private final String endpointUrl;

    public LlmChatRequest() {
        this.messages = null;
        this.options = null;
        this.clientType = null;
        this.tools = null;
        this.endpointId = null;
        this.endpointUrl = null;
    }

    public LlmChatRequest(@JsonProperty("messages") List<LlmChatMessage> messages,
                          @JsonProperty("options") LlmChatOptions options,
                          @JsonProperty("clientType") ClientType clientType,
                          @JsonProperty("tools") List<ChatToolDefinition> tools,
                          @JsonProperty("endpointId") String endpointId) {
        this(messages, options, clientType, tools, endpointId, null);
    }

    public LlmChatRequest(List<LlmChatMessage> messages,
                          LlmChatOptions options,
                          ClientType clientType,
                          List<ChatToolDefinition> tools,
                          String endpointId,
                          String endpointUrl) {
        this.messages = messages;
        this.options = options;
        this.clientType = clientType;
        this.tools = tools;
        this.endpointId = endpointId;
        this.endpointUrl = endpointUrl;
    }

    public List<LlmChatMessage> getMessages() { return messages; }
    public LlmChatOptions getOptions() { return options; }
    public ClientType getClientType() { return clientType; }
    public List<ChatToolDefinition> getTools() { return tools; }
    public String getEndpointId() { return endpointId; }
    public String getEndpointUrl() { return endpointUrl; }
}
