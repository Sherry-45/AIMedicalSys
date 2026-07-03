package com.aimedical.modules.ai.impl.metrics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_call_log", indexes = {
    @Index(name = "idx_call_time", columnList = "callTime"),
    @Index(name = "idx_capability_call_time", columnList = "capabilityId, callTime DESC"),
    @Index(name = "idx_degraded_call_time", columnList = "degraded, callTime DESC")
})
public class AiCallLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "DATETIME(3)")
    private LocalDateTime callTime;

    @Column(length = 50)
    private String capabilityId;

    @Column(length = 50)
    private String capabilityName;

    @Column(length = 50)
    private String visitId;

    @Column(length = 50)
    private String patientId;

    @Column(length = 50)
    private String departmentId;

    @Column(length = 20)
    private String callerRole;

    @Column(length = 50)
    private String callerId;

    @Column(length = 50)
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String inputSummary;

    @Column(columnDefinition = "TEXT")
    private String outputSummary;

    @Column
    private boolean degraded;

    @Column(length = 255)
    private String degradationReason;

    @Column
    private long elapsedMs;

    @Column(length = 50)
    private String errorCode;

    @Column(length = 500)
    private String errorMessage;

    @Column(length = 50)
    private String modelId;

    @Column
    private int retryCount;

    @Column(length = 50)
    private String sessionId;

    @Column
    private Integer promptVersion;

    @Column
    private int promptTokens;

    @Column
    private int completionTokens;

    @Column
    private Integer totalTokens;

    public AiCallLogEntity() {
    }

    public AiCallLogEntity(Long id, LocalDateTime callTime,
                           String capabilityId, String capabilityName,
                           String visitId, String patientId,
                           String departmentId, String callerRole, String callerId,
                           String userId,
                           String inputSummary, String outputSummary,
                           boolean degraded, String degradationReason,
                           long elapsedMs,
                           String errorCode, String errorMessage,
                           String modelId, int retryCount,
                           String sessionId, Integer promptVersion,
                           int promptTokens, int completionTokens, Integer totalTokens) {
        this.id = id;
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getCallTime() { return callTime; }
    public void setCallTime(LocalDateTime callTime) { this.callTime = callTime; }
    public String getCapabilityId() { return capabilityId; }
    public void setCapabilityId(String capabilityId) { this.capabilityId = capabilityId; }
    public String getCapabilityName() { return capabilityName; }
    public void setCapabilityName(String capabilityName) { this.capabilityName = capabilityName; }
    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public String getCallerRole() { return callerRole; }
    public void setCallerRole(String callerRole) { this.callerRole = callerRole; }
    public String getCallerId() { return callerId; }
    public void setCallerId(String callerId) { this.callerId = callerId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getInputSummary() { return inputSummary; }
    public void setInputSummary(String inputSummary) { this.inputSummary = inputSummary; }
    public String getOutputSummary() { return outputSummary; }
    public void setOutputSummary(String outputSummary) { this.outputSummary = outputSummary; }
    public boolean isDegraded() { return degraded; }
    public void setDegraded(boolean degraded) { this.degraded = degraded; }
    public String getDegradationReason() { return degradationReason; }
    public void setDegradationReason(String degradationReason) { this.degradationReason = degradationReason; }
    public long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(long elapsedMs) { this.elapsedMs = elapsedMs; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Integer getPromptVersion() { return promptVersion; }
    public void setPromptVersion(Integer promptVersion) { this.promptVersion = promptVersion; }
    public int getPromptTokens() { return promptTokens; }
    public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }
    public int getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }
    public Integer getTotalTokens() { return totalTokens; }
    public void setTotalTokens(Integer totalTokens) { this.totalTokens = totalTokens; }
}
