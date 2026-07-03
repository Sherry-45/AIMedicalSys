package com.aimedical.modules.ai.impl.router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.aimedical.modules.ai.impl.client.AuthType;
import com.aimedical.modules.ai.impl.client.ClientType;
import com.aimedical.modules.ai.impl.config.ModelRouteConfig;

@ConfigurationProperties(prefix = "ai.router")
public class AiRouterProperties {

    private static final Logger log = LoggerFactory.getLogger(AiRouterProperties.class);

    private Map<String, List<ModelRouteConfig>> routes = new HashMap<>();

    public Map<String, List<ModelRouteConfig>> getRoutes() { return routes; }
    public void setRoutes(Map<String, List<ModelRouteConfig>> routes) { this.routes = routes; }

    public Map<String, List<ModelRoute>> toModelRouteMap() {
        Map<String, List<ModelRoute>> result = new HashMap<>();
        for (Map.Entry<String, List<ModelRouteConfig>> entry : routes.entrySet()) {
            String capabilityId = entry.getKey();
            List<ModelRouteConfig> configs = entry.getValue();
            List<ModelRoute> modelRoutes = new ArrayList<>();
            if (configs != null) {
                for (ModelRouteConfig config : configs) {
                    ModelRoute route = convert(config);
                    if (route != null) {
                        modelRoutes.add(route);
                    }
                }
            }
            result.put(capabilityId, modelRoutes);
        }
        return result;
    }

    private static ModelRoute convert(ModelRouteConfig config) {
        if (config == null) return null;
        ClientType clientType = null;
        if (config.getClientType() != null) {
            try {
                clientType = ClientType.valueOf(config.getClientType().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid clientType value: '{}', falling back to {}", config.getClientType(), ClientType.HTTP_API);
                clientType = ClientType.HTTP_API;
            }
        }
        AuthType authType = null;
        if (config.getAuthType() != null) {
            try {
                authType = AuthType.valueOf(config.getAuthType().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid authType value: '{}', falling back to {}", config.getAuthType(), AuthType.NONE);
                authType = AuthType.NONE;
            }
        }
        return new ModelRoute(
            config.getEndpointId(),
            clientType,
            authType,
            config.getModelId(),
            config.getEndpointUrl(),
            config.getWeight(),
            config.getTimeoutMs(),
            config.getParameters()
        );
    }
}
