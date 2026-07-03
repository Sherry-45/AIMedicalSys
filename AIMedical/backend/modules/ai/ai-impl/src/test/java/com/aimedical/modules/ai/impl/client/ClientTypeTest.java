package com.aimedical.modules.ai.impl.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ClientTypeTest {

    @Test
    void shouldContainHttpApi() {
        assertNotNull(ClientType.valueOf("HTTP_API"));
    }

    @Test
    void shouldContainSpringAi() {
        assertNotNull(ClientType.valueOf("SPRING_AI"));
    }

    @Test
    void shouldHaveExactlyTwoConstants() {
        assertEquals(2, ClientType.values().length);
    }

    @Test
    void shouldThrowOnInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> ClientType.valueOf("INVALID"));
    }
}
