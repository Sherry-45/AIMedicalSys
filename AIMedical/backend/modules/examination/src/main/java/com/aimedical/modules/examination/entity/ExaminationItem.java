package com.aimedical.modules.examination.entity;

import com.aimedical.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "examination_item")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ExaminationItem extends BaseEntity {

    @Column(name = "examination_id", nullable = false)
    private Long examinationId;

    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(name = "finding", length = 1000)
    private String finding;

    @Column(name = "measurement", length = 200)
    private String measurement;

    @Column(name = "abnormal_flag", nullable = false)
    private Boolean abnormalFlag = false;
}
