package com.aimedical.modules.ai.impl.fallback;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.context.annotation.Primary;
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

/**
 * AI 服务降级包装实现。
 *
 * <p>采用 {@code @Primary} + 单委托模式：通过 {@link ObjectProvider} 注入
 * {@code aiOrchestrator} 限定的 {@link AiService} 实现作为主委托；
 * AI 不可用时直接返回降级结果。
 *
 * <p>{@code @ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")}
 * 确保仅在 AI 平台启用时激活本 Bean。
 *
 * <p>{@code @Primary} 确保 DoctorAiServiceImpl 注入的是本类而非具体实现，
 * 形成双重锁定降级（本类委托判定 + DoctorAiServiceImpl 兜底捕获）。
 */
@Service
@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")
@Primary
public class FallbackAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(FallbackAiService.class);

    private final AiService delegate;

    public FallbackAiService(@org.springframework.beans.factory.annotation.Qualifier("aiOrchestrator") ObjectProvider<AiService> delegateProvider) {
        AiService candidate = delegateProvider.getIfAvailable();
        if (candidate == null) {
            this.delegate = null;
            log.warn("AiOrchestrator 未找到，FallbackAiService 将直接处理所有请求");
        } else {
            this.delegate = candidate;
        }
    }

    private <T> CompletableFuture<AiResult<T>> handleEmptyDelegates() {
        log.warn("No available AiService delegate");
        return CompletableFuture.completedFuture(AiResult.degraded("No available AiService delegate"));
    }

    @Override
    public CompletableFuture<AiResult<TriageResponse>> triage(TriageRequest request) {
        if (delegate == null) {
            return handleEmptyDelegates();
        }
        return delegate.triage(request);
    }

    @Override
    public CompletableFuture<AiResult<DiagnosisResponse>> diagnosis(DiagnosisRequest request) {
        if (delegate == null) {
            return handleEmptyDelegates();
        }
        return delegate.diagnosis(request);
    }

    @Override
    public CompletableFuture<AiResult<PrescriptionCheckResponse>> prescriptionCheck(PrescriptionCheckRequest request) {
        if (delegate == null) {
            return handleEmptyDelegates();
        }
        return delegate.prescriptionCheck(request);
    }

    @Override
    public CompletableFuture<AiResult<MedicalRecordGenResponse>> generateMedicalRecord(MedicalRecordGenRequest request) {
        if (delegate == null) {
            return handleEmptyDelegates();
        }
        return delegate.generateMedicalRecord(request);
    }

    @Override
    public CompletableFuture<AiResult<InspectionReportResponse>> analysisReportForInspection(InspectionReportRequest request) {
        if (delegate == null) {
            return handleEmptyDelegates();
        }
        return delegate.analysisReportForInspection(request);
    }

    @Override
    public CompletableFuture<AiResult<LabTestReportResponse>> analysisReportForLabTest(LabTestReportRequest request) {
        if (delegate == null) {
            return handleEmptyDelegates();
        }
        return delegate.analysisReportForLabTest(request);
    }

    @Override
    public CompletableFuture<AiResult<ImageAnalysisResponse>> imageAnalysis(ImageAnalysisRequest request) {
        if (delegate == null) {
            return handleEmptyDelegates();
        }
        return delegate.imageAnalysis(request);
    }

    @Override
    public CompletableFuture<AiResult<KbQueryResponse>> knowledgeBaseQuery(KbQueryRequest request) {
        if (delegate == null) {
            return handleEmptyDelegates();
        }
        return delegate.knowledgeBaseQuery(request);
    }

    @Override
    public CompletableFuture<AiResult<ExaminationRecommendResponse>> recommendExamination(ExaminationRecommendRequest request) {
        if (delegate == null) {
            return handleEmptyDelegates();
        }
        return delegate.recommendExamination(request);
    }

    @Override
    public CompletableFuture<AiResult<PrescriptionAssistResponse>> prescriptionAssist(PrescriptionAssistRequest request) {
        if (delegate == null) {
            return handleEmptyDelegates();
        }
        return delegate.prescriptionAssist(request);
    }

    @Override
    public CompletableFuture<AiResult<ExecutionOrderResponse>> recommendExecutionOrder(ExecutionOrderRequest request) {
        if (delegate == null) {
            return handleEmptyDelegates();
        }
        return delegate.recommendExecutionOrder(request);
    }

    @Override
    public CompletableFuture<AiResult<ScheduleResponse>> schedule(ScheduleRequest request) {
        if (delegate == null) {
            return handleEmptyDelegates();
        }
        return delegate.schedule(request);
    }

    @Override
    public CompletableFuture<AiResult<DiscussionConclusionResponse>> discussionConclusion(DiscussionConclusionRequest request) {
        if (delegate == null) {
            return handleEmptyDelegates();
        }
        return delegate.discussionConclusion(request);
    }
}
