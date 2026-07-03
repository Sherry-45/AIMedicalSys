# 任务指令（v2）

## 动作
RETRY

## 任务描述
修复 v1 验证失败的 1 个测试用例 + 执行 code review 建议的变量命名清理。

### 具体变更

#### 1. 修复测试断言（必需）
- 文件: `AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/metrics/SlidingWindowMetricsStoreTest.java`
- 行 150-151: `assertTrue(ctx2.getLastFailureTime() > first, ...)` → `assertTrue(ctx2.getLastFailureTime() >= first, ...)`
- 原因：连续两次 `recordFailure` 在同一毫秒内发生时，`System.currentTimeMillis()` 返回相同值，导致 `lastFailureTime == first`，严格大于断言失败。其他三重断言（行 146-149）已确保 `lastFailureTime` 在合理时间区间内，`>=` 语义正确。

#### 2. 清理变量命名（建议，来自原 code review APPROVED）
- 文件: `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/metrics/SlidingWindowMetricsStore.java`
- 行 130-131: `totalElapsedDegraded` → `totalElapsedNonFailure`，`degradedEventCount` → `nonFailureEventCount`
- 原因：这两个变量同时累加 NORMAL_SUCCESS 与 DEGRADED 两类事件，原变量名暗示仅含 DEGRADED，降低可读性。

## RETRY 说明

### 失败原因
验证报告 verify_v1.md 显示 `buildDegradationContextShouldReturnMaxLastFailureTimeForMultipleFailures` 测试用例失败。失败输出：
```
second lastFailureTime should be strictly greater than first ==> expected: <true> but was: <false>
```

### 根因分析
测试流程：
1. `store.recordFailure("cap1")` — 记录第一次故障（时间戳 T1）
2. `ctx1 = store.buildDegradationContext(...)` — 获取 `first = T1`
3. `store.recordFailure("cap1")` — 记录第二次故障（时间戳 T2）
4. `ctx2 = store.buildDegradationContext(...)` — 获取 `lastFailureTime = max(T1, T2)`
5. 断言：`ctx2.getLastFailureTime() > first` → 即 `max(T1, T2) > T1`

当 T2 == T1（同一毫秒内）时，`max(T1, T2) == T1`，断言失败。

### 修正方向
将 `>` 改为 `>=`。此修正是安全的，因为：
- `buildDegradationContext` 的 `>` 找出 max 的逻辑不变（若两值相等，任一值即为正确 max）
- 行 146-149 已通过捕获 `beforeSecond`/`afterSecond` 确保 `lastFailureTime` 落在合理区间
- 时间精度依赖在单元测试中应最小化，使用宽松边界