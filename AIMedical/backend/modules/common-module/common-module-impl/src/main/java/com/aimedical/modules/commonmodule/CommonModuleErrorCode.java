package com.aimedical.modules.commonmodule;

import com.aimedical.common.exception.ErrorCode;

/**
 * 公共模块错误码。
 *
 * <p>覆盖用户/角色管理（管理员端）的业务异常场景。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public enum CommonModuleErrorCode implements ErrorCode {

    USER_NOT_FOUND("USER_NOT_FOUND", "用户不存在"),
    USERNAME_DUPLICATE("USERNAME_DUPLICATE", "用户名已存在"),
    USER_DISABLED("USER_DISABLED", "用户已被停用"),
    ROLE_NOT_FOUND("ROLE_NOT_FOUND", "角色不存在"),
    ROLE_CODE_DUPLICATE("ROLE_CODE_DUPLICATE", "角色编码已存在"),
    POST_NOT_FOUND("POST_NOT_FOUND", "岗位不存在"),
    POST_CODE_DUPLICATE("POST_CODE_DUPLICATE", "岗位编码已存在");

    private final String code;
    private final String message;

    CommonModuleErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
