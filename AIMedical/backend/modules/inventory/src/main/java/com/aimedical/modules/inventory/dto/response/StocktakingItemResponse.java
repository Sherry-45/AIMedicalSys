package com.aimedical.modules.inventory.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 盘点明细响应 DTO。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class StocktakingItemResponse {

    private Long id;
    private Long stocktakingId;
    private String drugCode;
    private String drugName;
    private String batchNo;
    private BigDecimal bookQuantity;
    private BigDecimal actualQuantity;
    private BigDecimal difference;
    private String differenceType;
    private String unit;
    private String remark;
}
