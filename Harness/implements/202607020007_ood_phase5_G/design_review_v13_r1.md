# 设计审查报告（v13 r1）

## 审查结果
REJECTED

## 发现

### **[一般]** DelegatingLlmChatService 回退路径空安全缺失

**问题**：设计 §5 保留现状回退逻辑：`delegate = delegates.get(ClientType.HTTP_API)` 后再直接 `delegate.chat(request)`（对应现有 `DelegatingLlmChatService.java:52`）。若 Map 中不存在 `HTTP_API` 对应实现，`delegate` 为 `null`，调用 `delegate.chat()` 抛出 NPE。

**为什么是问题**：AiClientConfig（§6）仅向 Map 注册 HttpApiLlmChatService 和 SpringAiLlmChatService。若这两个 Bean 的装配因配置错误缺失其一，DelegatingLlmChatService 在回退路径上会 NPE 而非给出可读错误。测试设计也未覆盖此场景。

**修正方向**：在 `delegates.get(ClientType.HTTP_API)` 后增加 null 检查，若 fallback 也为 null 则返回 `CompletableFuture.failedFuture(new LlmInfrastructureException("no fallback available"))` 或类似有意义的失败结果。同时测试应新增对应 case。

### **[一般]** AiResult.failure(String, String) 新增超出任务文件清单

**问题**：设计文件规划（#8）将 `ai-api/.../api/AiResult.java` 列为修改文件，新增 `failure(String errorCode, String fallbackReason)` 工厂方法。但任务指令 v13 的"全部涉及文件"表（12 项）未包含该文件。

**为什么是问题**：经核查现有 `AiResult.java`（75 行）确实缺少该双参 `failure` 方法，设计发现此依赖是正确的。但任务 scope 未授权跨 `ai-api` 模块修改，且设计文档未以任何方式标注此范围外变更。

**修正方向**：在设计的文件规划表或备注中明确标注 AiResult.java 变更为必要的前置依赖，或与任务制作者协调确认是否将其纳入 scope。

### **[轻微]** HttpApiLlmChatStreamService 构造函数引入未使用依赖

**问题**：§2 `HttpApiLlmChatStreamService` 构造器接受 `CredentialProvider` 和 `EndpointRateLimiter`，但 `chatStream()` 仅返回 `Flux.empty()`，两依赖均未使用。

**为什么是问题**：按任务要求（"构造器：同 HttpApiLlmChatService"）确需保留此签名，但未使用的注入是代码坏味，可能误导后续维护者。

**修正方向**：在设计中添加注释说明此构造器签名为 Phase 6 流式实现预留，明确当前未使用状态是有意为之。

### **[轻微]** 测试规划中 shouldConstructWithServiceMap 重复出现

**问题**：测试设计 §5 "现有测试适配" 中将 `shouldConstructWithServiceList` 改为 `shouldConstructWithServiceMap`，随后"新测试"表中又出现同名 `shouldConstructWithServiceMap`，未澄清是同一测试的重命名还是新增。

**为什么是问题**：可能导致编码阶段产生重复的测试方法定义。

**修正方向**：在"新测试"表中删除重复条目，或在"现有测试适配"中直接列出完整映射关系后不再重复。
