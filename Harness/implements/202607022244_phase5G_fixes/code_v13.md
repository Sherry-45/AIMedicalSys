# 实现报告（v13）

## 概述

实现了 R12 RETRY（修复 DiscussionConclusionCapabilityExecutorTest.java 末尾多余 `}`）及 T62/T54/T55 三项模板管理修复：promptVersion 类型从 `String` 统一改为 `Integer`；DatabasePromptTemplateManager 缓存键包含版本号并在 resolveExactVersion 中先查缓存；warmup 同时缓存 version 键。

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `template/PromptTemplateManager.java` | `render()` 第4参数 `String` → `Integer` |
| 修改 | `template/DatabasePromptTemplateManager.java` | T62+T54+T55：render/resolveExactVersion 签名变更；resolveExactVersion 先查缓存后查DB；新增 buildCacheKey 重载；warmup 双键缓存 |
| 修改 | `orchestrator/AbstractCapabilityExecutor.java` | executeStandardPipeline/doDegrade/handleSuccess 签名 `String` → `Integer`；AiCallRecord 构造时 `String.valueOf()` 转换 |
| 修改 | `orchestrator/impl/TriageCapabilityExecutor.java` | `@Value` 默认值 `:TRIAGE` → `:0`，字段 `String` → `Integer` |
| 修改 | `orchestrator/impl/KbQueryCapabilityExecutor.java` | 同上 `:KB_QUERY` → `:0` |
| 修改 | `orchestrator/impl/MedicalRecordGenCapabilityExecutor.java` | 同上 `:MEDICAL_RECORD_GEN` → `:0` |
| 修改 | `orchestrator/impl/PrescriptionAssistCapabilityExecutor.java` | 同上 `:RX_ASSIST` → `:0` |
| 修改 | `orchestrator/impl/PrescriptionCheckCapabilityExecutor.java` | 同上 `:RX_AUDIT` → `:0` |
| 修改 | `orchestrator/impl/ScheduleCapabilityExecutor.java` | 同上 `:SCHEDULE` → `:0` |
| 修改 | `orchestrator/impl/DiscussionConclusionCapabilityExecutor.java` | 同上 `:DISCUSSION_CONCLUSION` → `:0` |
| 修改 | `orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java` | R12 RETRY：删除多余 `}` |
| 修改 | `template/DatabasePromptTemplateManagerTest.java` | T62：`"2"`→`2`（3处），测试方法改名 `nullVersionShouldFallbackToActive`，`"not-a-number"`→`null` |
| 修改 | `orchestrator/AbstractCapabilityExecutorTest.java` | T62：23处 `"v1"`→`1`，2处 doDegrade 签名 `String promptVersion`→`Integer promptVersion` |

## 编译验证

编译通过。测试运行 560 例，2 例失败均为预存问题（SlidingWindowMetricsStoreTest、TimeoutDegradationStrategyTest，非本次变更引入）。

## 设计偏差说明

| 设计规格 | 偏差原因 | 实际处理 |
|---------|---------|---------|
| T55: `warmup()` 中 `if (pt.getVersion() != null)` | `PromptTemplate.getVersion()` 返回 `int` 原始类型，不可与 `null` 比较 | 改为 `if (pt.getVersion() > 0)` |
