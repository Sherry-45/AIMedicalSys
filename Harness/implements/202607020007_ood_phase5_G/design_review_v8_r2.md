# 设计审查报告（v8 r2）

## 审查结果
REJECTED

## 发现

### **[严重] DiscussionConclusionRequest 缺少 transcripts 数据，DiscussionTranscript 类型不存在**

`DiscussionConclusionCapabilityExecutor.doExecuteInternal()` 中调用了 `extractTranscripts(request)`（返回 `List<DiscussionTranscript>`）和 `updateRequestWithCompressed(request, compressed)`。但实际代码库中：

- `DiscussionConclusionRequest` 是空类，无字段无方法（`discussion/DiscussionConclusionRequest.java:3-7`）
- `DiscussionTranscript` 类不存在于代码库中
- `extractTranscripts()` 和 `updateRequestWithCompressed()` 方法在设计中没有定义

设计注（第 548 行）提到「若不存在，则在内部引用时通过 `Object` 处理」，但伪代码仍直接使用了 `List<DiscussionTranscript>` 类型签名，无法编译。设计必须给出具体的替代方案（如：在 `DiscussionConclusionCapabilityExecutor` 内部定义 `DiscussionTranscript` 静态类、修改 `DiscussionConclusionRequest` 添加 transcripts 字段、或基于 `Map`/`Object` 的类型擦除方案）。

### **[严重] DiscussionConclusion refineTimeoutReason() 超时区分未解决**

任务要求（task_v8.md 第 136 行）：`refineTimeoutReason()` 在压缩侵占超时窗口时应返回包含 `:transcriptSummaryCrowding` 后缀的 reason，否则为 `:primaryLlmTimeout`。

但 `refineTimeoutReason()` 返回 `DegradationReason` 枚举，Java 不支持枚举与字符串拼接。设计第 523-524 行承认此问题但仅说「由 `execute()` 中的异常处理代码处理」，未给出具体方案。`AbstractCapabilityExecutor.execute()` 第 167-170 行的 `exceptionally` 处理器是调用方，但设计未指出需要修改该处代码。超时区分功能实际上无法实现。

### **[一般] ModelEndpointHealthManager.getState() 签名与任务规范不一致**

任务规范（task_v8.md 第 150 行）要求新增 `getState(String)` 方法签名。设计改为 `getState(ModelRoute route)`。设计修订说明认为使用 `ModelRoute` 是「有意的设计细化」，但作为独立审查，该改动偏离了任务明确的接口签名要求。如果接受此偏离，需在 task_v8.md 中同步更新。

### **[一般] compressTranscripts() 将端点 URL 作为 modelId 传入 LlmChatOptions**

`compressTranscripts()`（第 530 行）：
```java
LlmChatOptions options = new LlmChatOptions(compressionLightweightEndpoint, null, null, null, null, null, null);
```
`compressionLightweightEndpoint` 语义上是端点 URL，却被传递给 `modelId` 参数。`ModelRoute` 类型中 `modelId` 与 `endpoint` 是分开的字段，说明系统区分两者。此设计可能导致 `llmChatService.chat()` 无法正确路由请求。

### **[一般] doExecuteInternal() 中 extractTranscripts/updateRequestWithCompressed 未定义**

即使不考虑缺失类型的问题，`doExecuteInternal()` 中引用的两个辅助方法 `extractTranscripts(DiscussionConclusionRequest)` 和 `updateRequestWithCompressed(DiscussionConclusionRequest, String)` 在所有列出的私有辅助方法中均未定义（第 472-506 行仅定义了 `preciseTokenCount`、`formatTranscripts`、`truncateTranscripts`、`compressTranscripts`）。

### **[轻微] 管线步骤 7 存在死代码**

第 259-260 行：
```
modelId = aiResult.getData().getUsage() != null
    ? chatRequest.getOptions().getModelId() : null
```
该局部变量 `modelId` 被赋值但未使用（第 262-265 行的 `AiCallRecord` 直接使用 `options.getModelId()`）。建议删除。

### **[轻微] refineTimeoutReason 的测试计划不可行**

测试计划要求验证 `refineTimeoutReason()` 的超时原因细化，但由于该功能在设计层面未解决（见严重问题 2），对应的测试设计不完整。

## 修改要求（仅 REJECTED 时）

存在 2 个严重问题，必须修复后方可通过：

1. **DiscussionConclusionRequest/Transcript 数据模型缺失**：确定 transcripts 数据的存储方式，定义所需的类型和提取/更新方法，确保 `doExecuteInternal()` 代码可编译。

2. **refineTimeoutReason 超时区分**：给出具体方案，例如：将 `execute()` 中第 167 行的 `refineTimeoutReason()` 返回值拼接后缀后传给 `doDegrade()`；或者将 `doDegrade()` 的 `degradeReason` 参数改为支持后缀拼接。
