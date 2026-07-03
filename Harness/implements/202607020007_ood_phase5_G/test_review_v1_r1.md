# 测试审查报告（v1 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** `SlidingWindowMetricsStoreTest.java:159-184` — `concurrentAccessShouldNotCauseDeadlock` 使用 `join()` 无超时参数，若发生死锁将永久挂起整个测试套件，使测试不可靠；且仅有最终 `getFailureRate > 0` 断言，未验证并发场景下的数据完整性（事件总数、统计值正确性），不足以验证线程安全。

- **[轻微]** `SlidingWindowMetricsStoreTest.java:130-140` — `buildDegradationContextShouldReturnMaxLastFailureTimeForMultipleFailures` 仅断言 `ctx2.getLastFailureTime() >= first`，未直接验证返回值是否为最新 FAILURE 事件的时间戳，断言强度不足。

- **[轻微]** `SlidingWindowMetricsStoreTest.java:150-156` — `maxEventsShouldNotBeExceeded` 仅断言 `avg > 0`，未验证队列实际是否被裁剪至 `maxEventsPerCapability` 上限，约束条件未直接测试。

- **[轻微]** `SlidingWindowMetricsStoreTest.java:118` — `buildDegradationContextShouldComputeCorrectStats` 中 `assertTrue(ctx.getSerializedTimestamp() > 0)` 是平凡真条件，建议与调用时刻的 `System.currentTimeMillis()` 进行差值范围校验。

## 修改要求（仅 REJECTED 时）

### 问题 1（一般）：并发测试方法不可靠且覆盖不足
- **位置**: `SlidingWindowMetricsStoreTest.java` 方法 `concurrentAccessShouldNotCauseDeadlock`
- **问题**: (a) `t1.join()` / `t2.join()` / `t3.join()` 无超时参数 —— 死锁时测试永久挂起；(b) 仅断言最终 `getFailureRate > 0`，未验证 2000 次写入的事件总数是否完整、失败率是否落于预期范围（约 0.5）。
- **为什么是问题**: 无超时的 `join()` 使测试套件存在挂起风险，降低 CI 可靠性；缺乏数据完整性断言意味着即使并发写入丢失事件、统计出错，测试仍可通过，无法真正验证线程安全协议的正确性。
- **期望修正方向**: (a) `t1.join(5000)` 加超时 + `assertFalse(t1.isAlive())` 检查线程正常结束；(b) 并发写入结束后，断言 `getInvocationCount`（若可观测）或等价总事件数，以及失败率落在 [0.45, 0.55] 区间。
