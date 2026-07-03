package com.aimedical.modules.ai.impl.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")
@Service
public class LoggingMetricsCollector implements AiMetricsCollector {

    private static final Logger log = LoggerFactory.getLogger(LoggingMetricsCollector.class);

    private final AiCallLogRepository repository;

    public LoggingMetricsCollector(AiCallLogRepository repository) {
        this.repository = repository;
    }

    @Override
    @Async("metricsAsyncExecutor")
    public void record(AiCallRecord record) {
        if (record == null) {
            log.warn("AiCallRecord is null, skipping persistence");
            return;
        }
        try {
            AiCallLogEntity entity = new AiCallLogEntity();
            entity.setCallTime(record.getCallTime());
            entity.setCapabilityId(record.getCapabilityId());
            entity.setCapabilityName(record.getCapabilityName());
            entity.setVisitId(record.getVisitId());
            entity.setPatientId(record.getPatientId());
            entity.setDepartmentId(record.getDepartmentId());
            entity.setCallerRole(record.getCallerRole());
            entity.setCallerId(record.getCallerId());
            entity.setUserId(record.getUserId());
            entity.setInputSummary(record.getInputSummary());
            entity.setOutputSummary(record.getOutputSummary());
            entity.setDegraded(record.isDegraded());
            entity.setDegradationReason(record.getDegradationReason());
            entity.setElapsedMs(record.getElapsedMs());
            entity.setErrorCode(record.getErrorCode());
            entity.setErrorMessage(record.getErrorMessage());
            entity.setModelId(record.getModelId());
            entity.setRetryCount(record.getRetryCount());
            entity.setSessionId(record.getSessionId());
            entity.setPromptVersion(record.getPromptVersion());
            entity.setPromptTokens(record.getPromptTokens());
            entity.setCompletionTokens(record.getCompletionTokens());
            entity.setTotalTokens(record.getTotalTokens());
            repository.save(entity);
        } catch (Exception e) {
            log.warn("Failed to persist AiCallLogEntity", e);
        }
    }
}
