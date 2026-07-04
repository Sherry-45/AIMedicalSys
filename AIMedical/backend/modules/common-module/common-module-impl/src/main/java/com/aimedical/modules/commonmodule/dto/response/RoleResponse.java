package com.aimedical.modules.commonmodule.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色响应 DTO。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class RoleResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean enabled;
    private Integer sort;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
