package com.aimedical.modules.ai.impl.metrics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AiCallLogEntityTest {

    @Test
    void shouldConstructWithNoArgsConstructor() {
        AiCallLogEntity entity = new AiCallLogEntity();
        assertNotNull(entity);
    }

    @Test
    void shouldConstructWithAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        AiCallLogEntity entity = new AiCallLogEntity(
            1L, now,
            "TRIAGE", "TRIAGE",
            "visit1", "pat1",
            "dept1", "doctor", "doc1",
            "user1",
            "input", "output",
            true, "Timeout",
            500L,
            "ERR001", "Something went wrong",
            "gpt-4", 2,
            "session1", 3,
            100, 50, 150
        );

        assertEquals(1L, entity.getId());
        assertEquals(now, entity.getCallTime());
        assertEquals("TRIAGE", entity.getCapabilityId());
        assertEquals("TRIAGE", entity.getCapabilityName());
        assertEquals("visit1", entity.getVisitId());
        assertEquals("pat1", entity.getPatientId());
        assertEquals("dept1", entity.getDepartmentId());
        assertEquals("doctor", entity.getCallerRole());
        assertEquals("doc1", entity.getCallerId());
        assertEquals("user1", entity.getUserId());
        assertEquals("input", entity.getInputSummary());
        assertEquals("output", entity.getOutputSummary());
        assertTrue(entity.isDegraded());
        assertEquals("Timeout", entity.getDegradationReason());
        assertEquals(500L, entity.getElapsedMs());
        assertEquals("ERR001", entity.getErrorCode());
        assertEquals("Something went wrong", entity.getErrorMessage());
        assertEquals("gpt-4", entity.getModelId());
        assertEquals(2, entity.getRetryCount());
        assertEquals("session1", entity.getSessionId());
        assertEquals(Integer.valueOf(3), entity.getPromptVersion());
        assertEquals(100, entity.getPromptTokens());
        assertEquals(50, entity.getCompletionTokens());
        assertEquals(Integer.valueOf(150), entity.getTotalTokens());
    }

    @Test
    void shouldSupportSetters() {
        LocalDateTime now = LocalDateTime.now();
        AiCallLogEntity entity = new AiCallLogEntity();

        entity.setId(10L);
        entity.setCallTime(now);
        entity.setCapabilityId("DIAGNOSIS");
        entity.setCapabilityName("DIAGNOSIS");
        entity.setVisitId("v100");
        entity.setPatientId("p200");
        entity.setDepartmentId("dept-3");
        entity.setCallerRole("nurse");
        entity.setCallerId("nurse-01");
        entity.setUserId("u99");
        entity.setInputSummary("in");
        entity.setOutputSummary("out");
        entity.setDegraded(true);
        entity.setDegradationReason("Fallback");
        entity.setElapsedMs(999L);
        entity.setErrorCode("E999");
        entity.setErrorMessage("error");
        entity.setModelId("claude-3");
        entity.setRetryCount(3);
        entity.setSessionId("sess-xyz");
        entity.setPromptVersion(5);
        entity.setPromptTokens(200);
        entity.setCompletionTokens(100);
        entity.setTotalTokens(300);

        assertEquals(10L, entity.getId());
        assertEquals(now, entity.getCallTime());
        assertEquals("DIAGNOSIS", entity.getCapabilityId());
        assertEquals("DIAGNOSIS", entity.getCapabilityName());
        assertEquals("v100", entity.getVisitId());
        assertEquals("p200", entity.getPatientId());
        assertEquals("dept-3", entity.getDepartmentId());
        assertEquals("nurse", entity.getCallerRole());
        assertEquals("nurse-01", entity.getCallerId());
        assertEquals("u99", entity.getUserId());
        assertEquals("in", entity.getInputSummary());
        assertEquals("out", entity.getOutputSummary());
        assertTrue(entity.isDegraded());
        assertEquals("Fallback", entity.getDegradationReason());
        assertEquals(999L, entity.getElapsedMs());
        assertEquals("E999", entity.getErrorCode());
        assertEquals("error", entity.getErrorMessage());
        assertEquals("claude-3", entity.getModelId());
        assertEquals(3, entity.getRetryCount());
        assertEquals("sess-xyz", entity.getSessionId());
        assertEquals(Integer.valueOf(5), entity.getPromptVersion());
        assertEquals(200, entity.getPromptTokens());
        assertEquals(100, entity.getCompletionTokens());
        assertEquals(Integer.valueOf(300), entity.getTotalTokens());
    }

    @Test
    void shouldHandleNullOptionalFields() {
        AiCallLogEntity entity = new AiCallLogEntity();

        entity.setInputSummary(null);
        entity.setOutputSummary(null);
        entity.setErrorCode(null);
        entity.setErrorMessage(null);
        entity.setTotalTokens(null);
        entity.setPromptVersion(null);

        assertNull(entity.getInputSummary());
        assertNull(entity.getOutputSummary());
        assertNull(entity.getErrorCode());
        assertNull(entity.getErrorMessage());
        assertNull(entity.getTotalTokens());
        assertNull(entity.getPromptVersion());
    }

    @Test
    void jpaAnnotationsShouldBePresent() throws Exception {
        Entity entityAnn = AiCallLogEntity.class.getAnnotation(Entity.class);
        assertNotNull(entityAnn);

        Table tableAnn = AiCallLogEntity.class.getAnnotation(Table.class);
        assertNotNull(tableAnn);
        assertEquals("ai_call_log", tableAnn.name());
        assertEquals(3, tableAnn.indexes().length);

        Index idxCallTime = tableAnn.indexes()[0];
        assertEquals("idx_call_time", idxCallTime.name());
        assertEquals("callTime", idxCallTime.columnList());

        Index idxCapCallTime = tableAnn.indexes()[1];
        assertEquals("idx_capability_call_time", idxCapCallTime.name());
        assertEquals("capabilityId, callTime DESC", idxCapCallTime.columnList());

        Index idxDegCallTime = tableAnn.indexes()[2];
        assertEquals("idx_degraded_call_time", idxDegCallTime.name());
        assertEquals("degraded, callTime DESC", idxDegCallTime.columnList());

        Field idField = AiCallLogEntity.class.getDeclaredField("id");
        assertNotNull(idField.getAnnotation(Id.class));
        GeneratedValue genVal = idField.getAnnotation(GeneratedValue.class);
        assertNotNull(genVal);
        assertEquals(GenerationType.IDENTITY, genVal.strategy());

        Field callTimeField = AiCallLogEntity.class.getDeclaredField("callTime");
        Column callTimeCol = callTimeField.getAnnotation(Column.class);
        assertNotNull(callTimeCol);
        assertFalse(callTimeCol.nullable());
    }
}
