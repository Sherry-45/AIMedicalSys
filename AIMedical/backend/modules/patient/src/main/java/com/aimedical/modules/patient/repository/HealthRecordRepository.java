package com.aimedical.modules.patient.repository;

import com.aimedical.modules.patient.entity.HealthRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecordEntity, Long> {

    List<HealthRecordEntity> findByPatientIdOrderByRecordDateDesc(Long patientId);

    List<HealthRecordEntity> findByPatientIdAndRecordTypeOrderByRecordDateDesc(Long patientId, String recordType);

    List<HealthRecordEntity> findByPatientIdAndRecordCategoryOrderByRecordDateDesc(Long patientId, String recordCategory);

    List<HealthRecordEntity> findByPatientIdAndRecordDateBetweenOrderByRecordDateDesc(Long patientId, LocalDate start, LocalDate end);

    List<HealthRecordEntity> findByPatientIdAndOrganizationOrderByRecordDateDesc(Long patientId, String organization);

    List<HealthRecordEntity> findByPatientIdAndRecordTypeAndRecordDateBetweenOrderByRecordDateDesc(Long patientId, String recordType, LocalDate start, LocalDate end);

    List<HealthRecordEntity> findByPatientIdAndOrganizationAndRecordDateBetweenOrderByRecordDateDesc(Long patientId, String organization, LocalDate start, LocalDate end);

    List<HealthRecordEntity> findBySourceIdAndSourceTable(Long sourceId, String sourceTable);

    long countByPatientIdAndRecordType(Long patientId, String recordType);
}
