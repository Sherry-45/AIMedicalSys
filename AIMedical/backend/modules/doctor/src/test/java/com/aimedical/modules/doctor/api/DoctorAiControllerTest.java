package com.aimedical.modules.doctor.api;

import com.aimedical.common.result.Result;
import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisRequest;
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisResponse;
import com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendRequest;
import com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendResponse;
import com.aimedical.modules.commonmodule.auth.CurrentUser;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DoctorAiController} 单元测试。
 *
 * <p>验证控制器正确委托给 service 并传递当前医生 ID；
 * 覆盖 currentDoctorId() 空值保护分支。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class DoctorAiControllerTest {

    @Mock
    private DoctorAiService doctorAiService;

    @Mock
    private CurrentUser currentUser;

    private DoctorAiController controller;

    private static final Long DOCTOR_ID = 200L;

    @BeforeEach
    void setUp() {
        controller = new DoctorAiController(doctorAiService, currentUser);
    }

    @Test
    void diagnosis_shouldDelegateToServiceWithCurrentDoctorId() {
        DiagnosisRequest request = new DiagnosisRequest(100L, "头痛", "无", "无");
        when(currentUser.getUserId()).thenReturn(DOCTOR_ID);
        DiagnosisResponse response = new DiagnosisResponse();
        response.setPossibleDiagnoses(List.of());
        response.setSummary("建议");
        when(doctorAiService.diagnosis(request, DOCTOR_ID))
                .thenReturn(Result.success(AiResult.success(response)));

        Result<AiResult<DiagnosisResponse>> result = controller.diagnosis(request);

        assertEquals("SUCCESS", result.getCode());
        verify(doctorAiService).diagnosis(request, DOCTOR_ID);
    }

    @Test
    void recommendExamination_shouldDelegateToServiceWithCurrentDoctorId() {
        ExaminationRecommendRequest request = new ExaminationRecommendRequest(100L, "感冒", "头痛");
        when(currentUser.getUserId()).thenReturn(DOCTOR_ID);
        ExaminationRecommendResponse response = new ExaminationRecommendResponse();
        response.setItems(List.of());
        when(doctorAiService.recommendExamination(request, DOCTOR_ID))
                .thenReturn(Result.success(AiResult.success(response)));

        Result<AiResult<ExaminationRecommendResponse>> result = controller.recommendExamination(request);

        assertEquals("SUCCESS", result.getCode());
        verify(doctorAiService).recommendExamination(request, DOCTOR_ID);
    }

    @Test
    void prescriptionAssist_shouldDelegateToServiceWithCurrentDoctorId() {
        AiPrescriptionAssistRequest request = new AiPrescriptionAssistRequest(100L, "感冒", "头痛");
        when(currentUser.getUserId()).thenReturn(DOCTOR_ID);
        when(doctorAiService.prescriptionAssist(request, DOCTOR_ID))
                .thenReturn(Result.success(AiResult.success(new AiPrescriptionAssistResponse(List.of(), "建议"))));

        Result<AiResult<AiPrescriptionAssistResponse>> result = controller.prescriptionAssist(request);

        assertEquals("SUCCESS", result.getCode());
        verify(doctorAiService).prescriptionAssist(request, DOCTOR_ID);
    }

    @Test
    void prescriptionAudit_shouldDelegateToServiceWithCurrentDoctorId() {
        AiPrescriptionAuditRequest request = new AiPrescriptionAuditRequest(1L, "感冒", List.of("阿莫西林"));
        when(currentUser.getUserId()).thenReturn(DOCTOR_ID);
        when(doctorAiService.prescriptionAudit(request, DOCTOR_ID))
                .thenReturn(Result.success(AiResult.success(new AiPrescriptionAuditResponse(AiRiskLevel.LOW, List.of(), true))));

        Result<AiResult<AiPrescriptionAuditResponse>> result = controller.prescriptionAudit(request);

        assertEquals("SUCCESS", result.getCode());
        verify(doctorAiService).prescriptionAudit(request, DOCTOR_ID);
    }

    @Test
    void generateMedicalRecord_shouldDelegateToServiceWithCurrentDoctorId() {
        AiMedicalRecordGenRequest request = new AiMedicalRecordGenRequest(100L, 1L, "头痛", "无", "无", "感冒");
        when(currentUser.getUserId()).thenReturn(DOCTOR_ID);
        when(doctorAiService.generateMedicalRecord(request, DOCTOR_ID))
                .thenReturn(Result.success(AiResult.success(new AiMedicalRecordGenResponse("", "", "", "", ""))));

        Result<AiResult<AiMedicalRecordGenResponse>> result = controller.generateMedicalRecord(request);

        assertEquals("SUCCESS", result.getCode());
        verify(doctorAiService).generateMedicalRecord(request, DOCTOR_ID);
    }

    @Test
    void anyEndpoint_shouldThrowWhenUserIdIsNull() {
        when(currentUser.getUserId()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> controller.diagnosis(
                new DiagnosisRequest(100L, "头痛", "无", "无")));
    }

    @Test
    void generateInspectionReport_shouldDelegateToServiceWithCurrentDoctorId() {
        AiInspectionReportRequest request = new AiInspectionReportRequest(1L, "CT", "ref-1", 100L, List.of(), "胸", "咳嗽");
        when(currentUser.getUserId()).thenReturn(DOCTOR_ID);
        when(doctorAiService.generateInspectionReport(request, DOCTOR_ID))
                .thenReturn(Result.success(AiResult.success(new AiInspectionReportResponse(
                        "", List.of(), "", "", List.of(), "", null, null))));

        Result<AiResult<AiInspectionReportResponse>> result = controller.generateInspectionReport(request);

        assertEquals("SUCCESS", result.getCode());
        verify(doctorAiService).generateInspectionReport(request, DOCTOR_ID);
    }

    @Test
    void imageAnalysis_shouldDelegateToServiceWithCurrentDoctorId() {
        AiImageAnalysisRequest request = new AiImageAnalysisRequest("ref-1", "chest-ct-v1", 100L, null, "CT", "胸", "咳嗽", null);
        when(currentUser.getUserId()).thenReturn(DOCTOR_ID);
        when(doctorAiService.imageAnalysis(request, DOCTOR_ID))
                .thenReturn(Result.success(AiResult.success(new AiImageAnalysisResponse(
                        "", null, "", null, ""))));

        Result<AiResult<AiImageAnalysisResponse>> result = controller.imageAnalysis(request);

        assertEquals("SUCCESS", result.getCode());
        verify(doctorAiService).imageAnalysis(request, DOCTOR_ID);
    }

    @Test
    void recommendExecutionOrder_shouldDelegateToServiceWithCurrentDoctorId() {
        List<AiExecutionOrderRequest.TaskItem> tasks = List.of(
                new AiExecutionOrderRequest.TaskItem(2L, "LAB", "血常规", "HIGH", 101L));
        AiExecutionOrderRequest request = new AiExecutionOrderRequest(tasks, null, "IMAGING_DOCTOR");
        when(currentUser.getUserId()).thenReturn(DOCTOR_ID);
        when(doctorAiService.recommendExecutionOrder(request, DOCTOR_ID))
                .thenReturn(Result.success(AiResult.success(new AiExecutionOrderResponse(
                        List.of(), "", Boolean.TRUE))));

        Result<AiResult<AiExecutionOrderResponse>> result = controller.recommendExecutionOrder(request);

        assertEquals("SUCCESS", result.getCode());
        verify(doctorAiService).recommendExecutionOrder(request, DOCTOR_ID);
    }

    @Test
    void discussionConclusion_shouldDelegateToServiceWithCurrentDoctorId() {
        List<AiDiscussionConclusionRequest.Transcript> transcripts = List.of(
                new AiDiscussionConclusionRequest.Transcript("DOCTOR", "张医生", "10:00", "考虑上呼吸道感染"));
        AiDiscussionConclusionRequest request = new AiDiscussionConclusionRequest(transcripts);
        when(currentUser.getUserId()).thenReturn(DOCTOR_ID);
        when(doctorAiService.discussionConclusion(request, DOCTOR_ID))
                .thenReturn(Result.success(AiResult.success(new AiDiscussionConclusionResponse("", "", ""))));

        Result<AiResult<AiDiscussionConclusionResponse>> result = controller.discussionConclusion(request);

        assertEquals("SUCCESS", result.getCode());
        verify(doctorAiService).discussionConclusion(request, DOCTOR_ID);
    }
}
