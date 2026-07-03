package com.aimedical.modules.window.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 线下挂号状态枚举。
 *
 * <p>状态机：ACTIVE(有效) -> CANCELLED(已取消)。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum OfflineRegistrationStatus implements BaseEnum {

    ACTIVE("ACTIVE", "有效"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;
}
