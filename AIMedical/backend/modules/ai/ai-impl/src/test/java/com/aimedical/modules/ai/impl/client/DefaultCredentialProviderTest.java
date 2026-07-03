package com.aimedical.modules.ai.impl.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCredentialProviderTest {

    private DefaultCredentialProvider provider;
    private CredentialProvider.Credential apiKeyCredential;
    private CredentialProvider.Credential oauth2Credential;

    @BeforeEach
    void setUp() {
        provider = new DefaultCredentialProvider();
        apiKeyCredential = new CredentialProvider.Credential(
                AuthType.API_KEY, "sk-valid", null, "/v1/secret/ai/ep1");
        oauth2Credential = new CredentialProvider.Credential(
                AuthType.OAUTH2, "token-123", Instant.now().plusSeconds(3600), "/v1/secret/ai/ep2");
        provider.registerCredential("ep1", apiKeyCredential);
        provider.registerCredential("ep2", oauth2Credential);
    }

    @Test
    void shouldStartInNormalState() {
        assertEquals(CredentialProviderState.NORMAL, provider.getState());
    }

    @Test
    void shouldReturnCredentialWhenExists() {
        Optional<CredentialProvider.Credential> result = provider.getCredential("ep1");
        assertTrue(result.isPresent());
        assertEquals("sk-valid", result.get().getCredentialValue());
    }

    @Test
    void shouldReturnEmptyForUnknownEndpoint() {
        Optional<CredentialProvider.Credential> result = provider.getCredential("unknown");
        assertFalse(result.isPresent());
    }

    @Test
    void shouldResetFailuresOnSuccessfulLookup() {
        provider.getCredential("unknown");
        provider.getCredential("unknown");
        provider.getCredential("ep1");
        Optional<CredentialProvider.Credential> result = provider.getCredential("unknown");
        assertFalse(result.isPresent());
    }

    @Test
    void shouldTransitionToCacheOnlyAfterSimulatedTimeout() {
        provider.simulateVaultTimeout();
        assertEquals(CredentialProviderState.CACHE_ONLY, provider.getState());
    }

    @Test
    void shouldReturnCachedCredentialInCacheOnlyState() {
        provider.simulateVaultTimeout();
        Optional<CredentialProvider.Credential> result = provider.getCredential("ep1");
        assertTrue(result.isPresent());
        assertEquals("sk-valid", result.get().getCredentialValue());
    }

    @Test
    void shouldReturnEmptyInCacheOnlyForUncachedEndpoint() {
        provider.simulateVaultTimeout();
        Optional<CredentialProvider.Credential> result = provider.getCredential("unknown");
        assertFalse(result.isPresent());
    }

    @Test
    void shouldTransitionToBackoffAfterFiveSimulatedTimeouts() {
        provider.simulateVaultTimeout();
        provider.simulateVaultTimeout();
        provider.simulateVaultTimeout();
        provider.simulateVaultTimeout();
        provider.simulateVaultTimeout();
        assertEquals(CredentialProviderState.BACKOFF, provider.getState());
    }

    @Test
    void shouldReturnEmptyDuringBackoff() {
        provider.simulateVaultTimeout();
        provider.simulateVaultTimeout();
        provider.simulateVaultTimeout();
        provider.simulateVaultTimeout();
        provider.simulateVaultTimeout();
        Optional<CredentialProvider.Credential> result = provider.getCredential("ep1");
        assertFalse(result.isPresent());
    }

    @Test
    void shouldTransitionToBackoffAfterFiveEmptyResponses() {
        for (int i = 0; i < 4; i++) {
            provider.getCredential("unknown");
        }
        assertEquals(CredentialProviderState.NORMAL, provider.getState());
        provider.getCredential("unknown");
        assertEquals(CredentialProviderState.BACKOFF, provider.getState());
    }

    @Test
    void shouldReturnFalseForExpiredCredential() {
        CredentialProvider.Credential expired = new CredentialProvider.Credential(
                AuthType.OAUTH2, "token-expired", Instant.now().minusSeconds(1), null);
        provider.registerCredential("expired-ep", expired);
        assertFalse(provider.isCredentialValid("expired-ep"));
    }

    @Test
    void shouldRecoverFromBackoffViaProbe() {
        provider.simulateVaultTimeout();
        provider.simulateVaultTimeout();
        provider.simulateVaultTimeout();
        provider.simulateVaultTimeout();
        provider.simulateVaultTimeout();
        assertEquals(CredentialProviderState.BACKOFF, provider.getState());
        provider.probeVault();
        assertEquals(CredentialProviderState.NORMAL, provider.getState());
        Optional<CredentialProvider.Credential> result = provider.getCredential("ep1");
        assertTrue(result.isPresent());
    }

    @Test
    void shouldRecoverViaSimulateVaultRecovery() {
        provider.simulateVaultTimeout();
        provider.simulateVaultRecovery();
        assertEquals(CredentialProviderState.NORMAL, provider.getState());
    }

    @Test
    void shouldReturnCredentialAfterRecovery() {
        provider.simulateVaultTimeout();
        provider.simulateVaultRecovery();
        Optional<CredentialProvider.Credential> result = provider.getCredential("ep1");
        assertTrue(result.isPresent());
    }

    @Test
    void shouldProbeVaultAndResetFromCacheOnly() {
        provider.simulateVaultTimeout();
        provider.probeVault();
        assertEquals(CredentialProviderState.NORMAL, provider.getState());
        Optional<CredentialProvider.Credential> result = provider.getCredential("ep1");
        assertTrue(result.isPresent());
    }

    @Test
    void shouldProbeVaultAndResetFromBackoff() {
        provider.simulateVaultTimeout();
        provider.simulateVaultTimeout();
        provider.simulateVaultTimeout();
        provider.simulateVaultTimeout();
        provider.simulateVaultTimeout();
        provider.probeVault();
        assertEquals(CredentialProviderState.NORMAL, provider.getState());
    }

    @Test
    void shouldReturnTrueWhenProbingNormalState() {
        assertTrue(provider.probeVault());
        assertEquals(CredentialProviderState.NORMAL, provider.getState());
    }

    @Test
    void shouldIdentifyValidCredential() {
        assertTrue(provider.isCredentialValid("ep1"));
    }

    @Test
    void shouldIdentifyInvalidCredentialForUnknownEndpoint() {
        assertFalse(provider.isCredentialValid("unknown"));
    }

    @Test
    void shouldUseCustomTtlForOauth2Credentials() {
        CredentialProvider.Credential expiredOAuth = new CredentialProvider.Credential(
                AuthType.OAUTH2, "expired-token", Instant.now(), null);
        provider.registerCredential("expired-oauth", expiredOAuth);
        provider.simulateVaultTimeout();
        assertFalse(provider.getCredential("expired-oauth").isPresent());
        assertTrue(provider.getCredential("ep1").isPresent());
    }

    @Test
    void shouldExtendTtlOnReadInCacheOnly() throws InterruptedException {
        // OAUTH2 credential with effective original TTL ≈ 100ms
        // (expiresAt = now + 60s + 100ms → TTL = 100ms after 60s safety window)
        Instant soon = Instant.now().plus(Duration.ofSeconds(60).plusMillis(100));
        CredentialProvider.Credential shortTtlOAuth = new CredentialProvider.Credential(
                AuthType.OAUTH2, "short-token", soon, null);
        provider.registerCredential("short-oauth", shortTtlOAuth);

        provider.simulateVaultTimeout();

        // Read in CACHE_ONLY triggers expireAfterRead → TTL extended by 30s (cap 600s)
        assertTrue(provider.getCredential("short-oauth").isPresent());

        // Wait past original 100ms TTL but well within the 30s extension window
        Thread.sleep(300);

        // Entry should survive due to expireAfterRead extension
        assertTrue(provider.getCredential("short-oauth").isPresent());
    }

    @Test
    void shouldAutoRecoverFromBackoffAfterTimerExpiry() throws Exception {
        for (int i = 0; i < 5; i++) {
            provider.simulateVaultTimeout();
        }
        assertEquals(CredentialProviderState.BACKOFF, provider.getState());
        Field backoffField = DefaultCredentialProvider.class.getDeclaredField("backoffUntil");
        backoffField.setAccessible(true);
        backoffField.setLong(provider, System.nanoTime() - 1);
        Optional<CredentialProvider.Credential> result = provider.getCredential("ep1");
        assertTrue(result.isPresent());
        assertEquals(CredentialProviderState.NORMAL, provider.getState());
    }
}
