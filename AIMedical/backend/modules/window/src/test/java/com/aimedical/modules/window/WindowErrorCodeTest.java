package com.aimedical.modules.window;

import com.aimedical.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class WindowErrorCodeTest {

    @Test
    void shouldDefineSeventeenCodes() {
        assertEquals(17, WindowErrorCode.values().length);
    }

    @Test
    void shouldExposeCodeAndMessageForRegistrationNotFound() {
        assertEquals("REGISTRATION_NOT_FOUND", WindowErrorCode.REGISTRATION_NOT_FOUND.getCode());
        assertEquals("线下挂号记录不存在", WindowErrorCode.REGISTRATION_NOT_FOUND.getMessage());
    }

    @Test
    void shouldExposeCodeAndMessageForRegistrationAlreadyCancelled() {
        assertEquals("REGISTRATION_ALREADY_CANCELLED", WindowErrorCode.REGISTRATION_ALREADY_CANCELLED.getCode());
        assertEquals("该挂号记录已取消，无法重复操作", WindowErrorCode.REGISTRATION_ALREADY_CANCELLED.getMessage());
    }

    @Test
    void shouldExposeCodeAndMessageForRegistrationInvalidState() {
        assertEquals("REGISTRATION_INVALID_STATE", WindowErrorCode.REGISTRATION_INVALID_STATE.getCode());
        assertEquals("当前挂号状态不允许该操作", WindowErrorCode.REGISTRATION_INVALID_STATE.getMessage());
    }

    @Test
    void shouldExposeCodeAndMessageForRegistrationNoDuplicate() {
        assertEquals("REGISTRATION_NO_DUPLICATE", WindowErrorCode.REGISTRATION_NO_DUPLICATE.getCode());
        assertEquals("挂号编号已存在", WindowErrorCode.REGISTRATION_NO_DUPLICATE.getMessage());
    }

    @Test
    void shouldExposeCodeAndMessageForRegistrationPaymentAlreadyRefunded() {
        assertEquals("REGISTRATION_PAYMENT_ALREADY_REFUNDED", WindowErrorCode.REGISTRATION_PAYMENT_ALREADY_REFUNDED.getCode());
        assertEquals("挂号关联缴费已退款，无法退号", WindowErrorCode.REGISTRATION_PAYMENT_ALREADY_REFUNDED.getMessage());
    }

    @Test
    void shouldExposeCodeAndMessageForPaymentNotFound() {
        assertEquals("PAYMENT_NOT_FOUND", WindowErrorCode.PAYMENT_NOT_FOUND.getCode());
        assertEquals("缴费记录不存在", WindowErrorCode.PAYMENT_NOT_FOUND.getMessage());
    }

    @Test
    void shouldExposeCodeAndMessageForPaymentInvalidState() {
        assertEquals("PAYMENT_INVALID_STATE", WindowErrorCode.PAYMENT_INVALID_STATE.getCode());
        assertEquals("当前缴费状态不允许该操作", WindowErrorCode.PAYMENT_INVALID_STATE.getMessage());
    }

    @Test
    void shouldExposeCodeAndMessageForPaymentAlreadyPaid() {
        assertEquals("PAYMENT_ALREADY_PAID", WindowErrorCode.PAYMENT_ALREADY_PAID.getCode());
        assertEquals("该缴费记录已支付，不可重复支付", WindowErrorCode.PAYMENT_ALREADY_PAID.getMessage());
    }

    @Test
    void shouldExposeCodeAndMessageForPaymentAlreadyRefunded() {
        assertEquals("PAYMENT_ALREADY_REFUNDED", WindowErrorCode.PAYMENT_ALREADY_REFUNDED.getCode());
        assertEquals("该缴费记录已退款，无法重复退款", WindowErrorCode.PAYMENT_ALREADY_REFUNDED.getMessage());
    }

    @Test
    void shouldExposeCodeAndMessageForPaymentNoDuplicate() {
        assertEquals("PAYMENT_NO_DUPLICATE", WindowErrorCode.PAYMENT_NO_DUPLICATE.getCode());
        assertEquals("缴费编号已存在", WindowErrorCode.PAYMENT_NO_DUPLICATE.getMessage());
    }

    @Test
    void shouldExposeCodeAndMessageForPaymentItemEmpty() {
        assertEquals("PAYMENT_ITEM_EMPTY", WindowErrorCode.PAYMENT_ITEM_EMPTY.getCode());
        assertEquals("缴费至少需要包含一个项目", WindowErrorCode.PAYMENT_ITEM_EMPTY.getMessage());
    }

    @Test
    void shouldExposeCodeAndMessageForPaymentAmountMismatch() {
        assertEquals("PAYMENT_AMOUNT_MISMATCH", WindowErrorCode.PAYMENT_AMOUNT_MISMATCH.getCode());
        assertEquals("支付金额与应缴金额不一致", WindowErrorCode.PAYMENT_AMOUNT_MISMATCH.getMessage());
    }

    @Test
    void shouldExposeCodeAndMessageForRefundAmountExceeded() {
        assertEquals("REFUND_AMOUNT_EXCEEDED", WindowErrorCode.REFUND_AMOUNT_EXCEEDED.getCode());
        assertEquals("退款金额超过已支付金额", WindowErrorCode.REFUND_AMOUNT_EXCEEDED.getMessage());
    }

    @Test
    void shouldExposeCodeAndMessageForRefundNotPaid() {
        assertEquals("REFUND_NOT_PAID", WindowErrorCode.REFUND_NOT_PAID.getCode());
        assertEquals("未支付的记录不可退款", WindowErrorCode.REFUND_NOT_PAID.getMessage());
    }

    @Test
    void shouldExposeCodeAndMessageForReconcileInvalidState() {
        assertEquals("RECONCILE_INVALID_STATE", WindowErrorCode.RECONCILE_INVALID_STATE.getCode());
        assertEquals("仅已支付(PAID)的记录可对账", WindowErrorCode.RECONCILE_INVALID_STATE.getMessage());
    }

    @Test
    void shouldExposeCodeAndMessageForReconcileBatchNotFound() {
        assertEquals("RECONCILE_BATCH_NOT_FOUND", WindowErrorCode.RECONCILE_BATCH_NOT_FOUND.getCode());
        assertEquals("对账批次不存在或无记录", WindowErrorCode.RECONCILE_BATCH_NOT_FOUND.getMessage());
    }

    @Test
    void shouldExposeCodeAndMessageForReconcileNoRecords() {
        assertEquals("RECONCILE_NO_RECORDS", WindowErrorCode.RECONCILE_NO_RECORDS.getCode());
        assertEquals("对账列表不能为空", WindowErrorCode.RECONCILE_NO_RECORDS.getMessage());
    }

    @Test
    void shouldDefaultToBadRequestHttpStatus() {
        for (WindowErrorCode code : WindowErrorCode.values()) {
            assertInstanceOf(ErrorCode.class, code);
            assertEquals(HttpStatus.BAD_REQUEST, code.getHttpStatus());
            assertNotNull(code.getCode());
            assertNotNull(code.getMessage());
        }
    }

    @Test
    void shouldHaveUniqueCodes() {
        long uniqueCount = java.util.Arrays.stream(WindowErrorCode.values())
                .map(WindowErrorCode::getCode)
                .distinct()
                .count();
        assertEquals(WindowErrorCode.values().length, uniqueCount);
    }
}
