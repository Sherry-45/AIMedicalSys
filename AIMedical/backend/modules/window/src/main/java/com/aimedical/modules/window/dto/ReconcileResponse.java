package com.aimedical.modules.window.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 对账结果响应 DTO。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class ReconcileResponse {

    private String reconcileBatchNo;
    private Integer reconciledCount;
    private BigDecimal totalAmount;
}
