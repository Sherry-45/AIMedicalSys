package com.aimedical.modules.window;

import com.aimedical.common.exception.ErrorCode;

/**
 * 线下窗口模块业务错误码。
 *
 * <p>涵盖线下挂号、缴费、退费、对账等业务的错误场景。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public enum WindowErrorCode implements ErrorCode {

    REGISTRATION_NOT_FOUND("REGISTRATION_NOT_FOUND", "线下挂号记录不存在"),
    REGISTRATION_ALREADY_CANCELLED("REGISTRATION_ALREADY_CANCELLED", "该挂号记录已取消，无法重复操作"),
    REGISTRATION_INVALID_STATE("REGISTRATION_INVALID_STATE", "当前挂号状态不允许该操作"),
    REGISTRATION_NO_DUPLICATE("REGISTRATION_NO_DUPLICATE", "挂号编号已存在"),
    REGISTRATION_PAYMENT_ALREADY_REFUNDED("REGISTRATION_PAYMENT_ALREADY_REFUNDED", "挂号关联缴费已退款，无法退号"),

    PAYMENT_NOT_FOUND("PAYMENT_NOT_FOUND", "缴费记录不存在"),
    PAYMENT_INVALID_STATE("PAYMENT_INVALID_STATE", "当前缴费状态不允许该操作"),
    PAYMENT_ALREADY_PAID("PAYMENT_ALREADY_PAID", "该缴费记录已支付，不可重复支付"),
    PAYMENT_ALREADY_REFUNDED("PAYMENT_ALREADY_REFUNDED", "该缴费记录已退款，无法重复退款"),
    PAYMENT_NO_DUPLICATE("PAYMENT_NO_DUPLICATE", "缴费编号已存在"),
    PAYMENT_ITEM_EMPTY("PAYMENT_ITEM_EMPTY", "缴费至少需要包含一个项目"),
    PAYMENT_AMOUNT_MISMATCH("PAYMENT_AMOUNT_MISMATCH", "支付金额与应缴金额不一致"),

    REFUND_AMOUNT_EXCEEDED("REFUND_AMOUNT_EXCEEDED", "退款金额超过已支付金额"),
    REFUND_NOT_PAID("REFUND_NOT_PAID", "未支付的记录不可退款"),

    RECONCILE_INVALID_STATE("RECONCILE_INVALID_STATE", "仅已支付(PAID)的记录可对账"),
    RECONCILE_BATCH_NOT_FOUND("RECONCILE_BATCH_NOT_FOUND", "对账批次不存在或无记录"),
    RECONCILE_NO_RECORDS("RECONCILE_NO_RECORDS", "对账列表不能为空");

    private final String code;
    private final String message;

    WindowErrorCode(String code, String message) {
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
