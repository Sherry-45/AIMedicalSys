package com.aimedical.modules.ai.api.dto.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class CallContext {

    private final String departmentId;
    private final String callerRole;
    private final String callerId;
    private final String visitId;
    private final String patientId;
    private final String sessionId;
    private final String inputSummary;
    private final String outputSummary;
    private final Integer promptVersion;

    @JsonCreator
    public CallContext(
            @JsonProperty("department_id") String departmentId,
            @JsonProperty("caller_role") String callerRole,
            @JsonProperty("caller_id") String callerId,
            @JsonProperty("visit_id") String visitId,
            @JsonProperty("patient_id") String patientId,
            @JsonProperty("session_id") String sessionId,
            @JsonProperty("input_summary") String inputSummary,
            @JsonProperty("output_summary") String outputSummary,
            @JsonProperty("prompt_version") Integer promptVersion) {
        this.departmentId = departmentId;
        this.callerRole = callerRole;
        this.callerId = callerId;
        this.visitId = visitId;
        this.patientId = patientId;
        this.sessionId = sessionId;
        this.inputSummary = inputSummary;
        this.outputSummary = outputSummary;
        this.promptVersion = promptVersion;
    }

    public String getDepartmentId() { return departmentId; }
    public String getCallerRole() { return callerRole; }
    public String getCallerId() { return callerId; }
    public String getVisitId() { return visitId; }
    public String getPatientId() { return patientId; }
    public String getSessionId() { return sessionId; }
    public String getInputSummary() { return inputSummary; }
    public String getOutputSummary() { return outputSummary; }
    public Integer getPromptVersion() { return promptVersion; }

    public CallContext withOutputSummary(String outputSummary) {
        return new CallContext(departmentId, callerRole, callerId, visitId,
                patientId, sessionId, inputSummary, outputSummary, promptVersion);
    }

    public CallContext withPromptVersion(Integer promptVersion) {
        return new CallContext(departmentId, callerRole, callerId, visitId,
                patientId, sessionId, inputSummary, outputSummary, promptVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CallContext that)) return false;
        return Objects.equals(departmentId, that.departmentId)
                && Objects.equals(callerRole, that.callerRole)
                && Objects.equals(callerId, that.callerId)
                && Objects.equals(visitId, that.visitId)
                && Objects.equals(patientId, that.patientId)
                && Objects.equals(sessionId, that.sessionId)
                && Objects.equals(inputSummary, that.inputSummary)
                && Objects.equals(outputSummary, that.outputSummary)
                && Objects.equals(promptVersion, that.promptVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(departmentId, callerRole, callerId, visitId,
                patientId, sessionId, inputSummary, outputSummary, promptVersion);
    }

    @Override
    public String toString() {
        return "CallContext{departmentId=" + departmentId
                + ", callerRole=" + callerRole
                + ", callerId=" + callerId
                + ", visitId=" + visitId
                + ", patientId=" + patientId
                + ", sessionId=" + sessionId
                + ", inputSummary=" + inputSummary
                + ", outputSummary=" + outputSummary
                + ", promptVersion=" + promptVersion + "}";
    }
}
