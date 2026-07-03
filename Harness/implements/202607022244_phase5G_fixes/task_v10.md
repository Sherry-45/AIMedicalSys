# 任务指令（v10）

## 动作
RETRY + NEW

## 任务描述
修复 LLM 客户端基础设施验证遗留问题 + 实现配置与环境管理：涉及 4 个源文件。

### RETRY — LlmChatRequest Jackson 反序列化修复
- **文件**: `ai-impl/.../client/LlmChatRequest.java`
- **问题**: 5 参构造器与 6 参构造器均标注 `@JsonProperty`，Jackson 无法确定使用哪个构造器反序列化，导致 `shouldDeserializeFromJson:67` 和 `shouldRoundTripThroughJson:83` 测试错误
- **修正**: 移除 6 参构造器所有参数的 `@JsonProperty` 注解（保留 5 参构造器的 `@JsonProperty`），消除构造器歧义

### NEW — 配置与环境管理 6 项问题
| 问题 | 文件 | 说明 |
|------|------|------|
| T8 | `AiPlatformConfig.java` | 类级别缺少 `@ConditionalOnProperty` |
| T9 | `AiPlatformConfig.java` | delegatingLlmChatService 未用 ObjectProvider；springAiLlmChatService 缺 @ConditionalOnClass |
| T43 | `AiPlatformConfig.java` | refreshCapabilityTimeoutConfig Binder 重绑定不一致 |
| T44 | `AiPlatformConfig.java` | refreshWindowSeconds 直接调用 setter 线程安全确认 |
| T10 | `AiPlatformEnvironmentPostProcessor.java` | 缺少配置冲突告警日志 |
| T45 | `FallbackAiService.java` | delegate==null 无 fail-fast；aiPlatformEnabled 未使用 |

## 选择理由
R9 验证失败（2 个 LlmChatRequestTest 错误）仅需移除 6 参构造器 `@JsonProperty` 注解（2 行变更），修复代价极小，与 R10 配置任务无冲突可合并一轮。R10 6 项配置问题集中在 3 个源文件（AiPlatformConfig / AiPlatformEnvironmentPostProcessor / FallbackAiService），强相关且均涉及 Spring 条件化装配与配置刷新，合并处理减少轮次。

## 任务上下文

### RETRY — LlmChatRequest 修复
- 当前代码（`LlmChatRequest.java:32-44`）：6 参构造器每个参数标注 `@JsonProperty`，与 5 参构造器（`LlmChatRequest.java:24-29`）冲突
- 5 参构造器调用 `this(messages, options, clientType, tools, endpointId, null)` 委托给 6 参构造器
- Jackson 发现两个 `@JsonProperties` 标注的构造器→抛出异常
- **修正**: 6 参构造器参数全部移除 `@JsonProperty`；5 参构造器保留不变
- 验证：`LlmChatRequestTest.shouldDeserializeFromJson` 和 `shouldRoundTripThroughJson` 需通过

### T8 — @ConditionalOnProperty
- `AiPlatformConfig` 类级别添加 `@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true", matchIfMissing = false)`
- 新增导入：`org.springframework.boot.autoconfigure.condition.ConditionalOnProperty`
- 当 `ai.platform.enabled` 未设置或为 false 时，整个配置类不生效，其内部所有 `@Bean` 方法不执行
- FallbackAiService 已通过 `ObjectProvider<AiService>` 延迟解析 + null 守卫适配无 Bean 场景

### T9 — ObjectProvider + @ConditionalOnClass
- `springAiLlmChatService()` Bean 方法添加 `@ConditionalOnClass(name = "org.springframework.ai.chat.ChatModel")`
  - 新增导入：`org.springframework.boot.autoconfigure.condition.ConditionalOnClass`
- `delegatingLlmChatService(...)` 方法参数 `HttpApiLlmChatService httpApi` 和 `SpringAiLlmChatService springAi` 改为 `ObjectProvider<HttpApiLlmChatService>` 和 `ObjectProvider<SpringAiLlmChatService>`
  - 方法体内：`ObjectProvider.getIfAvailable()` 取得时 put，null 时不 put
  - 新增导入：`org.springframework.beans.factory.ObjectProvider`
- `httpApiLlmChatService()` 方法同理改为 `ObjectProvider<CredentialProvider>`、`ObjectProvider<EndpointRateLimiter>` 方式（可选，降低耦合）

### T43 — refreshCapabilityTimeoutConfig Binder 修复
- 当前 `refreshCapabilityTimeoutConfig()` 使用 `Binder.get(env).bind("ai.execution", Bindable.of(AiExecutionProperties.class))` 重新创建全新实例
- 改为 `applicationContext.getBean(AiExecutionProperties.class)` 读取已由 Spring 托管的单例 bean，与 `cacheInitialConfigValues()` 一致
- 移除 `Environment env`、`Binder`、`Bindable` 相关代码
- 简化后方法体：
  ```java
  AiExecutionProperties refreshedProps = applicationContext.getBean(AiExecutionProperties.class);
  capabilityTimeoutConfigRef.set(new ConcurrentHashMap<>(refreshedProps.getPerCapability()));
  thinAdapterPerCapabilityConfigRef.set(new ConcurrentHashMap<>(refreshedProps.getThinAdapter().getPerCapability()));
  parseTimeoutConfigRef.set(new ConcurrentHashMap<>(refreshedProps.getParse().getPerCapability()));
  parseTimeoutDefaultRef.set(refreshedProps.getParse().getDefaultTimeout());
  ```

### T44 — refreshWindowSeconds 线程安全确认
- T51（R8 已实现）已将 `SlidingWindowMetricsStore.windowSeconds` 改为 `AtomicLong` 并添加 `setWindowSeconds()` 参数校验（`windowSeconds > 0`）
- `refreshWindowSeconds()` 直接调用 `store.setWindowSeconds(windowSeconds)` 当前已是线程安全的
- 仅需确认无额外动作，无需修改代码。可在日志或注释中补充说明

### T10 — 配置冲突告警日志
- `AiPlatformEnvironmentPostProcessor.postProcessEnvironment()` 当前在 `platformEnabled != null && mockEnabled == null` 时静默设置 `ai.mock.enabled`
- 在 `env.getPropertySources().addLast(...)` 前添加：
  ```java
  log.warn("ai.platform.enabled={} 且 ai.mock.enabled 未设置，自动设置 ai.mock.enabled={}", platformEnabled, !Boolean.parseBoolean(platformEnabled));
  ```
- `AiPlatformEnvironmentPostProcessor` 需添加 Logger（SLF4J）：
  ```java
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AiPlatformEnvironmentPostProcessor.class);
  ```

### T45 — FallbackAiService 构造器修复
- `delegate == null` 时当前仅 `log.error(...)`，改为 `throw new IllegalStateException("No available AiService delegate: AI platform is enabled but no AiService implementation found")` fail-fast
- 移除 `@Value("${ai.platform.enabled:false}") boolean aiPlatformEnabled` 参数及对应的 `import org.springframework.beans.factory.annotation.Value`
- 保留 `ObjectProvider<AiService> delegateProvider` 参数

## 已有代码上下文

### LlmChatRequest.java（当前结构）
```java
// 5 参构造器（有 @JsonProperty，委托 6 参）
public LlmChatRequest(@JsonProperty("messages") List<LlmChatMessage> messages, ...) {
    this(messages, options, clientType, tools, endpointId, null);
}
// 6 参构造器（有 @JsonProperty → 需移除）
public LlmChatRequest(@JsonProperty("messages") List<LlmChatMessage> messages, ..., @JsonProperty("endpointUrl") String endpointUrl) {
    // 字段赋值
}
```
修正后：6 参构造器参数前所有 `@JsonProperty(...)` 移除。

### AiPlatformConfig.java（关键 Bean 定义）
- 类级：`@Configuration` + `@EnableConfigurationProperties` + `@EnableJpaRepositories` + `@EntityScan` + `@EnableAsync` + `@EnableScheduling`
- `springAiLlmChatService()`: 直接 `return new SpringAiLlmChatService()`，无条件
- `delegatingLlmChatService(HttpApiLlmChatService httpApi, SpringAiLlmChatService springAi)`: 直接注入，无 ObjectProvider
- `refreshCapabilityTimeoutConfig()`: 使用 Binder.bind() 重新读取 Environment
- `refreshWindowSeconds()`: `env.getProperty("ai.sliding-window.window-seconds")` → `store.setWindowSeconds(windowSeconds)`

### AiPlatformEnvironmentPostProcessor.java
- 实现 `EnvironmentPostProcessor`，无 Spring 注解，非 Bean
- 当前无 Logger，需手动添加

### FallbackAiService.java
- `@Service @Primary`，构造器 `(ObjectProvider<AiService> delegateProvider, @Value("${ai.platform.enabled:false}") boolean aiPlatformEnabled)`
- `delegateProvider.getIfUnique()` 返回 null 时仅 `log.error(...)`，无异常抛出
- `aiPlatformEnabled` 参数仅注入，方法体中从未使用

## RETRY 说明
**失败原因**: LlmChatRequest 6 参构造器的 `@JsonProperty` 注解与 5 参构造器冲突，Jackson 无法识别唯一构造器。
**修正方向**: 6 参构造器参数全部移除 `@JsonProperty` 注解（该构造器仅程序内调用，不涉及 JSON 反序列化）。
**验证**: LlmChatRequestTest.shouldDeserializeFromJson + shouldRoundTripThroughJson 需通过。
