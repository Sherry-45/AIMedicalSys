package com.aimedical.modules.ai.impl.orchestrator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisRequest;
import com.aimedical.modules.ai.api.dto.discussion.DiscussionConclusionRequest;
import com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendRequest;
import com.aimedical.modules.ai.api.dto.execution.ExecutionOrderRequest;
import com.aimedical.modules.ai.api.dto.image.ImageAnalysisRequest;
import com.aimedical.modules.ai.api.dto.inspection.InspectionReportRequest;
import com.aimedical.modules.ai.api.dto.kb.KbQueryRequest;
import com.aimedical.modules.ai.api.dto.labtest.LabTestReportRequest;
import com.aimedical.modules.ai.api.dto.medicalrecord.MedicalRecordGenRequest;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionAssistRequest;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionCheckRequest;
import com.aimedical.modules.ai.api.dto.schedule.ScheduleRequest;
import com.aimedical.modules.ai.api.dto.triage.TriageRequest;
import com.aimedical.modules.ai.api.dto.triage.TriageResponse;
import com.aimedical.modules.ai.impl.metrics.AiCallRecord;
import com.aimedical.modules.ai.impl.metrics.AiMetricsCollector;
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;

import static org.junit.jupiter.api.Assertions.*;

class AiOrchestratorTest {

    private SlidingWindowMetricsStore metricsStore;
    private AiMetricsCollector metricsCollector;
    private TestCapabilityExecutor triageExecutor;
    private TestCapabilityExecutor diagnosisExecutor;

    @BeforeEach
    void setUp() {
        metricsStore = new SlidingWindowMetricsStore();
        metricsCollector = new AiMetricsCollector() {
            @Override
            public void record(AiCallRecord r) {}
        };
        triageExecutor = new TestCapabilityExecutor("TRIAGE");
        diagnosisExecutor = new TestCapabilityExecutor("DIAGNOSIS");
    }

    private AiOrchestrator createOrchestrator(List<CapabilityExecutor<?, ?>> executors) {
        AiOrchestrator orch = new AiOrchestrator(executors, metricsStore, metricsCollector);
        orch.initExecutorMap();
        return orch;
    }

    @Test
    void triageShouldDelegateToTriageExecutorWithRequestPassthrough() {
        AiOrchestrator orch = createOrchestrator(List.of(triageExecutor));
        TriageRequest request = new TriageRequest();
        orch.triage(request);
        assertSame(request, triageExecutor.capturedRequest);
        assertEquals("TRIAGE", triageExecutor.capturedCapabilityId);
    }

    @Test
    void diagnosisShouldDelegateToDiagnosisExecutorWithRequestPassthrough() {
        AiOrchestrator orch = createOrchestrator(List.of(diagnosisExecutor));
        DiagnosisRequest request = new DiagnosisRequest();
        orch.diagnosis(request);
        assertSame(request, diagnosisExecutor.capturedRequest);
        assertEquals("DIAGNOSIS", diagnosisExecutor.capturedCapabilityId);
    }

    @Test
    void allMethodsShouldReturnSuccessResult() {
        List<String> allIds = List.of(
            "TRIAGE", "DIAGNOSIS", "RX_AUDIT", "MEDICAL_RECORD_GEN",
            "ANALYSIS_REPORT_INSPECTION", "ANALYSIS_REPORT_LABTEST", "IMAGE_ANALYSIS",
            "KB_QUERY", "RECOMMEND_EXAM", "RX_ASSIST", "RECOMMEND_EXEC_ORDER",
            "SCHEDULE", "DISCUSSION_CONCLUSION"
        );
        Map<String, TestCapabilityExecutor> executorMap = new ConcurrentHashMap<>();
        for (String id : allIds) {
            executorMap.put(id, new TestCapabilityExecutor(id));
        }
        AiOrchestrator orch = createOrchestrator(List.copyOf(executorMap.values()));

        assertTrue(orch.triage(new TriageRequest()).join().isSuccess());
        assertTrue(orch.diagnosis(new DiagnosisRequest()).join().isSuccess());
        assertTrue(orch.prescriptionCheck(new PrescriptionCheckRequest()).join().isSuccess());
        assertTrue(orch.generateMedicalRecord(new MedicalRecordGenRequest()).join().isSuccess());
        assertTrue(orch.analysisReportForInspection(new InspectionReportRequest()).join().isSuccess());
        assertTrue(orch.analysisReportForLabTest(new LabTestReportRequest()).join().isSuccess());
        assertTrue(orch.imageAnalysis(new ImageAnalysisRequest()).join().isSuccess());
        assertTrue(orch.knowledgeBaseQuery(new KbQueryRequest()).join().isSuccess());
        assertTrue(orch.recommendExamination(new ExaminationRecommendRequest()).join().isSuccess());
        assertTrue(orch.prescriptionAssist(new PrescriptionAssistRequest()).join().isSuccess());
        assertTrue(orch.recommendExecutionOrder(new ExecutionOrderRequest()).join().isSuccess());
        assertTrue(orch.schedule(new ScheduleRequest()).join().isSuccess());
        assertTrue(orch.discussionConclusion(new DiscussionConclusionRequest()).join().isSuccess());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForUnregisteredCapability() {
        // T19: unregistered capabilityId throws IllegalArgumentException directly
        AiOrchestrator orch = createOrchestrator(List.of(diagnosisExecutor));
        assertThrows(IllegalArgumentException.class, () -> orch.triage(new TriageRequest()));
    }

    @Test
    void shouldReturnFailureOnExecutorSyncException() {
        triageExecutor.syncException = new IllegalStateException("test error");
        AiOrchestrator orch = createOrchestrator(List.of(triageExecutor));
        AiResult<TriageResponse> result = orch.triage(new TriageRequest()).join();
        assertFalse(result.isSuccess());
        assertEquals("AI服务暂时不可用，请稍后重试", result.getErrorCode());
    }

    @Test
    void shouldRecordFailureOnSyncException() {
        triageExecutor.syncException = new IllegalStateException("test error");
        AiOrchestrator orch = createOrchestrator(List.of(triageExecutor));
        orch.triage(new TriageRequest()).join();
        assertTrue(metricsStore.getFailureRate("TRIAGE") > 0);
    }

    @Test
    void shouldRecordAiCallRecordOnSyncException() {
        // T20: metricsCollector.record() is called with proper AiCallRecord on exception
        final AiCallRecord[] captured = new AiCallRecord[1];
        metricsCollector = new AiMetricsCollector() {
            @Override
            public void record(AiCallRecord r) {
                captured[0] = r;
            }
        };
        triageExecutor.syncException = new IllegalStateException("test error");
        AiOrchestrator orch = createOrchestrator(List.of(triageExecutor));
        orch.triage(new TriageRequest()).join();
        assertNotNull(captured[0]);
        assertEquals("TRIAGE", captured[0].getCapabilityId());
        assertTrue(captured[0].isDegraded());
        assertEquals("IllegalStateException", captured[0].getDegradationReason());
    }

    @Test
    void shouldReturnExactCompletableFutureFromExecutor() {
        CompletableFuture<AiResult<Object>> specificFuture = new CompletableFuture<>();
        triageExecutor.result = specificFuture;
        AiOrchestrator orch = createOrchestrator(List.of(triageExecutor));
        CompletableFuture<AiResult<TriageResponse>> result = orch.triage(new TriageRequest());
        assertSame(specificFuture, result);
    }

    @Test
    void shouldPassthroughExceptionallyCompletedFuture() {
        CompletableFuture<AiResult<Object>> failedFuture = CompletableFuture.failedFuture(
            new RuntimeException("async error"));
        triageExecutor.result = failedFuture;
        AiOrchestrator orch = createOrchestrator(List.of(triageExecutor));
        CompletableFuture<AiResult<TriageResponse>> result = orch.triage(new TriageRequest());
        assertSame(failedFuture, result);
        assertThrows(Exception.class, result::join);
    }

    @Test
    void initExecutorMapShouldBuildCorrectMapping() {
        AiOrchestrator orch = createOrchestrator(List.of(triageExecutor, diagnosisExecutor));
        AiResult<TriageResponse> triageResult = orch.triage(new TriageRequest()).join();
        assertTrue(triageResult.isSuccess());
        AiResult<?> diagnosisResult = orch.diagnosis(new DiagnosisRequest()).join();
        assertTrue(diagnosisResult.isSuccess());
    }

    @Test
    void duplicateCapabilityIdShouldUseLastRegisteredExecutor() {
        TestCapabilityExecutor first = new TestCapabilityExecutor("TRIAGE") {
            @Override
            public CompletableFuture<AiResult<Object>> execute(Object request, String capabilityId) {
                return CompletableFuture.completedFuture(AiResult.success("first"));
            }
        };
        TestCapabilityExecutor second = new TestCapabilityExecutor("TRIAGE") {
            @Override
            public CompletableFuture<AiResult<Object>> execute(Object request, String capabilityId) {
                return CompletableFuture.completedFuture(AiResult.success("second"));
            }
        };
        AiOrchestrator orch = createOrchestrator(List.of(first, second));
        AiResult<TriageResponse> result = orch.triage(new TriageRequest()).join();
        assertTrue(result.isSuccess());
        assertEquals("second", result.getData());
    }

    @Test
    void shouldHandleConcurrentAccessToExecutorMap() throws Exception {
        // T35: ConcurrentHashMap handles concurrent access without data loss
        AiOrchestrator orch = createOrchestrator(List.of(triageExecutor, diagnosisExecutor));
        CompletableFuture.allOf(
            orch.triage(new TriageRequest()),
            orch.diagnosis(new DiagnosisRequest())
        ).join();
        assertTrue(triageExecutor.capturedRequest instanceof TriageRequest);
        assertTrue(diagnosisExecutor.capturedRequest instanceof DiagnosisRequest);
    }

    @Test
    void executorWithFailureResultShouldBeReturnedAsIs() {
        triageExecutor.result = CompletableFuture.completedFuture(AiResult.failure("BIZ_ERROR"));
        AiOrchestrator orch = createOrchestrator(List.of(triageExecutor));
        AiResult<TriageResponse> result = orch.triage(new TriageRequest()).join();
        assertFalse(result.isSuccess());
        assertEquals("BIZ_ERROR", result.getErrorCode());
    }

    static class TestCapabilityExecutor implements CapabilityExecutor<Object, Object> {

        final String capabilityId;
        Object capturedRequest;
        String capturedCapabilityId;
        CompletableFuture<AiResult<Object>> result = CompletableFuture.completedFuture(AiResult.success("ok"));
        RuntimeException syncException;

        TestCapabilityExecutor(String capabilityId) {
            this.capabilityId = capabilityId;
        }

        @Override
        public CompletableFuture<AiResult<Object>> execute(Object request, String capabilityId) {
            this.capturedRequest = request;
            this.capturedCapabilityId = capabilityId;
            if (syncException != null) {
                throw syncException;
            }
            return result;
        }

        @Override
        public String getCapabilityId() {
            return capabilityId;
        }

        @Override
        public Class<Object> getInputType() {
            return Object.class;
        }

        @Override
        public Class<Object> getOutputType() {
            return Object.class;
        }
    }
}
