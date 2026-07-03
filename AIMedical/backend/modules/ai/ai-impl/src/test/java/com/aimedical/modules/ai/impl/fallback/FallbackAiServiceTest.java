package com.aimedical.modules.ai.impl.fallback;

import org.junit.jupiter.api.Test;

import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.AiService;
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisRequest;
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisResponse;
import com.aimedical.modules.ai.api.dto.discussion.DiscussionConclusionRequest;
import com.aimedical.modules.ai.api.dto.discussion.DiscussionConclusionResponse;
import com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendRequest;
import com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendResponse;
import com.aimedical.modules.ai.api.dto.execution.ExecutionOrderRequest;
import com.aimedical.modules.ai.api.dto.execution.ExecutionOrderResponse;
import com.aimedical.modules.ai.api.dto.image.ImageAnalysisRequest;
import com.aimedical.modules.ai.api.dto.image.ImageAnalysisResponse;
import com.aimedical.modules.ai.api.dto.inspection.InspectionReportRequest;
import com.aimedical.modules.ai.api.dto.inspection.InspectionReportResponse;
import com.aimedical.modules.ai.api.dto.kb.KbQueryRequest;
import com.aimedical.modules.ai.api.dto.kb.KbQueryResponse;
import com.aimedical.modules.ai.api.dto.labtest.LabTestReportRequest;
import com.aimedical.modules.ai.api.dto.labtest.LabTestReportResponse;
import com.aimedical.modules.ai.api.dto.medicalrecord.MedicalRecordGenRequest;
import com.aimedical.modules.ai.api.dto.medicalrecord.MedicalRecordGenResponse;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionAssistRequest;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionAssistResponse;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionCheckRequest;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionCheckResponse;
import com.aimedical.modules.ai.api.dto.schedule.ScheduleRequest;
import com.aimedical.modules.ai.api.dto.schedule.ScheduleResponse;
import com.aimedical.modules.ai.api.dto.triage.TriageRequest;
import com.aimedical.modules.ai.api.dto.triage.TriageResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.ObjectProvider;

class FallbackAiServiceTest {

    @Test
    void shouldDelegateToFirstAvailableService() {
        AiService delegate = mock(AiService.class);
        TriageRequest request = new TriageRequest();
        TriageResponse response = new TriageResponse();
        when(delegate.triage(request)).thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));

        ObjectProvider<AiService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(delegate);
        FallbackAiService fallback = new FallbackAiService(provider);
        AiResult<TriageResponse> result = fallback.triage(request).join();

        assertTrue(result.isSuccess());
        assertSame(response, result.getData());
    }

    @Test
    void shouldThrowWhenNoDelegateAvailable() {
        ObjectProvider<AiService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        FallbackAiService fallback = new FallbackAiService(provider);
        assertNotNull(fallback);
        TriageRequest request = new TriageRequest();
        AiResult<TriageResponse> result = fallback.triage(request).join();
        assertTrue(result.isDegraded());
    }

    @Test
    void shouldReturnOriginalResultWhenDelegateAlreadyDegraded() {
        AiService delegate = mock(AiService.class);
        TriageRequest request = new TriageRequest();
        AiResult<TriageResponse> degradedResult = AiResult.degraded("already degraded");
        when(delegate.triage(request)).thenReturn(CompletableFuture.completedFuture(degradedResult));

        ObjectProvider<AiService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(delegate);
        FallbackAiService fallback = new FallbackAiService(provider);
        AiResult<TriageResponse> result = fallback.triage(request).join();

        assertFalse(result.isSuccess());
        assertTrue(result.isDegraded());
        assertEquals("already degraded", result.getFallbackReason());
    }

    @Test
    void diagnosisShouldDelegateWhenAvailable() {
        AiService delegate = mock(AiService.class);
        DiagnosisRequest request = new DiagnosisRequest();
        DiagnosisResponse response = new DiagnosisResponse();
        when(delegate.diagnosis(request)).thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));

        ObjectProvider<AiService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(delegate);
        FallbackAiService fallback = new FallbackAiService(provider);
        AiResult<DiagnosisResponse> result = fallback.diagnosis(request).join();

        assertTrue(result.isSuccess());
        assertSame(response, result.getData());
    }

    @Test
    void prescriptionCheckShouldDelegateWhenAvailable() {
        AiService delegate = mock(AiService.class);
        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        PrescriptionCheckResponse response = new PrescriptionCheckResponse();
        when(delegate.prescriptionCheck(request)).thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));

        ObjectProvider<AiService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(delegate);
        FallbackAiService fallback = new FallbackAiService(provider);
        AiResult<PrescriptionCheckResponse> result = fallback.prescriptionCheck(request).join();

        assertTrue(result.isSuccess());
        assertSame(response, result.getData());
    }

    @Test
    void generateMedicalRecordShouldDelegateWhenAvailable() {
        AiService delegate = mock(AiService.class);
        MedicalRecordGenRequest request = new MedicalRecordGenRequest();
        MedicalRecordGenResponse response = new MedicalRecordGenResponse();
        when(delegate.generateMedicalRecord(request)).thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));

        ObjectProvider<AiService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(delegate);
        FallbackAiService fallback = new FallbackAiService(provider);
        AiResult<MedicalRecordGenResponse> result = fallback.generateMedicalRecord(request).join();

        assertTrue(result.isSuccess());
        assertSame(response, result.getData());
    }

    @Test
    void analysisReportForInspectionShouldDelegateWhenAvailable() {
        AiService delegate = mock(AiService.class);
        InspectionReportRequest request = new InspectionReportRequest();
        InspectionReportResponse response = new InspectionReportResponse();
        when(delegate.analysisReportForInspection(request)).thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));

        ObjectProvider<AiService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(delegate);
        FallbackAiService fallback = new FallbackAiService(provider);
        AiResult<InspectionReportResponse> result = fallback.analysisReportForInspection(request).join();

        assertTrue(result.isSuccess());
        assertSame(response, result.getData());
    }

    @Test
    void analysisReportForLabTestShouldDelegateWhenAvailable() {
        AiService delegate = mock(AiService.class);
        LabTestReportRequest request = new LabTestReportRequest();
        LabTestReportResponse response = new LabTestReportResponse();
        when(delegate.analysisReportForLabTest(request)).thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));

        ObjectProvider<AiService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(delegate);
        FallbackAiService fallback = new FallbackAiService(provider);
        AiResult<LabTestReportResponse> result = fallback.analysisReportForLabTest(request).join();

        assertTrue(result.isSuccess());
        assertSame(response, result.getData());
    }

    @Test
    void imageAnalysisShouldDelegateWhenAvailable() {
        AiService delegate = mock(AiService.class);
        ImageAnalysisRequest request = new ImageAnalysisRequest();
        ImageAnalysisResponse response = new ImageAnalysisResponse();
        when(delegate.imageAnalysis(request)).thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));

        ObjectProvider<AiService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(delegate);
        FallbackAiService fallback = new FallbackAiService(provider);
        AiResult<ImageAnalysisResponse> result = fallback.imageAnalysis(request).join();

        assertTrue(result.isSuccess());
        assertSame(response, result.getData());
    }

    @Test
    void knowledgeBaseQueryShouldDelegateWhenAvailable() {
        AiService delegate = mock(AiService.class);
        KbQueryRequest request = new KbQueryRequest();
        KbQueryResponse response = new KbQueryResponse();
        when(delegate.knowledgeBaseQuery(request)).thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));

        ObjectProvider<AiService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(delegate);
        FallbackAiService fallback = new FallbackAiService(provider);
        AiResult<KbQueryResponse> result = fallback.knowledgeBaseQuery(request).join();

        assertTrue(result.isSuccess());
        assertSame(response, result.getData());
    }

    @Test
    void recommendExaminationShouldDelegateWhenAvailable() {
        AiService delegate = mock(AiService.class);
        ExaminationRecommendRequest request = new ExaminationRecommendRequest();
        ExaminationRecommendResponse response = new ExaminationRecommendResponse();
        when(delegate.recommendExamination(request)).thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));

        ObjectProvider<AiService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(delegate);
        FallbackAiService fallback = new FallbackAiService(provider);
        AiResult<ExaminationRecommendResponse> result = fallback.recommendExamination(request).join();

        assertTrue(result.isSuccess());
        assertSame(response, result.getData());
    }

    @Test
    void prescriptionAssistShouldDelegateWhenAvailable() {
        AiService delegate = mock(AiService.class);
        PrescriptionAssistRequest request = new PrescriptionAssistRequest();
        PrescriptionAssistResponse response = new PrescriptionAssistResponse();
        when(delegate.prescriptionAssist(request)).thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));

        ObjectProvider<AiService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(delegate);
        FallbackAiService fallback = new FallbackAiService(provider);
        AiResult<PrescriptionAssistResponse> result = fallback.prescriptionAssist(request).join();

        assertTrue(result.isSuccess());
        assertSame(response, result.getData());
    }

    @Test
    void recommendExecutionOrderShouldDelegateWhenAvailable() {
        AiService delegate = mock(AiService.class);
        ExecutionOrderRequest request = new ExecutionOrderRequest();
        ExecutionOrderResponse response = new ExecutionOrderResponse();
        when(delegate.recommendExecutionOrder(request)).thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));

        ObjectProvider<AiService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(delegate);
        FallbackAiService fallback = new FallbackAiService(provider);
        AiResult<ExecutionOrderResponse> result = fallback.recommendExecutionOrder(request).join();

        assertTrue(result.isSuccess());
        assertSame(response, result.getData());
    }

    @Test
    void scheduleShouldDelegateWhenAvailable() {
        AiService delegate = mock(AiService.class);
        ScheduleRequest request = new ScheduleRequest();
        ScheduleResponse response = new ScheduleResponse();
        when(delegate.schedule(request)).thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));

        ObjectProvider<AiService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(delegate);
        FallbackAiService fallback = new FallbackAiService(provider);
        AiResult<ScheduleResponse> result = fallback.schedule(request).join();

        assertTrue(result.isSuccess());
        assertSame(response, result.getData());
    }

    @Test
    void discussionConclusionShouldDelegateWhenAvailable() {
        AiService delegate = mock(AiService.class);
        DiscussionConclusionRequest request = new DiscussionConclusionRequest();
        DiscussionConclusionResponse response = new DiscussionConclusionResponse();
        when(delegate.discussionConclusion(request)).thenReturn(CompletableFuture.completedFuture(AiResult.success(response)));

        ObjectProvider<AiService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(delegate);
        FallbackAiService fallback = new FallbackAiService(provider);
        AiResult<DiscussionConclusionResponse> result = fallback.discussionConclusion(request).join();

        assertTrue(result.isSuccess());
        assertSame(response, result.getData());
    }
}
