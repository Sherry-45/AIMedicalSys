package com.aimedical.modules.window.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 线下挂号取消请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class OfflineRegistrationCancelRequest {

    @NotBlank(message = "取消原因不能为空")
    @Size(max = 500)
    private String cancelReason;
}
