package com.aimedical.modules.pharmacy;

import com.aimedical.common.exception.ErrorCode;

/**
 * 药房域错误码。
 * <p>
 * 覆盖药房库存、发药、退药流程中的业务异常场景。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public enum PharmacyErrorCode implements ErrorCode {

    // ===== 库存相关 =====
    STOCK_NOT_FOUND("STOCK_NOT_FOUND", "药房库存记录不存在"),
    STOCK_INSUFFICIENT("STOCK_INSUFFICIENT", "药房库存不足，无法发药"),
    STOCK_BATCH_NOT_FOUND("STOCK_BATCH_NOT_FOUND", "指定批次的库存记录不存在"),
    STOCK_ADJUST_INVALID("STOCK_ADJUST_INVALID", "库存调整数量无效"),

    // ===== 发药相关 =====
    DISPENSING_NOT_FOUND("DISPENSING_NOT_FOUND", "发药记录不存在"),
    DISPENSING_INVALID_STATE("DISPENSING_INVALID_STATE", "发药记录状态不允许该操作"),
    DISPENSING_ALREADY_REFUNDED("DISPENSING_ALREADY_REFUNDED", "发药记录已退药，无法重复操作"),
    DISPENSING_ITEM_NOT_FOUND("DISPENSING_ITEM_NOT_FOUND", "发药明细记录不存在"),
    DISPENSING_ITEM_EMPTY("DISPENSING_ITEM_EMPTY", "发药记录至少需要包含一个明细"),
    DISPENSING_DUPLICATE("DISPENSING_DUPLICATE", "该处方已生成发药记录，不可重复创建"),

    // ===== 退药相关 =====
    REFUND_NOT_FOUND("REFUND_NOT_FOUND", "退药记录不存在"),
    REFUND_INVALID_STATE("REFUND_INVALID_STATE", "退药记录状态不允许该操作"),
    REFUND_QUANTITY_EXCEEDED("REFUND_QUANTITY_EXCEEDED", "退药数量超过发药数量"),
    REFUND_ITEM_NOT_FOUND("REFUND_ITEM_NOT_FOUND", "退药明细记录不存在"),
    REFUND_REASON_EMPTY("REFUND_REASON_EMPTY", "退药原因不能为空"),
    REFUND_DUPLICATE("REFUND_DUPLICATE", "该发药记录已存在待处理的退药申请");

    private final String code;
    private final String message;

    PharmacyErrorCode(String code, String message) {
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
