package com.aimedical.modules.doctor.api;

import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.auth.CurrentUser;
import com.aimedical.modules.doctor.dto.DoctorDto;
import com.aimedical.modules.doctor.dto.DoctorProfileUpdateRequest;
import com.aimedical.modules.doctor.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 医生端控制器。
 *
 * <p>提供医生档案查询与更新接口，全部需要 DOCTOR 角色。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/doctor")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorController {

    private final DoctorService doctorService;
    private final CurrentUser currentUser;

    public DoctorController(DoctorService doctorService, CurrentUser currentUser) {
        this.doctorService = doctorService;
        this.currentUser = currentUser;
    }

    /**
     * 占位接口（保留向后兼容）。
     */
    @GetMapping("/placeholder")
    public Result<String> placeholder() {
        return doctorService.getPlaceholder();
    }

    /**
     * 查询当前登录医生的档案。GET /api/doctor/profile
     */
    @GetMapping("/profile")
    public Result<DoctorDto> getProfile() {
        return doctorService.getProfileByUserId(currentDoctorId());
    }

    /**
     * 更新当前登录医生的档案（仅可变字段）。PUT /api/doctor/profile
     */
    @PutMapping("/profile")
    public Result<DoctorDto> updateProfile(@Valid @RequestBody DoctorProfileUpdateRequest request) {
        return doctorService.updateProfileByUserId(currentDoctorId(), request);
    }

    private Long currentDoctorId() {
        Long userId = currentUser.getUserId();
        if (userId == null) {
            throw new IllegalStateException("无法获取当前登录医生ID");
        }
        return userId;
    }
}
