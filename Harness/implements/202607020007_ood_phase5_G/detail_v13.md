# 详细设计（v13）

## 概述

在 `ai-impl/.../client/` 包新增 4 个 LLM 调用实现类和 1 个临时装配配置类，覆盖 HTTP API 同步/流式和 Spring AI 存根两种实现；同步修改 `LlmChatRequest` 新增 `endpointId` 字段；重构 `DelegatingLlmChatService` 为 `Map<ClientType, LlmChatService>` 构造器注入、移除 `@Service`/`@Primary`；新增/修改对应测试。

## 文件规划

| # | 文件路径 | 操作 | 职责 |
|---|---------|------|------|
| 1 | `ai-impl/src/main/java/.../client/HttpApiLlmChatService.java` | **新建** | LlmChatService HTTP API 直连同步实现（chat + structuredChat） |
| 2 | `ai-impl/src/main/java/.../client/HttpApiLlmChatStreamService.java` | **新建** | LlmChatStreamService HTTP API 流式实现（chatStream → Flux.empty()） |
| 3 | `ai-impl/src/main/java/.../client/SpringAiLlmChatService.java` | **新建** | LlmChatService Spring AI 同步存根（抛 UnsupportedOperationException） |
| 4 | `ai-impl/src/main/java/.../client/SpringAiLlmChatStreamService.java` | **新建** | LlmChatStreamService Spring AI 流式存根（抛 UnsupportedOperationException） |
| 5 | `ai-impl/src/main/java/.../client/LlmChatRequest.java` | **修改** | 新增 `endpointId` 字段 + getter + 构造器参数 |
| 6 | `ai-impl/src/main/java/.../client/DelegatingLlmChatService.java` | **修改** | 构造器改为 `Map<ClientType, LlmChatService>`；移除 `@Service`/`@Primary`；保留回退逻辑 |
| 7 | `ai-impl/src/main/java/.../client/AiClientConfig.java` | **新建** | 临时 `@Configuration`，注册 4 个实现 + DelegatingLlmChatService（@Bean @Primary） |
| 8 | `ai-api/src/main/java/.../api/AiResult.java` | **修改** | 新增 2 参数 `failure(String errorCode, String fallbackReason)` 工厂方法（跨 ai-api 模块必要前置依赖） |
| 9 | `ai-impl/src/test/java/.../client/HttpApiLlmChatServiceTest.java` | **新建** | 测试凭据缺失/限流拒绝/HTTP 异常/成功/structuredChat 未实现 |
| 10 | `ai-impl/src/test/java/.../client/SpringAiLlmChatServiceTest.java` | **新建** | 测试 UnsupportedOperationException（chat + structuredChat） |
| 11 | `ai-impl/src/test/java/.../client/HttpApiLlmChatStreamServiceTest.java` | **新建** | 测试 Flux.empty() 返回 |
| 12 | `ai-impl/src/test/java/.../client/SpringAiLlmChatStreamServiceTest.java` | **新建** | 测试 UnsupportedOperationException（chatStream） |
| 13 | `ai-impl/src/test/java/.../client/DelegatingLlmChatServiceTest.java` | **修改** | 适配 Map 构造器；移除 initDelegates 相关测试；新增回退/分发/防御性拷贝测试 |

所有文件均在以下基路径下，后续省略：
- 源码：`AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/`
- 测试：`AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/`
- AiResult：`AIMedical/backend/modules/ai/ai-api/src/main/java/com/aimedical/modules/ai/api/`

## 类型定义

### 0. `LlmChatRequest`（修改）

**形态**：class（已有，新增字段）
**包路径**：`com.aimedical.modules.ai.impl.client`
**修改内容**：新增 `endpointId` 字段 + 构造器参数 + getter

```java
// 新增字段
private final String endpointId;

// 无参构造器追加
public LlmChatRequest() {
    // ... 现有 4 个字段置 null ...
    this.endpointId = null;
}

// 全参构造器追加第 5 个参数（放在最后）
public LlmChatRequest(@JsonProperty("messages") List<LlmChatMessage> messages,
                      @JsonProperty("options") LlmChatOptions options,
                      @JsonProperty("clientType") ClientType clientType,
                      @JsonProperty("tools") List<ChatToolDefinition> tools,
                      @JsonProperty("endpointId") String endpointId) {
    // ... 现有 4 个字段赋值 ...
    this.endpointId = endpointId;
}

// 新增 getter
public String getEndpointId() { return endpointId; }
```

### 1. `HttpApiLlmChatService`

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：LlmChatService 的 HTTP API 直连实现，通过 `endpointId` 查询凭据和限流后发起 HTTP POST 请求

```java
package com.aimedical.modules.ai.impl.client;

import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.impl.client.exception.AiAbilityInputInvalidException;
import com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureException;
import com.aimedical.modules.ai.impl.client.exception.StructuredOutputNotSupportedException;
import java.util.concurrent.CompletableFuture;

public class HttpApiLlmChatService implements LlmChatService {

    private final CredentialProvider credentialProvider;
    private final EndpointRateLimiter endpointRateLimiter;

    public HttpApiLlmChatService(CredentialProvider credentialProvider,
                                 EndpointRateLimiter endpointRateLimiter);

    @Override
    public ClientType getClientType();  // → ClientType.HTTP_API

    @Override
    public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest request);

    @Override
    public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
            LlmChatRequest request, Class<T> targetClass);
}
```

**公开接口**：
- `ClientType getClientType()` — 返回 `ClientType.HTTP_API`
- `CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest request)` — 实现逻辑见行为契约
- `<T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(...)` — 直接抛出 `StructuredOutputNotSupportedException`

**构造方式**：`new HttpApiLlmChatService(credentialProvider, endpointRateLimiter)`，由 `AiClientConfig @Bean` 注册

**类型关系**：implements `LlmChatService`

### 2. `HttpApiLlmChatStreamService`

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：LlmChatStreamService 的 HTTP API 流式占位实现，返回 `Flux.empty()`

```java
package com.aimedical.modules.ai.impl.client;

import reactor.core.publisher.Flux;

public class HttpApiLlmChatStreamService implements LlmChatStreamService {

    // 以下两个字段为 Phase 6 流式实现预留，当前 chatStream() 仅返回 Flux.empty()
    private final CredentialProvider credentialProvider;
    private final EndpointRateLimiter endpointRateLimiter;

    public HttpApiLlmChatStreamService(CredentialProvider credentialProvider,
                                       EndpointRateLimiter endpointRateLimiter);

    @Override
    public Flux<LlmChatResponse> chatStream(LlmChatRequest request);  // → Flux.empty()
}
```

**公开接口**：
- `Flux<LlmChatResponse> chatStream(LlmChatRequest request)` — 返回 `Flux.empty()`（流式能力为 Phase 6 范围；CredentialProvider/EndpointRateLimiter 构造器参数为 Phase 6 预留，当前未使用）

**构造方式**：同 HttpApiLlmChatService，由 `AiClientConfig @Bean` 注册

**类型关系**：implements `LlmChatStreamService`

### 3. `SpringAiLlmChatService`

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：LlmChatService 的 Spring AI 存根实现，所有方法抛 `UnsupportedOperationException`

```java
package com.aimedical.modules.ai.impl.client;

import com.aimedical.modules.ai.api.AiResult;
import java.util.concurrent.CompletableFuture;

public class SpringAiLlmChatService implements LlmChatService {

    public SpringAiLlmChatService();  // 无参

    @Override
    public ClientType getClientType();  // → ClientType.SPRING_AI

    @Override
    public CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest request);

    @Override
    public <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
            LlmChatRequest request, Class<T> targetClass);
}
```

**公开接口**：
- `getClientType()` → `ClientType.SPRING_AI`
- `chat()` / `structuredChat()` → `throw new UnsupportedOperationException("Spring AI not available")`

**构造方式**：无参构造器，由 `AiClientConfig @Bean` 注册

### 4. `SpringAiLlmChatStreamService`

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：LlmChatStreamService 的 Spring AI 流式存根

```java
package com.aimedical.modules.ai.impl.client;

import reactor.core.publisher.Flux;

public class SpringAiLlmChatStreamService implements LlmChatStreamService {

    public SpringAiLlmChatStreamService();  // 无参

    @Override
    public Flux<LlmChatResponse> chatStream(LlmChatRequest request);
}
```

**公开接口**：
- `chatStream()` → `throw new UnsupportedOperationException("Spring AI not available")`

### 5. `DelegatingLlmChatService`（重构）

**形态**：class（已有，重构）
**包路径**：`com.aimedical.modules.ai.impl.client`

**变更说明**：
- 移除 `@Service` 注解（不再自动扫描为 Spring Bean）
- 移除 `@Primary` 注解（由 `AiClientConfig @Bean` 方法标记 `@Primary`）
- 移除 `allServices` 字段（旧版 List 注入）
- 移除 `initDelegates()` 方法（`@PostConstruct` 不再需要）
- 构造器改为 `Map<ClientType, LlmChatService>` 注入

```java
package com.aimedical.modules.ai.impl.client;

import com.aimedical.modules.ai.api.AiResult;
import com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DelegatingLlmChatService implements LlmChatService {

    private static final Logger log = LoggerFactory.getLogger(DelegatingLlmChatService.class);

    private final Map<ClientType, LlmChatService> delegates;

    public DelegatingLlmChatService(Map<ClientType, LlmChatService> delegates);
}
```

**公开接口**：
- 构造器 `DelegatingLlmChatService(Map<ClientType, LlmChatService> delegates)` — 内部防御性拷贝：`this.delegates = Collections.unmodifiableMap(new HashMap<>(delegates))`
- `chat(LlmChatRequest request)` — 从 `delegates.get(request.getClientType())` 查找对应实现；null / 无对应 → ERROR 日志 + 回退到 `delegates.get(ClientType.HTTP_API)`；回退仍为 null → 返回 `CompletableFuture.failedFuture(new LlmInfrastructureException("no fallback available"))`
- `structuredChat(LlmChatRequest request, Class<T> targetClass)` — 同上回退逻辑
- `getClientType()` — 返回 `null`

**构造方式**：由 `AiClientConfig @Bean` 创建，不再自动扫描

### 6. `AiClientConfig`

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：Task 10~18 之间的临时配置类，统一注册 4 个 LLM 实现 + DelegatingLlmChatService

```java
package com.aimedical.modules.ai.impl.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class AiClientConfig {

    @Bean
    HttpApiLlmChatService httpApiLlmChatService(
            CredentialProvider credentialProvider,
            EndpointRateLimiter endpointRateLimiter);

    @Bean
    HttpApiLlmChatStreamService httpApiLlmChatStreamService(
            CredentialProvider credentialProvider,
            EndpointRateLimiter endpointRateLimiter);

    @Bean
    SpringAiLlmChatService springAiLlmChatService();

    @Bean
    SpringAiLlmChatStreamService springAiLlmChatStreamService();

    @Bean
    @Primary
    DelegatingLlmChatService delegatingLlmChatService(
            HttpApiLlmChatService httpApi,
            SpringAiLlmChatService springAi);
}
```

**构造方式**：Spring `@Configuration` 自动扫描

### 7. `AiResult`（修改，ai-api 模块）

**形态**：class（已有，新增工厂方法）
**包路径**：`com.aimedical.modules.ai.api`

**修改内容**：新增双参数 `failure` 工厂方法

```java
// 新增
public static <T> AiResult<T> failure(String errorCode, String message) {
    return new AiResult<>(false, null, errorCode, false, message);
}
```

## 错误处理

| 异常场景 | 处理方式 |
|---------|---------|
| `request.getEndpointId() == null` | 抛出 `AiAbilityInputInvalidException("endpointId must not be null")` |
| CredentialProvider 返回 empty | 抛出 `LlmInfrastructureException("No credential for endpoint: " + endpointId)` |
| RateLimiter 限流拒绝 | 返回 `AiResult.failure("RATE_LIMITED", "请求被限流")` |
| HTTP 请求异常 | 捕获异常，返回 `AiResult.failure("HTTP_ERROR", exception.getMessage())` |
| structuredChat 未实现 | 抛出 `StructuredOutputNotSupportedException`，触发回调方回退 |
| Spring AI 未实现 | 抛出 `UnsupportedOperationException("Spring AI not available")` |

**AiResult.failure 双参版本**：`ai-api/.../api/AiResult.java` 新增 `failure(String errorCode, String fallbackReason)` 工厂方法，将 message 存入 `fallbackReason` 字段

## 行为契约

### HttpApiLlmChatService.chat() 执行流程
1. `String endpointId = request.getEndpointId()`
2. 若 `endpointId == null` → `throw new AiAbilityInputInvalidException("endpointId must not be null")`
3. `Optional<Credential> credential = credentialProvider.getCredential(endpointId)`
4. 若 `credential.isEmpty()` → `throw new LlmInfrastructureException("No credential for endpoint: " + endpointId)`
5. `boolean acquired = endpointRateLimiter.tryAcquire(endpointId)`
6. 若 `!acquired` → `return CompletableFuture.completedFuture(AiResult.failure("RATE_LIMITED", "请求被限流"))`
7. 使用 `java.net.http.HttpClient` 构造 HTTP POST 请求（含超时设置）；调用封装在 try-catch 中：
   - 异常时 → `return CompletableFuture.completedFuture(AiResult.failure("HTTP_ERROR", exception.getMessage()))`
   - 成功时 → `return CompletableFuture.completedFuture(AiResult.success(response))`
   - HttpClient 实例在 chat() 方法内 `HttpClient.newHttpClient()` 创建，不注入（为 Phase 6 预留重构空间）

### DelegatingLlmChatService 回退规则
- `chat()` / `structuredChat()` 从 `delegates.get(request.getClientType())` 查找
- 若 `request.getClientType() == null` 或 `delegates.get(ct) == null`：
  - `log.error("未找到 ClientType={} 的实现，回退到 HTTP_API", ct)`
  - `delegate = delegates.get(ClientType.HTTP_API)`
  - 若 `delegate == null`（fallback 不可用）→ 返回 `CompletableFuture.failedFuture(new LlmInfrastructureException("no fallback available"))`
- 防御性拷贝：构造器内 `this.delegates = Collections.unmodifiableMap(new HashMap<>(delegates))`

## 依赖关系

**依赖的已有类型**：
| 类型 | 所在包 | 用途 |
|------|--------|------|
| `LlmChatService` | `client` | 4 个实现类和 DelegatingLlmChatService 实现的接口 |
| `LlmChatStreamService` | `client` | HttpApiLlmChatStreamService / SpringAiLlmChatStreamService 实现的接口 |
| `ClientType` | `client` | HTTP_API / SPRING_AI 枚举 |
| `LlmChatRequest` | `client` | 含 getEndpointId()/getClientType() |
| `LlmChatResponse` | `client` | 返回 DTO |
| `StructuredChatResult<T>` | `client` | structuredChat 返回中泛型类型 |
| `AiResult<T>` | `ai-api` | 统一返回包装类型 |
| `CredentialProvider` | `client` | 凭据查询 |
| `EndpointRateLimiter` | `client` | 端点限流 |
| `AiAbilityInputInvalidException` | `client.exception` | 输入校验异常 |
| `LlmInfrastructureException` | `client.exception` | 基础设施异常 |
| `StructuredOutputNotSupportedException` | `client.exception` | structuredChat 未实现异常 |

**外部依赖**：reactor-core（已在 pom.xml 添加）

**暴露给后续任务的公开接口**：
- 4 个实现 + DelegatingLlmChatService → Task 18 AiPlatformConfig 吸收 AiClientConfig
- AiResult.failure(errorCode, message) → 后续任务直接使用
- DelegatingLlmChatService（@Primary） → CapabilityExecutor 自动装配

## 测试设计

### HttpApiLlmChatServiceTest

依赖 Mock 方式：使用 `Mockito`（项目已有依赖）。Mock `CredentialProvider` 和 `EndpointRateLimiter`。

| 测试方法 | 覆盖路径 | Mock 设置 | 验证要点 |
|---------|---------|-----------|---------|
| `shouldReturnHttpApiClientType` | getClientType() | 无 | 返回 `ClientType.HTTP_API` |
| `shouldThrowWhenEndpointIdIsNull` | chat() endpointId == null | 无 | 抛 `AiAbilityInputInvalidException("endpointId must not be null")` |
| `shouldThrowWhenCredentialMissing` | credentialProvider return empty | credentialProvider.getCredential → Optional.empty() | 抛 `LlmInfrastructureException("No credential for endpoint: ...")` |
| `shouldReturnRateLimitedResult` | RateLimiter 限流拒绝 | tryAcquire → false | `AiResult.failure("RATE_LIMITED", "请求被限流")`，success=false |
| `shouldReturnHttpErrorResult` | HTTP 调用异常 | tryAcquire → true; credential → non-empty; 触发异常 | `AiResult.failure("HTTP_ERROR", ...)`，success=false |
| `shouldReturnChatSuccessResult` | 全路径成功 | 全部正常返回 | `AiResult.success(...)`，success=true |
| `shouldThrowStructuredOutputNotSupportedException` | structuredChat() | 无 | 抛 `StructuredOutputNotSupportedException` |

### SpringAiLlmChatServiceTest
| 测试方法 | 覆盖路径 | 验证要点 |
|---------|---------|---------|
| `shouldReturnSpringAiClientType` | getClientType() | 返回 `ClientType.SPRING_AI` |
| `shouldThrowOnChat` | chat() | 抛 `UnsupportedOperationException("Spring AI not available")` |
| `shouldThrowOnStructuredChat` | structuredChat() | 抛 `UnsupportedOperationException` |

### HttpApiLlmChatStreamServiceTest
| 测试方法 | 覆盖路径 | 验证要点 |
|---------|---------|---------|
| `shouldReturnEmptyFlux` | chatStream() | `Flux.empty()` 返回，StepVerifier 验证完成 |

### SpringAiLlmChatStreamServiceTest
| 测试方法 | 覆盖路径 | 验证要点 |
|---------|---------|---------|
| `shouldThrowOnChatStream` | chatStream() | 抛 `UnsupportedOperationException` |

### DelegatingLlmChatServiceTest（修改）

现有测试 `shouldConstructWithServiceList` → 改为 `shouldConstructWithServiceMap`
现有测试 `shouldReturnNullClientType`（保留）
现有测试 `shouldFallbackToHttpApiWhenClientTypeIsNull`（保留，移除 initDelegates 调用）
现有测试 `shouldFallbackForUnmappedClientType`（保留，移除 initDelegates 调用）
现有测试 `shouldFallbackToHttpApiWhenClientTypeIsNullForStructuredChat`（保留）
现有测试 `shouldFallbackForUnmappedClientTypeForStructuredChat`（保留）
现有测试 `shouldDispatchToCorrectClientType`（适配 Map 构造器）
现有测试 `shouldDelegateStructuredChat`（适配 Map 构造器）
现有测试 `shouldProduceUnmodifiableDelegates`（保留，移除 initDelegates 调用）
现有测试 `shouldNotFailOnEmptyServiceList` → `shouldNotFailOnEmptyMap`
现有测试 `shouldSkipSelfInInitDelegates` → **移除**（initDelegates 不再存在）
现有测试 `shouldLogErrorForMissingClientTypeInInitDelegates` → **移除**（initDelegates 不再存在）
现有测试 `shouldPropagateExceptionFromDelegate`（保留，适配 Map 构造器）

| 新测试方法 | 覆盖路径 | 验证要点 |
|-----------|---------|---------|
| `shouldDispatchToCorrectClientType` | 分发 | request 透传到对应 delegate |
| `shouldPropagateExceptionFromDelegate` | 异常传播 | delegate 抛异常经 Future 透出 |
| `shouldReturnFailedFutureWhenFallbackIsNull` | fallback null | Map 中无 HTTP_API，回退为 null → failedFuture + LlmInfrastructureException |

**LlmChatRequest 构造器同步更新**：所有测试中 `new LlmChatRequest(null, null, null, null)` 的 4 参数调用需更新为 `new LlmChatRequest(null, null, null, null, null)`（5 参数全 null），或改用 `new LlmChatRequest()` 无参构造器。涉及约 8 处调用（分散在分发/回退/异常传播等测试方法中）。

## 修订说明（v13 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] DelegatingLlmChatServiceTest 中 LlmChatRequest 构造器调用未同步更新 — 4 参数构造器不再存在 | 在测试迁移清单末尾新增 `LlmChatRequest 构造器同步更新` 说明，要求所有 `new LlmChatRequest(null, null, null, null)` 更新为 `new LlmChatRequest(null, null, null, null, null)` 或无参构造器 |
| [轻微] DelegatingLlmChatService 回退路径的异常导入缺失 | 在 DelegatingLlmChatService import 列表补充 `import com.aimedical.modules.ai.impl.client.exception.LlmInfrastructureException` |
| [轻微] HttpApiLlmChatService.chat() HTTP 调用方式未明确 | 在行为契约 step 7 中明确选用 `java.net.http.HttpClient`，`HttpClient.newHttpClient()` 在 chat() 方法内创建，不注入 |

## 修订说明（v13 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] DelegatingLlmChatService 回退路径空安全缺失 — fallback 为 null 时直接 NPE | 在 DelegatingLlmChatService 回退规则和类型定义中增加 null 检查：fallback 仍为 null 时返回 `CompletableFuture.failedFuture(new LlmInfrastructureException("no fallback available"))`；测试新增 `shouldReturnFailedFutureWhenFallbackIsNull` |
| [一般] AiResult.failure(String, String) 新增超出任务文件清单 — 设计文件规划 #8 超出 ai-impl 模块范围 | 在设计文件规划表 #8 行备注中标注"（跨 ai-api 模块必要前置依赖）"，澄清此变更为必要的前置依赖，设计确认需纳入 scope |
| [轻微] HttpApiLlmChatStreamService 构造函数引入未使用依赖 — CredentialProvider/EndpointRateLimiter 在当前 chatStream() 中未使用 | 在 HttpApiLlmChatStreamService 类型定义和公开接口说明中添加注释：当前未使用状态为 Phase 6 流式实现预留，有意保留构造器签名 |
| [轻微] 测试规划中 shouldConstructWithServiceMap 重复出现 — 同时出现在"现有测试适配"和"新测试方法"表中 | 从"新测试方法"表中删除 `shouldConstructWithServiceMap` 条目（已在"现有测试适配"中作为重命名处理） |
