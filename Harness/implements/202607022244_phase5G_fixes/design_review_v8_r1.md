# 设计审查报告（v8 r1）

## 审查结果
REJECTED

## 发现

### [一般] capabilityName 字段无法通过工厂方法设置

设计定义了 `capabilityName` 为 AiCallRecord 字段（第3号），但三个静态工厂方法（`success()`/`failure()`/`degraded()`）均不接受 `capabilityName` 参数。类声明为不可变值对象（所有字段 `final`），全参构造器标记为 `private`，调用方无法在创建实例时传入 `capabilityName`，导致该字段始终为 `null`。

task_v8.md 明确要求「capabilityName 保留为构造函数参数或 setter，由调用方传入」，但设计未提供任何传入途径。

**期望修正方向**：将 `capabilityName` 作为参数添加到三个工厂方法签名中，或在设计文档中明确说明该字段留空（future scope），并调整字段定义表注释。

### [一般] totalTokens 字段在 success() 工厂方法中无赋值路径

设计定义了 `totalTokens` 为 AiCallRecord 字段（第23号），但 `success()` 工厂方法不接收 `totalTokens` 参数，也未声明从 `promptTokens + completionTokens` 内部计算。随着 LoggingMetricsCollector T16 修复改为 `entity.setTotalTokens(record.getTotalTokens())`，成功调用场景下 `totalTokens` 将始终传入 `null`，数据丢失问题未真正解决。

**期望修正方向**：在 `success()` 工厂方法内部将 `totalTokens` 计算为 `promptTokens + completionTokens`（值类型 `Integer`，可安全求和），并在设计文档行为契约中明确此推算规则；或将其作为单独参数加入 `success()` 签名。

## 修改要求（仅 REJECTED 时）

1. **capabilityName**：添加到三个工厂方法签名中作为参数，或明确标注该字段当前不可设置并说明后续注入方案。
2. **totalTokens**：在 success() 内计算（promptTokens + completionTokens），并在行为契约中记录该规则。
