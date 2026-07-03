# 计划审查报告（v19 r1）

## 审查结果
REJECTED

## 发现

- **[严重]** `@Async("metricsAsyncExecutor")` 无对应 Bean 导致启动失败 — task_v19.md 要求 `LoggingMetricsCollector.record()` 标注 `@Async("metricsAsyncExecutor")`，但同时声明"无需自定义线程池 Bean（自定义线程池推迟至 AiPlatformConfig 阶段）"。Spring 按名称查找 `metricsAsyncExecutor` Executor Bean 时不存在，context 刷新时抛出 `NoSuchBeanDefinitionException`，应用无法启动。该命名 Bean 在当前代码库中也不存在。**修正方向**：方案 A（推荐）— 当前任务先用 `@Async` 无 qualifier，使用 Spring 默认 `applicationTaskExecutor`，待 Task 18 AiPlatformConfig 时再改名为 `metricsAsyncExecutor` 并配置专用线程池；方案 B — 在本任务中创建最小 `@Bean(name = "metricsAsyncExecutor")` ThreadPoolTaskExecutor（core=1, max=2, queue=1000, DiscardPolicy），但违背"推迟至 AiPlatformConfig"的任务约定。

- **[一般]** AiCallLogEntity 字段与现有 AiCallRecord 差异较大，缺失字段映射策略未定义 — 现有 `AiCallRecord` 仅 15 个字段（无 callTime、capabilityName、inputSummary、outputSummary、errorCode、errorMessage、retryCount、totalTokens），而 AiCallLogEntity 需 22+ 业务字段。task 仅明确 callTime（`LocalDateTime.now()`）和 capabilityName（=capabilityId）的填充策略，其余缺失字段（inputSummary/outputSummary/errorCode/errorMessage/totalTokens/promptVersion 等）的默认值或 null 策略未说明，实现者可能做出不一致选择。**修正方向**：在实施要点中显式列出每个 AiCallRecord 到 AiCallLogEntity 的字段映射表，标注哪些字段直接复制、哪些用 null/default、哪些需派生。

- **[轻微]** plan.md row 14 "涉及文件"列 `metrics/ 下指标采集全部类型` 表述过于笼统，未区分已有文件（AiMetricsCollector.java 存根、AiCallRecord.java）和本任务新建文件，可能导致后期追踪混淆。**修正方向**：明确列出 5 个新建文件路径，并注明 2 个已有文件无需修改。

## 修改要求（仅 REJECTED 时）

1. **[严重]** `@Async("metricsAsyncExecutor")` 与"不创建 Executor Bean"矛盾。选择方案 A：改为 `@Async`（无 qualifier），使用 Spring 默认 executor；或方案 B：新增最小 metricsAsyncExecutor Bean。推荐方案 A，代价最小且符合"推迟至 AiPlatformConfig"的约定。
2. **[一般]** 补全 AiCallLogEntity 缺失字段的映射策略表，明确哪些字段从 AiCallRecord 直接复制、哪些用 null/default 值。
