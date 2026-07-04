package com.aimedical.modules.inventory.repository;

import com.aimedical.modules.inventory.entity.TransferOrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 调拨单仓储。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface TransferOrderRepository extends JpaRepository<TransferOrderEntity, Long> {

    /**
     * 按状态查询（分页）。
     */
    Page<TransferOrderEntity> findByStatus(String status, Pageable pageable);

    /**
     * 按调拨类型查询（分页）。
     */
    Page<TransferOrderEntity> findByTransferType(String transferType, Pageable pageable);

    /**
     * 按状态 + 调拨类型查询（分页）。
     */
    Page<TransferOrderEntity> findByStatusAndTransferType(String status, String transferType, Pageable pageable);

    /**
     * 按调拨单号查询。
     */
    Optional<TransferOrderEntity> findByTransferNo(String transferNo);

    /**
     * 判断调拨单号是否已存在。
     */
    boolean existsByTransferNo(String transferNo);
}
