package com.aimedical.modules.ai.api.dto.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Phase4BusinessExceptionTest {

    @Test
    void shouldConstructWithMessage() {
        Phase4BusinessException ex = new Phase4BusinessException("test msg") {};
        assertEquals("test msg", ex.getMessage());
    }

    @Test
    void shouldConstructWithMessageAndCause() {
        Throwable cause = new RuntimeException("root");
        Phase4BusinessException ex = new Phase4BusinessException("test msg", cause) {};
        assertEquals("test msg", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void shouldBeInstanceOfRuntimeException() {
        Phase4BusinessException ex = new Phase4BusinessException("test") {};
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    void shouldConstructWithMessageAndErrorCode() {
        Phase4BusinessException ex = new Phase4BusinessException("test msg", "ERR_CODE") {};
        assertEquals("test msg", ex.getMessage());
        assertEquals("ERR_CODE", ex.getErrorCode());
    }

    @Test
    void shouldReturnNullErrorCodeWhenConstructedWithoutErrorCode() {
        Phase4BusinessException ex = new Phase4BusinessException("msg") {};
        assertNull(ex.getErrorCode());
    }
}
