package com.aimedical.modules.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 盘点驳回请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class StocktakingRejectRequest {

    @NotBlank
    @Size(max = 500)
    private String rejectReason;
}
