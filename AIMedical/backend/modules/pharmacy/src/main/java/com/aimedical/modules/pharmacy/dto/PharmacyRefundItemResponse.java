package com.aimedical.modules.pharmacy.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 退药明细响应。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class PharmacyRefundItemResponse {

    private Long id;
    private Long refundId;
    private Long dispensingItemId;
    private String drugCode;
    private String drugName;
    private String batchNo;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String remark;
}
