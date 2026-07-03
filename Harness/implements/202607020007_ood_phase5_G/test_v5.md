# 测试报告（v5）

## 测试文件

| 文件路径 | 操作 | 说明 |
|---------|------|------|
| `ai-impl/src/test/java/.../orchestrator/AiOrchestratorTest.java` | 修正 | 修正 `shouldReturnFailureForUnregisteredCapability` 测试用例 |

## 测试覆盖说明

| 行为契约 | 测试方法 | 覆盖维度 |
|---------|---------|---------|
| 方法调用映射 — triage | `triageShouldDelegateToTriageExecutorWithRequestPassthrough` | 正常路径 |
| 方法调用映射 — diagnosis | `diagnosisShouldDelegateToDiagnosisExecutorWithRequestPassthrough` | 正常路径 |
| 方法调用映射 — 全部 13 方法 | `allMethodsShouldReturnSuccessResult` | 正常路径 |
| executorMap 初始化 — 正确映射 | `initExecutorMapShouldBuildCorrectMapping` | 正常路径 |
| executorMap 初始化 — 重复 key 后覆盖 | `duplicateCapabilityIdShouldUseLastRegisteredExecutor` | 边界条件 |
| 未注册 capabilityId | `shouldReturnFailureForUnregisteredCapability` | 错误路径 |
| 同步异常保护 | `shouldReturnFailureOnExecutorSyncException` | 错误路径 |
| 同步异常指标记录 | `shouldRecordFailureOnSyncException` | 错误路径 / 状态交互 |
| 返回值透传（同一 CompletableFuture） | `shouldReturnExactCompletableFutureFromExecutor` | 正常路径 |
| 异步异常不干预 | `shouldPassthroughExceptionallyCompletedFuture` | 错误路径 |
| executor 返回 failure 结果透传 | `executorWithFailureResultShouldBeReturnedAsIs` | 错误路径 |

## 修订说明（v5 r1）

| 审查意见 | 修改措施 |
|---------|---------|
| [严重] `shouldReturnFailureForUnregisteredCapability` 使用含 `triageExecutor` 的列表导致测试无法覆盖未注册分支 | 使用 `List.of(diagnosisExecutor)` 构造 orchestrator，使 `executorMap.get("TRIAGE")` 返回 null；断言改为检查 `"未注册能力标识"` |

## 测试验证

测试用例全部独立，无执行顺序依赖。每个行为契约至少一个正向用例覆盖。
