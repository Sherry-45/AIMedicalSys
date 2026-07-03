# 设计审查报告（v8 r7）

## 审查结果
REJECTED

## 发现

- **[一般]** `compressTranscripts()` 内外两层 `future.get()` 使用完全相同超时值 `transcriptSummaryTimeout`，未考虑线程池排队等待时间。`llmCallExecutor` 繁忙时外层 `future.get()` 可能在内部任务启动前即超时。期望修正：外层超时应大于内层 `chat().get()` 超时（如增加队列等待缓冲），或将外层 `get()` 设为 `transcriptSummaryTimeout + queueWaitBuffer`。

- **[一般]** `truncateTranscripts()` 返回 `transcripts.subList(0, cutoffIndex)`（视图），持有原大列表全部元素引用，GC 不可回收；压缩失败回退路径中该视图被设为 request 的 transcripts，直到管线结束才释放。建议改为 `new ArrayList<>(transcripts.subList(0, cutoffIndex))` 创建独立副本。

- **[轻微]** `preciseTokenCount()` 方法名与行为不符——实际为字符数除以 4 的粗略估算，并非精确 token 计数。建议更名为 `estimateTokenCount()` 避免歧义。

- **[轻微]** `refineTimeoutReason()` 中 `capabilityTimeout.toMillis() * 0.2` 的字面量 `0.2` 未抽取为命名常量，降低可维护性。

- **[轻微]** 管线伪代码中使用缩略 `...` 标记 `doDegrade()` 调用参数，关键路径（如结构化成功和 `ExecutionException` 分支）未明确列出 `modelId`/`promptVersion` 等参数来源，存在实现时参数错序风险。建议在伪代码中为每条 `doDegrade()` 调用列出完整实参列表。

## 修改要求（仅 REJECTED 时）

### [一般] compressTranscripts() 超时竞态风险

**问题**：`compressTranscripts()` 中两层 `future.get()` 均使用相同的 `transcriptSummaryTimeout`：
```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(..., llmCallExecutor);
return future.get(transcriptSummaryTimeout.toMillis(), TimeUnit.MILLISECONDS);
```
内部 `chat().get(...)` 也用同一超时。`llmCallExecutor` 是共享线程池，若队列中有其他任务等待，外层 `future.get()` 可能在内部 lambda 启动执行前就超时，导致"假超时"——压缩过程尚未开始即被判定失败。

**期望的修正方向**：外层 `future.get()` 超时应大于内层超时，例如 `transcriptSummaryTimeout.toMillis() + 5000`（5 秒排队缓冲），或使用 `llmCallExecutor.submit()` 后单独管理超时。

### [一般] truncateTranscripts() 内存泄漏风险

**问题**：`truncateTranscripts()` 返回 `transcripts.subList(0, cutoffIndex)`，该视图持有原始 `transcripts` 列表的全部内容引用。当压缩失败触发此回退时，`request.setTranscripts(...)` 设置此视图，即使视图只可见前 `cutoffIndex` 条，原始大列表全部元素仍无法被 GC 回收。

**期望的修正方向**：改为 `new ArrayList<>(transcripts.subList(0, cutoffIndex))` 创建独立副本，确保原大列表可被及时回收。
