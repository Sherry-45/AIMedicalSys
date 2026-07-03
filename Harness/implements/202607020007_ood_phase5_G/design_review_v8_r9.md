# 设计审查报告（v8 r9）

## 审查结果
APPROVED

## 发现

### 已验证的合规项

所有来自 requirement.md 和 task_v8.md 的要求均已在设计中覆盖：

1. **`executeStandardPipeline()` 管线实现** — 步骤完整（模板渲染 → 模型路由 → 健康检查 → LLM structuredChat → 结构化解析 → 指标采集），含 structuredChat 失败后 chat()+parse() 回退路径
2. **7 个 CapabilityExecutor 实现类** — 每个均标注 `@Service("capabilityId")`、17 参构造器 + `super(...)`、`getCapabilityId/getInputType/getOutputType`、`doExecuteInternal()` 委托至 `executeStandardPipeline()`
3. **DiscussionConclusionCapabilityExecutor** — 特有 3 参数（20 参数构造器）、前置压缩阶段、`refineTimeoutReason()` 重写、`estimateTokenCount/formatTranscripts/truncateTranscripts` 辅助方法
4. **5 个存根类型方法签名** — `PromptTemplateManager.render()`、`ModelRouter.route()`、`ModelEndpointHealthManager.getState()`、`StructuredOutputParser.parse()`、`AiMetricsCollector.record()`
5. **`doDegrade()` TODO 清理** — 替换为 `metricsCollector.record(new AiCallRecord(...))` 实际调用
6. **新建 DTO** — `StructuredOutputNotSupportedException`、`AiCallRecord`、`DiscussionTranscript` 结构完整
7. **`DiscussionConclusionRequest` 修改** — 追加 `List<DiscussionTranscript> transcripts` 字段
8. **`AbstractCapabilityExecutor` 变更** — `resolveTimeout()` 改 `protected`、`refineTimeoutReason()` 返回类型改为 `String`、提取 `handleSuccess()` 消除重复代码
9. **测试修改计划** — 覆盖 3 项已知断裂修复

### 代码交叉验证

- `extractUserId()` — 已存在于 `AbstractCapabilityExecutor.java:334` ✓
- `isKnownPhase4BusinessException()` — 已存在于 `AbstractCapabilityExecutor.java:324` ✓
- `StructuredChatResult<T>` — `getData()` 返回 T、`getUsage()` 返回 `LlmChatUsage` ✓
- `LlmChatService.structuredChat()` — 签名匹配 `(LlmChatRequest, Class<T>)` ✓
- `ClientType` 枚举 — 已存在于 `client/ClientType.java` ✓
- 全部 7 组 Request/Response DTO — 在 `ai-api/dto/` 下已验证存在 ✓
- `ModelEndpointHealthManager` — 当前为空类，设计新增 `getState()` 存根 ✓
- `AiCallRecord` / `StructuredOutputNotSupportedException` / `DiscussionTranscript` — 尚不存在，需新建 ✓

### 未发现问题

未发现达到 **严重** 或 **一般** 级别的缺陷。设计在以下方面表现出色：

- 修订历史详尽（v8 r4→r8），每轮审查意见均得到闭环处理
- 管线中 `inner catch (ExecutionException)` 抛出 `CompletionException` 的行为与父类 `execute()` 的 `.exceptionally()` 回调形成闭环
- DiscussionConclusion 压缩阶段的死锁风险已明确文档化（约束：`llmCallExecutor` 必须为多线程池）
- 所有 `doDegrade()` 调用均列出完整 15 参数实参列表，消除实现时参数错序风险
- 超时分配策略符合 task_v8 要求：优先 `parseTimeoutConfig` → `parseTimeoutDefault` → 兜底 60/40 比例
- `handleSuccess()` 中 `metricsCollector.record()` 调用已添加 `if (metricsCollector != null)` null-safe 保护

## 结论

设计完整覆盖所有任务要求，类型引用与现有代码库一致，错误处理路径完整，无严重或一般缺陷。

**APPROVED**
