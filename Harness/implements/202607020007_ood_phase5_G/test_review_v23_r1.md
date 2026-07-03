# 测试审查报告（v23 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** `PrescriptionLocalRuleFallbackTest.java` — 剂量超标测试仅覆盖低于下限的路径：`doseExceedShouldReturnWARN` 使用 drug_para 200mg（< 300 minDose），但未测试高于上限的场景（如 drug_para 1100mg > 1000 maxDose）。代码中 `>` 比较分支无任何测试覆盖，若该处存在缺陷（如误写为 `<`）无法检出。
- **[一般]** `PrescriptionLocalRuleFallbackTest.java` — 缺少 `prescriptionItems` 为空列表的边界测试。详细设计明确规定 null 和空列表行为相同（跳过检查 1-3，返回 PASS），但仅测试了 null 情况。若守卫条件 `!items.isEmpty()` 被意外修改，空列表行为将偏离设计且无法被现有测试捕获。
- **[一般]** `PrescriptionLocalRuleFallbackTest.java` — 缺少 `patientInfo.age` 为 null 的边界测试。设计规定 age 为 null 时 `isPediatric` 为 false，不触发儿童用药检查，但该场景未覆盖。若未来重构将 `getAge() != null && getAge() < 18` 简化为 `getAge() < 18`，将引发 NPE 而无法被现有测试发现。
- **[轻微]** `PrescriptionLocalRuleFallbackTest.java` — `doseExceedShouldReturnWARN` 测试命名不准确：实际测试剂量低于下限（under-range），建议改为 `doseOutOfRangeShouldReturnWARN` 或拆分上下限两个测试用例。
- **[轻微]** `PrescriptionLocalRuleFallbackTest.java` — 未使用的 import `java.util.ArrayList`（第 3 行）。

## 修改要求（REJECTED）

1. **`PrescriptionLocalRuleFallbackTest.java`** — 在 `doseExceedShouldReturnWARN` 之外，新增一个测试方法测试 dose > maxDose 的场景（例如 drug_para dose=1100，期望 DOSE_EXCEED + WARN），或将该方法扩展为参数化测试覆盖上下界。
2. **`PrescriptionLocalRuleFallbackTest.java`** — 新增测试方法，构造 `request.setPrescriptionItems(List.of())`（空列表），验证 riskLevel=PASS 且 alerts 为空。
3. **`PrescriptionLocalRuleFallbackTest.java`** — 新增或扩展现有测试用例，设置 `patient.setAge(null)` 并包含儿科慎用药品，验证不触发儿科警告。
