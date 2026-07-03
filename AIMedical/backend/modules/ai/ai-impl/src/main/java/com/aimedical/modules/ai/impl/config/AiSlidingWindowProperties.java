package com.aimedical.modules.ai.impl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.sliding-window")
public class AiSlidingWindowProperties {

    private int windowSeconds = 60;
    private int maxEventsPerCapability = 10000;

    public int getWindowSeconds() { return windowSeconds; }
    public void setWindowSeconds(int windowSeconds) { this.windowSeconds = windowSeconds; }
    public int getMaxEventsPerCapability() { return maxEventsPerCapability; }
    public void setMaxEventsPerCapability(int maxEventsPerCapability) { this.maxEventsPerCapability = maxEventsPerCapability; }
}
