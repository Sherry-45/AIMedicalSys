# 设计审查报告（v17 r1）

## 审查结果
REJECTED

## 发现

- **[严重]** `DatabasePromptTemplateManager.render()` 内部流程完全忽略 `promptVersion` 参数。接口描述（第42行）明确承诺"非 null 时按具体版本查找"，但 render 流程（第189-195行）始终按 ACTIVE 状态查询，即使 `promptVersion` 非 null 时也不例外。此外，`PromptTemplateRepository` 未定义按版本查询的方法（如 `findByCapabilityIdAndDepartmentIdAndVersion`），缓存 key 也未纳入版本维度。此缺陷将导致 `promptVersion` 非 null 时行为完全错误。

- **[严重]** `PromptTemplateRepository` 缺少 `promptVersion` 维度的查询方法。`render()` 在 `promptVersion != null` 时需要仓库方法按具体版本检索模板，但仓库仅定义了按状态查询的三种方法，无法支撑版本化查询需求。

## 修改要求（仅 REJECTED 时）

### [严重] render() 缺少 promptVersion 条件分支
- **问题**：render 流程（第189-195行）始终查询 ACTIVE 模板，从未检查 promptVersion 是否非 null。Repository 缺少按版本查询的方法。缓存 key 不含版本维度。
- **期望修正方向**：
  1. render() 流程添加 `promptVersion != null` 分支：调用 Repository 按版本查询
  2. `PromptTemplateRepository` 新增 `findByCapabilityIdAndDepartmentIdAndVersion(String capabilityId, String departmentId, int version)` 方法
  3. 评估缓存是否需扩展 `(capabilityId, departmentId, version)` 三维 key（否则非 ACTIVE 版本无法命中缓存）

### [严重] Repository 缺少版本查询方法
- **问题**：`PromptTemplateRepository`（第110-116行）仅有按状态查询的 3 个方法，缺少按精确版本查询的能力。
- **期望修正方向**：新增方法 `Optional<PromptTemplate> findByCapabilityIdAndDepartmentIdAndVersion(String capabilityId, String departmentId, int version)`，返回 Optional 以表达"该版本不存在"的语义（与 List 返回的"无结果"区分）。
