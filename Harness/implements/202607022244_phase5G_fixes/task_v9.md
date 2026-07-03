# 任务指令（v9）

## 动作
NEW

## 任务描述
修复 LLM 客户端基础设施 10 项问题（T6/T7/T36/T37/T38/T11/T12/T50/T39/T40），涉及 6 个源文件：
- `DelegatingLlmChatService.java` — T6(@PostConstruct 枚举校验)、T7(回退日志上下文)、T36(getClientType 返回 null)
- `EndpointRateLimiter.java` — T37(maxBurstSeconds 死代码)
- `HttpApiLlmChatService.java` — T38(限流绕过降级管线)、T11(HttpClient 池化)、T12(endpointUrl 混用)、T50(异常风格统一)
- `SpringAiLlmChatService.java` — T39(异常类型替换)
- `SpringAiLlmChatStreamService.java` — T39(异常类型替换)
- `LlmChatStreamService.java` — T40(未使用导入)

## 选择理由
R8（指标与健康管理）已验证通过（2527 pass / 0 fail）。R9 按计划推进 LLM 客户端基础设施——这些任务集中在 client 包下，涉及 DelegatingLlmChatService、EndpointRateLimiter、HttpApiLlmChatService 等核心 LLM 通信组件，修复后影响运行时 LLM 调用链路的健壮性和可观测性。

## 任务上下文
### T6: @PostConstruct 枚举值完整性校验
**问题**：`DelegatingLlmChatService` 构造器接收 `Map<ClientType, LlmChatService>`，但无启动期校验是否所有 `ClientType` 枚举值都有对应实现。
**修复**：添加 `@PostConstruct` 方法，遍历 `ClientType.values()`，对没有对应 `delegates` 条目的枚举值记录 WARN 日志。
**ClientType 当前值**：`HTTP_API`, `SPRING_AI`
**涉及导入**：`javax.annotation.PostConstruct`

### T7: 回退日志缺少上下文信息和健康检查端点告警
**问题**：`chat()` 和 `structuredChat()` 中的回退日志仅输出 `"未找到 ClientType={} 的实现，回退到 HTTP_API"`，缺少请求上下文信息（如 request detail）和健康检查端点告警。
**修复**：回退日志中增加 `request.getEndpointId()` 信息；添加 WARN 日志提示健康检查端点可能存在问题。
**日志模板**：
- `log.warn("未找到 ClientType={} 的实现（endpointId={}），回退到 HTTP_API；建议检查健康检查端点状态", ct, request.getEndpointId())`
- 原 `log.error` 改为 `log.warn`

### T36: getClientType() 返回 null 语义不清
**问题**：`DelegatingLlmChatService.getClientType()` 返回 `null`。作为委托代理，其自身不持有具体 ClientType，返回 null 可能误导调用方在其上调用 `chat()` 时预期行为。
**修复**：改为抛出 `UnsupportedOperationException`（语义明确：委托代理不支持独立调用），移除 existing 测试中 `shouldReturnNullClientType` 并更新为 `shouldThrowOnGetClientType`。

### T37: maxBurstSeconds 死代码
**问题**：`EndpointRateLimiter` 声明了 `defaultMaxBurstSeconds` 字段（`@Value("${...}")`），但在 `tryAcquire()` 中从未使用。Guava `RateLimiter.create(permitsPerSecond)` 默认 burst 为 1 秒。
**修复**：移除 `defaultMaxBurstSeconds` 字段及相关 `@Value` 注入；或者使用 `RateLimiter.create(permitsPerSecond, maxBurstSeconds, TimeUnit.SECONDS)` 应用该配置。选择移除死代码（更简洁，且 Guava 默认 burst 行为满足设计文档要求）。
**注意**：如果移除，需同步移除字段声明、@Value 注解和字段注入。

### T38: 限流拒绝绕过降级管线
**问题**：`HttpApiLlmChatService.chat()` 中限流拒绝时返回 `AiResult.failure("RATE_LIMITED", "请求被限流")`，直接以成功 CompletableFuture 完成，绕过了调用方（AbstractCapabilityExecutor）的降级管线。降级管线依赖异常传播来触发降级决策。
**修复**：抛出 `LlmInfrastructureException("RATE_LIMITED: " + endpointId)` 替代 `AiResult.failure`。同步更新测试 `shouldReturnRateLimitedResult` 从 assert result.isSuccess()==false 改为 assertThrows(LlmInfrastructureException.class)。
**涉及测试文件**：`HttpApiLlmChatServiceTest.java`

### T11: HttpClient 无连接池复用
**问题**：`HttpApiLlmChatService.chat()` 每次调用都执行 `HttpClient client = HttpClient.newHttpClient()`，无连接池、无 TLS 会话复用，频繁 GC 压力。
**修复**：将 `HttpClient` 提升为类字段，在构造器中初始化一次（使用 `HttpClient.newBuilder()` 配置超时等参数），后续 `chat()` 调用复用同一实例。
```java
private final HttpClient httpClient;

public HttpApiLlmChatService(CredentialProvider credentialProvider,
                             EndpointRateLimiter endpointRateLimiter,
                             HttpClient httpClient) {
    // 新增 HttpClient 构造参数
}

// 或使用默认构造：
private final HttpClient httpClient = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .build();
```
**首选方案**：构造器注入 `HttpClient` 或内部构建默认客户端（作为类字段而非方法局部变量）。

### T12: endpointId 误用作 HTTP URI
**问题**：`HttpApiLlmChatService.chat()` line 57 使用 `URI.create(endpointId)` 作为 HTTP 请求 URI。endpointId 是服务标识符（如 `"gpt-4"`），不是 URL。正确做法是从请求或配置中获取 `endpointUrl`。
**修复**：需要了解 `LlmChatRequest` 中是否有 `endpointUrl` 字段。检查现有代码——LlmChatRequest 只有 `endpointId`。需要读取 `AiPlatformConfig` 或类似配置来获取 endpointId→endpointUrl 映射。
**查看请求模型**：`LlmChatRequest` 需要增加 `endpointUrl` 字段，或在 `HttpApiLlmChatService` 中维护 endpointUrl 映射。

**注意**：需要检查 LlmChatRequest 是否有 endpointUrl 字段。

### T50: structuredChat() 异常处理风格不一致
**问题**：`HttpApiLlmChatService.structuredChat()` 直接抛出 `StructuredOutputNotSupportedException`（继承 RuntimeException），而 `chat()` 对异常统一返回 `AiResult.failure`。
**修复**：使 `structuredChat()` 也返回 `CompletableFuture.completedFuture(AiResult.failure(...))` 而不是直接抛异常，保持与 `chat()` 一致风格。
```java
@Override
public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
        LlmChatRequest request, Class<T> targetClass) {
    return CompletableFuture.completedFuture(
        AiResult.failure("NOT_SUPPORTED", "structuredChat not implemented for HTTP API"));
}
```

### T39: UnsupportedOperationException 应替换为 LlmInfrastructureException
**问题**：`SpringAiLlmChatService.chat()` 和 `SpringAiLlmChatStreamService.chatStream()` 抛出 `UnsupportedOperationException("Spring AI not available")`，但设计文档要求 LLM 基础设施级异常应使用 `LlmInfrastructureException`。
**修复**：替换两个类中的异常类型：
- `SpringAiLlmChatService` line 18: `throw new UnsupportedOperationException("Spring AI not available")` → `throw new LlmInfrastructureException("Spring AI not available")`
- `SpringAiLlmChatStreamService` line 12: `throw new UnsupportedOperationException("Spring AI not available")` → `throw new LlmInfrastructureException("Spring AI not available")`
**涉及测试文件**：`SpringAiLlmChatServiceTest.java`、`SpringAiLlmChatStreamServiceTest.java` — assertThrows 中的异常类型需同步更新。

### T40: LlmChatStreamService 未使用导入
**问题**：`LlmChatStreamService.java` line 3 `import com.aimedical.modules.ai.impl.client.exception.AiAbilityInputInvalidException;` 从未在接口中使用。
**修复**：移除该 import 语句。

## 已有代码上下文

### DelegatingLlmChatService.java
- 路径：`ai-impl/.../client/DelegatingLlmChatService.java`
- 53 行，无 `@PostConstruct` 方法
- `getClientType()` 返回 `null`
- 回退日志使用 `log.error` 级别，无 endpointId 上下文

### EndpointRateLimiter.java
- 路径：`ai-impl/.../client/EndpointRateLimiter.java`
- 47 行，`defaultMaxBurstSeconds` 声明但未使用
- `tryAcquire()` 创建 RateLimiter 时仅传入 `permitsPerSecond`

### HttpApiLlmChatService.java
- 路径：`ai-impl/.../client/HttpApiLlmChatService.java`
- 79 行，每次 chat() 新建 `HttpClient.newHttpClient()`
- line 57 使用 `URI.create(endpointId)` 而非 endpointUrl
- line 47-48 限流拒绝返回 `AiResult.failure` 而非抛异常
- line 78 structuredChat() 直接抛异常而非返回 AiResult.failure

### SpringAiLlmChatService.java
- 路径：`ai-impl/.../client/SpringAiLlmChatService.java`
- 25 行，chat() 和 structuredChat() 均抛 `UnsupportedOperationException`

### SpringAiLlmChatStreamService.java
- 路径：`ai-impl/.../client/SpringAiLlmChatStreamService.java`
- 13 行，chatStream() 抛 `UnsupportedOperationException`

### LlmChatStreamService.java
- 路径：`ai-impl/.../client/LlmChatStreamService.java`
- 7 行，引用了未使用的 `AiAbilityInputInvalidException`

### 关键 API 类型
- `LlmInfrastructureException`（`ai-impl/.../client/exception/`）：继承 `RuntimeException`，消息构造器
- `ClientType` 枚举：`HTTP_API`, `SPRING_AI`
- `LlmChatRequest`：含 `getEndpointId()`, `getClientType()` 等
- `AiResult`：`success(data)` / `failure(errorCode, errorMessage)` 工厂方法
- `StructuredOutputNotSupportedException`：继承 `RuntimeException`

### 涉及测试文件（需同步更新）
- `DelegatingLlmChatServiceTest.java` — T36：`shouldReturnNullClientType` → `shouldThrowOnGetClientType`
- `EndpointRateLimiterTest.java` — T37：无变更（移除字段不影响测试行为）
- `HttpApiLlmChatServiceTest.java` — T38/T11/T12/T50：限流异常类型变更、HttpClient 构造参数、结构化 chat 返回 AiResult.failure
- `SpringAiLlmChatServiceTest.java` — T39：assertThrows 异常类型 `UnsupportedOperationException` → `LlmInfrastructureException`
- `SpringAiLlmChatStreamServiceTest.java` — T39：同上
- `LlmChatStreamServiceTest.java` — T40：无变更（仅移除导入）
