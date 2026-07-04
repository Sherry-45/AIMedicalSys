package com.aimedical.modules.doctor.service;

import com.aimedical.common.result.Result;
import com.aimedical.modules.doctor.dto.DoctorDto;
import com.aimedical.modules.doctor.dto.DoctorProfileUpdateRequest;

public interface DoctorService {
    Result<String> getPlaceholder();

    boolean existsById(Long id);

    String getRealName(Long id);

    /**
     * 按 user_id 查询医生档案。
     *
     * @param userId 当前登录医生的 user_id
     * @return 医生档案 DTO
     */
    Result<DoctorDto> getProfileByUserId(Long userId);

    /**
     * 按 user_id 更新医生档案（仅可变字段）。
     *
     * @param userId  当前登录医生的 user_id
     * @param request 更新请求
     * @return 更新后的医生档案 DTO
     */
    Result<DoctorDto> updateProfileByUserId(Long userId, DoctorProfileUpdateRequest request);
}
