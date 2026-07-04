package com.aimedical.modules.labtest.api;

import com.aimedical.common.result.PageResponse;
import com.aimedical.common.result.Result;
import com.aimedical.modules.labtest.dto.LabTestCompleteRequest;
import com.aimedical.modules.labtest.dto.LabTestCreateRequest;
import com.aimedical.modules.labtest.dto.LabTestDTO;
import com.aimedical.modules.labtest.dto.LabTestExecutionOrderDTO;
import com.aimedical.modules.labtest.dto.LabTestQueryRequest;
import com.aimedical.modules.labtest.dto.LabTestTrendDTO;
import com.aimedical.modules.labtest.entity.AbnormalFlag;
import com.aimedical.modules.labtest.entity.LabTestStatus;
import com.aimedical.modules.labtest.entity.SampleType;
import com.aimedical.modules.labtest.service.LabTestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * LabTestController 单元测试。
 * 通过 mock LabTestService 验证各端点正确委派并返回 Result.success。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LabTestController 端点委派")
class LabTestControllerTest {

    @Mock
    private LabTestService labTestService;

    @InjectMocks
    private LabTestController controller;

    @Test
    @DisplayName("POST / 创建检验委派 service.createLabTest")
    void createLabTest() {
        LabTestCreateRequest request = new LabTestCreateRequest();
        request.setPatientId(1L);
        request.setDoctorId(2L);
        request.setTestType("血常规");
        request.setSampleType(SampleType.BLOOD);
        LabTestDTO dto = new LabTestDTO();
        dto.setId(1L);
        when(labTestService.createLabTest(any())).thenReturn(dto);

        Result<LabTestDTO> result = controller.createLabTest(request);

        assertEquals("SUCCESS", result.getCode());
        assertSame(dto, result.getData());
        verify(labTestService).createLabTest(request);
    }

    @Test
    @DisplayName("GET /{id} 委派 service.getLabTest")
    void getLabTest() {
        LabTestDTO dto = new LabTestDTO();
        dto.setId(2L);
        when(labTestService.getLabTest(2L)).thenReturn(dto);

        Result<LabTestDTO> result = controller.getLabTest(2L);

        assertEquals("SUCCESS", result.getCode());
        assertSame(dto, result.getData());
        verify(labTestService).getLabTest(2L);
    }

    @Test
    @DisplayName("GET / 委派 service.getLabTests")
    void getLabTests() {
        LabTestQueryRequest query = new LabTestQueryRequest();
        query.setPatientId(1L);
        PageResponse<LabTestDTO> page = PageResponse.of(Collections.emptyList(), 0, 0, 20);
        when(labTestService.getLabTests(any())).thenReturn(page);

        Result<PageResponse<LabTestDTO>> result = controller.getLabTests(query);

        assertEquals("SUCCESS", result.getCode());
        assertSame(page, result.getData());
        verify(labTestService).getLabTests(query);
    }

    @Test
    @DisplayName("PUT /{id}/collect 委派 service.collectSample")
    void collectSample() {
        LabTestDTO dto = new LabTestDTO();
        dto.setId(3L);
        dto.setStatus(LabTestStatus.COLLECTED);
        when(labTestService.collectSample(3L)).thenReturn(dto);

        Result<LabTestDTO> result = controller.collectSample(3L);

        assertEquals("SUCCESS", result.getCode());
        assertSame(dto, result.getData());
        verify(labTestService).collectSample(3L);
    }

    @Test
    @DisplayName("PUT /{id}/start 委派 service.startLabTest")
    void startLabTest() {
        LabTestDTO dto = new LabTestDTO();
        dto.setId(4L);
        when(labTestService.startLabTest(4L)).thenReturn(dto);

        Result<LabTestDTO> result = controller.startLabTest(4L);

        assertEquals("SUCCESS", result.getCode());
        assertSame(dto, result.getData());
        verify(labTestService).startLabTest(4L);
    }

    @Test
    @DisplayName("PUT /{id}/complete 委派 service.completeLabTest")
    void completeLabTest() {
        com.aimedical.modules.labtest.dto.LabTestItemRequest item = new com.aimedical.modules.labtest.dto.LabTestItemRequest();
        item.setItemName("白细胞计数");
        item.setResult("6.5");
        item.setUnit("10^9/L");
        item.setReferenceRange("4-10");
        item.setAbnormalFlag(AbnormalFlag.NORMAL);
        LabTestCompleteRequest request = new LabTestCompleteRequest();
        request.setReportConclusion("正常");
        request.setItems(List.of(item));
        LabTestDTO dto = new LabTestDTO();
        dto.setId(5L);
        dto.setStatus(LabTestStatus.COMPLETED);
        when(labTestService.completeLabTest(eq(5L), any())).thenReturn(dto);

        Result<LabTestDTO> result = controller.completeLabTest(5L, request);

        assertEquals("SUCCESS", result.getCode());
        assertSame(dto, result.getData());
        verify(labTestService).completeLabTest(5L, request);
    }

    @Test
    @DisplayName("PUT /{id}/cancel 委派 service.cancelLabTest")
    void cancelLabTest() {
        LabTestDTO dto = new LabTestDTO();
        dto.setId(6L);
        dto.setStatus(LabTestStatus.CANCELLED);
        when(labTestService.cancelLabTest(6L)).thenReturn(dto);

        Result<LabTestDTO> result = controller.cancelLabTest(6L);

        assertEquals("SUCCESS", result.getCode());
        assertSame(dto, result.getData());
        verify(labTestService).cancelLabTest(6L);
    }

    @Test
    @DisplayName("POST /{id}/ai-report 委派 service.generateAiReport")
    void generateAiReport() {
        LabTestDTO dto = new LabTestDTO();
        dto.setId(7L);
        dto.setAiInterpretation("AI解读报告已生成");
        when(labTestService.generateAiReport(7L)).thenReturn(dto);

        Result<LabTestDTO> result = controller.generateAiReport(7L);

        assertEquals("SUCCESS", result.getCode());
        assertSame(dto, result.getData());
        verify(labTestService).generateAiReport(7L);
    }

    @Test
    @DisplayName("GET /trend/{patientId} 委派 service.getTrend")
    void getTrend() {
        LabTestTrendDTO dto = new LabTestTrendDTO();
        dto.setItemName("白细胞计数");
        dto.setUnit("10^9/L");
        dto.setPoints(Collections.emptyList());
        when(labTestService.getTrend(eq(8L), eq("白细胞计数"))).thenReturn(dto);

        Result<LabTestTrendDTO> result = controller.getTrend(8L, "白细胞计数");

        assertEquals("SUCCESS", result.getCode());
        assertSame(dto, result.getData());
        verify(labTestService).getTrend(8L, "白细胞计数");
    }

    @Test
    @DisplayName("GET /execution-order/{patientId} 委派 service.recommendExecutionOrder")
    void recommendExecutionOrder() {
        LabTestExecutionOrderDTO dto = new LabTestExecutionOrderDTO();
        LabTestExecutionOrderDTO.OrderItem item = new LabTestExecutionOrderDTO.OrderItem();
        item.setTaskId(1L);
        item.setPriority("P1");
        dto.setExecutionOrder(List.of(item));
        dto.setSummary("按紧急度排序");
        dto.setDisclaimerRequired(true);
        when(labTestService.recommendExecutionOrder(9L)).thenReturn(dto);

        Result<LabTestExecutionOrderDTO> result = controller.recommendExecutionOrder(9L);

        assertEquals("SUCCESS", result.getCode());
        assertSame(dto, result.getData());
        verify(labTestService).recommendExecutionOrder(9L);
    }
}
