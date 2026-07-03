package com.aimedical.modules.ai.impl.metrics;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingMetricsCollectorTest {

    @Mock
    private AiCallLogRepository repository;

    @Captor
    private ArgumentCaptor<AiCallLogEntity> entityCaptor;

    private LoggingMetricsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new LoggingMetricsCollector(repository);
    }

    @Test
    void shouldSaveEntityWhenRecordCalled() {
        AiCallRecord record = new AiCallRecord(
            "TRIAGE", "gpt-4", "2",
            "user1", "dept1", "session1",
            "visit1", "pat1", "doctor", "doc1",
            1500L, false, null,
            100, 50
        );

        collector.record(record);

        verify(repository).save(entityCaptor.capture());
        AiCallLogEntity entity = entityCaptor.getValue();
        assertNotNull(entity.getCallTime());
        assertEquals("TRIAGE", entity.getCapabilityId());
        assertEquals("gpt-4", entity.getModelId());
        assertEquals(Integer.valueOf(2), entity.getPromptVersion());
        assertEquals(1500L, entity.getElapsedMs());
        assertEquals(100, entity.getPromptTokens());
        assertEquals(50, entity.getCompletionTokens());
    }

    @Test
    void shouldHandleNullRecordGracefully() {
        Logger logbackLogger = (Logger) LoggerFactory.getLogger(LoggingMetricsCollector.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logbackLogger.addAppender(listAppender);

        collector.record(null);

        verifyNoInteractions(repository);
        assertEquals(1, listAppender.list.size());
        assertEquals(Level.WARN, listAppender.list.get(0).getLevel());
        assertEquals("AiCallRecord is null, skipping persistence", listAppender.list.get(0).getMessage());

        logbackLogger.detachAppender(listAppender);
    }

    @Test
    void shouldHandleRepositoryExceptionGracefully() {
        Logger logbackLogger = (Logger) LoggerFactory.getLogger(LoggingMetricsCollector.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logbackLogger.addAppender(listAppender);

        AiCallRecord record = new AiCallRecord(
            "TRIAGE", "gpt-4", "v1",
            "user1", "dept1", "session1",
            "visit1", "pat1", "doctor", "doc1",
            100L, false, null,
            10, 5
        );

        doThrow(new RuntimeException("DB error")).when(repository).save(any());

        assertDoesNotThrow(() -> collector.record(record));

        assertEquals(1, listAppender.list.size());
        assertEquals(Level.WARN, listAppender.list.get(0).getLevel());
        assertTrue(listAppender.list.get(0).getMessage().contains("Failed to persist AiCallLogEntity"));

        logbackLogger.detachAppender(listAppender);
    }

    @Test
    void shouldSetCallTimeToNow() {
        AiCallRecord record = new AiCallRecord(
            "TRIAGE", "gpt-4", null,
            null, null, null,
            null, null, null, null,
            0L, false, null,
            0, 0
        );

        LocalDateTime before = LocalDateTime.now();
        collector.record(record);
        LocalDateTime after = LocalDateTime.now();

        verify(repository).save(entityCaptor.capture());
        LocalDateTime callTime = entityCaptor.getValue().getCallTime();
        assertNotNull(callTime);
        assertTrue(Duration.between(before, callTime).abs().toSeconds() < 1,
            "callTime should be close to LocalDateTime.now()");
        assertTrue(Duration.between(after, callTime).abs().toSeconds() < 1,
            "callTime should be close to LocalDateTime.now()");
    }

    @Test
    void shouldMapFieldsCorrectly() {
        AiCallRecord record = new AiCallRecord(
            "DIAGNOSIS", "claude-3", "v3",
            "dr-001", "cardio", "sess-abc",
            "vis-999", "pat-456", "specialist", "dr-zhang",
            2345L, true, "ModelUnavailable",
            200, 80
        );

        collector.record(record);

        verify(repository).save(entityCaptor.capture());
        AiCallLogEntity entity = entityCaptor.getValue();

        assertEquals("DIAGNOSIS", entity.getCapabilityId());
        assertEquals("claude-3", entity.getModelId());
        assertEquals("dr-001", entity.getUserId());
        assertEquals("cardio", entity.getDepartmentId());
        assertEquals("sess-abc", entity.getSessionId());
        assertEquals("vis-999", entity.getVisitId());
        assertEquals("pat-456", entity.getPatientId());
        assertEquals("specialist", entity.getCallerRole());
        assertEquals("dr-zhang", entity.getCallerId());
        assertTrue(entity.isDegraded());
        assertEquals("ModelUnavailable", entity.getDegradationReason());
        assertEquals(2345L, entity.getElapsedMs());
        assertEquals(200, entity.getPromptTokens());
        assertEquals(80, entity.getCompletionTokens());
    }

    @Test
    void shouldHandleNullPromptVersion() {
        AiCallRecord record = new AiCallRecord(
            "TRIAGE", "gpt-4", null,
            null, null, null,
            null, null, null, null,
            0L, false, null,
            0, 0
        );

        collector.record(record);
        verify(repository).save(entityCaptor.capture());
        assertNull(entityCaptor.getValue().getPromptVersion());
    }

    @Test
    void shouldParseValidPromptVersion() {
        AiCallRecord record = new AiCallRecord(
            "TRIAGE", "gpt-4", "5",
            null, null, null,
            null, null, null, null,
            0L, false, null,
            0, 0
        );

        collector.record(record);
        verify(repository).save(entityCaptor.capture());
        assertEquals(Integer.valueOf(5), entityCaptor.getValue().getPromptVersion());
    }

    @Test
    void shouldHandleInvalidPromptVersion() {
        AiCallRecord record = new AiCallRecord(
            "TRIAGE", "gpt-4", "abc",
            null, null, null,
            null, null, null, null,
            0L, false, null,
            0, 0
        );

        collector.record(record);
        verify(repository).save(entityCaptor.capture());
        assertNull(entityCaptor.getValue().getPromptVersion());
    }

    @Test
    void shouldSetCapabilityNameFromRecord() {
        AiCallRecord record = new AiCallRecord(
            "RX_AUDIT", "gpt-4", "v1",
            null, null, null,
            null, null, null, null,
            0L, false, null,
            0, 0
        );

        collector.record(record);

        verify(repository).save(entityCaptor.capture());
        assertNull(entityCaptor.getValue().getCapabilityName());
    }

    @Test
    void shouldMapNewFieldsFromRecord() {
        LocalDateTime now = LocalDateTime.now();
        AiCallRecord record = AiCallRecord.success(
            "DIAGNOSIS", "诊断能力", now, 1500L,
            "cardio", "gpt-4", 3,
            100, 50, "input text", "output text",
            "visit1", "pat1", "session1",
            "doctor", "doc1", "user1",
            2, null
        );

        collector.record(record);

        verify(repository).save(entityCaptor.capture());
        AiCallLogEntity entity = entityCaptor.getValue();

        assertEquals("诊断能力", entity.getCapabilityName());
        assertEquals("input text", entity.getInputSummary());
        assertEquals("output text", entity.getOutputSummary());
        assertEquals(3, entity.getRetryCount());
        assertEquals(Integer.valueOf(150), entity.getTotalTokens());
    }

    @Test
    void shouldMapErrorFieldsFromFailureRecord() {
        AiCallRecord record = AiCallRecord.failure(
            "TRIAGE", "分诊能力", LocalDateTime.now(), 500L,
            "ERR_TIMEOUT", "timeout occurred",
            "dept1", "input",
            "v1", "p1", "s1",
            "doc", "d1", "u1",
            1, null
        );

        collector.record(record);

        verify(repository).save(entityCaptor.capture());
        AiCallLogEntity entity = entityCaptor.getValue();

        assertEquals("ERR_TIMEOUT", entity.getErrorCode());
        assertEquals("timeout occurred", entity.getErrorMessage());
        assertEquals(0, entity.getRetryCount());
        assertNull(entity.getModelId());
        assertNull(entity.getTotalTokens());
    }

    @Test
    void shouldMapDegradedFieldsFromDegradedRecord() {
        AiCallRecord record = AiCallRecord.degraded(
            "RX_AUDIT", "处方审核", LocalDateTime.now(), 300L,
            "Overloaded", "claude-3",
            "dept1", "input text",
            "v1", "p1", "s1",
            "doc", "d1", "u1",
            "output", 3, null
        );

        collector.record(record);

        verify(repository).save(entityCaptor.capture());
        AiCallLogEntity entity = entityCaptor.getValue();

        assertEquals("处方审核", entity.getCapabilityName());
        assertEquals("Overloaded", entity.getDegradationReason());
        assertEquals("claude-3", entity.getModelId());
        assertTrue(entity.isDegraded());
        assertNull(entity.getErrorCode());
        assertNull(entity.getErrorMessage());
    }

    @Test
    void shouldSetCallTimeFromRecordNotNow() {
        LocalDateTime specificTime = LocalDateTime.of(2026, 7, 3, 12, 0, 0);
        AiCallRecord record = AiCallRecord.success(
            "DIAGNOSIS", "诊断能力", specificTime, 100L,
            "dept1", "gpt-4", 0,
            10, 20, null, null,
            "v1", "p1", "s1",
            "doc", "d1", "u1",
            1, null
        );

        collector.record(record);

        verify(repository).save(entityCaptor.capture());
        assertEquals(specificTime, entityCaptor.getValue().getCallTime());
    }

    @Test
    void shouldSetNullOptionalFieldsWhenRecordReturnsNull() {
        AiCallRecord record = new AiCallRecord(
            "TRIAGE", "gpt-4", null,
            null, null, null,
            null, null, null, null,
            0L, false, null,
            0, 0
        );

        collector.record(record);

        verify(repository).save(entityCaptor.capture());
        AiCallLogEntity entity = entityCaptor.getValue();

        assertNull(entity.getInputSummary());
        assertNull(entity.getOutputSummary());
        assertNull(entity.getErrorCode());
        assertNull(entity.getErrorMessage());
        assertNull(entity.getTotalTokens());
        assertEquals(0, entity.getRetryCount());
    }

    @Test
    void recordMethodShouldBeAnnotatedWithAsyncMetricsAsyncExecutor() throws Exception {
        java.lang.reflect.Method method = LoggingMetricsCollector.class.getMethod("record", AiCallRecord.class);
        org.springframework.scheduling.annotation.Async annotation =
            method.getAnnotation(org.springframework.scheduling.annotation.Async.class);
        assertNotNull(annotation);
        assertEquals("metricsAsyncExecutor", annotation.value());
    }

    @Test
    void shouldNotThrowWhenConcurrentlyCalled() throws InterruptedException {
        AiCallRecord record = new AiCallRecord(
            "TRIAGE", "gpt-4", "v1",
            "user1", "dept1", "session1",
            "visit1", "pat1", "doctor", "doc1",
            100L, false, null,
            10, 5
        );

        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicBoolean anyException = new AtomicBoolean(false);

        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(() -> {
                try {
                    collector.record(record);
                } catch (Exception e) {
                    anyException.set(true);
                } finally {
                    latch.countDown();
                }
            });
            t.start();
        }

        latch.await();
        assertFalse(anyException.get());
        verify(repository, times(threadCount)).save(any());
    }
}
