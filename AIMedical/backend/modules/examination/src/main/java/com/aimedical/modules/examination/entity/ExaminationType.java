package com.aimedical.modules.examination.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExaminationType implements BaseEnum {

    CT("CT", "CT扫描"),
    MRI("MRI", "磁共振"),
    X_RAY("X_RAY", "X光"),
    ULTRASOUND("ULTRASOUND", "超声"),
    MAMMOGRAPHY("MAMMOGRAPHY", "乳腺钼靶"),
    ENDOSCOPY("ENDOSCOPY", "内窥镜"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String desc;
}
