package com.aimedical.modules.ai.impl.metrics;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AiCallRecordTest {

    @Test
    void shouldConstructWithAllFields() {
        AiCallRecord record = new AiCallRecord(
            "TRIAGE", "gpt-4", "2",
            "user1", "dept1", "session1",
            "visit1", "pat1", "doctor", "doc1",
            1500L, false, null,
            100, 50
        );

        assertEquals("TRIAGE", record.getCapabilityId());
        assertEquals("gpt-4", record.getModelId());
        assertEquals(Integer.valueOf(2), record.getPromptVersion());
        assertEquals("user1", record.getUserId());
        assertEquals("dept1", record.getDepartmentId());
        assertEquals("session1", record.getSessionId());
        assertEquals("visit1", record.getVisitId());
        assertEquals("pat1", record.getPatientId());
        assertEquals("doctor", record.getCallerRole());
        assertEquals("doc1", record.getCallerId());
        assertEquals(1500L, record.getElapsedMs());
        assertFalse(record.isDegraded());
        assertNull(record.getDegradationReason());
        assertEquals(100, record.getPromptTokens());
        assertEquals(50, record.getCompletionTokens());
    }

    @Test
    void shouldConstructWithDegradedState() {
        AiCallRecord record = new AiCallRecord(
            "RX_AUDIT", "gpt-4", "v1",
            "user1", "dept1", "session1",
            "visit1", "pat1", "system", "sys",
            500L, true, "Timeout",
            0, 0
        );

        assertTrue(record.isDegraded());
        assertEquals("Timeout", record.getDegradationReason());
        assertEquals(0, record.getPromptTokens());
        assertEquals(0, record.getCompletionTokens());
    }

    @Test
    void shouldHandleNullReferences() {
        AiCallRecord record = new AiCallRecord(
            null, null, null,
            null, null, null,
            null, null, null, null,
            0L, false, null,
            0, 0
        );

        assertNull(record.getCapabilityId());
        assertNull(record.getModelId());
        assertNull(record.getDegradationReason());
        assertFalse(record.isDegraded());
    }

    @Test
    void successFactoryShouldCreateFullyInitializedInstance() {
        LocalDateTime now = LocalDateTime.now();
        AiCallRecord record = AiCallRecord.success(
            "DIAGNOSIS", "诊断能力", now, 1500L,
            "cardio", "gpt-4", 2,
            100, 50,
            "input text", "output text",
            "visit1", "pat1", "session1",
            "doctor", "doc1", "user1",
            3, null
        );

        assertEquals("DIAGNOSIS", record.getCapabilityId());
        assertEquals("诊断能力", record.getCapabilityName());
        assertEquals(now, record.getCallTime());
        assertEquals(1500L, record.getElapsedMs());
        assertEquals("cardio", record.getDepartmentId());
        assertEquals("gpt-4", record.getModelId());
        assertEquals(2, record.getRetryCount());
        assertEquals(Integer.valueOf(100), Integer.valueOf(record.getPromptTokens()));
        assertEquals(Integer.valueOf(50), Integer.valueOf(record.getCompletionTokens()));
        assertEquals(Integer.valueOf(150), record.getTotalTokens());
        assertEquals("input text", record.getInputSummary());
        assertEquals("output text", record.getOutputSummary());
        assertEquals("visit1", record.getVisitId());
        assertEquals("pat1", record.getPatientId());
        assertEquals("session1", record.getSessionId());
        assertEquals("doctor", record.getCallerRole());
        assertEquals("doc1", record.getCallerId());
        assertEquals("user1", record.getUserId());
        assertEquals(Integer.valueOf(3), record.getPromptVersion());
        assertFalse(record.isDegraded());
        assertNull(record.getDegradationReason());
        assertNull(record.getErrorCode());
        assertNull(record.getErrorMessage());
    }

    @Test
    void successFactoryShouldSetTotalTokensToNullWhenPromptTokensNull() {
        AiCallRecord record = AiCallRecord.success(
            "DIAGNOSIS", "诊断能力", LocalDateTime.now(), 100L,
            "dept1", "gpt-4", 0,
            null, 50,
            null, null,
            "v1", "p1", "s1",
            "doc", "d1", "u1",
            1, null
        );

        assertNull(record.getTotalTokens());
    }

    @Test
    void successFactoryShouldSetTotalTokensToNullWhenCompletionTokensNull() {
        AiCallRecord record = AiCallRecord.success(
            "DIAGNOSIS", "诊断能力", LocalDateTime.now(), 100L,
            "dept1", "gpt-4", 0,
            100, null,
            null, null,
            "v1", "p1", "s1",
            "doc", "d1", "u1",
            1, null
        );

        assertNull(record.getTotalTokens());
    }

    @Test
    void failureFactoryShouldCreateInstanceWithCorrectDefaults() {
        AiCallRecord record = AiCallRecord.failure(
            "TRIAGE", "分诊能力", LocalDateTime.now(), 500L,
            "ERR_TIMEOUT", "Request timed out",
            "dept1", "input text",
            "v1", "p1", "s1",
            "doc", "d1", "u1",
            2, null
        );

        assertEquals("TRIAGE", record.getCapabilityId());
        assertEquals("分诊能力", record.getCapabilityName());
        assertEquals("ERR_TIMEOUT", record.getErrorCode());
        assertEquals("Request timed out", record.getErrorMessage());
        assertEquals("input text", record.getInputSummary());
        assertEquals("dept1", record.getDepartmentId());
        assertNull(record.getModelId());
        assertEquals(0, record.getRetryCount());
        assertFalse(record.isDegraded());
        assertNull(record.getDegradationReason());
        assertNull(record.getTotalTokens());
        assertNull(record.getOutputSummary());
        assertEquals(Integer.valueOf(2), record.getPromptVersion());
    }

    @Test
    void degradedFactoryShouldCreateInstanceWithCorrectDefaults() {
        AiCallRecord record = AiCallRecord.degraded(
            "RX_AUDIT", "处方审核能力", LocalDateTime.now(), 300L,
            "ModelOverloaded", "claude-3",
            "dept1", "input text",
            "v1", "p1", "s1",
            "doc", "d1", "u1",
            "output text", 5, null
        );

        assertEquals("RX_AUDIT", record.getCapabilityId());
        assertEquals("处方审核能力", record.getCapabilityName());
        assertEquals("ModelOverloaded", record.getDegradationReason());
        assertEquals("claude-3", record.getModelId());
        assertEquals("input text", record.getInputSummary());
        assertEquals("output text", record.getOutputSummary());
        assertTrue(record.isDegraded());
        assertEquals(0, record.getRetryCount());
        assertNull(record.getErrorCode());
        assertNull(record.getErrorMessage());
        assertNull(record.getTotalTokens());
        assertEquals(Integer.valueOf(5), record.getPromptVersion());
    }

    @Test
    void sentinelReasonExperimentAssignErrorShouldOverridePromptVersionToMinusOne() {
        AiCallRecord record = AiCallRecord.success(
            "DIAGNOSIS", "诊断能力", LocalDateTime.now(), 100L,
            "dept1", "gpt-4", 0,
            10, 20, null, null,
            "v1", "p1", "s1",
            "doc", "d1", "u1",
            3, "EXPERIMENT_ASSIGN_ERROR"
        );

        assertEquals(Integer.valueOf(-1), record.getPromptVersion());
    }

    @Test
    void sentinelReasonOtherShouldKeepPromptVersionUnchanged() {
        AiCallRecord record = AiCallRecord.success(
            "DIAGNOSIS", "诊断能力", LocalDateTime.now(), 100L,
            "dept1", "gpt-4", 0,
            10, 20, null, null,
            "v1", "p1", "s1",
            "doc", "d1", "u1",
            3, "SOME_OTHER_REASON"
        );

        assertEquals(Integer.valueOf(3), record.getPromptVersion());
    }

    @Test
    void legacyConstructorShouldStillWorkWithDegradedState() {
        AiCallRecord record = new AiCallRecord(
            "RX_AUDIT", "gpt-4", "v1",
            "user1", "dept1", "session1",
            "visit1", "pat1", "system", "sys",
            500L, true, "Timeout",
            0, 0
        );

        assertTrue(record.isDegraded());
        assertEquals("Timeout", record.getDegradationReason());
        assertNull(record.getCapabilityName());
    }

    @Test
    void legacyConstructorShouldHandleNullPromptVersion() {
        AiCallRecord record = new AiCallRecord(
            "TRIAGE", "gpt-4", null,
            "user1", "dept1", "session1",
            "visit1", "pat1", "doctor", "doc1",
            100L, false, null,
            10, 5
        );

        assertNull(record.getPromptVersion());
    }

    @Test
    void all23GettersShouldBeAccessible() {
        LocalDateTime now = LocalDateTime.now();
        AiCallRecord record = AiCallRecord.success(
            "cap1", "capName1", now, 100L,
            "dept1", "model1", 1,
            10, 20, "in", "out",
            "v1", "p1", "s1",
            "role1", "cid1", "uid1",
            1, null
        );

        assertNotNull(record.getCallTime());
        assertNotNull(record.getCapabilityId());
        assertNotNull(record.getCapabilityName());
        assertNotNull(record.getVisitId());
        assertNotNull(record.getPatientId());
        assertNotNull(record.getDepartmentId());
        assertNotNull(record.getCallerRole());
        assertNotNull(record.getCallerId());
        assertNotNull(record.getUserId());
        assertNotNull(record.getInputSummary());
        assertNotNull(record.getOutputSummary());
        assertNotNull(record.getModelId());
        assertNotNull(record.getSessionId());
        assertNotNull(record.getPromptVersion());
        assertNotNull(record.getTotalTokens());
    }
}
