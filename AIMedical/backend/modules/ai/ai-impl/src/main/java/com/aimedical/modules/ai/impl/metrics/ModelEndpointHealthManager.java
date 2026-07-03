package com.aimedical.modules.ai.impl.metrics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")
@Service
public class ModelEndpointHealthManager {

    private static class EndpointState {
        final AtomicReference<EndpointHealthState> healthState = new AtomicReference<>(EndpointHealthState.CONNECTED);
        final AtomicInteger consecutiveSlowCalls = new AtomicInteger(0);
        final AtomicInteger consecutiveFailures = new AtomicInteger(0);
        final AtomicInteger cumulativeFailures = new AtomicInteger(0);
        final AtomicInteger consecutiveSuccesses = new AtomicInteger(0);
        final AtomicLong lastProbeTime = new AtomicLong(0);
        final AtomicLong slowCallThresholdMs = new AtomicLong(5000L);
    }

    private final ConcurrentHashMap<String, EndpointState> states = new ConcurrentHashMap<>();

    public ModelEndpointHealthManager() {}

    public EndpointHealthState getState(String endpointId) {
        return states.computeIfAbsent(endpointId, k -> new EndpointState()).healthState.get();
    }

    public boolean tryProbe(String endpointId) {
        EndpointState state = states.computeIfAbsent(endpointId, k -> new EndpointState());
        synchronized (state) {
            if (state.healthState.get() != EndpointHealthState.UNAVAILABLE) {
                return false;
            }
            long now = System.currentTimeMillis();
            long last = state.lastProbeTime.get();
            if (now - last >= 30_000) {
                state.lastProbeTime.set(now);
                return true;
            }
            return false;
        }
    }

    public void recordCallResult(String endpointId, boolean success, long elapsedMs) {
        EndpointState state = states.computeIfAbsent(endpointId, k -> new EndpointState());
        synchronized (state) {
            EndpointHealthState current = state.healthState.get();
            long threshold = state.slowCallThresholdMs.get();

            if (current == EndpointHealthState.CONNECTED) {
                if (!success) {
                    int failures = state.consecutiveFailures.incrementAndGet();
                    if (failures >= 5) {
                        state.healthState.set(EndpointHealthState.UNAVAILABLE);
                        state.consecutiveSlowCalls.set(0);
                        state.consecutiveFailures.set(0);
                        state.consecutiveSuccesses.set(0);
                        state.cumulativeFailures.set(0);
                    }
                } else if (elapsedMs > threshold) {
                    int slowCalls = state.consecutiveSlowCalls.incrementAndGet();
                    if (slowCalls >= 3) {
                        state.healthState.set(EndpointHealthState.DEGRADED);
                        state.consecutiveSlowCalls.set(0);
                    }
                } else {
                    state.consecutiveSuccesses.set(0);
                }
            } else if (current == EndpointHealthState.DEGRADED) {
                if (!success) {
                    int failures = state.cumulativeFailures.incrementAndGet();
                    state.consecutiveSuccesses.set(0);
                    if (failures >= 5) {
                        state.healthState.set(EndpointHealthState.UNAVAILABLE);
                        state.consecutiveSlowCalls.set(0);
                        state.consecutiveFailures.set(0);
                        state.consecutiveSuccesses.set(0);
                        state.cumulativeFailures.set(0);
                    }
                } else if (elapsedMs < threshold) {
                    int successes = state.consecutiveSuccesses.incrementAndGet();
                    if (successes >= 3) {
                        state.healthState.set(EndpointHealthState.CONNECTED);
                        state.consecutiveSlowCalls.set(0);
                        state.consecutiveFailures.set(0);
                        state.consecutiveSuccesses.set(0);
                        state.cumulativeFailures.set(0);
                    }
                } else {
                    state.consecutiveSuccesses.set(0);
                }
            } else if (current == EndpointHealthState.UNAVAILABLE) {
                if (success) {
                    state.healthState.set(EndpointHealthState.DEGRADED);
                    state.consecutiveSlowCalls.set(0);
                    state.consecutiveFailures.set(0);
                    state.consecutiveSuccesses.set(0);
                    state.cumulativeFailures.set(0);
                } else {
                    state.lastProbeTime.set(System.currentTimeMillis());
                }
            }
        }
    }

    public void setSlowCallThreshold(String endpointId, long thresholdMs) {
        states.computeIfAbsent(endpointId, k -> new EndpointState()).slowCallThresholdMs.set(thresholdMs);
    }

    public void setLastProbeTime(String endpointId, long timestampMs) {
        states.computeIfAbsent(endpointId, k -> new EndpointState()).lastProbeTime.set(timestampMs);
    }

    public long getSlowCallThreshold(String endpointId) {
        return states.computeIfAbsent(endpointId, k -> new EndpointState()).slowCallThresholdMs.get();
    }

    public long getLastProbeTime(String endpointId) {
        return states.computeIfAbsent(endpointId, k -> new EndpointState()).lastProbeTime.get();
    }
}
