-- 28. examination 检查记录主表
DROP TABLE IF EXISTS examination_item;
DROP TABLE IF EXISTS examination;
CREATE TABLE examination (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    examination_type VARCHAR(20) NOT NULL,
    body_part VARCHAR(200),
    clinical_diagnosis VARCHAR(500),
    scheduled_at DATETIME,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    emergency_flag TINYINT(1) NOT NULL DEFAULT 0,
    image_url VARCHAR(500),
    image_type VARCHAR(50),
    impression TEXT,
    conclusion VARCHAR(1000),
    ai_interpretation TEXT,
    ai_confidence DOUBLE,
    image_analysis_result TEXT,
    image_confidence DOUBLE,
    reported_at DATETIME,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    INDEX idx_examination_patient (patient_id),
    INDEX idx_examination_doctor (doctor_id),
    INDEX idx_examination_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查记录主表';

-- 29. examination_item 检查明细表
CREATE TABLE examination_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    examination_id BIGINT NOT NULL,
    item_name VARCHAR(200) NOT NULL,
    finding VARCHAR(1000),
    measurement VARCHAR(200),
    abnormal_flag TINYINT(1) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    INDEX idx_exam_item_examination (examination_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查明细表';
