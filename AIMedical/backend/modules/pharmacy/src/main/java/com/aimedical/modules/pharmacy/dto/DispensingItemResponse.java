package com.aimedical.modules.pharmacy.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 发药明细响应。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class DispensingItemResponse {

    private Long id;
    private Long dispensingId;
    private String drugCode;
    private String drugName;
    private String specification;
    private String batchNo;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String dosage;
    private String usageMethod;
    private String frequency;
    private Integer days;
    private String remark;
}
