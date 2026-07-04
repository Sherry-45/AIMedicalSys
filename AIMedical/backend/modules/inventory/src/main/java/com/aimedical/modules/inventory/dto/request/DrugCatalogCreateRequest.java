package com.aimedical.modules.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 药品目录创建请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class DrugCatalogCreateRequest {

    @NotBlank(message = "药品编码不能为空")
    @Size(max = 32, message = "药品编码长度不能超过32")
    private String drugCode;

    @NotBlank(message = "药品名称不能为空")
    @Size(max = 128, message = "药品名称长度不能超过128")
    private String drugName;

    @Size(max = 128)
    private String genericName;

    @Size(max = 128)
    private String specification;

    @Size(max = 128)
    private String manufacturer;

    @Size(max = 32)
    private String drugForm;

    @NotBlank(message = "药品分类不能为空")
    @Size(max = 32)
    private String drugCategory;

    @Size(max = 32)
    private String unit;

    private BigDecimal retailPrice;

    private BigDecimal purchasePrice;

    private Boolean otcFlag;

    @Size(max = 500)
    private String remark;
}
