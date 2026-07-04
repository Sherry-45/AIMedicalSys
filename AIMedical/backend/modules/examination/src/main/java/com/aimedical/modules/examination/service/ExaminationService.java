package com.aimedical.modules.examination.service;

import com.aimedical.common.result.PageResponse;
import com.aimedical.modules.examination.dto.ExaminationCompleteRequest;
import com.aimedical.modules.examination.dto.ExaminationCreateRequest;
import com.aimedical.modules.examination.dto.ExaminationDTO;
import com.aimedical.modules.examination.dto.ExaminationQueryRequest;
import com.aimedical.modules.examination.dto.ExecutionOrderDTO;

import java.time.LocalDateTime;

public interface ExaminationService {

    ExaminationDTO createExamination(ExaminationCreateRequest request);

    ExaminationDTO scheduleExamination(Long id, LocalDateTime scheduledAt);

    ExaminationDTO startExamination(Long id);

    ExaminationDTO completeExamination(Long id, ExaminationCompleteRequest request);

    ExaminationDTO cancelExamination(Long id);

    ExaminationDTO getExamination(Long id);

    PageResponse<ExaminationDTO> getExaminations(ExaminationQueryRequest query);

    ExaminationDTO generateAiReport(Long id);

    ExaminationDTO analyzeImage(Long id);

    ExecutionOrderDTO recommendExecutionOrder(Long patientId);
}
