# 设计审查报告（v13 r3）

## 审查结果
APPROVED

## 发现

### [轻微] 文件 #8 路径占位符 `...` 跨模块展开规则不明确

**位置**：文件规划表 #8 `ai-api/src/main/java/.../api/AiResult.java`

**描述**：设计在基路径说明中为 `AiResult` 单独列出了基路径 `AIMedical/backend/modules/ai/ai-api/src/main/java/com/aimedical/modules/ai/api/`，但文件 #8 的路径 `ai-api/src/main/java/.../api/AiResult.java` 使用了 `...` 占位符。源码模块（ai-impl）的 `...` 展开规则为 `com/aimedical/modules/ai/impl/`（含 `impl` 段），而 AiResult 位于 `com.aimedical.modules.ai.api` 包（不含 `impl`）。`...` 占位符在跨模块文件上的展开惯例未明确定义，存在轻微解读歧义。

**建议**：将文件 #8 的路径写为完整形式 `ai-api/src/main/java/com/aimedical/modules/ai/api/AiResult.java`，避免依赖 `...` 占位符的推断，或在基路径说明中明确每个基路径对应的 `...` 展开包路径。

---

### [轻微] DelegatingLlmChatService 回退 null 路径缺少 fallback 前日志

**位置**：行为契约 — DelegatingLlmChatService 回退规则

**描述**：当 `delegates.get(ClientType.HTTP_API)` 也为 null 时，设计返回 `CompletableFuture.failedFuture(new LlmInfrastructureException("no fallback available"))`。但此路径下没有 `log.error()` 输出日志。虽然在 earlier `ct == null || delegate == null` 时已有一条 ERROR 日志记录了回退动作，但该日志不区分"回退成功"和"回退也失败"。建议在返回 failedFuture 之前增加一条明确日志，降低排障难度。

**建议**：在 failedFuture 前追加 `log.error("回退不可用：HTTP_API 未注册")`。

---

## 总体评价

设计完整覆盖了 task_v13.md 列出的 12 个涉及文件以及必要的第 13 个文件（AiResult 双参数 failure 工厂方法，已验证现有代码中 `AiResult.failure(String)` 仅单参数版本，双参数版本必须新增），所有行为契约与任务规范一致，测试规划完整。未发现 **[严重]** 或 **[一般]** 级别问题。
