package com.aimedical.modules.patient.converter;

import com.aimedical.modules.patient.dto.health.HealthRecordResponse;
import com.aimedical.modules.patient.entity.HealthRecordCategory;
import com.aimedical.modules.patient.entity.HealthRecordEntity;
import com.aimedical.modules.patient.entity.HealthRecordType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 健康档案记录实体与 DTO 之间的转换器。
 */
@Component
public class HealthRecordConverter {

    public HealthRecordResponse toResponse(HealthRecordEntity entity) {
        if (entity == null) {
            return null;
        }
        HealthRecordResponse response = new HealthRecordResponse();
        response.setId(entity.getId());
        response.setPatientId(entity.getPatientId());
        response.setRecordType(entity.getRecordType());
        HealthRecordType type = HealthRecordType.fromCode(entity.getRecordType());
        response.setRecordTypeDesc(type != null ? type.getDesc() : null);
        response.setRecordCategory(entity.getRecordCategory());
        HealthRecordCategory category = HealthRecordCategory.fromCode(entity.getRecordCategory());
        response.setRecordCategoryDesc(category != null ? category.getDesc() : null);
        response.setTitle(entity.getTitle());
        response.setContent(entity.getContent());
        response.setOrganization(entity.getOrganization());
        response.setDepartment(entity.getDepartment());
        response.setDoctorName(entity.getDoctorName());
        response.setSourceId(entity.getSourceId());
        response.setSourceTable(entity.getSourceTable());
        response.setReportData(entity.getReportData());
        response.setRecordDate(entity.getRecordDate());
        response.setRemark(entity.getRemark());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public List<HealthRecordResponse> toResponseList(List<HealthRecordEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
