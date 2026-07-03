package com.aimedical.modules.ai.impl.client;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CredentialProviderTest {

    @Test
    void shouldConstructCredentialWithAllFields() {
        Instant expiresAt = Instant.now().plusSeconds(3600);
        CredentialProvider.Credential credential = new CredentialProvider.Credential(
                AuthType.API_KEY, "sk-test", expiresAt, "/v1/secret/ai/ep1");
        assertEquals(AuthType.API_KEY, credential.getAuthType());
        assertEquals("sk-test", credential.getCredentialValue());
        assertEquals(expiresAt, credential.getExpiresAt());
        assertEquals("/v1/secret/ai/ep1", credential.getVaultPath());
    }

    @Test
    void shouldAllowNullExpiresAtAndVaultPath() {
        CredentialProvider.Credential credential = new CredentialProvider.Credential(
                AuthType.NONE, null, null, null);
        assertEquals(AuthType.NONE, credential.getAuthType());
        assertNull(credential.getCredentialValue());
        assertNull(credential.getExpiresAt());
        assertNull(credential.getVaultPath());
    }

    @Test
    void shouldHaveThreeStates() {
        assertEquals(3, CredentialProviderState.values().length);
        assertNotNull(CredentialProviderState.valueOf("NORMAL"));
        assertNotNull(CredentialProviderState.valueOf("CACHE_ONLY"));
        assertNotNull(CredentialProviderState.valueOf("BACKOFF"));
    }
}
