package com.aimedical.modules.inventory.converter;

import com.aimedical.modules.inventory.dto.response.DrugCatalogResponse;
import com.aimedical.modules.inventory.dto.response.InventoryStockResponse;
import com.aimedical.modules.inventory.dto.response.StocktakingItemResponse;
import com.aimedical.modules.inventory.dto.response.StocktakingResponse;
import com.aimedical.modules.inventory.dto.response.TransferItemResponse;
import com.aimedical.modules.inventory.dto.response.TransferOrderResponse;
import com.aimedical.modules.inventory.entity.DrugCatalogEntity;
import com.aimedical.modules.inventory.entity.InventoryStockEntity;
import com.aimedical.modules.inventory.entity.StocktakingEntity;
import com.aimedical.modules.inventory.entity.StocktakingItemEntity;
import com.aimedical.modules.inventory.entity.TransferItemEntity;
import com.aimedical.modules.inventory.entity.TransferOrderEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link InventoryConverter} 单元测试。
 */
class InventoryConverterTest {

    private final InventoryConverter converter = new InventoryConverter();

    // ==================== toDrugCatalogResponse ====================

    @Test
    void toDrugCatalogResponseShouldReturnNullForNullEntity() {
        assertNull(converter.toDrugCatalogResponse(null));
    }

    @Test
    void toDrugCatalogResponseShouldMapAllFields() {
        DrugCatalogEntity entity = new DrugCatalogEntity();
        entity.setId(1L);
        entity.setDrugCode("DRG001");
        entity.setDrugName("阿莫西林");
        entity.setGenericName("阿莫西林");
        entity.setSpecification("0.25g*24片");
        entity.setManufacturer("华北制药");
        entity.setDrugForm("片剂");
        entity.setDrugCategory("WESTERN_MEDICINE");
        entity.setUnit("盒");
        entity.setRetailPrice(new BigDecimal("25.50"));
        entity.setPurchasePrice(new BigDecimal("15.00"));
        entity.setOtcFlag(true);
        entity.setEnabled(true);
        entity.setRemark("常用药");
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updated = LocalDateTime.of(2024, 1, 2, 11, 0);
        entity.setCreatedAt(created);
        entity.setUpdatedAt(updated);

        DrugCatalogResponse r = converter.toDrugCatalogResponse(entity);

        assertEquals(1L, r.getId());
        assertEquals("DRG001", r.getDrugCode());
        assertEquals("阿莫西林", r.getDrugName());
        assertEquals("阿莫西林", r.getGenericName());
        assertEquals("0.25g*24片", r.getSpecification());
        assertEquals("华北制药", r.getManufacturer());
        assertEquals("片剂", r.getDrugForm());
        assertEquals("WESTERN_MEDICINE", r.getDrugCategory());
        assertEquals("盒", r.getUnit());
        assertEquals(new BigDecimal("25.50"), r.getRetailPrice());
        assertEquals(new BigDecimal("15.00"), r.getPurchasePrice());
        assertTrue(r.getOtcFlag());
        assertTrue(r.getEnabled());
        assertEquals("常用药", r.getRemark());
        assertEquals(created, r.getCreatedAt());
        assertEquals(updated, r.getUpdatedAt());
    }

    @Test
    void toDrugCatalogResponseShouldHandleNullFields() {
        DrugCatalogEntity entity = new DrugCatalogEntity();
        entity.setId(1L);
        entity.setDrugCode("DRG001");

        DrugCatalogResponse r = converter.toDrugCatalogResponse(entity);

        assertEquals(1L, r.getId());
        assertEquals("DRG001", r.getDrugCode());
        assertNull(r.getDrugName());
        assertNull(r.getGenericName());
        assertNull(r.getSpecification());
        assertNull(r.getManufacturer());
        assertNull(r.getDrugForm());
        assertNull(r.getDrugCategory());
        assertNull(r.getUnit());
        assertNull(r.getRetailPrice());
        assertNull(r.getPurchasePrice());
        assertNull(r.getOtcFlag());
        assertNull(r.getRemark());
        assertNull(r.getCreatedAt());
        assertNull(r.getUpdatedAt());
    }

    // ==================== toStockResponse ====================

    @Test
    void toStockResponseShouldReturnNullForNullEntity() {
        assertNull(converter.toStockResponse(null));
    }

    @Test
    void toStockResponseShouldMapAllFields() {
        InventoryStockEntity entity = new InventoryStockEntity();
        entity.setId(1L);
        entity.setDrugCode("DRG001");
        entity.setBatchNo("BATCH2024");
        entity.setQuantity(new BigDecimal("100"));
        entity.setUnit("盒");
        entity.setPurchasePrice(new BigDecimal("15.00"));
        entity.setRetailPrice(new BigDecimal("25.50"));
        entity.setExpiryDate(LocalDate.of(2025, 12, 31));
        entity.setProductionDate(LocalDate.of(2024, 1, 1));
        entity.setWarehouseLocation("A-01");
        entity.setRemark("库存备注");
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updated = LocalDateTime.of(2024, 1, 2, 11, 0);
        entity.setCreatedAt(created);
        entity.setUpdatedAt(updated);

        InventoryStockResponse r = converter.toStockResponse(entity);

        assertEquals(1L, r.getId());
        assertEquals("DRG001", r.getDrugCode());
        assertEquals("BATCH2024", r.getBatchNo());
        assertEquals(new BigDecimal("100"), r.getQuantity());
        assertEquals("盒", r.getUnit());
        assertEquals(new BigDecimal("15.00"), r.getPurchasePrice());
        assertEquals(new BigDecimal("25.50"), r.getRetailPrice());
        assertEquals(LocalDate.of(2025, 12, 31), r.getExpiryDate());
        assertEquals(LocalDate.of(2024, 1, 1), r.getProductionDate());
        assertEquals("A-01", r.getWarehouseLocation());
        assertEquals("库存备注", r.getRemark());
        assertEquals(created, r.getCreatedAt());
        assertEquals(updated, r.getUpdatedAt());
    }

    @Test
    void toStockResponseShouldHandleNullFields() {
        InventoryStockEntity entity = new InventoryStockEntity();
        entity.setId(2L);
        entity.setDrugCode("DRG002");

        InventoryStockResponse r = converter.toStockResponse(entity);

        assertEquals(2L, r.getId());
        assertEquals("DRG002", r.getDrugCode());
        assertNull(r.getBatchNo());
        assertNull(r.getQuantity());
        assertNull(r.getUnit());
        assertNull(r.getPurchasePrice());
        assertNull(r.getRetailPrice());
        assertNull(r.getExpiryDate());
        assertNull(r.getProductionDate());
        assertNull(r.getWarehouseLocation());
        assertNull(r.getRemark());
    }

    // ==================== toStocktakingItemResponse ====================

    @Test
    void toStocktakingItemResponseShouldReturnNullForNullEntity() {
        assertNull(converter.toStocktakingItemResponse(null));
    }

    @Test
    void toStocktakingItemResponseShouldMapAllFields() {
        StocktakingItemEntity entity = new StocktakingItemEntity();
        entity.setId(1L);
        entity.setStocktakingId(10L);
        entity.setDrugCode("DRG001");
        entity.setDrugName("阿莫西林");
        entity.setBatchNo("BATCH2024");
        entity.setBookQuantity(new BigDecimal("100"));
        entity.setActualQuantity(new BigDecimal("98"));
        entity.setDifference(new BigDecimal("-2"));
        entity.setDifferenceType("LOSS");
        entity.setUnit("盒");
        entity.setRemark("盘点备注");

        StocktakingItemResponse r = converter.toStocktakingItemResponse(entity);

        assertEquals(1L, r.getId());
        assertEquals(10L, r.getStocktakingId());
        assertEquals("DRG001", r.getDrugCode());
        assertEquals("阿莫西林", r.getDrugName());
        assertEquals("BATCH2024", r.getBatchNo());
        assertEquals(new BigDecimal("100"), r.getBookQuantity());
        assertEquals(new BigDecimal("98"), r.getActualQuantity());
        assertEquals(new BigDecimal("-2"), r.getDifference());
        assertEquals("LOSS", r.getDifferenceType());
        assertEquals("盒", r.getUnit());
        assertEquals("盘点备注", r.getRemark());
    }

    @Test
    void toStocktakingItemResponseShouldHandleNullFields() {
        StocktakingItemEntity entity = new StocktakingItemEntity();
        entity.setId(1L);

        StocktakingItemResponse r = converter.toStocktakingItemResponse(entity);

        assertEquals(1L, r.getId());
        assertNull(r.getStocktakingId());
        assertNull(r.getDrugCode());
        assertNull(r.getDrugName());
        assertNull(r.getBatchNo());
        assertNull(r.getBookQuantity());
        assertNull(r.getActualQuantity());
        assertNull(r.getDifference());
        assertNull(r.getDifferenceType());
        assertNull(r.getUnit());
        assertNull(r.getRemark());
    }

    // ==================== toStocktakingResponse ====================

    @Test
    void toStocktakingResponseShouldReturnNullForNullEntity() {
        assertNull(converter.toStocktakingResponse(null, null));
        assertNull(converter.toStocktakingResponse(null, Collections.emptyList()));
    }

    @Test
    void toStocktakingResponseShouldMapAllFieldsAndItems() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStocktakingNo("STK20240101");
        entity.setStocktakingType("FULL");
        entity.setStatus("DRAFT");
        entity.setOperatorId(100L);
        entity.setOperatorName("张三");
        entity.setStartTime(LocalDateTime.of(2024, 1, 1, 9, 0));
        entity.setEndTime(LocalDateTime.of(2024, 1, 1, 17, 0));
        entity.setTotalItems(5);
        entity.setSurplusItems(1);
        entity.setLossItems(2);
        entity.setRemark("盘点备注");
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime updated = LocalDateTime.of(2024, 1, 1, 17, 0);
        entity.setCreatedAt(created);
        entity.setUpdatedAt(updated);

        StocktakingItemEntity item = new StocktakingItemEntity();
        item.setId(11L);
        item.setStocktakingId(1L);
        item.setDrugCode("DRG001");

        StocktakingResponse r = converter.toStocktakingResponse(entity, List.of(item));

        assertEquals(1L, r.getId());
        assertEquals("STK20240101", r.getStocktakingNo());
        assertEquals("FULL", r.getStocktakingType());
        assertEquals("DRAFT", r.getStatus());
        assertEquals(100L, r.getOperatorId());
        assertEquals("张三", r.getOperatorName());
        assertEquals(LocalDateTime.of(2024, 1, 1, 9, 0), r.getStartTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 17, 0), r.getEndTime());
        assertEquals(5, r.getTotalItems());
        assertEquals(1, r.getSurplusItems());
        assertEquals(2, r.getLossItems());
        assertEquals("盘点备注", r.getRemark());
        assertEquals(created, r.getCreatedAt());
        assertEquals(updated, r.getUpdatedAt());
        assertEquals(1, r.getItems().size());
        assertEquals(11L, r.getItems().get(0).getId());
    }

    @Test
    void toStocktakingResponseShouldUseEmptyItemsWhenItemsNull() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);
        entity.setStocktakingNo("STK001");

        StocktakingResponse r = converter.toStocktakingResponse(entity, null);

        assertNotNull(r);
        assertTrue(r.getItems().isEmpty());
    }

    @Test
    void toStocktakingResponseShouldUseEmptyItemsWhenItemsEmpty() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);

        StocktakingResponse r = converter.toStocktakingResponse(entity, Collections.emptyList());

        assertNotNull(r);
        assertTrue(r.getItems().isEmpty());
    }

    @Test
    void toStocktakingResponseShouldKeepNullItemAsNullInList() {
        StocktakingEntity entity = new StocktakingEntity();
        entity.setId(1L);

        StocktakingResponse r = converter.toStocktakingResponse(entity, Collections.singletonList(null));

        assertNotNull(r);
        assertEquals(1, r.getItems().size());
        assertNull(r.getItems().get(0));
    }

    // ==================== toTransferItemResponse ====================

    @Test
    void toTransferItemResponseShouldReturnNullForNullEntity() {
        assertNull(converter.toTransferItemResponse(null));
    }

    @Test
    void toTransferItemResponseShouldMapAllFields() {
        TransferItemEntity entity = new TransferItemEntity();
        entity.setId(1L);
        entity.setTransferId(20L);
        entity.setDrugCode("DRG001");
        entity.setDrugName("阿莫西林");
        entity.setSpecification("0.25g*24片");
        entity.setBatchNo("BATCH2024");
        entity.setQuantity(new BigDecimal("50"));
        entity.setUnit("盒");
        entity.setUnitPrice(new BigDecimal("15.00"));
        entity.setAmount(new BigDecimal("750.00"));
        entity.setRemark("调拨明细备注");

        TransferItemResponse r = converter.toTransferItemResponse(entity);

        assertEquals(1L, r.getId());
        assertEquals(20L, r.getTransferId());
        assertEquals("DRG001", r.getDrugCode());
        assertEquals("阿莫西林", r.getDrugName());
        assertEquals("0.25g*24片", r.getSpecification());
        assertEquals("BATCH2024", r.getBatchNo());
        assertEquals(new BigDecimal("50"), r.getQuantity());
        assertEquals("盒", r.getUnit());
        assertEquals(new BigDecimal("15.00"), r.getUnitPrice());
        assertEquals(new BigDecimal("750.00"), r.getAmount());
        assertEquals("调拨明细备注", r.getRemark());
    }

    @Test
    void toTransferItemResponseShouldHandleNullFields() {
        TransferItemEntity entity = new TransferItemEntity();
        entity.setId(1L);

        TransferItemResponse r = converter.toTransferItemResponse(entity);

        assertEquals(1L, r.getId());
        assertNull(r.getTransferId());
        assertNull(r.getDrugCode());
        assertNull(r.getDrugName());
        assertNull(r.getSpecification());
        assertNull(r.getBatchNo());
        assertNull(r.getQuantity());
        assertNull(r.getUnit());
        assertNull(r.getUnitPrice());
        assertNull(r.getAmount());
        assertNull(r.getRemark());
    }

    // ==================== toTransferResponse ====================

    @Test
    void toTransferResponseShouldReturnNullForNullEntity() {
        assertNull(converter.toTransferResponse(null, null));
        assertNull(converter.toTransferResponse(null, Collections.emptyList()));
    }

    @Test
    void toTransferResponseShouldMapAllFieldsAndItems() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setTransferNo("TRF20240101");
        entity.setTransferType("INVENTORY_TO_PHARMACY");
        entity.setStatus("DRAFT");
        entity.setSourceDept("药库");
        entity.setTargetDept("门诊药房");
        entity.setApplicantId(200L);
        entity.setApplicantName("李四");
        entity.setApproverId(300L);
        entity.setApproverName("王五");
        entity.setApprovedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        entity.setShippedAt(LocalDateTime.of(2024, 1, 1, 11, 0));
        entity.setReceivedAt(LocalDateTime.of(2024, 1, 1, 14, 0));
        entity.setTotalItems(3);
        entity.setTotalAmount(new BigDecimal("2250.00"));
        entity.setRejectReason(null);
        entity.setRemark("调拨备注");
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime updated = LocalDateTime.of(2024, 1, 1, 14, 0);
        entity.setCreatedAt(created);
        entity.setUpdatedAt(updated);

        TransferItemEntity item = new TransferItemEntity();
        item.setId(11L);
        item.setTransferId(1L);
        item.setDrugCode("DRG001");

        TransferOrderResponse r = converter.toTransferResponse(entity, List.of(item));

        assertEquals(1L, r.getId());
        assertEquals("TRF20240101", r.getTransferNo());
        assertEquals("INVENTORY_TO_PHARMACY", r.getTransferType());
        assertEquals("DRAFT", r.getStatus());
        assertEquals("药库", r.getSourceDept());
        assertEquals("门诊药房", r.getTargetDept());
        assertEquals(200L, r.getApplicantId());
        assertEquals("李四", r.getApplicantName());
        assertEquals(300L, r.getApproverId());
        assertEquals("王五", r.getApproverName());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), r.getApprovedAt());
        assertEquals(LocalDateTime.of(2024, 1, 1, 11, 0), r.getShippedAt());
        assertEquals(LocalDateTime.of(2024, 1, 1, 14, 0), r.getReceivedAt());
        assertEquals(3, r.getTotalItems());
        assertEquals(new BigDecimal("2250.00"), r.getTotalAmount());
        assertNull(r.getRejectReason());
        assertEquals("调拨备注", r.getRemark());
        assertEquals(created, r.getCreatedAt());
        assertEquals(updated, r.getUpdatedAt());
        assertEquals(1, r.getItems().size());
        assertEquals(11L, r.getItems().get(0).getId());
    }

    @Test
    void toTransferResponseShouldUseEmptyItemsWhenItemsNull() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);
        entity.setTransferNo("TRF001");

        TransferOrderResponse r = converter.toTransferResponse(entity, null);

        assertNotNull(r);
        assertTrue(r.getItems().isEmpty());
    }

    @Test
    void toTransferResponseShouldUseEmptyItemsWhenItemsEmpty() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);

        TransferOrderResponse r = converter.toTransferResponse(entity, Collections.emptyList());

        assertNotNull(r);
        assertTrue(r.getItems().isEmpty());
    }

    @Test
    void toTransferResponseShouldKeepNullItemAsNullInList() {
        TransferOrderEntity entity = new TransferOrderEntity();
        entity.setId(1L);

        TransferOrderResponse r = converter.toTransferResponse(entity, Collections.singletonList(null));

        assertNotNull(r);
        assertEquals(1, r.getItems().size());
        assertNull(r.getItems().get(0));
    }
}
