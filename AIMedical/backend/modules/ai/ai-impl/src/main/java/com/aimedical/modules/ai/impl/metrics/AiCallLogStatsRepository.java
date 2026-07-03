package com.aimedical.modules.ai.impl.metrics;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiCallLogStatsRepository extends JpaRepository<AiCallLogStats, Long> {
    List<AiCallLogStats> findByCapabilityIdAndStatMonthBetween(String capabilityId, String startMonth, String endMonth);
}
