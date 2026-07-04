package com.aimedical.modules.pharmacy;

import com.aimedical.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PharmacyErrorCodeTest {

    @Test
    void shouldDefineAllConstants() {
        assertEquals(16, PharmacyErrorCode.values().length);
        assertNotNull(PharmacyErrorCode.valueOf("STOCK_NOT_FOUND"));
        assertNotNull(PharmacyErrorCode.valueOf("STOCK_INSUFFICIENT"));
        assertNotNull(PharmacyErrorCode.valueOf("STOCK_BATCH_NOT_FOUND"));
        assertNotNull(PharmacyErrorCode.valueOf("STOCK_ADJUST_INVALID"));
        assertNotNull(PharmacyErrorCode.valueOf("DISPENSING_NOT_FOUND"));
        assertNotNull(PharmacyErrorCode.valueOf("DISPENSING_INVALID_STATE"));
        assertNotNull(PharmacyErrorCode.valueOf("DISPENSING_ALREADY_REFUNDED"));
        assertNotNull(PharmacyErrorCode.valueOf("DISPENSING_ITEM_NOT_FOUND"));
        assertNotNull(PharmacyErrorCode.valueOf("DISPENSING_ITEM_EMPTY"));
        assertNotNull(PharmacyErrorCode.valueOf("DISPENSING_DUPLICATE"));
        assertNotNull(PharmacyErrorCode.valueOf("REFUND_NOT_FOUND"));
        assertNotNull(PharmacyErrorCode.valueOf("REFUND_INVALID_STATE"));
        assertNotNull(PharmacyErrorCode.valueOf("REFUND_QUANTITY_EXCEEDED"));
        assertNotNull(PharmacyErrorCode.valueOf("REFUND_ITEM_NOT_FOUND"));
        assertNotNull(PharmacyErrorCode.valueOf("REFUND_REASON_EMPTY"));
        assertNotNull(PharmacyErrorCode.valueOf("REFUND_DUPLICATE"));
    }

    @Test
    void shouldReturnCodeAndMessageForStockNotFound() {
        assertEquals("STOCK_NOT_FOUND", PharmacyErrorCode.STOCK_NOT_FOUND.getCode());
        assertEquals("药房库存记录不存在", PharmacyErrorCode.STOCK_NOT_FOUND.getMessage());
    }

    @Test
    void shouldReturnCodeAndMessageForStockInsufficient() {
        assertEquals("STOCK_INSUFFICIENT", PharmacyErrorCode.STOCK_INSUFFICIENT.getCode());
        assertEquals("药房库存不足，无法发药", PharmacyErrorCode.STOCK_INSUFFICIENT.getMessage());
    }

    @Test
    void shouldReturnCodeAndMessageForStockBatchNotFound() {
        assertEquals("STOCK_BATCH_NOT_FOUND", PharmacyErrorCode.STOCK_BATCH_NOT_FOUND.getCode());
        assertEquals("指定批次的库存记录不存在", PharmacyErrorCode.STOCK_BATCH_NOT_FOUND.getMessage());
    }

    @Test
    void shouldReturnCodeAndMessageForStockAdjustInvalid() {
        assertEquals("STOCK_ADJUST_INVALID", PharmacyErrorCode.STOCK_ADJUST_INVALID.getCode());
        assertEquals("库存调整数量无效", PharmacyErrorCode.STOCK_ADJUST_INVALID.getMessage());
    }

    @Test
    void shouldReturnCodeAndMessageForDispensingNotFound() {
        assertEquals("DISPENSING_NOT_FOUND", PharmacyErrorCode.DISPENSING_NOT_FOUND.getCode());
        assertEquals("发药记录不存在", PharmacyErrorCode.DISPENSING_NOT_FOUND.getMessage());
    }

    @Test
    void shouldReturnCodeAndMessageForDispensingInvalidState() {
        assertEquals("DISPENSING_INVALID_STATE", PharmacyErrorCode.DISPENSING_INVALID_STATE.getCode());
        assertEquals("发药记录状态不允许该操作", PharmacyErrorCode.DISPENSING_INVALID_STATE.getMessage());
    }

    @Test
    void shouldReturnCodeAndMessageForDispensingAlreadyRefunded() {
        assertEquals("DISPENSING_ALREADY_REFUNDED", PharmacyErrorCode.DISPENSING_ALREADY_REFUNDED.getCode());
        assertEquals("发药记录已退药，无法重复操作", PharmacyErrorCode.DISPENSING_ALREADY_REFUNDED.getMessage());
    }

    @Test
    void shouldReturnCodeAndMessageForDispensingItemNotFound() {
        assertEquals("DISPENSING_ITEM_NOT_FOUND", PharmacyErrorCode.DISPENSING_ITEM_NOT_FOUND.getCode());
        assertEquals("发药明细记录不存在", PharmacyErrorCode.DISPENSING_ITEM_NOT_FOUND.getMessage());
    }

    @Test
    void shouldReturnCodeAndMessageForDispensingItemEmpty() {
        assertEquals("DISPENSING_ITEM_EMPTY", PharmacyErrorCode.DISPENSING_ITEM_EMPTY.getCode());
        assertEquals("发药记录至少需要包含一个明细", PharmacyErrorCode.DISPENSING_ITEM_EMPTY.getMessage());
    }

    @Test
    void shouldReturnCodeAndMessageForDispensingDuplicate() {
        assertEquals("DISPENSING_DUPLICATE", PharmacyErrorCode.DISPENSING_DUPLICATE.getCode());
        assertEquals("该处方已生成发药记录，不可重复创建", PharmacyErrorCode.DISPENSING_DUPLICATE.getMessage());
    }

    @Test
    void shouldReturnCodeAndMessageForRefundNotFound() {
        assertEquals("REFUND_NOT_FOUND", PharmacyErrorCode.REFUND_NOT_FOUND.getCode());
        assertEquals("退药记录不存在", PharmacyErrorCode.REFUND_NOT_FOUND.getMessage());
    }

    @Test
    void shouldReturnCodeAndMessageForRefundInvalidState() {
        assertEquals("REFUND_INVALID_STATE", PharmacyErrorCode.REFUND_INVALID_STATE.getCode());
        assertEquals("退药记录状态不允许该操作", PharmacyErrorCode.REFUND_INVALID_STATE.getMessage());
    }

    @Test
    void shouldReturnCodeAndMessageForRefundQuantityExceeded() {
        assertEquals("REFUND_QUANTITY_EXCEEDED", PharmacyErrorCode.REFUND_QUANTITY_EXCEEDED.getCode());
        assertEquals("退药数量超过发药数量", PharmacyErrorCode.REFUND_QUANTITY_EXCEEDED.getMessage());
    }

    @Test
    void shouldReturnCodeAndMessageForRefundItemNotFound() {
        assertEquals("REFUND_ITEM_NOT_FOUND", PharmacyErrorCode.REFUND_ITEM_NOT_FOUND.getCode());
        assertEquals("退药明细记录不存在", PharmacyErrorCode.REFUND_ITEM_NOT_FOUND.getMessage());
    }

    @Test
    void shouldReturnCodeAndMessageForRefundReasonEmpty() {
        assertEquals("REFUND_REASON_EMPTY", PharmacyErrorCode.REFUND_REASON_EMPTY.getCode());
        assertEquals("退药原因不能为空", PharmacyErrorCode.REFUND_REASON_EMPTY.getMessage());
    }

    @Test
    void shouldReturnCodeAndMessageForRefundDuplicate() {
        assertEquals("REFUND_DUPLICATE", PharmacyErrorCode.REFUND_DUPLICATE.getCode());
        assertEquals("该发药记录已存在待处理的退药申请", PharmacyErrorCode.REFUND_DUPLICATE.getMessage());
    }

    @Test
    void allValuesShouldImplementErrorCode() {
        for (PharmacyErrorCode code : PharmacyErrorCode.values()) {
            assertInstanceOf(ErrorCode.class, code);
        }
    }
}
