package com.aimedical.modules.ai.impl.client;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")
@Component
public class EndpointRateLimiter {

    private final ConcurrentHashMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    private final Environment environment;

    @Value("${ai.rate-limiting.endpoints.default.permits-per-second:10}")
    private double defaultPermitsPerSecond;

    @Value("${ai.rate-limiting.endpoints.default.queue-wait-millis:100}")
    private long defaultQueueWaitMillis;

    @Value("${ai.rate-limiting.endpoints.default.max-burst-seconds:0}")
    private long defaultMaxBurstSeconds;

    public EndpointRateLimiter(Environment environment) {
        this.environment = environment;
    }

    public boolean tryAcquire(String endpointId) {
        long queueWaitMillis = environment.getProperty(
                "ai.rate-limiting.endpoints." + endpointId + ".queue-wait-millis",
                Long.class,
                defaultQueueWaitMillis);
        return tryAcquire(endpointId, queueWaitMillis, TimeUnit.MILLISECONDS);
    }

    public boolean tryAcquire(String endpointId, long timeout, TimeUnit unit) {
        RateLimiter limiter = limiters.computeIfAbsent(endpointId, id -> {
            double permitsPerSecond = environment.getProperty(
                    "ai.rate-limiting.endpoints." + id + ".permits-per-second",
                    Double.class,
                    defaultPermitsPerSecond);
            long maxBurstSeconds = environment.getProperty(
                    "ai.rate-limiting.endpoints." + id + ".max-burst-seconds",
                    Long.class,
                    defaultMaxBurstSeconds);
            if (maxBurstSeconds > 0) {
                return RateLimiter.create(permitsPerSecond, maxBurstSeconds, TimeUnit.SECONDS);
            }
            return RateLimiter.create(permitsPerSecond);
        });
        return limiter.tryAcquire(timeout, unit);
    }
}
