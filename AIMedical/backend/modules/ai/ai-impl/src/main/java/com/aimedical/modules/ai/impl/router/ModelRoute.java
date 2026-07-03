package com.aimedical.modules.ai.impl.router;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.aimedical.modules.ai.impl.client.AuthType;
import com.aimedical.modules.ai.impl.client.ClientType;

public class ModelRoute {

    private final String endpointId;
    private final ClientType clientType;
    private final AuthType authType;
    private final String modelId;
    private final String endpointUrl;
    private final int weight;
    private final long timeoutMs;
    private final Map<String, Object> parameters;

    public ModelRoute(String endpointId, ClientType clientType, AuthType authType,
                      String modelId, String endpointUrl, int weight,
                      long timeoutMs, Map<String, Object> parameters) {
        this.endpointId = endpointId;
        this.clientType = clientType;
        this.authType = authType;
        this.modelId = modelId;
        this.endpointUrl = endpointUrl;
        this.weight = weight;
        this.timeoutMs = timeoutMs;
        this.parameters = parameters != null
            ? Collections.unmodifiableMap(new HashMap<>(parameters))
            : Collections.emptyMap();
    }

    public static ModelRoute of(String modelId) {
        return new ModelRoute(null, null, null, modelId, null, 1, 30000L, null);
    }

    public String getEndpointId() { return endpointId; }
    public ClientType getClientType() { return clientType; }
    public AuthType getAuthType() { return authType; }
    public String getModelId() { return modelId; }
    public String getEndpointUrl() { return endpointUrl; }
    public int getWeight() { return weight; }
    public long getTimeoutMs() { return timeoutMs; }
    public Map<String, Object> getParameters() { return parameters; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelRoute that)) return false;
        return weight == that.weight && timeoutMs == that.timeoutMs
            && Objects.equals(endpointId, that.endpointId)
            && clientType == that.clientType
            && authType == that.authType
            && Objects.equals(modelId, that.modelId)
            && Objects.equals(endpointUrl, that.endpointUrl)
            && Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpointId, clientType, authType, modelId, endpointUrl, weight, timeoutMs, parameters);
    }

    @Override
    public String toString() {
        return "ModelRoute{modelId='" + modelId + "', endpointId='" + endpointId + "'}";
    }
}
