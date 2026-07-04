package com.aimedical.modules.inventory.repository;

import com.aimedical.modules.inventory.entity.StocktakingItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 盘点明细仓储。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface StocktakingItemRepository extends JpaRepository<StocktakingItemEntity, Long> {

    /**
     * 按盘点单ID查询全部明细。
     */
    List<StocktakingItemEntity> findByStocktakingId(Long stocktakingId);

    /**
     * 按盘点单ID删除全部明细。
     */
    void deleteByStocktakingId(Long stocktakingId);
}
