package com.aimedical.modules.doctor.service.impl;

import com.aimedical.common.result.Result;
import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.AiService;
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisRequest;
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisResponse;
import com.aimedical.modules.ai.api.dto.discussion.DiscussionConclusionRequest;
import com.aimedical.modules.ai.api.dto.discussion.DiscussionConclusionResponse;
import com.aimedical.modules.ai.api.dto.discussion.DiscussionTranscript;
import com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendRequest;
import com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendResponse;
import com.aimedical.modules.ai.api.dto.execution.ExecutionOrderRequest;
import com.aimedical.modules.ai.api.dto.execution.ExecutionOrderResponse;
import com.aimedical.modules.ai.api.dto.image.ImageAnalysisRequest;
import com.aimedical.modules.ai.api.dto.image.ImageAnalysisResponse;
import com.aimedical.modules.ai.api.dto.inspection.InspectionReportRequest;
import com.aimedical.modules.ai.api.dto.inspection.InspectionReportResponse;
import com.aimedical.modules.ai.api.dto.medicalrecord.MedicalRecordGenResponse;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionAssistResponse;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 医生端 AI 服务实现（带降级包装）。
 *
 * <p>降级策略：
 * <ul>
 *   <li>当 {@code ai.doctor.mock-degrade=true}（默认）时，直接返回降级结果与兜底数据，
 *       用于演示 AI 不可用时的降级 UI 路径</li>
 *   <li>当 {@code ai.doctor.mock-degrade=false} 时，实际调用 {@link AiService}；
 *       调用异常或返回降级结果时，统一包装为 {@link AiResult#degraded}</li>
 * </ul>
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Service
public class DoctorAiServiceImpl implements DoctorAiService {

    private static final Logger log = LoggerFactory.getLogger(DoctorAiServiceImpl.class);

    private final AiService aiService;

    @Value("${ai.doctor.mock-degrade:true}")
    private boolean mockDegrade;

    public DoctorAiServiceImpl(AiService aiService) {
        this.aiService = aiService;
    }

    @Override
    public Result<AiResult<DiagnosisResponse>> diagnosis(DiagnosisRequest request, Long doctorUserId) {
        if (mockDegrade) {
            return Result.success(degradedDiagnosis());
        }
        try {
            AiResult<DiagnosisResponse> result = aiService.diagnosis(request).join();
            if (result.isDegraded() || !result.isSuccess()) {
                return Result.success(degradedDiagnosis());
            }
            DiagnosisResponse aiData = result.getData();
            if (aiData == null) {
                return Result.success(degradedDiagnosis());
            }
            return Result.success(AiResult.success(aiData));
        } catch (Exception e) {
            log.warn("AI diagnosis 调用异常，降级处理", e);
            return Result.success(degradedDiagnosis());
        }
    }

    @Override
    public Result<AiResult<ExaminationRecommendResponse>> recommendExamination(ExaminationRecommendRequest request, Long doctorUserId) {
        if (mockDegrade) {
            return Result.success(degradedExamination());
        }
        try {
            AiResult<ExaminationRecommendResponse> result = aiService.recommendExamination(request).join();
            if (result.isDegraded() || !result.isSuccess()) {
                return Result.success(degradedExamination());
            }
            ExaminationRecommendResponse aiData = result.getData();
            if (aiData == null) {
                return Result.success(degradedExamination());
            }
            return Result.success(AiResult.success(aiData));
        } catch (Exception e) {
            log.warn("AI recommendExamination 调用异常，降级处理", e);
            return Result.success(degradedExamination());
        }
    }

    @Override
    public Result<AiResult<AiPrescriptionAssistResponse>> prescriptionAssist(AiPrescriptionAssistRequest request, Long doctorUserId) {
        if (mockDegrade) {
            return Result.success(degradedPrescriptionAssist());
        }
        try {
            com.aimedical.modules.ai.api.dto.prescription.PrescriptionAssistRequest aiReq =
                    new com.aimedical.modules.ai.api.dto.prescription.PrescriptionAssistRequest();
            aiReq.setDiagnosis(request.diagnosis());
            aiReq.setPrescriptionId(request.patientId() != null ? String.valueOf(request.patientId()) : null);
            AiResult<PrescriptionAssistResponse> result = aiService.prescriptionAssist(aiReq).join();
            if (result.isDegraded() || !result.isSuccess()) {
                return Result.success(degradedPrescriptionAssist());
            }
            PrescriptionAssistResponse aiData = result.getData();
            if (aiData == null) {
                return Result.success(degradedPrescriptionAssist());
            }
            AiPrescriptionAssistResponse data = new AiPrescriptionAssistResponse(List.of(),
                    aiData.getPrescriptionDraft() != null ? aiData.getPrescriptionDraft() : "");
            return Result.success(AiResult.success(data));
        } catch (Exception e) {
            log.warn("AI prescriptionAssist 调用异常，降级处理", e);
            return Result.success(degradedPrescriptionAssist());
        }
    }

    @Override
    public Result<AiResult<AiPrescriptionAuditResponse>> prescriptionAudit(AiPrescriptionAuditRequest request, Long doctorUserId) {
        if (mockDegrade) {
            return Result.success(degradedPrescriptionAudit());
        }
        try {
            com.aimedical.modules.ai.api.dto.prescription.PrescriptionCheckRequest aiReq =
                    new com.aimedical.modules.ai.api.dto.prescription.PrescriptionCheckRequest();
            aiReq.setPrescriptionId(request.prescriptionId() != null ? String.valueOf(request.prescriptionId()) : null);
            AiResult<PrescriptionCheckResponse> result = aiService.prescriptionCheck(aiReq).join();
            if (result.isDegraded() || !result.isSuccess()) {
                return Result.success(degradedPrescriptionAudit());
            }
            PrescriptionCheckResponse aiData = result.getData();
            if (aiData == null) {
                return Result.success(degradedPrescriptionAudit());
            }
            List<String> warnings = aiData.getAlerts() == null
                    ? List.of()
                    : aiData.getAlerts().stream()
                            .map(a -> a.getAlertMessage() != null ? a.getAlertMessage() : "")
                            .toList();
            AiPrescriptionAuditResponse data = new AiPrescriptionAuditResponse(
                    parseRiskLevel(aiData.getRiskLevel()),
                    warnings,
                    !aiData.isFromFallback());
            return Result.success(AiResult.success(data));
        } catch (Exception e) {
            log.warn("AI prescriptionCheck 调用异常，降级处理", e);
            return Result.success(degradedPrescriptionAudit());
        }
    }

    @Override
    public Result<AiResult<AiMedicalRecordGenResponse>> generateMedicalRecord(AiMedicalRecordGenRequest request, Long doctorUserId) {
        if (mockDegrade) {
            return Result.success(degradedMedicalRecordGen());
        }
        try {
            com.aimedical.modules.ai.api.dto.medicalrecord.MedicalRecordGenRequest aiReq =
                    new com.aimedical.modules.ai.api.dto.medicalrecord.MedicalRecordGenRequest();
            aiReq.setPatientId(request.patientId() != null ? String.valueOf(request.patientId()) : null);
            StringBuilder dialogue = new StringBuilder();
            if (request.chiefComplaint() != null) dialogue.append("主诉: ").append(request.chiefComplaint()).append("\n");
            if (request.presentIllness() != null) dialogue.append("现病史: ").append(request.presentIllness()).append("\n");
            if (request.pastHistory() != null) dialogue.append("既往史: ").append(request.pastHistory()).append("\n");
            if (request.diagnosis() != null) dialogue.append("诊断: ").append(request.diagnosis());
            aiReq.setDialogueText(dialogue.toString());
            AiResult<MedicalRecordGenResponse> result = aiService.generateMedicalRecord(aiReq).join();
            if (result.isDegraded() || !result.isSuccess()) {
                return Result.success(degradedMedicalRecordGen());
            }
            MedicalRecordGenResponse aiData = result.getData();
            if (aiData == null) {
                return Result.success(degradedMedicalRecordGen());
            }
            AiMedicalRecordGenResponse data = new AiMedicalRecordGenResponse(
                    nullToEmpty(aiData.getChiefComplaint()),
                    nullToEmpty(aiData.getPresentIllness()),
                    nullToEmpty(aiData.getPastHistory()),
                    nullToEmpty(aiData.getPreliminaryDiagnosis()),
                    nullToEmpty(aiData.getTreatmentPlan()));
            return Result.success(AiResult.success(data));
        } catch (Exception e) {
            log.warn("AI generateMedicalRecord 调用异常，降级处理", e);
            return Result.success(degradedMedicalRecordGen());
        }
    }

    @Override
    public Result<AiResult<AiInspectionReportResponse>> generateInspectionReport(AiInspectionReportRequest request, Long doctorUserId) {
        if (mockDegrade) {
            return Result.success(degradedInspectionReport());
        }
        try {
            InspectionReportRequest aiReq = new InspectionReportRequest();
            aiReq.setExaminationId(request.examinationId());
            aiReq.setExaminationType(request.examinationType());
            aiReq.setRawDataRef(request.rawDataRef());
            aiReq.setPatientId(request.patientId());
            aiReq.setDoctorId(doctorUserId);
            aiReq.setBodyPart(request.bodyPart());
            aiReq.setClinicalDiagnosis(request.clinicalDiagnosis());
            if (request.items() != null) {
                List<InspectionReportRequest.Item> items = request.items().stream()
                        .map(it -> {
                            InspectionReportRequest.Item item = new InspectionReportRequest.Item();
                            item.setItemName(it.itemName());
                            item.setValue(it.value());
                            item.setUnit(it.unit());
                            item.setReferenceRange(it.referenceRange());
                            item.setStatus(it.status());
                            return item;
                        })
                        .toList();
                aiReq.setItems(items);
            }
            AiResult<InspectionReportResponse> result = aiService.analysisReportForInspection(aiReq).join();
            if (result.isDegraded() || !result.isSuccess()) {
                return Result.success(degradedInspectionReport());
            }
            InspectionReportResponse aiData = result.getData();
            if (aiData == null) {
                return Result.success(degradedInspectionReport());
            }
            AiInspectionReportResponse data = mapInspectionReport(aiData);
            return Result.success(AiResult.success(data));
        } catch (Exception e) {
            log.warn("AI generateInspectionReport 调用异常，降级处理", e);
            return Result.success(degradedInspectionReport());
        }
    }

    @Override
    public Result<AiResult<AiImageAnalysisResponse>> imageAnalysis(AiImageAnalysisRequest request, Long doctorUserId) {
        if (mockDegrade) {
            return Result.success(degradedImageAnalysis());
        }
        try {
            ImageAnalysisRequest aiReq = new ImageAnalysisRequest();
            aiReq.setImageRef(request.imageRef());
            aiReq.setModelId(request.modelId());
            aiReq.setPatientId(request.patientId());
            aiReq.setExaminationId(request.examinationId());
            aiReq.setExaminationType(request.examinationType());
            aiReq.setBodyPart(request.bodyPart());
            aiReq.setClinicalDiagnosis(request.clinicalDiagnosis());
            aiReq.setImageType(request.imageType());
            AiResult<ImageAnalysisResponse> result = aiService.imageAnalysis(aiReq).join();
            if (result.isDegraded() || !result.isSuccess()) {
                return Result.success(degradedImageAnalysis());
            }
            ImageAnalysisResponse aiData = result.getData();
            if (aiData == null) {
                return Result.success(degradedImageAnalysis());
            }
            AiImageAnalysisResponse data = mapImageAnalysis(aiData);
            return Result.success(AiResult.success(data));
        } catch (Exception e) {
            log.warn("AI imageAnalysis 调用异常，降级处理", e);
            return Result.success(degradedImageAnalysis());
        }
    }

    @Override
    public Result<AiResult<AiExecutionOrderResponse>> recommendExecutionOrder(AiExecutionOrderRequest request, Long doctorUserId) {
        if (mockDegrade) {
            return Result.success(degradedExecutionOrder(request));
        }
        try {
            ExecutionOrderRequest aiReq = new ExecutionOrderRequest();
            aiReq.setTaskRole(request.taskRole());
            if (request.taskItems() != null) {
                List<ExecutionOrderRequest.TaskItem> taskItems = request.taskItems().stream()
                        .map(it -> {
                            ExecutionOrderRequest.TaskItem item = new ExecutionOrderRequest.TaskItem();
                            item.setTaskId(it.taskId());
                            item.setTaskType(it.taskType());
                            item.setItemName(it.itemName());
                            item.setUrgencyHint(it.urgencyHint());
                            item.setPatientId(it.patientId());
                            return item;
                        })
                        .toList();
                aiReq.setTaskItems(taskItems);
            }
            if (request.context() != null) {
                ExecutionOrderRequest.Context ctx = new ExecutionOrderRequest.Context();
                ctx.setEncounterId(request.context().encounterId());
                ctx.setDepartmentId(request.context().departmentId());
                ctx.setAvailableResources(request.context().availableResources());
                aiReq.setContext(ctx);
            }
            AiResult<ExecutionOrderResponse> result = aiService.recommendExecutionOrder(aiReq).join();
            if (result.isDegraded() || !result.isSuccess()) {
                return Result.success(degradedExecutionOrder(request));
            }
            ExecutionOrderResponse aiData = result.getData();
            if (aiData == null) {
                return Result.success(degradedExecutionOrder(request));
            }
            AiExecutionOrderResponse data = mapExecutionOrder(aiData);
            return Result.success(AiResult.success(data));
        } catch (Exception e) {
            log.warn("AI recommendExecutionOrder 调用异常，降级处理", e);
            return Result.success(degradedExecutionOrder(request));
        }
    }

    @Override
    public Result<AiResult<AiDiscussionConclusionResponse>> discussionConclusion(AiDiscussionConclusionRequest request, Long doctorUserId) {
        if (mockDegrade) {
            return Result.success(degradedDiscussionConclusion());
        }
        try {
            DiscussionConclusionRequest aiReq = new DiscussionConclusionRequest();
            if (request.transcripts() != null) {
                List<DiscussionTranscript> transcripts = request.transcripts().stream()
                        .map(t -> {
                            DiscussionTranscript ts = new DiscussionTranscript();
                            ts.setSpeakerRole(t.speakerRole());
                            ts.setSpeakerName(t.speakerName());
                            ts.setTimestamp(t.timestamp());
                            ts.setContent(t.content());
                            return ts;
                        })
                        .toList();
                aiReq.setTranscripts(transcripts);
            }
            AiResult<DiscussionConclusionResponse> result = aiService.discussionConclusion(aiReq).join();
            if (result.isDegraded() || !result.isSuccess()) {
                return Result.success(degradedDiscussionConclusion());
            }
            // 当前 AI 能力响应为空对象，统一包装为带降级提示的结论摘要
            return Result.success(degradedDiscussionConclusion());
        } catch (Exception e) {
            log.warn("AI discussionConclusion 调用异常，降级处理", e);
            return Result.success(degradedDiscussionConclusion());
        }
    }

    // ---------- 工具方法 ----------

    /**
     * 将 AI 检查报告响应映射为医生端 DTO。
     */
    private static AiInspectionReportResponse mapInspectionReport(InspectionReportResponse aiData) {
        List<AiInspectionReportResponse.AbnormalItem> abnormalItems = aiData.getAbnormalItems() == null
                ? List.of()
                : aiData.getAbnormalItems().stream()
                        .map(a -> new AiInspectionReportResponse.AbnormalItem(
                                nullToEmpty(a.getItemName()),
                                nullToEmpty(a.getValue()),
                                nullToEmpty(a.getUnit()),
                                nullToEmpty(a.getStatus())))
                        .toList();
        AiInspectionReportResponse.ImageRecognition imgRec = null;
        if (aiData.getImageRecognition() != null) {
            imgRec = new AiInspectionReportResponse.ImageRecognition(
                    nullToEmpty(aiData.getImageRecognition().getModelId()),
                    nullToEmpty(aiData.getImageRecognition().getRecognitionSummary()),
                    aiData.getImageRecognition().getConfidence());
        }
        return new AiInspectionReportResponse(
                nullToEmpty(aiData.getReportDraft()),
                aiData.getFindings() == null ? List.of() : aiData.getFindings(),
                nullToEmpty(aiData.getImpression()),
                nullToEmpty(aiData.getAuxiliaryInterpretation()),
                abnormalItems,
                nullToEmpty(aiData.getComparisonSummary()),
                aiData.getConfidence(),
                imgRec);
    }

    /**
     * 将 AI 影像分析响应映射为医生端 DTO。
     */
    private static AiImageAnalysisResponse mapImageAnalysis(ImageAnalysisResponse aiData) {
        AiImageAnalysisResponse.RecognitionResult rec = null;
        if (aiData.getRecognitionResult() != null) {
            rec = new AiImageAnalysisResponse.RecognitionResult(
                    aiData.getRecognitionResult().getRegions() == null
                            ? List.of() : aiData.getRecognitionResult().getRegions(),
                    aiData.getRecognitionResult().getLabels() == null
                            ? List.of() : aiData.getRecognitionResult().getLabels(),
                    aiData.getRecognitionResult().getScores() == null
                            ? List.of() : aiData.getRecognitionResult().getScores());
        }
        return new AiImageAnalysisResponse(
                nullToEmpty(aiData.getModelId()),
                rec,
                nullToEmpty(aiData.getSegmentationMaskRef()),
                aiData.getConfidence(),
                nullToEmpty(aiData.getAuxiliaryAdvice()));
    }

    /**
     * 将 AI 执行顺序响应映射为医生端 DTO。
     */
    private static AiExecutionOrderResponse mapExecutionOrder(ExecutionOrderResponse aiData) {
        List<AiExecutionOrderResponse.OrderItem> orderItems = aiData.getExecutionOrder() == null
                ? List.of()
                : aiData.getExecutionOrder().stream()
                        .map(o -> new AiExecutionOrderResponse.OrderItem(
                                o.getTaskId(),
                                nullToEmpty(o.getPriority()),
                                nullToEmpty(o.getRecommendedTime()),
                                nullToEmpty(o.getReason())))
                        .toList();
        return new AiExecutionOrderResponse(
                orderItems,
                nullToEmpty(aiData.getSummary()),
                aiData.getDisclaimerRequired() == null ? Boolean.TRUE : aiData.getDisclaimerRequired());
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    /**
     * 将 AI 返回的风险等级字符串解析为 {@link AiRiskLevel} 枚举，
     * 无法匹配时返回 null（表示未知风险，由前端/审核人人工判断）。
     */
    private static AiRiskLevel parseRiskLevel(String code) {
        if (code == null) {
            return null;
        }
        for (AiRiskLevel level : AiRiskLevel.values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        return null;
    }

    // ---------- 降级兜底数据 ----------

    private AiResult<DiagnosisResponse> degradedDiagnosis() {
        DiagnosisResponse fallback = new DiagnosisResponse();
        fallback.setPossibleDiagnoses(List.of());
        fallback.setSummary("AI 诊断服务不可用，请医生根据主诉、现病史及查体进行人工诊断");
        return AiResult.degraded(
                fallback,
                "AI 诊断服务不可用，请医生根据主诉、现病史及查体进行人工诊断");
    }

    private AiResult<ExaminationRecommendResponse> degradedExamination() {
        // 兜底：提供常见检查项建议，避免完全无内容
        List<ExaminationRecommendResponse.ExaminationItem> fallbackItems = List.of(
                new ExaminationRecommendResponse.ExaminationItem("血常规", "检验", "通用基础检查"),
                new ExaminationRecommendResponse.ExaminationItem("尿常规", "检验", "通用基础检查"),
                new ExaminationRecommendResponse.ExaminationItem("心电图", "检查", "心血管基础筛查"),
                new ExaminationRecommendResponse.ExaminationItem("胸部X光", "检查", "呼吸系统基础筛查")
        );
        ExaminationRecommendResponse fallback = new ExaminationRecommendResponse();
        fallback.setItems(fallbackItems);
        return AiResult.degraded(
                fallback,
                "AI 检查推荐服务不可用，已返回通用检查项建议，请医生结合临床判断");
    }

    private AiResult<AiPrescriptionAssistResponse> degradedPrescriptionAssist() {
        return AiResult.degraded(
                new AiPrescriptionAssistResponse(List.of(), "AI 辅助开方服务不可用，请医生根据诊疗规范与药品说明书手动开具处方"),
                "AI 辅助开方服务不可用，请医生根据诊疗规范与药品说明书手动开具处方");
    }

    private AiResult<AiPrescriptionAuditResponse> degradedPrescriptionAudit() {
        return AiResult.degraded(
                new AiPrescriptionAuditResponse(null,
                        List.of("AI 处方审核服务不可用，请药师进行人工审核，重点关注过敏史、配伍禁忌与用法用量"),
                        false),
                "AI 处方审核服务不可用，请药师进行人工审核，重点关注过敏史、配伍禁忌与用法用量");
    }

    private AiResult<AiMedicalRecordGenResponse> degradedMedicalRecordGen() {
        return AiResult.degraded(
                new AiMedicalRecordGenResponse("", "", "", "", ""),
                "AI 病历生成服务不可用，请医生使用病历模板手动录入病历内容");
    }

    private AiResult<AiInspectionReportResponse> degradedInspectionReport() {
        AiInspectionReportResponse fallback = new AiInspectionReportResponse(
                "",
                List.of(),
                "",
                "AI 检查报告生成服务不可用，请医生根据原始数据手动撰写报告草稿",
                List.of(),
                "",
                null,
                null);
        return AiResult.degraded(
                fallback,
                "AI 检查报告生成服务不可用，请医生根据原始数据手动撰写报告草稿");
    }

    private AiResult<AiImageAnalysisResponse> degradedImageAnalysis() {
        AiImageAnalysisResponse fallback = new AiImageAnalysisResponse(
                "",
                null,
                "",
                null,
                "AI 影像分析服务不可用，请影像科医生进行人工判读");
        return AiResult.degraded(
                fallback,
                "AI 影像分析服务不可用，请影像科医生进行人工判读");
    }

    /**
     * 执行顺序降级兜底：按紧急度（HIGH > MEDIUM > LOW）+ 提交顺序排序，
     * 避免完全无内容，让医技端可继续操作。
     */
    private AiResult<AiExecutionOrderResponse> degradedExecutionOrder(AiExecutionOrderRequest request) {
        List<AiExecutionOrderResponse.OrderItem> fallbackOrder = new ArrayList<>();
        if (request.taskItems() != null) {
            // 简单降级策略：紧急度高的先执行；同紧急度按列表原序
            List<AiExecutionOrderRequest.TaskItem> sorted = new ArrayList<>(request.taskItems());
            sorted.sort((a, b) -> urgencyRank(b.urgencyHint()) - urgencyRank(a.urgencyHint()));
            int idx = 1;
            for (AiExecutionOrderRequest.TaskItem t : sorted) {
                fallbackOrder.add(new AiExecutionOrderResponse.OrderItem(
                        t.taskId(),
                        t.urgencyHint() != null ? t.urgencyHint() : "MEDIUM",
                        "按紧急度排序 #" + idx,
                        "AI 不可用，按紧急度降级排序"));
                idx++;
            }
        }
        AiExecutionOrderResponse fallback = new AiExecutionOrderResponse(
                fallbackOrder,
                "AI 执行顺序推荐服务不可用，已按紧急度降级排序",
                Boolean.TRUE);
        return AiResult.degraded(
                fallback,
                "AI 执行顺序推荐服务不可用，已按紧急度降级排序，请医技端结合临床判断");
    }

    private static int urgencyRank(String hint) {
        if (hint == null) return 1;
        return switch (hint.toUpperCase()) {
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 1;
        };
    }

    private AiResult<AiDiscussionConclusionResponse> degradedDiscussionConclusion() {
        AiDiscussionConclusionResponse fallback = new AiDiscussionConclusionResponse(
                "AI 讨论结论生成服务不可用，请主持人根据讨论记录人工归纳结论",
                "",
                "");
        return AiResult.degraded(
                fallback,
                "AI 讨论结论生成服务不可用，请主持人根据讨论记录人工归纳结论");
    }
}
