package com.aimedical.modules.inventory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link InventoryErrorCode} 单元测试。
 */
class InventoryErrorCodeTest {

    @Test
    void shouldDefineAllErrorCodes() {
        assertEquals(15, InventoryErrorCode.values().length);
    }

    @Test
    void drugNotFoundShouldExposeCodeAndMessage() {
        assertEquals("DRUG_NOT_FOUND", InventoryErrorCode.DRUG_NOT_FOUND.getCode());
        assertEquals("药品目录不存在", InventoryErrorCode.DRUG_NOT_FOUND.getMessage());
    }

    @Test
    void drugCodeDuplicateShouldExposeCodeAndMessage() {
        assertEquals("DRUG_CODE_DUPLICATE", InventoryErrorCode.DRUG_CODE_DUPLICATE.getCode());
        assertEquals("药品编码已存在", InventoryErrorCode.DRUG_CODE_DUPLICATE.getMessage());
    }

    @Test
    void drugDisabledShouldExposeCodeAndMessage() {
        assertEquals("DRUG_DISABLED", InventoryErrorCode.DRUG_DISABLED.getCode());
        assertEquals("药品已停用", InventoryErrorCode.DRUG_DISABLED.getMessage());
    }

    @Test
    void stockNotFoundShouldExposeCodeAndMessage() {
        assertEquals("STOCK_NOT_FOUND", InventoryErrorCode.STOCK_NOT_FOUND.getCode());
        assertEquals("库存批次不存在", InventoryErrorCode.STOCK_NOT_FOUND.getMessage());
    }

    @Test
    void stockInsufficientShouldExposeCodeAndMessage() {
        assertEquals("STOCK_INSUFFICIENT", InventoryErrorCode.STOCK_INSUFFICIENT.getCode());
        assertEquals("库存数量不足", InventoryErrorCode.STOCK_INSUFFICIENT.getMessage());
    }

    @Test
    void stockBatchNotFoundShouldExposeCodeAndMessage() {
        assertEquals("STOCK_BATCH_NOT_FOUND", InventoryErrorCode.STOCK_BATCH_NOT_FOUND.getCode());
        assertEquals("该药品批次库存不存在", InventoryErrorCode.STOCK_BATCH_NOT_FOUND.getMessage());
    }

    @Test
    void stocktakingNotFoundShouldExposeCodeAndMessage() {
        assertEquals("STOCKTAKING_NOT_FOUND", InventoryErrorCode.STOCKTAKING_NOT_FOUND.getCode());
        assertEquals("盘点单不存在", InventoryErrorCode.STOCKTAKING_NOT_FOUND.getMessage());
    }

    @Test
    void stocktakingInvalidStateShouldExposeCodeAndMessage() {
        assertEquals("STOCKTAKING_INVALID_STATE", InventoryErrorCode.STOCKTAKING_INVALID_STATE.getCode());
        assertEquals("盘点单状态不允许该操作", InventoryErrorCode.STOCKTAKING_INVALID_STATE.getMessage());
    }

    @Test
    void stocktakingItemEmptyShouldExposeCodeAndMessage() {
        assertEquals("STOCKTAKING_ITEM_EMPTY", InventoryErrorCode.STOCKTAKING_ITEM_EMPTY.getCode());
        assertEquals("盘点单至少需要包含一个明细", InventoryErrorCode.STOCKTAKING_ITEM_EMPTY.getMessage());
    }

    @Test
    void stocktakingActualEmptyShouldExposeCodeAndMessage() {
        assertEquals("STOCKTAKING_ACTUAL_EMPTY", InventoryErrorCode.STOCKTAKING_ACTUAL_EMPTY.getCode());
        assertEquals("提交实际数量时明细不能为空", InventoryErrorCode.STOCKTAKING_ACTUAL_EMPTY.getMessage());
    }

    @Test
    void transferNotFoundShouldExposeCodeAndMessage() {
        assertEquals("TRANSFER_NOT_FOUND", InventoryErrorCode.TRANSFER_NOT_FOUND.getCode());
        assertEquals("调拨单不存在", InventoryErrorCode.TRANSFER_NOT_FOUND.getMessage());
    }

    @Test
    void transferInvalidStateShouldExposeCodeAndMessage() {
        assertEquals("TRANSFER_INVALID_STATE", InventoryErrorCode.TRANSFER_INVALID_STATE.getCode());
        assertEquals("调拨单状态不允许该操作", InventoryErrorCode.TRANSFER_INVALID_STATE.getMessage());
    }

    @Test
    void transferItemEmptyShouldExposeCodeAndMessage() {
        assertEquals("TRANSFER_ITEM_EMPTY", InventoryErrorCode.TRANSFER_ITEM_EMPTY.getCode());
        assertEquals("调拨单至少需要包含一个明细", InventoryErrorCode.TRANSFER_ITEM_EMPTY.getMessage());
    }

    @Test
    void transferDeptSameShouldExposeCodeAndMessage() {
        assertEquals("TRANSFER_DEPT_SAME", InventoryErrorCode.TRANSFER_DEPT_SAME.getCode());
        assertEquals("调出与调入部门不能相同", InventoryErrorCode.TRANSFER_DEPT_SAME.getMessage());
    }

    @Test
    void transferApproveRejectReasonRequiredShouldExposeCodeAndMessage() {
        assertEquals("TRANSFER_APPROVE_REJECT_REASON_REQUIRED",
                InventoryErrorCode.TRANSFER_APPROVE_REJECT_REASON_REQUIRED.getCode());
        assertEquals("驳回时必须填写驳回原因",
                InventoryErrorCode.TRANSFER_APPROVE_REJECT_REASON_REQUIRED.getMessage());
    }

    @Test
    void allCodesShouldBeUnique() {
        long distinct = java.util.Arrays.stream(InventoryErrorCode.values())
                .map(InventoryErrorCode::getCode)
                .distinct()
                .count();
        assertEquals(InventoryErrorCode.values().length, distinct);
    }
}
