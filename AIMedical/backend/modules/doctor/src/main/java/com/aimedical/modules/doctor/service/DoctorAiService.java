package com.aimedical.modules.doctor.service;

import com.aimedical.common.result.Result;
import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisRequest;
import com.aimedical.modules.ai.api.dto.diagnosis.DiagnosisResponse;
import com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendRequest;
import com.aimedical.modules.ai.api.dto.examination.ExaminationRecommendResponse;
import com.aimedical.modules.doctor.dto.request.AiMedicalRecordGenRequest;
import com.aimedical.modules.doctor.dto.request.AiPrescriptionAssistRequest;
import com.aimedical.modules.doctor.dto.request.AiPrescriptionAuditRequest;
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
}
