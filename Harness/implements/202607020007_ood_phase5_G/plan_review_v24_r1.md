# 计划审查报告（v24 r1）

## 审查结果
REJECTED

## 发现

### **[严重] 问题1：DegradationStrategy Bean 未注册为 Spring Bean，`getBeansOfType` 获取不完整**

`task_v24.md` 上下文声明："各 DegradationStrategy 实现：标记 @Component("timeout")、@Component("circuit-breaker")、@Component("noop")，被 AiPlatformConfig.@PostConstruct 通过 getBeansOfType 加载"

但实际代码中，仅 `NoOpDegradationStrategy` 标注了 `@Component`。`TimeoutDegradationStrategy` 和 `CircuitBreakerDegradationStrategy` 是纯 POJO，无任何 Spring 注解。

后果：`AiPlatformConfig.@PostConstruct` 执行 `context.getBeansOfType(DegradationStrategy.class)` 时只会找到 `NoOpDegradationStrategy`（1/3），降级策略 Map 不完整，管线降级行为静默失效。

修正方向：方案 A — 在本任务中给 `TimeoutDegradationStrategy` 和 `CircuitBreakerDegradationStrategy` 添加 `@Component` 注解；方案 B — 在 `AiPlatformConfig` 的 `@PostConstruct` 中通过 `context.getBeansOfType(...)` 兜底手动注册，但需作为本任务涉及文件纳入计划。

### **[严重] 问题2：`@Value("${...}")` 无法注入 `Map<String, Duration>`**

`task_v24.md` 中 3 个 `@Bean` 方法试图使用 `@Value("${ai.execution.timeout.per-capability}")` 返回 `Map<String, Duration>`。`${}` 占位符语法只能解析 Environment 中的单值 String 属性，不能解析结构化的 Map 类型。

YAML 中的嵌套 Map（如 `ai.execution.timeout.per-capability.TRIAGE`）被 Spring 扁平化为独立属性键，不存在一个名为 `ai.execution.timeout.per-capability` 的单一属性返回整个 Map。

设计文档 §3.9 明确规定了正确的方案：通过 `@EnableConfigurationProperties` 引入 `AiExecutionProperties`，@Bean 方法注入该属性类后调用 `executionProperties.getPerCapability()`。

修正方向：废弃 `@Value` 注入方案，改为遵循设计文档的模式：
- 创建 `AiExecutionProperties`、`AiDegradationProperties` 等 `@ConfigurationProperties` 类
- @Bean 方法注入这些属性类，提取 Map 字段

### **[严重] 问题3：缺失 7 个 `@ConfigurationProperties` 属性类**

设计文档 §3.9 定义了 8 个 `@ConfigurationProperties` 类，当前仅 `AiRouterProperties` 存在。缺少以下 7 个：
- `AiExecutionProperties`（前缀 `ai.execution`）
- `AiDegradationProperties`（前缀 `ai.degradation`）
- `AiRateLimitingProperties`（前缀 `ai.rate-limiting`）
- `AiMetricsAsyncProperties`（前缀 `ai.metrics.async`）
- `AiPlatformProperties`（前缀 `ai.platform`）
- `AiSlidingWindowProperties`（前缀 `ai.sliding-window`）
- `AiTemplateProperties`（前缀 `ai.template.fallback`）

不创建这些类的后果：
- `@EnableConfigurationProperties` 无法引入属性绑定，Map 注入无可用来源
- 配置类职责不清，所有配置集中在单一 @Bean 方法中违反设计意图
- 设计文档中的 `AiPlatformConfig` 类型定位明确列出这些类

修正方向：创建全部 7 个缺失的 `@ConfigurationProperties` 类，纳入涉及文件清单；`AiPlatformConfig` 标注 `@EnableConfigurationProperties` 逐一引入。

### **[严重] 问题4：缺失 4 个线程池 `@Bean` 定义**

设计文档 §3.9 明确要求以下线程池 @Bean：
- `@Bean("llmCallExecutor")` — 被 `AbstractCapabilityExecutor` 和全部 13× 具体执行器构造器注入，当前无此 Bean
- `@Bean("metricsAsyncExecutor")` — 被 `LoggingMetricsCollector.@Async("metricsAsyncExecutor")` 引用
- `@Bean("transcriptSummaryExecutor")` — 被 `DiscussionConclusionCapabilityExecutor` 前置 LLM 压缩调用引用
- `@Bean("scheduledTaskExecutor")` — 底座内多个 `@Scheduled` 任务共享

`task_v24.md` 的 @Bean 定义清单为零个线程池 Bean。无 `llmCallExecutor` Bean 将导致所有 CapabilityExecutor 构造器的 `Executor llmCallExecutor` 参数无法 Autowire，启动失败。

修正方向：将以上 4 个线程池 @Bean 纳入 task_v24.md 的 @Bean 方法清单，其中 `metricsAsyncExecutor` 依赖 `MetricsAsyncProperties`，`llmCallExecutor` 和 `transcriptSummaryExecutor` 依赖线程池参数配置。

### **[严重] 问题5：缺失 `@EnableJpaRepositories` 和 `@EntityScan`**

设计文档 §3.9 明确要求：
> 需在 AiPlatformConfig 或其他顶层配置类上声明 `@EnableJpaRepositories` 和 `@EntityScan` 确保 Spring Data JPA 正确扫描到所有 Repository 和 Entity。

`task_v24.md` 完全未提及 JPA 扫描配置。`AiPlatformConfig` 的形态仅列出 `@Configuration` + `@EnableAsync` + `ApplicationContextAware`。

后果：`PromptTemplateRepository`、`ExperimentRepository`、`AiCallLogRepository` 及其对应 @Entity 不会被扫描，Spring 抛出 `NoSuchBeanDefinitionException`。

修正方向：类形态补充 `@EnableJpaRepositories` 和 `@EntityScan`，扫描范围 `com.aimedical.modules.ai.impl`。

### **[一般] 问题6：`@Value` 在 `@Bean` 方法上语法不正确**

`task_v24.md` 中多处出现 `@Bean @Value("${...}") Map<String, Duration> method(...)` 的写法。`@Value` 标注在 `@Bean` 方法上不代表注入返回值——Spring 中 `@Value` 用于字段/参数注入。正确的写法应当将 `@Value` 放在方法参数前（对单值注入而言），或按设计文档注入 `@ConfigurationProperties` 类。

### **[一般] 问题7：`chatFallbackTimeout` 未定义**

启动期配置校验约束 `parseTimeout <= chatFallbackTimeout` 引用了 `chatFallbackTimeout`，但该变量既未在 @Bean 定义中出现，也未作为配置属性定义。校验无法执行。

修正方向：明确 `chatFallbackTimeout` 的来源——是 `capabilityTimeoutConfig` 中的值？还是独立的属性？应在 @Bean 或 @ConfigurationProperties 中明确定义。

### **[一般] 问题8：缺失测试规划**

`AiPlatformConfig` 内含复杂的启动期初始化逻辑（@PostConstruct 降级策略 Map 构建、配置校验）、定时刷新（@Scheduled 3 个方法）、多 Bean 装配。当前任务无任何测试策略或测试文件规划。

修正方向：补充测试规划，至少覆盖：
- @PostConstruct 策略 Map 构建正确性
- 启动期配置校验（per-capability >= thin-adapter + 5s, parseTimeout <= chatFallbackTimeout）
- @Scheduled refreshCapabilityTimeoutConfig 刷新有效性
- AiPlatformEnvironmentPostProcessor 属性转发
- 删除 AiClientConfig 后不破坏现有 Bean 装配

### **[轻微] 问题9：AiRouterProperties @TODO 未处理**

`AiRouterProperties.java:11-15` 包含 `@TODO Phase5:` 注释，明确声明"此存根类将在 Task 18（AiPlatformConfig）中被吸收或委托为更合适的配置加载方式"。计划仅说"保留"而未解决其 ModelRoute 不可变对象无法被 @ConfigurationProperties setter 绑定问题。该问题虽非新引入，但应在本次任务中一并处理。

## 修改要求

以上 5 个严重 + 4 个一般问题均需修正。核心修正方向：

1. **@ConfigurationProperties 体系**：创建 7 个缺失的属性类，`AiPlatformConfig` 标注 `@EnableConfigurationProperties`——替代当前错误的 `@Value` Map 注入方案
2. **线程池 Bean**：补充 4 个线程池 `@Bean` 定义（llmCallExecutor/metricsAsyncExecutor/transcriptSummaryExecutor/scheduledTaskExecutor）
3. **JPA 扫描**：类形态补充 `@EnableJpaRepositories` + `@EntityScan`
4. **DegradationStrategy Bean**：方案 A — 添加 `@Component` 到两个策略实现（纳入涉及文件）；方案 B — 改为通过 `AiPlatformConfig` 手动创建 @Bean
5. **AiClientConfig 删除前确认**：确认 `@EnableAsync` 已在 AiPlatformConfig 保留
6. **补充测试规划**：至少包含 AiPlatformConfigTest 和 AiPlatformEnvironmentPostProcessorTest
