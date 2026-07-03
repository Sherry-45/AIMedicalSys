# 计划审查报告（v3 r1）

## 审查结果
REJECTED

## 发现

- **[严重]** AbstractCapabilityExecutor 构造器引用了当前不存在的 8 个类型，代码无法编译。构造器参数中包含 `PromptTemplateManager`、`ModelRouter`、`LlmChatService`、`StructuredOutputParser`、`AiMetricsCollector`、`ModelEndpointHealthManager`、`LocalRuleFallback`（以上全部在后续任务中才定义），以及字段提取方法中使用的 `AiRequestBase`（Task 16 Batch5）。这些类型在当前代码库中均不存在。计划声称"无前置依赖"与事实矛盾——即使各类型可传 null，它们的 Java 类型本身必须在编译期存在。

- **[严重]** 修复方向不明确。计划及 task_v3.md 仅在行 142 写了一句"暂不依赖（后续任务实现）"，但未说明编译期类型缺失如何解决。可能的修复方案（选一）：(a) 在当前任务中创建空接口桩（stub）放在 ai-api 模块；(b) 将构造器参数类型改为 `Object` 并配合文档说明后续替换；(c) 拆分构造器，将尚未实现的类型参数推迟到子类或 setter 注入。当前计划未做任何选择。

- **[一般]** 计划摘要遗漏多个重要成员方法。`executeStandardPipeline()` 占位方法、`isKnownPhase4BusinessException()` 辅助方法、`refineTimeoutReason()` 方法、`knownPhase4Packages` 静态字段均未在计划中提及。虽然 task_v3.md 包含了这些细节，但计划作为独立规划产物理应覆盖完整成员清单，以便在更高层面评估工作量完整性。

- **[一般]** 计划未提及测试要求。task_v3.md 明确要求 7 类测试覆盖（接口契约、构造器、降级路径、正常路径、防御性拷贝、字段提取、异常识别），但计划中没有任何关于测试策略或测试文件的说明。这导致 Plannner 在规划阶段缺少对测试工作量和风险（如 Mock 策略复杂度）的评估。

## 修改要求

1. **解决编译器类型依赖**（严重）：选择并记录具体方案：
   - 选项 A：在 ai-api 模块创建空接口桩（PromptTemplateManager, ModelRouter, LlmChatService, StructuredOutputParser, AiMetricsCollector, ModelEndpointHealthManager, LocalRuleFallback, AiRequestBase），仅声明接口，无方法体，确保编译通过。这些桩将在对应任务实现时被替换。
   - 选项 B：构造器参数使用 `Object` 类型（最轻量，但丧失类型安全）。
   - 选项 C：构造器暂不包含这 8 个参数，改用子类覆写或 setter 注入方式在后续任务补充。

2. **补充计划摘要**（一般）：在 Task 3 描述中补充 missing 成员：`executeStandardPipeline()` 占位方法、`isKnownPhase4BusinessException()`、`refineTimeoutReason()`、`knownPhase4Packages` 静态字段。

3. **测试规划**（一般）：在计划中明确测试文件路径、测试策略（Mock 框架选择、如何 Mock 降级策略链、超时测试方法），以及预期的测试覆盖率边界。
