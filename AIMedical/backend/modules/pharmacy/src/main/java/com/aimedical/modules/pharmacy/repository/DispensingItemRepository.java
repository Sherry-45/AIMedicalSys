package com.aimedical.modules.pharmacy.repository;

import com.aimedical.modules.pharmacy.entity.DispensingItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 发药明细仓储。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface DispensingItemRepository extends JpaRepository<DispensingItemEntity, Long> {

    /** 按发药记录ID查询所有明细 */
    List<DispensingItemEntity> findByDispensingId(Long dispensingId);

    /** 按发药记录ID列表批量查询明细（用于分页场景避免 N+1） */
    List<DispensingItemEntity> findByDispensingIdIn(List<Long> dispensingIds);
}
