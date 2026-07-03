# 代码审查报告（v11 r1）

## 审查结果
REJECTED

## 发现

- **[严重]** `ai-impl/.../metrics/SlidingWindowMetricsStoreTest.java:38-39` — T30 将 `DegradationContext.invocationCount` / `failureCount` 由 `Integer` 改为 `int` 后，`assertNull(ctx.getInvocationCount())` 和 `assertNull(ctx.getFailureCount())` 必然失败：`int` 原始类型默认值为 `0`，无法为 `null`。实现者虽修复了同一文件中的 2 处 `.intValue()` 调用，但遗漏了这 2 处 `assertNull` → `assertEquals(0, ...)` 的变更。

## 修改要求

1. **`SlidingWindowMetricsStoreTest.java:38`**: `assertNull(ctx.getInvocationCount())` → `assertEquals(0, ctx.getInvocationCount())`
2. **`SlidingWindowMetricsStoreTest.java:39`**: `assertNull(ctx.getFailureCount())` → `assertEquals(0, ctx.getFailureCount())`
