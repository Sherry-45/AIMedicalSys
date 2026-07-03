package com.aimedical.modules.ai.impl.metrics;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.aimedical.modules.ai.api.degradation.DegradationContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")
@Component
public class SlidingWindowMetricsStore {

    private final ConcurrentHashMap<String, Deque<WindowedEvent>> windows = new ConcurrentHashMap<>();
    private final AtomicLong windowSeconds = new AtomicLong(60);
    private final int maxEventsPerCapability = 10000;

    public void recordSuccess(String capabilityId, long elapsedMs) {
        record(capabilityId, EventType.NORMAL_SUCCESS, elapsedMs);
    }

    public void recordDegraded(String capabilityId, long elapsedMs) {
        record(capabilityId, EventType.DEGRADED, elapsedMs);
    }

    public void recordFailure(String capabilityId) {
        record(capabilityId, EventType.FAILURE, 0L);
    }

    private void record(String capabilityId, EventType type, long elapsedMs) {
        Deque<WindowedEvent> deque = windows.computeIfAbsent(capabilityId, k -> new LinkedList<>());
        synchronized (deque) {
            long cutoff = System.currentTimeMillis() - windowSeconds.get() * 1000;
            while (!deque.isEmpty() && deque.peekFirst().timestamp < cutoff) {
                deque.removeFirst();
            }
            while (deque.size() >= maxEventsPerCapability) {
                deque.removeFirst();
            }
            deque.addLast(new WindowedEvent(type, System.currentTimeMillis(), elapsedMs));
        }
    }

    public double getFailureRate(String capabilityId) {
        Deque<WindowedEvent> deque = windows.get(capabilityId);
        if (deque == null) return 0.0;
        WindowedEvent[] snapshot;
        synchronized (deque) {
            long cutoff = System.currentTimeMillis() - windowSeconds.get() * 1000;
            while (!deque.isEmpty() && deque.peekFirst().timestamp < cutoff) {
                deque.removeFirst();
            }
            snapshot = deque.toArray(new WindowedEvent[0]);
        }
        long successCount = 0, failureCount = 0;
        for (WindowedEvent event : snapshot) {
            if (event.type == EventType.NORMAL_SUCCESS) successCount++;
            else if (event.type == EventType.FAILURE) failureCount++;
        }
        long denominator = successCount + failureCount;
        return denominator == 0 ? 0.0 : (double) failureCount / denominator;
    }

    public double getEffectiveFailureRate(String capabilityId) {
        Deque<WindowedEvent> deque = windows.get(capabilityId);
        if (deque == null) return 0.0;
        WindowedEvent[] snapshot;
        synchronized (deque) {
            long cutoff = System.currentTimeMillis() - windowSeconds.get() * 1000;
            while (!deque.isEmpty() && deque.peekFirst().timestamp < cutoff) {
                deque.removeFirst();
            }
            snapshot = deque.toArray(new WindowedEvent[0]);
        }
        long successCount = 0, degradedCount = 0, failureCount = 0;
        for (WindowedEvent event : snapshot) {
            switch (event.type) {
                case NORMAL_SUCCESS -> successCount++;
                case DEGRADED -> degradedCount++;
                case FAILURE -> failureCount++;
            }
        }
        long denominator = successCount + failureCount + degradedCount;
        return denominator == 0 ? 0.0 : (double) failureCount / denominator;
    }

    public double getAverageElapsed(String capabilityId) {
        Deque<WindowedEvent> deque = windows.get(capabilityId);
        if (deque == null) return 0.0;
        WindowedEvent[] snapshot;
        synchronized (deque) {
            long cutoff = System.currentTimeMillis() - windowSeconds.get() * 1000;
            while (!deque.isEmpty() && deque.peekFirst().timestamp < cutoff) {
                deque.removeFirst();
            }
            snapshot = deque.toArray(new WindowedEvent[0]);
        }
        long totalElapsed = 0;
        long count = 0;
        for (WindowedEvent event : snapshot) {
            totalElapsed += event.elapsedMs;
            count++;
        }
        return count == 0 ? 0.0 : (double) totalElapsed / count;
    }

    public DegradationContext buildDegradationContext(String capabilityId, String requestType) {
        Deque<WindowedEvent> deque = windows.get(capabilityId);
        if (deque == null) {
            return new DegradationContext.Builder()
                    .serviceName(capabilityId)
                    .requestType(requestType)
                    .serializedTimestamp(System.currentTimeMillis())
                    .build();
        }
        WindowedEvent[] snapshot;
        synchronized (deque) {
            long cutoff = System.currentTimeMillis() - windowSeconds.get() * 1000;
            while (!deque.isEmpty() && deque.peekFirst().timestamp < cutoff) {
                deque.removeFirst();
            }
            snapshot = deque.toArray(new WindowedEvent[0]);
        }
        long successCount = 0, degradedCount = 0, failureCount = 0;
        long lastFailureTime = 0;
        long totalElapsedNonFailure = 0;
        long nonFailureEventCount = 0;
        for (WindowedEvent event : snapshot) {
            switch (event.type) {
                case NORMAL_SUCCESS -> {
                    successCount++;
                    totalElapsedNonFailure += event.elapsedMs;
                    nonFailureEventCount++;
                }
                case DEGRADED -> {
                    degradedCount++;
                    totalElapsedNonFailure += event.elapsedMs;
                    nonFailureEventCount++;
                }
                case FAILURE -> {
                    failureCount++;
                    if (event.timestamp > lastFailureTime) {
                        lastFailureTime = event.timestamp;
                    }
                }
            }
        }
        long invocationCount = successCount + failureCount + degradedCount;
        long elapsedTime = nonFailureEventCount == 0 ? 0L : totalElapsedNonFailure / nonFailureEventCount;
        return new DegradationContext.Builder()
                .serviceName(capabilityId)
                .invocationCount((int) invocationCount)
                .lastFailureTime(lastFailureTime)
                .elapsedTime(elapsedTime)
                .failureCount((int) failureCount)
                .requestType(requestType)
                .serializedTimestamp(System.currentTimeMillis())
                .build();
    }

    public void setWindowSeconds(long windowSeconds) {
        if (windowSeconds <= 0) {
            throw new IllegalArgumentException("windowSeconds must be > 0, but got: " + windowSeconds);
        }
        this.windowSeconds.set(windowSeconds);
    }

    static class WindowedEvent {
        final EventType type;
        final long timestamp;
        final long elapsedMs;

        WindowedEvent(EventType type, long timestamp, long elapsedMs) {
            this.type = type;
            this.timestamp = timestamp;
            this.elapsedMs = elapsedMs;
        }
    }

    enum EventType {
        NORMAL_SUCCESS,
        DEGRADED,
        FAILURE
    }
}
