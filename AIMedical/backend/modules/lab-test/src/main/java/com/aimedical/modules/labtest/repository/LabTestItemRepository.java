package com.aimedical.modules.labtest.repository;

import com.aimedical.modules.labtest.entity.LabTestItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabTestItemRepository extends JpaRepository<LabTestItem, Long> {

    List<LabTestItem> findByLabTestId(Long labTestId);

    List<LabTestItem> findByLabTestIdIn(List<Long> labTestIds);
}
