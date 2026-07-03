package com.aimedical.modules.pharmacy.repository;

import com.aimedical.modules.pharmacy.entity.PharmacyStockEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 药房库存仓储。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface PharmacyStockRepository extends JpaRepository<PharmacyStockEntity, Long> {

    /** 按药品编码查询所有批次库存 */
    List<PharmacyStockEntity> findByDrugCode(String drugCode);

    /** 按药品编码 + 批次号查询库存 */
    Optional<PharmacyStockEntity> findByDrugCodeAndBatchNo(String drugCode, String batchNo);

    /** 查询有效期早于指定日期的库存（近效期预警） */
    List<PharmacyStockEntity> findByExpiryDateBefore(LocalDate date);

    /** 按药品编码模糊分页查询 */
    Page<PharmacyStockEntity> findByDrugCodeContaining(String drugCode, Pageable pageable);

    /** 按药品名称模糊分页查询 */
    Page<PharmacyStockEntity> findByDrugNameContaining(String drugName, Pageable pageable);

    /** 按药品编码 + 名称模糊分页查询 */
    Page<PharmacyStockEntity> findByDrugCodeContainingAndDrugNameContaining(String drugCode, String drugName, Pageable pageable);

    /**
     * 查询库存低于安全库存阈值的记录（低库存预警）。
     * 当 safety_stock 不为空且 quantity < safety_stock 时返回。
     */
    @Query("SELECT s FROM PharmacyStockEntity s WHERE s.safetyStock IS NOT NULL AND s.quantity < s.safetyStock")
    List<PharmacyStockEntity> findLowStock();

    /**
     * 按药品编码查询库存总量（跨批次汇总）。
     */
    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM PharmacyStockEntity s WHERE s.drugCode = :drugCode")
    BigDecimal sumQuantityByDrugCode(@Param("drugCode") String drugCode);
}
