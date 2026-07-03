package com.aimedical.modules.inventory.repository;

import com.aimedical.modules.inventory.entity.TransferItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 调拨明细仓储。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface TransferItemRepository extends JpaRepository<TransferItemEntity, Long> {

    /**
     * 按调拨单ID查询全部明细。
     */
    List<TransferItemEntity> findByTransferId(Long transferId);

    /**
     * 按调拨单ID删除全部明细。
     */
    void deleteByTransferId(Long transferId);
}
