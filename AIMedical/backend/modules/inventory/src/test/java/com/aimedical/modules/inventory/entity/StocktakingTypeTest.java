package com.aimedical.modules.inventory.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link StocktakingType} 单元测试。
 */
class StocktakingTypeTest {

    @Test
    void shouldDefineThreeTypes() {
        assertEquals(3, StocktakingType.values().length);
        assertNotNull(StocktakingType.valueOf("FULL"));
        assertNotNull(StocktakingType.valueOf("PARTIAL"));
        assertNotNull(StocktakingType.valueOf("SPOT"));
    }

    @Test
    void shouldExposeCodeAndDescForFull() {
        assertEquals("FULL", StocktakingType.FULL.getCode());
        assertEquals("全盘", StocktakingType.FULL.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForPartial() {
        assertEquals("PARTIAL", StocktakingType.PARTIAL.getCode());
        assertEquals("部分盘", StocktakingType.PARTIAL.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForSpot() {
        assertEquals("SPOT", StocktakingType.SPOT.getCode());
        assertEquals("抽盘", StocktakingType.SPOT.getDesc());
    }

    @Test
    void allCodesShouldBeUnique() {
        long distinct = java.util.Arrays.stream(StocktakingType.values())
                .map(StocktakingType::getCode)
                .distinct()
                .count();
        assertEquals(StocktakingType.values().length, distinct);
    }
}
