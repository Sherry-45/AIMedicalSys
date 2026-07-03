package com.aimedical.modules.ai.impl.metrics;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AiCallLogRepository extends JpaRepository<AiCallLogEntity, Long> {
    long countByCallTimeBefore(LocalDateTime cutoff);
}
