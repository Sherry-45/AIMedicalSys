package com.aimedical.modules.ai.impl.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aimedical.modules.ai.api.degradation.DegradationContext;

import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowMetricsStoreTest {

    private SlidingWindowMetricsStore store;

    @BeforeEach
    void setUp() {
        store = new SlidingWindowMetricsStore();
    }

    @Test
    void shouldReturnZeroFailureRateForUnknownCapability() {
        assertEquals(0.0, store.getFailureRate("unknown"), 0.0);
    }

    @Test
    void shouldReturnZeroEffectiveFailureRateForUnknownCapability() {
        assertEquals(0.0, store.getEffectiveFailureRate("unknown"), 0.0);
    }

    @Test
    void shouldReturnZeroAverageElapsedForUnknownCapability() {
        assertEquals(0.0, store.getAverageElapsed("unknown"), 0.0);
    }

    @Test
    void shouldReturnZeroValueContextForUnknownCapability() {
        DegradationContext ctx = store.buildDegradationContext("unknown", "sync");
        assertNotNull(ctx);
        assertEquals(0, ctx.getInvocationCount());
        assertEquals(0, ctx.getFailureCount());
        assertEquals("sync", ctx.getRequestType());
    }

    @Test
    void shouldRecordSuccessAndComputeFailureRate() {
        store.recordSuccess("cap1", 100L);
        assertEquals(0.0, store.getFailureRate("cap1"), 0.0);
    }

    @Test
    void shouldRecordFailureAndComputeFailureRate() {
        store.recordFailure("cap1");
        assertEquals(1.0, store.getFailureRate("cap1"), 0.0);
    }

    @Test
    void shouldComputeFailureRateWithMixedEvents() {
        store.recordSuccess("cap1", 100L);
        store.recordSuccess("cap1", 100L);
        store.recordFailure("cap1");
        assertEquals(1.0 / 3.0, store.getFailureRate("cap1"), 0.0001);
    }

    @Test
    void getFailureRateShouldIncludeOnlySuccessAndFailure() {
        store.recordSuccess("cap1", 100L);
        store.recordDegraded("cap1", 100L);
        store.recordFailure("cap1");
        assertEquals(0.5, store.getFailureRate("cap1"), 0.0001);
    }

    @Test
    void getEffectiveFailureRateShouldIncludeAllEventsInDenominator() {
        store.recordSuccess("cap1", 100L);
        store.recordDegraded("cap1", 100L);
        store.recordFailure("cap1");
        assertEquals(1.0 / 3.0, store.getEffectiveFailureRate("cap1"), 0.0001);
    }

    @Test
    void getEffectiveFailureRateShouldReturnZeroWhenNoFailures() {
        store.recordSuccess("cap1", 100L);
        store.recordDegraded("cap1", 100L);
        assertEquals(0.0, store.getEffectiveFailureRate("cap1"), 0.0);
    }

    @Test
    void getAverageElapsedShouldReturnCorrectAverage() {
        store.recordSuccess("cap1", 100L);
        store.recordSuccess("cap1", 200L);
        assertEquals(150.0, store.getAverageElapsed("cap1"), 0.0001);
    }

    @Test
    void getAverageElapsedShouldIncludeDegradedEvents() {
        store.recordSuccess("cap1", 100L);
        store.recordDegraded("cap1", 300L);
        assertEquals(200.0, store.getAverageElapsed("cap1"), 0.0001);
    }

    @Test
    void getAverageElapsedShouldIncludeFailureWithZeroElapsed() {
        store.recordSuccess("cap1", 100L);
        store.recordFailure("cap1");
        assertEquals(50.0, store.getAverageElapsed("cap1"), 0.0001);
    }

    @Test
    void buildDegradationContextShouldComputeCorrectStats() {
        store.recordSuccess("cap1", 100L);
        store.recordSuccess("cap1", 200L);
        store.recordDegraded("cap1", 150L);
        store.recordFailure("cap1");
        long beforeBuild = System.currentTimeMillis();
        DegradationContext ctx = store.buildDegradationContext("cap1", "sync");
        long afterBuild = System.currentTimeMillis();
        assertTrue(ctx.isInitialized());
        assertEquals(4, ctx.getInvocationCount());
        assertEquals(1, ctx.getFailureCount());
        assertEquals(150L, ctx.getElapsedTime());
        assertTrue(ctx.getSerializedTimestamp() >= beforeBuild,
                "serializedTimestamp should be >= before buildDegradationContext");
        assertTrue(ctx.getSerializedTimestamp() <= afterBuild + 50,
                "serializedTimestamp should be <= after buildDegradationContext + 50ms slack");
        assertEquals("sync", ctx.getRequestType());
    }

    @Test
    void buildDegradationContextLastFailureTimeShouldBeZeroWhenNoFailures() {
        store.recordSuccess("cap1", 100L);
        DegradationContext ctx = store.buildDegradationContext("cap1", "sync");
        assertEquals(0L, ctx.getLastFailureTime());
    }

    @Test
    void buildDegradationContextShouldReturnMaxLastFailureTimeForMultipleFailures() {
        store.recordSuccess("cap1", 100L);
        store.recordFailure("cap1");
        DegradationContext ctx1 = store.buildDegradationContext("cap1", "sync");
        long first = ctx1.getLastFailureTime();
        assertTrue(first > 0);

        long beforeSecond = System.currentTimeMillis();
        store.recordFailure("cap1");
        long afterSecond = System.currentTimeMillis();
        DegradationContext ctx2 = store.buildDegradationContext("cap1", "sync");
        assertTrue(ctx2.getLastFailureTime() >= beforeSecond,
                "lastFailureTime should be >= second failure recording start");
        assertTrue(ctx2.getLastFailureTime() <= afterSecond,
                "lastFailureTime should be <= second failure recording end");
        assertTrue(ctx2.getLastFailureTime() >= first,
                "second lastFailureTime should be strictly greater than first");
    }

    @Test
    void setWindowSecondsShouldThrowWhenZero() {
        assertThrows(IllegalArgumentException.class, () -> store.setWindowSeconds(0));
    }

    @Test
    void setWindowSecondsShouldThrowWhenNegative() {
        assertThrows(IllegalArgumentException.class, () -> store.setWindowSeconds(-1));
    }

    @Test
    void setWindowSecondsShouldAcceptPositive() {
        store.setWindowSeconds(120);
        // no exception expected
        assertEquals(0.0, store.getFailureRate("new-cap"), 0.0);
    }

    @Test
    void lazyEvictionShouldNotRemoveEventsWithinWindow() {
        store.setWindowSeconds(600);
        store.recordSuccess("cap1", 100L);
        assertEquals(0.0, store.getFailureRate("cap1"), 0.0);
    }

    @Test
    void maxEventsShouldNotBeExceeded() {
        for (int i = 0; i < 12000; i++) {
            store.recordSuccess("cap1", i);
        }
        DegradationContext ctx = store.buildDegradationContext("cap1", "sync");
        assertTrue(ctx.isInitialized());
        // maxEventsPerCapability = 10000, all 12000 events within 60s window
        assertEquals(10000, ctx.getInvocationCount());
    }

    @Test
    void concurrentAccessShouldNotCauseDeadlock() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                store.recordSuccess("cap1", i);
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                store.recordFailure("cap1");
            }
        });
        Thread t3 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                store.getFailureRate("cap1");
                store.getEffectiveFailureRate("cap1");
                store.getAverageElapsed("cap1");
            }
        });
        t1.start();
        t2.start();
        t3.start();
        t1.join(5000);
        t2.join(5000);
        t3.join(5000);
        assertFalse(t1.isAlive(), "t1 should complete within 5000ms");
        assertFalse(t2.isAlive(), "t2 should complete within 5000ms");
        assertFalse(t3.isAlive(), "t3 should complete within 5000ms");

        DegradationContext ctx = store.buildDegradationContext("cap1", "sync");
        assertTrue(ctx.isInitialized());
        assertEquals(2000, ctx.getInvocationCount(),
                "all 2000 events should be recorded within window");
        double rate = store.getFailureRate("cap1");
        assertTrue(rate >= 0.45 && rate <= 0.55,
                "expected failure rate ~0.5 but got " + rate);
    }
}
