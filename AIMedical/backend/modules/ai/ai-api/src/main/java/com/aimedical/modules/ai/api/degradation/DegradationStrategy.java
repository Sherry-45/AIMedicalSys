package com.aimedical.modules.ai.api.degradation;

public interface DegradationStrategy {

    boolean shouldDegrade(DegradationContext context);

    default int getOrder() {
        return 0;
    }
}
