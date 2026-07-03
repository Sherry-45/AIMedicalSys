# 设计审查报告（v8 r8）

## 审查结果
REJECTED

## 发现

### **[严重] ModelEndpointHealthManager.getState() null 行为与规格矛盾**

- **位置**：detail_v8.md L253（规格）与 L786-790（管线代码）
- **问题**：规格（L253）写明 `getState()` 返回 `"HEALTHY"` 表示健康，**null 表示不可用**。但管线中的条件判断为 `if (healthState != null && !"HEALTHY".equals(healthState))`，即 null 时**不进入降级**，与规格直接对立。
- **影响**：当 `getState()` 返回 null（存根实现即如此），管线错误地通过健康检查继续调用 LLM，而非降级为 `ENDPOINT_UNAVAILABLE`。如果实际 `ModelEndpointHealthManager` 实现也遵循"null 不可用"约定，此缺陷会导致生产环境调用不可用端点。
- **期望修正**：二选一：
  - A）修正管线代码，将条件改为 `if (healthState == null || !"HEALTHY".equals(healthState))`，使 null 也触发的降级；
  - B）修正规格说明，明确 null 表示"状态未知，允许通过"。

### **[一般] DiscussionConclusionCapabilityExecutor 压缩阶段复用 llmCallExecutor 存在死锁风险**

- **位置**：detail_v8.md L676-690（`compressTranscripts()`），L712（行为契约 #7）
- **问题**：`doExecuteInternal()` 本身已在 `llmCallExecutor` 线程池上执行（见 `AbstractCapabilityExecutor.execute()` L137: `CompletableFuture.supplyAsync(() -> doExecuteInternal(...), llmCallExecutor)`）。`DiscussionConclusionCapabilityExecutor` 的压缩阶段通过 `CompletableFuture.supplyAsync(() -> {...}, llmCallExecutor)` 向**同一个**线程池提交新任务。若线程池为单线程（如 `Executors.newSingleThreadExecutor()`），内部任务将永远无法启动，导致死锁。
- **影响**：当 `llmCallExecutor` 并发度不足时，`DiscussionConclusionCapabilityExecutor` 的压缩阶段将永远阻塞，且外层 `orTimeout` 超时后返回 `TIMEOUT` 降级。生产环境可能间歇性触发此问题。
- **期望修正**：设计文档中需明确标注此风险，并补充文档约束——要求 `llmCallExecutor` 必须为多线程池（最小线程数≥2），或改用独立线程池执行压缩任务（如新增 `transcriptSummaryExecutor` 参数，但需与任务规约的 20 参数约束权衡）。
