package com.aimedical.modules.ai.impl.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.degradation")
public class AiDegradationProperties {

    private Map<String, List<String>> strategies = new HashMap<>();
    private int contextTtlSeconds = 60;
    private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();
    private TimeoutConfig timeout = new TimeoutConfig();

    public Map<String, List<String>> getStrategies() { return strategies; }
    public void setStrategies(Map<String, List<String>> strategies) { this.strategies = strategies; }
    public int getContextTtlSeconds() { return contextTtlSeconds; }
    public void setContextTtlSeconds(int contextTtlSeconds) { this.contextTtlSeconds = contextTtlSeconds; }
    public CircuitBreakerConfig getCircuitBreaker() { return circuitBreaker; }
    public void setCircuitBreaker(CircuitBreakerConfig circuitBreaker) { this.circuitBreaker = circuitBreaker; }
    public TimeoutConfig getTimeout() { return timeout; }
    public void setTimeout(TimeoutConfig timeout) { this.timeout = timeout; }

    public static class CircuitBreakerConfig {
        private double failureRateThreshold = 0.5;
        private long openWindowMs = 30000;
        public double getFailureRateThreshold() { return failureRateThreshold; }
        public void setFailureRateThreshold(double failureRateThreshold) { this.failureRateThreshold = failureRateThreshold; }
        public long getOpenWindowMs() { return openWindowMs; }
        public void setOpenWindowMs(long openWindowMs) { this.openWindowMs = openWindowMs; }
    }

    public static class TimeoutConfig {
        private long thresholdMs = 30000;
        public long getThresholdMs() { return thresholdMs; }
        public void setThresholdMs(long thresholdMs) { this.thresholdMs = thresholdMs; }
    }
}
