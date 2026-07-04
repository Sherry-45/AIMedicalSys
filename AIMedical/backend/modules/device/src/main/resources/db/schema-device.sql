-- 32. device_info 设备信息表
DROP TABLE IF EXISTS device_message;
DROP TABLE IF EXISTS device_info;
CREATE TABLE device_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_code VARCHAR(50) NOT NULL,
    device_name VARCHAR(200) NOT NULL,
    device_type VARCHAR(30) NOT NULL,
    protocol VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',
    manufacturer VARCHAR(200),
    model VARCHAR(200),
    location VARCHAR(500),
    connection_config TEXT,
    last_heartbeat_at DATETIME,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_device_code (device_code),
    INDEX idx_device_type (device_type),
    INDEX idx_device_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备信息表';

-- 33. device_message 设备报文表
CREATE TABLE device_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    protocol VARCHAR(20) NOT NULL,
    raw_content TEXT,
    parsed_content TEXT,
    processed TINYINT(1) NOT NULL DEFAULT 0,
    received_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    INDEX idx_device_message_device (device_id),
    INDEX idx_device_message_type (message_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备报文表';
