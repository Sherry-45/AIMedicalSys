package com.aimedical.modules.examination.dto;

import lombok.Data;

@Data
public class ExaminationItemDTO {

    private Long id;

    private Long examinationId;

    private String itemName;

    private String finding;

    private String measurement;

    private Boolean abnormalFlag;
}
