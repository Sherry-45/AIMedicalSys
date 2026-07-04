package com.aimedical.modules.commonmodule.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色更新请求（部分更新）。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleUpdateRequest {

    @Size(max = 64, message = "角色名称长度不能超过64")
    private String name;

    @Size(max = 200, message = "描述长度不能超过200")
    private String description;

    private Boolean enabled;

    private Integer sort;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
