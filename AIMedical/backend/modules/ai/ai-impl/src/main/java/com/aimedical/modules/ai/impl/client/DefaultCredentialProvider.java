package com.aimedical.modules.ai.impl.client;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.aimedical.modules.ai.impl.client.exception.CredentialUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")
@Component
public class DefaultCredentialProvider implements CredentialProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultCredentialProvider.class);

    private final ConcurrentHashMap<String, Credential> credentialStore;
    private final Cache<String, Credential> cache;
    private final AtomicInteger consecutiveFailures;
    private final AtomicReference<CredentialProviderState> state;
    private volatile long backoffUntil;

    public DefaultCredentialProvider() {
        this.credentialStore = new ConcurrentHashMap<>();
        this.consecutiveFailures = new AtomicInteger(0);
        this.state = new AtomicReference<>(CredentialProviderState.NORMAL);
        this.backoffUntil = 0L;

        Expiry<String, Credential> expiry = new Expiry<String, Credential>() {
            @Override
            public long expireAfterCreate(String key, Credential credential, long currentTime) {
                if (credential.getAuthType() == AuthType.OAUTH2 && credential.getExpiresAt() != null) {
                    long ttl = Duration.between(Instant.now(), credential.getExpiresAt()).toNanos() - 60_000_000_000L;
                    return Math.min(ttl, 300_000_000_000L);
                }
                return 300_000_000_000L;
            }

            @Override
            public long expireAfterUpdate(String key, Credential credential, long currentDuration, long currentTime) {
                return currentDuration;
            }

            @Override
            public long expireAfterRead(String key, Credential credential, long currentDuration, long currentTime) {
                if (state.get() == CredentialProviderState.CACHE_ONLY) {
                    long extended = currentDuration + 30_000_000_000L;
                    return Math.min(extended, 600_000_000_000L);
                }
                return currentDuration;
            }
        };

        this.cache = Caffeine.newBuilder()
                .expireAfter(expiry)
                .build();
    }

    public void registerCredential(String endpointId, Credential credential) {
        credentialStore.put(endpointId, credential);
        cache.put(endpointId, credential);
    }

    @Override
    public Optional<Credential> getCredential(String endpointId) {
        CredentialProviderState currentState = state.get();

        if (currentState == CredentialProviderState.BACKOFF) {
            if (System.nanoTime() < backoffUntil) {
                return Optional.empty();
            }
            if (state.compareAndSet(CredentialProviderState.BACKOFF, CredentialProviderState.NORMAL)) {
                consecutiveFailures.set(0);
                backoffUntil = 0L;
            }
            currentState = state.get();
        }

        if (currentState == CredentialProviderState.CACHE_ONLY) {
            Credential cached = cache.getIfPresent(endpointId);
            return Optional.ofNullable(cached);
        }

        Credential stored = credentialStore.get(endpointId);
        if (stored != null) {
            cache.put(endpointId, stored);
            // Cache hit: reset failure tracking since credential was served successfully
            consecutiveFailures.set(0);
            return Optional.of(stored);
        }

        int failures = consecutiveFailures.incrementAndGet();
        if (failures >= 5) {
            state.set(CredentialProviderState.BACKOFF);
            backoffUntil = System.nanoTime() + 30_000_000_000L;
        }
        return Optional.empty();
    }

    @Override
    public CredentialProviderState getState() {
        return state.get();
    }

    @Override
    public boolean isCredentialValid(String endpointId) {
        Credential credential = credentialStore.get(endpointId);
        if (credential == null) {
            return false;
        }
        if (credential.getExpiresAt() != null && Instant.now().isAfter(credential.getExpiresAt())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean probeVault() {
        CredentialProviderState currentState = state.get();
        if (currentState != CredentialProviderState.NORMAL) {
            state.set(CredentialProviderState.NORMAL);
            consecutiveFailures.set(0);
            backoffUntil = 0L;
        }
        return true;
    }

    public void simulateVaultTimeout() {
        int failures = consecutiveFailures.incrementAndGet();
        CredentialProviderState currentState = state.get();
        if (currentState == CredentialProviderState.NORMAL) {
            state.set(CredentialProviderState.CACHE_ONLY);
        } else if (currentState == CredentialProviderState.CACHE_ONLY && failures >= 5) {
            state.set(CredentialProviderState.BACKOFF);
            backoffUntil = System.nanoTime() + 30_000_000_000L;
        }
    }

    public void simulateVaultRecovery() {
        state.set(CredentialProviderState.NORMAL);
        consecutiveFailures.set(0);
        backoffUntil = 0L;
    }
}
