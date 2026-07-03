package com.aimedical.modules.pharmacy.repository;

import com.aimedical.modules.pharmacy.entity.PharmacyRefundItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 退药明细仓储。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface PharmacyRefundItemRepository extends JpaRepository<PharmacyRefundItemEntity, Long> {

    /** 按退药记录ID查询所有明细 */
    List<PharmacyRefundItemEntity> findByRefundId(Long refundId);

    /** 按退药记录ID列表批量查询明细（用于分页场景避免 N+1） */
    List<PharmacyRefundItemEntity> findByRefundIdIn(List<Long> refundIds);

    /** 按原发药明细ID查询关联的退药明细（用于累计退药数量校验） */
    List<PharmacyRefundItemEntity> findByDispensingItemId(Long dispensingItemId);
}
