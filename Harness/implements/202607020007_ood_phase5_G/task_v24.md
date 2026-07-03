# 任务指令（v24 r1 修订）

## 动作
NEW（审议修订）

## 任务描述
在 ai-impl/config/ 包新建底座统一配置装配体系，包含：AiPlatformConfig（统一 @Configuration 装配类）、AiPlatformEnvironmentPostProcessor（配置转发前置处理器）、7 个 @ConfigurationProperties 属性类、ModelRouteConfig 可变 DTO（解决 AiRouterProperties 的 YAML 绑定限制）；同步修改 TimeoutDegradationStrategy 和 CircuitBreakerDegradationStrategy 添加 @Component 注解；修改 AiRouterProperties 和 DefaultModelRouter 适配可绑定配置类型；删除 AiClientConfig（被 AiPlatformConfig 吸收）。

### 1. AiPlatformConfig
**文件**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiPlatformConfig.java`
**形态**：`@Configuration` + `@EnableConfigurationProperties({AiRouterProperties.class, AiExecutionProperties.class, AiDegradationProperties.class, AiRateLimitingProperties.class, AiMetricsAsyncProperties.class})` + `@EnableJpaRepositories(basePackages = "com.aimedical.modules.ai.impl")` + `@EntityScan(basePackages = "com.aimedical.modules.ai.impl")` + `@EnableAsync` + `implements ApplicationContextAware`
**职责**：底座统一配置装配类，吸收现有的 AiClientConfig 的全部 @Bean 定义；通过 @ConfigurationProperties 注入绑定 Map<String, Duration> 等结构化配置（禁止使用 @Value 注入 Map）；管理 4 个线程池 Bean；在 @PostConstruct 阶段构建降级策略 Map、执行启动期配置校验；提供 @Scheduled 定时刷新方法。

#### 必须包含的 @Bean 方法（从 AiClientConfig 吸收）：
```java
@Bean
HttpApiLlmChatService httpApiLlmChatService(CredentialProvider cp, EndpointRateLimiter er)

@Bean
HttpApiLlmChatStreamService httpApiLlmChatStreamService(CredentialProvider cp, EndpointRateLimiter er)

@Bean
SpringAiLlmChatService springAiLlmChatService()

@Bean
SpringAiLlmChatStreamService springAiLlmChatStreamService()

@Bean
@Primary
DelegatingLlmChatService delegatingLlmChatService(
    HttpApiLlmChatService httpApi, SpringAiLlmChatService springAi)
```

#### 线程池 @Bean 定义（新增，4 个）：

**llmCallExecutor**：
```java
@Bean("llmCallExecutor")
public Executor llmCallExecutor() {
    int coreSize = Runtime.getRuntime().availableProcessors();
    return new ThreadPoolExecutor(coreSize, 2 * coreSize, 60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(100),
        new ThreadPoolExecutor.CallerRunsPolicy());
}
```
被 AbstractCapabilityExecutor 及全部 13 个 CapabilityExecutor 子类的构造器第 16 参数注入。

**metricsAsyncExecutor**：
```java
@Bean("metricsAsyncExecutor")
public Executor metricsAsyncExecutor(AiMetricsAsyncProperties metricsAsyncProperties) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(metricsAsyncProperties.getCorePoolSize());
    executor.setMaxPoolSize(metricsAsyncProperties.getMaxPoolSize());
    executor.setQueueCapacity(metricsAsyncProperties.getQueueCapacity());
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy() {
        @Override public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            log.warn("指标采集任务被丢弃: queueSize={}, activeCount={}",
                e.getQueue().size(), e.getActiveCount());
            super.rejectedExecution(r, e);
        }
    });
    executor.setThreadNamePrefix("metrics-async-");
    executor.initialize();
    return executor;
}
```
被 LoggingMetricsCollector.record() 上的 @Async("metricsAsyncExecutor") 引用。

**transcriptSummaryExecutor**：
```java
@Bean("transcriptSummaryExecutor")
public Executor transcriptSummaryExecutor() {
    int coreSize = Math.min(Runtime.getRuntime().availableProcessors(), 4);
    return new ThreadPoolExecutor(coreSize, 2 * coreSize, 60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(20),
        new ThreadPoolExecutor.DiscardPolicy() {
            @Override public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                log.warn("转录摘要压缩任务被丢弃: queueSize={}, activeCount={}",
                    e.getQueue().size(), e.getActiveCount());
                super.rejectedExecution(r, e);
            }
        });
}
```
被 DiscussionConclusionCapabilityExecutor 的前置 LLM 压缩调用使用。

**scheduledTaskExecutor**：
```java
@Bean("scheduledTaskExecutor")
public ThreadPoolTaskScheduler scheduledTaskExecutor() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(3);
    scheduler.setThreadNamePrefix("scheduled-task-");
    scheduler.setWaitForTasksToCompleteOnShutdown(true);
    scheduler.setAwaitTerminationSeconds(10);
    scheduler.initialize();
    return scheduler;
}
```
底座内多个 @Scheduled 定时任务共享此线程池。

#### 配置属性 @Bean 方法（通过 @ConfigurationProperties 注入，禁止 @Value）：
```java
@Bean("capabilityTimeoutConfig")
public Map<String, Duration> capabilityTimeoutConfig(AiExecutionProperties executionProperties) {
    return executionProperties.getPerCapability();
}

@Bean("thinAdapterPerCapabilityConfig")
public Map<String, Duration> thinAdapterPerCapabilityConfig(AiExecutionProperties executionProperties) {
    return executionProperties.getThinAdapter().getPerCapability();
}

@Bean("parseTimeoutConfig")
public Map<String, Duration> parseTimeoutConfig(AiExecutionProperties executionProperties) {
    return executionProperties.getParse().getPerCapability();
}

@Bean
public Duration parseTimeoutDefault(AiExecutionProperties executionProperties) {
    return executionProperties.getParse().getDefaultTimeout();
}

@Bean("degradationStrategyMapRef")
public AtomicReference<Map<String, List<DegradationStrategy>>> degradationStrategyMapRef() {
    return this.strategyMapRef; // 由 @PostConstruct 初始化
}

@Bean("modelRouteMap")
public Map<String, List<ModelRoute>> modelRouteMap(AiRouterProperties routerProperties) {
    return routerProperties.toModelRouteMap();
}
```

#### @PostConstruct 初始化：
- 从 ApplicationContext 获取全部 DegradationStrategy Bean（`context.getBeansOfType(DegradationStrategy.class)`），按 AiDegradationProperties.strategies 配置的策略白名单构建 `Map<String, List<DegradationStrategy>>`，封装为 `AtomicReference` 并通过 `@Bean("degradationStrategyMapRef")` 暴露
- 配置校验 1（超时层级）：对所有薄适配器能力逐一验证 `per-capability >= thin-adapter.per-capability + 5s`。校验不通过抛出 `IllegalStateException`
- 配置校验 2（解析超时上限）：`parseTimeout <= chatFallbackTimeout`，其中 `chatFallbackTimeout` = `capabilityTimeoutConfig.get(capabilityId)`。校验不通过抛出 `IllegalStateException`

#### @Scheduled 定时刷新（fixedDelay=60000，使用 scheduledTaskExecutor）：
- `refreshDegradationStrategies()` — 从 AiDegradationProperties 重新读取策略白名单，重新构建策略 Map 并通过 AtomicReference 原子替换
- `refreshCapabilityTimeoutConfig()` — 通过 Environment 重新读取配置值并更新 capabilityTimeoutConfig Map Bean 缓存
- `refreshWindowSeconds()` — 读取 AiSlidingWindowProperties.windowSeconds 并通过 AtomicLong 发布到 SlidingWindowMetricsStore

### 2. AiPlatformEnvironmentPostProcessor
**文件**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiPlatformEnvironmentPostProcessor.java`
**形态**：`class implements EnvironmentPostProcessor`
**职责**：Spring 启动早期将 `ai.platform.enabled` → `ai.mock.enabled` 反向转发，两开关互斥。

```java
public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
    String platformEnabled = env.getProperty("ai.platform.enabled");
    if (platformEnabled != null && env.getProperty("ai.mock.enabled") == null) {
        boolean mockDisabled = !Boolean.parseBoolean(platformEnabled);
        env.getPropertySources().addLast(
            new MapPropertySource("aiPlatformForwarding",
                Collections.singletonMap("ai.mock.enabled", String.valueOf(mockDisabled))));
    }
}
```

### 3. META-INF/spring.factories
**文件**：`ai-impl/src/main/resources/META-INF/spring.factories`
**内容**：
```
org.springframework.boot.env.EnvironmentPostProcessor=\
com.aimedical.modules.ai.impl.config.AiPlatformEnvironmentPostProcessor
```

### 4. @ConfigurationProperties 属性类（7 个新建）

所有属性类位于 `ai-impl/config/` 包，均标注 `@ConfigurationProperties` 并提供 getter/setter：

| 类名 | 前缀 | 关键字段 |
|------|------|---------|
| AiExecutionProperties | `ai.execution` | `timeout.perCapability: Map<String, Duration>`, `timeout.thinAdapter: ThinAdapterConfig`(内嵌), `timeout.parse: ParseConfig`(内嵌) |
| AiDegradationProperties | `ai.degradation` | `strategies: Map<String, List<String>>`, `contextTtlSeconds: int`, `circuitBreaker: CircuitBreakerConfig`(内嵌) |
| AiRateLimitingProperties | `ai.rate-limiting` | `enabled: boolean`, `endpoints: Map<String, EndpointRateLimitConfig>`(内嵌) |
| AiMetricsAsyncProperties | `ai.metrics.async` | `corePoolSize: int`=1, `maxPoolSize: int`=2, `queueCapacity: int`=1000 |
| AiPlatformProperties | `ai.platform` | `enabled: boolean`=false |
| AiSlidingWindowProperties | `ai.sliding-window` | `windowSeconds: int`=60, `maxEventsPerCapability: int`=10000 |
| AiTemplateProperties | `ai.template.fallback` | `capabilityFallback: Map<String, String>` |

#### AiExecutionProperties 内嵌结构：
```java
@ConfigurationProperties(prefix = "ai.execution")
public class AiExecutionProperties {
    private Map<String, Duration> perCapability = new HashMap<>();
    private ThinAdapterConfig thinAdapter = new ThinAdapterConfig();
    private ParseConfig parse = new ParseConfig();
    // getters/setters

    public static class ThinAdapterConfig {
        private Map<String, Duration> perCapability = new HashMap<>();
        private Duration defaultTimeout = Duration.ofSeconds(30);
        // getters/setters
    }

    public static class ParseConfig {
        private Map<String, Duration> perCapability = new HashMap<>();
        private Duration defaultTimeout = Duration.ofSeconds(5);
        // getters/setters
    }
}
```

### 5. ModelRouteConfig（解决 AiRouterProperties YAML 绑定限制）

**文件**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/ModelRouteConfig.java`
**形态**：可变 POJO（含无参构造器 + setters），与 ModelRoute 字段对等但全部字段可变，用于 Spring Boot @ConfigurationProperties setter 绑定。
**字段**：`endpointId`(String)、`clientType`(String)、`authType`(String)、`modelId`(String)、`endpointUrl`(String)、`weight`(int)、`timeoutMs`(long)、`parameters`(Map<String, Object>)

#### AiRouterProperties 修改：
```java
@ConfigurationProperties(prefix = "ai.router")
public class AiRouterProperties {
    private Map<String, List<ModelRouteConfig>> routes = new HashMap<>();
    // getters/setters

    public Map<String, List<ModelRoute>> toModelRouteMap() {
        // 转换 ModelRouteConfig → ModelRoute
        // clientType: String → ClientType.valueOf(upperCase)
        // authType: String → AuthType.valueOf(upperCase)
    }
}
```

#### DefaultModelRouter 修改：
`refreshRouteTable()` 中 `routerProperties.getRoutes()` 返回类型变为 `Map<String, List<ModelRouteConfig>>`，需先调用 `routerProperties.toModelRouteMap()` 获取 `Map<String, List<ModelRoute>>`。

### 6. DegradationStrategy @Component 注册
- `TimeoutDegradationStrategy.java`：类签名添加 `@Component("timeout")`
- `CircuitBreakerDegradationStrategy.java`：类签名添加 `@Component("circuit-breaker")`

确保 `AiPlatformConfig.@PostConstruct` 中 `context.getBeansOfType(DegradationStrategy.class)` 能获取全部三个策略 Bean（timeout / circuit-breaker / noop）。

### 7. 删除 AiClientConfig.java
`ai-impl/client/AiClientConfig.java` 被 AiPlatformConfig 完全吸收后删除。其 `@EnableAsync` 注解已移至 AiPlatformConfig。

## 涉及文件清单

| 操作 | 文件路径 |
|------|---------|
| 新建 | ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiPlatformConfig.java |
| 新建 | ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiPlatformEnvironmentPostProcessor.java |
| 新建 | ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiExecutionProperties.java |
| 新建 | ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiDegradationProperties.java |
| 新建 | ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiRateLimitingProperties.java |
| 新建 | ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiMetricsAsyncProperties.java |
| 新建 | ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiPlatformProperties.java |
| 新建 | ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiSlidingWindowProperties.java |
| 新建 | ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiTemplateProperties.java |
| 新建 | ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/ModelRouteConfig.java |
| 新建 | ai-impl/src/main/resources/META-INF/spring.factories |
| 修改 | ai-impl/src/main/java/com/aimedical/modules/ai/impl/degradation/TimeoutDegradationStrategy.java（添加 @Component("timeout")） |
| 修改 | ai-impl/src/main/java/com/aimedical/modules/ai/impl/degradation/CircuitBreakerDegradationStrategy.java（添加 @Component("circuit-breaker")） |
| 修改 | ai-impl/src/main/java/com/aimedical/modules/ai/impl/router/AiRouterProperties.java（routes 字段类型改为 Map<String, List<ModelRouteConfig>>，新增 toModelRouteMap()） |
| 修改 | ai-impl/src/main/java/com/aimedical/modules/ai/impl/router/DefaultModelRouter.java（refreshRouteTable() 适配新类型） |
| 删除 | ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/AiClientConfig.java（被 AiPlatformConfig 吸收） |
| 新建 | ai-impl/src/test/java/com/aimedical/modules/ai/impl/config/AiPlatformConfigTest.java |
| 新建 | ai-impl/src/test/java/com/aimedical/modules/ai/impl/config/AiPlatformEnvironmentPostProcessorTest.java |

## 选择理由
Batch6 P3 首项。AiPlatformConfig 是底座统一配置装配类，负责 Bean 装配、@ConfigurationProperties 绑定、线程池管理、配置校验、定时刷新。AiPlatformEnvironmentPostProcessor 在 Spring 启动早期完成属性互斥转发。7 个 @ConfigurationProperties 类按照设计文档 §3.9 的拆分方案（AiRouterProperties/AiExecutionProperties/AiDegradationProperties/AiRateLimitingProperties/AiMetricsAsyncProperties/AiPlatformProperties/AiSlidingWindowProperties/AiTemplateProperties）实现，消除单一巨型配置类的反模式。

## 任务上下文
### 需求/设计约束（从 06_ood_phase5_G.md 摘录）
- §3.9：AiPlatformConfig 使用 class，标注 @Configuration + @EnableConfigurationProperties + implements ApplicationContextAware；不实现 EnvironmentPostProcessor
- §3.9：AiPlatformEnvironmentPostProcessor 通过 META-INF/spring.factories 注册，不标注任何 Spring 注解
- §3.9：降级策略 Map 构建——@PostConstruct 通过 context.getBeansOfType(DegradationStrategy.class) 获取策略 Bean，按 YAML 配置白名单构建 Map。需确保所有 DegradationStrategy 实现已标注 @Component
- §3.9：4 个线程池 @Bean 定义（llmCallExecutor/metricsAsyncExecutor/transcriptSummaryExecutor/scheduledTaskExecutor）
- §3.9：@EnableJpaRepositories + @EntityScan(basePackages = "com.aimedical.modules.ai.impl") 确保 JPA Repository 和 Entity 被扫描
- §3.9：禁止 @Value 注入 Map 类型，改为 @ConfigurationProperties 注入
- §3.9：启动期配置校验——per-capability >= thin-adapter + 5s；parseTimeout <= chatFallbackTimeout

### 已有代码上下文
- **AiClientConfig.java**（将被删除）：现有临时 @Configuration + @EnableAsync，包含 5 个 @Bean 定义，位于 ai-impl/client/ 包
- **AiRouterProperties.java**（需修改）：现有 @ConfigurationProperties(prefix = "ai.router") 存根，routes 字段类型为 `Map<String, List<ModelRoute>>`（不可绑定），需改为 `Map<String, List<ModelRouteConfig>>`
- **DefaultModelRouter.java**（需修改）：现有 @Service，在 refreshRouteTable() 中调用 routerProperties.getRoutes()，需适配新类型
- **各 DegradationStrategy 实现**：NoOpDegradationStrategy 已有 @Component；TimeoutDegradationStrategy 和 CircuitBreakerDegradationStrategy 无 @Component（需添加）
- **各 CapabilityExecutor 实现**：构造器注入 `AtomicReference<Map<String, List<DegradationStrategy>>>`、`Executor llmCallExecutor`、`Map<String, Duration> capabilityTimeoutConfig` 等，通过 AiPlatformConfig @Bean 方法提供
- **AbstractCapabilityExecutor**：构造器第 16 参数 `Executor llmCallExecutor`，第 2 参数 `Map<String, Duration> capabilityTimeoutConfig` 等

## 测试规划

### AiPlatformConfigTest
- `shouldBuildDegradationStrategyMapOnPostConstruct`：Mock ApplicationContext 返回 3 个 DegradationStrategy Bean，验证 @PostConstruct 后 strategyMapRef 包含正确条目
- `shouldValidateTimeoutHierarchyOnPostConstruct`：配置 per-capability < thin-adapter + 5s 时验证抛出 IllegalStateException
- `shouldValidateParseTimeoutOnPostConstruct`：配置 parseTimeout > chatFallbackTimeout 时验证抛出 IllegalStateException
- `shouldRefreshCapabilityTimeoutConfigOnScheduledRefresh`：模拟 Environment 配置变更，调用 refreshCapabilityTimeoutConfig() 后验证 Map 值更新
- `shouldProvideAllThreadPoolBeans`：验证 4 个线程池 Bean 的正确创建和配置参数
- `shouldAbsorbAiClientConfigBeans`：验证 AiPlatformConfig 的 5 个 @Bean 方法（4 LLM + 1 Delegating）与原有 AiClientConfig 行为一致

### AiPlatformEnvironmentPostProcessorTest
- `shouldForwardPlatformEnabledTrueToMockDisabled`：ai.platform.enabled=true → ai.mock.enabled=false
- `shouldForwardPlatformEnabledFalseToMockEnabled`：ai.platform.enabled=false → ai.mock.enabled=true
- `shouldNotOverrideExistingMockEnabled`：ai.mock.enabled 已存在时不覆盖
- `shouldNotForwardWhenPlatformEnabledNull`：ai.platform.enabled 未设置时不做任何操作

### 依赖计数
无新增 Maven 依赖。AiImplPomCleanDependencyTest.totalDependenciesCountShouldBeEleven 无需修改（当前 = 11，维持不变）。

## 修订说明（v24 r1）

| 审查意见 | 修改措施 |
|---------|---------|
| [严重] DegradationStrategy Bean 未注册为 Spring Bean，getBeansOfType 获取不完整 | TimeoutDegradationStrategy 添加 @Component("timeout")，CircuitBreakerDegradationStrategy 添加 @Component("circuit-breaker")；列入涉及文件清单 |
| [严重] @Value("${...}") 无法注入 Map<String, Duration> | 废弃 @Value 注入方案。改为完整的 @ConfigurationProperties 体系：新建 7 个属性类，AiPlatformConfig 标注 @EnableConfigurationProperties 逐个引入；@Bean 方法注入属性类后调用 getter 提取 Map |
| [严重] 缺失 7 个 @ConfigurationProperties 属性类 | 新建 AiExecutionProperties、AiDegradationProperties、AiRateLimitingProperties、AiMetricsAsyncProperties、AiPlatformProperties、AiSlidingWindowProperties、AiTemplateProperties |
| [严重] 缺失 4 个线程池 @Bean 定义 | 新增 llmCallExecutor（ThreadPoolExecutor + CallerRunsPolicy）、metricsAsyncExecutor（ThreadPoolTaskExecutor + DiscardPolicy，注入 AiMetricsAsyncProperties）、transcriptSummaryExecutor（ThreadPoolExecutor + DiscardPolicy）、scheduledTaskExecutor（ThreadPoolTaskScheduler poolSize=3） |
| [严重] 缺失 @EnableJpaRepositories 和 @EntityScan | AiPlatformConfig 类形态补充 @EnableJpaRepositories + @EntityScan(basePackages = "com.aimedical.modules.ai.impl") |
| [一般] @Value 在 @Bean 方法上语法不正确 | 已废弃 @Value 方案，全部改为 @ConfigurationProperties 注入，不涉及 @Value 语法问题 |
| [一般] chatFallbackTimeout 未定义 | 明确 chatFallbackTimeout = capabilityTimeoutConfig.get(capabilityId)，在任务描述和上下文说明中清晰定义 |
| [一般] 缺失测试规划 | 新增 AiPlatformConfigTest（6 测试方法）和 AiPlatformEnvironmentPostProcessorTest（4 测试方法）规划 |
| [轻微] AiRouterProperties @TODO 未处理 | 新建 ModelRouteConfig 可变 POJO；AiRouterProperties.routes 类型改为 Map<String, List<ModelRouteConfig>>；新增 toModelRouteMap() 转换方法；DefaultModelRouter.refreshRouteTable() 适配新类型；删除 @TODO 注释 |
