# R2: ai-impl/client/ + ai-impl/router/ + ai-impl/config/（LLM调用层 + 模型路由 + 配置装配）

审查时间：2026-07-02

### 审查范围

```
ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/
  LlmChatService.java
  LlmChatStreamService.java
  DelegatingLlmChatService.java
  HttpApiLlmChatService.java
  HttpApiLlmChatStreamService.java
  SpringAiLlmChatService.java
  SpringAiLlmChatStreamService.java
  LlmChatRequest.java
  LlmChatMessage.java
  LlmChatMessageRole.java
  LlmChatOptions.java
  LlmChatResponse.java
  StructuredChatResult.java
  ChatToolDefinition.java
  ClientType.java
  AuthType.java
  CredentialProvider.java
  DefaultCredentialProvider.java
  EndpointRateLimiter.java
  exception/StructuredOutputNotSupportedException.java
  exception/LlmInfrastructureException.java
  exception/CredentialUnavailableException.java
  exception/AiAbilityInputInvalidException.java
ai-impl/src/main/java/com/aimedical/modules/ai/impl/router/
  ModelRouter.java
  DefaultModelRouter.java
  ModelRoute.java
  AiRouterProperties.java
ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/
  AiPlatformConfig.java
  AiPlatformEnvironmentPostProcessor.java
  AiPlatformProperties.java
  AiDegradationProperties.java
  AiExecutionProperties.java
  AiMetricsAsyncProperties.java
  AiRateLimitingProperties.java
  AiSlidingWindowProperties.java
  AiTemplateProperties.java
  ModelRouteConfig.java
ai-impl/src/main/java/com/aimedical/modules/ai/impl/fallback/
  FallbackAiService.java
```

### 发现

#### [严重] DelegatingLlmChatService 缺少 @PostConstruct 枚举值完整性校验

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/DelegatingLlmChatService.java:12-55`
- **描述**：设计文档 §1.3 `ClientType` 条目和 §3.2 `DelegatingLlmChatService` 明确要求"在 `@PostConstruct` 阶段检查 `delegates` Map 中是否所有已注册的 `ClientType` 枚举值均有对应实现，若某种实现缺失（如 YAML 配置了 `SPRING_AI` 但 Spring AI 依赖未引入），则在启动期发出 ERROR 日志"。当前 `DelegatingLlmChatService` 仅有构造器，无 `@PostConstruct` 校验逻辑。缺失枚举值实现仅在运行时 `chat()`/`structuredChat()` 被调用时才以 ERROR 日志回退到 HTTP_API，不符合设计文档"启动期 ERROR 日志 + 推送告警事件到健康检查体系"的防护要求。
- **建议**：添加 `@PostConstruct` 方法，遍历 `ClientType.values()`，对 `delegates` 中缺失的枚举值输出 ERROR 日志，并（若已引入健康检查体系）推送告警事件。

#### [严重] DelegatingLlmChatService 回退日志级别应为 ERROR 但仅匹配部分设计要求

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/DelegatingLlmChatService.java:27-28, 42-43`
- **描述**：设计文档 §1.3 `ClientType` 条目要求"回退到默认实现（HttpApiLlmChatService）时，回退的日志级别提升为 **ERROR**（而非 WARN）并通过健康检查端点暴露告警状态"。当前实现确实使用了 ERROR 级别（符合），但日志消息中未携带足够的上下文信息（如缺失的 clientType 值和当前 endpointId），也未通过健康检查端点暴露告警状态。
- **建议**：(1) 日志消息增加 `clientType` 值和可用的 `endpointId` 上下文；(2) 若健康检查体系已可用，调用 `ModelEndpointHealthManager` 或自定义 HealthIndicator 暴露配置错误告警。

#### [严重] AiPlatformConfig 缺少 @ConditionalOnProperty 条件注解

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiPlatformConfig.java:51-66`
- **描述**：设计文档 §3.1 Bean 装配策略要求 `AiOrchestrator` 标注 `@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")`，且 `AiPlatformConfig` 作为底座 Bean 装配的核心 `@Configuration` 类应仅在 `ai.platform.enabled=true` 时激活。当前 `AiPlatformConfig` 无 `@ConditionalOnProperty` 注解，意味着即使底座关闭（`ai.platform.enabled=false`），所有 Bean（线程池、策略 Map、DelegatingLlmChatService 等）仍会被装配，造成不必要的资源占用和潜在的 Bean 冲突。虽然 `AiOrchestrator` 和各 `CapabilityExecutor` 有自己的 `@ConditionalOnProperty`，但 `AiPlatformConfig` 中定义的 `HttpApiLlmChatService`、`SpringAiLlmChatService`、`DelegatingLlmChatService`、线程池等 Bean 在底座关闭时仍会被创建。
- **建议**：在 `AiPlatformConfig` 类上添加 `@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")`，确保底座关闭时整个配置类不生效。

#### [严重] AiPlatformConfig delegatingLlmChatService Bean 装配未按设计使用 ObjectProvider 延迟解析

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiPlatformConfig.java:127-136`
- **描述**：设计文档 §3.2 `DelegatingLlmChatService` Bean 装配方式明确要求 `springAiLlmChatService` 标注 `@ConditionalOnClass(name = "org.springframework.ai.chat.model.ChatModel")`，且 `delegatingLlmChatService` 通过 `ObjectProvider<LlmChatService>` 注入 Spring AI 实现，使用 `getIfAvailable()` 避免强制注入导致的 `NoSuchBeanDefinitionException`。当前实现直接通过参数注入 `SpringAiLlmChatService`，当项目未引入 Spring AI 依赖时，`SpringAiLlmChatService` Bean 不存在（但当前 `springAiLlmChatService()` 方法未标注 `@ConditionalOnClass`，总是创建 Bean），将导致：(1) 无 Spring AI 环境下 `SpringAiLlmChatService` 仍被创建但方法调用均抛 `UnsupportedOperationException`；(2) delegates Map 中 `SPRING_AI` 分支指向一个不可用的实现，运行时 `clientType=SPRING_AI` 的请求不会触发回退逻辑（因为 delegate 非 null），而是直接抛出 `UnsupportedOperationException`。
- **建议**：(1) `springAiLlmChatService()` Bean 方法添加 `@ConditionalOnClass(name = "org.springframework.ai.chat.model.ChatModel")`；(2) `delegatingLlmChatService()` 方法改用 `ObjectProvider<SpringAiLlmChatService>` 注入，`getIfAvailable()` 返回 null 时仅注册 `HTTP_API` 分发项。

#### [严重] AiPlatformEnvironmentPostProcessor 使用 addLast 导致转发优先级过低

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiPlatformEnvironmentPostProcessor.java:17`
- **描述**：`EnvironmentPostProcessor` 将 `ai.mock.enabled` 转发值通过 `env.getPropertySources().addLast()` 插入。`addLast` 使该 PropertySource 优先级最低——如果 YAML 配置文件中已存在 `ai.mock.enabled` 属性，转发值将被覆盖（设计文档明确说明"仅当 PropertySource 中不存在 `ai.mock.enabled` 时才生效"，此行为符合设计意图）。但问题在于：如果用户在 YAML 中显式设置了 `ai.mock.enabled=true` 同时 `ai.platform.enabled=true`，两者同时激活，将导致 `AiOrchestrator` 和 `MockAiService` 两个 `AiService` 实现同时注册，`FallbackAiService` 的 `ObjectProvider.getIfUnique()` 将返回 null（非唯一），所有 13 个 AI 能力返回降级结果。当前代码在 `ai.mock.enabled` 已存在时不做转发（第 15 行 `env.getProperty("ai.mock.enabled") == null` 检查），这是正确的。但无告警日志——运维无法感知配置冲突。
- **建议**：当 `ai.mock.enabled` 属性已存在且与转发预期值不同时，输出 WARN 日志提示配置冲突。

#### [严重] HttpApiLlmChatService.chat() 同步阻塞调用却返回 CompletableFuture

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/HttpApiLlmChatService.java:34-73`
- **描述**：`chat()` 方法签名返回 `CompletableFuture<AiResult<LlmChatResponse>>`，但实现中 `HttpClient.send()` 为同步阻塞调用（第 64 行），然后通过 `CompletableFuture.completedFuture()` 包装返回。这意味着：(1) 在 `CapabilityExecutor` 的 `supplyAsync()` 提交到 `llmCallExecutor` 线程池之前，调用方可能误以为此方法是非阻塞的；(2) 更关键的是，`HttpClient.newHttpClient()` 在每次调用时创建新实例（第 52 行），无法复用连接池，每次请求都建立新 TCP 连接，性能开销大且无法利用 HTTP/2 多路复用。
- **建议**：(1) 将 `HttpClient` 实例提升为类字段，在构造器中创建并复用（`HttpClient` 线程安全，设计文档 §3.2 也要求"HTTP 客户端基于连接池实现"）；(2) 若需真正异步，改用 `HttpClient.sendAsync()` 返回的 `CompletableFuture` 直接链式转换。

#### [严重] HttpApiLlmChatService.chat() 中 endpointId 被误用作 URI

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/HttpApiLlmChatService.java:56-57`
- **描述**：第 56-57 行 `URI.create(endpointId)` 将 `endpointId` 直接作为 HTTP 请求的 URI。但 `endpointId` 在设计文档 §3.2 中明确定义为"端点唯一标识，用于 ModelEndpointHealthManager 健康状态索引和 Vault 密钥查询"，并非端点 URL。`ModelRoute` 同时拥有 `endpointId` 和 `endpointUrl` 两个字段，HTTP 请求应使用 `endpointUrl` 而非 `endpointId`。当前 `LlmChatRequest` 中有 `endpointId` 字段但无 `endpointUrl` 字段，说明请求构建时未将完整的端点 URL 传入。
- **建议**：(1) 在 `LlmChatRequest` 中新增 `endpointUrl` 字段；(2) `HttpApiLlmChatService.chat()` 使用 `request.getEndpointUrl()` 构建 `URI`，`endpointId` 仅用于限流和凭据查询。

#### [一般] DelegatingLlmChatService.getClientType() 返回 null

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/DelegatingLlmChatService.java:52-54`
- **描述**：`LlmChatService` 接口定义了 `getClientType()` 方法，`HttpApiLlmChatService` 返回 `ClientType.HTTP_API`，`SpringAiLlmChatService` 返回 `ClientType.SPRING_AI`。`DelegatingLlmChatService` 作为分发层返回 null 语义不清——分发层本身不属于任何单一 ClientType。但 `LlmChatService` 接口未在 Javadoc 中说明 `getClientType()` 返回 null 的契约含义，调用方可能误用。
- **建议**：在 `LlmChatService.getClientType()` 的 Javadoc 中明确说明分发层实现返回 null 的语义，或考虑在接口中添加 `@Nullable` 注解。

#### [一般] EndpointRateLimiter 未使用设计文档要求的 maxBurstSeconds 配置

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/EndpointRateLimiter.java:17-18, 39-45`
- **描述**：类声明了 `defaultMaxBurstSeconds` 字段（第 18 行），但 `computeIfAbsent` 中创建 `RateLimiter` 时仅使用 `permitsPerSecond`（第 44 行 `RateLimiter.create(permitsPerSecond)`），未调用 `RateLimiter.create(permitsPerSecond, warmupPeriod, TimeUnit.SECONDS)` 使用预热线配置。Guava `RateLimiter` 的 `create(double, long, TimeUnit)` 重载支持预热期（warmup），当前字段声明但未使用属死代码。
- **建议**：使用 `RateLimiter.create(permitsPerSecond, warmupPeriodSeconds, TimeUnit.SECONDS)` 替换当前调用，或移除 `defaultMaxBurstSeconds` 字段。

#### [一般] EndpointRateLimiter 限流拒绝时返回 false 但调用方未统一处理

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/HttpApiLlmChatService.java:46-49`
- **描述**：设计文档 §3.2 限流保护要求"限流超时不重试，直接降级"，降级原因为 `DegradationReason.INFRASTRUCTURE_ERROR + ":RateLimitExceeded"`。当前 `HttpApiLlmChatService.chat()` 在 `tryAcquire` 返回 false 时，返回 `AiResult.failure("RATE_LIMITED", "请求被限流")`——这绕过了 `CapabilityExecutor` 的降级管线，不会记录 `AiCallRecord`、不会触发 `SlidingWindowMetricsStore.recordFailure()`，导致限流事件在指标系统中不可见。
- **建议**：限流拒绝时应抛出 `LlmInfrastructureException("Rate limit exceeded for endpoint: " + endpointId)`，由 `CapabilityExecutor` 的异常处理管线统一走降级路径，确保指标采集和降级记录完整。

#### [一般] SpringAiLlmChatService 和 SpringAiLlmChatStreamService 抛出 UnsupportedOperationException

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/SpringAiLlmChatService.java:18-19, 23-24`；`SpringAiLlmChatStreamService.java:12`
- **描述**：两个 Spring AI 实现类的所有方法均直接抛出 `UnsupportedOperationException`，这在项目未引入 Spring AI 依赖时不应该被实例化（见本报告"严重-4"关于 `@ConditionalOnClass` 缺失的发现）。即使添加了条件注解，当前 `UnsupportedOperationException` 也不属于设计文档定义的异常分类（`StructuredOutputNotSupportedException` 或 `LlmInfrastructureException`），`CapabilityExecutor` 的异常处理管线无法正确分类此异常——它将被归入"其余基础设施异常"而非"Spring AI 不可用"的明确语义。
- **建议**：若 Spring AI 不可用但方法被意外调用，应抛出 `LlmInfrastructureException("Spring AI ChatModel not available")` 而非 `UnsupportedOperationException`。

#### [一般] LlmChatStreamService 接口导入未使用的异常类

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/LlmChatStreamService.java:3`
- **描述**：`LlmChatStreamService` 接口导入了 `AiAbilityInputInvalidException`（第 3 行），但接口方法签名中未引用此异常。设计文档 §3.2 提到"违反约束时返回 `Flux.error(AiAbilityInputInvalidException)`"，但当前接口方法签名未声明 `throws`，导入为未使用。
- **建议**：移除未使用的导入，或在 Javadoc 中声明 `@throws AiAbilityInputInvalidException` 文档化此行为。

#### [一般] ModelRouter.route() 签名与设计文档不一致

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/router/ModelRouter.java:4`
- **描述**：设计文档 §2.3 类图和 §3.2 定义 `ModelRouter.route(String capabilityId, ExperimentAssignment assignment)`，第二个参数为 `ExperimentAssignment`。当前实现为 `route(String capabilityId, Object request)`，第二个参数为 `Object request`，丢失了类型安全，且未利用实验分组信息进行路由决策。`DefaultModelRouter.route()` 实现中也未使用第二个参数。
- **建议**：将第二个参数改为 `ExperimentAssignment assignment`（可 `@Nullable`），与设计文档保持一致；`DefaultModelRouter` 实现中考虑基于实验分组的模型覆盖逻辑。

#### [一般] DefaultModelRouter.refreshRouteTable() @Scheduled 未指定 scheduler

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/router/DefaultModelRouter.java:34`
- **描述**：`@Scheduled(fixedDelay = 60000)` 未指定 `scheduler` 属性，将使用 Spring 默认的单线程调度器。而 `AiPlatformConfig` 中定义了 `scheduledTaskExecutor` Bean（3 线程）用于其他定时任务。设计文档 §3.9 热加载机制要求路由表定时刷新，若默认调度器被其他长耗时任务阻塞，路由表刷新将延迟。
- **建议**：添加 `scheduler = "scheduledTaskExecutor"` 指定调度器，与 `AiPlatformConfig` 中其他 `@Scheduled` 方法一致。

#### [一般] AiPlatformConfig.refreshCapabilityTimeoutConfig() 使用 Binder 重新绑定与 @ConfigurationProperties 状态不一致

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiPlatformConfig.java:283-294`
- **描述**：定时刷新使用 `Binder.get(env).bind("ai.execution", ...)` 从 Environment 重新绑定配置，但 `@EnableConfigurationProperties` 绑定的 `AiExecutionProperties` Bean 的状态不会同步更新。这导致同一个 JVM 中存在两套配置视图：`@Autowired AiExecutionProperties` 仍是启动时的旧值，而 `AtomicReference` 中是最新值。如果新增代码意外通过 `AiExecutionProperties` Bean 读取配置，将获取到过期数据。
- **建议**：在 `refreshCapabilityTimeoutConfig()` 的 Javadoc 中明确警告"此刷新仅更新 AtomicReference 引用，不更新 @ConfigurationProperties Bean"，或在方法中同步更新 `AiExecutionProperties` Bean 的字段值。

#### [一般] AiPlatformConfig.refreshWindowSeconds() 定时刷新与 SlidingWindowMetricsStore 的耦合方式

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiPlatformConfig.java:297-310`
- **描述**：设计文档 §3.9 要求 `sliding-window.window-seconds` 热加载使用 `AtomicLong`，但当前实现通过 `applicationContext.getBean(SlidingWindowMetricsStore.class).setWindowSeconds()` 直接调用 setter。若 `SlidingWindowMetricsStore` 的 `setWindowSeconds()` 未正确处理并发（如正在淘汰窗口事件时修改窗口宽度），可能导致滑动窗口数据不一致。
- **建议**：确认 `SlidingWindowMetricsStore.setWindowSeconds()` 的实现是否线程安全（应使用 `AtomicLong` 或 `volatile` + 全量替换策略）。

#### [一般] FallbackAiService 构造器中 ObjectProvider.getIfUnique() 在多 AiService Bean 时静默返回 null

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/fallback/FallbackAiService.java:49-55`
- **描述**：设计文档 §3.1 Bean 装配策略要求 `@ConditionalOnProperty` 保证同时只有一个非装饰器 `AiService` 实现有效。但 `FallbackAiService` 构造器中 `delegateProvider.getIfUnique()` 在存在多个候选 Bean 时返回 null，且 `aiPlatformEnabled` 参数注入但未使用（第 50 行），构造器未根据 `aiPlatformEnabled` 值做任何逻辑判断。当配置错误导致多个 `AiService` 同时注册时，`delegate` 为 null，所有 13 个方法均返回 `AiResult.degraded()`，但无启动期 fail-fast 告警。
- **建议**：(1) 移除未使用的 `aiPlatformEnabled` 参数，或用于条件化日志输出；(2) 在 `delegate == null` 时根据 `aiPlatformEnabled` 值输出不同级别的日志——`true` 时 ERROR（底座启用但无可用 delegate=配置错误），`false` 时 INFO（底座关闭，期望行为）。

#### [一般] ChatToolDefinition 部分可变：strict 字段有 setter 破坏不可变性

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/ChatToolDefinition.java:11, 31`
- **描述**：`ChatToolDefinition` 的 `name`、`description`、`parameters` 为 `final`（不可变），但 `strict` 字段为非 final 且有 `setStrict()` setter（第 11 行 `private boolean strict = true`，第 31 行 setter），破坏了值对象的不可变性契约。设计文档 §1.3 定义 `ChatToolDefinition` 为"Tool 定义值对象"，值对象应为不可变。
- **建议**：将 `strict` 改为 `final` 字段，通过构造器参数赋值，移除 `setStrict()` setter。

#### [一般] LlmChatOptions 全部字段可变，不符合设计文档值对象定义

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/LlmChatOptions.java:5-49`
- **描述**：设计文档 §1.3 定义 `LlmChatOptions` 为"对话参数配置值对象"，值对象应为不可变。当前所有字段均有 setter 方法，且无 `final` 修饰。虽然设计文档 §3.2 提到"两阶段填充"需要 setter（阶段一从 `ModelRoute.parameters` 映射，阶段二由 `CapabilityExecutor` 覆盖），但可考虑使用 Builder 模式替代 setter 保持不可变性。
- **建议**：考虑使用 Builder 模式：阶段一构造 Builder 设置基值，阶段二在 Builder 上覆盖后 `build()` 生成不可变实例。若保留当前可变设计，应在 Javadoc 中明确说明"此值对象在两阶段填充期间可变，填充完成后不应再修改"。

#### [一般] AiRouterProperties.convert() 枚举转换失败时静默回退

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/router/AiRouterProperties.java:44-49`
- **描述**：`ClientType.valueOf()` 抛出 `IllegalArgumentException` 时，静默回退到 `ClientType.HTTP_API`（第 48-49 行）。设计文档 §1.3 `ClientType` 条目要求"YAML 配置中 `client` 字符串值绑定到 ClientType 枚举，未匹配值时抛出 `ConversionFailedException`"。当前实现吞掉了配置错误，运维无法感知 YAML 中拼写错误的 clientType 值。`AuthType` 同理（第 53-57 行回退到 `NONE`）。
- **建议**：枚举转换失败时应输出 WARN 日志记录原始值和回退目标，或在 `@PostConstruct` 阶段校验路由配置的完整性。

#### [一般] DefaultCredentialProvider.getCredential() 在 NORMAL 状态下 credentialStore 命中后重置 consecutiveFailures 为 0

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/DefaultCredentialProvider.java:90-95`
- **描述**：第 93 行 `consecutiveFailures.set(0)` 在 `credentialStore.get(endpointId)` 命中时重置连续失败计数器。但 `credentialStore` 是本地内存 Map（`registerCredential()` 写入），并非 Vault 查询。从 `credentialStore` 命中不应视为"Vault 查询成功"，因为真正的 Vault 查询失败场景下 `credentialStore` 中可能仍有旧数据。此逻辑可能导致 Vault 连续不可达的计数被意外重置，使状态机无法正确进入 `CACHE_ONLY` / `BACKOFF` 状态。
- **建议**：区分 `credentialStore` 命中与 Vault 查询成功，仅在真正 Vault 查询成功时重置 `consecutiveFailures`。

#### [一般] HttpApiLlmChatService.structuredChat() 直接抛出 StructuredOutputNotSupportedException

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/HttpApiLlmChatService.java:76-79`
- **描述**：`structuredChat()` 同步抛出 `StructuredOutputNotSupportedException`（非 `CompletableFuture.failedFuture`），意味着此异常将在 `CapabilityExecutor.supplyAsync()` lambda 中抛出，被包装为 `ExecutionException` 后由 `CompletableFuture.exceptionally()` 捕获。但设计文档 §3.2 异常传播契约说"仅在 `structuredChat()` 路径中的 `StructuredOutputNotSupportedException` 和 `LlmInfrastructureException` 以异常形式传播供回调处理"。当前实现符合这一约定，但与 `chat()` 方法的异常处理模式不一致（`chat()` 将异常包装为 `AiResult.failure()` 返回，不抛出）。两种异常模式共存增加了调用方的处理复杂度。
- **建议**：统一异常处理模式——要么 `structuredChat()` 也返回 `CompletableFuture.failedFuture(exception)`，要么在 Javadoc 中明确标注两种方法的异常传播差异。

#### [轻微] LlmChatRequest 空参构造器将所有字段设为 null

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/LlmChatRequest.java:14-20`
- **描述**：`LlmChatRequest` 的空参构造器将 `messages` 设为 null。设计文档 §3.2 声明 `messages` 为"非空，至少包含一条消息"。空参构造器主要为 Jackson 反序列化服务，但允许构造 `messages=null` 的实例与字段契约矛盾。
- **建议**：空参构造器是 Jackson 反序列化所需，可保留但添加 `@SuppressWarnings` 或 Javadoc 说明仅供反序列化使用。考虑在 `chat()` 方法入口处增加 `messages` 非空校验（设计文档 §3.2 要求违反时返回 `AiResult.failure("LLM_AI_INPUT_INVALID")`，当前 `HttpApiLlmChatService` 未实现此校验）。

#### [轻微] LlmChatResponse.LlmChatUsage 未按设计文档定义为独立静态类

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/LlmChatResponse.java:34`
- **描述**：设计文档 §2.1 目录结构中列出 `LlmChatUsage.java` 为独立文件，§1.3 定义为"static class"内嵌于 `LlmChatResponse`。当前实现为内嵌静态类（`LlmChatResponse.LlmChatUsage`），与设计文档 §1.3 一致但与 §2.1 目录结构不一致（§2.1 列出了独立文件）。当前内嵌实现是合理的，无需修改，此处仅标注设计文档 §2.1 目录结构可能需要更新。

#### [轻微] DefaultModelRouter.refreshRouteTable() 中使用全限定类名

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/router/DefaultModelRouter.java:83`
- **描述**：第 83 行 `java.util.HashMap<String, ModelRoute[]> table = new java.util.HashMap<>()` 使用全限定类名，而文件顶部已有 `import java.util.HashMap`（但实际 import 列表中未导入 `HashMap`，仅导入了 `Collections`、`List`、`Map`）。应通过 import 声明替代全限定名。
- **建议**：在 import 区域添加 `import java.util.HashMap`，将第 83 行改为 `HashMap<String, ModelRoute[]> table = new HashMap<>()`。

#### [轻微] AiPlatformConfig.validateConfig() 中 perCapability 遍历重复

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/config/AiPlatformConfig.java:246-273`
- **描述**：`validateConfig()` 方法对 `perCap.entrySet()` 遍历了两次（第 251 行和第 264 行），两次遍历可合并为一次，减少迭代开销。虽然 `perCapability` Map 规模小（最多 13 项），但合并遍历可提升代码可读性。
- **建议**：合并两次遍历为一次，在同一循环内校验 thinAdapter 和 parseTimeout 两项约束。

#### [轻微] FallbackAiService 13 个方法中 delegate null 检查代码重复

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/fallback/FallbackAiService.java:63-164`
- **描述**：13 个方法均包含相同的 `if (delegate == null) { return handleEmptyDelegates(); }` 模式。可使用公共模板方法或动态代理减少重复。
- **建议**：考虑在构造器中确保 `delegate` 非 null（`ai.platform.enabled=true` 时 fail-fast），消除每个方法的 null 检查；或使用 `InvocationHandler` 动态代理统一处理。

#### [轻微] ModelRoute 缺少 connectionTimeout 和 readTimeout 字段

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/router/ModelRoute.java:1-72`
- **描述**：设计文档 §3.2 `ModelRoute` 字段扩展表明确定义了 `connectionTimeout: Duration` 和 `readTimeout: Duration` 字段，当前实现仅有 `timeoutMs: long`，且类型为 `long` 而非设计文档要求的 `Duration`。丢失了连接超时和读取超时的独立配置能力。
- **建议**：将 `timeoutMs: long` 拆分为 `connectionTimeout: Duration` 和 `readTimeout: Duration`，与设计文档保持一致。

#### [轻微] CredentialProvider 接口定义了 Credential 内嵌类但未定义 CredentialProviderState

- **位置**：`ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/CredentialProvider.java:47-51`
- **描述**：`CredentialProviderState` 枚举定义为包私有（`enum CredentialProviderState`，无 `public`），位于 `CredentialProvider.java` 文件末尾。设计文档 §3.2 定义其为 `CredentialProvider` 的内部枚举或独立公开类型，当前包私有可见性使外部模块无法查询 `CredentialProvider` 的状态。
- **建议**：将 `CredentialProviderState` 改为 `public` 枚举，或作为 `CredentialProvider` 接口的内嵌类型 `CredentialProvider.State`。

### 本轮统计

| 严重程度 | 数量 |
|---------|------|
| 严重 | 6 |
| 一般 | 12 |
| 轻微 | 6 |

### 总评

LLM 调用层 + 模型路由 + 配置装配的整体架构设计与设计文档的核心思路一致：`DelegatingLlmChatService` 分发模式、`AiPlatformEnvironmentPostProcessor` 配置转发时序、`EndpointRateLimiter` 令牌桶限流、`AtomicReference` 热加载机制等关键设计点均已落地。

但存在 6 项严重问题需优先修复：(1) `DelegatingLlmChatService` 缺少 `@PostConstruct` 枚举值完整性校验，无法在启动期感知配置错误；(2) `AiPlatformConfig` 缺少 `@ConditionalOnProperty`，底座关闭时仍装配全部 Bean；(3) `springAiLlmChatService` 未标注 `@ConditionalOnClass`，`delegatingLlmChatService` 未使用 `ObjectProvider` 延迟解析，导致无 Spring AI 环境下 `SPRING_AI` 分支指向不可用实现且不触发回退逻辑；(4) `HttpApiLlmChatService` 中 `endpointId` 被误用作 HTTP URI（应使用 `endpointUrl`），且 `HttpClient` 每次调用创建新实例。这 6 项问题组合起来可能导致：运行时 `clientType=SPRING_AI` 的请求不经过回退逻辑直接抛出 `UnsupportedOperationException`，HTTP 调用使用错误的 URI 且无连接池复用。

DTO 值对象层（`LlmChatRequest`、`LlmChatMessage`、`LlmChatResponse`、`StructuredChatResult`）设计合理，核心字段与设计文档 §1.3 一致，不可变性较好（除 `LlmChatOptions` 和 `ChatToolDefinition.strict`）。`DefaultModelRouter` 的权重路由和 `AtomicReference` 全量替换热加载实现正确。`AiPlatformEnvironmentPostProcessor` 的配置转发时序和 `addLast` 优先级策略正确，但缺少配置冲突告警。
