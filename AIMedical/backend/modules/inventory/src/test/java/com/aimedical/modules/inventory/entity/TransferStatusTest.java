package com.aimedical.modules.inventory.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link TransferStatus} 单元测试。
 */
class TransferStatusTest {

    @Test
    void shouldDefineSevenStatuses() {
        assertEquals(7, TransferStatus.values().length);
        assertNotNull(TransferStatus.valueOf("DRAFT"));
        assertNotNull(TransferStatus.valueOf("PENDING_APPROVAL"));
        assertNotNull(TransferStatus.valueOf("APPROVED"));
        assertNotNull(TransferStatus.valueOf("IN_TRANSIT"));
        assertNotNull(TransferStatus.valueOf("RECEIVED"));
        assertNotNull(TransferStatus.valueOf("REJECTED"));
        assertNotNull(TransferStatus.valueOf("CANCELLED"));
    }

    @Test
    void shouldExposeCodeAndDescForDraft() {
        assertEquals("DRAFT", TransferStatus.DRAFT.getCode());
        assertEquals("草稿", TransferStatus.DRAFT.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForPendingApproval() {
        assertEquals("PENDING_APPROVAL", TransferStatus.PENDING_APPROVAL.getCode());
        assertEquals("待审批", TransferStatus.PENDING_APPROVAL.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForApproved() {
        assertEquals("APPROVED", TransferStatus.APPROVED.getCode());
        assertEquals("已审批", TransferStatus.APPROVED.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForInTransit() {
        assertEquals("IN_TRANSIT", TransferStatus.IN_TRANSIT.getCode());
        assertEquals("在途", TransferStatus.IN_TRANSIT.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForReceived() {
        assertEquals("RECEIVED", TransferStatus.RECEIVED.getCode());
        assertEquals("已接收", TransferStatus.RECEIVED.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForRejected() {
        assertEquals("REJECTED", TransferStatus.REJECTED.getCode());
        assertEquals("已驳回", TransferStatus.REJECTED.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForCancelled() {
        assertEquals("CANCELLED", TransferStatus.CANCELLED.getCode());
        assertEquals("已取消", TransferStatus.CANCELLED.getDesc());
    }

    @Test
    void allCodesShouldBeUnique() {
        long distinct = java.util.Arrays.stream(TransferStatus.values())
                .map(TransferStatus::getCode)
                .distinct()
                .count();
        assertEquals(TransferStatus.values().length, distinct);
    }
}
