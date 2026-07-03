# 测试审查报告（v12 r1）

## 审查结果
**REJECTED**

## 发现

- **[一般]** `LlmChatStreamServiceTest.java:19` — `shouldBePublicInterface` 测试方法名声称验证 `public` 接口，但实际仅检查 `isInterface()`，未使用 `Modifier.isPublic()` 验证访问修饰符。若 `LlmChatStreamService` 被改为包级可见（package-private），该测试仍会通过，但设计规格（`detail_v12.md:33`）声明为 `public interface`。

- **[一般]** `DelegatingLlmChatServiceTest.java` — 未覆盖设计契约（`detail_v12.md:152` 行为契约第3条）中 `initDelegates()` 遍历 `ClientType.values()` 并对缺失枚举值输出 ERROR 日志的行为。该行为是设计规格中明确规定的行为契约，应当通过 LogAppender 注入的方式覆盖。

- **[轻微]** `DelegatingLlmChatServiceTest.java:134` — `shouldPropagateExceptionFromDelegate` 仅测试 `chat()` 的异常透传，未测试 `structuredChat()` 的异常透传路径。代码虽对称但未显式验证。

- **[轻微]** `DelegatingLlmChatServiceTest.java:51-68` — 回退测试（`shouldFallbackToHttpApiWhenClientTypeIsNull`、`shouldFallbackForUnmappedClientType`）仅验证 `chat()` 方法，未验证 `structuredChat()` 的回退路径。

## 修改要求

### 问题1（严重 - LlmChatStreamServiceTest.java:19）
**问题**：`shouldBePublicInterface` 使用 `assertTrue(LlmChatStreamService.class.isInterface())` 断言，该方法仅判断是否为接口，无法区分 `public` 与 package-private 接口。
**期望修正**：改用 `assertTrue(Modifier.isPublic(LlmChatStreamService.class.getModifiers()))` 验证 public 修饰符；或在测试方法名中明确其为接口检查（如 `shouldBeInterface`）并在方法体内新增单独的 `shouldBePublic` 测试方法使用 `Modifier.isPublic` 验证。

### 问题2（严重 - DelegatingLlmChatServiceTest.java）
**问题**：设计契约 `detail_v12.md` 行为契约第3条要求 `initDelegates()` 遍历 `ClientType.values()` 并检查是否有未注册的枚举值，缺失时输出 ERROR 日志。该行为没有任何测试覆盖。
**期望修正**：使用 Logback/Log4j TestAppender（或 SLF4J 测试框架）注入 Logger，创建仅部分 ClientType 有实现的场景，验证缺失枚举值对应的 ERROR 日志确实被记录。
