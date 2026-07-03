package com.aimedical.modules.ai.impl.config;

import java.util.HashMap;
import java.util.Map;

public class ModelRouteConfig {

    private String endpointId;
    private String clientType;
    private String authType;
    private String modelId;
    private String endpointUrl;
    private int weight;
    private long timeoutMs;
    private Map<String, Object> parameters = new HashMap<>();

    public ModelRouteConfig() {}

    public String getEndpointId() { return endpointId; }
    public void setEndpointId(String endpointId) { this.endpointId = endpointId; }
    public String getClientType() { return clientType; }
    public void setClientType(String clientType) { this.clientType = clientType; }
    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    public long getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
}
