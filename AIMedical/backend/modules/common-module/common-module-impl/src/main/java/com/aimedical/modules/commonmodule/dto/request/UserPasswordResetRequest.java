package com.aimedical.modules.commonmodule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员重置用户密码请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class UserPasswordResetRequest {

    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度需在8-64之间")
    private String newPassword;

    /** 是否要求用户下次登录修改密码（默认 true） */
    private Boolean passwordChangeRequired;
}
