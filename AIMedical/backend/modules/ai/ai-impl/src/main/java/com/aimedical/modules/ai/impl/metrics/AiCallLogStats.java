package com.aimedical.modules.ai.impl.metrics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_call_log_stats", indexes = {
    @Index(name = "idx_stats_capability_month",
           columnList = "capabilityId, statMonth DESC")
})
public class AiCallLogStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String capabilityId;

    @Column(length = 7)
    private String statMonth;

    @Column
    private long totalCalls;

    @Column
    private long successCount;

    @Column
    private long degradedCount;

    @Column
    private long failureCount;

    @Column
    private double avgElapsedMs;

    @Column
    private double p50ElapsedMs;

    @Column
    private double p95ElapsedMs;

    @Column
    private double p99ElapsedMs;

    public AiCallLogStats() {
    }

    public AiCallLogStats(Long id, String capabilityId, String statMonth,
                          long totalCalls, long successCount, long degradedCount, long failureCount,
                          double avgElapsedMs, double p50ElapsedMs, double p95ElapsedMs, double p99ElapsedMs) {
        this.id = id;
        this.capabilityId = capabilityId;
        this.statMonth = statMonth;
        this.totalCalls = totalCalls;
        this.successCount = successCount;
        this.degradedCount = degradedCount;
        this.failureCount = failureCount;
        this.avgElapsedMs = avgElapsedMs;
        this.p50ElapsedMs = p50ElapsedMs;
        this.p95ElapsedMs = p95ElapsedMs;
        this.p99ElapsedMs = p99ElapsedMs;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCapabilityId() { return capabilityId; }
    public void setCapabilityId(String capabilityId) { this.capabilityId = capabilityId; }
    public String getStatMonth() { return statMonth; }
    public void setStatMonth(String statMonth) { this.statMonth = statMonth; }
    public long getTotalCalls() { return totalCalls; }
    public void setTotalCalls(long totalCalls) { this.totalCalls = totalCalls; }
    public long getSuccessCount() { return successCount; }
    public void setSuccessCount(long successCount) { this.successCount = successCount; }
    public long getDegradedCount() { return degradedCount; }
    public void setDegradedCount(long degradedCount) { this.degradedCount = degradedCount; }
    public long getFailureCount() { return failureCount; }
    public void setFailureCount(long failureCount) { this.failureCount = failureCount; }
    public double getAvgElapsedMs() { return avgElapsedMs; }
    public void setAvgElapsedMs(double avgElapsedMs) { this.avgElapsedMs = avgElapsedMs; }
    public double getP50ElapsedMs() { return p50ElapsedMs; }
    public void setP50ElapsedMs(double p50ElapsedMs) { this.p50ElapsedMs = p50ElapsedMs; }
    public double getP95ElapsedMs() { return p95ElapsedMs; }
    public void setP95ElapsedMs(double p95ElapsedMs) { this.p95ElapsedMs = p95ElapsedMs; }
    public double getP99ElapsedMs() { return p99ElapsedMs; }
    public void setP99ElapsedMs(double p99ElapsedMs) { this.p99ElapsedMs = p99ElapsedMs; }
}
