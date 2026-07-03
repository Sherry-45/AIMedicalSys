package com.aimedical.modules.window.repository;

import com.aimedical.modules.window.entity.PaymentItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * 缴费明细仓储。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface PaymentItemRepository extends JpaRepository<PaymentItemEntity, Long> {

    /**
     * 按缴费记录ID查询明细。
     */
    List<PaymentItemEntity> findByPaymentId(Long paymentId);

    /**
     * 按缴费记录ID集合批量查询明细（避免分页转换时的 N+1 查询）。
     */
    List<PaymentItemEntity> findByPaymentIdIn(Collection<Long> paymentIds);
}
