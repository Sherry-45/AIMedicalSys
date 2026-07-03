package com.aimedical.modules.inventory.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DrugCategory} 单元测试。
 */
class DrugCategoryTest {

    @Test
    void shouldDefineFourCategories() {
        assertEquals(4, DrugCategory.values().length);
        assertNotNull(DrugCategory.valueOf("WESTERN_MEDICINE"));
        assertNotNull(DrugCategory.valueOf("CHINESE_MEDICINE"));
        assertNotNull(DrugCategory.valueOf("BIOLOGICAL"));
        assertNotNull(DrugCategory.valueOf("DEVICE"));
    }

    @Test
    void shouldExposeCodeAndDescForWesternMedicine() {
        assertEquals("WESTERN_MEDICINE", DrugCategory.WESTERN_MEDICINE.getCode());
        assertEquals("西药", DrugCategory.WESTERN_MEDICINE.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForChineseMedicine() {
        assertEquals("CHINESE_MEDICINE", DrugCategory.CHINESE_MEDICINE.getCode());
        assertEquals("中药", DrugCategory.CHINESE_MEDICINE.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForBiological() {
        assertEquals("BIOLOGICAL", DrugCategory.BIOLOGICAL.getCode());
        assertEquals("生物制品", DrugCategory.BIOLOGICAL.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForDevice() {
        assertEquals("DEVICE", DrugCategory.DEVICE.getCode());
        assertEquals("器械", DrugCategory.DEVICE.getDesc());
    }

    @Test
    void allCodesShouldBeUnique() {
        long distinct = java.util.Arrays.stream(DrugCategory.values())
                .map(DrugCategory::getCode)
                .distinct()
                .count();
        assertEquals(DrugCategory.values().length, distinct);
    }
}
