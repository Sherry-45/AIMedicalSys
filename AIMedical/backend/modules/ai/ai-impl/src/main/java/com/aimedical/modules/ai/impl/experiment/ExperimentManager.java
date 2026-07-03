package com.aimedical.modules.ai.impl.experiment;

public interface ExperimentManager {
    ExperimentAssignment assign(String capabilityId, String userId, String sessionId);
}
