# 任务指令（v13 r1）

## 动作
REVIEW_REVISED

## 任务描述
在 `ai-impl/client/` 包新增 4 个 LLM 调用实现类，覆盖同步对话（LlmChatService）和流式对话（LlmChatStreamService）的 HTTP API 与 Spring AI 两种实现；同步修改 `LlmChatRequest.java` 新增 `endpointId` 字段；重构 `DelegatingLlmChatService.java` 为 `Map<ClientType, LlmChatService>` 构造器注入、移除 `@Service`/`@Primary`；新增 `AiClientConfig.java` 临时 `@Configuration` 统一装配（Task 18 AiPlatformConfig 将吸收）。

### 全部涉及文件
| # | 文件路径 | 操作 | 说明 |
|---|---------|------|------|
| 1 | `ai-impl/.../client/HttpApiLlmChatService.java` | **新建** | LlmChatService HTTP API 直连同步实现（chat + structuredChat） |
| 2 | `ai-impl/.../client/HttpApiLlmChatStreamService.java` | **新建** | LlmChatStreamService HTTP API 流式实现（chatStream → Flux.empty()） |
| 3 | `ai-impl/.../client/SpringAiLlmChatService.java` | **新建** | LlmChatService Spring AI 同步存根（抛 UnsupportedOperationException） |
| 4 | `ai-impl/.../client/SpringAiLlmChatStreamService.java` | **新建** | LlmChatStreamService Spring AI 流式存根（抛 UnsupportedOperationException） |
| 5 | `ai-impl/.../client/LlmChatRequest.java` | **修改** | 新增 `endpointId` 字段 + getter + 构造器参数 |
| 6 | `ai-impl/.../client/DelegatingLlmChatService.java` | **修改** | 构造器改为 `Map<ClientType, LlmChatService>`；移除 `@Service`/`@Primary`；保留回退逻辑 |
| 7 | `ai-impl/.../client/AiClientConfig.java` | **新建** | 临时 `@Configuration`，注册 4 个实现 + DelegatingLlmChatService（@Bean @Primary） |
| 8 | `ai-impl/.../client/HttpApiLlmChatServiceTest.java` | **新建** | 测试凭据缺失/限流拒绝/HTTP 异常三路径 + chat success |
| 9 | `ai-impl/.../client/SpringAiLlmChatServiceTest.java` | **新建** | 测试 UnsupportedOperationException（chat + structuredChat） |
| 10 | `ai-impl/.../client/HttpApiLlmChatStreamServiceTest.java` | **新建** | 测试 Flux.empty() 返回 |
| 11 | `ai-impl/.../client/SpringAiLlmChatStreamServiceTest.java` | **新建** | 测试 UnsupportedOperationException（chatStream） |
| 12 | `ai-impl/.../client/DelegatingLlmChatServiceTest.java` | **修改** | 适配 Map 构造器；移除 initDelegates 相关测试；新增回退/分发/防御性拷贝测试 |

### 类型详细要求

#### 0. LlmChatRequest（修改）
- 在现有 4 个字段基础上新增 `private final String endpointId` 字段
- 在无参构造器中赋值 `this.endpointId = null`
- 在全参构造器中增加 `@JsonProperty("endpointId") String endpointId` 参数（放在最后）
- 新增 `public String getEndpointId() { return endpointId; }`
- 无参 + 全参构造器同步更新，全参构造器参数顺序：messages, options, clientType, tools, endpointId

#### 1. HttpApiLlmChatService
- **类签名**：`public class HttpApiLlmChatService implements LlmChatService`
- **构造器**：`public HttpApiLlmChatService(CredentialProvider credentialProvider, EndpointRateLimiter endpointRateLimiter)`
- **getClientType()**：返回 `ClientType.HTTP_API`
- **chat(LlmChatRequest request)**：
  1. 从 `request.getEndpointId()` 获取 endpointId（若为 null → 抛 `AiAbilityInputInvalidException("endpointId must not be null")`）
  2. 调用 `credentialProvider.getCredential(endpointId)` → 返回 empty 时抛 `LlmInfrastructureException("No credential for endpoint: " + endpointId)`
  3. 调用 `endpointRateLimiter.tryAcquire(endpointId)` → false 时返回 `AiResult.failure("RATE_LIMITED", "请求被限流")`
  4. 构造 HTTP POST 请求（使用 `java.net.http.HttpClient` 或占位实现），调用封装在 try-catch 中，异常时返回 `AiResult.failure("HTTP_ERROR", exception.getMessage())`
  5. 成功时返回 `CompletableFuture.completedFuture(AiResult.success(response))`
- **structuredChat()**：直接抛出 `StructuredOutputNotSupportedException("structured output not supported yet")`，触发回调方回退到 chat() + StructuredOutputParser.parse() 路径
- **无 @Component/@Service/@Repository 注解**：纯 POJO，由 AiClientConfig @Bean 注册

#### 2. HttpApiLlmChatStreamService
- **类签名**：`public class HttpApiLlmChatStreamService implements LlmChatStreamService`
- **构造器**：同 HttpApiLlmChatService（接受 CredentialProvider + EndpointRateLimiter）
- **chatStream()**：返回 `Flux.empty()`（流式能力为 Phase 6 范围，Phase 5 返回空 Flux 占位）

#### 3. SpringAiLlmChatService
- **类签名**：`public class SpringAiLlmChatService implements LlmChatService`
- **构造器**：无参
- **getClientType()**：返回 `ClientType.SPRING_AI`
- **chat()**：`throw new UnsupportedOperationException("Spring AI not available")`
- **structuredChat()**：同上

#### 4. SpringAiLlmChatStreamService
- **类签名**：`public class SpringAiLlmChatStreamService implements LlmChatStreamService`
- **chatStream()**：`throw new UnsupportedOperationException("Spring AI not available")`

#### 5. DelegatingLlmChatService（重构）
- **变更说明**：
  - 移除 `@Service` 注解（不再自动扫描为 Spring Bean）
  - 移除 `@Primary` 注解（由 AiClientConfig @Bean 方法标记 @Primary）
  - 构造器改为 `public DelegatingLlmChatService(Map<ClientType, LlmChatService> delegates)`
  - 移除 `allServices` 字段和 `initDelegates()` 方法（Map 由调用方直接传入）
  - `delegates` 字段直接赋值：`this.delegates = Collections.unmodifiableMap(new HashMap<>(delegates))`
- **保留行为**：
  - `chat()` / `structuredChat()` 从 `delegates.get(request.getClientType())` 查找
  - null / 无对应 → ERROR 日志 + 回退到 `delegates.get(ClientType.HTTP_API)`
  - `getClientType()` 返回 null

#### 6. AiClientConfig（临时 @Configuration）
- **包路径**：`com.aimedical.modules.ai.impl.client`
- **职责**：Task 10 到 Task 18 之间的临时配置类，统一注册 4 个 LLM 实现 + DelegatingLlmChatService；Task 18 (AiPlatformConfig) 将吸收该配置类
- **内容**：
  ```java
  @Configuration
  public class AiClientConfig {

      @Bean
      HttpApiLlmChatService httpApiLlmChatService(
              CredentialProvider credentialProvider,
              EndpointRateLimiter endpointRateLimiter) {
          return new HttpApiLlmChatService(credentialProvider, endpointRateLimiter);
      }

      @Bean
      HttpApiLlmChatStreamService httpApiLlmChatStreamService(
              CredentialProvider credentialProvider,
              EndpointRateLimiter endpointRateLimiter) {
          return new HttpApiLlmChatStreamService(credentialProvider, endpointRateLimiter);
      }

      @Bean
      SpringAiLlmChatService springAiLlmChatService() {
          return new SpringAiLlmChatService();
      }

      @Bean
      SpringAiLlmChatStreamService springAiLlmChatStreamService() {
          return new SpringAiLlmChatStreamService();
      }

      @Bean
      @Primary
      DelegatingLlmChatService delegatingLlmChatService(
              HttpApiLlmChatService httpApi,
              SpringAiLlmChatService springAi) {
          Map<ClientType, LlmChatService> delegates = new HashMap<>();
          delegates.put(httpApi.getClientType(), httpApi);
          delegates.put(springAi.getClientType(), springAi);
          return new DelegatingLlmChatService(delegates);
      }
  }
  ```

## 选择理由
Task 10 是 Batch3 P1 LLM 调用层的最终实现层，填补 LlmChatService / LlmChatStreamService 接口的具体实现。endpointId 是凭据查询和限流的前置输入，必须在 LlmChatRequest 中传递。DelegatingLlmChatService 重构为 Map 构造器 + AiClientConfig 临时装配，为 Task 18 AiPlatformConfig 统一集中配置做准备。

## 任务上下文

### 设计文档对照
| 设计文档 § | 类型 | 关键约束 |
|-----------|------|---------|
| §3.2.2 | HttpApiLlmChatService | 仅实现 LlmChatService；无 @Component；由 AiPlatformConfig @Bean 注册 |
| §3.2.2 | HttpApiLlmChatStreamService | 仅实现 LlmChatStreamService；流式能力 Phase 6 范围 |
| §3.2.2 | SpringAiLlmChatService | 仅实现 LlmChatService；@ConditionalOnClass 在 @Bean 层级 |
| §3.2.2 | SpringAiLlmChatStreamService | 仅实现 LlmChatStreamService |
| §3.2.2 | LlmChatRequest.endpointId | 凭据查询和限流按 endpointId 执行 |

### 接口形态（Task 10 完成后）
```java
// LlmChatService — 不变
public interface LlmChatService {
    CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest request);
    <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
            LlmChatRequest request, Class<T> targetClass);
    ClientType getClientType();
}

// LlmChatStreamService — 不变
public interface LlmChatStreamService {
    Flux<LlmChatResponse> chatStream(LlmChatRequest request);
}
```

### 已有依赖类型
- `CredentialProvider`：`getCredential(String endpointId)` → `Optional<Credential>`
- `EndpointRateLimiter`：`tryAcquire(String endpointId)` → `boolean`
- `ClientType`：`HTTP_API`, `SPRING_AI`
- `LlmChatRequest`：新增 `getEndpointId()` + 现有 `getMessages()/getOptions()/getClientType()/getTools()`
- `LlmChatResponse`：含 `content`, `usage(LlmChatUsage)`, `modelId`, `retryCount`
- `StructuredChatResult<T>`：含 `data`, `retryCount`, `usage`
- `AiResult<T>`：`AiResult.success(T)` / `AiResult.failure(String code, String message)`
- `StructuredOutputNotSupportedException`：RuntimeException，message + cause 双构造器
- `AiAbilityInputInvalidException`：RuntimeException，message + cause 双构造器
- `LlmInfrastructureException`：RuntimeException，message + cause 双构造器

### 异常处理策略
| 异常场景 | 处理方式 |
|---------|---------|
| `request.getEndpointId() == null` | 抛出 `AiAbilityInputInvalidException("endpointId must not be null")` |
| CredentialProvider 返回 empty | 抛出 `LlmInfrastructureException("No credential for endpoint: " + endpointId)` |
| RateLimiter 限流拒绝 | 返回 `AiResult.failure("RATE_LIMITED", "请求被限流")` |
| HTTP 请求异常 | 捕获异常，返回 `AiResult.failure("HTTP_ERROR", exception.getMessage())` |
| structuredChat 未实现 | 抛出 `StructuredOutputNotSupportedException`，触发回调方回退 |

### 已有代码上下文
已完成前置任务涉及文件清单（全部在 `ai-impl/src/main/java/com/aimedical/modules/ai/impl/` 下）：
- `client/LlmChatService.java` — 接口定义（含 getClientType）
- `client/LlmChatStreamService.java` — 流式接口
- `client/DelegatingLlmChatService.java` — 当前版本（List 注入 + @Service/@Primary），需重构
- `client/CredentialProvider.java` — 凭据查询接口
- `client/DefaultCredentialProvider.java` — 凭据查询默认实现
- `client/EndpointRateLimiter.java` — 端点限流器
- `client/ClientType.java` — 客户端类型枚举（HTTP_API, SPRING_AI）
- `client/LlmChatRequest.java` — 需新增 endpointId 字段
- `client/LlmChatResponse.java` / `StructuredChatResult.java` — DTO 类型
- `client/LlmChatOptions.java` / `LlmChatMessage.java` / `LlmChatMessageRole.java` — DTO 类型
- `client/ChatToolDefinition.java` — DTO 类型
- `client/exception/StructuredOutputNotSupportedException.java`
- `client/exception/LlmInfrastructureException.java`
- `client/exception/AiAbilityInputInvalidException.java`
- `pom.xml` — 已包含 reactor-core 依赖

### 测试规划

#### Test 1: HttpApiLlmChatServiceTest
| 测试方法 | 覆盖路径 | 验证要点 |
|---------|---------|---------|
| `shouldReturnHttpApiClientType` | getClientType() | 返回 `ClientType.HTTP_API` |
| `shouldThrowWhenEndpointIdIsNull` | 凭证获取前校验 | 抛 `AiAbilityInputInvalidException` |
| `shouldThrowWhenCredentialMissing` | 凭证获取返回 empty | 抛 `LlmInfrastructureException` |
| `shouldReturnRateLimitedResult` | 限流拒绝 | `AiResult.failure("RATE_LIMITED", ...)` |
| `shouldReturnHttpErrorResult` | HTTP 调用抛异常 | `AiResult.failure("HTTP_ERROR", ...)` |
| `shouldReturnChatSuccessResult` | 全路径成功 | `AiResult.success(...)`，返回的 response 内容正确 |
| `shouldThrowStructuredOutputNotSupportedException` | structuredChat() | 抛 `StructuredOutputNotSupportedException` |

#### Test 2: SpringAiLlmChatServiceTest
| 测试方法 | 覆盖路径 | 验证要点 |
|---------|---------|---------|
| `shouldReturnSpringAiClientType` | getClientType() | 返回 `ClientType.SPRING_AI` |
| `shouldThrowOnChat` | chat() 调用 | 抛 `UnsupportedOperationException("Spring AI not available")` |
| `shouldThrowOnStructuredChat` | structuredChat() 调用 | 抛 `UnsupportedOperationException` |

#### Test 3: HttpApiLlmChatStreamServiceTest
| 测试方法 | 覆盖路径 | 验证要点 |
|---------|---------|---------|
| `shouldReturnEmptyFlux` | chatStream() | `Flux.empty()` 返回，验证无元素发出 |

#### Test 4: SpringAiLlmChatStreamServiceTest
| 测试方法 | 覆盖路径 | 验证要点 |
|---------|---------|---------|
| `shouldThrowOnChatStream` | chatStream() 调用 | 抛 `UnsupportedOperationException` |

#### Test 5: DelegatingLlmChatServiceTest（修改）
| 测试方法 | 覆盖路径 | 验证要点 |
|---------|---------|---------|
| `shouldConstructWithServiceMap` | Map 构造器 | 构造成功，非 null |
| `shouldReturnNullClientType` | getClientType() | 返回 null |
| `shouldDispatchToCorrectClientType` | chat 分发 | 按 ClientType 转发到正确 delegate |
| `shouldDelegateStructuredChat` | structuredChat 分发 | targetClass 参数透传至正确 delegate |
| `shouldFallbackToHttpApiWhenClientTypeIsNull` | chat null fallback | `getClientType()==null` 时回退到 HTTP_API |
| `shouldFallbackForUnmappedClientType` | chat unmapped fallback | 无对应 delegate 时回退到 HTTP_API |
| `shouldFallbackForUnmappedClientTypeForStructuredChat` | structuredChat unmapped fallback | structuredChat 回退到 HTTP_API |
| `shouldProduceUnmodifiableDelegates` | 防御性拷贝 | 反射读取 delegates field，确认 unmodifiable |
| `shouldPropagateExceptionFromDelegate` | 委托异常传播 | Runtime 异常从 delegate 经 Future 透出 |

## 已有代码上下文

已完成前置任务涉及文件清单（全部在 `ai-impl/src/main/java/com/aimedical/modules/ai/impl/` 下）：
- `client/LlmChatService.java` — 接口定义（含 getClientType）
- `client/LlmChatStreamService.java` — 流式接口
- `client/DelegatingLlmChatService.java` — 分发实现（当前 List 注入 + @Service/@Primary，本任务重构）
- `client/CredentialProvider.java` — 凭据查询接口
- `client/DefaultCredentialProvider.java` — 凭据查询默认实现
- `client/EndpointRateLimiter.java` — 端点限流器
- `client/ClientType.java` — 客户端类型枚举
- `client/LlmChatRequest.java` / `LlmChatResponse.java` / `StructuredChatResult.java` — DTO 类型
- `client/exception/StructuredOutputNotSupportedException.java`
- `client/exception/LlmInfrastructureException.java`
- `client/exception/AiAbilityInputInvalidException.java`
- `pom.xml` — 已包含 reactor-core 依赖

## 修订说明（v13 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] endpointId 来源缺失 — LlmChatRequest 不存在 endpointId 字段 | 在 LlmChatRequest 新增 `endpointId` 字段 + 构造器参数 + getter |
| [严重] DelegatingLlmChatService 改造未纳入任务范围 | 新增涉及文件行：DelegatingLlmChatService 重构为 `Map<ClientType, LlmChatService>` 构造器 + 移除 @Service/@Primary |
| [一般] 新实现无法被 DelegatingLlmChatService 发现 | 新增 AiClientConfig @Configuration 临时装配层，@Bean 注册 4 个实现 + DelegatingLlmChatService（@Primary）；Task 18 AiPlatformConfig 吸收 |
| [一般] endpointId 字段缺失阻塞测试编写 | 补充 endpointId 后 HttpApiLlmChatServiceTest 覆盖凭证获取/限流/HTTP 三大核心路径 + edge cases |

## 修订说明（v13 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] DelegatingLlmChatServiceTest.java 未纳入涉及文件 | 在"全部涉及文件"表增加第 12 行 DelegatingLlmChatServiceTest.java（修改） |
| [一般] 测试规划未覆盖重构后 DelegatingLlmChatService 行为契约 | 在测试规划中新增 Test 5: DelegatingLlmChatServiceTest，覆盖 9 个测试方法（构造/分发/回退/防御性拷贝/异常传播） |
