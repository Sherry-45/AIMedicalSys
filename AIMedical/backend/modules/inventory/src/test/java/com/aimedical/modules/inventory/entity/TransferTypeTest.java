package com.aimedical.modules.inventory.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link TransferType} 单元测试。
 */
class TransferTypeTest {

    @Test
    void shouldDefineThreeTypes() {
        assertEquals(3, TransferType.values().length);
        assertNotNull(TransferType.valueOf("INVENTORY_TO_PHARMACY"));
        assertNotNull(TransferType.valueOf("PHARMACY_TO_INVENTORY"));
        assertNotNull(TransferType.valueOf("PHARMACY_TO_PHARMACY"));
    }

    @Test
    void shouldExposeCodeAndDescForInventoryToPharmacy() {
        assertEquals("INVENTORY_TO_PHARMACY", TransferType.INVENTORY_TO_PHARMACY.getCode());
        assertEquals("药库到药房", TransferType.INVENTORY_TO_PHARMACY.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForPharmacyToInventory() {
        assertEquals("PHARMACY_TO_INVENTORY", TransferType.PHARMACY_TO_INVENTORY.getCode());
        assertEquals("药房到药库", TransferType.PHARMACY_TO_INVENTORY.getDesc());
    }

    @Test
    void shouldExposeCodeAndDescForPharmacyToPharmacy() {
        assertEquals("PHARMACY_TO_PHARMACY", TransferType.PHARMACY_TO_PHARMACY.getCode());
        assertEquals("药房到药房", TransferType.PHARMACY_TO_PHARMACY.getDesc());
    }

    @Test
    void allCodesShouldBeUnique() {
        long distinct = java.util.Arrays.stream(TransferType.values())
                .map(TransferType::getCode)
                .distinct()
                .count();
        assertEquals(TransferType.values().length, distinct);
    }
}
