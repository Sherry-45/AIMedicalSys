package com.aimedical.modules.inventory.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品目录响应 DTO。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class DrugCatalogResponse {

    private Long id;
    private String drugCode;
    private String drugName;
    private String genericName;
    private String specification;
    private String manufacturer;
    private String drugForm;
    private String drugCategory;
    private String unit;
    private BigDecimal retailPrice;
    private BigDecimal purchasePrice;
    private Boolean otcFlag;
    private Boolean enabled;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
