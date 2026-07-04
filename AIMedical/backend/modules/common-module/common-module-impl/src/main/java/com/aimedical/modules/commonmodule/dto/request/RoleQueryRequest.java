package com.aimedical.modules.commonmodule.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色分页查询请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class RoleQueryRequest {

    @Size(max = 64, message = "关键字长度不能超过64")
    private String keyword;

    private Boolean enabled;

    private Integer page = 0;

    private Integer size = 20;
}
