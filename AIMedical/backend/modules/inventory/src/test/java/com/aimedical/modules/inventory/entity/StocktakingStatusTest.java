package com.aimedical.modules.inventory.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link StocktakingStatus} 单元测试。
 */
class StocktakingStatusTest {

    @Test
    void shouldDefineFourStatuses() {
        assertEquals(4, StocktakingStatus.values().length);
        assertNotNull(StocktakingStatus.valueOf("DRAFT"));
        assertNotNull(StocktakingStatus.valueOf("IN_PROGRESS"));
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
