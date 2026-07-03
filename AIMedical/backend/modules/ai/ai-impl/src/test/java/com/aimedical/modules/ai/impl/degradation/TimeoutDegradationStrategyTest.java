package com.aimedical.modules.ai.impl.degradation;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aimedical.modules.ai.api.degradation.DegradationContext;

import static org.junit.jupiter.api.Assertions.*;

class TimeoutDegradationStrategyTest {

    private static final long THRESHOLD_MS = 100L;
    private static final double THRESHOLD_RATIO = 0.8;
    private static final long THRESHOLD_80 = (long) (THRESHOLD_MS * THRESHOLD_RATIO); // 80

    private TimeoutDegradationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new TimeoutDegradationStrategy(Duration.ofMillis(THRESHOLD_MS));
    }

    @Test
    void shouldNotDegradeWhenNoInvocationData() {
        DegradationContext context = new DegradationContext();
        assertFalse(strategy.shouldDegrade(context));
    }

    @Test
    void shouldNotDegradeWhenInvocationCountIsZero() {
        DegradationContext context = new DegradationContext();
        context.setInvocationCount(0);
        context.setElapsedTime(THRESHOLD_80 + 10);
        assertFalse(strategy.shouldDegrade(context));
    }

    @Test
    void shouldDegradeWhenElapsedExceedsThreshold() {
        DegradationContext context = new DegradationContext();
        context.setInvocationCount(5);
        context.setElapsedTime(THRESHOLD_80 + 1);
        assertTrue(strategy.shouldDegrade(context));
    }

    @Test
    void shouldNotDegradeWhenElapsedBelowThreshold() {
        DegradationContext context = new DegradationContext();
        context.setInvocationCount(5);
        context.setElapsedTime(THRESHOLD_80 - 1);
        assertFalse(strategy.shouldDegrade(context));
    }

    @Test
    void shouldNotDegradeWhenElapsedEqualsExactThreshold() {
        DegradationContext context = new DegradationContext();
        context.setInvocationCount(5);
        context.setElapsedTime(THRESHOLD_80);
        assertFalse(strategy.shouldDegrade(context));
    }

    @Test
    void shouldUseDefaultTimeoutWhenNull() {
        TimeoutDegradationStrategy defaultStrategy = new TimeoutDegradationStrategy(null);
        DegradationContext context = new DegradationContext();
        context.setInvocationCount(1);
        context.setElapsedTime(30000L);
        assertTrue(defaultStrategy.shouldDegrade(context));
    }

    @Test
    void orderShouldBe20() {
        assertEquals(20, strategy.getOrder());
    }
}
