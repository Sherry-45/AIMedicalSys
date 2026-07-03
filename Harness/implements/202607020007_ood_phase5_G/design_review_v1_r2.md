# 设计审查报告（v1 r2）

## 审查结果
APPROVED

## 发现

### [轻微] serializedTimestamp 字段注释与实际时间单位不一致

`DegradationContext.serializedTimestamp` 字段注释标注为"秒级时间戳"（§类型定义 DegradationContext 字段列表，line 95），但：
- `buildDegradationContext()` 中赋值为 `System.currentTimeMillis()`（毫秒，§SlidingWindowMetricsStore buildDegradationContext 第 2 步）
- `isFresh()` 中与 `DEFAULT_TTL_MILLIS = 60_000L`（毫秒）比较

实际行为以毫秒为单位一致，注释误导。应修正注释为"毫秒级时间戳"。

### [轻微] DegradationReason.fromCode() 返回 null 未明确防御契约

`fromCode(String code)` 在未匹配时返回 null（§行为契约已约定是 null-safe 不抛异常）。返回 null 将 NPE 风险推给调用方。建议在 Javadoc 中标注 `@return 匹配的枚举值，未匹配时返回 null`，或考虑使用 `Optional<DegradationReason>`。

### [轻微] task_v1.md §具体交付物 中 getFailureRate/getEffectiveFailureRate 的公式附注与实际 OOD 设计文档不一致

验证发现 OOD 设计文档 §3.5（`Docs/06_ood_phase5_G.md` line 2510-2516）定义的公式为：
- `getFailureRate = recordFailure / (recordSuccess + recordFailure)`（排除 recordDegraded，供熔断器判定）
- `getEffectiveFailureRate = recordFailure / (recordSuccess + recordDegraded + recordFailure)`（降级计入分母不计入分子，供超时策略参考）

设计（detail_v1.md）的公式与之完全一致。task_v1.md 中 line 54-55 的公式附注（含 degraded 在分母和分子中的不同组合）与 OOD 设计文档不符，需 task 维护者同步修正任务描述中的公式附注以消除歧义。**本问题不影响设计的正确性，仅需要任务描述修复。**
