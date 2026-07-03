# 任务指令（v21）

## 动作
NEW

## 任务描述

### 1. EndpointHealthState 枚举（新建）
在 `ai-impl/metrics/` 包下新建 `EndpointHealthState.java`，包含三个枚举常量：`CONNECTED`（正常）、`DEGRADED`（性能退化）、`UNAVAILABLE`（不可用）。

### 2. ModelEndpointHealthManager 实现（重写现有存根）
在 `ai-impl/metrics/` 包下重写 `ModelEndpointHealthManager.java`，从存根（getState 返回 String）升级为完整实现：

**状态模型**：每个端点（endpointId）维护独立状态
- CONNECTED ↔ DEGRADED ↔ UNAVAILABLE + CONNECTED → UNAVAILABLE（直接跳转）

**方法签名**：
- `EndpointHealthState getState(String endpointId)` — 返回端点当前健康状态
- `boolean tryProbe(String endpointId)` — 端点 UNAVAILABLE 时判定是否到探测窗口（距离上次探测 >= 30 秒），到窗口返回 true 并记录探测时间，否则返回 false
- `void recordCallResult(String endpointId, boolean success, long elapsedMs)` — 记录调用结果，触发状态转换

**状态转换规则**（见设计文档 §3.2）：
- CONNECTED → DEGRADED：连续 3 次调用耗时 > 阈值（阈值由 `resolveSlowCallThreshold()` 方法判定，固定 5000ms 默认值）
- CONNECTED → UNAVAILABLE：连续 5 次调用失败（success=false）
- DEGRADED → CONNECTED：连续 3 次正常调用（每次耗时 < 阈值）
- DEGRADED → UNAVAILABLE：累积失败次数 >= 5
- UNAVAILABLE → CONNECTED：tryProbe 允许探测且探测成功（recordCallResult 收到 success=true）
- UNAVAILABLE → UNAVAILABLE：探测失败（重置 30 秒计时器）

**内部数据结构**：`ConcurrentHashMap<String, EndpointState>` 存储每个端点的内部状态对象，包含：
- `AtomicReference<EndpointHealthState> healthState`
- `AtomicInteger consecutiveSlowCalls`（连续慢调用计数）
- `AtomicInteger consecutiveFailures`（连续失败计数）
- `AtomicInteger cumulativeFailures`（累积失败计数，用于 DEGRADED→UNAVAILABLE）
- `AtomicInteger consecutiveSuccesses`（连续成功计数，用于 DEGRADED→CONNECTED）
- `AtomicLong lastProbeTime`（上次探测时间戳）
- `AtomicLong lastSlowCallThresholdMs`（慢调用阈值，默认 5000ms）

**线程安全**：所有状态字段使用 `Atomic*` 或 `ConcurrentHashMap`；`recordCallResult` 和 `tryProbe` 方法内部使用 `synchronized` 或 CAS 循环确保状态转换原子性。

### 3. AbstractCapabilityExecutor 适配（修改已有文件）
`AbstractCapabilityExecutor.java`（行 359-363）当前使用 String 比较 `"HEALTHY".equals(healthState)`，修改为使用 `EndpointHealthState` 枚举比较：
```java
EndpointHealthState healthState = endpointHealthManager.getState(routeResult.getEndpointId());
if (healthState == EndpointHealthState.UNAVAILABLE) {
    // 在降级前先检查 tryProbe
    boolean canProbe = endpointHealthManager.tryProbe(routeResult.getEndpointId());
    if (!canProbe) {
        return doDegrade(... DegradationReason.ENDPOINT_UNAVAILABLE ...);
    }
    // canProbe=true → 允许探测调用，管线进入 LLM 调用阶段（超时阈值减半）
}
```

### 4. 测试文件更新（含新建 + 修改）

**新建 `EndpointHealthStateTest.java`** — 覆盖：
- 3 个枚举常量存在性
- CONNECTED/DEGRADED/UNAVAILABLE ordinal 和 name 验证
- null 安全相关测试

**新建 `ModelEndpointHealthManagerTest.java`**（重写现有 2 方法存根测试） — 覆盖以下场景：
1. `getState` 对新端点应返回 CONNECTED（首次访问自动注册）
2. CONNECTED→DEGRADED：连续 3 次慢调用后状态变为 DEGRADED
3. CONNECTED→UNAVAILABLE：连续 5 次失败后状态变为 UNAVAILABLE（直接跳转，跳过 DEGRADED）
4. DEGRADED→CONNECTED：在 DEGRADED 状态下连续 3 次正常调用（success=true, elapsed < 阈值）后回退 CONNECTED
5. DEGRADED→UNAVAILABLE：累积 5 次失败后变为 UNAVAILABLE
6. UNAVAILABLE→CONNECTED：tryProbe 返回 true，recordCallResult(success=true) 后回 CONNECTED
7. UNAVAILABLE 下未到探测窗口：tryProbe 返回 false
8. UNAVAILABLE 下已到探测窗口：tryProbe 返回 true（需操控时间或使用 setter 手动设置 lastProbeTime）
9. 并发安全：20 线程并发混合调用 recordCallResult 和 tryProbe，验证最终状态落在有效转换表范围内

**修改 `AbstractCapabilityExecutorTest.java`** — 适配 `getState` 返回类型从 String 改为 EndpointHealthState：
- ~16 处匿名内部类覆盖 `getState` 的方法从 `return "HEALTHY"` 改为 `return EndpointHealthState.CONNECTED`
- 同理修改 `DiscussionConclusionCapabilityExecutorTest.java`（~3 处）和 `TriageCapabilityExecutorTest.java`（~1 处）

### 5. 涉及文件清单

| 操作 | 文件路径 |
|------|---------|
| 新建 | `ai-impl/.../metrics/EndpointHealthState.java` |
| 重写 | `ai-impl/.../metrics/ModelEndpointHealthManager.java` |
| 修改 | `ai-impl/.../orchestrator/AbstractCapabilityExecutor.java`（行 359-363 适配枚举） |
| 新建 | `ai-impl/.../metrics/EndpointHealthStateTest.java` |
| 新建 | `ai-impl/.../metrics/ModelEndpointHealthManagerTest.java` |
| 修改 | `ai-impl/.../orchestrator/AbstractCapabilityExecutorTest.java`（~16 处 mock 适配） |
| 修改 | `ai-impl/.../orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java`（~3 处 mock 适配） |
| 修改 | `ai-impl/.../orchestrator/impl/TriageCapabilityExecutorTest.java`（~1 处 mock 适配） |

## 选择理由

Batch4 P2 最后一项。ModelEndpointHealthManager 是底座管线的端点健康监控组件（被 AbstractCapabilityExecutor 和 ModelRouter 引用），所有前置依赖均已就绪（SlidingWindowMetricsStore、DegradationContext、ModelRouter）。当前代码存根返回 String，管线中已硬编码 `"HEALTHY"` 字符串比较，必须实装为正式状态机。

## 任务上下文

### 设计文档摘录（06_ood_phase5_G.md §3.2）

```
状态模型:
  CONNECTED ←→ DEGRADED ←→ UNAVAILABLE
  CONNECTED ──────────────────→ UNAVAILABLE (直接跳转)

状态转换表:
  | CONNECTED | 连续 3 次调用耗时 > 阈值 | DEGRADED |
  | CONNECTED | 连续 N 次失败（默认 5） | UNAVAILABLE |
  | DEGRADED | 连续 3 次正常调用 | CONNECTED |
  | DEGRADED | 累积失败次数 >= 5 | UNAVAILABLE |
  | UNAVAILABLE | 探测成功 | CONNECTED |
  | UNAVAILABLE | 探测失败 | UNAVAILABLE |
```

探测调用触发：UNAVAILABLE 下距离上次探测 >= 30 秒时 tryProbe() 返回 true。

### 已有代码上下文

- `ModelEndpointHealthManager.java` — 当前为空壳存根，仅有一个 `getState(String) → String` 方法返回 null
- `ModelEndpointHealthManagerTest.java` — 2 个空测试，仅验证 getState 返回 null
- `AbstractCapabilityExecutor.java:359-363` — 使用 `endpointHealthManager.getState(routeResult.getEndpointId())` 并检查 `!"HEALTHY".equals(healthState)` 触发降级
- 所有 CapabilityExecutor 测试文件中 mock HealthManager 均使用匿名类覆盖 `getState` 返回 `"HEALTHY"` 字符串
- 依赖关系：ModelRouter（已完成）、SlidingWindowMetricsStore（已完成）、DegradationContext（已完成）

## 修订说明（v21 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| plan.md 状态模型错误（UP/DOWN/RECOVERING），task_v21.md 正确使用 CONNECTED/DEGRADED/UNAVAILABLE | task_v21.md 内容无需修改（§1 已使用正确枚举常量）；修正 plan.md R25 NEW 描述 |
| plan.md 方法签名缺失 | task_v21.md 无需修改（§2 已完整定义三个方法签名和状态转换表）；补充 plan.md 实施要点 |
| plan.md 未纳入 AbstractCapabilityExecutor 适配 | task_v21.md 无需修改（§3 已完整定义行 359-363 适配内容）；补充 plan.md 涉及文件清单 |
| plan.md 测试规划缺失 | task_v21.md 无需修改（§4 已完整定义 5 个测试文件）；补充 plan.md 测试规划 |
| plan.md 详细计划过于笼统 | task_v21.md 无需修改（内容已满足要求）；补充 plan.md 为完整实施要点章节 |
