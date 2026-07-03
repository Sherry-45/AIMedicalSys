package com.aimedical.modules.ai.impl.degradation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.aimedical.modules.ai.api.degradation.DegradationContext;
import com.aimedical.modules.ai.api.degradation.DegradationStrategy;

@Component
@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")
@ConditionalOnMissingBean(DegradationStrategy.class)
public class NoOpDegradationStrategy implements DegradationStrategy {

    @Override
    public boolean shouldDegrade(DegradationContext context) {
        return false;
    }
}
