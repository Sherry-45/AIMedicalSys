# 测试报告（v7）

## 概述

为修复 A（CircuitBreakerDegradationStrategy）和修复 B（AbstractCapabilityExecutor.handleSuccess()）编写/补充单元测试。

## 测试文件变更

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `ai-impl/src/test/java/.../degradation/CircuitBreakerDegradationStrategyTest.java` | 新增2个、删除1个测试用例 |
| 修改 | `ai-impl/src/test/java/.../orchestrator/AbstractCapabilityExecutorTest.java` | 新增1个测试用例 |

## 测试用例变更

### CircuitBreakerDegradationStrategyTest（修复 A）

- **删除** `shouldTriggerOpenOnFirstClosedCallWithHighFailureRate` — 与已有 `shouldTransitionToOpenAndReturnTrueOnHighFailureRate` 完全重复（审查意见 v7 r1 minor#1）

**保留**（以下 2 个用例）：

1. **`shouldNotOpenWhenFirstClosedCallHasLowFailureRate`** — 验证首次调用时若 failureRate 低于阈值，computeIfAbsent 创建条目但保持 CLOSED，不应误触发 OPEN。与 `shouldNotDegradeWhenFailureRateBelowThreshold` 略有重叠但增加状态断言，差异化合理，予以保留（审查意见 v7 r1 minor#2 分歧）
2. **`shouldOpenOnLaterClosedCallAfterFailureRateRises`** — 验证 failureCount 跨多次 CLOSED 调用累积，当 failureRate = 50% 时最终触发 OPEN 转换（审查意见 v7 r1 severe：已修正记录为 2 success + 2 failure）

### AbstractCapabilityExecutorTest（修复 B）

4. **`executeStandardPipelineShouldHandleNullMetricsStoreOnSuccess`** — 验证 metricsStore == null 时 handleSuccess() 静默跳过 recordSuccess，不抛 NPE，管道正常返回成功结果

## 行为契约覆盖

| 契约 | 覆盖测试 | 验证方式 |
|------|---------|---------|
| Fix A: 每次 shouldDegrade() 递增 failureCount | shouldOpenOnLaterClosedCallAfterFailureRateRises | 多次 CLOSED 调用后 failureRate=50% 触发 OPEN，间接证明 failureCount 累积 |
| Fix A: CLOSED→OPEN 重置 failureCount=1 | shouldSetFailureCountToOneWhenTransitionToOpen（已有） | OPEN 转换后 failureCount 被重置 |
| Fix A: computeIfAbsent 确保首次调用创建条目 | shouldNotOpenWhenFirstClosedCallHasLowFailureRate | 首次 CLOSED 调用创建条目但保持 CLOSED（R6 时 computeIfAbsent 缺失导致路径死锁） |
| Fix B: metricsStore==null 时静默跳过 recordSuccess | executeStandardPipelineShouldHandleNullMetricsStoreOnSuccess | null metricsStore 下成功执行不抛 NPE |
| Fix B: 与已有 null 守卫模式一致 | executeStandardPipelineShouldHandleNullMetricsStoreOnSuccess | 与 checkPreDegradation()、doDegrade() 相同规避模式 |
