package com.aimedical.modules.inventory.repository;

import com.aimedical.modules.inventory.entity.InventoryStockEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 库存批次仓储。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface InventoryStockRepository extends JpaRepository<InventoryStockEntity, Long> {

    /**
     * 按药品编码查询全部批次（分页）。
     */
    Page<InventoryStockEntity> findByDrugCode(String drugCode, Pageable pageable);

    /**
     * 按药品编码查询全部批次（不分页，用于盘点自动填充账面数量）。
     */
    List<InventoryStockEntity> findByDrugCode(String drugCode);

    /**
     * 按药品编码 + 批次号查询。
     */
    Optional<InventoryStockEntity> findByDrugCodeAndBatchNo(String drugCode, String batchNo);

    /**
     * 按批次号查询。
     */
    List<InventoryStockEntity> findByBatchNo(String batchNo);

    /**
     * 查询有效期早于指定日期的批次（近效期预警）。
     */
    List<InventoryStockEntity> findByExpiryDateBefore(LocalDate date);

    /**
     * 按药品编码 + 批次号查询（分页）。
     */
    Page<InventoryStockEntity> findByDrugCodeAndBatchNo(String drugCode, String batchNo, Pageable pageable);
}
