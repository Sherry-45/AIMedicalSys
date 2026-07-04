package com.aimedical.modules.inventory.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link StocktakingDifferenceType} 单元测试。
 */
class StocktakingDifferenceTypeTest {

    @Test
    void shouldDefineThreeTypes() {
        assertEquals(3, StocktakingDifferenceType.values().length);
        assertNotNull(StocktakingDifferenceType.valueOf("SURPLUS"));
        assertNotNull(StocktakingDifferenceType.valueOf("LOSS"));
        assertNotNull(StocktakingDifferenceType.valueOf("NONE"));
    }

    @Test
    void shouldExposeCodeAndDescForSurplus() {
        assertEquals("SURPLUS", StocktakingDifferenceType.SURPLUS.getCode());
        assertEquals("盘盈", StocktakingDifferenceType.SURPLUS.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForLoss() {
        assertEquals("LOSS", StocktakingDifferenceType.LOSS.getCode());
        assertEquals("盘亏", StocktakingDifferenceType.LOSS.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForNone() {
        assertEquals("NONE", StocktakingDifferenceType.NONE.getCode());
        assertEquals("无差异", StocktakingDifferenceType.NONE.getDesc());
    }

    @Test
    void allCodesShouldBeUnique() {
        long distinct = java.util.Arrays.stream(StocktakingDifferenceType.values())
                .map(StocktakingDifferenceType::getCode)
                .distinct()
                .count();
        assertEquals(StocktakingDifferenceType.values().length, distinct);
    }
}
