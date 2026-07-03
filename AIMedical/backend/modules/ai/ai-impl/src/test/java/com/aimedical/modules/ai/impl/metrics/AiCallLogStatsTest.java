package com.aimedical.modules.ai.impl.metrics;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class AiCallLogStatsTest {

    @Test
    void shouldConstructWithNoArgsConstructor() {
        AiCallLogStats stats = new AiCallLogStats();
        assertNotNull(stats);
    }

    @Test
    void shouldConstructWithAllArgsConstructor() {
        AiCallLogStats stats = new AiCallLogStats(
            1L, "TRIAGE", "2026-07",
            100L, 80L, 10L, 10L,
            250.0, 100.0, 800.0, 1500.0
        );

        assertEquals(1L, stats.getId());
        assertEquals("TRIAGE", stats.getCapabilityId());
        assertEquals("2026-07", stats.getStatMonth());
        assertEquals(100L, stats.getTotalCalls());
        assertEquals(80L, stats.getSuccessCount());
        assertEquals(10L, stats.getDegradedCount());
        assertEquals(10L, stats.getFailureCount());
        assertEquals(250.0, stats.getAvgElapsedMs(), 0.001);
        assertEquals(100.0, stats.getP50ElapsedMs(), 0.001);
        assertEquals(800.0, stats.getP95ElapsedMs(), 0.001);
        assertEquals(1500.0, stats.getP99ElapsedMs(), 0.001);
    }

    @Test
    void shouldSupportSetters() {
        AiCallLogStats stats = new AiCallLogStats();

        stats.setId(2L);
        stats.setCapabilityId("RX_AUDIT");
        stats.setStatMonth("2026-06");
        stats.setTotalCalls(50L);
        stats.setSuccessCount(40L);
        stats.setDegradedCount(5L);
        stats.setFailureCount(5L);
        stats.setAvgElapsedMs(180.5);
        stats.setP50ElapsedMs(120.0);
        stats.setP95ElapsedMs(500.0);
        stats.setP99ElapsedMs(900.0);

        assertEquals(2L, stats.getId());
        assertEquals("RX_AUDIT", stats.getCapabilityId());
        assertEquals("2026-06", stats.getStatMonth());
        assertEquals(50L, stats.getTotalCalls());
        assertEquals(40L, stats.getSuccessCount());
        assertEquals(5L, stats.getDegradedCount());
        assertEquals(5L, stats.getFailureCount());
        assertEquals(180.5, stats.getAvgElapsedMs(), 0.001);
        assertEquals(120.0, stats.getP50ElapsedMs(), 0.001);
        assertEquals(500.0, stats.getP95ElapsedMs(), 0.001);
        assertEquals(900.0, stats.getP99ElapsedMs(), 0.001);
    }

    @Test
    void jpaAnnotationsShouldBePresent() throws Exception {
        Entity entityAnn = AiCallLogStats.class.getAnnotation(Entity.class);
        assertNotNull(entityAnn);

        Table tableAnn = AiCallLogStats.class.getAnnotation(Table.class);
        assertNotNull(tableAnn);
        assertEquals("ai_call_log_stats", tableAnn.name());
        assertEquals(1, tableAnn.indexes().length);

        Index idx = tableAnn.indexes()[0];
        assertEquals("idx_stats_capability_month", idx.name());
        assertEquals("capabilityId, statMonth DESC", idx.columnList());

        Field idField = AiCallLogStats.class.getDeclaredField("id");
        assertNotNull(idField.getAnnotation(Id.class));
        GeneratedValue genVal = idField.getAnnotation(GeneratedValue.class);
        assertNotNull(genVal);
        assertEquals(GenerationType.IDENTITY, genVal.strategy());
    }
}
