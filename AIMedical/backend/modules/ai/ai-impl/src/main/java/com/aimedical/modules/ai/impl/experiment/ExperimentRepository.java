package com.aimedical.modules.ai.impl.experiment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ExperimentRepository extends JpaRepository<Experiment, Long> {

    List<Experiment> findByStatus(ExperimentStatus status);

    List<Experiment> findByCapabilityIdAndStatus(String capabilityId, ExperimentStatus status);

    @Query("SELECT e FROM Experiment e LEFT JOIN FETCH e.groups WHERE e.capabilityId = :capabilityId AND e.status = :status")
    List<Experiment> findByCapabilityIdAndStatusWithGroups(@Param("capabilityId") String capabilityId,
                                                           @Param("status") ExperimentStatus status);

    List<Experiment> findByStatusAndEndTimeBetween(ExperimentStatus status,
                                                    LocalDateTime start, LocalDateTime end);

    List<Experiment> findByCapabilityIdOrderByEndTimeDesc(String capabilityId);
}
