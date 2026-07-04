package com.aimedical.modules.doctor.service;

import com.aimedical.common.result.Result;
import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisRequest;
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisResponse;
import com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendRequest;
import com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendResponse;
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

/**
 * 医生端 AI 服务（带降级包装）。
 *
 * <p>所有方法返回 {@link AiResult}，当 AI 不可用时显式返回降级结果与兜底数据，
 * 前端据此展示降级标识 UI。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface DoctorAiService {

    Result<AiResult<DiagnosisResponse>> diagnosis(DiagnosisRequest request, Long doctorUserId);

    Result<AiResult<ExaminationRecommendResponse>> recommendExamination(ExaminationRecommendRequest request, Long doctorUserId);

    Result<AiResult<AiPrescriptionAssistResponse>> prescriptionAssist(AiPrescriptionAssistRequest request, Long doctorUserId);

    Result<AiResult<AiPrescriptionAuditResponse>> prescriptionAudit(AiPrescriptionAuditRequest request, Long doctorUserId);

    Result<AiResult<AiMedicalRecordGenResponse>> generateMedicalRecord(AiMedicalRecordGenRequest request, Long doctorUserId);

    /**
     * 检查报告生成（§3.4.5）。
     */
    Result<AiResult<AiInspectionReportResponse>> generateInspectionReport(AiInspectionReportRequest request, Long doctorUserId);

    /**
     * 影像分析（§3.4.7）。
     */
    Result<AiResult<AiImageAnalysisResponse>> imageAnalysis(AiImageAnalysisRequest request, Long doctorUserId);

    /**
     * 执行顺序推荐（§3.4.11）。
     */
    Result<AiResult<AiExecutionOrderResponse>> recommendExecutionOrder(AiExecutionOrderRequest request, Long doctorUserId);

    /**
     * 讨论结论生成（§3.4.12）。
     */
    Result<AiResult<AiDiscussionConclusionResponse>> discussionConclusion(AiDiscussionConclusionRequest request, Long doctorUserId);
}
