package com.aimedical.modules.inventory.repository;

import com.aimedical.modules.inventory.entity.StocktakingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 盘点单仓储。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface StocktakingRepository extends JpaRepository<StocktakingEntity, Long> {

    /**
     * 按状态查询（分页）。
     */
    Page<StocktakingEntity> findByStatus(String status, Pageable pageable);

    /**
     * 按盘点单号查询。
     */
    Optional<StocktakingEntity> findByStocktakingNo(String stocktakingNo);

    /**
     * 判断盘点单号是否已存在。
     */
    boolean existsByStocktakingNo(String stocktakingNo);
}
