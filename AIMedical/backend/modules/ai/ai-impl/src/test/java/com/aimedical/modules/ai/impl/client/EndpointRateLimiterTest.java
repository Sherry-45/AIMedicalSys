package com.aimedical.modules.ai.impl.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EndpointRateLimiterTest {

    private Environment environment;
    private EndpointRateLimiter rateLimiter;

    @BeforeEach
    void setUp() throws Exception {
        environment = mock(Environment.class);
        when(environment.getProperty(anyString(), eq(Double.class), anyDouble()))
                .thenAnswer(invocation -> invocation.getArgument(2));
        when(environment.getProperty(anyString(), eq(Long.class), anyLong()))
                .thenAnswer(invocation -> invocation.getArgument(2));
        rateLimiter = new EndpointRateLimiter(environment);
        var permitsField = EndpointRateLimiter.class.getDeclaredField("defaultPermitsPerSecond");
        permitsField.setAccessible(true);
        permitsField.set(rateLimiter, 10.0);
        var queueField = EndpointRateLimiter.class.getDeclaredField("defaultQueueWaitMillis");
        queueField.setAccessible(true);
        queueField.set(rateLimiter, 100L);
    }

    @Test
    void shouldAcquireToken() {
        assertTrue(rateLimiter.tryAcquire("ep1", 5000, TimeUnit.MILLISECONDS));
    }

    @Test
    void shouldAcquireTokenWithCustomTimeout() {
        // Exhaust tokens, then short timeout should return false
        for (int i = 0; i < 20; i++) {
            rateLimiter.tryAcquire("timeout-ep", 0, TimeUnit.SECONDS);
        }
        assertFalse(rateLimiter.tryAcquire("timeout-ep", 1, TimeUnit.MILLISECONDS));
    }

    @Test
    void shouldAcquireTokenImmediatelyWithFreshLimiter() {
        assertTrue(rateLimiter.tryAcquire("ep1", 0, TimeUnit.SECONDS));
    }

    @Test
    void shouldUseDefaultConfigWhenNoEndpointSpecificConfig() throws Exception {
        // Set very low default rate to verify default config is used for unknown endpoints
        var permitsField = EndpointRateLimiter.class.getDeclaredField("defaultPermitsPerSecond");
        permitsField.setAccessible(true);
        permitsField.set(rateLimiter, 0.001);
        assertTrue(rateLimiter.tryAcquire("unconfigured-ep", 5000, TimeUnit.MILLISECONDS));
        assertFalse(rateLimiter.tryAcquire("unconfigured-ep", 0, TimeUnit.SECONDS));
    }

    @Test
    void shouldCreateSeparateLimitersForDifferentEndpoints() {
        assertTrue(rateLimiter.tryAcquire("ep-a", 5000, TimeUnit.MILLISECONDS));
        assertTrue(rateLimiter.tryAcquire("ep-b", 5000, TimeUnit.MILLISECONDS));
    }

    @Test
    void shouldAcquireWithDefaultWaitTime() {
        assertTrue(rateLimiter.tryAcquire("ep-default"));
    }

    @Test
    void shouldRejectWhenOverRate() {
        for (int i = 0; i < 20; i++) {
            rateLimiter.tryAcquire("burst-ep", 0, TimeUnit.SECONDS);
        }
        assertFalse(rateLimiter.tryAcquire("burst-ep", 0, TimeUnit.SECONDS));
    }
}
