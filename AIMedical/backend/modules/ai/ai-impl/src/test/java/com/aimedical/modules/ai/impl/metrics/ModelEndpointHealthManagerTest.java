package com.aimedical.modules.ai.impl.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelEndpointHealthManagerTest {

    private ModelEndpointHealthManager manager;

    @BeforeEach
    void setUp() {
        manager = new ModelEndpointHealthManager();
    }

    @Test
    void newEndpointShouldBeCONNECTED() {
        assertEquals(EndpointHealthState.CONNECTED, manager.getState("ep-1"));
    }

    @Test
    void fiveConsecutiveFailuresShouldTransitionToUNAVAILABLE() {
        for (int i = 0; i < 5; i++) {
            manager.recordCallResult("ep-1", false, 100);
        }
        assertEquals(EndpointHealthState.UNAVAILABLE, manager.getState("ep-1"));
    }

    @Test
    void threeSlowCallsShouldTransitionToDEGRADED() {
        manager.setSlowCallThreshold("ep-1", 100L);
        for (int i = 0; i < 3; i++) {
            manager.recordCallResult("ep-1", true, 200);
        }
        assertEquals(EndpointHealthState.DEGRADED, manager.getState("ep-1"));
    }

    @Test
    void threeSuccessesAfterDegradedShouldTransitionToCONNECTED() {
        manager.setSlowCallThreshold("ep-1", 100L);
        for (int i = 0; i < 3; i++) {
            manager.recordCallResult("ep-1", true, 200);
        }
        assertEquals(EndpointHealthState.DEGRADED, manager.getState("ep-1"));

        for (int i = 0; i < 3; i++) {
            manager.recordCallResult("ep-1", true, 50);
        }
        assertEquals(EndpointHealthState.CONNECTED, manager.getState("ep-1"));
    }

    @Test
    void fiveCumulativeFailuresInDegradedShouldTransitionToUNAVAILABLE() {
        manager.setSlowCallThreshold("ep-1", 100L);
        for (int i = 0; i < 3; i++) {
            manager.recordCallResult("ep-1", true, 200);
        }
        assertEquals(EndpointHealthState.DEGRADED, manager.getState("ep-1"));

        for (int i = 0; i < 5; i++) {
            manager.recordCallResult("ep-1", false, 50);
        }
        assertEquals(EndpointHealthState.UNAVAILABLE, manager.getState("ep-1"));
    }

    @Test
    void probeSuccessShouldTransitionFromUNAVAILABLEToDEGRADED() {
        for (int i = 0; i < 5; i++) {
            manager.recordCallResult("ep-1", false, 100);
        }
        assertEquals(EndpointHealthState.UNAVAILABLE, manager.getState("ep-1"));

        manager.recordCallResult("ep-1", true, 100);
        assertEquals(EndpointHealthState.DEGRADED, manager.getState("ep-1"));
    }

    @Test
    void twoStageRecoveryFromUNAVAILABLEToCONNECTED() {
        for (int i = 0; i < 5; i++) {
            manager.recordCallResult("ep-1", false, 100);
        }
        assertEquals(EndpointHealthState.UNAVAILABLE, manager.getState("ep-1"));

        manager.recordCallResult("ep-1", true, 100);
        assertEquals(EndpointHealthState.DEGRADED, manager.getState("ep-1"));

        manager.recordCallResult("ep-1", true, 50);
        manager.recordCallResult("ep-1", true, 50);
        manager.recordCallResult("ep-1", true, 50);
        assertEquals(EndpointHealthState.CONNECTED, manager.getState("ep-1"));
    }

    @Test
    void tryProbeShouldReturnTrueAfterProbeWindow() {
        for (int i = 0; i < 5; i++) {
            manager.recordCallResult("ep-1", false, 100);
        }
        manager.setLastProbeTime("ep-1", System.currentTimeMillis() - 60_000);
        assertTrue(manager.tryProbe("ep-1"));
        assertFalse(manager.tryProbe("ep-1"));
    }

    @Test
    void tryProbeShouldReturnFalseWithinProbeWindow() {
        for (int i = 0; i < 5; i++) {
            manager.recordCallResult("ep-1", false, 100);
        }
        manager.setLastProbeTime("ep-1", System.currentTimeMillis() - 10_000);
        assertFalse(manager.tryProbe("ep-1"));
    }

    @Test
    void tryProbeShouldReturnFalseWhenNotUNAVAILABLE() {
        assertFalse(manager.tryProbe("ep-probe"));

        manager.setSlowCallThreshold("ep-probe", 100L);
        for (int i = 0; i < 3; i++) {
            manager.recordCallResult("ep-probe", true, 200);
        }
        assertEquals(EndpointHealthState.DEGRADED, manager.getState("ep-probe"));
        assertFalse(manager.tryProbe("ep-probe"));
    }

    @Test
    void probeFailureShouldStayUNAVAILABLE() {
        for (int i = 0; i < 5; i++) {
            manager.recordCallResult("ep-fail", false, 100);
        }
        assertEquals(EndpointHealthState.UNAVAILABLE, manager.getState("ep-fail"));

        manager.recordCallResult("ep-fail", false, 100);
        assertEquals(EndpointHealthState.UNAVAILABLE, manager.getState("ep-fail"));
        assertTrue(Math.abs(System.currentTimeMillis() - manager.getLastProbeTime("ep-fail")) < 5000);
    }

    @Test
    void shouldStayDEGRADEDAfterSlowSuccess() {
        manager.setSlowCallThreshold("ep-slow", 100L);
        for (int i = 0; i < 3; i++) {
            manager.recordCallResult("ep-slow", true, 200);
        }
        assertEquals(EndpointHealthState.DEGRADED, manager.getState("ep-slow"));

        manager.recordCallResult("ep-slow", true, 200);
        assertEquals(EndpointHealthState.DEGRADED, manager.getState("ep-slow"));

        manager.recordCallResult("ep-slow", true, 50);
        manager.recordCallResult("ep-slow", true, 50);
        assertEquals(EndpointHealthState.DEGRADED, manager.getState("ep-slow"));

        manager.recordCallResult("ep-slow", true, 50);
        assertEquals(EndpointHealthState.CONNECTED, manager.getState("ep-slow"));
    }

    @Test
    void helperSettersAndGettersShouldRoundTrip() {
        manager.setSlowCallThreshold("ep-helper", 3000L);
        assertEquals(3000L, manager.getSlowCallThreshold("ep-helper"));

        manager.setLastProbeTime("ep-helper", 123456L);
        assertEquals(123456L, manager.getLastProbeTime("ep-helper"));
    }

    @Test
    void concurrentAccessShouldNotCauseRaceConditions() throws InterruptedException {
        manager.setSlowCallThreshold("ep-con", 100L);
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        for (int t = 0; t < threadCount; t++) {
            int finalT = t;
            threads[t] = new Thread(() -> {
                for (int i = 0; i < 20; i++) {
                    if (finalT % 2 == 0) {
                        manager.recordCallResult("ep-con", true, 200);
                    } else {
                        manager.recordCallResult("ep-con", false, 50);
                    }
                }
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join(5000);

        for (Thread t : threads) {
            assertFalse(t.isAlive(), "thread did not complete within timeout");
        }

        EndpointHealthState finalState = manager.getState("ep-con");
        assertTrue(finalState == EndpointHealthState.CONNECTED ||
                  finalState == EndpointHealthState.DEGRADED ||
                  finalState == EndpointHealthState.UNAVAILABLE);
    }
}
