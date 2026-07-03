# 测试审查报告（v23 r2）

## 审查结果
REJECTED

## 发现

- **[一般]** `PrescriptionLocalRuleFallbackTest.java` — 缺少混合 BLOCK+WARN 告警的优先级测试。行为契约明确要求 BLOCK > WARN > PASS，当前仅分别测试了单一严重等级场景。若 `hasBlockAlert` / `hasWarnAlert` 逻辑被误改或顺序颠倒，现有测试无法捕获回归。

- **[一般]** `PrescriptionLocalRuleFallbackTest.java` — 缺少未知药品 ID 的白名单静默跳过测试。核心安全策略（契约#1）要求未知 ID 不产生任何告警，但所有测试用例均使用硬编码表中已知的药品 ID。应添加一个使用表外 drugId（如 `"drug_unknown"`）的用例，验证返回 PASS 且 alerts 为空。

- **[轻微]** `PrescriptionLocalRuleFallbackTest.java` — 缺少 `drugId` 为 null 的边界测试。错误处理表明确要求单条 `drugId` 为 null 时 `toInteractionKey` 返回空串、`DRUG_INGREDIENTS.get(null)` 返回 null，静默跳过。未覆盖。

- **[轻微]** `PrescriptionLocalRuleFallbackTest.java` — 缺少剂量为 0 或负数的边界测试。实现中 `checkDoseRange` 仅当 `item.getDose() > 0` 时执行，0 和负数应被静默跳过。未覆盖。

- **[轻微]** `PrescriptionLocalRuleFallbackTest.java` — 缺少 `patientInfo` 非 null 但 `allergyDetails` 和 `allergyHistory` 均为 null 的显式边界测试。虽然 `allRulesPassShouldReturnPASS` 间接覆盖，但非针对性测试，不够精确。

## 修改要求（仅 REJECTED 时）

1. **`PrescriptionLocalRuleFallbackTest.java` — 添加混合 BLOCK+WARN 优先级测试**
   - 问题：缺少 BLOCK 告警与 WARN 告警同时存在时验证风险等级为 BLOCK 的用例
   - 期望：新增测试方法（如 `mixedBlockAndWarnShouldReturnBLOCK`），构造同时触发 DRUG_INTERACTION（BLOCK）和 DOSE_EXCEED（WARN）的输入，断言 `riskLevel` 为 `"BLOCK"`

2. **`PrescriptionLocalRuleFallbackTest.java` — 添加未知药品 ID 的白名单测试**
   - 问题：缺少未知药品 ID 所有规则静默跳过、返回 PASS 的用例
   - 期望：新增测试方法（如 `unknownDrugIdShouldReturnPASS`），使用表外 drugId（如 `"drug_unknown"`）构造单药品处方，断言返回 PASS 且无告警
