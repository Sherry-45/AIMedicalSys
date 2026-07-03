# 任务指令（v20）

## 动作
RETRY

## 任务描述
修复 `LoggingMetricsCollectorTest.shouldSaveEntityWhenRecordCalled` 中 promptVersion 测试数据错误。

**预期文件路径**：`ai-impl/src/test/java/com/aimedical/modules/ai/impl/metrics/LoggingMetricsCollectorTest.java`

**具体修改**：
- 第 43 行：`"v2"` → `"2"`
- 无其他变更

## 选择理由
v19 验证执行时该测试失败（唯一失败用例），修复后可全线通过，解锁后续任务推进。

## 任务上下文
- 本次仅为测试数据修复，无需修改生产代码
- 修复后应执行 `mvn test` 验证全部 ai-impl 测试通过

## RETRY 说明
**失败原因摘要**：
v19 verify FAILED — `shouldSaveEntityWhenRecordCalled:57` expected `<2>` but was `<null>`。

根因：测试第 43 行使用 `"v2"` 作为 promptVersion，`parsePromptVersion("v2")` 内 `Integer.parseInt("v2")` 抛出 `NumberFormatException`，返回 `null`；断言 `assertEquals(Integer.valueOf(2), entity.getPromptVersion())` 因此获 null。

**修正方向**：
`LoggingMetricsCollectorTest.java:43`：将 `"v2"` 改为 `"2"`。

**生产代码无需修改**。`parsePromptVersion()` 的设计（null→null、有效整数→parse、无效→null）和 `shouldParseValidPromptVersion("5")` / `shouldHandleInvalidPromptVersion("abc")` 等测试均正确，仅该测试用例数据不匹配。
