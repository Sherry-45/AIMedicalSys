# 设计审查报告（v8 r3）

## 审查结果
REJECTED

## 发现

- **[严重]** `executeStandardPipeline()` 方法体第 348 行使用 `instanceof StructuredOutputNotSupportedException`，但该类既不存在于当前项目中，也未在文件规划表中列出。设计注释（第 404-405 行）矛盾地声称"使用 `cause.getClass().getName()` 字符串匹配判断"，而测试描述（第 653 行）也说"通过类名字符串"——代码与注释、测试三者不一致。`instanceof` 要求编译期存在该类型，这将导致编译失败。

- **[一般]** `DiscussionConclusionCapabilityExecutor.compressTranscripts()`（第 602-626 行）向 `llmCallExecutor` 提交 `CompletableFuture.supplyAsync()` 任务，然后阻塞调用 `future.get()`。而 `execute()` 模板方法中 `doExecuteInternal()` 本身也运行在 `llmCallExecutor` 上（`supplyAsync`）。若 `llmCallExecutor` 为单线程或线程数不足，将导致线程池死锁——外层线程阻塞等待内层任务，而内层任务无可用线程执行。任务规范（task_v8.md 第 134 行）虽标记为"暂用 llmCallExecutor 回退"，但设计文档未揭示此风险，也未提供规避措施（如提交前检查可用线程数或独立线程池注入）。

## 修改要求（仅 REJECTED 时）

1. **[严重]** `StructuredOutputNotSupportedException` 的处理必须二选一：(a) 将 `StructuredOutputNotSupportedException` 列入文件规划表（建议放在 `client/exception/` 子包下，继承 `RuntimeException`），代码中的 `instanceof` 即可编译；(b) 改为 `cause.getClass().getName().contains("StructuredOutputNotSupportedException")` 字符串匹配，与注释和测试描述一致。方案 (a) 更健壮，推荐使用。

2. **[一般]** 为 `compressTranscripts()` 引入独立的线程池（如 `transcriptSummaryExecutor`），或在设计文档中明确标注死锁风险并约定 `llmCallExecutor` 必须为多线程池（如 `new ThreadPoolExecutor(2, ...)`），同时添加 `supplyAsync` 前检查可用线程数的防御逻辑。
