package com.aimedical.modules.examination.api;

import com.aimedical.common.config.GlobalExceptionHandler;
import com.aimedical.common.result.PageResponse;
import com.aimedical.common.util.MessageInterpolator;
import com.aimedical.modules.examination.dto.ExaminationDTO;
import com.aimedical.modules.examination.dto.ExaminationItemDTO;
import com.aimedical.modules.examination.dto.ExecutionOrderDTO;
import com.aimedical.modules.examination.entity.ExaminationStatus;
import com.aimedical.modules.examination.entity.ExaminationType;
import com.aimedical.modules.examination.service.ExaminationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ExaminationController 单元测试。
 * 使用 standalone MockMvc，避免依赖 @SpringBootConfiguration。
 */
@ExtendWith(MockitoExtension.class)
class ExaminationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExaminationService examinationService;

    @Mock
    private MessageInterpolator messageInterpolator;

    @BeforeEach
    void setUp() {
        lenient().when(messageInterpolator.interpolate(any(), any())).thenReturn("mock error");
        ExaminationController controller = new ExaminationController(examinationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(messageInterpolator))
                .build();
    }

    @Test
    void createExamination_shouldDelegateToService() throws Exception {
        when(examinationService.createExamination(any())).thenReturn(buildDto(1L, ExaminationStatus.PENDING));

        mockMvc.perform(post("/api/examinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patientId\":1,\"doctorId\":2,\"examinationType\":\"CT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(examinationService).createExamination(any());
    }

    @Test
    void getExamination_shouldDelegateToService() throws Exception {
        when(examinationService.getExamination(1L)).thenReturn(buildDto(1L, ExaminationStatus.COMPLETED));

        mockMvc.perform(get("/api/examinations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));

        verify(examinationService).getExamination(1L);
    }

    @Test
    void getExaminations_shouldDelegateToService() throws Exception {
        PageResponse<ExaminationDTO> pageResponse = PageResponse.of(
                Collections.singletonList(buildDto(1L, ExaminationStatus.PENDING)), 1L, 0, 20);
        when(examinationService.getExaminations(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/examinations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1));

        verify(examinationService).getExaminations(any());
    }

    @Test
    void scheduleExamination_shouldDelegateToService() throws Exception {
        when(examinationService.scheduleExamination(any(), any(LocalDateTime.class)))
                .thenReturn(buildDto(1L, ExaminationStatus.SCHEDULED));

        mockMvc.perform(put("/api/examinations/1/schedule")
                        .param("scheduledAt", "2026-08-01T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"));

        verify(examinationService).scheduleExamination(any(), any(LocalDateTime.class));
    }

    @Test
    void startExamination_shouldDelegateToService() throws Exception {
        when(examinationService.startExamination(1L))
                .thenReturn(buildDto(1L, ExaminationStatus.IN_PROGRESS));

        mockMvc.perform(put("/api/examinations/1/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        verify(examinationService).startExamination(1L);
    }

    @Test
    void completeExamination_shouldDelegateToService() throws Exception {
        when(examinationService.completeExamination(any(), any()))
                .thenReturn(buildDto(1L, ExaminationStatus.COMPLETED));

        mockMvc.perform(put("/api/examinations/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"impression\":\"im\",\"conclusion\":\"con\",\"items\":[{\"itemName\":\"item\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        verify(examinationService).completeExamination(any(), any());
    }

    @Test
    void cancelExamination_shouldDelegateToService() throws Exception {
        when(examinationService.cancelExamination(1L))
                .thenReturn(buildDto(1L, ExaminationStatus.CANCELLED));

        mockMvc.perform(put("/api/examinations/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        verify(examinationService).cancelExamination(1L);
    }

    @Test
    void generateAiReport_shouldDelegateToService() throws Exception {
        when(examinationService.generateAiReport(1L))
                .thenReturn(buildDto(1L, ExaminationStatus.COMPLETED));

        mockMvc.perform(post("/api/examinations/1/ai-report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));

        verify(examinationService).generateAiReport(1L);
    }

    @Test
    void analyzeImage_shouldDelegateToService() throws Exception {
        when(examinationService.analyzeImage(1L))
                .thenReturn(buildDto(1L, ExaminationStatus.COMPLETED));

        mockMvc.perform(post("/api/examinations/1/image-analysis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));

        verify(examinationService).analyzeImage(1L);
    }

    @Test
    void recommendExecutionOrder_shouldDelegateToService() throws Exception {
        ExecutionOrderDTO dto = new ExecutionOrderDTO();
        ExecutionOrderDTO.OrderItem item = new ExecutionOrderDTO.OrderItem();
        item.setTaskId(2L);
        item.setPriority("P1");
        item.setReason("急诊优先");
        dto.setExecutionOrder(List.of(item));
        dto.setSummary("急诊优先");
        dto.setDisclaimerRequired(true);
        when(examinationService.recommendExecutionOrder(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/examinations/execution-order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.executionOrder[0].taskId").value(2))
                .andExpect(jsonPath("$.data.summary").value("急诊优先"));

        verify(examinationService).recommendExecutionOrder(1L);
    }

    private ExaminationDTO buildDto(Long id, ExaminationStatus status) {
        ExaminationDTO dto = new ExaminationDTO();
        dto.setId(id);
        dto.setPatientId(1L);
        dto.setDoctorId(2L);
        dto.setExaminationType(ExaminationType.CT);
        dto.setStatus(status);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        List<ExaminationItemDTO> items = Collections.emptyList();
        dto.setItems(items);
        return dto;
    }
}
