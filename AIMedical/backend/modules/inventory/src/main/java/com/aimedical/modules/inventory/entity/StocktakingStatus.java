package com.aimedical.modules.inventory.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 盘点状态枚举。
 *
 * <p>状态流转：DRAFT -> IN_PROGRESS -> COMPLETED (或 CANCELLED)
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum StocktakingStatus implements BaseEnum {

    DRAFT("DRAFT", "草稿"),
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;
}
