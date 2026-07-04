package com.aimedical.modules.inventory.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 盘点状态枚举。
 *
 * <p>状态流转：DRAFT -> IN_PROGRESS -> PENDING_APPROVAL -> APPROVED -> COMPLETED
 * <p>可从 PENDING_APPROVAL -> REJECTED
 * <p>可从 DRAFT/IN_PROGRESS -> CANCELLED
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum StocktakingStatus implements BaseEnum {

    DRAFT("DRAFT", "草稿"),
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    PENDING_APPROVAL("PENDING_APPROVAL", "待审批"),
    APPROVED("APPROVED", "已审批"),
    REJECTED("REJECTED", "已驳回"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;
}
