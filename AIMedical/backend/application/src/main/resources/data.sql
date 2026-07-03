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
(7,  2,    'menu:appointment',  '预约管理',   '预约管理',         true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 5, true, 'MENU',      'calendar',     '/appointment'),
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
(30, 29,   'menu:user',         '用户管理',   '用户管理菜单',     true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 1, true, 'MENU',     'user',         '/system/user'),
(31, 29,   'menu:role',         '角色管理',   '角色管理菜单',     true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 2, true, 'MENU',     'role',         '/system/role'),
(32, 29,   'menu:menu',         '菜单管理',   '菜单管理菜单',     true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 3, true, 'MENU',     'menu',         '/system/menu');

-- 密码统一 password123 (BCrypt)
MERGE INTO sys_user (id, username, password, nickname, phone, email, enabled, password_change_required, token_version, user_type, deleted, created_at, updated_at) KEY(id) VALUES
(1, 'admin',      '$2a$10$S2kRnxEIV3e8UuvncH3cGuOhu1XSdaVJuwg9f3T6gfPmWeJsFOCYq', '系统管理员', '13800138001', 'admin@aimedical.com',      true, false, 0, 'ADMIN',   false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'doctor001',  '$2a$10$S2kRnxEIV3e8UuvncH3cGuOhu1XSdaVJuwg9f3T6gfPmWeJsFOCYq', '张医生',     '13800138002', 'doctor001@aimedical.com', true, false, 0, 'DOCTOR',  false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, '13800138003', '$2a$10$S2kRnxEIV3e8UuvncH3cGuOhu1XSdaVJuwg9f3T6gfPmWeJsFOCYq', '李患者',     '13800138003', 'patient001@aimedical.com',true, false, 0, 'PATIENT', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

MERGE INTO user_role (user_id, role_id) KEY(user_id, role_id) VALUES
(1, 1), (2, 2), (3, 3);

MERGE INTO user_post (user_id, post_id) KEY(user_id, post_id) VALUES
(1, 1), (2, 2), (3, 3);

MERGE INTO post_function (post_id, function_id) KEY(post_id, function_id) VALUES
-- 管理员：全部功能（1-32）
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
(1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16),
(1, 17), (1, 18), (1, 19), (1, 20), (1, 21), (1, 22), (1, 23), (1, 24),
(1, 25), (1, 26), (1, 27), (1, 28), (1, 29), (1, 30), (1, 31), (1, 32),
-- 医生：诊疗 + AI + 药房 + 药库 + 窗口 + 健康档案（1-28，不含系统管理 29-32）
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8),
(2, 9), (2, 10), (2, 11), (2, 12), (2, 13), (2, 14), (2, 15), (2, 16),
(2, 17), (2, 18), (2, 19), (2, 20), (2, 21), (2, 22), (2, 23), (2, 24),
(2, 25), (2, 26), (2, 27), (2, 28),
-- 患者：仅仪表盘
(3, 1);

-- 重置自增计数器，避免后续业务 INSERT 主键冲突
-- 各表当前最大 ID：sys_role=3, sys_post=3, sys_function=32, sys_user=3
ALTER TABLE sys_role ALTER COLUMN id RESTART WITH 4;
ALTER TABLE sys_post ALTER COLUMN id RESTART WITH 4;
ALTER TABLE sys_function ALTER COLUMN id RESTART WITH 33;
ALTER TABLE sys_user ALTER COLUMN id RESTART WITH 4;

-- Phase3 种子数据：医生档案（doctor_profile）
INSERT INTO doctor_profile (id, user_id, real_name, title, department, deleted, created_at, updated_at) VALUES
(1, 2, '张医生', '副主任医师', '内科', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 重置 doctor_profile 自增计数器，避免后续业务 INSERT 主键冲突
ALTER TABLE doctor_profile ALTER COLUMN id RESTART WITH 2;

-- Phase2 种子数据：患者档案、过敏史、慢病史、挂号、导诊记录
MERGE INTO patient_profile (id, user_id, real_name, gender, phone, emergency_contact, avatar_url, deleted, created_at, updated_at) KEY(id) VALUES
(1, 3, '李明', 'MALE', '13800138003', '王芳 13700000001', NULL, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

MERGE INTO patient_allergy (id, patient_id, allergen, reaction_type, severity, occurred_at, deleted, created_at, updated_at) KEY(id) VALUES
(1, 1, '青霉素', '皮疹', 'MILD', '2015-03-10', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

MERGE INTO patient_chronic_disease (id, patient_id, disease_name, diagnosed_at, current_status, deleted, created_at, updated_at) KEY(id) VALUES
(1, 1, '高血压', '2022-01-15', 'STABLE', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

MERGE INTO registration (id, patient_id, registration_type, department, scheduled_date, scheduled_time_slot, status, deleted, created_at, updated_at) KEY(id) VALUES
(1, 1, 'OUTPATIENT', '神经内科', '2026-07-01', '08:00-08:30', 'PENDING', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 1, 'EXAMINATION', NULL, '2026-07-02', '10:30-11:00', 'CONFIRMED', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 1, 'OUTPATIENT', '普通内科', '2026-07-01', '15:00-15:30', 'COMPLETED', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

MERGE INTO triage_record (id, patient_id, chief_complaint, session_id, recommended_departments, recommended_doctors, is_degraded, rule_version, rule_set_id, matched_rules, deleted, created_at, updated_at) KEY(id) VALUES
(1, 3, '头痛3天，伴有恶心，前额搏动性疼痛', 'mock-session-001', '神经内科,普通内科,中医科', '王主任,张副主任,李主治医师', false, 'v1.0.0', 'rule-set-neuro', '头痛规则-偏头痛,头痛规则-紧张性', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 3, '发烧2天，体温38.5°C，咳嗽咽痛', 'mock-session-002', '呼吸内科,普通内科,感染科', '王主任,李主治医师', false, 'v1.0.0', 'rule-set-resp', '发热规则-上感,咳嗽规则', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 3, '腹痛1天，右下腹持续性疼痛', 'mock-degraded-001', '普通内科', '张副主任', true, 'v1.0.0', 'rule-set-abd', '', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
