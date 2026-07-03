package com.aimedical.modules.ai.impl.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuthTypeTest {

    @Test
    void shouldContainApiKey() {
        assertNotNull(AuthType.valueOf("API_KEY"));
    }

    @Test
    void shouldContainOauth2() {
        assertNotNull(AuthType.valueOf("OAUTH2"));
    }

    @Test
    void shouldContainNone() {
        assertNotNull(AuthType.valueOf("NONE"));
    }

    @Test
    void shouldHaveExactlyThreeConstants() {
        assertEquals(3, AuthType.values().length);
    }

    @Test
    void shouldThrowOnInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> AuthType.valueOf("INVALID"));
    }
}
