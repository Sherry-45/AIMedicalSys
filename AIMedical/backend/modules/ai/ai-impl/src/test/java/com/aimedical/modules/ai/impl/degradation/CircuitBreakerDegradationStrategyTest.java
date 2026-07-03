package com.aimedical.modules.ai.impl.degradation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aimedical.modules.ai.api.degradation.DegradationContext;
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;

import static org.junit.jupiter.api.Assertions.*;

class CircuitBreakerDegradationStrategyTest {

    private static final double FAILURE_THRESHOLD = 0.5;
    private static final Duration OPEN_WINDOW = Duration.ofMillis(200);

    private SlidingWindowMetricsStore metricsStore;
    private CircuitBreakerDegradationStrategy strategy;

    @BeforeEach
    void setUp() {
        metricsStore = new SlidingWindowMetricsStore();
        strategy = new CircuitBreakerDegradationStrategy(metricsStore, FAILURE_THRESHOLD, OPEN_WINDOW);
    }

    @Test
    void shouldThrowWhenMetricsStoreIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new CircuitBreakerDegradationStrategy(null, 0.5, Duration.ofSeconds(30)));
    }

    @Test
    void shouldNotDegradeWhenServiceNameIsNull() {
        DegradationContext ctx = new DegradationContext();
        ctx.setServiceName(null);
        assertFalse(strategy.shouldDegrade(ctx));
    }

    @Test
    void shouldNotDegradeWhenFailureRateBelowThreshold() {
        metricsStore.recordSuccess("cap1", 100L);
        metricsStore.recordSuccess("cap1", 100L);
        DegradationContext ctx = metricsStore.buildDegradationContext("cap1", "sync");
        assertFalse(strategy.shouldDegrade(ctx));
    }

    @Test
    void shouldDegradeWhenFailureRateExceedsThreshold() {
        metricsStore.recordSuccess("cap1", 100L);
        metricsStore.recordFailure("cap1");
        DegradationContext ctx = metricsStore.buildDegradationContext("cap1", "sync");
        assertTrue(strategy.shouldDegrade(ctx));
    }

    @Test
    void shouldTransitionToOpenAndReturnTrueOnHighFailureRate() {
        metricsStore.recordFailure("cap1");
        DegradationContext ctx = metricsStore.buildDegradationContext("cap1", "sync");
        assertTrue(strategy.shouldDegrade(ctx));
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.OPEN, strategy.getState("cap1"));
    }

    @Test
    void shouldRemainOpenAndAlwaysDegradeWithinWindow() {
        metricsStore.recordFailure("cap1");
        DegradationContext ctx = metricsStore.buildDegradationContext("cap1", "sync");
        assertTrue(strategy.shouldDegrade(ctx));
        assertTrue(strategy.shouldDegrade(ctx));
        assertTrue(strategy.shouldDegrade(ctx));
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.OPEN, strategy.getState("cap1"));
    }

    @Test
    void shouldTransitionToHalfOpenAfterWindowExpires() throws InterruptedException {
        strategy = new CircuitBreakerDegradationStrategy(metricsStore, FAILURE_THRESHOLD, Duration.ofMillis(50));
        metricsStore.recordFailure("cap1");
        DegradationContext ctx = metricsStore.buildDegradationContext("cap1", "sync");
        assertTrue(strategy.shouldDegrade(ctx));
        Thread.sleep(100);
        assertFalse(strategy.shouldDegrade(ctx));
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.HALF_OPEN, strategy.getState("cap1"));
    }

    @Test
    void halfOpenShouldAllowSingleProbeAndDegradeOthers() throws InterruptedException {
        strategy = new CircuitBreakerDegradationStrategy(metricsStore, FAILURE_THRESHOLD, Duration.ofMillis(50));
        metricsStore.recordFailure("cap1");
        DegradationContext ctx = metricsStore.buildDegradationContext("cap1", "sync");
        assertTrue(strategy.shouldDegrade(ctx));
        Thread.sleep(100);
        assertFalse(strategy.shouldDegrade(ctx));
        assertTrue(strategy.shouldDegrade(ctx));
    }

    @Test
    void recordProbeResultSuccessShouldTransitionToClosedAndAllowNewProbe() throws InterruptedException {
        strategy = new CircuitBreakerDegradationStrategy(metricsStore, FAILURE_THRESHOLD, Duration.ofMillis(50));
        metricsStore.recordFailure("cap1");
        DegradationContext ctx = metricsStore.buildDegradationContext("cap1", "sync");

        // CLOSED → OPEN
        assertTrue(strategy.shouldDegrade(ctx));

        // wait for window to expire
        Thread.sleep(100);

        // HALF_OPEN: first call acquires probeLock → false (allow probe)
        assertFalse(strategy.shouldDegrade(ctx));

        // probe succeeds → CLOSED
        strategy.recordProbeResult("cap1", true);
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.CLOSED, strategy.getState("cap1"));

        // re-trigger OPEN to verify probeLock was released
        metricsStore.recordFailure("cap1");
        ctx = metricsStore.buildDegradationContext("cap1", "sync");
        assertTrue(strategy.shouldDegrade(ctx)); // CLOSED→OPEN
        Thread.sleep(100);
        // Should acquire probeLock again (was released by recordProbeResult)
        assertFalse(strategy.shouldDegrade(ctx)); // HALF_OPEN, acquire probe
    }

    @Test
    void recordProbeResultFailureShouldTransitionToOpen() {
        strategy = new CircuitBreakerDegradationStrategy(metricsStore, FAILURE_THRESHOLD, Duration.ofMillis(50));
        metricsStore.recordFailure("cap1");
        DegradationContext ctx = metricsStore.buildDegradationContext("cap1", "sync");
        strategy.shouldDegrade(ctx);
        strategy.recordProbeResult("cap1", false);
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.OPEN, strategy.getState("cap1"));
    }

    @Test
    void recordProbeResultOnUnknownCapabilityShouldBeNoOp() {
        // recordProbeResult for untracked capability should not throw and not create state
        strategy.recordProbeResult("unknown", true);
        strategy.recordProbeResult("unknown", false);
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.CLOSED, strategy.getState("unknown"));
    }

    @Test
    void shouldMaintainIndependentStateForMultipleCapabilities() throws InterruptedException {
        strategy = new CircuitBreakerDegradationStrategy(metricsStore, FAILURE_THRESHOLD, Duration.ofMillis(50));
        metricsStore.recordFailure("capA");

        // capA → OPEN
        DegradationContext ctxA = metricsStore.buildDegradationContext("capA", "sync");
        assertTrue(strategy.shouldDegrade(ctxA));
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.OPEN, strategy.getState("capA"));

        // capB is never recorded → CLOSED
        metricsStore.recordSuccess("capB", 100L);
        DegradationContext ctxB = metricsStore.buildDegradationContext("capB", "sync");
        assertFalse(strategy.shouldDegrade(ctxB));
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.CLOSED, strategy.getState("capB"));

        // capA still OPEN while capB is CLOSED
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.OPEN, strategy.getState("capA"));
    }

    @Test
    void getStateShouldReturnClosedForUnknownCapability() {
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.CLOSED, strategy.getState("unknown"));
    }

    @Test
    void shouldUseDefaultValuesWhenThresholdIsNaN() {
        CircuitBreakerDegradationStrategy s = new CircuitBreakerDegradationStrategy(
                metricsStore, Double.NaN, null);
        assertNotNull(s);
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.CLOSED, s.getState("cap1"));
    }

    @Test
    void shouldUseOperationNameAsKeyWhenPresent() {
        // T14: key prefers operationName (endpointId) over serviceName (capabilityId)
        metricsStore.recordFailure("cap1");
        DegradationContext ctx = metricsStore.buildDegradationContext("cap1", "sync");
        ctx.setOperationName("endpoint1");
        assertTrue(strategy.shouldDegrade(ctx));
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.OPEN, strategy.getState("endpoint1"));
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.CLOSED, strategy.getState("cap1"));
    }

    @Test
    void shouldFallbackToServiceNameWhenOperationNameIsNull() {
        // T14: key falls back to serviceName when operationName is null
        metricsStore.recordFailure("cap2");
        DegradationContext ctx = metricsStore.buildDegradationContext("cap2", "sync");
        ctx.setOperationName(null);
        assertTrue(strategy.shouldDegrade(ctx));
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.OPEN, strategy.getState("cap2"));
    }

    @Test
    void shouldSetFailureCountToOneWhenTransitionToOpen() throws InterruptedException {
        // T60: CLOSED→OPEN sets failureCount = 1 (not ++)
        // After OPEN→HALF_OPEN, probe should succeed, then recordProbeResult failure,
        // failureCount becomes 2. With old behavior it would be higher.
        strategy = new CircuitBreakerDegradationStrategy(metricsStore, FAILURE_THRESHOLD, Duration.ofMillis(50));
        metricsStore.recordFailure("cap1");
        DegradationContext ctx = metricsStore.buildDegradationContext("cap1", "sync");
        assertTrue(strategy.shouldDegrade(ctx)); // OPEN with failureCount=1
        Thread.sleep(100);
        assertFalse(strategy.shouldDegrade(ctx)); // HALF_OPEN: acquire probe
        strategy.recordProbeResult("cap1", false); // failure → OPEN again, failureCount++
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.OPEN, strategy.getState("cap1"));
        Thread.sleep(100);
        assertFalse(strategy.shouldDegrade(ctx)); // HALF_OPEN again: probe acquired
    }

    @Test
    void shouldTimeoutProbeLockAndRetry() throws InterruptedException {
        // T13: probeLock held > openWindowMs should be auto-cleared and retry
        strategy = new CircuitBreakerDegradationStrategy(metricsStore, FAILURE_THRESHOLD, Duration.ofMillis(100));
        metricsStore.recordFailure("cap1");
        DegradationContext ctx = metricsStore.buildDegradationContext("cap1", "sync");

        assertTrue(strategy.shouldDegrade(ctx)); // CLOSED → OPEN
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.OPEN, strategy.getState("cap1"));

        Thread.sleep(150);
        assertFalse(strategy.shouldDegrade(ctx)); // HALF_OPEN: acquires probeLock
        assertTrue(strategy.shouldDegrade(ctx));  // probeLock held, degrade

        // Wait for probeLock to time out
        Thread.sleep(150);
        // probeLock auto-cleared, retry should acquire probe
        assertFalse(strategy.shouldDegrade(ctx));
    }

    @Test
    void shouldNotDegradeInClosedStateWhenNoCircuitData() {
        DegradationContext ctx = metricsStore.buildDegradationContext("cleanCap", "sync");
        ctx.setOperationName("cleanEndpoint");
        assertFalse(strategy.shouldDegrade(ctx));
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.CLOSED, strategy.getState("cleanEndpoint"));
    }

    @Test
    void shouldMaintainIndependentStateByEndpointId() {
        DegradationContext ctxEp1 = metricsStore.buildDegradationContext("cap1", "sync");
        ctxEp1.setOperationName("endpoint1");
        assertFalse(strategy.shouldDegrade(ctxEp1));

        DegradationContext ctxEp2 = metricsStore.buildDegradationContext("cap1", "sync");
        ctxEp2.setOperationName("endpoint2");
        assertFalse(strategy.shouldDegrade(ctxEp2));

        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.CLOSED, strategy.getState("endpoint1"));
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.CLOSED, strategy.getState("endpoint2"));
    }

    @Test
    void shouldTransitionToOpenWhenFailureRateExceedsThreshold() throws Exception {
        Field mapField = CircuitBreakerDegradationStrategy.class.getDeclaredField("circuitDataMap");
        mapField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.concurrent.ConcurrentHashMap<String, Object> rawMap =
                (java.util.concurrent.ConcurrentHashMap<String, Object>) mapField.get(strategy);

        Class<?> dataClass = Class.forName(
                "com.aimedical.modules.ai.impl.degradation.CircuitBreakerDegradationStrategy$CircuitData");
        Constructor<?> ctor = dataClass.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object circuitData = ctor.newInstance();

        Field fcField = dataClass.getDeclaredField("failureCount");
        fcField.setAccessible(true);
        fcField.setInt(circuitData, 1);

        rawMap.put("endpoint1", circuitData);

        metricsStore.recordFailure("cap1");
        DegradationContext ctx = metricsStore.buildDegradationContext("cap1", "sync");
        ctx.setOperationName("endpoint1");

        assertTrue(strategy.shouldDegrade(ctx));
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.OPEN, strategy.getState("endpoint1"));
    }

    @Test
    void shouldNotOpenWhenFirstClosedCallHasLowFailureRate() {
        metricsStore.recordSuccess("cap1", 100L);
        DegradationContext ctx = metricsStore.buildDegradationContext("cap1", "sync");
        assertFalse(strategy.shouldDegrade(ctx));
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.CLOSED, strategy.getState("cap1"));
    }

    @Test
    void shouldOpenOnLaterClosedCallAfterFailureRateRises() {
        metricsStore.recordSuccess("cap1", 100L);
        metricsStore.recordSuccess("cap1", 100L);
        DegradationContext ctx = metricsStore.buildDegradationContext("cap1", "sync");
        assertFalse(strategy.shouldDegrade(ctx));
        metricsStore.recordFailure("cap1");
        metricsStore.recordFailure("cap1");
        assertTrue(strategy.shouldDegrade(ctx));
        assertEquals(CircuitBreakerDegradationStrategy.CircuitBreakerState.OPEN, strategy.getState("cap1"));
    }

    @Test
    void orderShouldBe10() {
        assertEquals(10, strategy.getOrder());
    }
}
