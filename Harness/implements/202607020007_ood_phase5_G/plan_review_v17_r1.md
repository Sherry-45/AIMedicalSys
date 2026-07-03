# 计划审查报告（v17 r1）

## 审查结果
REJECTED

## 发现

### [严重] AbstractCapabilityExecutor 生产代码调用未更新，导致编译失败

**问题**：计划将 `PromptTemplateManager.render()` 从 2 参数签名改为 4 参数签名，但 `AbstractCapabilityExecutor.java:344` 在 `executeStandardPipeline()` 中以 2 参数形式调用 `promptTemplateManager.render(templateKey, variables)`。任务文件声称"AbstractCapabilityExecutor 生产代码中的调用不在本任务范围内"，但**修改接口签名而不更新所有调用方会导致项目无法编译**。这是一个不可行的分离决策——接口签名变更天然强制所有调用方同步适配。

**期望方向**：必须将 `AbstractCapabilityExecutor.java` 纳入本任务涉及文件，在 `executeStandardPipeline()` 中将 `render(templateKey, variables)` 改为 4 参数调用 `render(capabilityId, departmentId, variables, promptVersion)`，并移除 `String templateKey = capabilityId + ":" + promptVersion;` 这一行。

### [严重] 接口新增 getFallbackPrompt() 后无法使用 Lambda 模拟

**问题**：新 `PromptTemplateManager` 接口将包含两个抽象方法——`render(String, String, Map, Integer)` 和 `getFallbackPrompt(String)`——不再是函数式接口。但计划（task_v17.md §调用方 mock 更新范围）要求将 21 处 mock 从 2 参数 lambda 替换为 4 参数 lambda：

```java
// 计划指定的方式 → 编译失败：非函数式接口不能用 lambda
PromptTemplateManager mockTemplate = (capId, deptId, vars, ver) -> "rendered";
```

这会在 3 个测试文件中产生 21 处编译错误（AbstractCapabilityExecutorTest 17 处、DiscussionConclusionCapabilityExecutorTest 3 处、TriageCapabilityExecutorTest 1 处）。

**期望方向**：将 21 处 lambda 统一替换为 Mockito `mock(PromptTemplateManager.class)` + `when(...).thenReturn(...)` 模式，或匿名内部类。同时需要 stub `getFallbackPrompt()` 的行为以保证测试完整性。

## 修改要求

1. **修复 AbstractCapabilityExecutor 编译问题**：将该文件纳入涉及文件清单，在 `executeStandardPipeline()` 中将 `render()` 调用从 2 参数升级为 4 参数，与新的接口签名匹配。

2. **修复测试 mock 方案**：将 21 处 PromptTemplateManager lambda 替换为 Mockito mock 或匿名内部类，对应 stub 两个方法（render + getFallbackPrompt）。
