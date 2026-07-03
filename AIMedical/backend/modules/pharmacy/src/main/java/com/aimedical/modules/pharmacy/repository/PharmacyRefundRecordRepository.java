package com.aimedical.modules.pharmacy.repository;

import com.aimedical.modules.pharmacy.entity.PharmacyRefundRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 退药记录仓储。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface PharmacyRefundRecordRepository extends JpaRepository<PharmacyRefundRecordEntity, Long> {

    /** 按退药单号查询 */
    Optional<PharmacyRefundRecordEntity> findByRefundNo(String refundNo);

    /** 按发药记录ID查询所有退药记录 */
    List<PharmacyRefundRecordEntity> findByDispensingId(Long dispensingId);

    /** 按发药记录ID + 状态查询（用于校验是否已存在待处理退药） */
    Optional<PharmacyRefundRecordEntity> findByDispensingIdAndStatus(Long dispensingId, String status);

    /** 按患者ID倒序分页查询 */
    Page<PharmacyRefundRecordEntity> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);

    /** 按状态分页查询 */
    Page<PharmacyRefundRecordEntity> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
}
