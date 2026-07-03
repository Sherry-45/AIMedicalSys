# 测试审查报告（v7 r1）

## 审查结果
REJECTED

## 发现

- **[严重]** `CircuitBreakerDegradationStrategyTest.java:301-309` — 测试 `shouldOpenOnLaterClosedCallAfterFailureRateRises` 断言错误。测试记录 2 次成功 + 1 次失败后，failureRate = 33.3%，低于阈值 50%（`FAILURE_THRESHOLD = 0.5`），但 `assertTrue(strategy.shouldDegrade(ctx))` 断言应返回 true。实际代码中 `failureRate >= failureRateThreshold` 为 `0.333 >= 0.5` = false，将返回 false，测试必然失败。

- **[轻微]** `CircuitBreakerDegradationStrategyTest.java:286-291` — 测试 `shouldTriggerOpenOnFirstClosedCallWithHighFailureRate` 与已有测试 `shouldTransitionToOpenAndReturnTrueOnHighFailureRate`（第 58-64 行）完全一致，是重复测试。不影响正确性，但降低可维护性。

- **[轻微]** `CircuitBreakerDegradationStrategyTest.java:293-299` — 测试 `shouldNotOpenWhenFirstClosedCallHasLowFailureRate` 与已有测试 `shouldNotDegradeWhenFailureRateBelowThreshold`（第 42-48 行）行为覆盖重叠，仅增加状态断言，差异化有限。

## 修改要求（仅 REJECTED 时）

### 严重问题

**文件**: `CircuitBreakerDegradationStrategyTest.java:301-309`
**问题**: 测试 `shouldOpenOnLaterClosedCallAfterFailureRateRises` 设定的 failureRate（1/3 ≈ 33.3%）未达到阈值（50%），`assertTrue(strategy.shouldDegrade(ctx))` 与代码逻辑矛盾，测试必然失败。
**期望修正方向**: 调整 metricsStore 记录的数据，使第二次 `shouldDegrade()` 调用前的 failureRate ≥ threshold（50%）。例如：在第一次 shouldDegrade 返回 false 后再 recordFailure 两次，使队列变为 2 successes + 2 failures = 50% ≥ 50%。
