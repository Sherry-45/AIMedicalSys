package com.aimedical.modules.examination.converter;

import com.aimedical.modules.examination.dto.ExaminationDTO;
import com.aimedical.modules.examination.dto.ExaminationItemDTO;
import com.aimedical.modules.examination.entity.Examination;
import com.aimedical.modules.examination.entity.ExaminationItem;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ExaminationConverter {

    private ExaminationConverter() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ExaminationDTO toDTO(Examination examination, List<ExaminationItem> items) {
        if (examination == null) {
            return null;
        }
        ExaminationDTO dto = new ExaminationDTO();
        dto.setId(examination.getId());
        dto.setPatientId(examination.getPatientId());
        dto.setDoctorId(examination.getDoctorId());
        dto.setExaminationType(examination.getExaminationType());
        dto.setBodyPart(examination.getBodyPart());
        dto.setClinicalDiagnosis(examination.getClinicalDiagnosis());
        dto.setScheduledAt(examination.getScheduledAt());
        dto.setStatus(examination.getStatus());
        dto.setEmergencyFlag(examination.getEmergencyFlag());
        dto.setImageUrl(examination.getImageUrl());
        dto.setImageType(examination.getImageType());
        dto.setImpression(examination.getImpression());
        dto.setConclusion(examination.getConclusion());
        dto.setAiInterpretation(examination.getAiInterpretation());
        dto.setAiConfidence(examination.getAiConfidence());
        dto.setImageAnalysisResult(examination.getImageAnalysisResult());
        dto.setImageConfidence(examination.getImageConfidence());
        dto.setReportedAt(examination.getReportedAt());
        dto.setCreatedAt(examination.getCreatedAt());
        dto.setUpdatedAt(examination.getUpdatedAt());
        if (items != null) {
            dto.setItems(items.stream()
                    .map(ExaminationConverter::toItemDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setItems(Collections.emptyList());
        }
        return dto;
    }

    public static ExaminationItemDTO toItemDTO(ExaminationItem item) {
        if (item == null) {
            return null;
        }
        ExaminationItemDTO dto = new ExaminationItemDTO();
        dto.setId(item.getId());
        dto.setExaminationId(item.getExaminationId());
        dto.setItemName(item.getItemName());
        dto.setFinding(item.getFinding());
        dto.setMeasurement(item.getMeasurement());
        dto.setAbnormalFlag(item.getAbnormalFlag());
        return dto;
    }
}
