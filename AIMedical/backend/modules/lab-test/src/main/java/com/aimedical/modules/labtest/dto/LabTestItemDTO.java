package com.aimedical.modules.labtest.dto;

import com.aimedical.modules.labtest.entity.AbnormalFlag;
import lombok.Data;

/**
 * 检验明细 DTO。
 */
@Data
public class LabTestItemDTO {

    private Long id;
    private Long labTestId;
    private String itemName;
    private String result;
    private String unit;
    private String referenceRange;
    private AbnormalFlag abnormalFlag;
}
