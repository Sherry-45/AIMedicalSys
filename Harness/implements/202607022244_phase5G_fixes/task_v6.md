# 任务指令（v6）

## 动作
RETRY

## 任务描述
修复 R5 验证失败的 22 个测试，覆盖 3 类独立根因：

1. **AbstractCapabilityExecutor NPE（14个测试）**：`executeStandardPipeline()` line 354 中 `this.metricsStore` 为 null 时调用 `buildDegradationContext()` 引发 NullPointerException。R5 新增的熔断二次检查代码缺少 null 守卫。
2. **CircuitBreakerDegradationStrategy 状态隔离（1个测试）**：`shouldMaintainIndependentStateByEndpointId` 期望 endpoint 级状态独立，但 CLOSED→OPEN 决策使用 capability 级失败率 `metricsStore.getFailureRate(serviceName)`，导致同一 capability 下两个不同 endpoint 同时被打开。
3. **薄适配器超时降级（7个测试）**：测试辅助方法传入 `Runnable::run` 作为 `llmCallExecutor`（同步执行器），导致 `CompletableFuture.supplyAsync` 同步阻塞、`Future.get(timeout)` 永不及时触发。根源可追溯至 R1 T34（改用 llmCallExecutor）后测试未适配。

涉及文件：
- `ai-impl/.../orchestrator/AbstractCapabilityExecutor.java` — 修复 1
- `ai-impl/.../degradation/CircuitBreakerDegradationStrategy.java` — 修复 2
- `ai-impl/.../thinadapter/*CapabilityExecutorTest.java`（6个文件）— 修复 3

## 选择理由
R5 验证失败 22 个测试，覆盖代码路径（熔断降级管线、薄适配器执行器）存在根本性缺陷，必须在 R6 统一修复后再推进后续轮次。三个根因相互独立，可并行修复。

## 任务上下文
### 已有设计上下文
- R5 在 `AbstractCapabilityExecutor.executeStandardPipeline()` 中 routing 成功后新增熔断二次检查，循环遍历 `degradationStrategyMapRef` 中匹配 capabilityId 的 DegradationStrategy，对 `CircuitBreakerDegradationStrategy` 实例调用 `shouldDegrade()`。routing 后设置了 `cbCtx.setOperationName(routeResult.getEndpointId())`，使熔断键升级为 endpoint 级。
- `CircuitBreakerDegradationStrategy.shouldDegrade()` 通过 `context.getOperationName() != null ? context.getOperationName() : context.getServiceName()` 派生 key，但 CLOSED case 中 `metricsStore.getFailureRate(serviceName)` 使用 `serviceName`（capabilityId）查询失败率。
- 薄适配器 `doExecuteInternal()` 通过 `CompletableFuture.supplyAsync(task, llmCallExecutor)` + `Future.get(timeout, MILLISECONDS)` 实现超时。

### 验证失败摘要
```
Tests run: 539, Failures: 12, Errors: 10, Skipped: 0
```

| 分组 | 数量 | 测试 | 根因 |
|------|------|------|------|
| 1 | 14 | AbstractCapabilityExecutorTest: NPE 10 个 + CompletionException→NPE 4 个 | this.metricsStore == null 无守卫 |
| 2 | 1 | CircuitBreakerDegradationStrategyTest.shouldMaintainIndependentStateByEndpointId | capability 级失败率导致 endpoint 间状态未隔离 |
| 3 | 7 | 6 个薄适配器 shouldDegradeOnTimeout + Diagnosis 第二项 | Runnable::run 同步执行器使 Future.get 永不及时 |

### 修复方向

**修复 1：metricsStore NPE**
在 `executeStandardPipeline()` line 354 前添加 null 守卫：
```java
if (metricsStore != null && degradationStrategyMapRef != null) {
    DegradationContext cbCtx = metricsStore.buildDegradationContext(...);
    ...
}
```
与 `checkPreDegradation()` 中已有的 null 守卫模式一致。

**修复 2：endpoint 级状态隔离**
`CircuitBreakerDegradationStrategy.shouldDegrade()` 的 CLOSED case 需改用 endpoint 级失败跟踪。方案：
- 在 `CircuitData` 中维护 endpoint 级 `failureCount`（已有）
- CLOSED case 中检查 `circuitDataMap.get(key)` 是否存在且 `failureCount > 0`
- 仅当该 endpoint 已有历史失败记录时才使用 capability 级失败率决定是否 OPEN
- 对于无 `circuitData` 的洁净 endpoint，直接返回 false（不因其他 endpoint 的失败记录而打开）

影响：保证 endpoint1 的 OPEN 状态不影响 endpoint2 的 CLOSED 状态。

**修复 3：薄适配器超时降级**
将 6 个薄适配器测试文件（`AnalysisReportForInspectionCapabilityExecutorTest.java`、`AnalysisReportForLabTestCapabilityExecutorTest.java`、`DiagnosisCapabilityExecutorTest.java`、`ImageAnalysisCapabilityExecutorTest.java`、`RecommendExaminationCapabilityExecutorTest.java`、`RecommendExecutionOrderCapabilityExecutorTest.java`）中 `createExecutor()` 方法的 `Runnable::run` 替换为 `ForkJoinPool.commonPool()`，确保 `Future.get(timeout)` 可异步超时。

## RETRY 说明

| 原 R5 问题 | 验证失败详情 | 修正方向 |
|-----------|-------------|---------|
| T14 熔断二次检查在 executeStandardPipeline 中使用 metricsStore 无 null 守卫 | 14 个 AbstractCapabilityExecutorTest 失败（NPE） | 添加 metricsStore != null 守卫 |
| T14 熔断器 CLOSED case 使用 capability 级失败率 | 1 个 CircuitBreakerDegradationStrategyTest 失败（状态未隔离） | 改用 endpoint 级 circuitData.failureCount |
| T34 改用 llmCallExecutor 后测试未适配（Runnable::run 同步执行器） | 7 个薄适配器 shouldDegradeOnTimeout 失败 | 改为 ForkJoinPool.commonPool() |
