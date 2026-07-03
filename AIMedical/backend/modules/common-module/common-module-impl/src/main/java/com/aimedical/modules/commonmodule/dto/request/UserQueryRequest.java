package com.aimedical.modules.commonmodule.dto.request;

import com.aimedical.modules.commonmodule.api.UserType;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户分页查询请求。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class UserQueryRequest {

    /** 搜索关键字（用户名/昵称/手机号模糊匹配） */
    @Size(max = 64, message = "关键字长度不能超过64")
    private String keyword;

    /** 用户类型筛选（DOCTOR/PATIENT/ADMIN） */
    private UserType userType;

    /** 是否启用筛选 */
    private Boolean enabled;

    /** 页码，从 0 开始 */
    private Integer page = 0;

    /** 每页大小 */
    private Integer size = 20;
}
