package com.aimedical.modules.ai.impl.metrics;

public interface AiMetricsCollector {
    void record(AiCallRecord record);
}
