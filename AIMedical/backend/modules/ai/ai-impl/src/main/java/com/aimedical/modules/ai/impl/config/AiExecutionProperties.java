package com.aimedical.modules.ai.impl.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.execution")
public class AiExecutionProperties {

    private Map<String, Duration> perCapability = new HashMap<>();
    private ThinAdapterConfig thinAdapter = new ThinAdapterConfig();
    private ParseConfig parse = new ParseConfig();

    public Map<String, Duration> getPerCapability() { return perCapability; }
    public void setPerCapability(Map<String, Duration> perCapability) { this.perCapability = perCapability; }
    public ThinAdapterConfig getThinAdapter() { return thinAdapter; }
    public void setThinAdapter(ThinAdapterConfig thinAdapter) { this.thinAdapter = thinAdapter; }
    public ParseConfig getParse() { return parse; }
    public void setParse(ParseConfig parse) { this.parse = parse; }

    public static class ThinAdapterConfig {
        private Map<String, Duration> perCapability = new HashMap<>();
        private Duration defaultTimeout = Duration.ofSeconds(30);
        public Map<String, Duration> getPerCapability() { return perCapability; }
        public void setPerCapability(Map<String, Duration> perCapability) { this.perCapability = perCapability; }
        public Duration getDefaultTimeout() { return defaultTimeout; }
        public void setDefaultTimeout(Duration defaultTimeout) { this.defaultTimeout = defaultTimeout; }
    }

    public static class ParseConfig {
        private Map<String, Duration> perCapability = new HashMap<>();
        private Duration defaultTimeout = Duration.ofSeconds(5);
        public Map<String, Duration> getPerCapability() { return perCapability; }
        public void setPerCapability(Map<String, Duration> perCapability) { this.perCapability = perCapability; }
        public Duration getDefaultTimeout() { return defaultTimeout; }
        public void setDefaultTimeout(Duration defaultTimeout) { this.defaultTimeout = defaultTimeout; }
    }
}
