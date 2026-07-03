# 测试报告（v21 r1 修订）

## 修订内容

### 1. [一般] 缺少 UNAVAILABLE + tryProbe=true 管线测试

**文件**：`AbstractCapabilityExecutorTest.java`

新增测试方法 `executeStandardPipelineShouldContinueWhenEndpointUnavailableWithProbe`（行 549-591）：
- mock `getState` 返回 `EndpointHealthState.UNAVAILABLE`
- mock `tryProbe` 返回 `true`
- 提供完整的 `LlmChatService` mock 使管线继续执行
- 验证 `executeStandardPipeline` 返回成功结果（非降级），证明管线越过健康检查进入 LLM 调用

### 2. [轻微] tryProbe 返回 true 后未验证 lastProbeTime 更新

**文件**：`ModelEndpointHealthManagerTest.java`

`tryProbeShouldReturnTrueAfterProbeWindow` 新增行 85：`assertFalse(manager.tryProbe("ep-1"))`，验证首次调用返回 true 后 lastProbeTime 已被更新，第二次立即调用应返回 false。

### 3. [轻微] 探测失败未验证 lastProbeTime 重置

**文件**：`ModelEndpointHealthManagerTest.java`

`probeFailureShouldStayUNAVAILABLE` 新增行 118：`assertTrue(Math.abs(System.currentTimeMillis() - manager.getLastProbeTime("ep-fail")) < 5000)`，验证 UNAVAILABLE 下探测失败后 lastProbeTime 被更新为当前时间。

### 4. [轻微] 并发测试断言过弱

**文件**：`ModelEndpointHealthManagerTest.java`

`concurrentAccessShouldNotCauseRaceConditions` 强化验证：
- 新增行 169-171：检查所有线程在 5 秒内完成（`assertFalse(t.isAlive())`）
- 行 174-176：验证最终状态为合法枚举值之一（`CONNECTED`/`DEGRADED`/`UNAVAILABLE`）
