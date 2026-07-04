package com.aimedical.modules.examination.repository;

import com.aimedical.modules.examination.entity.Examination;
import com.aimedical.modules.examination.entity.ExaminationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExaminationRepository extends JpaRepository<Examination, Long> {

    Page<Examination> findByPatientId(Long patientId, Pageable pageable);

    Page<Examination> findByDoctorId(Long doctorId, Pageable pageable);

    Page<Examination> findByPatientIdAndDoctorId(Long patientId, Long doctorId, Pageable pageable);

    List<Examination> findByPatientIdAndStatus(Long patientId, ExaminationStatus status);
}
