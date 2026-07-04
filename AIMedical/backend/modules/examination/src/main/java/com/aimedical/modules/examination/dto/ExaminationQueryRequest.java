package com.aimedical.modules.examination.dto;

import com.aimedical.common.result.PageQuery;
import com.aimedical.modules.examination.entity.ExaminationStatus;
import com.aimedical.modules.examination.entity.ExaminationType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ExaminationQueryRequest extends PageQuery {

    private Long patientId;

    private Long doctorId;

    private ExaminationStatus status;

    private ExaminationType examinationType;
}
