# 详细设计（v11）

## 概述

为 `ai-impl/client/` 包新增 3 个类型——`LlmChatStreamService` 接口（流式对话）、`DelegatingLlmChatService` 分发实现（`@Primary`）、`AiAbilityInputInvalidException` 输入校验异常——实现 LLM 调用层的接口与分发层。同步修改 `LlmChatService` 接口（新增 `getClientType()` 方法签名）和 `ai-impl/pom.xml`（添加 reactor-core 编译期依赖）。新增 2 个测试文件覆盖接口契约和分发行为。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/LlmChatStreamService.java` | 新建 | 大模型流式对话客户端接口，返回 `Flux<LlmChatResponse>` |
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/DelegatingLlmChatService.java` | 新建 | `@Primary` LlmChatService 统一入口，按 ClientType 分发至具体实现 |
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/exception/AiAbilityInputInvalidException.java` | 新建 | AI 能力输入校验异常，extends RuntimeException |
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/LlmChatService.java` | 修改 | 新增 `ClientType getClientType()` 方法签名 |
| `AIMedical/backend/modules/ai/ai-impl/pom.xml` | 修改 | 添加 `io.projectreactor:reactor-core` 依赖（scope compile，版本由 Spring Boot BOM 管理） |
| `AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/client/LlmChatStreamServiceTest.java` | 新建 | 反射契约测试，验证 chatStream 方法签名 |
| `AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/client/DelegatingLlmChatServiceTest.java` | 新建 | 分发行为、回退逻辑、防御性封装、@PostConstruct 校验测试 |

## 类型定义

### `LlmChatStreamService`（接口）

**形态**：interface
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：大模型流式对话客户端接口，返回 Reactor `Flux` 支持背压控制；输入校验约束与 `LlmChatService.chat()` 相同

```java
package com.aimedical.modules.ai.impl.client;

import com.aimedical.modules.ai.impl.client.exception.AiAbilityInputInvalidException;
import reactor.core.publisher.Flux;

public interface LlmChatStreamService {
    Flux<LlmChatResponse> chatStream(LlmChatRequest request);
}
```

**公开接口**：
- `Flux<LlmChatResponse> chatStream(LlmChatRequest request)` — 流式对话，返回 Flux 逐字输出；输入参数违反约束时返回 `Flux.error(new AiAbilityInputInvalidException(...))`

**构造方式**：无构造器（interface）
**类型关系**：独立接口，无继承

### `DelegatingLlmChatService`（类）

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：LlmChatService 接口的统一入口实现，标注 `@Primary`；根据 `LlmChatRequest.clientType` 将调用派发至 `delegates` 中对应的实现

```java
package com.aimedical.modules.ai.impl.client;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Primary
public class DelegatingLlmChatService implements LlmChatService {

    private static final Logger log = LoggerFactory.getLogger(DelegatingLlmChatService.class);

    private final List<LlmChatService> allServices;
    private volatile Map<ClientType, LlmChatService> delegates;

    public DelegatingLlmChatService(List<LlmChatService> allServices);

    @PostConstruct
    void initDelegates();
}
```

**公开接口**：
- 构造器 `DelegatingLlmChatService(List<LlmChatService> allServices)` — Spring 自动注入所有 LlmChatService Bean
- `initDelegates()` — `@PostConstruct`，遍历 allServices，跳过自身（`if (service == this) continue`），通过 `service.getClientType()` 获取客户端类型，构建 `Map<ClientType, LlmChatService>`；先 `new HashMap<>()` 防御性拷贝，再 `Collections.unmodifiableMap()` 包装为不可变 Map；遍历 `ClientType` 枚举值，检查所有已注册值均有对应实现，缺失时 ERROR 日志
- `CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest request)` — 实现接口方法，从 `request.getClientType()` 获取目标类型，从 `delegates` 查找对应实现并转发；为 null 或无对应实现时回退到 `delegates.get(ClientType.HTTP_API)`，回退日志级别 ERROR
- `<T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(LlmChatRequest request, Class<T> targetClass)` — 同上，从 `delegates` 查找并转发
- `ClientType getClientType()` — 返回 `null`（DelegatingLlmChatService 自身不绑定特定客户端类型，`initDelegates()` 中通过 `service == this` 跳过）

**构造方式**：`@Service + @Primary` 自动注册，Spring 自动注入构造器参数

**字段说明**：
- `allServices`: `List<LlmChatService>` — 构造器注入的所有 LlmChatService Bean（含自身）
- `delegates`: `volatile Map<ClientType, LlmChatService>` — 初始化后不可变，保证线程安全

**异常传播契约**：不包装/转换异常；底层实现抛出的任何异常（`StructuredOutputNotSupportedException`、`LlmInfrastructureException`、超时等）原样透传

**类型关系**：implements `LlmChatService`

### `AiAbilityInputInvalidException`（类）

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.client.exception`
**职责**：AI 能力输入参数校验失败时抛出的异常，语义为请求参数违反约束

```java
package com.aimedical.modules.ai.impl.client.exception;

public class AiAbilityInputInvalidException extends RuntimeException {
    public AiAbilityInputInvalidException(String message);
    public AiAbilityInputInvalidException(String message, Throwable cause);
}
```

**构造方式**：`new AiAbilityInputInvalidException("clientType must not be null")` / `new AiAbilityInputInvalidException("invalid request", cause)`

**类型关系**：extends `RuntimeException`

### `LlmChatService`（接口，修改）

**形态**：interface（已有，新增方法签名）
**包路径**：`com.aimedical.modules.ai.impl.client`
**修改内容**：新增 `ClientType getClientType()` 方法签名

```java
public interface LlmChatService {
    CompletableFuture<AiResult<LlmChatResponse>> chat(LlmChatRequest request);
    <T> CompletableFuture<AiResult<StructuredChatResult<T>>> structuredChat(
            LlmChatRequest request, Class<T> targetClass);
    ClientType getClientType();
}
```

**已有方法**：保持不变
**新增方法**：
- `ClientType getClientType()` — 返回该实现对应的客户端类型标识；使 `DelegatingLlmChatService` 可在 `@PostConstruct` 中识别每个实现的客户端类型

## 错误处理

| 类型 | 异常/行为 | 传播策略 |
|------|----------|---------|
| 输入参数校验失败 | `AiAbilityInputInvalidException` (RuntimeException) | 由调用方（LlmChatStreamService 实现）包装为 `Flux.error()` 或直接抛出 |
| clientType 为 null 或无对应实现 | 回退到 `HTTP_API`，ERROR 日志 | 不抛出异常，静默降级 |
| 底层实现异常 | 原样透传 | DelegatingLlmChatService 不包装 |
| @PostConstruct 检查缺失实现 | ERROR 日志推送告警 | 不阻止启动，仅日志告警 |

## 行为契约

**DelegatingLlmChatService.initDelegates()**：
1. 遍历 `allServices`，对每个 `service`：
   - `if (service == this) continue` — 跳过自身
   - `ClientType ct = service.getClientType()` — 获取客户端类型
   - `map.put(ct, service)` — 构建映射
2. `this.delegates = Collections.unmodifiableMap(new HashMap<>(map))` — 防御性封装
3. 遍历 `ClientType.values()`，对每个枚举值：
   - 若 `delegates.containsKey(type)` 为 false → `log.error("缺少 ClientType={} 的实现", type)`
4. `this.delegates` 赋值前允许并发读取旧值（volatile 保证可见性）

**DelegatingLlmChatService.chat() / structuredChat() 调转发则**：
1. `ClientType ct = request.getClientType()`
2. `LlmChatService delegate = delegates.get(ct)`
3. 若 `ct == null || delegate == null`：
   - `log.error("未找到 ClientType={} 的实现，回退到 HTTP_API", ct)`
   - `delegate = delegates.get(ClientType.HTTP_API)`
   - 若 `delegate` 仍为 null → 由 Spring 上下文错误导致，等待调用时抛 NPE（当前阶段不可能发生，因为至少有一个 HTTP_API 实现注册）
4. 调用 `delegate.chat(request)` 或 `delegate.structuredChat(request, targetClass)` 并返回

**线程安全**：
- `delegates` 为不可变 Map 实例变量（`volatile` 保证可见性）
- 构造器调用后，`initDelegates()` 在单线程中执行一次
- 底层实现自身需保证线程安全

## 依赖关系

**依赖的已有类型**：
- `LlmChatService` — DelegatingLlmChatService 实现的接口
- `LlmChatRequest` — 含 `getClientType()` 返回 `ClientType`
- `ClientType` (`HTTP_API`, `SPRING_AI`) — 分发键和回退默认值
- `LlmChatResponse` — LlmChatStreamService 返回类型
- `StructuredOutputNotSupportedException` — AiAbilityInputInvalidException 参考其构造器模式
- `AiResult` (`com.aimedical.modules.ai.api.AiResult`) — LlmChatService 返回类型中的泛型参数

**外部依赖**（pom.xml 新增）：
- `io.projectreactor:reactor-core` — scope compile（版本由 Spring Boot BOM 管理，无需指定 version），LlmChatStreamService 方法签名引用 `Flux`

**暴露给后续任务的公开接口**：
- `LlmChatStreamService` 接口 → Task 10 的 `HttpApiLlmChatStreamService` / `SpringAiLlmChatStreamService` 实现
- `DelegatingLlmChatService`（`@Primary`） → CapabilityExecutor 仅依赖 `LlmChatService` 接口，自动装配到 DelegatingLlmChatService
- `AiAbilityInputInvalidException` → LlmChatStreamService 实现和 CapabilityExecutor 校验逻辑中抛出
