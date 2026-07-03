package com.aimedical.modules.ai.impl.degradation;

import java.time.Duration;

import com.aimedical.modules.ai.api.degradation.DegradationContext;
import com.aimedical.modules.ai.api.degradation.DegradationStrategy;

public class TimeoutDegradationStrategy implements DegradationStrategy {

    private final Duration timeoutThreshold;

    public TimeoutDegradationStrategy(Duration timeoutThreshold) {
        this.timeoutThreshold = timeoutThreshold != null ? timeoutThreshold : Duration.ofSeconds(30);
    }

    @Override
    public boolean shouldDegrade(DegradationContext context) {
        int invocationCount = context.getInvocationCount();
        if (invocationCount == 0) {
            return false;
        }
        return context.getElapsedTime() > timeoutThreshold.toMillis() * 0.8;
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
