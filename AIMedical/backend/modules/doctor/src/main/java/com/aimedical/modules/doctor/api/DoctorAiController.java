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
import com.aimedical.modules.doctor.service.DoctorAiService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 医生端 AI 控制器。
 *
 * <p>提供 9 个 AI 入口：占位诊断、开立检查、辅助开方、处方审核、病历生成、
 * 检查报告生成、影像分析、执行顺序推荐、讨论结论生成。
 * 所有接口返回 {@link AiResult}，AI 不可用时显式返回降级结果与兜底数据，
 * 前端据此展示降级标识 UI。全部需要 DOCTOR 角色。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/doctor/ai")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorAiController {

    private final DoctorAiService doctorAiService;
    private final CurrentUser currentUser;

    public DoctorAiController(DoctorAiService doctorAiService, CurrentUser currentUser) {
        this.doctorAiService = doctorAiService;
        this.currentUser = currentUser;
    }

    /**
     * 占位诊断入口。
     */
    @PostMapping("/diagnosis")
    public Result<AiResult<DiagnosisResponse>> diagnosis(@Valid @RequestBody DiagnosisRequest request) {
        return doctorAiService.diagnosis(request, currentDoctorId());
    }

    /**
     * 开立检查推荐入口。
     */
    @PostMapping("/examination")
    public Result<AiResult<ExaminationRecommendResponse>> recommendExamination(@Valid @RequestBody ExaminationRecommendRequest request) {
        return doctorAiService.recommendExamination(request, currentDoctorId());
    }

    /**
     * 辅助开方入口。
     */
    @PostMapping("/prescription-assist")
    public Result<AiResult<AiPrescriptionAssistResponse>> prescriptionAssist(@Valid @RequestBody AiPrescriptionAssistRequest request) {
        return doctorAiService.prescriptionAssist(request, currentDoctorId());
    }

    /**
     * 处方审核入口。
     */
    @PostMapping("/prescription-audit")
    public Result<AiResult<AiPrescriptionAuditResponse>> prescriptionAudit(@Valid @RequestBody AiPrescriptionAuditRequest request) {
        return doctorAiService.prescriptionAudit(request, currentDoctorId());
    }

    /**
     * 病历生成入口。
     */
    @PostMapping("/medical-record-gen")
    public Result<AiResult<AiMedicalRecordGenResponse>> generateMedicalRecord(@Valid @RequestBody AiMedicalRecordGenRequest request) {
        return doctorAiService.generateMedicalRecord(request, currentDoctorId());
    }

    /**
     * 检查报告生成入口（§3.4.5）。
     */
    @PostMapping("/inspection-report")
    public Result<AiResult<AiInspectionReportResponse>> generateInspectionReport(@Valid @RequestBody AiInspectionReportRequest request) {
        return doctorAiService.generateInspectionReport(request, currentDoctorId());
    }

    /**
     * 影像分析入口（§3.4.7）。
     */
    @PostMapping("/image-analysis")
    public Result<AiResult<AiImageAnalysisResponse>> imageAnalysis(@Valid @RequestBody AiImageAnalysisRequest request) {
        return doctorAiService.imageAnalysis(request, currentDoctorId());
    }

    /**
     * 执行顺序推荐入口（§3.4.11）。
     */
    @PostMapping("/execution-order")
    public Result<AiResult<AiExecutionOrderResponse>> recommendExecutionOrder(@Valid @RequestBody AiExecutionOrderRequest request) {
        return doctorAiService.recommendExecutionOrder(request, currentDoctorId());
    }

    /**
     * 讨论结论生成入口（§3.4.12）。
     */
    @PostMapping("/discussion-conclusion")
    public Result<AiResult<AiDiscussionConclusionResponse>> discussionConclusion(@Valid @RequestBody AiDiscussionConclusionRequest request) {
        return doctorAiService.discussionConclusion(request, currentDoctorId());
    }

    private Long currentDoctorId() {
        Long userId = currentUser.getUserId();
        if (userId == null) {
            throw new IllegalStateException("无法获取当前登录医生ID");
        }
        return userId;
    }
}
