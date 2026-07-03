# 任务指令（v11）

## 动作
NEW

## 任务描述
在 `ai-impl/client/` 包创建以下类型，实现 LLM 调用层的接口与分发层：

### 1. `LlmChatStreamService`（interface，新建）
- **包路径**：`com.aimedical.modules.ai.impl.client`
- 方法签名：`Flux<LlmChatResponse> chatStream(LlmChatRequest request)`
- 职责：大模型流式对话客户端接口，返回 Reactor `Flux` 支持背压控制
- 输入校验约束与 `LlmChatService.chat()` 相同；违反约束时返回 `Flux.error(AiAbilityInputInvalidException)`（异常类位于 `client/exception/` 包，本任务同步新建）
- ⚠️ 因方法签名直接引用 `Flux`，需确保 `reactor-core` 为编译期依赖（当前版本 Phase 5 仅声明接口，不要求实现；流式实现在 Task 10 中的 `HttpApiLlmChatStreamService` / `SpringAiLlmChatStreamService` 中提供）

### 2. `DelegatingLlmChatService`（class，新建）
- **包路径**：`com.aimedical.modules.ai.impl.client`
- **implements**：`LlmChatService`
- 标注 `@Primary`，作为 LlmChatService 接口的统一入口实现
- **分发机制**（参照 AiOrchestrator 模式 —— `AiOrchestrator.java:57-79`）：
  - 构造器接收 `List<LlmChatService> allServices`（Spring 自动注入所有 LlmChatService 实现）
  - `@PostConstruct void initDelegates()` 遍历 `allServices`，跳过自身（`if (service == this) continue`），通过 `service.getClientType()` 获取每个服务对应的客户端类型，构建 `Map<ClientType, LlmChatService> delegates`
  - 防御性封装：先 `new HashMap<>()` 拷贝，再 `Collections.unmodifiableMap()` 包装为不可变 Map
  - `chat(LlmChatRequest)`：从 `request.getClientType()` 获取目标客户端类型，从 `delegates` 查找对应实现并转发
  - `structuredChat(LlmChatRequest, Class<T>)`：同上，从 `delegates` 查找并转发
  - 若 `clientType` 为 null 或 `delegates` 中无对应实现，回退到默认实现（优先 `delegates.get(ClientType.HTTP_API)`），回退时日志级别为 **ERROR**（而非 WARN），确保运维可感知配置错误
- **异常传播契约**：不包装/转换异常；底层实现抛出的任何异常（`StructuredOutputNotSupportedException`、`LlmInfrastructureException`、超时等）原样透传
- **@PostConstruct 校验**：遍历 `ClientType` 枚举值，检查 `delegates` 中是否所有已注册的枚举值均有对应实现；若某种实现缺失（如 YAML 配置了 `SPRING_AI` 但 Spring AI 依赖未引入），在启动期发出 **ERROR** 日志（而非 WARN）并推送告警事件到健康检查体系
- **线程安全**：`delegates` 为不可变 Map 实例变量；底层实现自身需保证线程安全
- **不持有** `CredentialProvider`、`EndpointRateLimiter` 等字段（这些由底层实现持有）

### 3. `AiAbilityInputInvalidException`（class，新建）
- **包路径**：`com.aimedical.modules.ai.impl.client.exception`
- 继承 `RuntimeException`，沿用 `StructuredOutputNotSupportedException` 相同的构造器模式（String message + String message, Throwable cause）
- 语义：AI 能力输入参数校验失败时抛出的异常，表示请求参数违反约束

### 4. `LlmChatService`（interface，修改）
- **新增方法签名**：`ClientType getClientType()` — 使 DelegatingLlmChatService 可在 `@PostConstruct` 中识别每个实现的客户端类型
- 保留现有 `chat()` 和 `structuredChat()` 方法签名不变

## 涉及文件清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新建 | `ai-impl/src/main/java/.../client/LlmChatStreamService.java` | 流式对话接口（Flux 返回） |
| 新建 | `ai-impl/src/main/java/.../client/DelegatingLlmChatService.java` | @Primary 分发层实现 |
| 新建 | `ai-impl/src/main/java/.../client/exception/AiAbilityInputInvalidException.java` | 输入校验异常类 |
| 修改 | `ai-impl/pom.xml` | 添加 reactor-core 依赖（scope compile） |
| 修改 | `ai-impl/src/main/java/.../client/LlmChatService.java` | 新增 `ClientType getClientType()` 方法签名 |
| 新建 | `ai-impl/src/test/java/.../client/LlmChatStreamServiceTest.java` | 接口契约测试 |
| 新建 | `ai-impl/src/test/java/.../client/DelegatingLlmChatServiceTest.java` | 分发行为测试 |

## 测试规划

### `LlmChatStreamServiceTest`（接口契约测试）
- 通过反射验证 `chatStream` 方法签名：名称、参数类型（`LlmChatRequest`）、返回类型（`Flux`）
- 验证 `chatStream` 的泛型返回为 `Flux<LlmChatResponse>`
- 验证接口为 `public interface`
- 验证 `LlmChatStreamService.class` 的 `isInterface()` 为 true
- 不创建 mock/flux 实例——纯反射契约测试，与 `LlmChatServiceTest` 模式一致

### `DelegatingLlmChatServiceTest`（分发行为测试）
- **chat 分发正确性**：构造包含 `HTTP_API` 和 `SPRING_AI` mock 实现的 List，调用 `chat()` 验证根据 `clientType` 转发到正确的 mock 实现（Mockito verify）
- **structuredChat 分发正确性**：同上，验证 `structuredChat(LlmChatRequest, Class<T>)` 对应转发到正确的实现，并通过 `ArgumentCaptor` 断言 `targetClass` 参数正确透传
- **回退逻辑**：`clientType` 为 null 时，验证回退到默认实现（`HTTP_API`）；`clientType` 无对应实现时，验证回退到 `HTTP_API`
- **防御性封装**：`initDelegates()` 后尝试修改 delegates Map 应抛出 `UnsupportedOperationException`
- **异常透传**：底层实现抛出 `RuntimeException` 时，验证透传而非包装
- **@PostConstruct 校验**：调用 `initDelegates()` 后验证 delegates 正确构建，传入不完整 delegates（缺少某 ClientType 实现），通过日志 Appender 验证 ERROR 级别日志输出
- **自跳过**：验证 DelegatingLlmChatService 自身不在 delegates 中（`service == this` 跳过逻辑）

## 选择理由
Task 9 是 Batch3 P1 LLM 调用层的接口与分发层，是 Task 10（HttpApiLlmChatService + SpringAiLlmChatService + 流式实现类）的直接前置依赖。所有前置依赖均已就绪：
- DTO 类型（LlmChatRequest 含 clientType 字段、LlmChatResponse、ClientType 等）— 已完成
- CredentialProvider — 已完成（Task 8）
- EndpointRateLimiter — 已完成（Task 8）
- LlmChatService 接口存根 — 已完成

## 任务上下文
### 设计文档摘要（OOD Phase5_G §3.2）
- `LlmChatStreamService` 职责：封装与大语言模型交互的流式对话能力，返回 Reactor `Flux` 逐字输出。方法：`chatStream(LlmChatRequest) → Flux<LlmChatResponse>`。独立于 `LlmChatService` 以确保非流式代码保持零 Reactor 依赖。
- `DelegatingLlmChatService` 职责：LlmChatService 统一入口，根据 `LlmChatRequest.clientType`（HTTP_API / SPRING_AI）将调用派发至 `delegates` 中对应实现。消除双实现 Bean 装配二义性。CapabilityExecutor 仅依赖 `LlmChatService` 接口。
- 异常传播：不包装，原样透传（CapabilityExecutor 已按异常分类设计）。

### 已有代码上下文
- `LlmChatService.java`：`interface` 含 `chat(LlmChatRequest) → CompletableFuture<AiResult<LlmChatResponse>>` 和 `structuredChat(LlmChatRequest, Class<T>) → CompletableFuture<AiResult<StructuredChatResult<T>>>`
- `LlmChatRequest.java`：含 `getClientType()` 返回 `ClientType`
- `ClientType.java`：enum 含 `HTTP_API`, `SPRING_AI`
- `CredentialProvider.java` / `EndpointRateLimiter.java`：底层实现注入用（当前任务不直接持有）
- `ai-impl/client/exception/` 包已有 `StructuredOutputNotSupportedException`、`LlmInfrastructureException`、`CredentialUnavailableException`，`AiAbilityInputInvalidException` 沿用相同模式

---

## 修订说明（v11 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] AiAbilityInputInvalidException 不存在 | 新增 `AiAbilityInputInvalidException` 异常类到 `ai-impl/client/exception/` 包，更新任务描述 §3 和涉及文件清单 |
| [严重] reactor-core 编译期依赖缺失 | 在涉及文件清单补充 `ai-impl/pom.xml` 修改（添加 reactor-core compile 依赖） |
| [一般] 未提及测试规划 | 新增 `## 测试规划` 章节，分别描述 LlmChatStreamServiceTest 反射契约测试策略和 DelegatingLlmChatServiceTest 分发/回退/防御性封装/@PostConstruct 行为测试 |
| [轻微] DelegatingLlmChatService 关键契约未体现 | 补充防御性封装（`Collections.unmodifiableMap`）、@PostConstruct 校验收发（遍历所有 ClientType 枚举值、缺失时 ERROR 日志）、回退日志级别提升为 ERROR |

---

## 修订说明（v11 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] DelegatingLlmChatService 构造器签名导致 Spring 无法自动装配（Map<ClientType, LlmChatService> 不支持自动注入） | 参照 AiOrchestrator 模式：构造器改为接收 `List<LlmChatService> allServices`；LlmChatService 新增 `ClientType getClientType()` 接口方法；在 `@PostConstruct initDelegates()` 中构建 `Map<ClientType, LlmChatService>`，跳过自身（`service == this`） |
| [一般] DelegatingLlmChatServiceTest 遗漏 structuredChat 分发验证 | 测试规划补充 `structuredChat` 分发正确性验证用例，使用 ArgumentCaptor 断言 targetClass 参数透传 |
| [轻微] 防御性拷贝未明确（未指定先 new HashMap<>() 再 unmodifiableMap） | 分发机制中明确指定：先 `new HashMap<>()` 防御性拷贝，再 `Collections.unmodifiableMap()` 包装为不可变 |
