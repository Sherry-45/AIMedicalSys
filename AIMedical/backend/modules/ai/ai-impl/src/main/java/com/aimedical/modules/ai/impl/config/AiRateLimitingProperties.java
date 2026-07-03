package com.aimedical.modules.ai.impl.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.rate-limiting")
public class AiRateLimitingProperties {

    private boolean enabled = false;
    private Map<String, EndpointRateLimitConfig> endpoints = new HashMap<>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Map<String, EndpointRateLimitConfig> getEndpoints() { return endpoints; }
    public void setEndpoints(Map<String, EndpointRateLimitConfig> endpoints) { this.endpoints = endpoints; }

    public static class EndpointRateLimitConfig {
        private double permitsPerSecond = 10.0;
        private int warmupPeriodSeconds = 1;
        public double getPermitsPerSecond() { return permitsPerSecond; }
        public void setPermitsPerSecond(double permitsPerSecond) { this.permitsPerSecond = permitsPerSecond; }
        public int getWarmupPeriodSeconds() { return warmupPeriodSeconds; }
        public void setWarmupPeriodSeconds(int warmupPeriodSeconds) { this.warmupPeriodSeconds = warmupPeriodSeconds; }
    }
}
