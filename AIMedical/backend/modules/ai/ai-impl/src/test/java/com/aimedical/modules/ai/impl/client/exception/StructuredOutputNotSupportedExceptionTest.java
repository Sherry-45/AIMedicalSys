package com.aimedical.modules.ai.impl.client.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StructuredOutputNotSupportedExceptionTest {

    @Test
    void shouldConstructWithMessage() {
        StructuredOutputNotSupportedException ex = new StructuredOutputNotSupportedException("not supported");
        assertEquals("not supported", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void shouldConstructWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        StructuredOutputNotSupportedException ex = new StructuredOutputNotSupportedException("not supported", cause);
        assertEquals("not supported", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void shouldBeRuntimeException() {
        StructuredOutputNotSupportedException ex = new StructuredOutputNotSupportedException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
