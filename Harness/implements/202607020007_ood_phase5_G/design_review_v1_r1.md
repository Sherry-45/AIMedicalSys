# 设计审查报告（v1 r1）

## 审查结果
REJECTED

## 发现

### **[一般] getEffectiveFailureRate 公式与 OOD 设计文档不符**

- **位置**：`detail_v1.md:171-176`（`SlidingWindowMetricsStore.getEffectiveFailureRate`）
- **问题**：设计定义的公式为 `(failureCount + degradedCount) / (successCount + failureCount + degradedCount)`，即将降级事件同时计入分子和分母。
- **依据**：OOD 设计文档 `Docs/06_ood_phase5_G.md:2514-2516` 明确定义为 `recordFailure / (recordSuccess + recordDegraded + recordFailure)`，并注释"将降级兜底成功计入**分母**"，降级事件仅计入分母而不计入分子。此定位在第 4196 行设计决策表中进一步确认：`getFailureRate()` 与 `getEffectiveFailureRate()` 的差异在于分母是否包含 degraded，分子始终仅统计 failure。
- **影响**：公式差异将导致 TimeoutDegradationStrategy 读取的"有效失败率"偏高（degraded 视为失败），与 OOD 设计文档"降级兜底增多本身是系统退化信号，但不等于 LLM 调用失败"的判定语义冲突。
- **期望修正**：将 `getEffectiveFailureRate` 的分子修正为仅 `failureCount`，即 `failureCount / (successCount + failureCount + degradedCount)`，与 OOD 设计文档对齐。

### **[轻微] 文件路径格式与任务约定不一致**

- **位置**：`detail_v1.md:11-17`（文件规划表）
- **问题**：设计使用相对路径格式 `ai-api/src/main/java/...`，而任务使用完整模块路径 `AIMedical/backend/modules/ai/ai-api/src/main/java/...`。路径前缀不一致可能导致实现时定位错误。
- **期望修正**：统一为任务约定的路径格式，明确标注基准目录（如项目根或 ai 模块根）。
