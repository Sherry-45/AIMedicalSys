package com.aimedical.modules.ai.impl.degradation;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.aimedical.modules.ai.api.degradation.DegradationContext;
import com.aimedical.modules.ai.api.degradation.DegradationStrategy;
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;

public class CircuitBreakerDegradationStrategy implements DegradationStrategy {

    public enum CircuitBreakerState {
        CLOSED, OPEN, HALF_OPEN
    }

    private static class CircuitData {
        volatile long circuitOpenedAt;
        volatile long lastFailureTime;
        volatile int failureCount;
        final AtomicBoolean probeLock = new AtomicBoolean(false);
        volatile long probeAcquiredAt;
    }

    private final SlidingWindowMetricsStore metricsStore;
    private final double failureRateThreshold;
    private final long openWindowMs;
    private final ConcurrentHashMap<String, AtomicReference<CircuitBreakerState>> stateMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CircuitData> circuitDataMap = new ConcurrentHashMap<>();

    public CircuitBreakerDegradationStrategy(
            SlidingWindowMetricsStore metricsStore,
            double failureRateThreshold,
            Duration openWindow) {
        if (metricsStore == null) {
            throw new IllegalArgumentException("metricsStore must not be null");
        }
        this.metricsStore = metricsStore;
        this.failureRateThreshold = Double.isNaN(failureRateThreshold) ? 0.5 : failureRateThreshold;
        this.openWindowMs = openWindow != null ? openWindow.toMillis() : Duration.ofSeconds(30).toMillis();
    }

    @Override
    public boolean shouldDegrade(DegradationContext context) {
        String serviceName = context.getServiceName();
        String key = context.getOperationName() != null ? context.getOperationName() : serviceName;
        if (key == null) {
            return false;
        }

        AtomicReference<CircuitBreakerState> stateRef = stateMap.computeIfAbsent(
                key, k -> new AtomicReference<>(CircuitBreakerState.CLOSED));
        CircuitBreakerState state = stateRef.get();

        switch (state) {
            case CLOSED: {
                CircuitData data = circuitDataMap.computeIfAbsent(key, k -> new CircuitData());
                data.failureCount++;
                double failureRate = metricsStore.getFailureRate(serviceName);
                if (failureRate >= failureRateThreshold) {
                    long now = System.currentTimeMillis();
                    data.circuitOpenedAt = now;
                    data.lastFailureTime = now;
                    data.failureCount = 1;
                    stateRef.set(CircuitBreakerState.OPEN);
                    return true;
                }
                return false;
            }
            case OPEN: {
                CircuitData data = circuitDataMap.get(key);
                if (data != null && System.currentTimeMillis() - data.circuitOpenedAt >= openWindowMs) {
                    stateRef.set(CircuitBreakerState.HALF_OPEN);
                } else {
                    return true;
                }
            }
            // fall through to HALF_OPEN
            case HALF_OPEN: {
                CircuitData data = circuitDataMap.get(key);
                if (data != null && data.probeLock.compareAndSet(false, true)) {
                    data.probeAcquiredAt = System.currentTimeMillis();
                    return false;
                } else if (data != null && data.probeAcquiredAt != 0
                    && System.currentTimeMillis() - data.probeAcquiredAt > openWindowMs) {
                    data.probeLock.set(false);
                    data.probeAcquiredAt = 0;
                    if (data.probeLock.compareAndSet(false, true)) {
                        data.probeAcquiredAt = System.currentTimeMillis();
                        return false;
                    }
                    return true;
                } else {
                    return true;
                }
            }
            default:
                return false;
        }
    }

    public CircuitBreakerState getState(String key) {
        AtomicReference<CircuitBreakerState> ref = stateMap.get(key);
        return ref != null ? ref.get() : CircuitBreakerState.CLOSED;
    }

    public void recordProbeResult(String key, boolean success) {
        AtomicReference<CircuitBreakerState> stateRef = stateMap.get(key);
        if (stateRef == null) {
            return;
        }

        CircuitData data = circuitDataMap.get(key);
        if (success) {
            stateRef.set(CircuitBreakerState.CLOSED);
        } else {
            if (data != null) {
                data.circuitOpenedAt = System.currentTimeMillis();
                data.lastFailureTime = System.currentTimeMillis();
                data.failureCount++;
            }
            stateRef.set(CircuitBreakerState.OPEN);
        }

        if (data != null) {
            data.probeLock.set(false);
        }
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
