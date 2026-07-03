package com.aimedical.modules.inventory.dto.request;

import lombok.Data;

/**
 * 库存查询请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class InventoryStockQueryRequest {

    private String drugCode;

    private String batchNo;

    private Integer page = 0;

    private Integer size = 20;
}
