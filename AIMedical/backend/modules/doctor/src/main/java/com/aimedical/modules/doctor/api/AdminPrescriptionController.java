package com.aimedical.modules.doctor.api;

import com.aimedical.common.result.Result;
import com.aimedical.modules.doctor.converter.PrescriptionConverter;
import com.aimedical.modules.doctor.dto.response.PrescriptionResponse;
import com.aimedical.modules.doctor.entity.PrescriptionEntity;
import com.aimedical.modules.doctor.entity.PrescriptionStatus;
import com.aimedical.modules.doctor.repository.PrescriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 处方管理控制器（管理员端，只读）。
 *
 * <p>提供管理员查看全部处方的能力，不支持创建/修改/审核操作。
 * 安全策略：所有端点仅 ADMIN 角色可访问。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/admin/prescriptions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPrescriptionController {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionConverter prescriptionConverter;

    public AdminPrescriptionController(PrescriptionRepository prescriptionRepository,
                                       PrescriptionConverter prescriptionConverter) {
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionConverter = prescriptionConverter;
    }

    /**
     * 分页查询全部处方（可选状态筛选）。
     *
     * @param status 处方状态（DRAFT/PENDING_REVIEW/APPROVED/REJECTED），可选
     * @param page   页码，从 0 开始
     * @param size   每页大小
     */
    @GetMapping
    public Result<Page<PrescriptionResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        int p = page == null ? 0 : Math.max(0, page);
        int s = size == null ? 20 : Math.min(Math.max(1, size), 100);
        PageRequest pageable = PageRequest.of(p, s,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<PrescriptionEntity> entityPage;
        if (status == null || status.isBlank()) {
            entityPage = prescriptionRepository.findAll(pageable);
        } else {
            try {
                PrescriptionStatus statusEnum = PrescriptionStatus.valueOf(status.toUpperCase());
                entityPage = prescriptionRepository.findByStatusOrderByCreatedAtDesc(statusEnum, pageable);
            } catch (IllegalArgumentException e) {
                return Result.fail("PARAM_INVALID", "无效的处方状态: " + status);
            }
        }

        Page<PrescriptionResponse> responsePage = entityPage.map(prescriptionConverter::toResponse);
        return Result.success(responsePage);
    }
}
