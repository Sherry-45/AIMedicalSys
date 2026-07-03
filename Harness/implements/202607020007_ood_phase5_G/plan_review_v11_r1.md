# 计划审查报告（v11 r1）

## 审查结果
REJECTED

## 发现

- **[严重] AiAbilityInputInvalidException 不存在** — Task v11 要求 LlmChatStreamService "违反约束时返回 `Flux.error(AiAbilityInputInvalidException)`"（第13行），但该异常类在整个代码库中不存在。计划未提及需要创建此异常，也未指定其包路径。若不处理，LlmChatStreamService 无法编译。

- **[严重] reactor-core 编译期依赖缺失** — Task v11 明确警告（第14行）"需确保 reactor-core 为编译期依赖"，且 `Flux<LlmChatResponse>` 返回值直接引用 Reactor 类型。当前 ai-impl/pom.xml 未声明 reactor-core，spring-boot-starter-web 不传递依赖 reactor-core。计划未提及 pom.xml 修改。

- **[一般] 未提及测试规划** — Task v11 涉及文件清单（第42-43行）要求新建 `LlmChatStreamServiceTest.java` 和 `DelegatingLlmChatServiceTest.java`。计划未包含任何测试策略或用例覆盖说明。

- **[轻微] DelegatingLlmChatService 关键契约未在计划中体现** — Task 要求 `delegates` 使用 `Collections.unmodifiableMap` 防御性封装、@PostConstruct 校验全部 ClientType 均有实现、备选默认回退机制。计划摘要过于笼统，缺少这些关键约束。

## 修改要求

1. **[严重]** 在计划中明确 AiAbilityInputInvalidException 的创建位置（建议 ai-api 或 ai-impl/client/exception/ 包），并在涉及文件清单中补充。
2. **[严重]** 在涉及文件清单中补充 ai-impl/pom.xml（添加 reactor-core scope compile），并在计划中注明。
3. **[一般]** 补充测试规划，至少说明 LlmChatStreamService 接口契约测试（反射验证 + Flux 类型）和 DelegatingLlmChatService 分发/回退/防御性封装行为测试。
