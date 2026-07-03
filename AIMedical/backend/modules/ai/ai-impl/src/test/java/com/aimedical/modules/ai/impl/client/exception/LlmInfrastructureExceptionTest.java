package com.aimedical.modules.ai.impl.client.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LlmInfrastructureExceptionTest {

    @Test
    void shouldConstructWithMessage() {
        LlmInfrastructureException ex = new LlmInfrastructureException("Connection timeout");
        assertEquals("Connection timeout", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void shouldConstructWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        LlmInfrastructureException ex = new LlmInfrastructureException("HTTP 502", cause);
        assertEquals("HTTP 502", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void shouldBeRuntimeException() {
        LlmInfrastructureException ex = new LlmInfrastructureException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
