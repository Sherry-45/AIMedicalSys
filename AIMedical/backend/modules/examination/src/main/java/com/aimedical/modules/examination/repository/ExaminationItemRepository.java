package com.aimedical.modules.examination.repository;

import com.aimedical.modules.examination.entity.ExaminationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExaminationItemRepository extends JpaRepository<ExaminationItem, Long> {

    List<ExaminationItem> findByExaminationId(Long examinationId);

    List<ExaminationItem> findByExaminationIdIn(List<Long> examinationIds);

    /**
     * 按检查 ID 批量软删除明细，等价于 {@code @SQLDelete} 的批量版本，
     * 避免逐条 deleteAll 触发单条 UPDATE。
     */
    @Modifying
    @Query("UPDATE ExaminationItem e SET e.deleted = true, e.version = e.version + 1 "
            + "WHERE e.examinationId = :examinationId AND e.deleted = false")
    int softDeleteByExaminationId(@Param("examinationId") Long examinationId);
}
