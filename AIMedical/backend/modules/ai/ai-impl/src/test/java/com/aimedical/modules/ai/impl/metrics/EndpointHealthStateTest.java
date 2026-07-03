package com.aimedical.modules.ai.impl.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EndpointHealthStateTest {

    @Test
    void shouldHaveThreeConstants() {
        EndpointHealthState[] values = EndpointHealthState.values();
        assertEquals(3, values.length);
    }

    @Test
    void shouldContainCONNECTED() {
        assertNotNull(EndpointHealthState.valueOf("CONNECTED"));
    }

    @Test
    void shouldContainDEGRADED() {
        assertNotNull(EndpointHealthState.valueOf("DEGRADED"));
    }

    @Test
    void shouldContainUNAVAILABLE() {
        assertNotNull(EndpointHealthState.valueOf("UNAVAILABLE"));
    }

    @Test
    void ordinalShouldBeCONNECTED0() {
        assertEquals(0, EndpointHealthState.CONNECTED.ordinal());
    }

    @Test
    void ordinalShouldBeDEGRADED1() {
        assertEquals(1, EndpointHealthState.DEGRADED.ordinal());
    }

    @Test
    void ordinalShouldBeUNAVAILABLE2() {
        assertEquals(2, EndpointHealthState.UNAVAILABLE.ordinal());
    }

    @Test
    void shouldThrowForNullName() {
        assertThrows(NullPointerException.class, () -> EndpointHealthState.valueOf(null));
    }

    @Test
    void shouldThrowForInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> EndpointHealthState.valueOf("UNKNOWN"));
    }
}
