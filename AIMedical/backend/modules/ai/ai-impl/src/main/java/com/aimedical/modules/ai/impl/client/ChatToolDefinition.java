package com.aimedical.modules.ai.impl.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class ChatToolDefinition {

    private final String name;
    private final String description;
    private final JsonNode parameters;
    private boolean strict = true;

    public ChatToolDefinition() {
        this.name = null;
        this.description = null;
        this.parameters = null;
    }

    public ChatToolDefinition(@JsonProperty("name") String name,
                              @JsonProperty("description") String description,
                              @JsonProperty("parameters") JsonNode parameters) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public JsonNode getParameters() { return parameters; }
    public boolean isStrict() { return strict; }
}
