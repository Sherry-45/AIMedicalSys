package com.aimedical.modules.labtest.dto;

import com.aimedical.modules.labtest.entity.AbnormalFlag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 检验明细请求。
 */
@Data
public class LabTestItemRequest {

    @NotBlank
    @Size(max = 200, message = "检验项目名称长度不能超过 200")
    private String itemName;

    @NotBlank
    @Size(max = 100, message = "检验结果长度不能超过 100")
    private String result;

    @Size(max = 50, message = "单位长度不能超过 50")
    private String unit;

    @Size(max = 200, message = "参考范围长度不能超过 200")
    private String referenceRange;

    private AbnormalFlag abnormalFlag = AbnormalFlag.NORMAL;
}
