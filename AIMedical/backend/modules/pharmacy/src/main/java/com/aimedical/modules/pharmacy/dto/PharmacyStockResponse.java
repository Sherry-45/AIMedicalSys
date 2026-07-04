package com.aimedical.modules.pharmacy.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 药房库存响应。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class PharmacyStockResponse {

    private Long id;
    private String drugCode;
    private String drugName;
    private String batchNo;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal retailPrice;
    private LocalDate expiryDate;
    private String shelfLocation;
    private BigDecimal safetyStock;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
