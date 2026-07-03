package com.aimedical.modules.inventory.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link StocktakingStatus} 单元测试。
 */
class StocktakingStatusTest {

    @Test
    void shouldDefineSevenStatuses() {
        assertEquals(7, StocktakingStatus.values().length);
        assertNotNull(StocktakingStatus.valueOf("DRAFT"));
        assertNotNull(StocktakingStatus.valueOf("IN_PROGRESS"));
        assertNotNull(StocktakingStatus.valueOf("PENDING_APPROVAL"));
        assertNotNull(StocktakingStatus.valueOf("APPROVED"));
        assertNotNull(StocktakingStatus.valueOf("REJECTED"));
        assertNotNull(StocktakingStatus.valueOf("COMPLETED"));
        assertNotNull(StocktakingStatus.valueOf("CANCELLED"));
    }

    @Test
    void shouldExposeCodeAndDescForDraft() {
        assertEquals("DRAFT", StocktakingStatus.DRAFT.getCode());
        assertEquals("草稿", StocktakingStatus.DRAFT.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForInProgress() {
        assertEquals("IN_PROGRESS", StocktakingStatus.IN_PROGRESS.getCode());
        assertEquals("进行中", StocktakingStatus.IN_PROGRESS.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForPendingApproval() {
        assertEquals("PENDING_APPROVAL", StocktakingStatus.PENDING_APPROVAL.getCode());
        assertEquals("待审批", StocktakingStatus.PENDING_APPROVAL.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForApproved() {
        assertEquals("APPROVED", StocktakingStatus.APPROVED.getCode());
        assertEquals("已审批", StocktakingStatus.APPROVED.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForRejected() {
        assertEquals("REJECTED", StocktakingStatus.REJECTED.getCode());
        assertEquals("已驳回", StocktakingStatus.REJECTED.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForCompleted() {
        assertEquals("COMPLETED", StocktakingStatus.COMPLETED.getCode());
        assertEquals("已完成", StocktakingStatus.COMPLETED.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForCancelled() {
        assertEquals("CANCELLED", StocktakingStatus.CANCELLED.getCode());
        assertEquals("已取消", StocktakingStatus.CANCELLED.getDesc());
    }

    @Test
    void allCodesShouldBeUnique() {
        long distinct = java.util.Arrays.stream(StocktakingStatus.values())
                .map(StocktakingStatus::getCode)
                .distinct()
                .count();
        assertEquals(StocktakingStatus.values().length, distinct);
    }
}
