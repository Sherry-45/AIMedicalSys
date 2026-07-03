package com.aimedical.modules.inventory.service.impl;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.inventory.InventoryErrorCode;
import com.aimedical.modules.inventory.converter.InventoryConverter;
import com.aimedical.modules.inventory.dto.request.DrugCatalogCreateRequest;
import com.aimedical.modules.inventory.dto.request.DrugCatalogQueryRequest;
import com.aimedical.modules.inventory.dto.response.DrugCatalogResponse;
import com.aimedical.modules.inventory.entity.DrugCatalogEntity;
import com.aimedical.modules.inventory.repository.DrugCatalogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DrugCatalogServiceImpl} 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class DrugCatalogServiceImplTest {

    @Mock private DrugCatalogRepository drugCatalogRepository;
    @Mock private InventoryConverter converter;

    private DrugCatalogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DrugCatalogServiceImpl(drugCatalogRepository, converter);
    }

    // ==================== create ====================

    @Test
    void createShouldFailWhenDrugCodeDuplicate() {
        DrugCatalogCreateRequest req = buildCreateRequest("DRG001", "WESTERN_MEDICINE");
        when(drugCatalogRepository.existsByDrugCode("DRG001")).thenReturn(true);

        Result<DrugCatalogResponse> result = service.create(req);

        assertEquals(InventoryErrorCode.DRUG_CODE_DUPLICATE.getCode(), result.getCode());
        assertNull(result.getData());
        verify(drugCatalogRepository, never()).save(any());
    }

    @Test
    void createShouldThrowWhenDrugCategoryInvalid() {
        DrugCatalogCreateRequest req = buildCreateRequest("DRG001", "INVALID_CATEGORY");
        when(drugCatalogRepository.existsByDrugCode("DRG001")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.create(req));
        verify(drugCatalogRepository, never()).save(any());
    }

    @Test
    void createShouldSaveAndReturnResponse() {
        DrugCatalogCreateRequest req = buildCreateRequest("DRG001", "WESTERN_MEDICINE");
        req.setGenericName("阿莫西林");
        req.setSpecification("0.25g*24片");
        req.setManufacturer("华北制药");
        req.setDrugForm("片剂");
        req.setUnit("盒");
        req.setRetailPrice(new BigDecimal("25.50"));
        req.setPurchasePrice(new BigDecimal("15.00"));
        req.setOtcFlag(true);
        req.setRemark("常用药");

        when(drugCatalogRepository.existsByDrugCode("DRG001")).thenReturn(false);
        when(drugCatalogRepository.save(any(DrugCatalogEntity.class))).thenAnswer(inv -> {
            DrugCatalogEntity e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });
        DrugCatalogResponse expected = new DrugCatalogResponse();
        expected.setId(1L);
        when(converter.toDrugCatalogResponse(any(DrugCatalogEntity.class))).thenReturn(expected);

        Result<DrugCatalogResponse> result = service.create(req);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());

        ArgumentCaptor<DrugCatalogEntity> captor = ArgumentCaptor.forClass(DrugCatalogEntity.class);
        verify(drugCatalogRepository).save(captor.capture());
        DrugCatalogEntity saved = captor.getValue();
        assertEquals("DRG001", saved.getDrugCode());
        assertEquals("阿莫西林胶囊", saved.getDrugName());
        assertEquals("WESTERN_MEDICINE", saved.getDrugCategory());
        assertTrue(saved.getEnabled());
        assertEquals(new BigDecimal("25.50"), saved.getRetailPrice());
        assertEquals(new BigDecimal("15.00"), saved.getPurchasePrice());
        assertTrue(saved.getOtcFlag());
    }

    @Test
    void createShouldAcceptNullAndBlankCategory() {
        DrugCatalogCreateRequest req = buildCreateRequest("DRG001", null);
        when(drugCatalogRepository.existsByDrugCode("DRG001")).thenReturn(false);
        when(drugCatalogRepository.save(any(DrugCatalogEntity.class))).thenAnswer(inv -> {
            DrugCatalogEntity e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });
        when(converter.toDrugCatalogResponse(any(DrugCatalogEntity.class))).thenReturn(new DrugCatalogResponse());

        Result<DrugCatalogResponse> result = service.create(req);

        assertEquals("SUCCESS", result.getCode());
    }

    @Test
    void createShouldAcceptBlankCategory() {
        DrugCatalogCreateRequest req = buildCreateRequest("DRG001", "  ");
        when(drugCatalogRepository.existsByDrugCode("DRG001")).thenReturn(false);
        when(drugCatalogRepository.save(any(DrugCatalogEntity.class))).thenAnswer(inv -> {
            DrugCatalogEntity e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });
        when(converter.toDrugCatalogResponse(any(DrugCatalogEntity.class))).thenReturn(new DrugCatalogResponse());

        Result<DrugCatalogResponse> result = service.create(req);

        assertEquals("SUCCESS", result.getCode());
    }

    // ==================== update ====================

    @Test
    void updateShouldFailWhenNotFound() {
        DrugCatalogCreateRequest req = buildCreateRequest("DRG001", "WESTERN_MEDICINE");
        when(drugCatalogRepository.findById(1L)).thenReturn(Optional.empty());

        Result<DrugCatalogResponse> result = service.update(1L, req);

        assertEquals(InventoryErrorCode.DRUG_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
        verify(drugCatalogRepository, never()).save(any());
    }

    @Test
    void updateShouldThrowWhenCategoryInvalid() {
        DrugCatalogCreateRequest req = buildCreateRequest("DRG001", "BAD");
        DrugCatalogEntity entity = new DrugCatalogEntity();
        entity.setId(1L);
        entity.setDrugCode("DRG001");
        when(drugCatalogRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThrows(IllegalArgumentException.class, () -> service.update(1L, req));
        verify(drugCatalogRepository, never()).save(any());
    }

    @Test
    void updateShouldFailWhenDrugCodeChangedAndDuplicate() {
        DrugCatalogCreateRequest req = buildCreateRequest("DRG002", "WESTERN_MEDICINE");
        DrugCatalogEntity entity = new DrugCatalogEntity();
        entity.setId(1L);
        entity.setDrugCode("DRG001");
        when(drugCatalogRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(drugCatalogRepository.existsByDrugCode("DRG002")).thenReturn(true);

        Result<DrugCatalogResponse> result = service.update(1L, req);

        assertEquals(InventoryErrorCode.DRUG_CODE_DUPLICATE.getCode(), result.getCode());
        verify(drugCatalogRepository, never()).save(any());
    }

    @Test
    void updateShouldSaveWhenDrugCodeUnchanged() {
        DrugCatalogCreateRequest req = buildCreateRequest("DRG001", "WESTERN_MEDICINE");
        req.setDrugName("新名称");
        DrugCatalogEntity entity = new DrugCatalogEntity();
        entity.setId(1L);
        entity.setDrugCode("DRG001");
        when(drugCatalogRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(drugCatalogRepository.save(any(DrugCatalogEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        DrugCatalogResponse expected = new DrugCatalogResponse();
        expected.setId(1L);
        when(converter.toDrugCatalogResponse(any(DrugCatalogEntity.class))).thenReturn(expected);

        Result<DrugCatalogResponse> result = service.update(1L, req);

        assertEquals("SUCCESS", result.getCode());
        // 编码未变更时不应校验重复
        verify(drugCatalogRepository, never()).existsByDrugCode(any());
    }

    @Test
    void updateShouldReturnConflictOnOptimisticLock() {
        DrugCatalogCreateRequest req = buildCreateRequest("DRG001", "WESTERN_MEDICINE");
        DrugCatalogEntity entity = new DrugCatalogEntity();
        entity.setId(1L);
        entity.setDrugCode("DRG001");
        when(drugCatalogRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(drugCatalogRepository.save(any(DrugCatalogEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<DrugCatalogResponse> result = service.update(1L, req);

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    // ==================== delete ====================

    @Test
    void deleteShouldFailWhenNotFound() {
        when(drugCatalogRepository.findById(1L)).thenReturn(Optional.empty());

        Result<Void> result = service.delete(1L);

        assertEquals(InventoryErrorCode.DRUG_NOT_FOUND.getCode(), result.getCode());
        verify(drugCatalogRepository, never()).delete(any());
    }

    @Test
    void deleteShouldSoftDeleteWhenFound() {
        DrugCatalogEntity entity = new DrugCatalogEntity();
        entity.setId(1L);
        when(drugCatalogRepository.findById(1L)).thenReturn(Optional.of(entity));

        Result<Void> result = service.delete(1L);

        assertEquals("SUCCESS", result.getCode());
        verify(drugCatalogRepository).delete(entity);
    }

    // ==================== getById ====================

    @Test
    void getByIdShouldFailWhenNotFound() {
        when(drugCatalogRepository.findById(1L)).thenReturn(Optional.empty());

        Result<DrugCatalogResponse> result = service.getById(1L);

        assertEquals(InventoryErrorCode.DRUG_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void getByIdShouldReturnResponseWhenFound() {
        DrugCatalogEntity entity = new DrugCatalogEntity();
        entity.setId(1L);
        when(drugCatalogRepository.findById(1L)).thenReturn(Optional.of(entity));
        DrugCatalogResponse expected = new DrugCatalogResponse();
        expected.setId(1L);
        when(converter.toDrugCatalogResponse(entity)).thenReturn(expected);

        Result<DrugCatalogResponse> result = service.getById(1L);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(1L, result.getData().getId());
    }

    // ==================== getByDrugCode ====================

    @Test
    void getByDrugCodeShouldFailWhenNotFound() {
        when(drugCatalogRepository.findByDrugCode("DRG001")).thenReturn(Optional.empty());

        Result<DrugCatalogResponse> result = service.getByDrugCode("DRG001");

        assertEquals(InventoryErrorCode.DRUG_NOT_FOUND.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void getByDrugCodeShouldReturnResponseWhenFound() {
        DrugCatalogEntity entity = new DrugCatalogEntity();
        entity.setId(1L);
        when(drugCatalogRepository.findByDrugCode("DRG001")).thenReturn(Optional.of(entity));
        DrugCatalogResponse expected = new DrugCatalogResponse();
        expected.setId(1L);
        when(converter.toDrugCatalogResponse(entity)).thenReturn(expected);

        Result<DrugCatalogResponse> result = service.getByDrugCode("DRG001");

        assertEquals("SUCCESS", result.getCode());
        assertEquals(1L, result.getData().getId());
    }

    // ==================== query ====================

    @Test
    void queryShouldUseNameAndCategoryWhenBothProvided() {
        DrugCatalogQueryRequest req = new DrugCatalogQueryRequest();
        req.setDrugName("阿莫");
        req.setDrugCategory("WESTERN_MEDICINE");
        when(drugCatalogRepository.findByDrugNameContainingAndDrugCategory(
                eq("阿莫"), eq("WESTERN_MEDICINE"), any(Pageable.class)))
                .thenReturn(emptyPage());

        Result<Page<DrugCatalogResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        verify(drugCatalogRepository).findByDrugNameContainingAndDrugCategory(
                eq("阿莫"), eq("WESTERN_MEDICINE"), any(Pageable.class));
    }

    @Test
    void queryShouldUseNameOnlyWhenNameProvided() {
        DrugCatalogQueryRequest req = new DrugCatalogQueryRequest();
        req.setDrugName("阿莫");
        when(drugCatalogRepository.findByDrugNameContaining(eq("阿莫"), any(Pageable.class)))
                .thenReturn(emptyPage());

        Result<Page<DrugCatalogResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        verify(drugCatalogRepository).findByDrugNameContaining(eq("阿莫"), any(Pageable.class));
    }

    @Test
    void queryShouldUseCategoryOnlyWhenCategoryProvided() {
        DrugCatalogQueryRequest req = new DrugCatalogQueryRequest();
        req.setDrugCategory("DEVICE");
        when(drugCatalogRepository.findByDrugCategory(eq("DEVICE"), any(Pageable.class)))
                .thenReturn(emptyPage());

        Result<Page<DrugCatalogResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        verify(drugCatalogRepository).findByDrugCategory(eq("DEVICE"), any(Pageable.class));
    }

    @Test
    void queryShouldFindAllWhenNoFilter() {
        DrugCatalogQueryRequest req = new DrugCatalogQueryRequest();
        when(drugCatalogRepository.findAll(any(Pageable.class))).thenReturn(emptyPage());

        Result<Page<DrugCatalogResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        verify(drugCatalogRepository).findAll(any(Pageable.class));
    }

    @Test
    void queryShouldFilterByEnabledInMemory() {
        DrugCatalogEntity e1 = new DrugCatalogEntity();
        e1.setId(1L);
        e1.setEnabled(true);
        DrugCatalogEntity e2 = new DrugCatalogEntity();
        e2.setId(2L);
        e2.setEnabled(false);

        Page<DrugCatalogEntity> page = new PageImpl<>(List.of(e1, e2));
        when(drugCatalogRepository.findAll(any(Pageable.class))).thenReturn(page);

        DrugCatalogResponse r1 = new DrugCatalogResponse();
        r1.setId(1L);
        r1.setEnabled(true);
        DrugCatalogResponse r2 = new DrugCatalogResponse();
        r2.setId(2L);
        r2.setEnabled(false);
        when(converter.toDrugCatalogResponse(e1)).thenReturn(r1);
        when(converter.toDrugCatalogResponse(e2)).thenReturn(r2);

        DrugCatalogQueryRequest req = new DrugCatalogQueryRequest();
        req.setEnabled(true);

        Result<Page<DrugCatalogResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(1, result.getData().getContent().size());
        assertEquals(1L, result.getData().getContent().get(0).getId());
    }

    @Test
    void queryShouldFilterByEnabledFalseInMemory() {
        DrugCatalogEntity e1 = new DrugCatalogEntity();
        e1.setId(1L);
        e1.setEnabled(true);
        DrugCatalogEntity e2 = new DrugCatalogEntity();
        e2.setId(2L);
        e2.setEnabled(false);

        Page<DrugCatalogEntity> page = new PageImpl<>(List.of(e1, e2));
        when(drugCatalogRepository.findAll(any(Pageable.class))).thenReturn(page);

        DrugCatalogResponse r1 = new DrugCatalogResponse();
        r1.setId(1L);
        r1.setEnabled(true);
        DrugCatalogResponse r2 = new DrugCatalogResponse();
        r2.setId(2L);
        r2.setEnabled(false);
        when(converter.toDrugCatalogResponse(e1)).thenReturn(r1);
        when(converter.toDrugCatalogResponse(e2)).thenReturn(r2);

        DrugCatalogQueryRequest req = new DrugCatalogQueryRequest();
        req.setEnabled(false);

        Result<Page<DrugCatalogResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(1, result.getData().getContent().size());
        assertEquals(2L, result.getData().getContent().get(0).getId());
    }

    @Test
    void queryShouldUseDefaultPageAndSizeWhenNull() {
        DrugCatalogQueryRequest req = new DrugCatalogQueryRequest();
        req.setPage(null);
        req.setSize(null);
        when(drugCatalogRepository.findAll(any(Pageable.class))).thenReturn(emptyPage());

        Result<Page<DrugCatalogResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(drugCatalogRepository).findAll(captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(20, captor.getValue().getPageSize());
    }

    @Test
    void queryShouldHandleBlankNameAndCategoryAsAbsent() {
        DrugCatalogQueryRequest req = new DrugCatalogQueryRequest();
        req.setDrugName("  ");
        req.setDrugCategory("  ");
        when(drugCatalogRepository.findAll(any(Pageable.class))).thenReturn(emptyPage());

        Result<Page<DrugCatalogResponse>> result = service.query(req);

        assertEquals("SUCCESS", result.getCode());
        verify(drugCatalogRepository).findAll(any(Pageable.class));
    }

    // ==================== toggleEnabled ====================

    @Test
    void toggleEnabledShouldFailWhenNotFound() {
        when(drugCatalogRepository.findById(1L)).thenReturn(Optional.empty());

        Result<DrugCatalogResponse> result = service.toggleEnabled(1L);

        assertEquals(InventoryErrorCode.DRUG_NOT_FOUND.getCode(), result.getCode());
        verify(drugCatalogRepository, never()).save(any());
    }

    @Test
    void toggleEnabledShouldFlipTrueToFalse() {
        DrugCatalogEntity entity = new DrugCatalogEntity();
        entity.setId(1L);
        entity.setEnabled(true);
        when(drugCatalogRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(drugCatalogRepository.save(any(DrugCatalogEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        DrugCatalogResponse expected = new DrugCatalogResponse();
        expected.setEnabled(false);
        when(converter.toDrugCatalogResponse(any(DrugCatalogEntity.class))).thenReturn(expected);

        Result<DrugCatalogResponse> result = service.toggleEnabled(1L);

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<DrugCatalogEntity> captor = ArgumentCaptor.forClass(DrugCatalogEntity.class);
        verify(drugCatalogRepository).save(captor.capture());
        assertFalse(captor.getValue().getEnabled());
    }

    @Test
    void toggleEnabledShouldFlipFalseToTrue() {
        DrugCatalogEntity entity = new DrugCatalogEntity();
        entity.setId(1L);
        entity.setEnabled(false);
        when(drugCatalogRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(drugCatalogRepository.save(any(DrugCatalogEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(converter.toDrugCatalogResponse(any(DrugCatalogEntity.class))).thenReturn(new DrugCatalogResponse());

        Result<DrugCatalogResponse> result = service.toggleEnabled(1L);

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<DrugCatalogEntity> captor = ArgumentCaptor.forClass(DrugCatalogEntity.class);
        verify(drugCatalogRepository).save(captor.capture());
        assertTrue(captor.getValue().getEnabled());
    }

    @Test
    void toggleEnabledShouldFlipNullToTrue() {
        DrugCatalogEntity entity = new DrugCatalogEntity();
        entity.setId(1L);
        entity.setEnabled(null);
        when(drugCatalogRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(drugCatalogRepository.save(any(DrugCatalogEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(converter.toDrugCatalogResponse(any(DrugCatalogEntity.class))).thenReturn(new DrugCatalogResponse());

        Result<DrugCatalogResponse> result = service.toggleEnabled(1L);

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<DrugCatalogEntity> captor = ArgumentCaptor.forClass(DrugCatalogEntity.class);
        verify(drugCatalogRepository).save(captor.capture());
        assertTrue(captor.getValue().getEnabled());
    }

    @Test
    void toggleEnabledShouldReturnConflictOnOptimisticLock() {
        DrugCatalogEntity entity = new DrugCatalogEntity();
        entity.setId(1L);
        entity.setEnabled(true);
        when(drugCatalogRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(drugCatalogRepository.save(any(DrugCatalogEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        Result<DrugCatalogResponse> result = service.toggleEnabled(1L);

        assertEquals(GlobalErrorCode.CONFLICT.getCode(), result.getCode());
        assertNull(result.getData());
    }

    // ==================== helpers ====================

    private DrugCatalogCreateRequest buildCreateRequest(String drugCode, String category) {
        DrugCatalogCreateRequest req = new DrugCatalogCreateRequest();
        req.setDrugCode(drugCode);
        req.setDrugName("阿莫西林胶囊");
        req.setDrugCategory(category);
        return req;
    }

    private Page<DrugCatalogEntity> emptyPage() {
        return new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 20), 0);
    }

    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
