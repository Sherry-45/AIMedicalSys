package com.aimedical.modules.ai.impl.client.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CredentialUnavailableExceptionTest {

    @Test
    void shouldConstructWithMessage() {
        CredentialUnavailableException ex = new CredentialUnavailableException("Credential unavailable for endpoint ep1");
        assertEquals("Credential unavailable for endpoint ep1", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void shouldConstructWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("Vault timeout");
        CredentialUnavailableException ex = new CredentialUnavailableException("Vault timeout", cause);
        assertEquals("Vault timeout", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void shouldBeRuntimeException() {
        CredentialUnavailableException ex = new CredentialUnavailableException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
