package com.aimedical.modules.commonmodule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色创建请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class RoleCreateRequest {

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 64, message = "角色编码长度不能超过64")
    private String code;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 64, message = "角色名称长度不能超过64")
    private String name;

    @Size(max = 200, message = "描述长度不能超过200")
    private String description;

    private Boolean enabled = true;

    private Integer sort = 0;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
