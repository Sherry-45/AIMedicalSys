package com.aimedical.modules.doctor.api;

import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.auth.CurrentUser;
import com.aimedical.modules.doctor.dto.DoctorDto;
import com.aimedical.modules.doctor.dto.DoctorProfileUpdateRequest;
import com.aimedical.modules.doctor.service.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DoctorController} 单元测试。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class DoctorControllerTest {

    @Mock
    private DoctorService service;

    @Mock
    private CurrentUser currentUser;

    private DoctorController controller;

    private static final Long DOCTOR_ID = 200L;

    @BeforeEach
    void setUp() {
        controller = new DoctorController(service, currentUser);
    }

    @Test
    void shouldDelegateToServiceAndReturnResult() {
        when(service.getPlaceholder()).thenReturn(Result.success("doctor placeholder"));

        Result<String> result = controller.placeholder();
        assertEquals("SUCCESS", result.getCode());
        assertEquals("doctor placeholder", result.getData());
    }

    @Test
    void getProfile_shouldDelegateToServiceWithCurrentDoctorId() {
        when(currentUser.getUserId()).thenReturn(DOCTOR_ID);
        DoctorDto dto = new DoctorDto();
        dto.setUserId(DOCTOR_ID);
        dto.setRealName("张医生");
        when(service.getProfileByUserId(DOCTOR_ID)).thenReturn(Result.success(dto));

        Result<DoctorDto> result = controller.getProfile();

        assertEquals("SUCCESS", result.getCode());
        assertEquals("张医生", result.getData().getRealName());
        verify(service).getProfileByUserId(DOCTOR_ID);
    }

    @Test
    void updateProfile_shouldDelegateToServiceWithCurrentDoctorId() {
        when(currentUser.getUserId()).thenReturn(DOCTOR_ID);
        DoctorProfileUpdateRequest request = new DoctorProfileUpdateRequest();
        request.setTitle("副主任医师");
        request.setIntroduction("擅长呼吸系统疾病");
        DoctorDto dto = new DoctorDto();
        dto.setUserId(DOCTOR_ID);
        dto.setTitle("副主任医师");
        when(service.updateProfileByUserId(DOCTOR_ID, request)).thenReturn(Result.success(dto));

        Result<DoctorDto> result = controller.updateProfile(request);

        assertEquals("SUCCESS", result.getCode());
        assertEquals("副主任医师", result.getData().getTitle());
        verify(service).updateProfileByUserId(DOCTOR_ID, request);
    }

    @Test
    void getProfile_shouldThrowWhenUserIdIsNull() {
        when(currentUser.getUserId()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> controller.getProfile());
    }

    @Test
    void updateProfile_shouldThrowWhenUserIdIsNull() {
        when(currentUser.getUserId()).thenReturn(null);

        DoctorProfileUpdateRequest request = new DoctorProfileUpdateRequest();
        request.setConsultationFee(new BigDecimal("50.00"));
        assertThrows(IllegalStateException.class, () -> controller.updateProfile(request));
    }
}
