# 计划审查报告（v5 r1）

## 审查结果
REJECTED

## 发现

### **[严重]** T14 修复范围不完整 — 缺少 AbstractCapabilityExecutor

**问题描述**：T14（熔断器作用域 capabilityId→endpointId）的修复方案同时依赖于两个层面的修改：
1. `CircuitBreakerDegradationStrategy.shouldDegrade()` 中键从 `context.getServiceName()` 改为 `context.getOperationName()`（回退 serviceName）— 在 R5 范围内 ✅
2. `AbstractCapabilityExecutor.executeStandardPipeline()` 中 routing 结束后通过 `ctx.setOperationName(endpointId)` 设置 operationName — **不在 R5 范围内** ❌

**为什么是问题**：task_v5.md 第 36-41 行列出了 4 个策略选项，其中选项 1/4 明确需要 `executeStandardPipeline()` 中 `routing` 之后设置 `ctx.setOperationName(endpointId)`。但 R5 的涉改文件清单只包含 `CircuitBreakerDegradationStrategy.java`、`TimeoutDegradationStrategy.java`、`AiOrchestrator.java` 三个文件，不包含 `AbstractCapabilityExecutor.java`。若不修改 AbstractCapabilityExecutor，`DegradationContext.operationName` 永不会被设为 endpointId，`shouldDegrade()` 将始终走回退路径（以 capabilityId 为键），T14 修复实际无效——熔断器仍然是在 per-capability 而非 per-endpoint 粒度上工作。

**期望的修正方向**：
- 将 `AbstractCapabilityExecutor.java` 纳入 R5 修改范围，在 `executeStandardPipeline()` 中 routing 之后、任何降级检查之前插入 `ctx.setOperationName(endpointId)`；或
- 在 CircuitBreakerDegradationStrategy 内部通过注入 `ModelRouter` 自行根据 capabilityId 和 request 解析 endpointId，避免修改 AbstractCapabilityExecutor；或
- 在计划中明确说明 operationName 已在其他机制中被设置（例如 Routing 拦截器或 DegradationContext 构建器已填充此字段），并提供证据。

## 修改要求

1. **[严重]** 修正 T14 范围不完整问题：将 AbstractCapabilityExecutor 加入 R5 涉改文件，或在 CircuitBreakerDegradationStrategy 内部自行获取 endpointId，或说明 operationName 已有设置路径。
