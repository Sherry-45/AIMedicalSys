package com.aimedical.modules.inventory.dto.request;

import lombok.Data;

/**
 * 药品目录查询请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class DrugCatalogQueryRequest {

    private String drugCode;

    private String drugName;

    private String drugCategory;

    private Boolean enabled;

    private Integer page = 0;

    private Integer size = 20;
}
