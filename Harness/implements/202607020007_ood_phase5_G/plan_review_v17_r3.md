# 计划审查报告（v17 r3）

## 审查结果
REJECTED

## 发现

- **[一般]** task_v17.md "预期文件清单"表缺失 3 个需修改的测试文件。`AbstractCapabilityExecutorTest.java`、`DiscussionConclusionCapabilityExecutorTest.java`、`TriageCapabilityExecutorTest.java` 虽然在"调用方 mock 更新范围"中有详细描述，但未出现在 formal 文件清单表（row 7-11）中。v17 r2 审查已修复 plan.md 路线表，但 task_v17.md 文件清单表遗漏相同问题。文件清单表是 implementer 的主要工作依据，不完整会引入漏改风险。

- **[轻微]** TemplateChangedEvent 中引用的 `ChangeType` 枚举未在计划中定义来源。字段类型 `ChangeType`（含 CREATED/UPDATED/DELETED/STATUS_CHANGED）出现在事件类定义中（task_v17.md:108），但未说明是内嵌枚举、独立文件还是已存在类型。虽然不影响编译正确性（常见做法是内嵌枚举），但计划应明确以避免实现不一致。

- **[轻微]** `AiImplPomCleanDependencyTest.java` 测试方法 `totalDependenciesCountShouldBeEight` 的方法名与实际断言值不一致。当前断言 `assertEquals(9,...)`（方法名应为 `ShouldBeNine`），修改后断言 `assertEquals(11,...)`（方法名应为 `ShouldBeEleven`）。建议本轮同步重命名方法名以消除歧义。

## 修改要求（仅 REJECTED 时）

### [一般] 问题 1：文件清单表不完整

**问题**：task_v17.md "预期文件清单"表（含 11 行）未包含 3 个需修改的测试文件（AbstractCapabilityExecutorTest.java、DiscussionConclusionCapabilityExecutorTest.java、TriageCapabilityExecutorTest.java）。这 3 个文件在"调用方 mock 更新范围"中已明确有 ~20 处 lambda→Mockito mock 改造。

**为什么是问题**：文件清单是 implementer 完成任务的对照清单；遗漏导致 implementer 可能忽略修改这些测试文件，编译失败（PromptTemplateManager 接口新增 getFallbackPrompt() 后不再是函数式接口，lambda 编译错误）。

**修正方向**：在"预期文件清单"表中补充 3 行：
- Row 12: `orchestrator/AbstractCapabilityExecutorTest.java`（修改，16 处 lambda→Mockito mock + 新增 import static）
- Row 13: `orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java`（修改，3 处 lambda→Mockito mock）
- Row 14: `orchestrator/impl/TriageCapabilityExecutorTest.java`（修改，1 处 lambda→Mockito mock）
