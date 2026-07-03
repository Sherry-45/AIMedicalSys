-- =============================================
-- 智慧云脑诊疗平台 - 数据库 schema (H2 开发环境)
-- =============================================

DROP ALL OBJECTS;
SET REFERENTIAL_INTEGRITY FALSE;

-- ---------------------------------------------
-- 1. sys_user
-- ---------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`         BIGINT        NOT NULL AUTO_INCREMENT,
  `username`   VARCHAR(64)   NOT NULL,
  `password`   VARCHAR(128)  NOT NULL,
  `nickname`   VARCHAR(64)   NOT NULL,
  `phone`      VARCHAR(20)   DEFAULT NULL,
  `email`      VARCHAR(128)  DEFAULT NULL,
  `gender`     VARCHAR(10)   DEFAULT NULL,
  `age`        INT           DEFAULT NULL,
  `user_type`  VARCHAR(20)   NOT NULL,
  `enabled`    BOOLEAN       NOT NULL DEFAULT TRUE,
  `password_change_required` BOOLEAN NOT NULL DEFAULT FALSE,
  `token_version` INT           NOT NULL DEFAULT 0,
  `remark`     VARCHAR(500)  DEFAULT NULL,
  `created_at` TIMESTAMP     DEFAULT NULL,
  `updated_at` TIMESTAMP     DEFAULT NULL,
  `deleted`    BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_username_user_type` (`username`, `user_type`)
);

-- ---------------------------------------------
-- 2. sys_role
-- ---------------------------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `code`        VARCHAR(64)   NOT NULL,
  `name`        VARCHAR(64)   DEFAULT NULL,
  `description` VARCHAR(500)  DEFAULT NULL,
  `enabled`     BOOLEAN       DEFAULT TRUE,
  `sort`        INT           DEFAULT 0,
  `remark`      VARCHAR(500)  DEFAULT NULL,
  `version`     BIGINT        DEFAULT 0,
  `created_at`  TIMESTAMP     DEFAULT NULL,
  `updated_at`  TIMESTAMP     DEFAULT NULL,
  `deleted`     BOOLEAN       DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
);

-- ---------------------------------------------
-- 3. sys_post
-- ---------------------------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `code`        VARCHAR(64)   NOT NULL,
  `name`        VARCHAR(64)   DEFAULT NULL,
  `description` VARCHAR(500)  DEFAULT NULL,
  `role_id`     BIGINT        DEFAULT NULL,
  `enabled`     BOOLEAN       DEFAULT TRUE,
  `sort`        INT           DEFAULT 0,
  `remark`      VARCHAR(500)  DEFAULT NULL,
  `version`     BIGINT        DEFAULT 0,
  `created_at`  TIMESTAMP     DEFAULT NULL,
  `updated_at`  TIMESTAMP     DEFAULT NULL,
  `deleted`     BOOLEAN       DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  CONSTRAINT `fk_sys_post_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`)
);

-- ---------------------------------------------
-- 4. sys_function
-- ---------------------------------------------
DROP TABLE IF EXISTS `sys_function`;
CREATE TABLE `sys_function` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `parent_id`     BIGINT        DEFAULT NULL,
  `code`          VARCHAR(128)  NOT NULL,
  `name`          VARCHAR(64)   DEFAULT NULL,
  `type`          VARCHAR(20)   DEFAULT 'MENU',
  `path`          VARCHAR(128)  DEFAULT NULL,
  `component`     VARCHAR(255)  DEFAULT NULL,
  `icon`          VARCHAR(64)   DEFAULT NULL,
  `sort`          INT           DEFAULT 0,
  `visible`       BOOLEAN       DEFAULT TRUE,
  `perms`         VARCHAR(128)  DEFAULT NULL,
  `query_method`  VARCHAR(10)   DEFAULT NULL,
  `description`   VARCHAR(500)  DEFAULT NULL,
  `enabled`       BOOLEAN       DEFAULT TRUE,
  `remark`        VARCHAR(500)  DEFAULT NULL,
  `version`       BIGINT        DEFAULT 0,
  `created_at`    TIMESTAMP     DEFAULT NULL,
  `updated_at`    TIMESTAMP     DEFAULT NULL,
  `deleted`       BOOLEAN       DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_code_type` (`code`, `type`),
  CONSTRAINT `fk_sys_function_parent` FOREIGN KEY (`parent_id`) REFERENCES `sys_function` (`id`)
);

-- ---------------------------------------------
-- 5. sys_dict_type
-- ---------------------------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `id`         BIGINT        NOT NULL AUTO_INCREMENT,
  `dict_name`  VARCHAR(100)  DEFAULT NULL,
  `dict_type`  VARCHAR(100)  NOT NULL,
  `status`     BOOLEAN       DEFAULT TRUE,
  `remark`     VARCHAR(500)  DEFAULT NULL,
  `version`    BIGINT        DEFAULT 0,
  `created_at` TIMESTAMP     DEFAULT NULL,
  `updated_at` TIMESTAMP     DEFAULT NULL,
  `deleted`    BOOLEAN       DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`)
);

-- ---------------------------------------------
-- 6. sys_dict_data
-- ---------------------------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `id`         BIGINT        NOT NULL AUTO_INCREMENT,
  `dict_sort`  INT           DEFAULT 0,
  `dict_label` VARCHAR(100)  DEFAULT NULL,
  `dict_value` VARCHAR(100)  DEFAULT NULL,
  `dict_type`  VARCHAR(100)  DEFAULT NULL,
  `css_class`  VARCHAR(100)  DEFAULT NULL,
  `list_class` VARCHAR(100)  DEFAULT NULL,
  `is_default` BOOLEAN       DEFAULT FALSE,
  `status`     BOOLEAN       DEFAULT TRUE,
  `remark`     VARCHAR(500)  DEFAULT NULL,
  `version`    BIGINT        DEFAULT 0,
  `created_at` TIMESTAMP     DEFAULT NULL,
  `updated_at` TIMESTAMP     DEFAULT NULL,
  `deleted`    BOOLEAN       DEFAULT FALSE,
  PRIMARY KEY (`id`),
  KEY `idx_dict_type` (`dict_type`),
  CONSTRAINT `fk_sys_dict_data_type` FOREIGN KEY (`dict_type`) REFERENCES `sys_dict_type` (`dict_type`)
);

-- ---------------------------------------------
-- 7. user_role
-- ---------------------------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`),
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`)
);

-- ---------------------------------------------
-- 8. user_post
-- ---------------------------------------------
DROP TABLE IF EXISTS `user_post`;
CREATE TABLE `user_post` (
  `user_id` BIGINT NOT NULL,
  `post_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`, `post_id`),
  CONSTRAINT `fk_user_post_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_user_post_post` FOREIGN KEY (`post_id`) REFERENCES `sys_post` (`id`)
);

-- ---------------------------------------------
-- 9. post_function
-- ---------------------------------------------
DROP TABLE IF EXISTS `post_function`;
CREATE TABLE `post_function` (
  `post_id`     BIGINT NOT NULL,
  `function_id` BIGINT NOT NULL,
  PRIMARY KEY (`post_id`, `function_id`),
  CONSTRAINT `fk_post_function_post` FOREIGN KEY (`post_id`) REFERENCES `sys_post` (`id`),
  CONSTRAINT `fk_post_function_function` FOREIGN KEY (`function_id`) REFERENCES `sys_function` (`id`)
);

-- ---------------------------------------------
-- 10. patient_profile
-- ---------------------------------------------
DROP TABLE IF EXISTS `patient_profile`;
CREATE TABLE `patient_profile` (
  `id`                  BIGINT        NOT NULL AUTO_INCREMENT,
  `user_id`             BIGINT        DEFAULT NULL,
  `real_name`           VARCHAR(64)   DEFAULT NULL,
  `gender`              VARCHAR(20)   DEFAULT NULL,
  `birth_date`          DATE          DEFAULT NULL,
  `age`                 INT           DEFAULT NULL,
  `id_card`             VARCHAR(32)   DEFAULT NULL,
  `phone`               VARCHAR(20)   DEFAULT NULL,
  `emergency_contact`   VARCHAR(64)   DEFAULT NULL,
  `emergency_phone`     VARCHAR(20)   DEFAULT NULL,
  `address`             VARCHAR(255)  DEFAULT NULL,
  `avatar_url`          VARCHAR(500)  DEFAULT NULL,
  `remark`              VARCHAR(500)  DEFAULT NULL,
  `version`             BIGINT        DEFAULT 0,
  `created_at`          TIMESTAMP     DEFAULT NULL,
  `updated_at`          TIMESTAMP     DEFAULT NULL,
  `deleted`             BOOLEAN       DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_id_card` (`id_card`),
  KEY `idx_phone` (`phone`),
  CONSTRAINT `fk_patient_profile_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
);

-- ---------------------------------------------
-- 11. doctor_profile
-- ---------------------------------------------
DROP TABLE IF EXISTS `doctor_profile`;
CREATE TABLE `doctor_profile` (
  `id`                 BIGINT         NOT NULL AUTO_INCREMENT,
  `user_id`            BIGINT         DEFAULT NULL,
  `real_name`          VARCHAR(64)    DEFAULT NULL,
  `gender`             VARCHAR(20)    DEFAULT NULL,
  `title`              VARCHAR(64)    DEFAULT NULL,
  `department`         VARCHAR(64)    DEFAULT NULL,
  `specialty`          VARCHAR(255)   DEFAULT NULL,
  `introduction`       TEXT           DEFAULT NULL,
  `license_no`         VARCHAR(64)    DEFAULT NULL,
  `practice_years`     INT            DEFAULT NULL,
  `consultation_fee`   DECIMAL(10, 2) DEFAULT NULL,
  `remark`             VARCHAR(500)   DEFAULT NULL,
  `version`            BIGINT         DEFAULT 0,
  `created_at`         TIMESTAMP      DEFAULT NULL,
  `updated_at`         TIMESTAMP      DEFAULT NULL,
  `deleted`            BOOLEAN        DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_license_no` (`license_no`),
  KEY `idx_department` (`department`),
  CONSTRAINT `fk_doctor_profile_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
);

-- ---------------------------------------------
-- 12. admin_profile
-- ---------------------------------------------
DROP TABLE IF EXISTS `admin_profile`;
CREATE TABLE `admin_profile` (
  `id`         BIGINT        NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT        DEFAULT NULL,
  `real_name`  VARCHAR(64)   DEFAULT NULL,
  `gender`     VARCHAR(20)   DEFAULT NULL,
  `phone`      VARCHAR(20)   DEFAULT NULL,
  `department` VARCHAR(64)   DEFAULT NULL,
  `remark`     VARCHAR(500)  DEFAULT NULL,
  `version`    BIGINT        DEFAULT 0,
  `created_at` TIMESTAMP     DEFAULT NULL,
  `updated_at` TIMESTAMP     DEFAULT NULL,
  `deleted`    BOOLEAN       DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_admin_profile_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
);

-- ---------------------------------------------
-- 13. consultation_queue
-- ---------------------------------------------
DROP TABLE IF EXISTS `consultation_queue`;
CREATE TABLE `consultation_queue` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `patient_id`    BIGINT        NOT NULL,
  `patient_name`  VARCHAR(64)   NOT NULL,
  `doctor_id`     BIGINT        NOT NULL,
  `registration_id` BIGINT      DEFAULT NULL,
  `department`    VARCHAR(64)   DEFAULT NULL,
  `status`        VARCHAR(20)   NOT NULL DEFAULT 'WAITING',
  `registered_at` TIMESTAMP     DEFAULT NULL,
  `called_at`     TIMESTAMP     DEFAULT NULL,
  `finished_at`   TIMESTAMP     DEFAULT NULL,
  `version`       BIGINT        NOT NULL DEFAULT 0,
  `created_at`    TIMESTAMP     DEFAULT NULL,
  `updated_at`    TIMESTAMP     DEFAULT NULL,
  `deleted`       BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  KEY `idx_doctor_status` (`doctor_id`, `status`),
  KEY `idx_patient_id` (`patient_id`),
  UNIQUE KEY `uk_queue_registration` (`registration_id`),
  CONSTRAINT `fk_consultation_queue_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_profile` (`id`),
  CONSTRAINT `fk_consultation_queue_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `sys_user` (`id`)
);

-- ---------------------------------------------
-- 14. medical_record
-- ---------------------------------------------
DROP TABLE IF EXISTS `medical_record`;
CREATE TABLE `medical_record` (
  `id`                 BIGINT        NOT NULL AUTO_INCREMENT,
  `patient_id`         BIGINT        NOT NULL,
  `doctor_id`          BIGINT        NOT NULL,
  `department`         VARCHAR(64)   DEFAULT NULL,
  `version_no`         INT           NOT NULL DEFAULT 0,
  `version`            BIGINT        NOT NULL DEFAULT 0,
  `status`             VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
  `chief_complaint`    TEXT          DEFAULT NULL,
  `present_illness`   TEXT          DEFAULT NULL,
  `past_history`       TEXT          DEFAULT NULL,
  `diagnosis`          TEXT          DEFAULT NULL,
  `treatment_plan`     TEXT          DEFAULT NULL,
  `prescription_id`    BIGINT        DEFAULT NULL,
  `template_id`        BIGINT        DEFAULT NULL,
  `ai_generated`       BOOLEAN       NOT NULL DEFAULT FALSE,
  `remark`             VARCHAR(500)  DEFAULT NULL,
  `draft_key`          VARCHAR(60),
  `official_key`       VARCHAR(60),
  `created_at`         TIMESTAMP     DEFAULT NULL,
  `updated_at`         TIMESTAMP     DEFAULT NULL,
  `deleted`            BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_patient_doctor_draft` (`draft_key`),
  UNIQUE KEY `uk_patient_official_version` (`official_key`),
  KEY `idx_patient_status` (`patient_id`, `status`),
  KEY `idx_doctor_id` (`doctor_id`),
  KEY `idx_prescription_id` (`prescription_id`),
  CONSTRAINT `fk_medical_record_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_profile` (`id`),
  CONSTRAINT `fk_medical_record_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `sys_user` (`id`)
);

-- ---------------------------------------------
-- 15. prescription
-- ---------------------------------------------
DROP TABLE IF EXISTS `prescription`;
CREATE TABLE `prescription` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT,
  `patient_id`     BIGINT        NOT NULL,
  `patient_name`   VARCHAR(64)   NOT NULL,
  `doctor_id`      BIGINT        NOT NULL,
  `department`     VARCHAR(64)   DEFAULT NULL,
  `status`         VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
  `diagnosis`      VARCHAR(500)  DEFAULT NULL,
  `ai_checked`     BOOLEAN       NOT NULL DEFAULT FALSE,
  `ai_risk_level`  VARCHAR(20)   DEFAULT NULL,
  `audit_remark`   VARCHAR(500)  DEFAULT NULL,
  `audited_by`     BIGINT        DEFAULT NULL,
  `audited_at`     TIMESTAMP     DEFAULT NULL,
  `remark`         VARCHAR(500)  DEFAULT NULL,
  `created_at`     TIMESTAMP     DEFAULT NULL,
  `updated_at`     TIMESTAMP     DEFAULT NULL,
  `deleted`        BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  KEY `idx_patient_status` (`patient_id`, `status`),
  KEY `idx_doctor_id` (`doctor_id`),
  KEY `idx_patient_doctor_created` (`patient_id`, `doctor_id`, `created_at`),
  CONSTRAINT `fk_prescription_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_profile` (`id`),
  CONSTRAINT `fk_prescription_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `sys_user` (`id`)
);

-- ---------------------------------------------
-- 16. prescription_item
-- ---------------------------------------------
DROP TABLE IF EXISTS `prescription_item`;
CREATE TABLE `prescription_item` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `prescription_id` BIGINT     NOT NULL,
  `drug_name`    VARCHAR(128) NOT NULL,
  `specification` VARCHAR(128) DEFAULT NULL,
  `dosage`       VARCHAR(64)  DEFAULT NULL,
  `usage_method` VARCHAR(64)  DEFAULT NULL,
  `frequency`    VARCHAR(64)  DEFAULT NULL,
  `quantity`     DECIMAL(10,2) NOT NULL DEFAULT 1,
  `unit`         VARCHAR(32)  DEFAULT NULL,
  `remark`       VARCHAR(500) DEFAULT NULL,
  `created_at`   TIMESTAMP    DEFAULT NULL,
  `updated_at`   TIMESTAMP    DEFAULT NULL,
  `deleted`      BOOLEAN      NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  KEY `idx_prescription_id` (`prescription_id`),
  CONSTRAINT `fk_prescription_item_prescription` FOREIGN KEY (`prescription_id`) REFERENCES `prescription` (`id`)
);

-- ---------------------------------------------
-- 17. medical_record_template
-- ---------------------------------------------
DROP TABLE IF EXISTS `medical_record_template`;
CREATE TABLE `medical_record_template` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `doctor_id`   BIGINT        NOT NULL,
  `department`  VARCHAR(64)   DEFAULT NULL,
  `name`        VARCHAR(128)  NOT NULL,
  `content`     TEXT          DEFAULT NULL,
  `version_no`  INT           NOT NULL DEFAULT 1,
  `version`     BIGINT        NOT NULL DEFAULT 0,
  `status`      VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
  `created_at`  TIMESTAMP     DEFAULT NULL,
  `updated_at`  TIMESTAMP     DEFAULT NULL,
  `deleted`     BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  KEY `idx_doctor_id` (`doctor_id`),
  KEY `idx_department` (`department`),
  CONSTRAINT `fk_template_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `sys_user` (`id`)
);

-- ---------------------------------------------
-- 18. condition_entry
-- ---------------------------------------------
DROP TABLE IF EXISTS `condition_entry`;
CREATE TABLE `condition_entry` (
  `id`         BIGINT        NOT NULL AUTO_INCREMENT,
  `patient_id` BIGINT        NOT NULL,
  `content`    TEXT          NOT NULL,
  `created_at` TIMESTAMP     DEFAULT NULL,
  `updated_at` TIMESTAMP     DEFAULT NULL,
  `deleted`    BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  KEY `idx_patient_id` (`patient_id`),
  CONSTRAINT `fk_condition_entry_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_profile` (`id`)
);

-- ---------------------------------------------
-- 19. audit_record  处方审核记录
-- ---------------------------------------------
DROP TABLE IF EXISTS `audit_record`;
CREATE TABLE `audit_record` (
  `audit_id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `prescription_id`       VARCHAR(64)   NOT NULL,
  `prescription_order_id` VARCHAR(64)   DEFAULT NULL,
  `doctor_id`             VARCHAR(64)   DEFAULT NULL,
  `patient_id`            VARCHAR(64)   DEFAULT NULL,
  `audit_time`            TIMESTAMP     DEFAULT NULL,
  `from_fallback`         BOOLEAN       DEFAULT FALSE,
  `force_submitted`       BOOLEAN       DEFAULT NULL,
  `force_submit_time`     TIMESTAMP     DEFAULT NULL,
  `audit_sequence`        INT           DEFAULT 0,
  `is_latest`             BOOLEAN       DEFAULT FALSE,
  `original_prescription`  TEXT         DEFAULT NULL,
  `risk_level`            VARCHAR(20)   DEFAULT NULL,
  `ai_result`             TEXT          DEFAULT NULL,
  `audit_issues`          TEXT          DEFAULT NULL,
  `version`               BIGINT        DEFAULT 0,
  `created_at`            TIMESTAMP     DEFAULT NULL,
  `updated_at`            TIMESTAMP     DEFAULT NULL,
  `deleted`               BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`audit_id`),
  KEY `idx_audit_prescription_id` (`prescription_id`),
  KEY `idx_audit_order_is_latest` (`prescription_order_id`, `is_latest`)
);

-- ---------------------------------------------
-- 20. dosage_standard  剂量标准
-- ---------------------------------------------
DROP TABLE IF EXISTS `dosage_standard`;
CREATE TABLE `dosage_standard` (
  `id`                       BIGINT        NOT NULL AUTO_INCREMENT,
  `drug_code`                VARCHAR(50)   NOT NULL,
  `route_of_administration`  VARCHAR(20)   NOT NULL,
  `age_range_start`          INT           DEFAULT NULL,
  `age_range_end`            INT           DEFAULT NULL,
  `weight_range_start`       DECIMAL(10,2) DEFAULT NULL,
  `weight_range_end`         DECIMAL(10,2) DEFAULT NULL,
  `single_max`               DECIMAL(12,3) NOT NULL,
  `daily_max`                DECIMAL(12,3) DEFAULT NULL,
  `unit`                     VARCHAR(20)   NOT NULL,
  `version`                  BIGINT        DEFAULT 0,
  `created_at`               TIMESTAMP     DEFAULT NULL,
  `updated_at`               TIMESTAMP     DEFAULT NULL,
  `deleted`                  BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  KEY `idx_dosage_drug_route` (`drug_code`, `route_of_administration`)
);

-- ---------------------------------------------
-- 21. drug_allergy_mapping  药品过敏映射
-- ---------------------------------------------
DROP TABLE IF EXISTS `drug_allergy_mapping`;
CREATE TABLE `drug_allergy_mapping` (
  `id`         BIGINT        NOT NULL AUTO_INCREMENT,
  `drug_code`  VARCHAR(64)   NOT NULL,
  `allergens`  TEXT          DEFAULT NULL,
  `version`    BIGINT        DEFAULT 0,
  `created_at` TIMESTAMP     DEFAULT NULL,
  `updated_at` TIMESTAMP     DEFAULT NULL,
  `deleted`    BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_drug_allergy_drug_code` (`drug_code`)
);

-- ---------------------------------------------
-- 22. drug_composition_dict  药品成分字典
-- ---------------------------------------------
DROP TABLE IF EXISTS `drug_composition_dict`;
CREATE TABLE `drug_composition_dict` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `drug_code`   VARCHAR(64)   NOT NULL,
  `ingredients` TEXT          DEFAULT NULL,
  `version`     BIGINT        DEFAULT 0,
  `created_at`  TIMESTAMP     DEFAULT NULL,
  `updated_at`  TIMESTAMP     DEFAULT NULL,
  `deleted`     BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_drug_composition_drug_code` (`drug_code`)
);

-- ---------------------------------------------
-- 23. drug_contraindication_mapping  药品禁忌映射
-- ---------------------------------------------
DROP TABLE IF EXISTS `drug_contraindication_mapping`;
CREATE TABLE `drug_contraindication_mapping` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT,
  `drug_code`         VARCHAR(64)   NOT NULL,
  `contraindications` TEXT          DEFAULT NULL,
  `version`           BIGINT        DEFAULT 0,
  `created_at`        TIMESTAMP     DEFAULT NULL,
  `updated_at`        TIMESTAMP     DEFAULT NULL,
  `deleted`           BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_drug_contra_drug_code` (`drug_code`)
);

-- =============================================
-- Phase 4: 药房域 / 药库域 / 线下窗口 / 健康档案增强
-- =============================================

-- 35. drug_catalog
DROP TABLE IF EXISTS `drug_catalog`;
CREATE TABLE `drug_catalog` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `drug_code`       VARCHAR(64)   NOT NULL,
  `drug_name`       VARCHAR(255)  NOT NULL,
  `generic_name`    VARCHAR(255)  DEFAULT NULL,
  `specification`   VARCHAR(255)  DEFAULT NULL,
  `manufacturer`    VARCHAR(255)  DEFAULT NULL,
  `drug_form`       VARCHAR(50)   DEFAULT NULL,
  `drug_category`   VARCHAR(50)   NOT NULL,
  `unit`            VARCHAR(20)   DEFAULT NULL,
  `retail_price`    DECIMAL(10,2) DEFAULT NULL,
  `purchase_price`  DECIMAL(10,2) DEFAULT NULL,
  `otc_flag`        BOOLEAN       DEFAULT FALSE,
  `enabled`         BOOLEAN       NOT NULL DEFAULT TRUE,
  `remark`          VARCHAR(500)  DEFAULT NULL,
  `version`         BIGINT        DEFAULT 0,
  `created_at`      TIMESTAMP     DEFAULT NULL,
  `updated_at`      TIMESTAMP     DEFAULT NULL,
  `deleted`         BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_drug_code` (`drug_code`)
);

-- 36. inventory_stock
DROP TABLE IF EXISTS `inventory_stock`;
CREATE TABLE `inventory_stock` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `drug_code`       VARCHAR(64)   NOT NULL,
  `batch_no`        VARCHAR(64)   DEFAULT NULL,
  `quantity`        DECIMAL(12,2) NOT NULL DEFAULT 0,
  `unit`            VARCHAR(20)   DEFAULT NULL,
  `purchase_price`  DECIMAL(10,2) DEFAULT NULL,
  `retail_price`    DECIMAL(10,2) DEFAULT NULL,
  `expiry_date`     DATE          DEFAULT NULL,
  `production_date` DATE          DEFAULT NULL,
  `warehouse_location` VARCHAR(64) DEFAULT NULL,
  `remark`          VARCHAR(500)  DEFAULT NULL,
  `version`         BIGINT        DEFAULT 0,
  `created_at`      TIMESTAMP     DEFAULT NULL,
  `updated_at`      TIMESTAMP     DEFAULT NULL,
  `deleted`         BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`)
);

-- 37. pharmacy_stock
DROP TABLE IF EXISTS `pharmacy_stock`;
CREATE TABLE `pharmacy_stock` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `drug_code`       VARCHAR(64)   NOT NULL,
  `drug_name`       VARCHAR(255)  DEFAULT NULL,
  `batch_no`        VARCHAR(64)   DEFAULT NULL,
  `quantity`        DECIMAL(12,2) NOT NULL DEFAULT 0,
  `unit`            VARCHAR(20)   DEFAULT NULL,
  `retail_price`    DECIMAL(10,2) DEFAULT NULL,
  `expiry_date`     DATE          DEFAULT NULL,
  `shelf_location`  VARCHAR(64)   DEFAULT NULL,
  `safety_stock`    DECIMAL(12,2) DEFAULT 0,
  `remark`          VARCHAR(500)  DEFAULT NULL,
  `version`         BIGINT        DEFAULT 0,
  `created_at`      TIMESTAMP     DEFAULT NULL,
  `updated_at`      TIMESTAMP     DEFAULT NULL,
  `deleted`         BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`)
);

-- 38. dispensing_record
DROP TABLE IF EXISTS `dispensing_record`;
CREATE TABLE `dispensing_record` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT,
  `dispensing_no`     VARCHAR(32)   NOT NULL,
  `prescription_id`   BIGINT        DEFAULT NULL,
  `medical_order_id`  BIGINT        DEFAULT NULL,
  `patient_id`        BIGINT        DEFAULT NULL,
  `patient_name`      VARCHAR(64)   DEFAULT NULL,
  `pharmacist_id`     BIGINT        DEFAULT NULL,
  `pharmacist_name`   VARCHAR(64)   DEFAULT NULL,
  `status`            VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
  `total_quantity`    DECIMAL(12,2) DEFAULT NULL,
  `total_amount`      DECIMAL(10,2) DEFAULT NULL,
  `dispensed_at`      TIMESTAMP     DEFAULT NULL,
  `remark`            VARCHAR(500)  DEFAULT NULL,
  `version`           BIGINT        DEFAULT 0,
  `created_at`        TIMESTAMP     DEFAULT NULL,
  `updated_at`        TIMESTAMP     DEFAULT NULL,
  `deleted`           BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dispensing_no` (`dispensing_no`)
);

-- 39. dispensing_item
DROP TABLE IF EXISTS `dispensing_item`;
CREATE TABLE `dispensing_item` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT,
  `dispensing_id`     BIGINT        NOT NULL,
  `drug_code`         VARCHAR(64)   NOT NULL,
  `drug_name`         VARCHAR(255)  NOT NULL,
  `specification`     VARCHAR(255)  DEFAULT NULL,
  `batch_no`          VARCHAR(64)   DEFAULT NULL,
  `quantity`          DECIMAL(12,2) NOT NULL,
  `unit`              VARCHAR(20)   DEFAULT NULL,
  `unit_price`        DECIMAL(10,2) DEFAULT NULL,
  `amount`            DECIMAL(10,2) DEFAULT NULL,
  `dosage`            VARCHAR(100)  DEFAULT NULL,
  `usage_method`      VARCHAR(100)  DEFAULT NULL,
  `frequency`         VARCHAR(50)   DEFAULT NULL,
  `days`              INT           DEFAULT NULL,
  `remark`            VARCHAR(500)  DEFAULT NULL,
  `version`           BIGINT        DEFAULT 0,
  `created_at`        TIMESTAMP     DEFAULT NULL,
  `updated_at`        TIMESTAMP     DEFAULT NULL,
  `deleted`           BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`)
);

-- 40. pharmacy_refund_record
DROP TABLE IF EXISTS `pharmacy_refund_record`;
CREATE TABLE `pharmacy_refund_record` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT,
  `refund_no`         VARCHAR(32)   NOT NULL,
  `dispensing_id`     BIGINT        NOT NULL,
  `patient_id`        BIGINT        DEFAULT NULL,
  `patient_name`      VARCHAR(64)   DEFAULT NULL,
  `pharmacist_id`     BIGINT        DEFAULT NULL,
  `pharmacist_name`   VARCHAR(64)   DEFAULT NULL,
  `status`            VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
  `refund_reason`     VARCHAR(500)  DEFAULT NULL,
  `total_quantity`    DECIMAL(12,2) DEFAULT NULL,
  `total_amount`      DECIMAL(10,2) DEFAULT NULL,
  `refunded_at`       TIMESTAMP     DEFAULT NULL,
  `remark`            VARCHAR(500)  DEFAULT NULL,
  `version`           BIGINT        DEFAULT 0,
  `created_at`        TIMESTAMP     DEFAULT NULL,
  `updated_at`        TIMESTAMP     DEFAULT NULL,
  `deleted`           BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`)
);

-- 41. pharmacy_refund_item
DROP TABLE IF EXISTS `pharmacy_refund_item`;
CREATE TABLE `pharmacy_refund_item` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT,
  `refund_id`         BIGINT        NOT NULL,
  `dispensing_item_id` BIGINT       DEFAULT NULL,
  `drug_code`         VARCHAR(64)   NOT NULL,
  `drug_name`         VARCHAR(255)  NOT NULL,
  `batch_no`          VARCHAR(64)   DEFAULT NULL,
  `quantity`          DECIMAL(12,2) NOT NULL,
  `unit`              VARCHAR(20)   DEFAULT NULL,
  `unit_price`        DECIMAL(10,2) DEFAULT NULL,
  `amount`            DECIMAL(10,2) DEFAULT NULL,
  `remark`            VARCHAR(500)  DEFAULT NULL,
  `version`           BIGINT        DEFAULT 0,
  `created_at`        TIMESTAMP     DEFAULT NULL,
  `updated_at`        TIMESTAMP     DEFAULT NULL,
  `deleted`           BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`)
);

-- 42. stocktaking
DROP TABLE IF EXISTS `stocktaking`;
CREATE TABLE `stocktaking` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `stocktaking_no`  VARCHAR(32)   NOT NULL,
  `stocktaking_type` VARCHAR(20)  NOT NULL DEFAULT 'FULL',
  `status`          VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
  `operator_id`     BIGINT        DEFAULT NULL,
  `operator_name`   VARCHAR(64)   DEFAULT NULL,
  `start_time`      TIMESTAMP     DEFAULT NULL,
  `end_time`        TIMESTAMP     DEFAULT NULL,
  `total_items`     INT           DEFAULT 0,
  `surplus_items`   INT           DEFAULT 0,
  `loss_items`      INT           DEFAULT 0,
  `remark`          VARCHAR(500)  DEFAULT NULL,
  `version`         BIGINT        DEFAULT 0,
  `created_at`      TIMESTAMP     DEFAULT NULL,
  `updated_at`      TIMESTAMP     DEFAULT NULL,
  `deleted`         BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stocktaking_no` (`stocktaking_no`)
);

-- 43. stocktaking_item
DROP TABLE IF EXISTS `stocktaking_item`;
CREATE TABLE `stocktaking_item` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT,
  `stocktaking_id`   BIGINT        NOT NULL,
  `drug_code`        VARCHAR(64)   NOT NULL,
  `drug_name`        VARCHAR(255)  DEFAULT NULL,
  `batch_no`         VARCHAR(64)   DEFAULT NULL,
  `book_quantity`    DECIMAL(12,2) DEFAULT NULL,
  `actual_quantity`  DECIMAL(12,2) DEFAULT NULL,
  `difference`       DECIMAL(12,2) DEFAULT NULL,
  `difference_type`  VARCHAR(20)   DEFAULT NULL,
  `unit`             VARCHAR(20)   DEFAULT NULL,
  `remark`           VARCHAR(500)  DEFAULT NULL,
  `version`          BIGINT        DEFAULT 0,
  `created_at`       TIMESTAMP     DEFAULT NULL,
  `updated_at`       TIMESTAMP     DEFAULT NULL,
  `deleted`          BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`)
);

-- 44. transfer_order
DROP TABLE IF EXISTS `transfer_order`;
CREATE TABLE `transfer_order` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `transfer_no`     VARCHAR(32)   NOT NULL,
  `transfer_type`   VARCHAR(20)   NOT NULL,
  `status`          VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
  `source_dept`     VARCHAR(64)   DEFAULT NULL,
  `target_dept`     VARCHAR(64)   DEFAULT NULL,
  `applicant_id`    BIGINT        DEFAULT NULL,
  `applicant_name`  VARCHAR(64)   DEFAULT NULL,
  `approver_id`     BIGINT        DEFAULT NULL,
  `approver_name`   VARCHAR(64)   DEFAULT NULL,
  `approved_at`     TIMESTAMP     DEFAULT NULL,
  `shipped_at`      TIMESTAMP     DEFAULT NULL,
  `received_at`     TIMESTAMP     DEFAULT NULL,
  `total_items`     INT           DEFAULT 0,
  `total_amount`    DECIMAL(10,2) DEFAULT NULL,
  `reject_reason`   VARCHAR(500)  DEFAULT NULL,
  `remark`          VARCHAR(500)  DEFAULT NULL,
  `version`         BIGINT        DEFAULT 0,
  `created_at`      TIMESTAMP     DEFAULT NULL,
  `updated_at`      TIMESTAMP     DEFAULT NULL,
  `deleted`         BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transfer_no` (`transfer_no`)
);

-- 45. transfer_item
DROP TABLE IF EXISTS `transfer_item`;
CREATE TABLE `transfer_item` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `transfer_id`     BIGINT        NOT NULL,
  `drug_code`       VARCHAR(64)   NOT NULL,
  `drug_name`       VARCHAR(255)  NOT NULL,
  `specification`   VARCHAR(255)  DEFAULT NULL,
  `batch_no`        VARCHAR(64)   DEFAULT NULL,
  `quantity`        DECIMAL(12,2) NOT NULL,
  `unit`            VARCHAR(20)   DEFAULT NULL,
  `unit_price`      DECIMAL(10,2) DEFAULT NULL,
  `amount`          DECIMAL(10,2) DEFAULT NULL,
  `remark`          VARCHAR(500)  DEFAULT NULL,
  `version`         BIGINT        DEFAULT 0,
  `created_at`      TIMESTAMP     DEFAULT NULL,
  `updated_at`      TIMESTAMP     DEFAULT NULL,
  `deleted`         BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`)
);

-- 46. offline_registration
DROP TABLE IF EXISTS `offline_registration`;
CREATE TABLE `offline_registration` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT,
  `registration_no`   VARCHAR(32)   NOT NULL,
  `patient_id`        BIGINT        DEFAULT NULL,
  `patient_name`      VARCHAR(64)   NOT NULL,
  `patient_phone`     VARCHAR(20)   DEFAULT NULL,
  `id_card`           VARCHAR(32)   DEFAULT NULL,
  `doctor_id`         BIGINT        DEFAULT NULL,
  `doctor_name`       VARCHAR(64)   DEFAULT NULL,
  `department`        VARCHAR(64)   DEFAULT NULL,
  `registration_type` VARCHAR(20)   NOT NULL DEFAULT 'OUTPATIENT',
  `status`            VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
  `registration_fee`  DECIMAL(10,2) DEFAULT NULL,
  `operator_id`       BIGINT        DEFAULT NULL,
  `operator_name`     VARCHAR(64)   DEFAULT NULL,
  `cancel_reason`     VARCHAR(500)  DEFAULT NULL,
  `cancel_time`       TIMESTAMP     DEFAULT NULL,
  `remark`            VARCHAR(500)  DEFAULT NULL,
  `version`           BIGINT        DEFAULT 0,
  `created_at`        TIMESTAMP     DEFAULT NULL,
  `updated_at`        TIMESTAMP     DEFAULT NULL,
  `deleted`           BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_offline_reg_no` (`registration_no`)
);

-- 47. payment_record
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT,
  `payment_no`        VARCHAR(32)   NOT NULL,
  `patient_id`        BIGINT        DEFAULT NULL,
  `patient_name`      VARCHAR(64)   DEFAULT NULL,
  `source_id`         BIGINT        DEFAULT NULL,
  `source_type`       VARCHAR(20)   DEFAULT NULL,
  `source_no`         VARCHAR(32)   DEFAULT NULL,
  `total_amount`      DECIMAL(10,2) NOT NULL,
  `paid_amount`       DECIMAL(10,2) DEFAULT NULL,
  `refund_amount`     DECIMAL(10,2) DEFAULT 0,
  `status`            VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
  `payment_method`    VARCHAR(20)   DEFAULT NULL,
  `payer_name`        VARCHAR(64)   DEFAULT NULL,
  `operator_id`       BIGINT        DEFAULT NULL,
  `operator_name`     VARCHAR(64)   DEFAULT NULL,
  `paid_at`           TIMESTAMP     DEFAULT NULL,
  `refunded_at`       TIMESTAMP     DEFAULT NULL,
  `reconciled_at`     TIMESTAMP     DEFAULT NULL,
  `refund_reason`     VARCHAR(500)  DEFAULT NULL,
  `reconcile_batch_no` VARCHAR(32)  DEFAULT NULL,
  `remark`            VARCHAR(500)  DEFAULT NULL,
  `version`           BIGINT        DEFAULT 0,
  `created_at`        TIMESTAMP     DEFAULT NULL,
  `updated_at`        TIMESTAMP     DEFAULT NULL,
  `deleted`           BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`)
);

-- 48. payment_item
DROP TABLE IF EXISTS `payment_item`;
CREATE TABLE `payment_item` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `payment_id`      BIGINT        NOT NULL,
  `item_type`       VARCHAR(20)   NOT NULL,
  `item_name`       VARCHAR(255)  NOT NULL,
  `quantity`        DECIMAL(10,2) DEFAULT 1,
  `unit_price`      DECIMAL(10,2) NOT NULL,
  `amount`          DECIMAL(10,2) NOT NULL,
  `remark`          VARCHAR(500)  DEFAULT NULL,
  `version`         BIGINT        DEFAULT 0,
  `created_at`      TIMESTAMP     DEFAULT NULL,
  `updated_at`      TIMESTAMP     DEFAULT NULL,
  `deleted`         BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`)
);

-- 49. health_record
DROP TABLE IF EXISTS `health_record`;
CREATE TABLE `health_record` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `patient_id`      BIGINT        NOT NULL,
  `record_type`     VARCHAR(30)   NOT NULL,
  `record_category` VARCHAR(30)   DEFAULT NULL,
  `title`           VARCHAR(255)  NOT NULL,
  `content`         TEXT          DEFAULT NULL,
  `organization`    VARCHAR(128)  DEFAULT NULL,
  `department`      VARCHAR(64)   DEFAULT NULL,
  `doctor_name`     VARCHAR(64)   DEFAULT NULL,
  `source_id`       BIGINT        DEFAULT NULL,
  `source_table`    VARCHAR(64)   DEFAULT NULL,
  `report_data`     TEXT          DEFAULT NULL,
  `record_date`     DATE          DEFAULT NULL,
  `remark`          VARCHAR(500)  DEFAULT NULL,
  `version`         BIGINT        DEFAULT 0,
  `created_at`      TIMESTAMP     DEFAULT NULL,
  `updated_at`      TIMESTAMP     DEFAULT NULL,
  `deleted`         BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`)
);

SET REFERENTIAL_INTEGRITY TRUE;
