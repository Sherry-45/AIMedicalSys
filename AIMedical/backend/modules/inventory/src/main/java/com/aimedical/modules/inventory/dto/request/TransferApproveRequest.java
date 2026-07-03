package com.aimedical.modules.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 调拨审批请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class TransferApproveRequest {

    @NotNull(message = "审批结果不能为空")
    private Boolean approved;

    @Size(max = 500)
    private String rejectReason;
}
