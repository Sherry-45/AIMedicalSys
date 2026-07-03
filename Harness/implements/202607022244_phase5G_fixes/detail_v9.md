# 详细设计（v9）

## 概述

修复 LLM 客户端基础设施 10 项问题（T6/T7/T36/T37/T38/T11/T12/T50/T39/T40），涉及 6 个源文件。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-impl/.../client/DelegatingLlmChatService.java` | 修改 | T6(@PostConstruct 枚举校验)、T7(回退日志上下文)、T36(getClientType 抛异常) |
| `ai-impl/.../client/EndpointRateLimiter.java` | 修改 | T37(移除 maxBurstSeconds 死代码) |
| `ai-impl/.../client/HttpApiLlmChatService.java` | 修改 | T38(限流抛异常)、T11(HttpClient 池化)、T12(endpointUrl)、T50(structuredChat 统一风格) |
| `ai-impl/.../client/SpringAiLlmChatService.java` | 修改 | T39(异常类型替换) |
| `ai-impl/.../client/SpringAiLlmChatStreamService.java` | 修改 | T39(异常类型替换) |
| `ai-impl/.../client/LlmChatStreamService.java` | 修改 | T40(移除未使用导入) |
| `ai-impl/.../client/LlmChatRequest.java` | 修改 | T12(新增 endpointUrl 字段) |

## 类型定义

### LlmChatRequest

**形态**：class（已有，新增字段）
**包路径**：`com.aimedical.modules.ai.impl.client`

**新增字段**：
```java
private final String endpointUrl;
```

**构造器变更**：全参构造器新增 `@JsonProperty("endpointUrl") String endpointUrl` 参数，位于 `endpointId` 之后。无参构造器中 `this.endpointUrl = null`。

**字段顺序**（最终 6 个字段）：messages, options, clientType, tools, endpointId, endpointUrl

**新增 getter**：
```java
public String getEndpointUrl() { return endpointUrl; }
```

### DelegatingLlmChatService

**形态**：class（已有，新增方法）
**包路径**：`com.aimedical.modules.ai.impl.client`

**T6 变更**：新增 `@PostConstruct` 方法：
```java
@PostConstruct
void checkDelegatesCompleteness() {
    for (ClientType ct : ClientType.values()) {
        if (!delegates.containsKey(ct)) {
            log.warn("ClientType={} 没有对应的 LlmChatService 实现，请在配置中注册", ct);
        }
    }
}
```

**新增导入**：`jakarta.annotation.PostConstruct`

**T7 变更**：`chat()` 和 `structuredChat()` 中的回退日志块：
```java
// 变更前（chat 中第 27 行）：
log.error("未找到 ClientType={} 的实现，回退到 HTTP_API", ct);

// 变更后：
log.warn("未找到 ClientType={} 的实现（endpointId={}），回退到 HTTP_API；建议检查健康检查端点状态", ct, request.getEndpointId());
```

同样修改 `structuredChat()` 中的对应行（第 42 行）。

**T36 变更**：
```java
// 变更前：
@Override
public ClientType getClientType() {
    return null;
}

// 变更后：
@Override
public ClientType getClientType() {
    throw new UnsupportedOperationException("DelegatingLlmChatService does not have its own ClientType");
}
```

### EndpointRateLimiter

**形态**：class（已有，移除字段）
**包路径**：`com.aimedical.modules.ai.impl.client`

**T37 变更**：移除以下字段声明及 @Value 注解（第 20-21 行）：
```java
// 需移除：
@Value("${ai.rate-limiting.endpoints.default.max-burst-seconds:1}")
private double defaultMaxBurstSeconds;
```

保留字段：`defaultPermitsPerSecond`, `defaultQueueWaitMillis`, `limiters`, `environment`。

`tryAcquire()` 方法中 `RateLimiter.create(permitsPerSecond)` 不变（仍使用单参数创建）。

### HttpApiLlmChatService

**形态**：class（已有，多处变更）
**包路径**：`com.aimedical.modules.ai.impl.client`

**T11 变更**：新增 httpClient 字段，构造器初始化：

```java
// 新增字段：
private final HttpClient httpClient;

// 构造器新增参数：
public HttpApiLlmChatService(CredentialProvider credentialProvider,
                              EndpointRateLimiter endpointRateLimiter,
                              HttpClient httpClient) {
    this.credentialProvider = credentialProvider;
    this.endpointRateLimiter = endpointRateLimiter;
    this.httpClient = httpClient;
}

// 同时保留无 HttpClient 参数的构造器作为兼容（内部创建默认客户端）：
public HttpApiLlmChatService(CredentialProvider credentialProvider,
                              EndpointRateLimiter endpointRateLimiter) {
    this(credentialProvider, endpointRateLimiter,
         HttpClient.newBuilder()
             .connectTimeout(Duration.ofSeconds(10))
             .build());
}
```

`chat()` 方法中移除 `HttpClient client = HttpClient.newHttpClient();`（原第 52 行），改为使用 `this.httpClient`。

**T12 变更**：`chat()` 方法中 URI 创建逻辑：
```java
// 变更前（第 57 行）：
.uri(URI.create(endpointId))

// 变更后：
String url = request.getEndpointUrl() != null ? request.getEndpointUrl() : endpointId;
.uri(URI.create(url))
```

**T38 变更**：`chat()` 方法中限流拒绝逻辑：
```java
// 变更前（第 46-49 行）：
boolean acquired = endpointRateLimiter.tryAcquire(endpointId);
if (!acquired) {
    return CompletableFuture.completedFuture(AiResult.failure("RATE_LIMITED", "请求被限流"));
}

// 变更后：
boolean acquired = endpointRateLimiter.tryAcquire(endpointId);
if (!acquired) {
    throw new LlmInfrastructureException("RATE_LIMITED: " + endpointId);
}
```

由于 `LlmInfrastructureException` 在 `catch` 块（原第 68-69 行）中被重新抛出，该异常会同步传播出 `chat()` 方法，与现有 `LlmInfrastructureException`/`AiAbilityInputInvalidException` 的传播方式一致。

**T50 变更**：`structuredChat()` 方法：
```java
// 变更前（第 76-79 行）：
@Override
public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
        LlmChatRequest request, Class<T> targetClass) {
    throw new StructuredOutputNotSupportedException("structuredChat not implemented for HTTP API");
}

// 变更后：
@Override
public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
        LlmChatRequest request, Class<T> targetClass) {
    return CompletableFuture.completedFuture(
        AiResult.failure("NOT_SUPPORTED", "structuredChat not implemented for HTTP API"));
}
```

移除导入：`import com.aimedical.modules.ai.impl.client.exception.StructuredOutputNotSupportedException;`

### SpringAiLlmChatService

**形态**：class（已有，异常类型替换）
**包路径**：`com.aimedical.modules.ai.impl.client`

**T39 变更**：
```java
// 变更前（第 18 行）：
throw new UnsupportedOperationException("Spring AI not available");
// 变更后：
throw new LlmInfrastructureException("Spring AI not available");

// 变更前（第 24 行）：
throw new UnsupportedOperationException("Spring AI not available");
// 变更后：
throw new LlmInfrastructureException("Spring AI not available");
```

**新增导入**：`import com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureException;`

### SpringAiLlmChatStreamService

**形态**：class（已有，异常类型替换）
**包路径**：`com.aimedical.modules.ai.impl.client`

**T39 变更**：
```java
// 变更前（第 12 行）：
throw new UnsupportedOperationException("Spring AI not available");
// 变更后：
throw new LlmInfrastructureException("Spring AI not available");
```

**新增导入**：`import com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureException;`

### LlmChatStreamService

**形态**：interface（已有，移除导入）
**包路径**：`com.aimedical.modules.ai.impl.client`

**T40 变更**：移除第 3 行 `import com.aimedical.modules.ai.impl.client.exception.AiAbilityInputInvalidException;`

接口内容不变。

## 错误处理

| 文件 | 变更 |
|------|------|
| DelegatingLlmChatService | T36：getClientType() 改为抛出 UnsupportedOperationException（明确语义：委托代理不支持独立调用） |
| HttpApiLlmChatService | T38：限流拒绝时抛 LlmInfrastructureException 而非返回 AiResult.failure，使异常沿现有异常传播路径到达调用方的降级管线。T50：structuredChat 不再抛 StructuredOutputNotSupportedException，改为返回 AiResult.failure |
| SpringAiLlmChatService | T39：UnsupportedOperationException 替换为 LlmInfrastructureException |
| SpringAiLlmChatStreamService | T39：同上 |

## 行为契约

| 组件 | 契约 |
|------|------|
| DelegatingLlmChatService | @PostConstruct 在 Spring 完成依赖注入后执行，仅记录 WARN 日志不阻止启动。getClientType() 永远抛出 UnsupportedOperationException。回退日志包含 endpointId 和健康检查告警，日志级别为 WARN |
| EndpointRateLimiter | maxBurstSeconds 字段完全移除，tryAcquire() 行为不变（Guava 默认 1 秒 burst） |
| HttpApiLlmChatService | httpClient 为类字段，所有 chat() 调用共享同一实例。URI 优先使用 request.getEndpointUrl()，fallback 到 endpointId。限流拒绝同步抛 LlmInfrastructureException。structuredChat 返回 AiResult.failure CompletableFuture |
| SpringAiLlmChatService | chat() 和 structuredChat() 抛 LlmInfrastructureException（消息内容不变） |
| SpringAiLlmChatStreamService | chatStream() 抛 LlmInfrastructureException（消息内容不变） |
| LlmChatStreamService | 仅移除未使用导入，接口签名不变 |
| LlmChatRequest | endpointUrl 为不可变字段，构造器在 endpointId 之后新增该参数。无参构造器中为 null。向后兼容：原有构造调用（不传 endpointUrl）仍可编译通过？——需确认：原有 5 参构造器签名不变，新增 6 参构造器，兼容 |

**兼容性说明——LlmChatRequest 构造器处理**：
为保证向后兼容，不修改现有的 5 参构造器签名，而是新增一个 6 参构造器：
```java
// 保留原有 5 参构造器（不变）：
public LlmChatRequest(@JsonProperty("messages") List<LlmChatMessage> messages,
                      @JsonProperty("options") LlmChatOptions options,
                      @JsonProperty("clientType") ClientType clientType,
                      @JsonProperty("tools") List<ChatToolDefinition> tools,
                      @JsonProperty("endpointId") String endpointId) {
    this(messages, options, clientType, tools, endpointId, null);
}

// 新增 6 参构造器：
public LlmChatRequest(@JsonProperty("messages") List<LlmChatMessage> messages,
                      @JsonProperty("options") LlmChatOptions options,
                      @JsonProperty("clientType") ClientType clientType,
                      @JsonProperty("tools") List<ChatToolDefinition> tools,
                      @JsonProperty("endpointId") String endpointId,
                      @JsonProperty("endpointUrl") String endpointUrl) {
    this.messages = messages;
    this.options = options;
    this.clientType = clientType;
    this.tools = tools;
    this.endpointId = endpointId;
    this.endpointUrl = endpointUrl;
}
```
无参构造器中增加 `this.endpointUrl = null`。

## 依赖关系

| 类型 | 依赖变更 |
|------|---------|
| DelegatingLlmChatService | 新增 `jakarta.annotation.PostConstruct` |
| EndpointRateLimiter | 无新增依赖，移除 `defaultMaxBurstSeconds` 字段 |
| HttpApiLlmChatService | 新增 `java.net.http.HttpClient` 字段；移除 `StructuredOutputNotSupportedException` 导入 |
| SpringAiLlmChatService | 新增 `LlmInfrastructureException` 导入 |
| SpringAiLlmChatStreamService | 新增 `LlmInfrastructureException` 导入 |
| LlmChatStreamService | 移除 `AiAbilityInputInvalidException` 导入 |
| LlmChatRequest | 无新增依赖，仅新增 `endpointUrl` 字段 |

## 测试文件变更

| 测试文件 | 变更 |
|---------|------|
| `DelegatingLlmChatServiceTest.java` | T36：`shouldReturnNullClientType` → `shouldThrowOnGetClientType`，`assertNull(...)` → `assertThrows(UnsupportedOperationException.class, ...)` |
| `EndpointRateLimiterTest.java` | T37：无变更（移除字段不影响测试行为） |
| `HttpApiLlmChatServiceTest.java` | T38：`shouldReturnRateLimitedResult` 中 `assertFalse(result.isSuccess())` → `assertThrows(LlmInfrastructureException.class, ...)`；T11：构造器调用需适配（新增 HttpClient 参数或用双参构造器）；T12：如需测试 endpointUrl 可新增用例；T50：`shouldThrowStructuredOutputNotSupported` → 断言改为 `assertFalse(result.isSuccess())` |
| `SpringAiLlmChatServiceTest.java` | T39：`assertThrows(UnsupportedOperationException.class, ...)` → `assertThrows(LlmInfrastructureException.class, ...)` |
| `SpringAiLlmChatStreamServiceTest.java` | T39：同上 |
| `LlmChatStreamServiceTest.java` | T40：无变更（仅移除导入） |
