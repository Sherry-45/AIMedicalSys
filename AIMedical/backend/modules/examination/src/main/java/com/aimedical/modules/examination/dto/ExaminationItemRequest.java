package com.aimedical.modules.examination.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExaminationItemRequest {

    @NotBlank
    @Size(max = 200, message = "检查项目名称长度不能超过 200")
    private String itemName;

    @Size(max = 1000, message = "检查所见长度不能超过 1000")
    private String finding;

    @Size(max = 200, message = "测量值长度不能超过 200")
    private String measurement;

    private Boolean abnormalFlag;
}
