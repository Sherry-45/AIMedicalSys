# 测试审查报告（v21 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** `AbstractCapabilityExecutorTest.java` — 缺少 UNAVAILABLE + tryProbe=true 管线路径的测试覆盖。设计 §AbstractCapabilityExecutor适配 明确规定了此分支（canProbe=true → 继续进入 LLM 调用，totalTimeoutMs 减半），生产代码 `AbstractCapabilityExecutor.java:383-386` 已实现该逻辑，但测试中仅有 UNAVAILABLE + tryProbe=false 分支的覆盖（`executeStandardPipelineShouldDegradeWhenEndpointUnavailable`），缺少 tryProbe=true 分支的测试。应新增测试：mock getState 返回 UNAVAILABLE、tryProbe 返回 true，验证管线继续执行且 totalTimeoutMs 被减半（可通过记录调用结果间接验证）。

- **[轻微]** `ModelEndpointHealthManagerTest.java` — tryProbe 返回 true 后未验证 lastProbeTime 被更新的副作用。设计 §tryProbe行为 明确要求"返回 true（更新 lastProbeTime = System.currentTimeMillis()）"，但 `tryProbeShouldReturnTrueAfterProbeWindow` 仅断言返回值，未验证 tryProbe 后立即再次调用应返回 false（证明 lastProbeTime 已更新）。

- **[轻微]** `ModelEndpointHealthManagerTest.java` — UNAVAILABLE + 探测失败（success=false）未验证 lastProbeTime 被重置的副作用。设计 §状态转换规则 UNAVAILABLE|success=false 明确副作用为"重置 lastProbeTime = System.currentTimeMillis()"，但 `probeFailureShouldStayUNAVAILABLE` 仅断言状态保持 UNAVAILABLE，未验证 lastProbeTime 更新。

- **[轻微]** `ModelEndpointHealthManagerTest.java` — 并发测试 `concurrentAccessShouldNotCauseRaceConditions` 仅断言 `finalState != null`，未验证任何状态机正确性（如最终状态合法、无异常抛出、计数一致性等），属于弱验收标准，实际验证效力有限。

## 修改要求

### 1. [一般] 缺少 UNAVAILABLE + tryProbe=true 管线测试

**文件**：`AbstractCapabilityExecutorTest.java`

**位置**：应在 `executeStandardPipelineShouldDegradeWhenEndpointUnavailable` 之后（第 545 行后）新增测试方法。

**问题**：设计 §AbstractCapabilityExecutor适配 第 2 项定义了 `healthState == UNAVAILABLE && tryProbe == true` 的行为："继续进入 LLM 调用，totalTimeoutMs 减半"。该逻辑已实现于 `AbstractCapabilityExecutor.java:383-386`，但没有任何测试覆盖此路径。现有 `executeStandardPipelineShouldDegradeWhenEndpointUnavailable` 仅覆盖了 tryProbe=false 的降级路径。

**期望**：新增类似如下的测试：
- mock `getState` 返回 `EndpointHealthState.UNAVAILABLE`
- mock `tryProbe` 返回 `true`
- 验证 `executeStandardPipeline` 继续执行 LLM 调用而非降级
- 建议验证 timeout 减半效应是否生效（可通过校验超时缩短导致的 `TIMEOUT` 降级或捕获调用参数间接验证）
