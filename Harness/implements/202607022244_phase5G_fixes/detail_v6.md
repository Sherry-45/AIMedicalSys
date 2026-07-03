# 详细设计（v6）

## 概述

修复 R5 验证失败的 22 个测试，覆盖 3 类独立根因：
1. **AbstractCapabilityExecutor NPE（14个测试）**：`executeStandardPipeline()` 中 `this.metricsStore` 为 null 时调用 `buildDegradationContext()` 引发 NPE
2. **CircuitBreakerDegradationStrategy 状态隔离（1个测试）**：CLOSED case 使用 capability 级失败率 `getFailureRate(serviceName)` 导致同 capability 下不同 endpoint 同时 OPEN
3. **薄适配器超时降级（7个测试）**：`createExecutor()` 传入 `Runnable::run` 同步执行器使 `Future.get(timeout)` 永不及时

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-impl/.../orchestrator/AbstractCapabilityExecutor.java` | 修改 | 修复1 — executeStandardPipeline() 中 metricsStore null 守卫 |
| `ai-impl/.../degradation/CircuitBreakerDegradationStrategy.java` | 修改 | 修复2 — CLOSED case 改用 endpoint 级失败跟踪 |
| `ai-impl/.../thinadapter/DiagnosisCapabilityExecutorTest.java` | 修改 | 修复3 — Runnable::run 替换为 ForkJoinPool.commonPool()（3处） |
| `ai-impl/.../thinadapter/AnalysisReportForInspectionCapabilityExecutorTest.java` | 修改 | 修复3 — Runnable::run 替换为 ForkJoinPool.commonPool()（1处） |
| `ai-impl/.../thinadapter/AnalysisReportForLabTestCapabilityExecutorTest.java` | 修改 | 修复3 — Runnable::run 替换为 ForkJoinPool.commonPool()（1处） |
| `ai-impl/.../thinadapter/ImageAnalysisCapabilityExecutorTest.java` | 修改 | 修复3 — Runnable::run 替换为 ForkJoinPool.commonPool()（1处） |
| `ai-impl/.../thinadapter/RecommendExaminationCapabilityExecutorTest.java` | 修改 | 修复3 — Runnable::run 替换为 ForkJoinPool.commonPool()（1处） |
| `ai-impl/.../thinadapter/RecommendExecutionOrderCapabilityExecutorTest.java` | 修改 | 修复3 — Runnable::run 替换为 ForkJoinPool.commonPool()（1处） |

## 类型定义

### AbstractCapabilityExecutor（修复1）

**形态**：class（已有）
**包路径**：`com.aimedical.modules.ai.impl.orchestrator`

**executeStandardPipeline() 变更（line 354）**：

```java
// 变更前（line 354）：
DegradationContext cbCtx = metricsStore.buildDegradationContext(capabilityId, request.getClass().getSimpleName());

// 变更后：
DegradationContext cbCtx = null;
if (metricsStore != null) {
    cbCtx = metricsStore.buildDegradationContext(capabilityId, request.getClass().getSimpleName());
    cbCtx.setOperationName(routeResult.getEndpointId());
}
Map<String, List<DegradationStrategy>> strategyMap = degradationStrategyMapRef.get();
if (cbCtx != null && strategyMap != null) {
    List<DegradationStrategy> strategies = strategyMap.get(capabilityId);
    if (strategies != null) {
        for (DegradationStrategy s : strategies) {
            if (s instanceof CircuitBreakerDegradationStrategy && s.shouldDegrade(cbCtx)) {
                return doDegrade(...);
            }
        }
    }
}
```

**行为**：当 `metricsStore == null` 时跳过熔断二次检查，与 `checkPreDegradation()` line 205 的 null 守卫模式一致。

### CircuitBreakerDegradationStrategy（修复2）

**形态**：class（已有）
**包路径**：`com.aimedical.modules.ai.impl.degradation`

**shouldDegrade() CLOSED case 变更（line 58-70）**：

```java
case CLOSED: {
    // 优先以 endpoint 级 circuitData 决定是否检查 capability 级失败率
    CircuitData data = circuitDataMap.get(key);
    if (data != null && data.failureCount > 0) {
        // 该 endpoint 已有失败记录，使用 capability 级失败率决定是否 OPEN
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

**行为契约**：
- 当 `circuitDataMap` 中无该 `key`（endpointId）的条目时，直接返回 false（不降级）
- 当该 endpoint 已有失败记录（`failureCount > 0`）时，使用 capability 级失败率决定是否 OPEN
- 保证 endpoint1 的 OPEN 状态不影响 endpoint2 的 CLOSED 状态
- 原 OPEN→HALF_OPEN、HALF_OPEN case 不变

### 薄适配器测试文件（修复3）

**形态**：class（已有 test classes）
**包路径**：`com.aimedical.modules.ai.impl.thinadapter`

**变更项**：

| 文件 | createExecutor() 中变更 | 直接构造中变更 |
|------|------------------------|---------------|
| `DiagnosisCapabilityExecutorTest` | line 162: `Runnable::run` → `ForkJoinPool.commonPool()` | lines 108, 126: `Runnable::run` → `ForkJoinPool.commonPool()` |
| `AnalysisReportForInspectionCapabilityExecutorTest` | line 92: `Runnable::run` → `ForkJoinPool.commonPool()` | — |
| `AnalysisReportForLabTestCapabilityExecutorTest` | line 92: `Runnable::run` → `ForkJoinPool.commonPool()` | — |
| `ImageAnalysisCapabilityExecutorTest` | line 92: `Runnable::run` → `ForkJoinPool.commonPool()` | — |
| `RecommendExaminationCapabilityExecutorTest` | line 92: `Runnable::run` → `ForkJoinPool.commonPool()` | — |
| `RecommendExecutionOrderCapabilityExecutorTest` | line 92: `Runnable::run` → `ForkJoinPool.commonPool()` | — |

**新增 import**（6个文件）：
```java
import java.util.concurrent.ForkJoinPool;
```

## 错误处理

- **修复1**：`metricsStore == null` 时静默跳过熔断二次检查——不降级，LLM 调用正常进行
- **修复2**：洁净 endpoint 直接返回 false，不因同 capability 下其他 endpoint 的失败而误降级
- **修复3**：无错误处理变更，仅测试辅助设施替换

## 行为契约

1. **修复1**：`AbstractCapabilityExecutor.executeStandardPipeline()` 中熔断二次检查仅在 `metricsStore != null` 时执行，缺省时跳过
2. **修复2**：`CircuitBreakerDegradationStrategy.shouldDegrade()` CLOSED case 先查 `circuitDataMap.containsKey(key) && failureCount > 0`，再查 capability 级失败率
3. **修复3**：6个薄适配器测试的 `createExecutor()` 及 `DiagnosisCapabilityExecutorTest` 中直接构造处统一使用 `ForkJoinPool.commonPool()` 作为 `llmCallExecutor`，确保 `supplyAsync` 异步执行、`Future.get(timeout)` 可超时

## 依赖关系

- `AbstractCapabilityExecutor`：无新增依赖
- `CircuitBreakerDegradationStrategy`：无新增依赖
- 6个薄适配器测试：新增 `import java.util.concurrent.ForkJoinPool`
- 不涉及 pom.xml、配置类或新建文件
