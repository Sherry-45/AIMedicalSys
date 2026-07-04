package com.aimedical.modules.labtest.dto;

import com.aimedical.common.result.PageQuery;
import com.aimedical.modules.labtest.entity.LabTestStatus;
import com.aimedical.modules.labtest.entity.SampleType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检验查询请求。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LabTestQueryRequest extends PageQuery {

    private Long patientId;
    private Long doctorId;
    private LabTestStatus status;
    private SampleType sampleType;
}
