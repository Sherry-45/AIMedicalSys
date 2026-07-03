package com.aimedical.modules.inventory.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存批次响应 DTO。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class InventoryStockResponse {

    private Long id;
    private String drugCode;
    private String batchNo;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal purchasePrice;
    private BigDecimal retailPrice;
    private LocalDate expiryDate;
    private LocalDate productionDate;
    private String warehouseLocation;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
