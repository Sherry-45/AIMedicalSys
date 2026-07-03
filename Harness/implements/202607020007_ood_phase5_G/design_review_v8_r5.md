# 设计审查报告（v8 r5）

## 审查结果
REJECTED

## 发现

### [严重] DiscussionConclusionCapabilityExecutor 构造器参数数量与任务规约不符，引入未定义 Spring Bean 依赖

**位置**：detail_v8.md:524-582

**问题**：
任务规约（task_v8.md:127）明确要求 DiscussionConclusionCapabilityExecutor 构造器为 **20 参数**（父类 17 + 特有 3），特有参数为 `compressionLightweightEndpoint`、`compressionLightweightClientType`、`transcriptSummaryTimeout`。

设计实际为 **21 参数**（父类 17 + 特有 4），额外引入了第 4 个特有参数 `@Qualifier("transcriptSummaryExecutor") Executor transcriptSummaryExecutor`。

该 Bean 在现有 Spring 上下文中不存在，设计中未提供任何 `@Bean` 定义或创建说明。应用启动时将抛出 `NoSuchBeanDefinitionException`，属于运行期阻塞性缺陷。

任务原文给出明确回退方案（task_v8.md:134）——"暂用 `llmCallExecutor` 回退"，设计未遵守。

**期望修正方向**：
- 删除 `transcriptSummaryExecutor` 参数，在 `compressTranscripts()` 中直接使用父类注入的 `llmCallExecutor`。
- 或在本设计中补充该 Bean 的创建定义（如假设在某 Configuration 类中的 `@Bean` 声明）。
- 参数计数恢复为 20（17+3），与任务规约一致。

### [一般] `transcriptSummaryElapsedMs` 字段归属与可见性矛盾

**位置**：detail_v8.md:555（子类 `private volatile long`）、detail_v8.md:923（父类变更汇总 `protected volatile long`）

**问题**：
- 父类变更汇总表将 `transcriptSummaryElapsedMs` 列为父类新增字段，可见性 `protected`。
- 子类 `DiscussionConclusionCapabilityExecutor` 代码中又用 `private volatile long` 声明同名字段。
- 若父类已有 `protected` 字段，子类的 `private` 声明会遮蔽父类字段，导致两个独立字段共存。此时子类 `doExecuteInternal()` 写入的是子类字段（`private`），而 `refineTimeoutReason()` 的 override 方法中访问的可能解析为父类字段（始终为 0），导致超时原因判断错误。
- 若该字段仅属子类，则父类变更表不应列出该条目。

**期望修正方向**：
- 统一字段归属。推荐方案：`transcriptSummaryElapsedMs` 仅在 `DiscussionConclusionCapabilityExecutor` 中声明（`private volatile`），父类不变更表移除该条目。`refineTimeoutReason()` 在子类中访问自身字段，行为确定。

### [轻微] `promptTemplateManager.render()` 异常时无日志记录

**位置**：detail_v8.md:761-764

**问题**：管线步骤 1 中 `render()` 抛出异常时被 try-catch 静默吞没，未记录任何日志。模板渲染异常是系统级故障（可能因配置错误、模板文件缺失等引起），需运维关注。静默吞没将导致故障排查困难。

**期望修正方向**：catch 块中增加 `log.warn("render failed for key: {}, fallback to raw promptVersion", templateKey, e)`。

### [轻微] `promptVersion` 回退语义异常

**位置**：detail_v8.md:762-764

**问题**：`render()` 失败或返回 null 时，回退使用 `promptVersion` 字符串（如 `"TRIAGE"`）直接作为 LLM 的 SYSTEM prompt 内容。LLM 收到的是原始模板键而非渲染后的指令文本，输出结果完全不可预测。当前方案虽为有意设计、可保系统不中断，但回退值语义错误。

**期望修正方向**：至少补充日志（见上）；同时考虑使用固定兜底提示词（如 `"You are a helpful medical AI assistant. Reply concisely."`）代替模板键字符串。

### [轻微] "shared_success_handler" 用注释标记，存在重复代码风险

**位置**：detail_v8.md:853, 881

**问题**：结构化输出成功路径（步骤 7）和 chat() 回退成功路径均需执行指标采集 + AiResult.success 的相同逻辑。设计以注释 `// → shared_success_handler` 标记而非提取为私有方法，实现时若不提取将导致两处重复代码。

**期望修正方向**：设计阶段明确提取为私有方法，如 `private AiResult<R> handleSuccess(R parsedResult, ...)`，由两处成功路径统一调用。

## 修改要求

综上所述，设计存在 1 项严重缺陷和 1 项一般缺陷，未达到批准标准。

### 必须修正

1. **修正 DiscussionConclusionCapabilityExecutor 构造器参数**：删除或妥善定义 `transcriptSummaryExecutor` 参数，恢复参数计数至 20（17+3），确保不依赖上下文中不存在的 Spring Bean。
2. **统一 `transcriptSummaryElapsedMs` 字段归属与可见性**：消除父类/子类声明矛盾，避免运行时超时原因判断错误。

### 应当修正

无
