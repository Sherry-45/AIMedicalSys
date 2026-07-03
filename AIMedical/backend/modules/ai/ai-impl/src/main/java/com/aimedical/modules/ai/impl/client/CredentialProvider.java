package com.aimedical.modules.ai.impl.client;

import java.time.Instant;
import java.util.Optional;

public interface CredentialProvider {

    Optional<Credential> getCredential(String endpointId);

    CredentialProviderState getState();

    boolean isCredentialValid(String endpointId);

    boolean probeVault();

    static class Credential {
        private final AuthType authType;
        private final String credentialValue;
        private final Instant expiresAt;
        private final String vaultPath;

        public Credential(AuthType authType, String credentialValue, Instant expiresAt, String vaultPath) {
            this.authType = authType;
            this.credentialValue = credentialValue;
            this.expiresAt = expiresAt;
            this.vaultPath = vaultPath;
        }

        public AuthType getAuthType() {
            return authType;
        }

        public String getCredentialValue() {
            return credentialValue;
        }

        public Instant getExpiresAt() {
            return expiresAt;
        }

        public String getVaultPath() {
            return vaultPath;
        }
    }
}

enum CredentialProviderState {
    NORMAL,
    CACHE_ONLY,
    BACKOFF
}
