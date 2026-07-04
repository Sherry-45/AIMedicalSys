package com.aimedical.modules.patient.api;

import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.auth.CurrentUser;
import com.aimedical.modules.patient.dto.health.HealthRecordCreateRequest;
import com.aimedical.modules.patient.dto.health.HealthRecordQueryRequest;
import com.aimedical.modules.patient.dto.health.HealthRecordResponse;
import com.aimedical.modules.patient.dto.health.HealthSummaryResponse;
import com.aimedical.modules.patient.dto.health.HealthTrendResponse;
import com.aimedical.modules.patient.entity.PatientEntity;
import com.aimedical.modules.patient.repository.PatientRepository;
import com.aimedical.modules.patient.service.HealthRecordService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 健康档案控制器（数据接入 + 健康档案增强）。
 *
 * <p>患者可访问自己的健康档案（完整报告整合、缴费记录、发药记录、健康趋势与合并摘要）；
 * 管理员可通过显式 patientId 参数查询任意患者档案。
 *
 * <p>对应需求：
 * <ul>
 *   <li>3.1.5 完整报告接入 - {@code GET /} 与 {@code GET /{id}}</li>
 *   <li>3.1.7 缴费记录查询 - {@code GET /payment-records}</li>
 *   <li>3.1.8 取药真实数据 - {@code GET /dispensing-records}</li>
 *   <li>健康档案增强（按时间/机构/类别检索 + 长期趋势合并展示）- {@code GET /trend}、{@code GET /summary}</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/patient/health-records")
@PreAuthorize("hasRole('PATIENT')")
public class HealthRecordController {

    private final HealthRecordService healthRecordService;
    private final CurrentUser currentUser;
    private final PatientRepository patientRepository;

    public HealthRecordController(HealthRecordService healthRecordService,
                                   CurrentUser currentUser,
                                   PatientRepository patientRepository) {
        this.healthRecordService = healthRecordService;
        this.currentUser = currentUser;
        this.patientRepository = patientRepository;
    }

    /**
     * 创建健康档案记录（管理员也可创建，用于各业务模块数据接入归档）。
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    public Result<HealthRecordResponse> create(@Valid @RequestBody HealthRecordCreateRequest request) {
        return healthRecordService.create(request);
    }

    /**
     * 按 ID 获取单条健康档案记录。
     */
    @GetMapping("/{id}")
    public Result<HealthRecordResponse> getById(@PathVariable Long id) {
        return healthRecordService.getById(id);
    }

    /**
     * 按时间/机构/类别/类型检索健康档案（分页）。
     *
     * <p>患者自动解析为自己的 patientId；管理员需显式传入 patientId。
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    public Result<List<HealthRecordResponse>> query(HealthRecordQueryRequest request,
                                                     @RequestParam(required = false) Long patientId) {
        Long resolved = resolvePatientId(patientId);
        if (resolved == null) {
            return Result.fail(GlobalErrorCode.UNAUTHORIZED.getCode(), "无法获取当前患者信息");
        }
        return healthRecordService.query(resolved, request);
    }

    /**
     * 获取长期健康趋势（按类型/类别聚合、最近记录、就诊机构、时间跨度）。
     */
    @GetMapping("/trend")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    public Result<HealthTrendResponse> getHealthTrend(@RequestParam(required = false) Long patientId) {
        Long resolved = resolvePatientId(patientId);
        if (resolved == null) {
            return Result.fail(GlobalErrorCode.UNAUTHORIZED.getCode(), "无法获取当前患者信息");
        }
        return healthRecordService.getHealthTrend(resolved);
    }

    /**
     * 获取健康档案合并摘要（过敏 + 慢病 + 趋势 + 最近就诊）。
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    public Result<HealthSummaryResponse> getHealthSummary(@RequestParam(required = false) Long patientId) {
        Long resolved = resolvePatientId(patientId);
        if (resolved == null) {
            return Result.fail(GlobalErrorCode.UNAUTHORIZED.getCode(), "无法获取当前患者信息");
        }
        return healthRecordService.getHealthSummary(resolved);
    }

    /**
     * 查询缴费记录（3.1.7）。
     */
    @GetMapping("/payment-records")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    public Result<List<HealthRecordResponse>> queryPaymentRecords(@RequestParam(required = false) Long patientId) {
        Long resolved = resolvePatientId(patientId);
        if (resolved == null) {
            return Result.fail(GlobalErrorCode.UNAUTHORIZED.getCode(), "无法获取当前患者信息");
        }
        return healthRecordService.queryPaymentRecords(resolved);
    }

    /**
     * 查询发药/取药记录（3.1.8 药房取药真实数据）。
     */
    @GetMapping("/dispensing-records")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    public Result<List<HealthRecordResponse>> queryDispensingRecords(@RequestParam(required = false) Long patientId) {
        Long resolved = resolvePatientId(patientId);
        if (resolved == null) {
            return Result.fail(GlobalErrorCode.UNAUTHORIZED.getCode(), "无法获取当前患者信息");
        }
        return healthRecordService.queryDispensingRecords(resolved);
    }

    /**
     * 解析 patientId：显式传入（管理员场景）优先；否则从当前登录用户解析其患者档案ID。
     *
     * @return 解析到的 patientId，无法解析时返回 null（由调用方返回 UNAUTHORIZED 业务错误）
     */
    private Long resolvePatientId(Long explicitPatientId) {
        if (explicitPatientId != null) {
            return explicitPatientId;
        }
        Long userId = currentUser.getUserId();
        if (userId == null) {
            return null;
        }
        return patientRepository.findByUserId(userId)
                .map(PatientEntity::getId)
                .orElse(null);
    }
}
