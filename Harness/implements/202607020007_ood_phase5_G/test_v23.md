# v23 Test Report (Revised r3)

## 修订说明

根据 test_review_v23_r3.md 审查意见逐项处理：

| 审查问题 | 处理 | 说明 |
|---------|------|------|
| fromFallback 验证缺失 | 已确认代码全部通过 | 实际代码中全部 18 个用例均已包含 `assertTrue(response.isFromFallback())`，仅报告描述未显式列出。现已在测试用例清单中补充标注 |
| 缺少副作用测试 | 已新增 `fallbackShouldNotModifyRequest` | 调用 fallback 前后分别捕获 request 内部对象的关键字段值，断言未被修改 |
| 缺少 comorbidities=null 测试 | 已新增 `nullComorbiditiesShouldSkipPregnancyCheck` | patient 非 null、comorbidities=null、含孕妇慎用药，验证妊娠判断为 false，不触发 alert |

## 测试文件

`ai-impl/src/test/java/com/aimedical/modules/ai/impl/fallback/PrescriptionLocalRuleFallbackTest.java`

## 测试用例清单

| # | 方法名 | 覆盖契约 | 输入概要 | 预期 |
|---|--------|---------|---------|------|
| 1 | `allRulesPassShouldReturnPASS` | 白名单安全策略 | drug_para 500mg, age=30, 无过敏/合并症 | PASS, fromFallback=true |
| 2 | `drugInteractionShouldReturnBLOCK` | 配伍禁忌 | drug_cef + drug_eth | BLOCK, fromFallback=true, DRUG_INTERACTION alert |
| 3 | `doseBelowMinShouldReturnWARN` | 剂量低于下限 | drug_para 200mg < 300 min | WARN, fromFallback=true, DOSE_EXCEED alert |
| 4 | `doseAboveMaxShouldReturnWARN` | 剂量高于上限 | drug_para 1100mg > 1000 max | WARN, fromFallback=true, DOSE_EXCEED alert |
| 5 | `duplicateDrugShouldReturnWARN` | 重复用药 | drug_para + drug_acet(同成分paracetamol) | WARN, fromFallback=true, DUPLICATE_DRUG alert |
| 6 | `allergyConflictShouldReturnBLOCK` | 过敏史检查 | allergy=paracetamol, drug_para | BLOCK, fromFallback=true, ALLERGY_CONFLICT alert |
| 7 | `pediatricCautionShouldReturnWARN` | 儿童用药警示 | age=10, drug_aspirin | WARN, fromFallback=true, SPECIAL_POP_WARN |
| 8 | `pregnantCautionShouldReturnBLOCK` | 孕妇用药警示 | age=28, 妊娠期高血压, drug_iso | BLOCK, fromFallback=true, SPECIAL_POP_WARN |
| 9 | `nullPrescriptionItemsShouldReturnPASS` | 空/Null items 安全 | prescriptionItems=null | PASS, fromFallback=true |
| 10 | `emptyPrescriptionItemsShouldReturnPASS` | 空列表边界 | prescriptionItems=List.of() | PASS, fromFallback=true, alerts 空 |
| 11 | `nullAgeShouldSkipPediatricCheck` | age=null 边界 | age=null, drug_aspirin | PASS, fromFallback=true, 无儿科警告 |
| 12 | `nullPatientInfoShouldSkipAllergyAndSpecialPop` | patient=null 安全 | patient=null, drug_cef+drug_eth | BLOCK, fromFallback=true (仅交互检查) |
| 13 | `mixedBlockAndWarnShouldReturnBLOCK` | 风险等级优先级 | drug_cef+drug_eth(交互BLOCK) + drug_para 1100mg(剂量WARN) | BLOCK, fromFallback=true, 两种 alert 共存 |
| 14 | `unknownDrugIdShouldReturnPASS` | 白名单安全策略 | drug_unknown 500mg | PASS, fromFallback=true, 无 alert |
| 15 | `nullDrugIdShouldBeSkipped` | null drugId 安全 | null + drug_para 500mg | PASS, fromFallback=true, 无 alert |
| 16 | `zeroDoseShouldBeSkipped` | 剂量 0 边界 | drug_para 0mg | PASS, fromFallback=true, 无 alert |
| 17 | `negativeDoseShouldBeSkipped` | 剂量负数边界 | drug_para -100mg | PASS, fromFallback=true, 无 alert |
| 18 | `nullAllergyFieldsShouldNotCauseAllergyAlert` | null 过敏字段边界 | patient 非 null, allergyDetails/allergyHistory 为 null | PASS, fromFallback=true, 无过敏 alert |
| 19 | `fallbackShouldNotModifyRequest` | 无副作用契约 | drug_para 500mg, age=30 | 调用前后 request 内部字段值不变 |
| 20 | `nullComorbiditiesShouldSkipPregnancyCheck` | comorbidities=null 边界 | age=28, drug_iso, comorbidities=null | PASS, fromFallback=true, 无 alert |

## 合规性

- [x] 基于行为契约，不测实现细节
- [x] 每个契约至少一个正向用例
- [x] 覆盖正常/边界/错误/状态交互
- [x] 用例独立，不依赖顺序
- [x] 未修改源码文件
