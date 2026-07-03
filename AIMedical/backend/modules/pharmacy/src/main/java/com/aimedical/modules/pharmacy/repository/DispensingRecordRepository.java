package com.aimedical.modules.pharmacy.repository;

import com.aimedical.modules.pharmacy.entity.DispensingRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 发药记录仓储。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface DispensingRecordRepository extends JpaRepository<DispensingRecordEntity, Long> {

    /** 按发药单号查询 */
    Optional<DispensingRecordEntity> findByDispensingNo(String dispensingNo);

    /** 按处方ID查询（用于防重复发药校验） */
    Optional<DispensingRecordEntity> findByPrescriptionId(Long prescriptionId);

    /** 按医嘱ID查询 */
    Optional<DispensingRecordEntity> findByMedicalOrderId(Long medicalOrderId);

    /** 按状态查询 */
    List<DispensingRecordEntity> findByStatus(String status);

    /** 按患者ID倒序分页查询 */
    Page<DispensingRecordEntity> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);

    /** 按患者ID + 状态倒序分页查询 */
    Page<DispensingRecordEntity> findByPatientIdAndStatusOrderByCreatedAtDesc(Long patientId, String status, Pageable pageable);

    /** 按状态分页查询 */
    Page<DispensingRecordEntity> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
}
