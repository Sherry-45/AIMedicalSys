package com.aimedical.modules.labtest.entity;

import com.aimedical.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 检验明细实体，记录单项检验结果。
 */
@Entity
@Table(name = "lab_test_item")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class LabTestItem extends BaseEntity {

    @Column(name = "lab_test_id", nullable = false)
    private Long labTestId;

    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(name = "result", length = 100)
    private String result;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "reference_range", length = 200)
    private String referenceRange;

    @Enumerated(EnumType.STRING)
    @Column(name = "abnormal_flag", nullable = false, length = 20)
    private AbnormalFlag abnormalFlag = AbnormalFlag.NORMAL;
}
