package com.aimedical.modules.inventory.repository;

import com.aimedical.modules.inventory.entity.DrugCatalogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 药品目录仓储。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface DrugCatalogRepository extends JpaRepository<DrugCatalogEntity, Long> {

    /**
     * 按药品编码查询。
     */
    Optional<DrugCatalogEntity> findByDrugCode(String drugCode);

    /**
     * 按药品名称模糊查询（分页）。
     */
    Page<DrugCatalogEntity> findByDrugNameContaining(String drugName, Pageable pageable);

    /**
     * 按药品分类查询（分页）。
     */
    Page<DrugCatalogEntity> findByDrugCategory(String drugCategory, Pageable pageable);

    /**
     * 按药品名称模糊 + 分类查询（分页）。
     */
    Page<DrugCatalogEntity> findByDrugNameContainingAndDrugCategory(String drugName, String drugCategory, Pageable pageable);

    /**
     * 判断药品编码是否已存在。
     */
    boolean existsByDrugCode(String drugCode);
}
