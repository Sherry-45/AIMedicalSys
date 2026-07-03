# 设计审查报告（v27 r1）

## 审查结果
REJECTED

## 发现

### **[严重]** 测试 import 变更说明错误：日志测试保留但标记删除日志相关 import

设计 §测试代码修改 > import 变更（line 100-106）列出删除以下 import：
- `import ch.qos.logback.classic.Level;`
- `import ch.qos.logback.classic.Logger;`
- `import ch.qos.logback.classic.spi.ILoggingEvent;`
- `import ch.qos.logback.core.read.ListAppender;`
- `import org.slf4j.LoggerFactory;`

但设计同时保留了 `shouldLogErrorOnConstruction` 和 `shouldLogWarnOnSubsequentCalls` 这两个测试（line 144-146），它们大量使用了上述 import（`Logger`、`ListAppender`、`ILoggingEvent`、`Level`、`LoggerFactory`）。

任务文件已明确指示（task_v27.md line 82）：**"如果日志测试已删除则删除，否则保留"**。

**后果**：生成代码后编译失败。

**修正方向**：保留所有 logback/日志相关 import（Level、Logger、ILoggingEvent、ListAppender、LoggerFactory）。或同步删除这两个日志测试（但设计选择了保留，需保持一致）。

---

### **[一般]** 测试映射表中的数量描述不一致

设计 line 120 声明"保留并修改的 **29** 个测试"——此计数正确（36 总 - 7 删除 = 29）。

但映射表（line 147-148）描述为：
- "**13** 个 `xxxShouldDelegateWhenAvailable`"
- "**13** 个 `xxxShouldReturnDegradedWhenNoDelegate`"

实际剩余的分组应为：
- 5 个命名的特殊测试（含 triage 委托/降级/已降级/日志）
- **12** 个 `xxxShouldDelegateWhenAvailable`（非 triage 方法 12 个，triage 已在上组）
- **12** 个 `xxxShouldReturnDegradedWhenNoDelegate`（非 triage 方法 12 个，triage 已在上组）

5 + 12 + 12 = 29，而非 5 + 13 + 13 = 31。

**后果**：编码时可能误判测试数量或遗漏/重复修改。

**修正方向**：将"13 个"改为"12 个"，或调整分组方式避免重复计数。

---

### **[轻微]** handleEmptyDelegates 行为变更未显式说明

设计将 `handleEmptyDelegates()` 简化为始终 log WARN（line 72-78）。当前代码中该方法已经无条件 log WARN（不含 AtomicBoolean），因此实际行为未变。但设计未明确说明已选择"简化日志逻辑"路径（task_v27.md line 45 的"或"选项）。建议补充一行注释说明已选择简化路径，便于后续维护理解。

## 修改要求（仅 REJECTED 时）

### 必须修正

**问题**：日志测试保留但日志 import 被标记删除，将导致编译失败。

**为什么是问题**：操作前后矛盾——保留依赖日志框架的测试方法，却移除测试方法使用的 import。

**期望修正方向**：
- 方案 A（推荐）：在 import 变更列表中**移除**对以下 5 行的删除标记：`ch.qos.logback.classic.Level`、`ch.qos.logback.classic.Logger`、`ch.qos.logback.classic.spi.ILoggingEvent`、`ch.qos.logback.core.read.ListAppender`、`org.slf4j.LoggerFactory`。
- 方案 B：同步删除 `shouldLogErrorOnConstruction` 和 `shouldLogWarnOnSubsequentCalls` 这两个测试并移除日志 import。但设计和任务均明确保留日志行为，不建议此方案。

### 应当修正

**问题**：映射表中"13 个 xxxShouldDelegateWhenAvailable"和"13 个 xxxShouldReturnDegradedWhenNoDelegate"的计数与 29 个保留测试的合计不一致。

**为什么是问题**：为编码阶段提供错误的数量指引，可能引起混淆。

**期望修正方向**：将 line 147 和 line 148 的"13 个"改为"12 个"。
