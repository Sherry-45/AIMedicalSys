package com.aimedical.modules.inventory.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 调拨明细响应 DTO。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class TransferItemResponse {

    private Long id;
    private Long transferId;
    private String drugCode;
    private String drugName;
    private String specification;
    private String batchNo;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String remark;
}
