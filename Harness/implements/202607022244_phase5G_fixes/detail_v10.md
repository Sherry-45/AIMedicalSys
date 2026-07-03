# 详细设计（v10）

## 概述

修复 LlmChatRequest Jackson 反序列化冲突（RETRY）+ 6 项配置与环境管理问题（T8/T9/T43/T44/T10/T45），涉及 4 个源文件。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-impl/.../client/LlmChatRequest.java` | 修改 | RETRY：6 参构造器移除 @JsonProperty |
| `ai-impl/.../config/AiPlatformConfig.java` | 修改 | T8(@ConditionalOnProperty)、T9(ObjectProvider + @ConditionalOnClass)、T43(refreshCapabilityTimeoutConfig 简化)、T44(线程安全确认) |
| `ai-impl/.../config/AiPlatformEnvironmentPostProcessor.java` | 修改 | T10(配置冲突告警日志) |
| `ai-impl/.../fallback/FallbackAiService.java` | 修改 | T45(fail-fast + 移除无用参数) |

## 类型定义

### LlmChatRequest（RETRY）

**形态**：class（已有，注解变更）
**包路径**：`com.aimedical.modules.ai.impl.client`

**变更内容**：
- 6 参构造器（当前 32-44 行）所有参数的 `@JsonProperty(...)` 注解移除
- 5 参构造器（当前 24-29 行）保留全部 `@JsonProperty` 注解不变
- 其余字段、getter、无参构造器均不变

**变更前后对照**：
```java
// 变更前（6 参构造器）：
public LlmChatRequest(@JsonProperty("messages") List<LlmChatMessage> messages,
                      @JsonProperty("options") LlmChatOptions options,
                      @JsonProperty("clientType") ClientType clientType,
                      @JsonProperty("tools") List<ChatToolDefinition> tools,
                      @JsonProperty("endpointId") String endpointId,
                      @JsonProperty("endpointUrl") String endpointUrl) {

// 变更后：
public LlmChatRequest(List<LlmChatMessage> messages,
                      LlmChatOptions options,
                      ClientType clientType,
                      List<ChatToolDefinition> tools,
                      String endpointId,
                      String endpointUrl) {
```

**Jackson 反序列化路径**：`ObjectMapper` 读取 JSON 时，唯一可选的构造器为 5 参构造器（带 `@JsonProperty`），6 参构造器无注解不会被 Jackson 考虑。

### AiPlatformConfig（T8/T9/T43/T44）

**形态**：class（已有，新增注解 + 方法体修改 + 参数修改）
**包路径**：`com.aimedical.modules.ai.impl.config`

**T8 — 类级 @ConditionalOnProperty**：
```java
// 在 @Configuration 之后，@EnableConfigurationProperties 之前新增：
@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true", matchIfMissing = false)
```
新增导入：`org.springframework.boot.autoconfigure.condition.ConditionalOnProperty`

**T9 — delegatingLlmChatService 参数改为 ObjectProvider**：
```java
// 变更前：
@Bean
@Primary
DelegatingLlmChatService delegatingLlmChatService(
        HttpApiLlmChatService httpApi,
        SpringAiLlmChatService springAi) {

// 变更后：
@Bean
@Primary
DelegatingLlmChatService delegatingLlmChatService(
        ObjectProvider<HttpApiLlmChatService> httpApiProvider,
        ObjectProvider<SpringAiLlmChatService> springAiProvider) {
    Map<ClientType, LlmChatService> delegates = new HashMap<>();
    HttpApiLlmChatService httpApi = httpApiProvider.getIfAvailable();
    if (httpApi != null) {
        delegates.put(ClientType.HTTP_API, httpApi);
    }
    SpringAiLlmChatService springAi = springAiProvider.getIfAvailable();
    if (springAi != null) {
        delegates.put(ClientType.SPRING_AI, springAi);
    }
    return new DelegatingLlmChatService(delegates);
}
```

**T9 — springAiLlmChatService 添加 @ConditionalOnClass**：
```java
@Bean
@ConditionalOnClass(name = "org.springframework.ai.chat.ChatModel")
SpringAiLlmChatService springAiLlmChatService() {
    return new SpringAiLlmChatService();
}
```
新增导入：`org.springframework.boot.autoconfigure.condition.ConditionalOnClass`

**T9 — httpApiLlmChatService 参数改为 ObjectProvider**：
```java
// 变更前：
@Bean
HttpApiLlmChatService httpApiLlmChatService(
        CredentialProvider credentialProvider,
        EndpointRateLimiter endpointRateLimiter) {

// 变更后：
@Bean
HttpApiLlmChatService httpApiLlmChatService(
        ObjectProvider<CredentialProvider> credentialProviderProvider,
        ObjectProvider<EndpointRateLimiter> endpointRateLimiterProvider) {
    return new HttpApiLlmChatService(
        credentialProviderProvider.getIfAvailable(),
        endpointRateLimiterProvider.getIfAvailable());
}
```

**T43 — refreshCapabilityTimeoutConfig 简化**：
```java
// 变更前（282-295 行）：
@Scheduled(fixedDelay = 60000, scheduler = "scheduledTaskExecutor")
public void refreshCapabilityTimeoutConfig() {
    log.debug("定时刷新超时配置");
    Environment env = applicationContext.getEnvironment();
    Binder binder = Binder.get(env);
    AiExecutionProperties refreshedProps = binder
        .bind("ai.execution", Bindable.of(AiExecutionProperties.class))
        .orElseGet(AiExecutionProperties::new);
    capabilityTimeoutConfigRef.set(new ConcurrentHashMap<>(refreshedProps.getPerCapability()));
    thinAdapterPerCapabilityConfigRef.set(new ConcurrentHashMap<>(refreshedProps.getThinAdapter().getPerCapability()));
    parseTimeoutConfigRef.set(new ConcurrentHashMap<>(refreshedProps.getParse().getPerCapability()));
    parseTimeoutDefaultRef.set(refreshedProps.getParse().getDefaultTimeout());
    log.debug("超时配置已刷新，包含 {} 项能力", refreshedProps.getPerCapability().size());
}

// 变更后：
@Scheduled(fixedDelay = 60000, scheduler = "scheduledTaskExecutor")
public void refreshCapabilityTimeoutConfig() {
    log.debug("定时刷新超时配置");
    AiExecutionProperties refreshedProps = applicationContext.getBean(AiExecutionProperties.class);
    capabilityTimeoutConfigRef.set(new ConcurrentHashMap<>(refreshedProps.getPerCapability()));
    thinAdapterPerCapabilityConfigRef.set(new ConcurrentHashMap<>(refreshedProps.getThinAdapter().getPerCapability()));
    parseTimeoutConfigRef.set(new ConcurrentHashMap<>(refreshedProps.getParse().getPerCapability()));
    parseTimeoutDefaultRef.set(refreshedProps.getParse().getDefaultTimeout());
    log.debug("超时配置已刷新，包含 {} 项能力", refreshedProps.getPerCapability().size());
}
```
可移除的导入：`org.springframework.boot.context.properties.bind.Bindable`、`org.springframework.boot.context.properties.bind.Binder`（`Environment` 保留，因 `refreshWindowSeconds()` 仍通过 `env.getProperty(...)` 读取配置）

**T44 — refreshWindowSeconds 线程安全确认**：
- `SlidingWindowMetricsStore.windowSeconds` 已为 `AtomicLong`（第 16 行）：`private final AtomicLong windowSeconds = new AtomicLong(60);`
- `setWindowSeconds(long windowSeconds)` 已有参数校验（第 162-167 行）
- `refreshWindowSeconds()` 方法体无需修改，当前调用 `store.setWindowSeconds(windowSeconds)` 已是线程安全的

### AiPlatformEnvironmentPostProcessor（T10）

**形态**：class（已有，新增 Logger 和日志）
**包路径**：`com.aimedical.modules.ai.impl.config`

**新增 Logger**：
```java
private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AiPlatformEnvironmentPostProcessor.class);
```

**postProcessEnvironment 方法变更**：
```java
// 变更前（13-20 行）：
public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication application) {
    String platformEnabled = env.getProperty("ai.platform.enabled");
    if (platformEnabled != null && env.getProperty("ai.mock.enabled") == null) {
        boolean mockDisabled = !Boolean.parseBoolean(platformEnabled);
        env.getPropertySources().addLast(
            new MapPropertySource("aiPlatformForwarding",
                Collections.singletonMap("ai.mock.enabled", String.valueOf(mockDisabled))));
    }
}

// 变更后：
public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication application) {
    String platformEnabled = env.getProperty("ai.platform.enabled");
    if (platformEnabled != null && env.getProperty("ai.mock.enabled") == null) {
        boolean mockDisabled = !Boolean.parseBoolean(platformEnabled);
        log.warn("ai.platform.enabled={} 且 ai.mock.enabled 未设置，自动设置 ai.mock.enabled={}",
            platformEnabled, String.valueOf(mockDisabled));
        env.getPropertySources().addLast(
            new MapPropertySource("aiPlatformForwarding",
                Collections.singletonMap("ai.mock.enabled", String.valueOf(mockDisabled))));
    }
}
```

**新增依赖**：`org.slf4j.Logger`、`org.slf4j.LoggerFactory`

### FallbackAiService（T45）

**形态**：class（已有，构造器修改）
**包路径**：`com.aimedical.modules.ai.impl.fallback`

**构造器变更**：
```java
// 变更前（49-55 行）：
public FallbackAiService(ObjectProvider<AiService> delegateProvider,
                          @Value("${ai.platform.enabled:false}") boolean aiPlatformEnabled) {
    this.delegate = delegateProvider.getIfUnique();
    if (this.delegate == null) {
        log.error("No available AiService delegate");
    }
}

// 变更后：
public FallbackAiService(ObjectProvider<AiService> delegateProvider) {
    this.delegate = delegateProvider.getIfUnique();
    if (this.delegate == null) {
        throw new IllegalStateException("No available AiService delegate: AI platform is enabled but no AiService implementation found");
    }
}
```

**移除导入**：`import org.springframework.beans.factory.annotation.Value;`

## 错误处理

| 文件 | 变更 |
|------|------|
| FallbackAiService | T45：delegate==null 时由 `log.error` 静默降级改为 `throw new IllegalStateException` fail-fast |
| AiPlatformConfig | T8：`@ConditionalOnProperty` 使 `ai.platform.enabled` 为 false/未设置时整个配置类不生效，其内部所有 `@Bean` 不注册 |

## 行为契约

| 组件 | 契约 |
|------|------|
| LlmChatRequest | 6 参构造器不再参与 Jackson 反序列化。5 参构造器是唯一的 Jackson 反序列化入口。JSON 中不包含 endpointUrl 时 deserialize 正常（endpointUrl=null） |
| AiPlatformConfig | 类级别条件化：`ai.platform.enabled=true` 时配置类生效。`springAiLlmChatService` 额外受 `ChatModel` 类存在性约束。`delegatingLlmChatService` 通过 ObjectProvider 延迟解析子服务，任一子服务缺失不影响整体 Bean 创建 |
| AiPlatformEnvironmentPostProcessor | 在设置 `ai.mock.enabled` 前输出 WARN 日志，告知用户自动覆盖行为。Logger 为类级别静态成员 |
| FallbackAiService | 移除 `aiPlatformEnabled` 构造参数。delegate 为 null 时立即抛出 IllegalStateException 阻止 Spring 容器启动。保留 `@Service`、`@Primary`、`ObjectProvider<AiService>` 参数 |

## 依赖关系

| 类型 | 依赖变更 |
|------|---------|
| LlmChatRequest | 无新增/移除依赖 |
| AiPlatformConfig | 新增：`ConditionalOnProperty`、`ConditionalOnClass`、`ObjectProvider`；移除：`Bindable`、`Binder`（Environment 保留，因 refreshWindowSeconds 仍依赖） |
| AiPlatformEnvironmentPostProcessor | 新增：`org.slf4j.Logger`、`org.slf4j.LoggerFactory` |
| FallbackAiService | 移除：`org.springframework.beans.factory.annotation.Value` |

## 测试文件变更

| 测试文件 | 变更 |
|---------|------|
| `LlmChatRequestTest.java` | 无需变更（RETRY 修复后 `shouldDeserializeFromJson:67` 和 `shouldRoundTripThroughJson:83` 应通过） |
| `AiPlatformConfigTest.java` | 建议新增 T8 验证：`@ConditionalOnProperty` 是否存在；T9 验证：`delegatingLlmChatService` 参数类型为 `ObjectProvider`；T43 验证：`refreshCapabilityTimeoutConfig` 调用 `applicationContext.getBean` |
| `FallbackAiServiceTest.java` | T45：构造器调用需移除 `aiPlatformEnabled` 参数；`delegate==null` 断言改为 `assertThrows(IllegalStateException.class, () -> new FallbackAiService(emptyProvider))` |

## 修订说明（v10 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| 设计声明可移除 Environment 导入，但 refreshWindowSeconds() 仍使用 env.getProperty(...) 需要该导入。应改为仅移除 Bindable 和 Binder，保留 Environment。 | 移除列表从 `Bindable`、`Binder`、`Environment` 改为仅 `Bindable`、`Binder`，标注 Environment 保留的原因；依赖关系表同步更正 |
