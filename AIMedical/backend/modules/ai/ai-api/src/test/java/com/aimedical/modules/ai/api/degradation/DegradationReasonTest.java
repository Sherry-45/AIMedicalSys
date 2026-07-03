package com.aimedical.modules.ai.api.degradation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DegradationReasonTest {

    @Test
    void shouldHaveEightConstants() {
        assertEquals(8, DegradationReason.values().length);
    }

    @Test
    void shouldReturnCodeForNoAvailableRoute() {
        assertEquals("NoAvailableRoute", DegradationReason.NO_AVAILABLE_ROUTE.getCode());
    }

    @Test
    void shouldReturnMessageForNoAvailableRoute() {
        assertEquals("No available model route", DegradationReason.NO_AVAILABLE_ROUTE.getMessage());
    }

    @Test
    void shouldReturnCodeForEndpointUnavailable() {
        assertEquals("EndpointUnavailable", DegradationReason.ENDPOINT_UNAVAILABLE.getCode());
    }

    @Test
    void shouldReturnMessageForEndpointUnavailable() {
        assertEquals("Endpoint is unavailable", DegradationReason.ENDPOINT_UNAVAILABLE.getMessage());
    }

    @Test
    void shouldReturnCodeForCircuitBreakerOpen() {
        assertEquals("CircuitBreakerOpen", DegradationReason.CIRCUIT_BREAKER_OPEN.getCode());
    }

    @Test
    void shouldReturnMessageForCircuitBreakerOpen() {
        assertEquals("Circuit breaker is open", DegradationReason.CIRCUIT_BREAKER_OPEN.getMessage());
    }

    @Test
    void shouldReturnCodeForParseFailure() {
        assertEquals("ParseFailure", DegradationReason.PARSE_FAILURE.getCode());
    }

    @Test
    void shouldReturnMessageForParseFailure() {
        assertEquals("LLM output parse failure", DegradationReason.PARSE_FAILURE.getMessage());
    }

    @Test
    void shouldReturnCodeForTimeout() {
        assertEquals("Timeout", DegradationReason.TIMEOUT.getCode());
    }

    @Test
    void shouldReturnMessageForTimeout() {
        assertEquals("Request timeout", DegradationReason.TIMEOUT.getMessage());
    }

    @Test
    void shouldReturnCodeForStrategyTriggered() {
        assertEquals("StrategyTriggered", DegradationReason.STRATEGY_TRIGGERED.getCode());
    }

    @Test
    void shouldReturnMessageForStrategyTriggered() {
        assertEquals("Degradation strategy triggered", DegradationReason.STRATEGY_TRIGGERED.getMessage());
    }

    @Test
    void shouldReturnCodeForInternalError() {
        assertEquals("InternalError", DegradationReason.INTERNAL_ERROR.getCode());
    }

    @Test
    void shouldReturnMessageForInternalError() {
        assertEquals("Internal error", DegradationReason.INTERNAL_ERROR.getMessage());
    }

    @Test
    void shouldReturnCodeForInfrastructureError() {
        assertEquals("InfrastructureError", DegradationReason.INFRASTRUCTURE_ERROR.getCode());
    }

    @Test
    void shouldReturnMessageForInfrastructureError() {
        assertEquals("Infrastructure error", DegradationReason.INFRASTRUCTURE_ERROR.getMessage());
    }

    @Test
    void fromCodeShouldReturnMatchingEnum() {
        assertEquals(DegradationReason.TIMEOUT, DegradationReason.fromCode("Timeout"));
    }

    @Test
    void fromCodeShouldReturnNullForUnknownCode() {
        assertNull(DegradationReason.fromCode("UnknownCode"));
    }

    @Test
    void fromCodeShouldReturnNullForNullInput() {
        assertNull(DegradationReason.fromCode(null));
    }

    @Test
    void toStringShouldReturnCode() {
        assertEquals("NoAvailableRoute", DegradationReason.NO_AVAILABLE_ROUTE.toString());
    }
}
