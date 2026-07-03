# 详细设计（v2）

## 概述

v2 为 retry 轮次，仅修复 v1 验证失败的 1 个测试断言 + 执行 code review 建议的变量命名清理。不涉及新增/删除类型，不改变任何行为契约。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/metrics/SlidingWindowMetricsStore.java` | 修改 | 变量重命名（行 130-131） |
| `AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/metrics/SlidingWindowMetricsStoreTest.java` | 修改 | 修复断言（行 150） |

## 类型定义

无变更。所有类型签名、公开接口、行为契约与 v1 设计一致。

## 变更清单

### 1. 测试断言修复

**文件**: `SlidingWindowMetricsStoreTest.java`
**位置**: 行 150-151
**变更**: `assertTrue(ctx2.getLastFailureTime() > first, ...)` → `assertTrue(ctx2.getLastFailureTime() >= first, ...)`
**原因**: 连续两次 `recordFailure` 在同一毫秒内发生时，`System.currentTimeMillis()` 返回相同值，`lastFailureTime == first`。其余三重断言（行 146-149）已确保 `lastFailureTime` 在合理时间区间内，`>=` 语义正确。

### 2. 变量命名清理

**文件**: `SlidingWindowMetricsStore.java`
**位置**: 行 130-131
**变更**:
- `totalElapsedDegraded` → `totalElapsedNonFailure`
- `degradedEventCount` → `nonFailureEventCount`
**原因**: 这两个变量同时累加 NORMAL_SUCCESS 与 DEGRADED 两类事件，原变量名暗示仅含 DEGRADED，降低可读性。

## 行为契约

无变更。`buildDegradationContext` 中的 `lastFailureTime` 计算逻辑不变（取最大 FAILURE 事件 timestamp），两种重命名后的变量语义不变。

## 依赖关系

无变更。

## 修订说明（v2 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| 测试断言 `>` 应为 `>=` | 将 `SlidingWindowMetricsStoreTest.java:150` 的 `>` 改为 `>=` |
| 变量名 `totalElapsedDegraded`/`degradedEventCount` 应体现同时累加 NORMAL_SUCCESS 和 DEGRADED | 重命名为 `totalElapsedNonFailure`/`nonFailureEventCount` |
