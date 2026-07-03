# 计划审查报告（v19 r2）

## 审查结果
REJECTED

## 发现

- **[严重]** @EnableAsync 缺失导致 @Async 无法生效 — task_v19.md 声称"@Async 支持：Spring Boot 默认启用 @Async，无需额外配置"，但 Spring Boot 不会自动启用 @EnableAsync。整个代码库中无任何 @EnableAsync 声明，也无 ThreadPoolTaskExecutor / AsyncConfigurer 配置。LoggingMetricsCollector.record() 上的 @Async 将被静默忽略，方法将在调用方线程同步执行，违背了"指标写入不影响主流程"的非阻塞设计意图。同时，test_v19.md 规划的"@Async 异步写入验证"测试在当前配置下无法验证真正的异步行为。
  修正方向：选择 A — 在此任务中将 @EnableAsync 添加到已有 @Configuration 类（AiClientConfig 或新增 AiMetricsConfig），与 @Async 注解同步生效；选择 B — 明确将 @EnableAsync 推迟至 Task 18 并承认本阶段 @Async 仅为装饰性（测试仅验证注解存在性），相应修正 test 规划描述。

- **[一般]** AiCallLogEntity 缺少 userId 字段 — AiCallRecord 包含 userId 字段（第 9 行、第 23 行构造器参数、第 47 行 getter），但 AiCallLogEntity 的字段列表和 AiCallRecord→AiCallLogEntity 映射表均未涵盖 userId。任务描述要求"字段与现有 AiCallRecord 对等"，但 userId 被遗漏。对于审计日志而言，userId 是关键的调用方身份上下文。
  修正方向：在 AiCallLogEntity 字段列表及映射表中增加 userId 字段（值来源：`record.getUserId()`，类型 String，可空），同步更新 AiCallLogEntityTest。

## 修改要求（仅 REJECTED 时）

1. **[严重]** 缺少 @EnableAsync 配置：选择 A（本任务添加）或选择 B（推迟并承认装饰性），相应的 task_v19.md 和 test 规划需做同步修正。
2. **[一般]** AiCallLogEntity 遗漏 userId：补充 userId 字段及映射策略，更新测试。
