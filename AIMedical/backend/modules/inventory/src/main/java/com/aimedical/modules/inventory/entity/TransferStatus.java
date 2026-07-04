package com.aimedical.modules.inventory.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 调拨状态枚举。
 *
 * <p>状态流转：DRAFT -> PENDING_APPROVAL -> APPROVED -> IN_TRANSIT -> RECEIVED
 * <p>可从 PENDING_APPROVAL -> REJECTED
 * <p>可从 DRAFT/PENDING_APPROVAL -> CANCELLED
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum TransferStatus implements BaseEnum {

    DRAFT("DRAFT", "草稿"),
    PENDING_APPROVAL("PENDING_APPROVAL", "待审批"),
    APPROVED("APPROVED", "已审批"),
    IN_TRANSIT("IN_TRANSIT", "在途"),
    RECEIVED("RECEIVED", "已接收"),
    REJECTED("REJECTED", "已驳回"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;
}
