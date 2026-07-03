# 计划审查报告（v8 r1）

## 审查结果
REJECTED

## 发现

### [严重] 存根接口缺失管线所需方法签名，将导致编译失败

`executeStandardPipeline()` 管线伪代码（task_v8.md §48-69）调用了以下存根方法，但这些方法在当前存根接口/类上不存在：

| 管线步骤 | 调用 | 所在类型 | 当前状态 |
|---------|------|---------|---------|
| 步骤 3 | `promptTemplateManager.render(templateKey, variables)` | `PromptTemplateManager` | 空接口，无方法 |
| 步骤 4 | `modelRouter.route(capabilityId, request)` | `ModelRouter` | 空接口，无方法 |
| 步骤 5 | `endpointHealthManager.getState(endpointId)` | `ModelEndpointHealthManager` | 仅有空构造器，无方法 |
| 步骤 9-10 | `structuredOutputParser.parse(content, targetClass)` | `StructuredOutputParser` | 空接口，无方法 |
| 步骤 12 | `metricsCollector.record(...)` | `AiMetricsCollector` | 空接口，无方法 |

此外，步骤 1 调用 `ExperimentManager` 进行实验分流，但 `AbstractCapabilityExecutor` 中没有 `ExperimentManager` 字段，且该类型在 Batch 4（task 13）尚未实现。

**为什么是问题**：管线方法体引用不存在的方法，编译直接失败。存根在 R3 时是为解决编译问题创建的，但当前任务未包含"给存根添加必要方法签名"这一步骤。

**修正方向**：在实施 `executeStandardPipeline()` 之前或作为同一批次的一部分，给以下类型添加最小方法签名（返回类型可用简单默认值，如 `null`/空对象）：
- `PromptTemplateManager.render(String templateKey, Map<String, Object> variables)` → `String`
- `ModelRouter.route(String capabilityId, Object request)` → `ModelRoute`
- `ModelEndpointHealthManager.getState(String endpointId)` → 枚举或 `String`（如 `"HEALTHY"`）
- `StructuredOutputParser.parse(String content, Class<T> targetClass)` → `T`
- `AiMetricsCollector.record(AiCallRecord record)` → `void`（或合适签名）
- 如有必要，新增 `ExperimentManager` 存根接口并加入 `AbstractCapabilityExecutor` 构造器

### [一般] 管线伪代码步骤 1（实验分流）与现有类结构不一致

`executeStandardPipeline()` 方法签名已接收 `promptVersion` 和 `sentinelReason` 作为参数，但伪代码步骤 1-2 要求"实验分流 → 提取 promptVersion/sentinelReason"，存在矛盾：

1. 若实验分流发生在管线之外（即 `doExecuteInternal()` 在调用 `executeStandardPipeline()` 前已完成分流），则管线内不应重复执行
2. 若实验分流是管线的一部分，则需要 `ExperimentManager` 依赖，但当前类中没有

**为什么是问题**：伪代码与类设计不一致，会导致实现时产生歧义。

**修正方向**：明确实验分流的归属：
- 选项 A：从管线伪代码中移除步骤 1-2（说明已由 `doExecuteInternal()` 传入确定值）
- 选项 B：若实验分流确是管线的一部分，则在 `AbstractCapabilityExecutor` 中添加 `ExperimentManager` 字段并给构造器增加对应参数

## 修改要求
对每个严重或一般问题：问题是什么、为什么是问题、期望的修正方向
