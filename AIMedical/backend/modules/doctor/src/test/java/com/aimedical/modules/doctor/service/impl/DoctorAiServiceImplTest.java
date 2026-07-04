package com.aimedical.modules.doctor.service.impl;

import com.aimedical.common.result.Result;
import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.AiService;
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisRequest;
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisResponse;
import com.aimedical.modules.ai.api.dto.execution.ExecutionOrderRequest;
import com.aimedical.modules.ai.api.dto.execution.ExecutionOrderResponse;
import com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendRequest;
import com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendResponse;
import com.aimedical.modules.ai.api.dto.image.ImageAnalysisRequest;
import com.aimedical.modules.ai.api.dto.image.ImageAnalysisResponse;
import com.aimedical.modules.ai.api.dto.inspection.InspectionReportRequest;
import com.aimedical.modules.ai.api.dto.inspection.InspectionReportResponse;
import com.aimedical.modules.ai.api.dto.medicalrecord.MedicalRecordGenRequest;
import com.aimedical.modules.ai.api.dto.medicalrecord.MedicalRecordGenResponse;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionAssistRequest;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionAssistResponse;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionCheckRequest;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionCheckResponse;
import com.aimedical.modules.doctor.dto.request.AiDiscussionConclusionRequest;
import com.aimedical.modules.doctor.dto.request.AiExecutionOrderRequest;
import com.aimedical.modules.doctor.dto.request.AiImageAnalysisRequest;
import com.aimedical.modules.doctor.dto.request.AiInspectionReportRequest;
import com.aimedical.modules.doctor.dto.request.AiMedicalRecordGenRequest;
import com.aimedical.modules.doctor.dto.request.AiPrescriptionAssistRequest;
import com.aimedical.modules.doctor.dto.request.AiPrescriptionAuditRequest;
import com.aimedical.modules.doctor.dto.response.AiDiscussionConclusionResponse;
import com.aimedical.modules.doctor.dto.response.AiExecutionOrderResponse;
import com.aimedical.modules.doctor.dto.response.AiImageAnalysisResponse;
import com.aimedical.modules.doctor.dto.response.AiInspectionReportResponse;
import com.aimedical.modules.doctor.dto.response.AiMedicalRecordGenResponse;
import com.aimedical.modules.doctor.dto.response.AiPrescriptionAssistResponse;
import com.aimedical.modules.doctor.dto.response.AiPrescriptionAuditResponse;
import com.aimedical.modules.doctor.entity.AiRiskLevel;
import com.aimedical.modules.doctor.service.DoctorAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DoctorAiServiceImpl} 单元测试。
 *
 * <p>覆盖两条路径：
 * <ul>
 *   <li>{@code mockDegrade=true}（默认）验证降级兜底数据与字段</li>
 *   <li>{@code mockDegrade=false} 验证 AiService 成功/异常/降级三种分支的字段映射与降级行为</li>
 * </ul>
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class DoctorAiServiceImplTest {

    @Mock
    private AiService aiService;

    private DoctorAiService service;

    @BeforeEach
    void setUp() {
        DoctorAiServiceImpl impl = new DoctorAiServiceImpl(aiService);
        // mockDegrade=true 为默认值，启用降级模式不实际调用 AiService
        ReflectionTestUtils.setField(impl, "mockDegrade", true);
        this.service = impl;
    }

    @Test
    void diagnosis_shouldReturnDegradedResultWhenMockDegrade() {
        Result<AiResult<DiagnosisResponse>> result =
                service.diagnosis(new DiagnosisRequest(100L, "头痛", "无", "无"), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isDegraded());
        assertNotNull(result.getData().getFallbackReason());
        assertNotNull(result.getData().getData());
        assertNotNull(result.getData().getData().getPossibleDiagnoses());
    }

    @Test
    void recommendExamination_shouldReturnDegradedResultWithFallbackItems() {
        Result<AiResult<ExaminationRecommendResponse>> result =
                service.recommendExamination(new ExaminationRecommendRequest(100L, "感冒", "头痛"), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().isDegraded());
        assertNotNull(result.getData().getData());
        assertFalse(result.getData().getData().getItems().isEmpty());
    }

    @Test
    void prescriptionAssist_shouldReturnDegradedResult() {
        Result<AiResult<AiPrescriptionAssistResponse>> result =
                service.prescriptionAssist(
                        new AiPrescriptionAssistRequest(100L, "感冒", "头痛"), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().isDegraded());
        assertNotNull(result.getData().getFallbackReason());
        assertNotNull(result.getData().getData());
    }

    @Test
    void prescriptionAudit_shouldReturnDegradedResult() {
        Result<AiResult<AiPrescriptionAuditResponse>> result =
                service.prescriptionAudit(
                        new AiPrescriptionAuditRequest(1L, "感冒", List.of("阿莫西林")), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().isDegraded());
        // 降级路径下 riskLevel 为 null（表示未知风险，由人工判断），passed=false
        assertNull(result.getData().getData().riskLevel());
        assertFalse(result.getData().getData().passed());
    }

    @Test
    void generateMedicalRecord_shouldReturnDegradedResult() {
        Result<AiResult<AiMedicalRecordGenResponse>> result =
                service.generateMedicalRecord(
                        new AiMedicalRecordGenRequest(100L, 1L, "头痛", "无", "无", "感冒"), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().isDegraded());
        assertNotNull(result.getData().getFallbackReason());
        assertNotNull(result.getData().getData());
    }

    // ---------- mockDegrade=false 路径：覆盖 AiService 成功/异常/降级三种分支 ----------

    /**
     * 辅助构造一个 {@code mockDegrade=false} 的 DoctorAiServiceImpl 实例，
     * 用于验证非降级模式下的真实 AiService 调用路径。
     */
    private DoctorAiService nonDegradingService() {
        DoctorAiServiceImpl impl = new DoctorAiServiceImpl(aiService);
        ReflectionTestUtils.setField(impl, "mockDegrade", false);
        return impl;
    }

    /**
     * 成功分支：AiService 返回 {@link AiResult#success}，
     * 验证 {@code possibleDiagnoses} 与 {@code summary} 字段被正确映射到响应 DTO。
     */
    @Test
    void diagnosis_shouldMapAiDataWhenServiceSucceeds() {
        DiagnosisResponse aiData = new DiagnosisResponse();
        aiData.setPossibleDiagnoses(List.of("上呼吸道感染", "急性咽炎"));
        aiData.setSummary("结合主诉与体征，考虑上呼吸道感染可能性大");
        when(aiService.diagnosis(any(DiagnosisRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(AiResult.success(aiData)));

        DoctorAiService nonDegrading = nonDegradingService();
        Result<AiResult<DiagnosisResponse>> result =
                nonDegrading.diagnosis(new DiagnosisRequest(100L, "头痛", "无", "无"), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertFalse(result.getData().isDegraded());
        DiagnosisResponse data = result.getData().getData();
        assertNotNull(data);
        assertEquals(List.of("上呼吸道感染", "急性咽炎"), data.getPossibleDiagnoses());
        assertEquals("结合主诉与体征，考虑上呼吸道感染可能性大", data.getSummary());
        verify(aiService).diagnosis(any(DiagnosisRequest.class));
    }

    /**
     * 异常分支：AiService 抛出异常（CompletableFuture.failedFuture），
     * 验证被 catch 后走降级路径返回兜底数据与降级原因。
     */
    @Test
    void diagnosis_shouldDegradeWhenServiceThrows() {
        when(aiService.diagnosis(any(DiagnosisRequest.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("AI 服务不可用")));

        DoctorAiService nonDegrading = nonDegradingService();
        Result<AiResult<DiagnosisResponse>> result =
                nonDegrading.diagnosis(new DiagnosisRequest(100L, "头痛", "无", "无"), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isDegraded());
        assertNotNull(result.getData().getFallbackReason());
        assertNotNull(result.getData().getData());
        verify(aiService).diagnosis(any(DiagnosisRequest.class));
    }

    /**
     * 降级分支：AiService 自身返回 {@link AiResult#degraded}（如限流/熔断），
     * 验证 DoctorAiServiceImpl 将其转换为统一的降级响应。
     */
    @Test
    void diagnosis_shouldDegradeWhenServiceReturnsDegradedResult() {
        when(aiService.diagnosis(any(DiagnosisRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(AiResult.degraded("AI 服务限流，已降级")));

        DoctorAiService nonDegrading = nonDegradingService();
        Result<AiResult<DiagnosisResponse>> result =
                nonDegrading.diagnosis(new DiagnosisRequest(100L, "头痛", "无", "无"), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isDegraded());
        assertNotNull(result.getData().getFallbackReason());
        verify(aiService).diagnosis(any(DiagnosisRequest.class));
    }

    @Test
    void recommendExamination_shouldMapAiDataWhenServiceSucceeds() {
        ExaminationRecommendResponse aiData = new ExaminationRecommendResponse();
        ExaminationRecommendResponse.ExaminationItem item1 =
                new ExaminationRecommendResponse.ExaminationItem("血常规", "检验", "排查感染");
        ExaminationRecommendResponse.ExaminationItem item2 =
                new ExaminationRecommendResponse.ExaminationItem("心电图", "检查", "排查心脏异常");
        aiData.setItems(List.of(item1, item2));
        when(aiService.recommendExamination(any(ExaminationRecommendRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(AiResult.success(aiData)));

        DoctorAiService nonDegrading = nonDegradingService();
        Result<AiResult<ExaminationRecommendResponse>> result =
                nonDegrading.recommendExamination(new ExaminationRecommendRequest(100L, "感冒", "头痛"), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertFalse(result.getData().isDegraded());
        List<ExaminationRecommendResponse.ExaminationItem> items = result.getData().getData().getItems();
        assertEquals(2, items.size());
        assertEquals("血常规", items.get(0).getName());
        assertEquals("检验", items.get(0).getCategory());
        assertEquals("心电图", items.get(1).getName());
        verify(aiService).recommendExamination(any(ExaminationRecommendRequest.class));
    }

    @Test
    void prescriptionAssist_shouldMapAiDataWhenServiceSucceeds() {
        PrescriptionAssistResponse aiData = new PrescriptionAssistResponse();
        aiData.setPrescriptionDraft("建议抗感染治疗");
        when(aiService.prescriptionAssist(any(PrescriptionAssistRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(AiResult.success(aiData)));

        DoctorAiService nonDegrading = nonDegradingService();
        Result<AiResult<AiPrescriptionAssistResponse>> result =
                nonDegrading.prescriptionAssist(new AiPrescriptionAssistRequest(100L, "感冒", "头痛"), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertFalse(result.getData().isDegraded());
        AiPrescriptionAssistResponse data = result.getData().getData();
        assertEquals("建议抗感染治疗", data.summary());
        verify(aiService).prescriptionAssist(any(PrescriptionAssistRequest.class));
    }

    @Test
    void prescriptionAudit_shouldMapAiDataWhenServiceSucceeds() {
        PrescriptionCheckResponse aiData = new PrescriptionCheckResponse();
        aiData.setRiskLevel("HIGH");
        com.aimedical.modules.ai.api.dto.prescription.AlertItem alert =
                new com.aimedical.modules.ai.api.dto.prescription.AlertItem();
        alert.setAlertMessage("与现有药物存在相互作用");
        aiData.setAlerts(List.of(alert));
        aiData.setFromFallback(false);
        when(aiService.prescriptionCheck(any(PrescriptionCheckRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(AiResult.success(aiData)));

        DoctorAiService nonDegrading = nonDegradingService();
        Result<AiResult<AiPrescriptionAuditResponse>> result =
                nonDegrading.prescriptionAudit(
                        new AiPrescriptionAuditRequest(1L, "感冒", List.of("阿莫西林")), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertFalse(result.getData().isDegraded());
        AiPrescriptionAuditResponse data = result.getData().getData();
        assertEquals(AiRiskLevel.HIGH, data.riskLevel());
        assertEquals(1, data.warnings().size());
        assertTrue(data.passed());
        verify(aiService).prescriptionCheck(any(PrescriptionCheckRequest.class));
    }

    @Test
    void generateMedicalRecord_shouldMapAiDataWhenServiceSucceeds() {
        MedicalRecordGenResponse aiData = new MedicalRecordGenResponse();
        aiData.setChiefComplaint("头痛三天");
        aiData.setPresentIllness("三天前无明显诱因出现头痛");
        aiData.setPastHistory("高血压五年");
        aiData.setPreliminaryDiagnosis("偏头痛");
        aiData.setTreatmentPlan("布洛芬口服");
        when(aiService.generateMedicalRecord(any(MedicalRecordGenRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(AiResult.success(aiData)));

        DoctorAiService nonDegrading = nonDegradingService();
        Result<AiResult<AiMedicalRecordGenResponse>> result =
                nonDegrading.generateMedicalRecord(
                        new AiMedicalRecordGenRequest(100L, 1L, "头痛", "无", "无", "感冒"), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertFalse(result.getData().isDegraded());
        AiMedicalRecordGenResponse data = result.getData().getData();
        assertEquals("头痛三天", data.chiefComplaint());
        assertEquals("三天前无明显诱因出现头痛", data.presentIllness());
        assertEquals("高血压五年", data.pastHistory());
        assertEquals("偏头痛", data.diagnosis());
        assertEquals("布洛芬口服", data.treatmentPlan());
        verify(aiService).generateMedicalRecord(any(MedicalRecordGenRequest.class));
    }

    // ---------- 新增 4 项 AI 入口的降级 + 成功分支测试 ----------

    @Test
    void generateInspectionReport_shouldReturnDegradedResultWhenMockDegrade() {
        Result<AiResult<AiInspectionReportResponse>> result =
                service.generateInspectionReport(
                        new AiInspectionReportRequest(1L, "CT", "ref-1", 100L, List.of(), "胸", "咳嗽"), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().isDegraded());
        assertNotNull(result.getData().getFallbackReason());
        assertNotNull(result.getData().getData());
    }

    @Test
    void imageAnalysis_shouldReturnDegradedResultWhenMockDegrade() {
        Result<AiResult<AiImageAnalysisResponse>> result =
                service.imageAnalysis(
                        new AiImageAnalysisRequest("ref-1", "chest-ct-v1", 100L, null, "CT", "胸", "咳嗽", null), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().isDegraded());
        assertNotNull(result.getData().getFallbackReason());
        assertNotNull(result.getData().getData());
    }

    @Test
    void recommendExecutionOrder_shouldReturnDegradedResultSortedByUrgency() {
        List<AiExecutionOrderRequest.TaskItem> tasks = List.of(
                new AiExecutionOrderRequest.TaskItem(1L, "IMAGING", "胸部CT", "LOW", 100L),
                new AiExecutionOrderRequest.TaskItem(2L, "LAB", "血常规", "HIGH", 101L),
                new AiExecutionOrderRequest.TaskItem(3L, "LAB", "尿常规", "MEDIUM", 102L));
        Result<AiResult<AiExecutionOrderResponse>> result =
                service.recommendExecutionOrder(
                        new AiExecutionOrderRequest(tasks, null, "IMAGING_DOCTOR"), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().isDegraded());
        List<AiExecutionOrderResponse.OrderItem> order = result.getData().getData().executionOrder();
        // HIGH 应排在最前
        assertEquals(2L, order.get(0).taskId());
        assertEquals("HIGH", order.get(0).priority());
    }

    @Test
    void discussionConclusion_shouldReturnDegradedResultWhenMockDegrade() {
        List<AiDiscussionConclusionRequest.Transcript> transcripts = List.of(
                new AiDiscussionConclusionRequest.Transcript("DOCTOR", "张医生", "10:00", "考虑上呼吸道感染"));
        Result<AiResult<AiDiscussionConclusionResponse>> result =
                service.discussionConclusion(new AiDiscussionConclusionRequest(transcripts), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertTrue(result.getData().isDegraded());
        assertNotNull(result.getData().getFallbackReason());
        assertNotNull(result.getData().getData());
    }

    @Test
    void generateInspectionReport_shouldMapAiDataWhenServiceSucceeds() {
        InspectionReportResponse aiData = new InspectionReportResponse();
        aiData.setReportDraft("胸部 CT 未见明显异常");
        aiData.setFindings(List.of("双肺纹理清晰"));
        aiData.setImpression("未见明显异常");
        aiData.setConfidence(85.0);
        when(aiService.analysisReportForInspection(any(InspectionReportRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(AiResult.success(aiData)));

        DoctorAiService nonDegrading = nonDegradingService();
        Result<AiResult<AiInspectionReportResponse>> result =
                nonDegrading.generateInspectionReport(
                        new AiInspectionReportRequest(1L, "CT", "ref-1", 100L, List.of(), "胸", "咳嗽"), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertFalse(result.getData().isDegraded());
        AiInspectionReportResponse data = result.getData().getData();
        assertEquals("胸部 CT 未见明显异常", data.reportDraft());
        assertEquals(List.of("双肺纹理清晰"), data.findings());
        assertEquals("未见明显异常", data.impression());
        assertEquals(85.0, data.confidence());
        verify(aiService).analysisReportForInspection(any(InspectionReportRequest.class));
    }

    @Test
    void imageAnalysis_shouldMapAiDataWhenServiceSucceeds() {
        ImageAnalysisResponse aiData = new ImageAnalysisResponse();
        aiData.setModelId("chest-ct-v1");
        ImageAnalysisResponse.RecognitionResult rec = new ImageAnalysisResponse.RecognitionResult();
        rec.setRegions(List.of("左肺上叶"));
        rec.setLabels(List.of("结节"));
        rec.setScores(List.of(0.92));
        aiData.setRecognitionResult(rec);
        aiData.setConfidence(92.0);
        aiData.setAuxiliaryAdvice("建议结合临床进一步复查");
        when(aiService.imageAnalysis(any(ImageAnalysisRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(AiResult.success(aiData)));

        DoctorAiService nonDegrading = nonDegradingService();
        Result<AiResult<AiImageAnalysisResponse>> result =
                nonDegrading.imageAnalysis(
                        new AiImageAnalysisRequest("ref-1", "chest-ct-v1", 100L, null, "CT", "胸", "咳嗽", null), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertFalse(result.getData().isDegraded());
        AiImageAnalysisResponse data = result.getData().getData();
        assertEquals("chest-ct-v1", data.modelId());
        assertEquals(List.of("左肺上叶"), data.recognitionResult().regions());
        assertEquals(List.of("结节"), data.recognitionResult().labels());
        assertEquals(92.0, data.confidence());
        verify(aiService).imageAnalysis(any(ImageAnalysisRequest.class));
    }

    @Test
    void recommendExecutionOrder_shouldMapAiDataWhenServiceSucceeds() {
        ExecutionOrderResponse aiData = new ExecutionOrderResponse();
        ExecutionOrderResponse.OrderItem item = new ExecutionOrderResponse.OrderItem();
        item.setTaskId(2L);
        item.setPriority("P1");
        item.setRecommendedTime("立即");
        item.setReason("急诊检查优先");
        aiData.setExecutionOrder(List.of(item));
        aiData.setSummary("按急诊优先排序");
        aiData.setDisclaimerRequired(Boolean.TRUE);
        when(aiService.recommendExecutionOrder(any(ExecutionOrderRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(AiResult.success(aiData)));

        DoctorAiService nonDegrading = nonDegradingService();
        List<AiExecutionOrderRequest.TaskItem> tasks = List.of(
                new AiExecutionOrderRequest.TaskItem(2L, "LAB", "血常规", "HIGH", 101L));
        Result<AiResult<AiExecutionOrderResponse>> result =
                nonDegrading.recommendExecutionOrder(
                        new AiExecutionOrderRequest(tasks, null, "IMAGING_DOCTOR"), 200L);

        assertEquals("SUCCESS", result.getCode());
        assertFalse(result.getData().isDegraded());
        AiExecutionOrderResponse data = result.getData().getData();
        assertEquals(1, data.executionOrder().size());
        assertEquals(2L, data.executionOrder().get(0).taskId());
        assertEquals("P1", data.executionOrder().get(0).priority());
        assertEquals("按急诊优先排序", data.summary());
        assertTrue(data.disclaimerRequired());
        verify(aiService).recommendExecutionOrder(any(ExecutionOrderRequest.class));
    }

    @Test
    void discussionConclusion_shouldDegradeEvenWhenServiceSucceedsDueToEmptyResponse() {
        // 当前 AI 能力响应为空对象，即使成功也走降级包装
        when(aiService.discussionConclusion(any(com.aimedical.modules.ai.api.dto.discussion.DiscussionConclusionRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(
                        AiResult.success(new com.aimedical.modules.ai.api.dto.discussion.DiscussionConclusionResponse())));

        DoctorAiService nonDegrading = nonDegradingService();
        List<AiDiscussionConclusionRequest.Transcript> transcripts = List.of(
                new AiDiscussionConclusionRequest.Transcript("DOCTOR", "张医生", "10:00", "考虑上呼吸道感染"));
        Result<AiResult<AiDiscussionConclusionResponse>> result =
                nonDegrading.discussionConclusion(new AiDiscussionConclusionRequest(transcripts), 200L);

        assertEquals("SUCCESS", result.getCode());
        // 由于 AI 响应为空对象，统一包装为降级
        assertTrue(result.getData().isDegraded());
        assertNotNull(result.getData().getFallbackReason());
        verify(aiService).discussionConclusion(any(com.aimedical.modules.ai.api.dto.discussion.DiscussionConclusionRequest.class));
    }
}
