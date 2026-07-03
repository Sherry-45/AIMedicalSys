# 计划审查报告（v16 r1）

## 审查结果
REJECTED

## 发现

- **[严重]** 测试文件修改未纳入计划 — 任务文件明确列出 3 个测试文件（`AbstractCapabilityExecutorTest.java` ~16 处、`DiscussionConclusionCapabilityExecutorTest.java` ~3 处、`TriageCapabilityExecutorTest.java` ~1 处）共 18+ 处 `ModelRouter` 的 lambda mock 需从返回 `String` 改为返回 `ModelRoute.of("model-1")`。计划文件（含路线表涉及文件列）均未提及任何测试文件修改。这将导致现有测试编译失败，整轮任务无法通过验证。

- **[一般]** `ModelRoute.of(String modelId)` 便捷工厂方法未纳入 — 任务文件"实施要点"第 1 条明确要求提供此静态工厂方法以向后兼容测试 mock，确保 `return (capId, req) -> ModelRoute.of("model-1")` 模式可编译。计划文件未体现此项契约。

- **[轻微]** 路线表涉及文件列仅列出 3 个文件（`ModelRouter.java`、`DefaultModelRouter.java`、`ModelRoute.java`），未包含需要同步修改的 `AbstractCapabilityExecutor.java` 及 3 个测试文件，不利于任务范围确认。

## 修改要求（仅 REJECTED 时）

1. **测试文件修改纳入计划**: 在路线表涉及文件列中补充 `AbstractCapabilityExecutorTest.java`、`DiscussionConclusionCapabilityExecutorTest.java`、`TriageCapabilityExecutorTest.java`（操作类型标记为"修改"），并在任务描述中注明 18+ 处 mock 更新范围。

2. **补充 ModelRoute.of() 工厂方法**: 在 ModelRoute 值对象定义中明确标注需提供 `public static ModelRoute of(String modelId)` 便捷工厂方法，返回仅 modelId 有效、其余字段为 null/默认值的实例，确保测试 mock 可一行式构造。
