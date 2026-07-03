# 测试审查报告（v25 r3）

## 审查结果
REJECTED

## 发现

- **[严重]** `Harness/implements/202607020007_ood_phase5_G/test_v25.md` — 测试报告仅覆盖 1 个文件（`AbstractCapabilityExecutorTest.java`），但详细设计（v25）明确列出 10 个需修改的测试文件。其余 9 个文件（`TriageCapabilityExecutorTest.java`、`DiscussionConclusionCapabilityExecutorTest.java`、`DiagnosisCapabilityExecutorTest.java`、`AnalysisReportForInspectionCapabilityExecutorTest.java`、`AnalysisReportForLabTestCapabilityExecutorTest.java`、`ImageAnalysisCapabilityExecutorTest.java`、`RecommendExaminationCapabilityExecutorTest.java`、`RecommendExecutionOrderCapabilityExecutorTest.java`、`DefaultModelRouterTest.java`）在测试报告中完全未提及，导致测试报告无法作为完整的状态追踪依据。

- **[一般]** 6个薄适配器测试文件（`DiagnosisCapabilityExecutorTest.java` 等）的 `createExecutor()` 中参数 8（`thinAdapterPerCapabilityConfig`）使用了 `new AtomicReference<>(Map.of())`，但详细设计（v25）明确该位置应当保留 `null`。实现报告声称「无偏差」，与实际代码不符。`null` 与 `Map.of()` 在语义上不等价（可能触发不同的生产代码分支），此偏离未在任何文档中说明。

## 修改要求（仅 REJECTED 时）

### 严重问题

**test_v25.md 测试报告不完整**

- 问题：测试报告仅包含 `AbstractCapabilityExecutorTest.java` 一条记录，遗漏了详细设计中列出的其他 9 个测试文件。
- 为什么是问题：测试报告的职责是记录所有受影响的测试文件及其状态，用作可追溯的验证依据。遗漏 9/10 的文件使报告不可靠。
- 修正方向：将全部 10 个修改的测试文件路径追加到 `test_v25.md`，每条格式为 `TEST_WRITTEN:{文件路径}`。

### 一般问题

**6个薄适配器测试文件参数 8 设计偏离**

- 涉及文件：`DiagnosisCapabilityExecutorTest.java`、`AnalysisReportForInspectionCapabilityExecutorTest.java`、`AnalysisReportForLabTestCapabilityExecutorTest.java`、`ImageAnalysisCapabilityExecutorTest.java`、`RecommendExaminationCapabilityExecutorTest.java`、`RecommendExecutionOrderCapabilityExecutorTest.java`
- 问题：参数 8（`thinAdapterPerCapabilityConfig`）在详细设计中指定为 `null`，但实际代码使用了 `new AtomicReference<>(Map.of())`。
- 为什么是问题：`null` 和 `Map.of()` 传递不同的语义 — `null` 触发生产代码的默认兜底逻辑，`Map.of()` 则跳过该分支。如果生产代码在 `null` 时使用默认配置，则测试可能遗漏了对该分支的覆盖。实现报告声称「无偏差」是事实错误。
- 修正方向：确认 `null` 或 `Map.of()` 哪个是正确的测试值。如果 `null` 正确则改回 `null`；如果 `Map.of()` 正确则更新详细设计和实现报告的偏差说明。
