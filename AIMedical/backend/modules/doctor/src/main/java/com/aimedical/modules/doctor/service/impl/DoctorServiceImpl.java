package com.aimedical.modules.doctor.service.impl;

import com.aimedical.common.exception.BusinessException;
import com.aimedical.common.exception.GlobalErrorCode;
import com.aimedical.common.result.Result;
import com.aimedical.modules.doctor.dto.DoctorDto;
import com.aimedical.modules.doctor.dto.DoctorProfileUpdateRequest;
import com.aimedical.modules.doctor.entity.DoctorEntity;
import com.aimedical.modules.doctor.repository.DoctorRepository;
import com.aimedical.modules.doctor.service.DoctorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorServiceImpl(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Override
    public Result<String> getPlaceholder() {
        return Result.success("doctor placeholder");
    }

    @Override
    public boolean existsById(Long id) {
        return doctorRepository.existsById(id);
    }

    @Override
    public String getRealName(Long id) {
        return doctorRepository.findById(id)
                .map(doctor -> doctor.getRealName())
                .orElse(null);
    }

    @Override
    public Result<DoctorDto> getProfileByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(GlobalErrorCode.UNAUTHORIZED, "无法获取当前登录医生ID");
        }
        DoctorEntity entity = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.NOT_FOUND, "医生档案不存在，请联系管理员创建"));
        return Result.success(toDto(entity));
    }

    @Override
    @Transactional
    public Result<DoctorDto> updateProfileByUserId(Long userId, DoctorProfileUpdateRequest request) {
        if (userId == null) {
            throw new BusinessException(GlobalErrorCode.UNAUTHORIZED, "无法获取当前登录医生ID");
        }
        DoctorEntity entity = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.NOT_FOUND, "医生档案不存在，请联系管理员创建"));

        // 仅更新可变字段，敏感字段（user_id / real_name / license_no）不允许通过此接口修改
        if (request.getGender() != null) entity.setGender(request.getGender());
        if (request.getTitle() != null) entity.setTitle(request.getTitle());
        if (request.getDepartment() != null) entity.setDepartment(request.getDepartment());
        if (request.getSpecialty() != null) entity.setSpecialty(request.getSpecialty());
        if (request.getIntroduction() != null) entity.setIntroduction(request.getIntroduction());
        if (request.getPracticeYears() != null) entity.setPracticeYears(request.getPracticeYears());
        if (request.getConsultationFee() != null) entity.setConsultationFee(request.getConsultationFee());
        if (request.getRemark() != null) entity.setRemark(request.getRemark());

        DoctorEntity saved = doctorRepository.save(entity);
        return Result.success(toDto(saved));
    }

    /**
     * 将实体转换为 DTO。
     */
    private static DoctorDto toDto(DoctorEntity entity) {
        DoctorDto dto = new DoctorDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setRealName(entity.getRealName());
        dto.setGender(entity.getGender());
        dto.setTitle(entity.getTitle());
        dto.setDepartment(entity.getDepartment());
        dto.setSpecialty(entity.getSpecialty());
        dto.setIntroduction(entity.getIntroduction());
        dto.setLicenseNo(entity.getLicenseNo());
        dto.setPracticeYears(entity.getPracticeYears());
        dto.setConsultationFee(entity.getConsultationFee());
        dto.setRemark(entity.getRemark());
        return dto;
    }
}
