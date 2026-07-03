package com.aimedical.modules.ai.impl.template;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {

    List<PromptTemplate> findByCapabilityIdAndStatus(String capabilityId, TemplateStatus status);

    List<PromptTemplate> findByCapabilityIdAndDepartmentIdAndStatus(
            String capabilityId, String departmentId, TemplateStatus status);

    Optional<PromptTemplate> findByCapabilityIdAndDepartmentIdAndVersion(
            String capabilityId, String departmentId, int version);

    List<PromptTemplate> findByStatus(TemplateStatus status);
}
