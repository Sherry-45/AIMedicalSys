package com.aimedical.modules.doctor.service.impl;

import com.aimedical.common.exception.BusinessException;
import com.aimedical.common.result.Result;
import com.aimedical.modules.doctor.dto.DoctorDto;
import com.aimedical.modules.doctor.dto.DoctorProfileUpdateRequest;
import com.aimedical.modules.doctor.entity.DoctorEntity;
import com.aimedical.modules.doctor.repository.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DoctorServiceImpl} 单元测试。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository repository;

    private DoctorServiceImpl service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new DoctorServiceImpl(repository);
    }

    @Test
    void shouldReturnSuccessCode() {
        Result<String> result = service.getPlaceholder();
        assertEquals("SUCCESS", result.getCode());
    }

    @Test
    void shouldReturnDoctorPlaceholderData() {
        Result<String> result = service.getPlaceholder();
        assertEquals("doctor placeholder", result.getData());
    }

    @Test
    void shouldReturnSuccessMessage() {
        Result<String> result = service.getPlaceholder();
        assertEquals("成功", result.getMessage());
    }

    // ---------- getProfileByUserId ----------

    @Test
    void getProfileByUserId_shouldReturnDtoWhenFound() {
        DoctorEntity entity = buildEntity(1L, 200L, "张医生");
        when(repository.findByUserId(200L)).thenReturn(Optional.of(entity));

        Result<DoctorDto> result = service.getProfileByUserId(200L);

        assertEquals("SUCCESS", result.getCode());
        DoctorDto dto = result.getData();
        assertEquals(1L, dto.getId());
        assertEquals(200L, dto.getUserId());
        assertEquals("张医生", dto.getRealName());
        assertEquals("主任医师", dto.getTitle());
    }

    @Test
    void getProfileByUserId_shouldThrowWhenNotFound() {
        when(repository.findByUserId(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.getProfileByUserId(999L));
        assertEquals("NOT_FOUND", ex.getErrorCode().getCode());
    }

    @Test
    void getProfileByUserId_shouldThrowWhenUserIdIsNull() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.getProfileByUserId(null));
        assertEquals("UNAUTHORIZED", ex.getErrorCode().getCode());
    }

    // ---------- updateProfileByUserId ----------

    @Test
    void updateProfileByUserId_shouldUpdateOnlyProvidedFields() {
        DoctorEntity entity = buildEntity(1L, 200L, "张医生");
        when(repository.findByUserId(200L)).thenReturn(Optional.of(entity));
        when(repository.save(any(DoctorEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        DoctorProfileUpdateRequest request = new DoctorProfileUpdateRequest();
        request.setTitle("副主任医师");
        request.setIntroduction("擅长呼吸系统疾病");
        request.setConsultationFee(new BigDecimal("80.00"));

        Result<DoctorDto> result = service.updateProfileByUserId(200L, request);

        assertEquals("SUCCESS", result.getCode());
        ArgumentCaptor<DoctorEntity> captor = ArgumentCaptor.forClass(DoctorEntity.class);
        verify(repository).save(captor.capture());
        DoctorEntity saved = captor.getValue();
        // 提供的字段已更新
        assertEquals("副主任医师", saved.getTitle());
        assertEquals("擅长呼吸系统疾病", saved.getIntroduction());
        assertEquals(new BigDecimal("80.00"), saved.getConsultationFee());
        // 未提供的字段保留原值
        assertEquals("张医生", saved.getRealName());
        assertEquals("内科", saved.getDepartment());
        // 响应 DTO 字段正确
        assertEquals("副主任医师", result.getData().getTitle());
    }

    @Test
    void updateProfileByUserId_shouldThrowWhenNotFound() {
        when(repository.findByUserId(999L)).thenReturn(Optional.empty());

        DoctorProfileUpdateRequest request = new DoctorProfileUpdateRequest();
        request.setTitle("副主任医师");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateProfileByUserId(999L, request));
        assertEquals("NOT_FOUND", ex.getErrorCode().getCode());
    }

    @Test
    void updateProfileByUserId_shouldThrowWhenUserIdIsNull() {
        DoctorProfileUpdateRequest request = new DoctorProfileUpdateRequest();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateProfileByUserId(null, request));
        assertEquals("UNAUTHORIZED", ex.getErrorCode().getCode());
    }

    // ---------- 辅助方法 ----------

    private static DoctorEntity buildEntity(Long id, Long userId, String realName) {
        DoctorEntity entity = new DoctorEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setRealName(realName);
        entity.setGender("M");
        entity.setTitle("主任医师");
        entity.setDepartment("内科");
        entity.setSpecialty("呼吸内科");
        entity.setIntroduction("从业多年");
        entity.setLicenseNo("LICENSE-001");
        entity.setPracticeYears(15);
        entity.setConsultationFee(new BigDecimal("50.00"));
        entity.setRemark("");
        return entity;
    }
}
