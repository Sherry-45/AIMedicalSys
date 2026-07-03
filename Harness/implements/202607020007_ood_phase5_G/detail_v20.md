# 详细设计（v20）

## 概述

修复 `LoggingMetricsCollectorTest.shouldSaveEntityWhenRecordCalled` 中 promptVersion 测试数据，使 `parsePromptVersion("2")` 返回 `Integer.valueOf(2)`，消除 v19 验证中唯一失败用例。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-impl/src/test/java/com/aimedical/modules/ai/impl/metrics/LoggingMetricsCollectorTest.java` | 修改 | 第 43 行 `"v2"` → `"2"` |

## 类型定义

**无变更**。仅测试数据修改，无需新增或修改类型。

## 错误处理

**无变更**。`parsePromptVersion()` 设计正确（null→null、有效整数→parse、无效→null），无需修改生产代码。

## 行为契约

**无变更**。测试夹具数据与断言对齐即可。

## 依赖关系

**无变更**。

## 修订说明（v20 r1）

| 审查意见 | 修改措施 |
|---------|---------|
| v19 verify FAILED — `shouldSaveEntityWhenRecordCalled:57` expected `<2>` but was `<null>` | 将第 43 行 `"v2"` 改为 `"2"`，使 `parsePromptVersion` 正确返回 `Integer.valueOf(2)` |
