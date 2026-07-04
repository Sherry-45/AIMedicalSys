package com.aimedical.modules.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 盘点单创建请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class StocktakingCreateRequest {

    @Size(max = 20)
    private String stocktakingType;

    @Size(max = 500)
    private String remark;

    @Valid
    @NotEmpty(message = "盘点明细不能为空")
    private List<StocktakingItemRequest> items;
}
