# 测试审查报告（v17 r1）

## 审查结果
APPROVED

## 发现

- **[轻微]** `PromptTemplateTest.java` — 设计要求"字段 nullable 约束验证"，当前仅测试了 `departmentId` 可为 null 的正向路径（`nullableDepartmentIdShouldBeAllowed`），未反向测试 `content` 的 `@Column(nullable = false)` 约束在插入 null content 时是否抛出 `DataIntegrityViolationException`。

- **[轻微]** `DatabasePromptTemplateManagerTest.java:159-166` — `versionNotFoundInDepartmentShouldFallbackToGlobal` 中 `activeGlobal` 的 version=1 被 mock 返回用于 version=2 查询。mock 返回的实体版本字段与实际查询版本不一致（虽然 resolveExactVersion 仅检查 status，不影响逻辑正确性），对读者造成混淆。

- **[轻微]** `DatabasePromptTemplateManagerTest.java:187-199` — `eventWithNullDepartmentIdShouldInvalidateAllAndRewarm` 仅通过 `verify(repository, times(2)).findByStatus(ACTIVE)` 间接验证 `invalidateAll()` 发生了。若 `onTemplateChanged()` 移除了 `invalidateAll()` 但保留了 `warmup()`，该验证仍会通过。

## 修改要求（仅 REJECTED 时）
（无）
