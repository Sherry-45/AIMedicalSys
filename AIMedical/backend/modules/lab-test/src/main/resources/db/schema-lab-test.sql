-- 30. lab_test 检验记录主表
DROP TABLE IF EXISTS lab_test_item;
DROP TABLE IF EXISTS lab_test;
CREATE TABLE lab_test (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    test_type VARCHAR(200) NOT NULL,
    sample_type VARCHAR(20) NOT NULL,
    collected_at DATETIME,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    report_conclusion VARCHAR(1000),
    ai_interpretation TEXT,
    reported_at DATETIME,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    INDEX idx_lab_test_patient (patient_id),
    INDEX idx_lab_test_doctor (doctor_id),
    INDEX idx_lab_test_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检验记录主表';

-- 31. lab_test_item 检验明细表
CREATE TABLE lab_test_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lab_test_id BIGINT NOT NULL,
    item_name VARCHAR(200) NOT NULL,
    result VARCHAR(100),
    unit VARCHAR(50),
    reference_range VARCHAR(200),
    abnormal_flag VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    INDEX idx_lab_test_item_test (lab_test_id),
    INDEX idx_lab_test_item_name (item_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检验明细表';
