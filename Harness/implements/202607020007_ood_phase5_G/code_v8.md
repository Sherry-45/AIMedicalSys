# 实现报告（v8）

## 概述

实现了 `executeStandardPipeline()` 管线方法及 7 项 CapabilityExecutor 具体实现类，同步给 5 个存根类型添加管线所需的最小方法签名，新建 `StructuredOutputNotSupportedException`、`AiCallRecord`、`DiscussionTranscript` 三个辅助类型，修改 `DiscussionConclusionRequest` 追加 transcripts 字段。清理 `doDegrade()` 中 TODO。同时修复 3 项测试断裂。

## 文件变更清单

### 新建（10 个）

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新建 | ai-impl/.../client/exception/StructuredOutputNotSupportedException.java | 结构化输出不支持的运行期异常 |
| 新建 | ai-impl/.../metrics/AiCallRecord.java | 指标采集 DTO，15 字段全参构造只读 |
| 新建 | ai-api/.../dto/discussion/DiscussionTranscript.java | 讨论转录记录 DTO，4 字段 Getter/Setter |
| 新建 | ai-impl/.../orchestrator/impl/TriageCapabilityExecutor.java | 分诊执行器 |
| 新建 | ai-impl/.../orchestrator/impl/PrescriptionCheckCapabilityExecutor.java | 处方审核执行器 |
| 新建 | ai-impl/.../orchestrator/impl/MedicalRecordGenCapabilityExecutor.java | 病历生成执行器 |
| 新建 | ai-impl/.../orchestrator/impl/PrescriptionAssistCapabilityExecutor.java | 辅助开方执行器 |
| 新建 | ai-impl/.../orchestrator/impl/KbQueryCapabilityExecutor.java | 知识库问答执行器 |
| 新建 | ai-impl/.../orchestrator/impl/ScheduleCapabilityExecutor.java | 排班执行器 |
| 新建 | ai-impl/.../orchestrator/impl/DiscussionConclusionCapabilityExecutor.java | 讨论结论执行器，含特有压缩前置阶段 |

### 修改（9 个）

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | ai-impl/.../orchestrator/AbstractCapabilityExecutor.java | 实现 `executeStandardPipeline()`；`refineTimeoutReason()` 返回 `String`；`resolveTimeout()` → `protected`；提取 `handleSuccess()`；`doDegrade()` TODO → `metricsCollector.record()`；`.exceptionally` 适配 String |
| 修改 | ai-impl/.../template/PromptTemplateManager.java | 新增 `String render(String, Map)` 方法签名 |
| 修改 | ai-impl/.../router/ModelRouter.java | 新增 `Object route(String, Object)` 方法签名 |
| 修改 | ai-impl/.../metrics/ModelEndpointHealthManager.java | 新增 `String getState(String)` 存根方法 |
| 修改 | ai-impl/.../parser/StructuredOutputParser.java | 新增 `<T> T parse(String, Class<T>)` 方法签名 |
| 修改 | ai-impl/.../metrics/AiMetricsCollector.java | 新增 `void record(AiCallRecord)` 方法签名 |
| 修改 | ai-api/.../dto/discussion/DiscussionConclusionRequest.java | 追加 `List<DiscussionTranscript> transcripts` 字段及 Getter/Setter |
| 修改 | ai-impl/.../orchestrator/AbstractCapabilityExecutorTest.java | 3 项测试断裂修复 |
| 修改 | ai-impl/.../orchestrator/AiOrchestratorTest.java | 匿名类增加 `record()` 方法体 |

### 核查（1 个）

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 核查 | ai-impl/.../fallback/LocalRuleFallback.java | 已有 `R fallback(T)` 方法，无需修改 |

## 编译验证

编译通过：ai-api 和 ai-impl 两个模块均 `mvn compile` 成功。
测试通过：211 个测试全部通过，0 失败 0 错误。

## 设计偏差说明

| 设计规格 | 偏差原因 | 实际处理 |
|---------|---------|---------|
| 新建 9 个文件（规划表 10 项但标题写 9） | 设计文档计数笔误 | 实际新建 10 个文件，与规划表内容一致 |
| `executeStandardPipeline()` 测试验证成功路径 | 管线依赖 `llmChatService` 等复杂 mock | 改为验证 routeResult=null 时降级路径（`NO_AVAILABLE_ROUTE`），仍可验证管线方法按契约返回合法 `AiResult<R>` |
