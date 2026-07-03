package com.aimedical.modules.ai.impl.metrics;

import java.time.LocalDateTime;

public class AiCallRecord {
    private final LocalDateTime callTime;
    private final String capabilityId;
    private final String capabilityName;
    private final String visitId;
    private final String patientId;
    private final String departmentId;
    private final String callerRole;
    private final String callerId;
    private final String userId;
    private final String inputSummary;
    private final String outputSummary;
    private final boolean degraded;
    private final String degradationReason;
    private final long elapsedMs;
    private final String errorCode;
    private final String errorMessage;
    private final String modelId;
    private final int retryCount;
    private final String sessionId;
    private final Integer promptVersion;
    private final int promptTokens;
    private final int completionTokens;
    private final Integer totalTokens;

    public AiCallRecord(String capabilityId, String modelId, String promptVersion,
                        String userId, String departmentId, String sessionId,
                        String visitId, String patientId, String callerRole, String callerId,
                        long elapsedMs, boolean degraded, String degradationReason,
                        int promptTokens, int completionTokens) {
        this(LocalDateTime.now(), capabilityId, null, visitId, patientId, departmentId,
             callerRole, callerId, userId, null, null, degraded, degradationReason, elapsedMs,
             null, null, modelId, 0, sessionId,
             promptVersion == null ? null : parsePromptVersion(promptVersion),
             promptTokens, completionTokens, null);
    }

    private static Integer parsePromptVersion(String version) {
        try {
            return Integer.valueOf(version);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private AiCallRecord(LocalDateTime callTime, String capabilityId, String capabilityName,
                         String visitId, String patientId, String departmentId,
                         String callerRole, String callerId, String userId,
                         String inputSummary, String outputSummary,
                         boolean degraded, String degradationReason, long elapsedMs,
                         String errorCode, String errorMessage, String modelId, int retryCount,
                         String sessionId, Integer promptVersion, int promptTokens,
                         int completionTokens, Integer totalTokens) {
        this.callTime = callTime;
        this.capabilityId = capabilityId;
        this.capabilityName = capabilityName;
        this.visitId = visitId;
        this.patientId = patientId;
        this.departmentId = departmentId;
        this.callerRole = callerRole;
        this.callerId = callerId;
        this.userId = userId;
        this.inputSummary = inputSummary;
        this.outputSummary = outputSummary;
        this.degraded = degraded;
        this.degradationReason = degradationReason;
        this.elapsedMs = elapsedMs;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.modelId = modelId;
        this.retryCount = retryCount;
        this.sessionId = sessionId;
        this.promptVersion = promptVersion;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
    }

    static AiCallRecord success(String capabilityId, String capabilityName,
                                LocalDateTime callTime, long elapsedMs,
                                String departmentId, String modelId, int retryCount,
                                Integer promptTokens, Integer completionTokens,
                                String inputSummary, String outputSummary,
                                String visitId, String patientId, String sessionId,
                                String callerRole, String callerId, String userId,
                                Integer promptVersion, String sentinelReason) {
        if ("EXPERIMENT_ASSIGN_ERROR".equals(sentinelReason)) {
            promptVersion = -1;
        }
        Integer totalTokens = (promptTokens != null && completionTokens != null)
                ? promptTokens + completionTokens : null;
        return new AiCallRecord(callTime, capabilityId, capabilityName,
                visitId, patientId, departmentId, callerRole, callerId, userId,
                inputSummary, outputSummary, false, null, elapsedMs,
                null, null, modelId, retryCount, sessionId, promptVersion,
                promptTokens != null ? promptTokens : 0,
                completionTokens != null ? completionTokens : 0, totalTokens);
    }

    static AiCallRecord failure(String capabilityId, String capabilityName,
                                LocalDateTime callTime, long elapsedMs,
                                String errorCode, String errorMessage,
                                String departmentId, String inputSummary,
                                String visitId, String patientId, String sessionId,
                                String callerRole, String callerId, String userId,
                                Integer promptVersion, String sentinelReason) {
        if ("EXPERIMENT_ASSIGN_ERROR".equals(sentinelReason)) {
            promptVersion = -1;
        }
        return new AiCallRecord(callTime, capabilityId, capabilityName,
                visitId, patientId, departmentId, callerRole, callerId, userId,
                inputSummary, null, false, null, elapsedMs,
                errorCode, errorMessage, null, 0, sessionId, promptVersion,
                0, 0, null);
    }

    static AiCallRecord degraded(String capabilityId, String capabilityName,
                                 LocalDateTime callTime, long elapsedMs,
                                 String degradationReason, String modelId,
                                 String departmentId, String inputSummary,
                                 String visitId, String patientId, String sessionId,
                                 String callerRole, String callerId, String userId,
                                 String outputSummary, Integer promptVersion,
                                 String sentinelReason) {
        if ("EXPERIMENT_ASSIGN_ERROR".equals(sentinelReason)) {
            promptVersion = -1;
        }
        return new AiCallRecord(callTime, capabilityId, capabilityName,
                visitId, patientId, departmentId, callerRole, callerId, userId,
                inputSummary, outputSummary, true, degradationReason, elapsedMs,
                null, null, modelId, 0, sessionId, promptVersion,
                0, 0, null);
    }

    public LocalDateTime getCallTime() { return callTime; }
    public String getCapabilityId() { return capabilityId; }
    public String getCapabilityName() { return capabilityName; }
    public String getVisitId() { return visitId; }
    public String getPatientId() { return patientId; }
    public String getDepartmentId() { return departmentId; }
    public String getCallerRole() { return callerRole; }
    public String getCallerId() { return callerId; }
    public String getUserId() { return userId; }
    public String getInputSummary() { return inputSummary; }
    public String getOutputSummary() { return outputSummary; }
    public boolean isDegraded() { return degraded; }
    public String getDegradationReason() { return degradationReason; }
    public long getElapsedMs() { return elapsedMs; }
    public String getErrorCode() { return errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public String getModelId() { return modelId; }
    public int getRetryCount() { return retryCount; }
    public String getSessionId() { return sessionId; }
    public Integer getPromptVersion() { return promptVersion; }
    public int getPromptTokens() { return promptTokens; }
    public int getCompletionTokens() { return completionTokens; }
    public Integer getTotalTokens() { return totalTokens; }
}
