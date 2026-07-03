package com.aimedical.modules.window.repository;

import com.aimedical.modules.window.entity.OfflineRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * 线下挂号仓储。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface OfflineRegistrationRepository extends JpaRepository<OfflineRegistrationEntity, Long>,
        JpaSpecificationExecutor<OfflineRegistrationEntity> {

    /**
     * 按挂号编号查询。
     */
    Optional<OfflineRegistrationEntity> findByRegistrationNo(String registrationNo);

    /**
     * 按患者ID查询挂号列表。
     */
    List<OfflineRegistrationEntity> findByPatientId(Long patientId);

    /**
     * 按状态查询挂号列表。
     */
    List<OfflineRegistrationEntity> findByStatus(String status);

    /**
     * 判断挂号编号是否已存在（唯一性校验）。
     */
    boolean existsByRegistrationNo(String registrationNo);
}
