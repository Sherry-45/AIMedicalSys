# 设计审查报告（v13 r2）

## 审查结果
REJECTED

## 发现

### [一般] DelegatingLlmChatServiceTest 中 LlmChatRequest 构造器调用未同步更新

**问题**：设计 §0 将 LlmChatRequest 的全参构造器从 4 参数（messages, options, clientType, tools）改为 5 参数（新增 endpointId），原有 4 参数构造器不再存在。但设计 §测试设计 中 DelegatingLlmChatServiceTest 的迁移清单仅说明了 DelegatingLlmChatService 自身的构造器适配（List→Map），未提及测试内 `new LlmChatRequest(null, null, null, null)` 的 8 处调用需要同步更新为 5 参数版本。

**为什么是问题**：照设计直接编码后，DelegatingLlmChatServiceTest 将因找不到 4 参数构造器而编译失败，测试全部不可运行。

**修正方向**：在 DelegatingLlmChatServiceTest 的测试迁移清单中显式说明所有 `new LlmChatRequest(null, null, null, null)` 调用需更新为 `new LlmChatRequest(null, null, null, null, null)` 或改用无参构造器。

### [轻微] DelegatingLlmChatService 回退路径的异常导入缺失

设计的行为契约描述了 `CompletableFuture.failedFuture(new LlmInfrastructureException("no fallback available"))`，但类的 import 列表中未包含 `LlmInfrastructureException`。编码时需补充该导入。

### [轻微] HttpApiLlmChatService.chat() HTTP 调用方式未明确

任务提及"使用 `java.net.http.HttpClient` 或占位实现"，设计仅描述"构造 HTTP POST 请求，调用封装在 try-catch 中"，未明确采用哪种 HTTP 客户端方案。建议在行为契约中明确选型以降低编码不确定性。
