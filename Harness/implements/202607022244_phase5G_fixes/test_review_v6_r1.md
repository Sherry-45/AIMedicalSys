# 测试审查报告（v6 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** `CircuitBreakerDegradationStrategyTest.java` — 测试覆盖不足：行为契约2 要求验证"当 endpoint 已有失败记录（failureCount > 0）时，使用 capability 级失败率决定是否 OPEN"，但测试套件缺少 CLOSED→OPEN 转换路径的覆盖。现有 `shouldNotDegradeInClosedStateWhenNoCircuitData` 仅验证无数据返回 false 的路径，`shouldMaintainIndependentStateByEndpointId` 被弱化为双向验证干净 endpoint 不降级而未覆盖隔离本质。需补充用例：设置 `circuitDataMap` 存在该 endpoint 条目且 `failureCount > 0`、模拟 `getFailureRate >= threshold`，验证 `shouldDegrade()` 返回 true 且状态转为 OPEN。

## 修改要求（仅 REJECTED 时）

1. `CircuitBreakerDegradationStrategyTest.java` — 位置：新增测试方法。问题：缺少 CLOSED→OPEN 转换路径的测试。设计契约（detail_v6.md:85-86）明确要求"当该 endpoint 已有失败记录时，使用 capability 级失败率决定是否 OPEN"，但仅测试了无数据场景。期望：新增测试（如 `shouldTransitionToOpenWhenFailureRateExceedsThreshold`），通过 setter/反射在 `circuitDataMap` 中预设 endpoint 条目（`failureCount > 0`），mock `metricsStore.getFailureRate()` 返回超阈值值，调用 `shouldDegrade()` 断言返回 true 且状态为 OPEN。
