# 任务指令（v5）

## 动作
NEW

## 任务描述
修复 Orchestrator 与熔断降级相关 7 项问题（T14/T60/T13/T61/T19/T20/T35）。涉及 4 个源文件：

| 文件 | 任务 |
|------|------|
| `ai-impl/.../degradation/CircuitBreakerDegradationStrategy.java` | T14/T60/T13 — 熔断器作用域 endpointId、failureCount 重置、probeLock 清理 |
| `ai-impl/.../degradation/TimeoutDegradationStrategy.java` | T61 — 移除未使用的 metricsStore 依赖 |
| `ai-impl/.../orchestrator/AiOrchestrator.java` | T19/T20/T35 — fail-fast 异常、metricsCollector.record()、ConcurrentHashMap 类型修正 |
| `ai-impl/.../orchestrator/AbstractCapabilityExecutor.java` | T14 — 在 executeStandardPipeline() 的 routing 后添加 CircuitBreaker 重新检查 |

## 选择理由
R4（核心执行器异常处理）已通过验证（80/0）。R5 是计划中下一步，聚焦熔断降级正确性与 Orchestrator 健壮性：
- **T14（严重）**：熔断器按 capabilityId 而非 endpointId 隔离状态，导致不同端点的熔断状态互相污染——跨端点故障隔离失效
- **T60（一般）**：CLOSED→OPEN 转换时 failureCount 未重置，跨周期累加导致后续熔断阈值判断失准
- **T13（一般）**：HALF_OPEN 探测线程崩溃后 probeLock 永久锁定，后续探测永远无法进入——熔断器卡在 HALF_OPEN
- **T61（一般）**：TimeoutDegradationStrategy 未使用注入的 metricsStore——死代码
- **T19（一般）**：未注册能力标识返回 failure 而非异常——调用方无法区分"能力不存在"与"执行失败"
- **T20（一般）**：catch 块中 metricsCollector.record() 未实现——同步异常场景指标不可见
- **T35（一般）**：executorMap 声明 volatile + ConcurrentHashMap 双重保护——冗余且文档误导

## 任务上下文

### T14 熔断器作用域 capabilityId→endpointId

**关键设计分析**：

`checkPreDegradation()`（`AbstractCapabilityExecutor.java:199`）在 `execute()` 方法中于 routing 之前被调用。此时 endpointId 未知，因此 `DegradationContext.operationName` 不会被设置（`ctx.setOperationName()` 从未被调用）。`CircuitBreakerDegradationStrategy.shouldDegrade()` 当前以 `context.getServiceName()` 为键，实际值为 capabilityId。

**修复分为两层**：

**层 1 — CircuitBreakerDegradationStrategy（键变更）**：
```java
// shouldDegrade() 中优先使用 endpointId
String key = context.getOperationName() != null
    ? context.getOperationName()
    : context.getServiceName();
```
- stateMap 和 circuitDataMap 的键改为优先取 operationName（endpointId），回退 serviceName（capabilityId）
- 这确保：当 operationName 已设置时走 per-endpoint 隔离；未设置时保持与旧行为兼容（per-capability）

**层 2 — AbstractCapabilityExecutor（routing 后设置 endpointId + 重新检查）**：

`executeStandardPipeline()` 中 routing 在 line 346 完成。route 成功后（line 351 之后），需要：
1. 用 endpointId 构建新的 `DegradationContext` 并设置 `operationName`
2. 对熔断降级策略做二次检查——因为 `checkPreDegradation()` 执行时 endpointId 未知，CircuitBreaker 走的是 capabilityId 回退路径

具体修改位置（`AbstractCapabilityExecutor.executeStandardPipeline()`，约 line 351 之后）：

```java
// routing 完成，routeResult 已验证非 null
// ==== 插入：endpointId 粒度的熔断降级检查 ====
DegradationContext cbCtx = metricsStore.buildDegradationContext(capabilityId, request.getClass().getSimpleName());
cbCtx.setOperationName(routeResult.getEndpointId());
// 只检查 CircuitBreakerDegradationStrategy
Map<String, List<DegradationStrategy>> strategyMap = degradationStrategyMapRef.get();
if (strategyMap != null) {
    List<DegradationStrategy> strategies = strategyMap.get(capabilityId);
    if (strategies != null) {
        for (DegradationStrategy s : strategies) {
            if (s instanceof CircuitBreakerDegradationStrategy && s.shouldDegrade(cbCtx)) {
                return doDegrade(userId, startTime, DegradationReason.STRATEGY_TRIGGERED.getCode(),
                    request, capabilityId, departmentId, callerRole, callerId,
                    visitId, patientId, sessionId, inputSummary, null, promptVersion, null, sentinelReason);
            }
        }
    }
}
// ==== 结束插入 ====
```

> **注意**：DegradationStrategy 的 `instanceof` 检查需要导入 `CircuitBreakerDegradationStrategy`。当前 `AbstractCapabilityExecutor.java` 已有 `degradationStrategyMapRef` 字段和 `degradation` 相关导入。插入点位于 routeResult 非空校验之后、endpointHealthManager 检查之前。

### T60 CLOSED→OPEN failureCount 重置

**现状**（line 63）：
```java
data.failureCount++;  // 进入 OPEN 时 failureCount 累加而非重置
```

**修正**：
```java
data.failureCount = 1;  // 重置为当前触发 OPEN 的这次失败
```

### T13 probeLock 清理路径

**现状**（line 80-85）：
```java
case HALF_OPEN: {
    CircuitData data = circuitDataMap.get(capabilityId);
    if (data != null && data.probeLock.compareAndSet(false, true)) {
        return false;  // 允许探测
    } else {
        return true;   // 其他请求降级
    }
}
```
探测线程获取 probeLock 后若在调用 `recordProbeResult()` 前崩溃，probeLock 永远为 true。

**修正**：
1. 在 `CircuitData` 中添加 `volatile long probeAcquiredAt` 字段
2. HALF_OPEN case 中获取 probeLock 成功后记录 `data.probeAcquiredAt = System.currentTimeMillis()`
3. 若 `probeLock` 已被持有且 `(System.currentTimeMillis() - data.probeAcquiredAt) > openWindowMs`，reset probeLock 并重试

### T61 移除未使用的 metricsStore

**现状**（line 12-27）：
```java
private final SlidingWindowMetricsStore metricsStore;  // 注入但未在 shouldDegrade() 中使用
```

**修正**：从 TimeoutDegradationStrategy 中移除 `metricsStore` 字段、构造器参数和 import。构造器简化为仅接受 `Duration timeoutThreshold`。

### T19 fail-fast 异常

**现状**（AiOrchestrator.java line 149-152）：
```java
if (executor == null) {
    log.warn("未注册能力标识: capabilityId={}", capabilityId);
    return CompletableFuture.completedFuture(AiResult.failure("未注册能力标识: " + capabilityId));
}
```

**修正**：
```java
if (executor == null) {
    throw new IllegalArgumentException("未注册能力标识: " + capabilityId);
}
```

### T20 metricsCollector.record() 实现

**现状**（AiOrchestrator.java line 158）：
```java
// TODO: metricsCollector.record() when AiMetricsCollector methods are defined
```

**修正**：在 catch 块中构建 AiCallRecord 并调用 metricsCollector.record()。由于 catch 块中 context 有限（仅有 capabilityId），其他字段使用 null 或 0 默认值：
```java
metricsCollector.record(new AiCallRecord(
    capabilityId, null, null, null, null, null, null, null, null, null,
    System.currentTimeMillis() - startTime, true, e.getClass().getSimpleName(), 0, 0));
```
需要将 `startTime` 捕获为局部变量或从异常上下文推断。当前 catch 块在 `handle()` 方法中，需添加 `startTime` 捕获（方法执行开始时记录）。

**注意**：catch 块目前没有 elapsed time，需在 `handle()` 方法开始时记录 `long startTime = System.currentTimeMillis()`，或在 executor.execute() 前记录。

### T35 ConcurrentHashMap 类型修正

**现状**（line 55, 69, 78）：
```java
private volatile Map<String, CapabilityExecutor<?, ?>> executorMap;  // volatile + Map 声明

// @PostConstruct initExecutorMap():
Map<String, CapabilityExecutor<?, ?>> map = new ConcurrentHashMap<>();
// ...
this.executorMap = map;  // 实际赋值为 ConcurrentHashMap
```

**修正**：
```java
private ConcurrentHashMap<String, CapabilityExecutor<?, ?>> executorMap;

// @PostConstruct:
ConcurrentHashMap<String, CapabilityExecutor<?, ?>> map = new ConcurrentHashMap<>();
// ...
this.executorMap = map;
```
移除 `volatile`（executorMap 仅在 @PostConstruct 中写入一次，之后只读），声明类型改为 `ConcurrentHashMap`。

## 已有代码上下文

### CircuitBreakerDegradationStrategy.java（ai-impl/degradation/）

- 位置：`ai-impl/src/main/java/.../degradation/CircuitBreakerDegradationStrategy.java`
- 124 行，实现 `DegradationStrategy` 接口
- 内部枚举 `CircuitBreakerState { CLOSED, OPEN, HALF_OPEN }`
- 内部类 `CircuitData { circuitOpenedAt, lastFailureTime, failureCount, probeLock }`
- `stateMap: ConcurrentHashMap<String, AtomicReference<CircuitBreakerState>>`
- `circuitDataMap: ConcurrentHashMap<String, CircuitData>`
- 当前键为 `context.getServiceName()`（实际按 capabilityId 寻址）
- 测试文件：`CircuitBreakerDegradationStrategyTest.java`（17 个测试用例，179 行）

### TimeoutDegradationStrategy.java（ai-impl/degradation/）

- 位置：`ai-impl/src/main/java/.../degradation/TimeoutDegradationStrategy.java`
- 33 行，实现 `DegradationStrategy` 接口
- 构造器接受 `SlidingWindowMetricsStore metricsStore + Duration timeoutThreshold`
- `shouldDegrade()` 仅使用 timeoutThreshold（80% 规则），metricsStore 未使用
- 测试文件：`TimeoutDegradationStrategyTest.java`（7 个测试用例，79 行）

### AiOrchestrator.java（ai-impl/orchestrator/）

- 位置：`ai-impl/src/main/java/.../orchestrator/AiOrchestrator.java`
- 162 行，实现 `AiService` 接口，标记 `@Service` + `@ConditionalOnProperty`
- `executorMap: volatile Map<String, CapabilityExecutor<?, ?>>` — 被 @PostConstruct 初始化为 ConcurrentHashMap
- `handle(String capabilityId, Object request)` 私有方法：
  - null check → 返回 `AiResult.failure`
  - try-catch → catch 块有 TODO 注释未实现 record
- 测试文件：`AiOrchestratorTest.java`（16 个测试用例，224 行）

### AbstractCapabilityExecutor.java（ai-impl/orchestrator/）

- 位置：`ai-impl/src/main/java/.../orchestrator/AbstractCapabilityExecutor.java`
- 535 行，抽象基类实现 `CapabilityExecutor<T, R>` 接口
- 关键字段：`modelRouter`、`degradationStrategyMapRef: AtomicReference<Map<String, List<DegradationStrategy>>>`、`metricsStore: SlidingWindowMetricsStore`
- `executeStandardPipeline()`（line 328-482）执行流程：
  1. 渲染 prompt（line 337）
  2. **routing**（line 346）：`modelRouter.route(capabilityId, request)` → `ModelRoute`（含 `getEndpointId()`）
  3. endpoint 健康检查（line 353-364）
  4. 构建 LLM 请求并执行（line 366+）
- `checkPreDegradation()`（line 199-229）：在 `execute()` 中于 routing 之前被调用，此时 endpointId 未知
- 测试文件：`AbstractCapabilityExecutorTest.java`

### DegradationContext.java（ai-api/degradation/）

- 携带 `serviceName`（= capabilityId）、`operationName`、`invocationCount`、`failureCount`、`elapsedTime` 等字段
- `getServiceName()` / `setServiceName()` — 当前被 CircuitBreakerDegradationStrategy 使用
- `getOperationName()` / `setOperationName()` — 可用于承载 endpointId
- 测试依赖：CircuitBreakerDegradationStrategyTest 和 TimeoutDegradationStrategyTest 依赖 `buildDegradationContext()` 构建 context

### 测试注意

- **T14/T60/T13**：现有 CircuitBreakerDegradationStrategyTest 键为 `"cap1"`、`"capA"`、`"capB"`。修正后需新增 endpointId 粒度的测试用例（相同 capabilityId 不同 endpointId 的独立状态）
- **T14 AbstractCapabilityExecutor**：新增的 routing 后熔断检查需添加测试覆盖——使用 mock strategyMap 验证 routing 成功后 CircuitBreaker 被正确检查
- **T19**：`shouldReturnFailureForUnregisteredCapability` 测试期望 `AiResult.failure`，改为异常后需更新为 `assertThrows()`
- **T20**：`shouldRecordFailureOnSyncException` 测试已验证 metricsStore 记录，新增验证 metricsCollector.record() 调用
- **T35**：类型变更不影响外部行为，现有测试保持兼容
- **T61**：移除 metricsStore 后 TimeoutDegradationStrategy 构造器签名变更，测试文件中 `setUp()` 需同步修改

## 修订说明（v5 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] T14 范围不完整 — AbstractCapabilityExecutor 未纳入 R5 涉改清单，executeStandardPipeline() 中 routing 后未设置 ctx.setOperationName(endpointId) | 将 AbstractCapabilityExecutor.java 纳入 R5 涉改文件；在 executeStandardPipeline() 中 routing 成功后插入 endpointId 粒度的熔断降级二次检查（创建 DegradationContext、setOperationName(endpointId)、仅对 CircuitBreakerDegradationStrategy 执行 shouldDegrade()）；更新涉改文件表、任务上下文、已有代码上下文、测试注意事项 |
