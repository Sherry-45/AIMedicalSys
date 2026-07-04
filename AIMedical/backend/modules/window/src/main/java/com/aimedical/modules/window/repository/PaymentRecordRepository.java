package com.aimedical.modules.window.repository;

import com.aimedical.modules.window.entity.PaymentRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * 缴费记录仓储。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface PaymentRecordRepository extends JpaRepository<PaymentRecordEntity, Long>,
        JpaSpecificationExecutor<PaymentRecordEntity> {

    /**
     * 按缴费编号查询。
     */
    Optional<PaymentRecordEntity> findByPaymentNo(String paymentNo);

    /**
     * 按患者ID查询缴费记录（按创建时间倒序）。
     */
    List<PaymentRecordEntity> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    /**
     * 按状态查询缴费记录。
     */
    List<PaymentRecordEntity> findByStatus(String status);

    /**
     * 按来源类型与来源ID查询缴费记录（用于业务关联）。
     */
    List<PaymentRecordEntity> findBySourceTypeAndSourceId(String sourceType, Long sourceId);

    /**
     * 按对账批次号查询缴费记录（用于对账批次明细）。
     */
    List<PaymentRecordEntity> findByReconcileBatchNo(String reconcileBatchNo);

    /**
     * 判断缴费编号是否已存在（唯一性校验）。
     */
    boolean existsByPaymentNo(String paymentNo);
}
