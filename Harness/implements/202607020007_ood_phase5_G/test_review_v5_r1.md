# 测试审查报告（v5 r1）

## 审查结果
RESOLVED

## 发现

- **[严重]** `AiOrchestratorTest.java:100-106` — `shouldReturnFailureForUnregisteredCapability` 测试逻辑与用例意图完全背离。测试注册了 `triageExecutor`（capabilityId="TRIAGE"）后调用 `orch.triage()`，实际走的是已注册的正常路径，executor 返回默认的成功结果 `AiResult.success("ok")`。断言 `assertFalse(result.isSuccess())` 和 `assertTrue(result.getErrorCode().contains("TRIAGE"))` 在运行时会失败，因为结果是 `isSuccess()=true`。

## 修改要求

### 1. `shouldReturnFailureForUnregisteredCapability` — AiOrchestratorTest.java:100-106

**问题**：测试注册了 capabilityId="TRIAGE" 的 executor 后调用 `orch.triage()`（硬编码 "TRIAGE"），executor 被正常找到并返回成功结果。测试名称及断言期望的"未注册"路径无法进入。

**修正方向**：使用不含 "TRIAGE" 执行器的 executor 列表来构造 orchestrator（例如 `List.of(diagnosisExecutor)` 或空列表），使 `executorMap.get("TRIAGE")` 返回 null，从而进入 unregistered 分支。预期错误消息应为 `"未注册能力标识: TRIAGE"`，断言可改为 `assertTrue(result.getErrorCode().contains("未注册能力标识"))`。

## 修订确认（v5 r1）

| 审查意见 | 修改措施 | 状态 |
|---------|---------|------|
| [严重] `shouldReturnFailureForUnregisteredCapability` 使用含 `triageExecutor` 的列表导致无法覆盖未注册分支 | 使用 `List.of(diagnosisExecutor)` 构造 orchestrator；断言改为 `contains("未注册能力标识")` | ✓ 已修正 |
