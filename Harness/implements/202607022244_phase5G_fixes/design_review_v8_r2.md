# 设计审查报告（v8 r2）

## 审查结果
REJECTED

## 发现

### **[一般] 问题1：工厂方法无法设置 userId**

AiCallRecord 字段表含 23 个字段，其中 `userId`（#9）标注为"保留（上游 LoggingMetricsCollector 映射所需）"。全参构造器声明为 `private`，仅由工厂方法使用。但三个工厂方法（`success`/`failure`/`degraded`）签名中均**不包含** `userId` 参数，导致工厂方法无法向构造器传递 `userId` 值。设计未说明工厂方法产生的实例中 `userId` 如何被赋值，也未定义当 `userId` 为 null 时的映射行为。

**期望修正方向**：将 `userId` 加入三个工厂方法签名，或在设计中明确声明 `userId` 的默认值（如 null 值是否可接受）及对 `LoggingMetricsCollector` 映射的影响。

### **[一般] 问题2：degradationReason 字段命名不一致**

字段表第 13 行：`degradationReason | String | 已有 | 不变（保留原名 degradeReason）`——暗示字段在 `AiCallRecord` 中保持原名 `degradeReason`，getter 为 `getDegradeReason()`。  
但 `LoggingMetricsCollector` 额外调整节：`record.getDegradeReason() → record.getDegradationReason()`——暗示 getter 已重命名为 `getDegradationReason()`。  
两处对字段是否已重命名的描述矛盾，实现时会产生混淆。

**期望修正方向**：若字段在 `AiCallRecord` 中已从 `degradeReason` 重命名为 `degradationReason`，则字段表应标注"改名为 degradationReason"而非"不变"；若未重命名，则 `LoggingMetricsCollector` 调整应删除此条或明确其为未来预留变更。
