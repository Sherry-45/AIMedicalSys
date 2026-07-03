# 任务指令（v7）

## 动作
RETRY

## 任务描述
修复 R6 验证失败的 13 个测试：11 个 CircuitBreakerDegradationStrategyTest failure + 2 个 AbstractCapabilityExecutorTest error。涉及 2 个源文件。

## 选择理由
R6 为 R5 的 RETRY，验证仍有 13 个失败/错误。问题根因均在 R6 的代码修改中暴露，属于 R6 代码级回归。两处修复独立且简单，合并为同一轮 RETRY。

## 任务上下文
来自验证报告 `verify_v6.md`：
- **11 CircuitBreakerDegradationStrategyTest failures**: 全部 `expected: <true> but was: <false>`，涉及 shouldDegradeWhenFailureRateExceedsThreshold / shouldTransitionToOpenAndReturnTrueOnHighFailureRate / shouldRemainOpenAndAlwaysDegradeWithinWindow / shouldTransitionToHalfOpenAfterWindowExpires / halfOpenShouldAllowSingleProbeAndDegradeOthers / recordProbeResultSuccessShouldTransitionToClosedAndAllowNewProbe / shouldUseOperationNameAsKeyWhenPresent / shouldFallbackToServiceNameWhenOperationNameIsNull / shouldSetFailureCountToOneWhenTransitionToOpen / shouldTimeoutProbeLockAndRetry / shouldMaintainIndependentStateForMultipleCapabilities
- **1 error**: `executeStandardPipelineShouldDegradeWhenCircuitBreakerTriggersAfterRouting` — NPE at `llmChatService.structuredChat()` (line 425)
- **1 error**: `executeStandardPipelineShouldSkipCircuitBreakerCheckWhenMetricsStoreIsNull` — NPE at `metricsStore.recordSuccess()` (line 515)

## 已有代码上下文

### CircuitBreakerDegradationStrategy.java (CLOSED case, lines 58-72)
```java
case CLOSED: {
    CircuitData data = circuitDataMap.get(key);
    if (data != null && data.failureCount > 0) {
        double failureRate = metricsStore.getFailureRate(serviceName);
        if (failureRate >= failureRateThreshold) {
            long now = System.currentTimeMillis();
            data.circuitOpenedAt = now;
            data.lastFailureTime = now;
            data.failureCount = 1;
            stateRef.set(CircuitBreakerState.OPEN);
            return true;
        }
    }
    return false;
}
```
问题：`circuitDataMap` 无 `computeIfAbsent` 调用，`get(key)` 始终返回 null（除非 `recordProbeResult` 前置执行）。即使 entry 存在，`failureCount` 为 0，条件恒不满足。CLOSED→OPEN 路径被完全阻塞。

### AbstractCapabilityExecutor.java (handleSuccess, lines 504-517)
```java
private AiResult<R> handleSuccess(...) {
    if (metricsCollector != null) {
        metricsCollector.record(...);
    }
    metricsStore.recordSuccess(capabilityId, elapsedMs);  // line 515 — 无null检查!
    return AiResult.success(parsedResult);
}
```
问题：`metricsStore` 可能为 null，但 `recordSuccess` 调用前缺少 null 守卫。

## RETRY 说明

### 失败原因摘要
1. **CB CLOSED case chicken-and-egg**（11 fail + 1 error）：R6 将 CLOSED case 改为先查 `circuitDataMap.get(key).failureCount > 0` 再测 failure rate，但 circuitDataMap 条目从未创建（缺 `computeIfAbsent`），且 `failureCount` 未随 shouldDegrade 调用递增——恒为 0。所有 CLOSED→OPEN 路径被死锁。
2. **handleSuccess metricsStore NPE**（1 error）：`metricsStore.recordSuccess()` 无 `if (metricsStore != null)` 守卫，导致 metricsStore==null 时执行 `executeStandardPipeline` 全流程后 NPE。

### 修正方向
**修复 A — CircuitBreakerDegradationStrategy.java**：CLOSED case 恢复以下模式：
```java
case CLOSED: {
    CircuitData data = circuitDataMap.computeIfAbsent(key, k -> new CircuitData());
    data.failureCount++;
    double failureRate = metricsStore.getFailureRate(serviceName);
    if (failureRate >= failureRateThreshold) {
        long now = System.currentTimeMillis();
        data.circuitOpenedAt = now;
        data.lastFailureTime = now;
        data.failureCount = 1;  // T60: 重置为1而非++
        stateRef.set(CircuitBreakerState.OPEN);
        return true;
    }
    return false;
}
```
- 恢复 `computeIfAbsent` 确保 circuitDataMap 条目被创建
- `data.failureCount++` 每次 shouldDegrade 调用时递增（连续失败累积，T60 只在 OPEN 转换时重置）
- 移除 `data.failureCount > 0` 外层守卫（递增后恒 >0，无需此条件）
- endpoint 级隔离通过 key（operationName 优先）自动维持

**修复 B — AbstractCapabilityExecutor.java**：handleSuccess() line 515 添加 null 守卫：
```java
if (metricsStore != null) {
    metricsStore.recordSuccess(capabilityId, elapsedMs);
}
```
