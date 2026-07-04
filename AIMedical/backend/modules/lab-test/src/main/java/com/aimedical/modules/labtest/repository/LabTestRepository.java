package com.aimedical.modules.labtest.repository;

import com.aimedical.modules.labtest.entity.LabTest;
import com.aimedical.modules.labtest.entity.LabTestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabTestRepository extends JpaRepository<LabTest, Long> {

    List<LabTest> findByPatientIdAndStatus(Long patientId, LabTestStatus status);
}
