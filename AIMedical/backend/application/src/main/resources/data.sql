-- AIMedical 种子数据 (H2 MERGE INTO, 幂等 — DevTools 重启安全)
-- MERGE INTO KEY(id)  = 有则跳过，无才插入，不覆盖已存在数据

MERGE INTO sys_role (id, code, name, description, enabled, sort, deleted, created_at, updated_at) KEY(id) VALUES
(1, 'ADMIN',  '系统管理员', '拥有系统全部权限',         true, 1, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'DOCTOR', '医生',      '医生角色，拥有医生端功能权限', true, 2, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 'PATIENT', '患者',     '患者角色，拥有患者端功能权限', true, 3, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

MERGE INTO sys_post (id, code, name, description, enabled, sort, role_id, deleted, created_at, updated_at) KEY(id) VALUES
(1, 'SUPER_ADMIN',    '超级管理员', '系统超级管理员岗位', true, 1, 1, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'DOCTOR_GENERAL', '普通医生',   '普通医生岗位',       true, 2, 2, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 'PATIENT_GENERAL', '普通患者',  '普通患者岗位',       true, 3, 3, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

MERGE INTO sys_function (id, parent_id, code, name, description, enabled, deleted, created_at, updated_at, sort_order, visible, type, icon, path) KEY(id) VALUES
-- 顶层菜单
(1,  NULL, 'menu:dashboard',    '仪表盘',     '查看仪表盘',       true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, true, 'MENU',      'dashboard',    '/dashboard'),
-- 诊疗管理目录
(2,  NULL, 'menu:clinic',       '诊疗管理',   '门诊诊疗工作台',   true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 2, true, 'DIRECTORY', 'stethoscope',  '/clinic'),
(3,  2,    'menu:queue',        '叫号台',     '医生叫号台',       true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, true, 'MENU',      'bell',         '/queue'),
(4,  2,    'menu:patient',      '患者管理',   '患者列表',         true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 2, true, 'MENU',      'user-friend',  '/patient'),
(5,  2,    'menu:registration', '挂号管理',   '挂号记录管理',     true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 3, true, 'MENU',      'registration', '/registration'),
(6,  2,    'menu:prescriptions','我的处方',   '处方列表',         true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 4, true, 'MENU',      'edit-square',  '/prescriptions'),
-- AI 辅助目录
(8,  NULL, 'menu:ai',           'AI 辅助',    '人工智能辅助诊疗', true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 3, true, 'DIRECTORY', 'robot',        '/ai'),
(9,  8,    'menu:ai-diagnosis', 'AI 诊断',    'AI 辅助诊断',      true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, true, 'MENU',      'brain',        '/ai/diagnosis'),
(10, 8,    'menu:ai-examination','AI 检查推荐','AI 检查项推荐',   true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 2, true, 'MENU',      'search',       '/ai/examination'),
(11, 8,    'menu:ai-prescription-assist','AI 辅助开方','AI 处方辅助',true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 3, true, 'MENU',     'edit',         '/ai/prescription-assist'),
(12, 8,    'menu:ai-prescription-audit','AI 处方审核','AI 处方审核',true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 4, true, 'MENU',     'check',        '/ai/prescription-audit'),
(13, 8,    'menu:ai-medical-record-gen','AI 病历生成','AI 病历生成',true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 5, true, 'MENU',     'file-text',    '/ai/medical-record-gen'),
-- 药房管理目录
(14, NULL, 'menu:pharmacy',     '药房管理',   '药房工作台',       true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 4, true, 'DIRECTORY', 'medicine',     '/pharmacy'),
(15, 14,   'menu:pharmacy-dispense',   '发药工作台', '药房发药', true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, true, 'MENU', 'check',          '/pharmacy/dispense'),
(16, 14,   'menu:pharmacy-refund',     '退药处理',   '药房退药', true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 2, true, 'MENU', 'rollback',       '/pharmacy/refund'),
(17, 14,   'menu:pharmacy-drugs',      '药品目录',   '药品查询', true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 3, true, 'MENU', 'list',           '/pharmacy/drugs'),
-- 药库管理目录
(18, NULL, 'menu:inventory',    '药库管理',   '药库管理',         true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 5, true, 'DIRECTORY', 'box',          '/inventory'),
(19, 18,   'menu:inventory-stock',     '库存查询',   '药库库存', true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, true, 'MENU', 'search',         '/inventory/stock'),
(20, 18,   'menu:inventory-stocktaking','盘点管理',  '药库盘点', true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 2, true, 'MENU', 'clipboard',      '/inventory/stocktaking'),
(21, 18,   'menu:inventory-transfer',  '调拨管理',   '药库调拨', true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 3, true, 'MENU', 'swap',           '/inventory/transfer'),
-- 窗口服务目录
(22, NULL, 'menu:window',       '窗口服务',   '线下窗口服务',     true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 6, true, 'DIRECTORY', 'shop',         '/window'),
(23, 22,   'menu:window-registration', '线下挂号',   '窗口挂号', true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, true, 'MENU', 'user-add',       '/window/registration'),
(24, 22,   'menu:window-charging',     '收费退费',   '窗口收费', true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 2, true, 'MENU', 'money-collect',  '/window/charging'),
(25, 22,   'menu:window-payments',     '缴费记录',   '缴费查询', true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 3, true, 'MENU', 'file-done',      '/window/payments'),
-- 健康档案目录
(26, NULL, 'menu:health-record','健康档案',   '健康档案管理',     true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 7, true, 'DIRECTORY', 'heart',        '/health-record'),
(27, 26,   'menu:health-record-query', '档案查询',   '健康档案查询', true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, true, 'MENU', 'search',       '/health-record/query'),
(28, 26,   'menu:health-record-trend', '健康趋势',   '长期健康趋势', true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 2, true, 'MENU', 'line-chart',   '/health-record/trend'),
-- 系统管理目录（仅管理员可见）
(29, NULL, 'menu:system',       '系统管理',   '系统管理',         true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 8, true, 'DIRECTORY', 'setting',      '/system'),
(30, 29,   'menu:user',         '用户管理',   '用户管理菜单',     true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, true, 'MENU',     'user',         '/system/users'),
(31, 29,   'menu:role',         '角色管理',   '角色管理菜单',     true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 2, true, 'MENU',     'role',         '/system/roles'),
(32, 29,   'menu:menu',         '菜单管理',   '菜单管理菜单',     true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 3, true, 'MENU',     'menu',         '/system/menus'),
-- 数据查看目录（仅管理员可见，用于查看患者与处方全量数据）
(33, NULL, 'menu:data-view',    '数据查看',   '全量数据查看',     true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 2, true, 'DIRECTORY', 'data-view',   '/data-view'),
(34, 33,   'menu:patients',     '患者管理',   '患者列表（全量）', true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, true, 'MENU',      'user-friend', '/patients'),
(35, 33,   'menu:data-prescriptions','处方查询','处方列表（全量）',true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 2, true, 'MENU',      'edit-square', '/prescriptions');

-- 密码统一 password123 (BCrypt)
MERGE INTO sys_user (id, username, password, nickname, phone, email, enabled, password_change_required, token_version, user_type, deleted, created_at, updated_at) KEY(id) VALUES
(1, 'admin',      '$2a$10$S2kRnxEIV3e8UuvncH3cGuOhu1XSdaVJuwg9f3T6gfPmWeJsFOCYq', '系统管理员', '13800138001', 'admin@aimedical.com',      true, false, 0, 'ADMIN',   false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'doctor001',  '$2a$10$S2kRnxEIV3e8UuvncH3cGuOhu1XSdaVJuwg9f3T6gfPmWeJsFOCYq', '张医生',     '13800138002', 'doctor001@aimedical.com', true, false, 0, 'DOCTOR',  false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, '13800138003', '$2a$10$S2kRnxEIV3e8UuvncH3cGuOhu1XSdaVJuwg9f3T6gfPmWeJsFOCYq', '李患者',     '13800138003', 'patient001@aimedical.com',true, false, 0, 'PATIENT', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- 新增测试医生（不同科室，便于三端联动验证）
(4, 'doctor002',  '$2a$10$S2kRnxEIV3e8UuvncH3cGuOhu1XSdaVJuwg9f3T6gfPmWeJsFOCYq', '李医生',     '13800138004', 'doctor002@aimedical.com', true, false, 0, 'DOCTOR',  false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, 'doctor003',  '$2a$10$S2kRnxEIV3e8UuvncH3cGuOhu1XSdaVJuwg9f3T6gfPmWeJsFOCYq', '王医生',     '13800138005', 'doctor003@aimedical.com', true, false, 0, 'DOCTOR',  false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- 新增测试患者（覆盖不同性别/年龄，便于管理员端查看与医生端接诊）
(6, '13800138006', '$2a$10$S2kRnxEIV3e8UuvncH3cGuOhu1XSdaVJuwg9f3T6gfPmWeJsFOCYq', '王芳',       '13800138006', 'patient002@aimedical.com',true, false, 0, 'PATIENT', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(7, '13800138007', '$2a$10$S2kRnxEIV3e8UuvncH3cGuOhu1XSdaVJuwg9f3T6gfPmWeJsFOCYq', '赵强',       '13800138007', 'patient003@aimedical.com',true, false, 0, 'PATIENT', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(8, '13800138008', '$2a$10$S2kRnxEIV3e8UuvncH3cGuOhu1XSdaVJuwg9f3T6gfPmWeJsFOCYq', '孙丽',       '13800138008', 'patient004@aimedical.com',true, false, 0, 'PATIENT', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(9, '13800138009', '$2a$10$S2kRnxEIV3e8UuvncH3cGuOhu1XSdaVJuwg9f3T6gfPmWeJsFOCYq', '周明',       '13800138009', 'patient005@aimedical.com',true, false, 0, 'PATIENT', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

MERGE INTO user_role (user_id, role_id) KEY(user_id, role_id) VALUES
(1, 1), (2, 2), (3, 3),
(4, 2), (5, 2),
(6, 3), (7, 3), (8, 3), (9, 3);

MERGE INTO user_post (user_id, post_id) KEY(user_id, post_id) VALUES
(1, 1), (2, 2), (3, 3),
(4, 2), (5, 2),
(6, 3), (7, 3), (8, 3), (9, 3);

MERGE INTO post_function (post_id, function_id) KEY(post_id, function_id) VALUES
-- 管理员：仪表盘 + 数据查看 + 药房 + 药库 + 窗口 + 健康档案 + 系统管理
-- 移除：诊疗管理(2-6)、AI辅助(8-13) 等医生端操作菜单
(1, 1),
(1, 14), (1, 15), (1, 16), (1, 17),
(1, 18), (1, 19), (1, 20), (1, 21),
(1, 22), (1, 23), (1, 24), (1, 25),
(1, 26), (1, 27), (1, 28),
(1, 29), (1, 30), (1, 31), (1, 32),
(1, 33), (1, 34), (1, 35),
-- 医生：诊疗 + AI + 药房 + 药库 + 窗口 + 健康档案（1-28，不含 7=appointment 与 29-35 系统管理/数据查看）
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 8),
(2, 9), (2, 10), (2, 11), (2, 12), (2, 13), (2, 14), (2, 15), (2, 16),
(2, 17), (2, 18), (2, 19), (2, 20), (2, 21), (2, 22), (2, 23), (2, 24),
(2, 25), (2, 26), (2, 27), (2, 28),
-- 患者：仅仪表盘
(3, 1);

-- 重置自增计数器，避免后续业务 INSERT 主键冲突
-- 各表当前最大 ID：sys_role=3, sys_post=3, sys_function=35, sys_user=9
ALTER TABLE sys_role ALTER COLUMN id RESTART WITH 4;
ALTER TABLE sys_post ALTER COLUMN id RESTART WITH 4;
ALTER TABLE sys_function ALTER COLUMN id RESTART WITH 36;
ALTER TABLE sys_user ALTER COLUMN id RESTART WITH 10;

-- Phase3 种子数据：医生档案（doctor_profile）
MERGE INTO doctor_profile (id, user_id, real_name, title, department, deleted, created_at, updated_at) KEY(id) VALUES
(1, 2, '张医生', '副主任医师', '内科', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 4, '李医生', '主治医师',   '儿科', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 5, '王医生', '主任医师',   '外科', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 重置 doctor_profile 自增计数器，避免后续业务 INSERT 主键冲突
ALTER TABLE doctor_profile ALTER COLUMN id RESTART WITH 4;

-- Phase2 种子数据：患者档案、过敏史、慢病史、挂号、导诊记录
MERGE INTO patient_profile (id, user_id, real_name, gender, phone, emergency_contact, avatar_url, deleted, created_at, updated_at) KEY(id) VALUES
(1, 3, '李明', 'MALE', '13800138003', '王芳 13700000001', NULL, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 6, '王芳', 'FEMALE', '13800138006', '李明 13800138003', NULL, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 7, '赵强', 'MALE',   '13800138007', '赵母 13700000002', NULL, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 8, '孙丽', 'FEMALE', '13800138008', '孙父 13700000003', NULL, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, 9, '周明', 'MALE',   '13800138009', '周妻 13700000004', NULL, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

MERGE INTO patient_allergy (id, patient_id, allergen, reaction_type, severity, occurred_at, deleted, created_at, updated_at) KEY(id) VALUES
(1, 1, '青霉素', '皮疹', 'MILD', '2015-03-10', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

MERGE INTO patient_chronic_disease (id, patient_id, disease_name, diagnosed_at, current_status, deleted, created_at, updated_at) KEY(id) VALUES
(1, 1, '高血压', '2022-01-15', 'STABLE', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 2, '糖尿病', '2021-06-20', 'STABLE', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 5, '冠心病', '2023-03-10', 'STABLE', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 重置 patient_profile 自增计数器，避免后续业务 INSERT 主键冲突
ALTER TABLE patient_profile ALTER COLUMN id RESTART WITH 6;

MERGE INTO registration (id, patient_id, registration_type, department, scheduled_date, scheduled_time_slot, status, deleted, created_at, updated_at) KEY(id) VALUES
(1, 1, 'OUTPATIENT', '神经内科', '2026-07-01', '08:00-08:30', 'PENDING', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 1, 'EXAMINATION', NULL, '2026-07-02', '10:30-11:00', 'CONFIRMED', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 1, 'OUTPATIENT', '普通内科', '2026-07-01', '15:00-15:30', 'COMPLETED', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- 新增测试挂号（覆盖不同患者/科室/状态，便于三端联动）
(4, 2, 'OUTPATIENT', '内科',     '2026-07-04', '09:00-09:30', 'PENDING',   false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, 2, 'OUTPATIENT', '内科',     '2026-07-03', '14:00-14:30', 'COMPLETED', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(6, 3, 'EMERGENCY',  '急诊',     '2026-07-04', '00:00-00:30', 'COMPLETED', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(7, 4, 'OUTPATIENT', '儿科',     '2026-07-04', '10:00-10:30', 'CONFIRMED', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(8, 5, 'OUTPATIENT', '外科',     '2026-07-04', '11:00-11:30', 'PENDING',   false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 重置 registration 自增计数器
ALTER TABLE registration ALTER COLUMN id RESTART WITH 9;

MERGE INTO triage_record (id, patient_id, chief_complaint, session_id, recommended_departments, recommended_doctors, is_degraded, rule_version, rule_set_id, matched_rules, deleted, created_at, updated_at) KEY(id) VALUES
(1, 3, '头痛3天，伴有恶心，前额搏动性疼痛', 'mock-session-001', '神经内科,普通内科,中医科', '王主任,张副主任,李主治医师', false, 'v1.0.0', 'rule-set-neuro', '头痛规则-偏头痛,头痛规则-紧张性', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 3, '发烧2天，体温38.5°C，咳嗽咽痛', 'mock-session-002', '呼吸内科,普通内科,感染科', '王主任,李主治医师', false, 'v1.0.0', 'rule-set-resp', '发热规则-上感,咳嗽规则', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 3, '腹痛1天，右下腹持续性疼痛', 'mock-degraded-001', '普通内科', '张副主任', true, 'v1.0.0', 'rule-set-abd', '', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- ===========================================================================
-- Phase4 种子数据：药房 / 药库 / 窗口 业务数据（用于前端测试）
-- ===========================================================================

-- 药品目录（drug_catalog）
MERGE INTO drug_catalog (id, drug_code, drug_name, generic_name, specification, manufacturer, drug_form, drug_category, unit, retail_price, purchase_price, otc_flag, enabled, version, deleted, created_at, updated_at) KEY(id) VALUES
(1, 'AMX001', '阿莫西林胶囊', '阿莫西林', '0.25g*24粒', '华北制药', 'CAPSULE', 'WESTERN_MEDICINE', '盒', 25.00, 15.00, TRUE, TRUE, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'IBU001', '布洛芬片', '布洛芬', '0.2g*20片', '中美史克', 'TABLET', 'WESTERN_MEDICINE', '盒', 15.00, 8.00, TRUE, TRUE, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 'BLG001', '板蓝根颗粒', '板蓝根', '10g*20袋', '白云山', 'GRANULE', 'CHINESE_MEDICINE', '盒', 12.00, 6.00, TRUE, TRUE, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 'INS001', '胰岛素注射液', '胰岛素', '10ml:400单位', '诺和诺德', 'INJECTION', 'BIOLOGICAL', '支', 85.00, 60.00, FALSE, TRUE, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, 'SYR001', '一次性注射器', NULL, '5ml', '康德莱', 'DEVICE', 'DEVICE', '个', 2.50, 1.20, FALSE, TRUE, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(6, 'CCL001', '头孢克洛胶囊', '头孢克洛', '0.25g*12粒', '礼来', 'CAPSULE', 'WESTERN_MEDICINE', '盒', 35.00, 22.00, FALSE, TRUE, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 药库库存（inventory_stock）
MERGE INTO inventory_stock (id, drug_code, batch_no, quantity, unit, purchase_price, retail_price, expiry_date, production_date, warehouse_location, version, deleted, created_at, updated_at) KEY(id) VALUES
(1, 'AMX001', 'AMX202601', 1000, '盒', 15.00, 25.00, '2027-06-30', '2026-01-15', 'A-01-01', 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'IBU001', 'IBU202602', 800, '盒', 8.00, 15.00, '2027-12-31', '2026-02-20', 'A-01-02', 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 'BLG001', 'BLG202603', 500, '盒', 6.00, 12.00, '2027-09-30', '2026-03-10', 'A-02-01', 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 'INS001', 'INS202604', 200, '支', 60.00, 85.00, '2027-03-31', '2026-04-05', 'B-01-01', 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, 'SYR001', 'SYR202605', 2000, '个', 1.20, 2.50, '2028-01-31', '2026-05-12', 'B-02-01', 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(6, 'CCL001', 'CCL202606', 600, '盒', 22.00, 35.00, '2027-08-31', '2026-06-01', 'A-03-01', 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 药房库存（pharmacy_stock）
MERGE INTO pharmacy_stock (id, drug_code, drug_name, batch_no, quantity, unit, retail_price, expiry_date, shelf_location, safety_stock, version, deleted, created_at, updated_at) KEY(id) VALUES
(1, 'AMX001', '阿莫西林胶囊', 'AMX202601', 100, '盒', 25.00, '2027-06-30', 'P-01-01', 20, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'IBU001', '布洛芬片', 'IBU202602', 80, '盒', 15.00, '2027-12-31', 'P-01-02', 15, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 'BLG001', '板蓝根颗粒', 'BLG202603', 50, '盒', 12.00, '2027-09-30', 'P-02-01', 10, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 'CCL001', '头孢克洛胶囊', 'CCL202606', 60, '盒', 35.00, '2027-08-31', 'P-03-01', 10, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 发药记录（dispensing_record）
MERGE INTO dispensing_record (id, dispensing_no, patient_id, patient_name, pharmacist_id, pharmacist_name, status, total_quantity, total_amount, dispensed_at, version, deleted, created_at, updated_at) KEY(id) VALUES
(1, 'DISP20260704001', 1, '李明', 2, '张医生', 'PENDING', 3, 65.00, NULL, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'DISP20260704002', 1, '李明', 2, '张医生', 'DISPENSED', 3, 36.00, CURRENT_TIMESTAMP(), 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 'DISP20260704003', 1, '李明', 2, '张医生', 'DISPENSED', 2, 60.00, CURRENT_TIMESTAMP(), 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 发药明细（dispensing_item）
MERGE INTO dispensing_item (id, dispensing_id, drug_code, drug_name, specification, batch_no, quantity, unit, unit_price, amount, dosage, usage_method, frequency, days, version, deleted, created_at, updated_at) KEY(id) VALUES
(1, 1, 'AMX001', '阿莫西林胶囊', '0.25g*24粒', 'AMX202601', 2, '盒', 25.00, 50.00, '每次1粒', '口服', '每日3次', 5, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 1, 'IBU001', '布洛芬片', '0.2g*20片', 'IBU202602', 1, '盒', 15.00, 15.00, '每次1片', '口服', '必要时', 3, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 2, 'BLG001', '板蓝根颗粒', '10g*20袋', 'BLG202603', 3, '盒', 12.00, 36.00, '每次1袋', '冲服', '每日2次', 5, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 3, 'CCL001', '头孢克洛胶囊', '0.25g*12粒', 'CCL202606', 2, '盒', 35.00, 70.00, '每次1粒', '口服', '每日2次', 7, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, 3, 'AMX001', '阿莫西林胶囊', '0.25g*24粒', 'AMX202601', 1, '盒', 25.00, 25.00, '每次1粒', '口服', '每日3次', 5, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 退药记录（pharmacy_refund_record）
MERGE INTO pharmacy_refund_record (id, refund_no, dispensing_id, patient_id, patient_name, pharmacist_id, pharmacist_name, status, refund_reason, total_quantity, total_amount, version, deleted, created_at, updated_at) KEY(id) VALUES
(1, 'REF20260704001', 2, 1, '李明', 2, '张医生', 'PENDING', '患者药物过敏需退药', 3, 36.00, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 退药明细（pharmacy_refund_item）
MERGE INTO pharmacy_refund_item (id, refund_id, dispensing_item_id, drug_code, drug_name, batch_no, quantity, unit, unit_price, amount, version, deleted, created_at, updated_at) KEY(id) VALUES
(1, 1, 3, 'BLG001', '板蓝根颗粒', 'BLG202603', 3, '盒', 12.00, 36.00, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 线下挂号（offline_registration）
MERGE INTO offline_registration (id, registration_no, patient_id, patient_name, patient_phone, id_card, doctor_id, doctor_name, department, registration_type, status, registration_fee, operator_id, operator_name, version, deleted, created_at, updated_at) KEY(id) VALUES
(1, 'REG20260704001', 1, '李明', '13800138003', '110101199001011234', 2, '张医生', '内科', 'OUTPATIENT', 'ACTIVE', 30.00, 2, '张医生', 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'REG20260704002', 1, '王芳', '13900139002', '110101198505056789', 2, '张医生', '内科', 'OUTPATIENT', 'ACTIVE', 30.00, 2, '张医生', 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 'REG20260704003', 1, '赵强', '13700137003', '110101199203031111', 2, '张医生', '急诊', 'EMERGENCY', 'CANCELLED', 50.00, 2, '张医生', 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 缴费记录（payment_record）
MERGE INTO payment_record (id, payment_no, patient_id, patient_name, source_id, source_type, source_no, total_amount, paid_amount, refund_amount, status, payment_method, payer_name, operator_id, operator_name, paid_at, version, deleted, created_at, updated_at) KEY(id) VALUES
(1, 'PAY20260704001', 1, '李明', 1, 'REGISTRATION', 'REG20260704001', 30.00, 30.00, 0.00, 'PAID', 'CASH', '李明', 2, '张医生', CURRENT_TIMESTAMP(), 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'PAY20260704002', 1, '李明', 2, 'DISPENSING', 'DISP20260704002', 36.00, 36.00, 0.00, 'PAID', 'WECHAT', '李明', 2, '张医生', CURRENT_TIMESTAMP(), 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 'PAY20260704003', 1, '王芳', 2, 'REGISTRATION', 'REG20260704002', 30.00, 0.00, 0.00, 'PENDING', 'CASH', '王芳', 2, '张医生', NULL, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 缴费明细（payment_item）
MERGE INTO payment_item (id, payment_id, item_type, item_name, quantity, unit_price, amount, version, deleted, created_at, updated_at) KEY(id) VALUES
(1, 1, 'REGISTRATION_FEE', '门诊挂号费', 1, 30.00, 30.00, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 2, 'DRUG_FEE', '板蓝根颗粒', 3, 12.00, 36.00, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 3, 'REGISTRATION_FEE', '门诊挂号费', 1, 30.00, 30.00, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 调拨单（transfer_order）
MERGE INTO transfer_order (id, transfer_no, transfer_type, status, source_dept, target_dept, applicant_id, applicant_name, approver_id, approver_name, approved_at, total_items, total_amount, version, deleted, created_at, updated_at) KEY(id) VALUES
(1, 'TRF20260704001', 'INVENTORY_TO_PHARMACY', 'APPROVED', '药库', '门诊药房', 2, '张医生', 1, '管理员', CURRENT_TIMESTAMP(), 1, 1250.00, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'TRF20260704002', 'INVENTORY_TO_PHARMACY', 'PENDING_APPROVAL', '药库', '门诊药房', 2, '张医生', NULL, NULL, NULL, 1, 450.00, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 调拨明细（transfer_item）
MERGE INTO transfer_item (id, transfer_id, drug_code, drug_name, specification, batch_no, quantity, unit, unit_price, amount, version, deleted, created_at, updated_at) KEY(id) VALUES
(1, 1, 'AMX001', '阿莫西林胶囊', '0.25g*24粒', 'AMX202601', 50, '盒', 25.00, 1250.00, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 2, 'IBU001', '布洛芬片', '0.2g*20片', 'IBU202602', 30, '盒', 15.00, 450.00, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 盘点单（stocktaking）
MERGE INTO stocktaking (id, stocktaking_no, stocktaking_type, status, operator_id, operator_name, start_time, total_items, surplus_items, loss_items, version, deleted, created_at, updated_at) KEY(id) VALUES
(1, 'STK20260704001', 'FULL', 'IN_PROGRESS', 2, '张医生', CURRENT_TIMESTAMP(), 2, 1, 1, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 盘点明细（stocktaking_item）
MERGE INTO stocktaking_item (id, stocktaking_id, drug_code, drug_name, batch_no, book_quantity, actual_quantity, difference, difference_type, unit, version, deleted, created_at, updated_at) KEY(id) VALUES
(1, 1, 'AMX001', '阿莫西林胶囊', 'AMX202601', 1000, 998, -2, 'LOSS', '盒', 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 1, 'IBU001', '布洛芬片', 'IBU202602', 800, 805, 5, 'SURPLUS', '盒', 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 重置自增计数器，避免后续业务 INSERT 主键冲突
ALTER TABLE drug_catalog ALTER COLUMN id RESTART WITH 7;
ALTER TABLE inventory_stock ALTER COLUMN id RESTART WITH 7;
ALTER TABLE pharmacy_stock ALTER COLUMN id RESTART WITH 5;
ALTER TABLE dispensing_record ALTER COLUMN id RESTART WITH 4;
ALTER TABLE dispensing_item ALTER COLUMN id RESTART WITH 6;
ALTER TABLE pharmacy_refund_record ALTER COLUMN id RESTART WITH 2;
ALTER TABLE pharmacy_refund_item ALTER COLUMN id RESTART WITH 2;
ALTER TABLE offline_registration ALTER COLUMN id RESTART WITH 4;
ALTER TABLE payment_record ALTER COLUMN id RESTART WITH 4;
ALTER TABLE payment_item ALTER COLUMN id RESTART WITH 4;
ALTER TABLE transfer_order ALTER COLUMN id RESTART WITH 3;
ALTER TABLE transfer_item ALTER COLUMN id RESTART WITH 3;
ALTER TABLE stocktaking ALTER COLUMN id RESTART WITH 2;
ALTER TABLE stocktaking_item ALTER COLUMN id RESTART WITH 3;

-- ===========================================================================
-- 处方种子数据（用于管理员端处方查询页面测试，三端数据互通）
-- ===========================================================================

-- 处方主表（prescription）
MERGE INTO prescription (id, patient_id, patient_name, doctor_id, department, status, diagnosis, ai_checked, ai_risk_level, audit_remark, audited_by, audited_at, remark, version, deleted, created_at, updated_at) KEY(id) VALUES
(1, 1, '李明', 2, '内科', 'APPROVED',       '上呼吸道感染',     TRUE,  'LOW',    '用药合理', 1, CURRENT_TIMESTAMP(), '患者主诉咳嗽3天', 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 2, '王芳', 4, '儿科', 'PENDING_REVIEW', '小儿支气管炎',     TRUE,  'MEDIUM', NULL,       NULL, NULL,                '需关注儿童剂量',  0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 3, '赵强', 5, '外科', 'REJECTED',       '阑尾炎术后抗感染', FALSE, NULL,     '剂量偏大，请调整后重新提交', 1, CURRENT_TIMESTAMP(), NULL,                0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 1, '李明', 2, '内科', 'DRAFT',          '高血压复查',       FALSE, NULL,     NULL,       NULL, NULL,                '常规复查处方',    0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 处方明细（prescription_item）
MERGE INTO prescription_item (id, prescription_id, drug_name, specification, dosage, usage_method, frequency, quantity, unit, remark, version, deleted, created_at, updated_at) KEY(id) VALUES
(1, 1, '阿莫西林胶囊', '0.25g*24粒', '每次1粒', '口服', '每日3次', 2, '盒', NULL, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 1, '布洛芬片',     '0.2g*20片',  '每次1片', '口服', '必要时',  1, '盒', NULL, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 2, '板蓝根颗粒',   '10g*20袋',   '每次1袋', '冲服', '每日2次', 3, '盒', '儿童减半', 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 3, '头孢克洛胶囊', '0.25g*12粒', '每次1粒', '口服', '每日2次', 4, '盒', NULL, 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, 4, '氨氯地平片',   '5mg*7片',    '每次1片', '口服', '每日1次', 1, '盒', '长效降压', 0, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 重置处方相关自增计数器
ALTER TABLE prescription ALTER COLUMN id RESTART WITH 5;
ALTER TABLE prescription_item ALTER COLUMN id RESTART WITH 6;
