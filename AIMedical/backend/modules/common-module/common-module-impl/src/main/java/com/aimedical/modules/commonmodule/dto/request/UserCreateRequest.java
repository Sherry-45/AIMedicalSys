package com.aimedical.modules.commonmodule.dto.request;

import com.aimedical.modules.commonmodule.api.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * 用户创建请求。
 *
 * <p>管理员端创建用户（管理员/医生/患者）时使用。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度需在3-32之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度需在8-64之间")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称长度不能超过64")
    private String nickname;

    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过128")
    private String email;

    @NotNull(message = "用户类型不能为空")
    private UserType userType;

    @Size(max = 10, message = "性别长度不能超过10")
    private String gender;

    private Integer age;

    /** 角色 ID 集合（可选，关联到 sys_role） */
    private Set<Long> roleIds;

    /** 岗位 ID 集合（可选，关联到 sys_post） */
    private Set<Long> postIds;

    /** 是否需要下次登录修改密码（默认 false） */
    private Boolean passwordChangeRequired;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
