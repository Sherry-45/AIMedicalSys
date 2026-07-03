package com.aimedical.modules.pharmacy.converter;

import com.aimedical.modules.pharmacy.dto.DispensingItemResponse;
import com.aimedical.modules.pharmacy.dto.DispensingResponse;
import com.aimedical.modules.pharmacy.dto.PharmacyRefundItemResponse;
import com.aimedical.modules.pharmacy.dto.PharmacyRefundResponse;
import com.aimedical.modules.pharmacy.dto.PharmacyStockResponse;
import com.aimedical.modules.pharmacy.entity.DispensingItemEntity;
import com.aimedical.modules.pharmacy.entity.DispensingRecordEntity;
import com.aimedical.modules.pharmacy.entity.PharmacyRefundItemEntity;
import com.aimedical.modules.pharmacy.entity.PharmacyRefundRecordEntity;
import com.aimedical.modules.pharmacy.entity.PharmacyStockEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PharmacyConverterTest {

    private PharmacyConverter converter;

    @BeforeEach
    void setUp() {
        converter = new PharmacyConverter();
    }

    // ===== 库存 =====

    @Test
    void toStockResponseShouldReturnNullForNullEntity() {
        assertNull(converter.toResponse((PharmacyStockEntity) null));
    }

    @Test
    void toStockResponseShouldMapAllFields() {
        PharmacyStockEntity entity = new PharmacyStockEntity();
        entity.setId(1L);
        entity.setDrugCode("DRG001");
        entity.setDrugName("阿莫西林");
        entity.setBatchNo("B2024");
        entity.setQuantity(new BigDecimal("100"));
        entity.setUnit("盒");
        entity.setRetailPrice(new BigDecimal("12.50"));
        entity.setExpiryDate(LocalDate.of(2026, 1, 1));
        entity.setShelfLocation("A-01");
        entity.setSafetyStock(new BigDecimal("20"));
        entity.setRemark("备注");
        LocalDateTime createdAt = LocalDateTime.of(2024, 6, 1, 9, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 6, 2, 10, 0);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        PharmacyStockResponse r = converter.toResponse(entity);

        assertEquals(1L, r.getId());
        assertEquals("DRG001", r.getDrugCode());
        assertEquals("阿莫西林", r.getDrugName());
        assertEquals("B2024", r.getBatchNo());
        assertEquals(new BigDecimal("100"), r.getQuantity());
        assertEquals("盒", r.getUnit());
        assertEquals(new BigDecimal("12.50"), r.getRetailPrice());
        assertEquals(LocalDate.of(2026, 1, 1), r.getExpiryDate());
        assertEquals("A-01", r.getShelfLocation());
        assertEquals(new BigDecimal("20"), r.getSafetyStock());
        assertEquals("备注", r.getRemark());
        assertEquals(createdAt, r.getCreatedAt());
        assertEquals(updatedAt, r.getUpdatedAt());
    }

    @Test
    void toStockResponseListShouldReturnEmptyForNull() {
        assertTrue(converter.toStockResponseList(null).isEmpty());
    }

    @Test
    void toStockResponseListShouldMapEachEntity() {
        PharmacyStockEntity e1 = new PharmacyStockEntity();
        e1.setId(1L);
        e1.setDrugCode("DRG001");
        PharmacyStockEntity e2 = new PharmacyStockEntity();
        e2.setId(2L);
        e2.setDrugCode("DRG002");

        List<PharmacyStockResponse> list = converter.toStockResponseList(List.of(e1, e2));

        assertEquals(2, list.size());
        assertEquals(1L, list.get(0).getId());
        assertEquals("DRG001", list.get(0).getDrugCode());
        assertEquals(2L, list.get(1).getId());
        assertEquals("DRG002", list.get(1).getDrugCode());
    }

    @Test
    void toStockResponseListShouldReturnEmptyForEmptyList() {
        assertTrue(converter.toStockResponseList(Collections.emptyList()).isEmpty());
    }

    // ===== 发药记录 =====

    @Test
    void toDispensingResponseShouldReturnNullForNullEntity() {
        assertNull(converter.toResponse((DispensingRecordEntity) null));
    }

    @Test
    void toDispensingResponseShouldMapAllFields() {
        DispensingRecordEntity entity = new DispensingRecordEntity();
        entity.setId(1L);
        entity.setDispensingNo("DISP001");
        entity.setPrescriptionId(10L);
        entity.setMedicalOrderId(20L);
        entity.setPatientId(30L);
        entity.setPatientName("张三");
        entity.setPharmacistId(40L);
        entity.setPharmacistName("李药师");
        entity.setStatus("PENDING");
        entity.setTotalQuantity(new BigDecimal("5"));
        entity.setTotalAmount(new BigDecimal("62.50"));
        entity.setDispensedAt(LocalDateTime.of(2024, 6, 1, 9, 0));
        entity.setRemark("备注");
        LocalDateTime createdAt = LocalDateTime.of(2024, 6, 1, 8, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 6, 1, 9, 0);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        DispensingResponse r = converter.toResponse(entity);

        assertEquals(1L, r.getId());
        assertEquals("DISP001", r.getDispensingNo());
        assertEquals(10L, r.getPrescriptionId());
        assertEquals(20L, r.getMedicalOrderId());
        assertEquals(30L, r.getPatientId());
        assertEquals("张三", r.getPatientName());
        assertEquals(40L, r.getPharmacistId());
        assertEquals("李药师", r.getPharmacistName());
        assertEquals("PENDING", r.getStatus());
        assertEquals(new BigDecimal("5"), r.getTotalQuantity());
        assertEquals(new BigDecimal("62.50"), r.getTotalAmount());
        assertEquals(LocalDateTime.of(2024, 6, 1, 9, 0), r.getDispensedAt());
        assertEquals("备注", r.getRemark());
        assertEquals(createdAt, r.getCreatedAt());
        assertEquals(updatedAt, r.getUpdatedAt());
        assertNull(r.getItems());
    }

    // ===== 发药明细 =====

    @Test
    void toDispensingItemResponseShouldReturnNullForNullEntity() {
        assertNull(converter.toResponse((DispensingItemEntity) null));
    }

    @Test
    void toDispensingItemResponseShouldMapAllFields() {
        DispensingItemEntity entity = new DispensingItemEntity();
        entity.setId(1L);
        entity.setDispensingId(100L);
        entity.setDrugCode("DRG001");
        entity.setDrugName("阿莫西林");
        entity.setSpecification("0.25g*24片");
        entity.setBatchNo("B2024");
        entity.setQuantity(new BigDecimal("5"));
        entity.setUnit("盒");
        entity.setUnitPrice(new BigDecimal("12.50"));
        entity.setAmount(new BigDecimal("62.50"));
        entity.setDosage("每次1片");
        entity.setUsageMethod("口服");
        entity.setFrequency("一日三次");
        entity.setDays(7);
        entity.setRemark("饭后服用");

        DispensingItemResponse r = converter.toResponse(entity);

        assertEquals(1L, r.getId());
        assertEquals(100L, r.getDispensingId());
        assertEquals("DRG001", r.getDrugCode());
        assertEquals("阿莫西林", r.getDrugName());
        assertEquals("0.25g*24片", r.getSpecification());
        assertEquals("B2024", r.getBatchNo());
        assertEquals(new BigDecimal("5"), r.getQuantity());
        assertEquals("盒", r.getUnit());
        assertEquals(new BigDecimal("12.50"), r.getUnitPrice());
        assertEquals(new BigDecimal("62.50"), r.getAmount());
        assertEquals("每次1片", r.getDosage());
        assertEquals("口服", r.getUsageMethod());
        assertEquals("一日三次", r.getFrequency());
        assertEquals(7, r.getDays());
        assertEquals("饭后服用", r.getRemark());
    }

    @Test
    void toDispensingItemResponseListShouldReturnEmptyForNull() {
        assertTrue(converter.toDispensingItemResponseList(null).isEmpty());
    }

    @Test
    void toDispensingItemResponseListShouldMapEachEntity() {
        DispensingItemEntity e1 = new DispensingItemEntity();
        e1.setId(1L);
        e1.setDrugCode("DRG001");
        DispensingItemEntity e2 = new DispensingItemEntity();
        e2.setId(2L);
        e2.setDrugCode("DRG002");

        List<DispensingItemResponse> list = converter.toDispensingItemResponseList(List.of(e1, e2));

        assertEquals(2, list.size());
        assertEquals("DRG001", list.get(0).getDrugCode());
        assertEquals("DRG002", list.get(1).getDrugCode());
    }

    @Test
    void toDispensingResponseWithItemsShouldAssembleItems() {
        DispensingRecordEntity entity = new DispensingRecordEntity();
        entity.setId(1L);
        entity.setDispensingNo("DISP001");

        DispensingItemEntity item = new DispensingItemEntity();
        item.setId(10L);
        item.setDispensingId(1L);
        item.setDrugCode("DRG001");

        DispensingResponse r = converter.toResponse(entity, List.of(item));

        assertNotNull(r);
        assertEquals(1L, r.getId());
        assertEquals("DISP001", r.getDispensingNo());
        assertNotNull(r.getItems());
        assertEquals(1, r.getItems().size());
        assertEquals("DRG001", r.getItems().get(0).getDrugCode());
    }

    @Test
    void toDispensingResponseWithItemsShouldReturnNullWhenEntityNull() {
        DispensingResponse r = converter.toResponse(null, List.of(new DispensingItemEntity()));
        assertNull(r);
    }

    @Test
    void toDispensingResponseWithItemsShouldHandleNullItems() {
        DispensingRecordEntity entity = new DispensingRecordEntity();
        entity.setId(1L);
        DispensingResponse r = converter.toResponse(entity, null);
        assertNotNull(r);
        assertNotNull(r.getItems());
        assertTrue(r.getItems().isEmpty());
    }

    // ===== 退药记录 =====

    @Test
    void toRefundResponseShouldReturnNullForNullEntity() {
        assertNull(converter.toResponse((PharmacyRefundRecordEntity) null));
    }

    @Test
    void toRefundResponseShouldMapAllFields() {
        PharmacyRefundRecordEntity entity = new PharmacyRefundRecordEntity();
        entity.setId(1L);
        entity.setRefundNo("RFD001");
        entity.setDispensingId(10L);
        entity.setPatientId(20L);
        entity.setPatientName("张三");
        entity.setPharmacistId(30L);
        entity.setPharmacistName("李药师");
        entity.setStatus("PENDING");
        entity.setRefundReason("药品过敏");
        entity.setTotalQuantity(new BigDecimal("2"));
        entity.setTotalAmount(new BigDecimal("25.00"));
        entity.setRefundedAt(LocalDateTime.of(2024, 6, 2, 10, 0));
        entity.setRemark("备注");
        LocalDateTime createdAt = LocalDateTime.of(2024, 6, 2, 9, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 6, 2, 10, 0);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        PharmacyRefundResponse r = converter.toResponse(entity);

        assertEquals(1L, r.getId());
        assertEquals("RFD001", r.getRefundNo());
        assertEquals(10L, r.getDispensingId());
        assertEquals(20L, r.getPatientId());
        assertEquals("张三", r.getPatientName());
        assertEquals(30L, r.getPharmacistId());
        assertEquals("李药师", r.getPharmacistName());
        assertEquals("PENDING", r.getStatus());
        assertEquals("药品过敏", r.getRefundReason());
        assertEquals(new BigDecimal("2"), r.getTotalQuantity());
        assertEquals(new BigDecimal("25.00"), r.getTotalAmount());
        assertEquals(LocalDateTime.of(2024, 6, 2, 10, 0), r.getRefundedAt());
        assertEquals("备注", r.getRemark());
        assertEquals(createdAt, r.getCreatedAt());
        assertEquals(updatedAt, r.getUpdatedAt());
        assertNull(r.getItems());
    }

    // ===== 退药明细 =====

    @Test
    void toRefundItemResponseShouldReturnNullForNullEntity() {
        assertNull(converter.toResponse((PharmacyRefundItemEntity) null));
    }

    @Test
    void toRefundItemResponseShouldMapAllFields() {
        PharmacyRefundItemEntity entity = new PharmacyRefundItemEntity();
        entity.setId(1L);
        entity.setRefundId(100L);
        entity.setDispensingItemId(200L);
        entity.setDrugCode("DRG001");
        entity.setDrugName("阿莫西林");
        entity.setBatchNo("B2024");
        entity.setQuantity(new BigDecimal("2"));
        entity.setUnit("盒");
        entity.setUnitPrice(new BigDecimal("12.50"));
        entity.setAmount(new BigDecimal("25.00"));
        entity.setRemark("退药明细备注");

        PharmacyRefundItemResponse r = converter.toResponse(entity);

        assertEquals(1L, r.getId());
        assertEquals(100L, r.getRefundId());
        assertEquals(200L, r.getDispensingItemId());
        assertEquals("DRG001", r.getDrugCode());
        assertEquals("阿莫西林", r.getDrugName());
        assertEquals("B2024", r.getBatchNo());
        assertEquals(new BigDecimal("2"), r.getQuantity());
        assertEquals("盒", r.getUnit());
        assertEquals(new BigDecimal("12.50"), r.getUnitPrice());
        assertEquals(new BigDecimal("25.00"), r.getAmount());
        assertEquals("退药明细备注", r.getRemark());
    }

    @Test
    void toRefundItemResponseListShouldReturnEmptyForNull() {
        assertTrue(converter.toRefundItemResponseList(null).isEmpty());
    }

    @Test
    void toRefundItemResponseListShouldMapEachEntity() {
        PharmacyRefundItemEntity e1 = new PharmacyRefundItemEntity();
        e1.setId(1L);
        e1.setDrugCode("DRG001");
        PharmacyRefundItemEntity e2 = new PharmacyRefundItemEntity();
        e2.setId(2L);
        e2.setDrugCode("DRG002");

        List<PharmacyRefundItemResponse> list = converter.toRefundItemResponseList(List.of(e1, e2));

        assertEquals(2, list.size());
        assertEquals("DRG001", list.get(0).getDrugCode());
        assertEquals("DRG002", list.get(1).getDrugCode());
    }

    @Test
    void toRefundResponseWithItemsShouldAssembleItems() {
        PharmacyRefundRecordEntity entity = new PharmacyRefundRecordEntity();
        entity.setId(1L);
        entity.setRefundNo("RFD001");

        PharmacyRefundItemEntity item = new PharmacyRefundItemEntity();
        item.setId(10L);
        item.setRefundId(1L);
        item.setDrugCode("DRG001");

        PharmacyRefundResponse r = converter.toResponse(entity, List.of(item));

        assertNotNull(r);
        assertEquals(1L, r.getId());
        assertEquals("RFD001", r.getRefundNo());
        assertNotNull(r.getItems());
        assertEquals(1, r.getItems().size());
        assertEquals("DRG001", r.getItems().get(0).getDrugCode());
    }

    @Test
    void toRefundResponseWithItemsShouldReturnNullWhenEntityNull() {
        PharmacyRefundResponse r = converter.toResponse(null, List.of(new PharmacyRefundItemEntity()));
        assertNull(r);
    }

    @Test
    void toRefundResponseWithItemsShouldHandleNullItems() {
        PharmacyRefundRecordEntity entity = new PharmacyRefundRecordEntity();
        entity.setId(1L);
        PharmacyRefundResponse r = converter.toResponse(entity, null);
        assertNotNull(r);
        assertNotNull(r.getItems());
        assertTrue(r.getItems().isEmpty());
    }
}
