package com.aimedical.modules.ai.impl.orchestrator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

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
import com.aimedical.modules.ai.impl.metrics.AiCallRecord;
import com.aimedical.modules.ai.impl.metrics.AiMetricsCollector;
import com.aimedical.modules.ai.impl.metrics.SlidingWindowMetricsStore;

import jakarta.annotation.PostConstruct;

@Service
@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")
public class AiOrchestrator implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiOrchestrator.class);

    private final List<CapabilityExecutor<?, ?>> executorList;
    private final SlidingWindowMetricsStore metricsStore;
    private final AiMetricsCollector metricsCollector;
    private ConcurrentHashMap<String, CapabilityExecutor<?, ?>> executorMap;

    public AiOrchestrator(
        List<CapabilityExecutor<?, ?>> executorList,
        SlidingWindowMetricsStore metricsStore,
        AiMetricsCollector metricsCollector
    ) {
        this.executorList = executorList;
        this.metricsStore = metricsStore;
        this.metricsCollector = metricsCollector;
    }

    @PostConstruct
    void initExecutorMap() {
        ConcurrentHashMap<String, CapabilityExecutor<?, ?>> map = new ConcurrentHashMap<>();
        for (CapabilityExecutor<?, ?> executor : executorList) {
            String capabilityId = executor.getCapabilityId();
            CapabilityExecutor<?, ?> old = map.put(capabilityId, executor);
            if (old != null) {
                log.warn("重复 capabilityId 被覆盖: capabilityId={}, oldExecutor={}, newExecutor={}",
                    capabilityId, old.getClass().getName(), executor.getClass().getName());
            }
        }
        this.executorMap = map;
    }

    @Override
    public CompletableFuture<AiResult<TriageResponse>> triage(TriageRequest request) {
        return handle("TRIAGE", request);
    }

    @Override
    public CompletableFuture<AiResult<DiagnosisResponse>> diagnosis(DiagnosisRequest request) {
        return handle("DIAGNOSIS", request);
    }

    @Override
    public CompletableFuture<AiResult<PrescriptionCheckResponse>> prescriptionCheck(PrescriptionCheckRequest request) {
        return handle("RX_AUDIT", request);
    }

    @Override
    public CompletableFuture<AiResult<MedicalRecordGenResponse>> generateMedicalRecord(MedicalRecordGenRequest request) {
        return handle("MEDICAL_RECORD_GEN", request);
    }

    @Override
    public CompletableFuture<AiResult<InspectionReportResponse>> analysisReportForInspection(InspectionReportRequest request) {
        return handle("ANALYSIS_REPORT_INSPECTION", request);
    }

    @Override
    public CompletableFuture<AiResult<LabTestReportResponse>> analysisReportForLabTest(LabTestReportRequest request) {
        return handle("ANALYSIS_REPORT_LABTEST", request);
    }

    @Override
    public CompletableFuture<AiResult<ImageAnalysisResponse>> imageAnalysis(ImageAnalysisRequest request) {
        return handle("IMAGE_ANALYSIS", request);
    }

    @Override
    public CompletableFuture<AiResult<KbQueryResponse>> knowledgeBaseQuery(KbQueryRequest request) {
        return handle("KB_QUERY", request);
    }

    @Override
    public CompletableFuture<AiResult<ExaminationRecommendResponse>> recommendExamination(ExaminationRecommendRequest request) {
        return handle("RECOMMEND_EXAM", request);
    }

    @Override
    public CompletableFuture<AiResult<PrescriptionAssistResponse>> prescriptionAssist(PrescriptionAssistRequest request) {
        return handle("RX_ASSIST", request);
    }

    @Override
    public CompletableFuture<AiResult<ExecutionOrderResponse>> recommendExecutionOrder(ExecutionOrderRequest request) {
        return handle("RECOMMEND_EXEC_ORDER", request);
    }

    @Override
    public CompletableFuture<AiResult<ScheduleResponse>> schedule(ScheduleRequest request) {
        return handle("SCHEDULE", request);
    }

    @Override
    public CompletableFuture<AiResult<DiscussionConclusionResponse>> discussionConclusion(DiscussionConclusionRequest request) {
        return handle("DISCUSSION_CONCLUSION", request);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> CompletableFuture<AiResult<T>> handle(String capabilityId, Object request) {
        long startTime = System.currentTimeMillis();
        CapabilityExecutor executor = executorMap.get(capabilityId);
        if (executor == null) {
            throw new IllegalArgumentException("未注册能力标识: " + capabilityId);
        }
        try {
            return executor.execute(request, capabilityId);
        } catch (Exception e) {
            log.error("执行 capability 时发生异常: capabilityId={}", capabilityId, e);
            metricsStore.recordFailure(capabilityId);
            metricsCollector.record(new AiCallRecord(
                capabilityId, null, null, null, null, null, null, null, null, null,
                System.currentTimeMillis() - startTime, true, e.getClass().getSimpleName(), 0, 0));
            return CompletableFuture.completedFuture(AiResult.failure("AI服务暂时不可用，请稍后重试"));
        }
    }
}
