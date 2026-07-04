package com.aimedical.modules.commonmodule.dto.request;

import com.aimedical.modules.commonmodule.api.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * 用户更新请求。
 *
 * <p>所有字段可选（部分更新）。用户名和密码不允许在此接口修改。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class UserUpdateRequest {

    @Size(max = 64, message = "昵称长度不能超过64")
    private String nickname;

    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过128")
    private String email;

    private UserType userType;

    @Size(max = 10, message = "性别长度不能超过10")
    private String gender;

    private Integer age;

    /** 角色 ID 集合（传入则全量替换原角色） */
    private Set<Long> roleIds;

    /** 岗位 ID 集合（传入则全量替换原岗位） */
    private Set<Long> postIds;

    private Boolean enabled;

    private Boolean passwordChangeRequired;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
