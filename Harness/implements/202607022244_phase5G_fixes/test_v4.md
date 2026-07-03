# 测试报告（v4）

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `orchestrator/AbstractCapabilityExecutorTest.java` | TestableExecutor 构造器 4 个参数 AtomicReference→直接类型；所有 37 处构造调用更新；T3 测试变更（Phase4BusinessException→CompletionException）；T5 测试变更（Phase4BusinessException 传播）；T27 测试变更（extractCallerId→"SYSTEM"）；新增 T4/T5 LlmInfrastructureException 降级测试；新增 T26 userId 传播测试；新增 T28 endpointHealthManager null 保护测试 |
| 修改 | `orchestrator/impl/TriageCapabilityExecutorTest.java` | 构造器调用 4 个 AtomicReference 参数→直接类型 |
| 修改 | `orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java` | 构造器调用 4 个 AtomicReference 参数→直接类型 |
| 修改 | `thinadapter/DiagnosisCapabilityExecutorTest.java` | 构造器调用 3 个 AtomicReference 参数→直接类型 |
| 修改 | `thinadapter/ImageAnalysisCapabilityExecutorTest.java` | 同上 |
| 修改 | `thinadapter/AnalysisReportForLabTestCapabilityExecutorTest.java` | 同上 |
| 修改 | `thinadapter/AnalysisReportForInspectionCapabilityExecutorTest.java` | 同上 |
| 修改 | `thinadapter/RecommendExecutionOrderCapabilityExecutorTest.java` | 同上 |
| 修改 | `thinadapter/RecommendExaminationCapabilityExecutorTest.java` | 同上 |

## 测试结果

`mvn test -pl modules/ai/ai-impl -Dtest="AbstractCapabilityExecutorTest,TriageCapabilityExecutorTest,DiscussionConclusionCapabilityExecutorTest"`

**测试运行：80，失败：0，错误：0，跳过：0**

### 行为契约覆盖验证

| 契约 | 测试方法 | 状态 |
|------|---------|------|
| T3：exceptionally() 中 Phase4BusinessException 不再触发降级 | `executeShouldPropagatePhase4BusinessException` — 验证 throw CompletionException | ✅ |
| T4：LlmInfrastructureException 使用 instanceof 检测 | `executeStandardPipelineShouldDegradeOnLlmInfrastructureException` — 外部分支 INFRASTRUCTURE_ERROR | ✅ |
| T4（fallback 内）：同上 | `executeStandardPipelineShouldDegradeOnLlmInfrastructureExceptionInFallback` — 内部分支 INFRASTRUCTURE_ERROR | ✅ |
| T5：外部分支-Phase4BusinessException 传播 | `executeStandardPipelineShouldPropagatePhase4BusinessException` — throw CompletionException | ✅ |
| T5：内部分支-Phase4BusinessException 传播 | `executeStandardPipelineShouldPropagatePhase4BusinessExceptionInFallback` — throw CompletionException | ✅ |
| T21：4 字段/13 子类构造器直接类型 | 所有 9 个测试文件的构造器调用编译通过 | ✅ |
| T26：doDegrade/checkPreDegradation userId 参数 | `executeShouldPassUserIdThroughDegradePath` — 验证 userId 传递至 doDegrade | ✅ |
| T27：extractCallerRole/extractCallerId 委托至 RequestContextUtils | `extractCallerRoleShouldReturnNull` — 仍可返回 null；`extractCallerIdShouldReturnSystemWhenNoAuth` — 返回 "SYSTEM" | ✅ |
| T28：endpointHealthManager null 保护 | `executeStandardPipelineShouldContinueWhenEndpointHealthManagerIsNull` — null 时继续执行 | ✅ |

## 设计偏差说明

无偏差。所有测试变更严格按照详细设计 v4 和实现报告 v4 执行。

## 已知问题

6 个薄适配器测试文件的 `shouldDegradeOnTimeout` 测试因使用 `Runnable::run`（同步执行）作为 `llmCallExecutor`，导致 `supplyAsync` 的委托任务在当前线程同步完成，`delegateFuture.get(timeout, MILLISECONDS)` 在 `supplyAsync` 返回时 Future 已完成，不会触发 TimeoutException。该问题在变更前后均存在，非本次引入。
