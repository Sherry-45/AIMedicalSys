# 设计审查报告（v3 r4）

## 审查结果
REJECTED

## 发现

### **[严重]** `llmCallExecutor` 在 supplyAsync 调用中未定义

在 `execute()` 模板方法的 `CompletableFuture.supplyAsync(() -> { ... }, llmCallExecutor)` 中使用了 `llmCallExecutor` 作为线程池参数，但该类中未声明此字段，构造器参数中也未包含，亦非局部变量。该代码片段将导致编译错误。

**修正方向**：
- 在 `AbstractCapabilityExecutor` 中添加 `protected final Executor llmCallExecutor` 字段
- 在 16 参数构造器中增加 `Executor llmCallExecutor` 参数
- 同步更新字段列表、构造器签名及相关文档

### **[一般]** doExtractDepartmentId/doExtractVisitId/doExtractPatientId/doExtractSessionId 默认行为与任务描述不一致

任务要求四个 `doExtract*` 方法的默认实现使用 `instanceof AiRequestBase` + cast 模式提取字段值；但设计当前全部返回 null，并在注释中标注为"当前批次；待 AiRequestBase 补齐 getter 后升级"。然而 AiRequestBase 存根是空抽象类（无任何 getter 方法），因此按任务描述的 instanceof + cast 模式无法编译，按设计的 null 返回又与任务描述不符——两者自相矛盾。

**修正方向**（二选一）：
- 选项 A：为 `AiRequestBase` 存根添加 `getDepartmentId()`/`getVisitId()`/`getPatientId()`/`getSessionId()` 四个 getter 方法（作为抽象方法或返回 null 的具体空实现），使 `doExtract*` 可按任务描述的 instanceof + cast 模式实现
- 选项 B：修改任务描述，正式确认当前批次 `doExtract*` 方法返回 null 是可接受行为，将 instanceof + cast 模式延迟到后续批次

## 修改要求

1. **[严重]** `llmCallExecutor` 字段缺失 → 添加字段 + 构造器参数，确保编译通过
2. **[一般]** 四个 `doExtract*` 方法与任务描述冲突 → 二选一方案，需与任务描述方协调明确
