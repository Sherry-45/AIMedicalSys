package com.aimedical.modules.ai.impl.client.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiAbilityInputInvalidExceptionTest {

    @Test
    void shouldConstructWithMessage() {
        AiAbilityInputInvalidException ex = new AiAbilityInputInvalidException("clientType must not be null");
        assertEquals("clientType must not be null", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void shouldConstructWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        AiAbilityInputInvalidException ex = new AiAbilityInputInvalidException("invalid request", cause);
        assertEquals("invalid request", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void shouldBeRuntimeException() {
        AiAbilityInputInvalidException ex = new AiAbilityInputInvalidException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
