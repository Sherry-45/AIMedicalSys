package com.aimedical.modules.labtest.dto;

import com.aimedical.modules.labtest.entity.SampleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 检验创建请求。
 */
@Data
public class LabTestCreateRequest {

    @NotNull
    private Long patientId;

    @NotNull
    private Long doctorId;

    @NotBlank
    @Size(max = 200, message = "检验类型长度不能超过 200")
    private String testType;

    @NotNull
    private SampleType sampleType;
}
