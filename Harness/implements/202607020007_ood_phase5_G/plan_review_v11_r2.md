# 计划审查报告（v11 r2）

## 审查结果
REJECTED

## 发现

- **[严重] DelegatingLlmChatService 构造器签名导致 Spring 无法自动装配** — 计划要求 DelegatingLlmChatService 标注 `@Primary`，但构造器签名定义为 `Map<ClientType, LlmChatService> delegates`。Spring 容器不支持以枚举（`ClientType`）为键自动注入 `Map`，仅支持 `Map<String, V>`（bean 名称→bean）或 `Map<Class<V>, V>`。若 DelegatingLlmChatService 通过组件扫描（如 `@Service`/`@Component`）被 Spring 管理，该构造器无法被 Spring 满足，启动时抛出 `UnsatisfiedDependencyException`。对比项目中使用 `List<CapabilityExecutor<?,?>>` 再由 `@PostConstruct` 构建内部 Map 的既有模式（`AiOrchestrator.java:57-79`），该设计存在根本性装配缺陷。

- **[一般] DelegatingLlmChatServiceTest 遗漏 structuredChat 分发验证** — 测试规划仅显式提及验证 `chat()` 的正确转发（"验证 chat() 对应转发到正确的实现"），但未提及 `structuredChat(LlmChatRequest, Class<T>)` 的分发验证。DelegatingLlmChatService 实现 LlmChatService 的两个方法，测试规划应明确覆盖两条分发路径，特别是 `targetClass` 参数正确传递的验证。

- **[轻微] 防御性拷贝未明确** — 计划要求 "Collections.unmodifiableMap 防御性封装"，但未指定先执行 `new HashMap<>(delegates)` 防御性拷贝。直接包装构造器入参可能导致外部持有原 Map 引用的代码在包装后仍能修改其内容。

## 修改要求

1. **[严重]** 解决 DelegatingLlmChatService 的 Spring 装配问题。推荐方案：参照 AiOrchestrator 模式，将构造器改为接收 `List<LlmChatService> allServices`，通过新增接口方法（如 LlmChatService.getClientType() 或分离的 ClientTypeResolver）或命名约定在 `@PostConstruct` 中构建 `Map<ClientType, LlmChatService>`。若坚持使用 `Map<ClientType, LlmChatService>` 构造器，则必须将 DelegatingLlmChatService 的 `@Primary` 移至 @Bean 声明，并依赖外部 @Configuration 显式构建 Map。
2. **[一般]** 在测试规划中补充 structuredChat 分发验证用例，包括 targetClass 参数透传的 Mockito verify 断言。
3. **[轻微]** 明确要求构造器中先执行 `new HashMap<>(delegates)` 再包裹 Collections.unmodifiableMap。
