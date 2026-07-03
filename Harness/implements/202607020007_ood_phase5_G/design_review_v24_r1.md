# 设计审查报告（v24 r1）

## 审查结果
REJECTED

## 发现

### [严重] `refreshCapabilityTimeoutConfig()` 方法体为空（detail_v24.md:328-333）

`@Scheduled(fixedDelay = 60000)` 声明该方法每 60 秒定时刷新超时配置，但方法体内仅包含注释，无任何实际操作。Spring 的 `@Bean` 方法默认为 singleton 范围，`capabilityTimeoutConfig` 的返回值在首次调用后被缓存，该定时方法无法更新其内容。

**影响**：方法名和行为严重背离——调用方（含测试计划 `shouldRefreshCapabilityTimeoutConfigOnScheduledRefresh`）预期配置会被刷新，实际毫无效果。同时浪费每 60 秒的线程调度开销。

**修正方向**：要么实现实际的刷新逻辑（如通过 `Environment` 重新读取配置并发布事件通知消费者更新引用），要么移除 `@Scheduled` 注解并在方法体添加 `@TODO` 标注暂未实现。

### [严重] `refreshWindowSeconds()` 解析 windowSeconds 后未传播到 SlidingWindowMetricsStore（detail_v24.md:335-348）

方法从 Environment 解析 `ai.sliding-window.window-seconds` 得到 `windowSeconds` 变量，但后续代码仅声明 `SlidingWindowMetricsStore store` 变量而未对 `store` 做任何操作。注释提到"通过 AtomicLong 发布更新"但实际未实现任何更新机制。SlidingWindowMetricsStore 已存在 `package-private void setWindowSeconds(long)` 方法（SlidingWindowMetricsStore.java:167），可直接调用。

**影响**：该定时刷新方法同为空操作，与标称功能不符。配置中修改 `ai.sliding-window.window-seconds` 对运行时无任何效果。

**修正方向**：在解析成功后调用 `store.setWindowSeconds(windowSeconds)`；如需要考虑线程间可见性，将 `SlidingWindowMetricsStore.windowSeconds` 改为 `volatile`（当前已是 volatile，详见 SlidingWindowMetricsStore.java:15）。

### [一般] `refreshDegradationStrategies()` 无声丢弃找不到的策略名（detail_v24.md:302-325）

`@PostConstruct` 的 `buildDegradationStrategyMap()` 在策略名未匹配到 Bean 时记录 `log.warn`（line 257），但定时刷新 `refreshDegradationStrategies()` 中对应的 `for` 循环仅在 `strategy != null` 时添加策略，未匹配时无声跳过（line 314-318），缺少 `else` 分支的 `log.warn`。

**影响**：运行时若 YAML 配置引用了一个不存在的策略名，启动期可以感知（@PostConstruct 有日志），但定时刷新后配置被原子替换，丢失的策略名不再有任何可见告警，运维难以排查。

**修正方向**：在 `refreshDegradationStrategies()` 中补充与 `buildDegradationStrategyMap()` 一致的 `log.warn` 日志。

### [一般] 3 个 @ConfigurationProperties 类未注册为 Spring 管理的 Bean（detail_v24.md:565-619）

`AiPlatformProperties`、`AiSlidingWindowProperties`、`AiTemplateProperties` 标注了 `@ConfigurationProperties` 但未被加入 `@EnableConfigurationProperties`（line 88-94），也未通过 `@Component` 或 `@ConfigurationPropertiesScan` 注册。它们将不会被 Spring Boot 自动绑定，`@ConfigurationProperties` 注解实际不生效。

**影响**：
- `AiSlidingWindowProperties` 的 `maxEventsPerCapability` 即使在配置文件中设定也无法绑定（SlidingWindowMetricsStore 中该字段为 `final int maxEventsPerCapability = 10000` 硬编码，也不可调整）。
- 这些类目前虽未在底座中注入，但作为设计产物的「7 个 @ConfigurationProperties 属性类」定义不完整，后续使用者将发现它们如同普通 POJO。

**修正方向**：将三个类加入 `@EnableConfigurationProperties`，或者标注 `@ConfigurationPropertiesScan(basePackages = "...")`。

### [一般] 测试计划 `shouldRefreshCapabilityTimeoutConfigOnScheduledRefresh` 与空方法体冲突（task_v24.md:293）

任务文件的测试规划要求该测试方法"调用 refreshCapabilityTimeoutConfig() 后验证 Map 值更新"，但方法体为空，无法实现任何值变更。

**影响**：该测试要么永远通不过，要么被迫写成无意义的通过（如验证与刷新无关的状态），失去测试意义。

**修正方向**：同 [严重] 第 1 项，必须先实现刷新逻辑，或调整测试规划。

### [轻微] 未使用的 import（detail_v24.md:48-50, 65）

`java.util.concurrent.atomic.AtomicLong`（line 49）和 `org.springframework.scheduling.annotation.Async`（line 65）被导入但未被任何代码使用。

**影响**：不符合项目代码风格实践，import 冗余。

**修正方向**：移除未使用的 import。

### [轻微] `buildDegradationStrategyMap()` 与 `refreshDegradationStrategies()` 代码重复（detail_v24.md:242-264 vs 302-325）

两个方法包含完全相同的降级策略 Map 构建逻辑（从 `context.getBeansOfType` 获取策略 + 按配置白名单组装），仅日志处理不同。

**影响**：维护时需要修改两个地方，容易遗漏。

**修正方向**：抽取公共方法 `rebuildStrategyMap(AiDegradationProperties props, boolean warnOnMissing)` 减少重复。

### [轻微] @Bean 方法返回类型与任务规格不完全一致（detail_v24.md:158, 166, 184 vs task_v24.md:39, 51, 73）

任务规格中 `llmCallExecutor`、`metricsAsyncExecutor`、`transcriptSummaryExecutor` 返回类型均为 `Executor`，设计中改为具体的 `ThreadPoolExecutor`/`ThreadPoolTaskExecutor`。

**影响**：功能正确，但若消费者按 `Executor` 接口注入，使用具体类型的方法（如 `setThreadNamePrefix`）将不可在注入点调用。

**修正方向**：建议保持 `Executor` 返回类型，或在类型注释中说明为何需要具体类型。
