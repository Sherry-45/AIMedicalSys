# 测试审查报告（v23 r3）

## 审查结果
REJECTED

## 发现

- **[一般]** `ai-impl/src/test/java/.../PrescriptionLocalRuleFallbackTest.java` — 仅测试用例 1 (`allRulesPassShouldReturnPASS`) 明确提及验证 `fromFallback=true`，其余 17 个用例的描述均未包含对 `fromFallback` 的断言。行为契约明确要求"所有返回结果的 `isFromFallback()` 均为 `true`"，这是一个贯穿性的核心契约，缺失验证将导致测试无法发现 `fromFallback` 被错误设置为 `false` 的缺陷。

- **[一般]** `ai-impl/src/test/java/.../PrescriptionLocalRuleFallbackTest.java` — 缺少副作用测试。详细设计行为契约第 4 条明确要求"不修改入参 `request`"，但没有任何测试用例设计来验证调用 `fallback()` 后 `request` 对象的内容未被篡改（如 `prescriptionItems` 中的元素、`patientInfo` 等）。这是安全契约的关键验证点。

- **[轻微]** `ai-impl/src/test/java/.../PrescriptionLocalRuleFallbackTest.java` — 缺少 `comorbidities` 为 null 的边界测试。详细设计错误处理表明确提及"`patientInfo.comorbidities` 为 null → 妊娠判断为 false，不触发孕妇检查"，但现有用例中无专门针对此场景的测试。

## 修改要求（仅 REJECTED 时）

1. **`PrescriptionLocalRuleFallbackTest.java` — `fromFallback` 验证缺失**
   - **问题**：核心行为契约"所有返回结果的 `fromFallback` 均为 `true`"未在多个测试用例中被验证，存在漏测风险。
   - **期望修正**：在所有测试用例的预期断言中补充 `assertTrue(result.isFromFallback())`，确保无论何种场景，返回的 `PrescriptionCheckResponse` 的 `fromFallback` 字段始终为 `true`。

2. **`PrescriptionLocalRuleFallbackTest.java` — 缺少副作用测试**
   - **问题**：行为契约要求 `fallback()` 不修改入参 `request`，但无测试覆盖。如果未来实现误修改了入参，现有测试无法发现。
   - **期望修正**：新增一个测试用例（或在已有用例中追加断言），在调用 `fallback()` 前后分别捕获 `request` 及其内部对象的关键字段值，断言未被修改。例如：先记录 `request.getPrescriptionItems().get(0).getDose()`，调用 `fallback()` 后再次断言该值未变。
