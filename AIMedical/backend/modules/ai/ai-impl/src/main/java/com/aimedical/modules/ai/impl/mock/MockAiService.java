package com.aimedical.modules.ai.impl.mock;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
import com.aimedical.modules.ai.api.dto.triage.RecommendedDepartment;
import com.aimedical.modules.ai.api.dto.triage.RecommendedDoctor;
import com.aimedical.modules.ai.api.dto.triage.TriageRequest;
import com.aimedical.modules.ai.api.dto.triage.TriageResponse;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "ai.mock.enabled", havingValue = "true", matchIfMissing = true)
public class MockAiService implements AiService {

    public enum ResponseStrategy {
        NORMAL, DECORATED, FALLBACK, STATIC, AI_UNAVAILABLE, TIMEOUT
    }

    private ResponseStrategy strategy = ResponseStrategy.NORMAL;

    public MockAiService() {}
    public MockAiService(String strategyName) { this.strategy = ResponseStrategy.valueOf(strategyName); }

    public ResponseStrategy getStrategy() { return strategy; }
    public void setStrategy(ResponseStrategy v) { this.strategy = v; }

    @Override
    public CompletableFuture<AiResult<TriageResponse>> triage(TriageRequest request) {
        TriageResponse response = new TriageResponse();
        response.setSessionId(request.getSessionId() != null
                ? request.getSessionId()
                : UUID.randomUUID().toString());

        if (request.getChiefComplaint() != null
                && request.getChiefComplaint().startsWith("degraded:")) {
            response.setComplete(true);
            response.setDegraded(true);
            response.setReason("AI 服务繁忙，已降级为常见分诊规则推荐");
            return CompletableFuture.completedFuture(
                    new AiResult<>(false, response, null, true, response.getReason()));
        }

        if (request.getAdditionalResponses() == null || request.getAdditionalResponses().isEmpty()) {
            response.setComplete(false);
            response.setQuestion("请问这个症状持续多久了？有没有其他伴随症状？之前是否因此就医或服用过药物？");
        } else {
            response.setComplete(true);
            RecommendedDepartment dept = new RecommendedDepartment();
            dept.setDepartmentId("1");
            dept.setDepartmentName("神经内科");
            dept.setScore(92f);
            response.setDepartments(List.of(dept));

            RecommendedDoctor doc = new RecommendedDoctor();
            doc.setDoctorId("101");
            doc.setDoctorName("王主任");
            doc.setAvailableSlotCount(5);
            doc.setScore(95f);
            response.setDoctors(List.of(doc));
            response.setReason("根据主诉综合分析，建议优先就诊神经内科以排除相关疾病");
        }
        return CompletableFuture.completedFuture(AiResult.success(response));
    }

    @Override
    public CompletableFuture<AiResult<DiagnosisResponse>> diagnosis(DiagnosisRequest request) {
        return CompletableFuture.completedFuture(AiResult.success(new DiagnosisResponse()));
    }

    @Override
    public CompletableFuture<AiResult<PrescriptionCheckResponse>> prescriptionCheck(PrescriptionCheckRequest request) {
        return CompletableFuture.completedFuture(AiResult.success(new PrescriptionCheckResponse()));
    }

    @Override
    public CompletableFuture<AiResult<MedicalRecordGenResponse>> generateMedicalRecord(MedicalRecordGenRequest request) {
        return CompletableFuture.completedFuture(AiResult.success(new MedicalRecordGenResponse()));
    }

    @Override
    public CompletableFuture<AiResult<InspectionReportResponse>> analysisReportForInspection(InspectionReportRequest request) {
        InspectionReportResponse response = new InspectionReportResponse();
        String type = request.getExaminationType() == null ? "未知" : request.getExaminationType();
        String part = request.getBodyPart() == null ? "" : request.getBodyPart();
        response.setReportDraft("[Mock] " + type + " 检查报告草稿：基于原始数据生成，未见明显异常。");
        response.setImpression("[Mock] " + type + " 检查 " + part + " 影像表现：未见明显异常。");
        response.setAuxiliaryInterpretation("[Mock] 结合临床诊断，辅助判读结论为未见明显异常。");
        response.setFindings(List.of("影像清晰", "未见占位性病变"));
        response.setAbnormalItems(List.of());
        response.setComparisonSummary("[Mock] 与历史结果对比无明显变化。");
        response.setConfidence(92.0);
        // image_recognition 缺省（Mock 不模拟内部 3.4.7 调用）
        return CompletableFuture.completedFuture(AiResult.success(response));
    }

    @Override
    public CompletableFuture<AiResult<LabTestReportResponse>> analysisReportForLabTest(LabTestReportRequest request) {
        LabTestReportResponse response = new LabTestReportResponse();
        String testType = request.getTestType() == null ? "常规检验" : request.getTestType();
        response.setReportDraft("[Mock] " + testType + " 检验报告草稿：依据原始结果数据生成，整体平稳。");
        response.setInterpretation("[Mock] " + testType + " 检验结果整体平稳，未见危急值。");
        response.setSuggestions(List.of("建议定期复查"));
        response.setConfidence(90.0);
        // 依据输入项构造异常项（status 非 NORMAL 视为异常）
        List<LabTestReportResponse.AbnormalItem> abnormalItems =
                (request.getItems() == null ? List.<LabTestReportRequest.Item>of() : request.getItems()).stream()
                        .filter(it -> it.getStatus() != null && !"NORMAL".equals(it.getStatus()))
                        .map(it -> {
                            LabTestReportResponse.AbnormalItem ai = new LabTestReportResponse.AbnormalItem();
                            ai.setItemName(it.getItemName());
                            ai.setValue(it.getValue());
                            ai.setUnit(it.getUnit());
                            ai.setReferenceRange(it.getReferenceRange());
                            ai.setStatus(it.getStatus());
                            ai.setDelta(null);
                            return ai;
                        })
                        .collect(java.util.stream.Collectors.toList());
        response.setAbnormalItems(abnormalItems);
        return CompletableFuture.completedFuture(AiResult.success(response));
    }

    @Override
    public CompletableFuture<AiResult<ImageAnalysisResponse>> imageAnalysis(ImageAnalysisRequest request) {
        ImageAnalysisResponse response = new ImageAnalysisResponse();
        String modelId = request.getModelId() == null ? "MOCK_MODEL" : request.getModelId();
        response.setModelId(modelId);
        ImageAnalysisResponse.RecognitionResult result = new ImageAnalysisResponse.RecognitionResult();
        result.setRegions(List.of("未见明显异常区域"));
        result.setLabels(List.of("normal"));
        result.setScores(List.of(0.92));
        response.setRecognitionResult(result);
        response.setConfidence(88.0);
        response.setAuxiliaryAdvice("[Mock] 建议结合临床进一步评估。");
        return CompletableFuture.completedFuture(AiResult.success(response));
    }

    @Override
    public CompletableFuture<AiResult<KbQueryResponse>> knowledgeBaseQuery(KbQueryRequest request) {
        return CompletableFuture.completedFuture(AiResult.success(new KbQueryResponse()));
    }

    @Override
    public CompletableFuture<AiResult<ExaminationRecommendResponse>> recommendExamination(ExaminationRecommendRequest request) {
        return CompletableFuture.completedFuture(AiResult.success(new ExaminationRecommendResponse()));
    }

    @Override
    public CompletableFuture<AiResult<PrescriptionAssistResponse>> prescriptionAssist(PrescriptionAssistRequest request) {
        return CompletableFuture.completedFuture(AiResult.success(new PrescriptionAssistResponse()));
    }

    @Override
    public CompletableFuture<AiResult<ExecutionOrderResponse>> recommendExecutionOrder(ExecutionOrderRequest request) {
        ExecutionOrderResponse response = new ExecutionOrderResponse();
        List<ExecutionOrderRequest.TaskItem> tasks = request.getTaskItems() == null
                ? List.of()
                : request.getTaskItems();
        // 按紧急度排序：HIGH > MEDIUM > LOW，同级别保持原序（稳定排序）
        List<ExecutionOrderResponse.OrderItem> order = new java.util.ArrayList<>();
        for (ExecutionOrderRequest.TaskItem task : tasks) {
            ExecutionOrderResponse.OrderItem item = new ExecutionOrderResponse.OrderItem();
            item.setTaskId(task.getTaskId());
            String urgency = task.getUrgencyHint() == null ? "MEDIUM" : task.getUrgencyHint();
            switch (urgency) {
                case "HIGH":
                    item.setPriority("P1");
                    break;
                case "MEDIUM":
                    item.setPriority("P2");
                    break;
                default:
                    item.setPriority("P3");
                    break;
            }
            item.setRecommendedTime(null);
            item.setReason("[Mock] 紧急度 " + urgency + " 对应优先级 " + item.getPriority());
            order.add(item);
        }
        // 按 P1 > P2 > P3 排序
        order.sort(java.util.Comparator.comparingInt(o -> {
            switch (o.getPriority()) {
                case "P1": return 1;
                case "P2": return 2;
                default: return 3;
            }
        }));
        response.setExecutionOrder(order);
        response.setSummary("[Mock] 按紧急度优先级排序，共 " + order.size() + " 项任务。");
        response.setDisclaimerRequired(true);
        return CompletableFuture.completedFuture(AiResult.success(response));
    }

    @Override
    public CompletableFuture<AiResult<ScheduleResponse>> schedule(ScheduleRequest request) {
        return CompletableFuture.completedFuture(AiResult.success(new ScheduleResponse()));
    }

    @Override
    public CompletableFuture<AiResult<DiscussionConclusionResponse>> discussionConclusion(DiscussionConclusionRequest request) {
        return CompletableFuture.completedFuture(AiResult.success(new DiscussionConclusionResponse()));
    }
}
