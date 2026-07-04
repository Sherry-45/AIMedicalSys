-- 设备 Mock 种子数据
INSERT INTO device_info (device_code, device_name, device_type, protocol, status, manufacturer, model, location, connection_config) VALUES
('LAB-001', '全自动生化分析仪', 'LAB_EQUIPMENT', 'HL7', 'ONLINE', '罗氏', 'Cobas 8000', '检验科1楼', '{"host":"192.168.1.100","port":5000}'),
('IMG-001', 'CT扫描机', 'IMAGING_EQUIPMENT', 'DICOM', 'ONLINE', '西门子', 'SOMATOM', '放射科1楼', '{"aeTitle":"AIMEDICAL_CT","port":11112}'),
('MON-001', '心电监护仪', 'MONITOR', 'HL7', 'ONLINE', '飞利浦', 'IntelliVue', 'ICU 1号床', '{"host":"192.168.1.200","port":5001}'),
('LAB-002', '血细胞分析仪', 'LAB_EQUIPMENT', 'ASTM', 'OFFLINE', '希森美康', 'XN-3000', '检验科2楼', '{"host":"192.168.1.101","port":5002}'),
('IMG-002', 'MRI扫描机', 'IMAGING_EQUIPMENT', 'DICOM', 'MAINTENANCE', 'GE', 'Signa', '放射科2楼', '{"aeTitle":"AIMEDICAL_MRI","port":11113}');

-- device_message 种子数据（覆盖 HL7 / DICOM / ASTM 三种协议示例报文）
-- 设备 id 映射（device_info 自增主键）：LAB-001=1(HL7), IMG-001=2(DICOM), MON-001=3(HL7), LAB-002=4(ASTM), IMG-002=5(DICOM)
-- 注意：raw_content / parsed_content 中的反斜杠按 MySQL 转义规则处理——
--   raw_content 中单个 '\' 写作 '\\'；parsed_content 的 JSON 中 '\\'（两个反斜杠）写作 '\\\\'。
--   JSON 内部的双引号在单引号包裹的 SQL 字符串中无需转义（与现有 connection_config 写法一致）。

-- 1) HL7 ORU^R01 观察结果消息（LAB-001, device_id=1）
INSERT INTO device_message (device_id, message_type, protocol, raw_content, parsed_content, processed, received_at) VALUES
(1, 'OBSERVATION', 'HL7',
'MSH|^~\\&|Cobas8000|LAB|HIS|HOSP|20260701100000||ORU^R01|MSG00001|P|2.5
PID|1||PAT12345||ZhangSan||19800101|M
OBR|1||ORD001|GLU^BloodGlucose|||20260701100000
OBX|1|NM|GLU^BloodGlucose||5.6|mmol/L|3.9-6.1|N|||F',
'{"segments":[{"name":"MSH","fields":["^~\\\\&","Cobas8000","LAB","HIS","HOSP","20260701100000","","ORU^R01","MSG00001","P","2.5"]},{"name":"PID","fields":["1","","PAT12345","","ZhangSan","","19800101","M"]},{"name":"OBR","fields":["1","","ORD001","GLU^BloodGlucose","","","20260701100000"]},{"name":"OBX","fields":["1","NM","GLU^BloodGlucose","","5.6","mmol/L","3.9-6.1","N","","","F"]}]}',
0, '2026-07-01 10:00:00');

-- 2) DICOM dump 影像消息（IMG-001, device_id=2）
INSERT INTO device_message (device_id, message_type, protocol, raw_content, parsed_content, processed, received_at) VALUES
(2, 'IMAGE_DICOM', 'DICOM',
'# DICOM Dump - CT Image
00080018=1.2.840.10008.5.1.4.1.1.2
00100010=ZhangSan
00100020=PAT12345
00100030=19800101
00080060=CT
00080050=ACC001
0020000D=1.2.840.10008.5.1.4.1.1.2.20260701',
'{"tags":{"00080018":"1.2.840.10008.5.1.4.1.1.2","00100010":"ZhangSan","00100020":"PAT12345","00100030":"19800101","00080060":"CT","00080050":"ACC001","0020000D":"1.2.840.10008.5.1.4.1.1.2.20260701"}}',
0, '2026-07-01 10:00:00');

-- 3) ASTM 测试结果消息（LAB-002, device_id=4）
INSERT INTO device_message (device_id, message_type, protocol, raw_content, parsed_content, processed, received_at) VALUES
(4, 'TEST_RESULT', 'ASTM',
'H|\\^&|||XN3000||HIS|||||P|1|20260701100000
P|1||PAT12345||ZhangSan||19800101|M
O|1|ORD001||GLU^BloodGlucose|||N
R|1|^^GLU^BloodGlucose|5.6|mmol/L|3.9-6.1|N
C|1|I|Result Normal
L|1|N',
'{"records":[{"type":"H","fields":["\\\\^&","","","XN3000","","HIS","","","","","P","1","20260701100000"]},{"type":"P","fields":["1","","PAT12345","","ZhangSan","","19800101","M"]},{"type":"O","fields":["1","ORD001","","GLU^BloodGlucose","","","N"]},{"type":"R","fields":["1","^^GLU^BloodGlucose","5.6","mmol/L","3.9-6.1","N"]},{"type":"C","fields":["1","I","Result Normal"]},{"type":"L","fields":["1","N"]}]}',
0, '2026-07-01 10:00:00');
