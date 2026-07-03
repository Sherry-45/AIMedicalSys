package com.aimedical.modules.ai.impl.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.template.fallback")
public class AiTemplateProperties {

    private Map<String, String> capabilityFallback = new HashMap<>();

    public Map<String, String> getCapabilityFallback() { return capabilityFallback; }
    public void setCapabilityFallback(Map<String, String> capabilityFallback) { this.capabilityFallback = capabilityFallback; }
}
