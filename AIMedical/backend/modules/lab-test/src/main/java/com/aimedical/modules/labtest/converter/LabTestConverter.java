package com.aimedical.modules.labtest.converter;

import com.aimedical.modules.labtest.dto.LabTestDTO;
import com.aimedical.modules.labtest.dto.LabTestItemDTO;
import com.aimedical.modules.labtest.entity.LabTest;
import com.aimedical.modules.labtest.entity.LabTestItem;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 检验域实体与 DTO 转换工具类。
 */
public final class LabTestConverter {

    private LabTestConverter() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static LabTestDTO toDTO(LabTest entity, List<LabTestItem> items) {
        if (entity == null) {
            return null;
        }
        LabTestDTO dto = new LabTestDTO();
        dto.setId(entity.getId());
        dto.setPatientId(entity.getPatientId());
        dto.setDoctorId(entity.getDoctorId());
        dto.setTestType(entity.getTestType());
        dto.setSampleType(entity.getSampleType());
        dto.setCollectedAt(entity.getCollectedAt());
        dto.setStatus(entity.getStatus());
        dto.setReportConclusion(entity.getReportConclusion());
        dto.setAiInterpretation(entity.getAiInterpretation());
        dto.setReportedAt(entity.getReportedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setItems(toItemDtoList(items));
        return dto;
    }

    public static LabTestItemDTO toItemDTO(LabTestItem entity) {
        if (entity == null) {
            return null;
        }
        LabTestItemDTO dto = new LabTestItemDTO();
        dto.setId(entity.getId());
        dto.setLabTestId(entity.getLabTestId());
        dto.setItemName(entity.getItemName());
        dto.setResult(entity.getResult());
        dto.setUnit(entity.getUnit());
        dto.setReferenceRange(entity.getReferenceRange());
        dto.setAbnormalFlag(entity.getAbnormalFlag());
        return dto;
    }

    public static List<LabTestItemDTO> toItemDtoList(List<LabTestItem> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream().map(LabTestConverter::toItemDTO).collect(Collectors.toList());
    }
}
