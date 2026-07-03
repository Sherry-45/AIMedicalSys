# 设计审查报告（v17 r3）

## 审查结果
APPROVED

## 发现

- **[轻微] render() promptVersion 参数类型与 OOD 规格不符**：OOD 设计文档（06_ood_phase5_G.md）规定参数类型为 `Integer promptVersion`，设计改为 `String promptVersion`。设计说明第 1 条（line 363）给出了合理理由（与现有 7 个 CapabilityExecutor 的 `private String promptVersion` 字段一致，避免级联修改），实现层内部 `Integer.parseInt()` 按需转换。虽不影响正确性，但属上游规格偏差。

- **[轻微] render() 步骤 1b-1e 两层查询后的判定不明确**：步骤 b 执行了科室级→全局级两层 `findByCapabilityIdAndDepartmentIdAndVersion` 查询，但步骤 c-e 中的"找到/未找到"未明确区分是针对科室级结果还是全局级结果。实现时需明确逻辑：先查科室级，未找到再查全局级；对找到的结果依次验证 status==ACTIVE。

- **[轻微] render() 步骤 2e 在 departmentId=null 时存在冗余查询**：当 `departmentId` 参数为 null 时，步骤 2d 已执行 `findByCapabilityIdAndDepartmentIdAndStatus(capabilityId, null, ACTIVE)`，步骤 2e 执行完全相同的查询。建议在步骤 2e 前添加 `departmentId != null` 条件判断。

- **[轻微] 全局模板兜底后的缓存 key 未明确**：render() 步骤 2e 查到 departmentId=null 的全局模板后，步骤 2f "填充缓存"未说明缓存 key 使用原始 `departmentId` 还是 `null`。若使用 null key，后续同一科室请求无法命中缓存。建议明确约定：用原始请求的 departmentId 构造缓存 key。

- **[轻微] departmentId=null 事件清理粒度过粗**：onTemplateChanged() 中当 event.departmentId==null 时执行 `cache.invalidateAll()` 清空所有 capability 的缓存条目（不仅仅是 event 指定的 capability），然后全量 warmup()。功能正确但影响范围过大。设计说明第 3 条（line 365）已承认此折衷。

## 修改要求
无（无严重或一般问题，设计通过）
