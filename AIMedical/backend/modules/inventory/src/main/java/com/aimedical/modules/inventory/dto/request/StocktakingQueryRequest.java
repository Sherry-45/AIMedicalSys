package com.aimedical.modules.inventory.dto.request;

import lombok.Data;

/**
 * 盘点单查询请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class StocktakingQueryRequest {

    private String status;

    private Integer page = 0;

    private Integer size = 20;
}
