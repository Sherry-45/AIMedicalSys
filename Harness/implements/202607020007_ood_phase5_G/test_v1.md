# 测试报告（v1）

## 概述

在编码 agent 已有测试基础上，按详细设计行为契约进行审查与补充。三个测试文件共新增 9 个测试用例，覆盖剩余 getMessage 验证、显式零值边界、Serializable 接口、以及多 FAILURE 事件 lastFailureTime 取最大值。

## 变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | AIMedical/backend/modules/ai/ai-api/src/test/java/com/aimedical/modules/ai/api/degradation/DegradationReasonTest.java | 新增 6 个 getMessage 测试（CircuitBreakerOpen/ParseFailure/Timeout/StrategyTriggered/InternalError/InfrastructureError），用例数 15 → 21 |
| 修改 | AIMedical/backend/modules/ai/ai-api/src/test/java/com/aimedical/modules/ai/api/degradation/DegradationContextTest.java | 新增显式零值边界测试 + Serializable 验证，用例数 16 → 18 |
| 修改 | AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/metrics/SlidingWindowMetricsStoreTest.java | 新增多 FAILURE lastFailureTime 取最大值测试，用例数 18 → 19 |

## 行为契约覆盖矩阵

| 契约 | 测试位置 | 覆盖维度 |
|------|---------|---------|
| DegradationReason.getCode() 返回 code 值 | DegradationReasonTest | 全部 8 枚举正向 ✅ |
| DegradationReason.getMessage() 返回 message 值 | DegradationReasonTest | 全部 8 枚举正向 ✅ |
| DegradationReason.fromCode(code) 返回匹配枚举或 null | DegradationReasonTest | 正向匹配 ✅ / 未知 code ✅ / null 输入 ✅ |
| DegradationReason.toString() 返回 code | DegradationReasonTest | 正向 ✅ |
| DegradationContext.Builder.build() 全字段赋值 | DegradationContextTest | 全字段 ✅ / 部分字段 ✅ / 独立实例 ✅ |
| DegradationContext.postDeserializationValidate() 全默认→requestType=null | DegradationContextTest | 隐含默认 ✅ / 显式零值 ✅ /幂等性 ✅ / 任一字段非默认则不清理 ✅ |
| DegradationContext.isFresh() 返回 boolean | DegradationContextTest | TTL 内 ✅ / 过期 ✅ / timestamp=0 ✅ |
| DegradationContext.isInitialized() 返回 boolean | DegradationContextTest | 双字段非 null ✅ / 单字段 null ✅ / 全 null ✅ |
| DegradationContext 实现 Serializable | DegradationContextTest | instanceof 验证 ✅ |
| SlidingWindowMetricsStore.recordXxx 追加事件 | SlidingWindowMetricsStoreTest | recordSuccess ✅ / recordFailure ✅ / recordDegraded ✅ |
| SlidingWindowMetricsStore.getXxxRate 返回 0.0~1.0 | SlidingWindowMetricsStoreTest | 无事件 0.0 ✅ / 混合事件 ✅ / 分母不含 degraded ✅ / 分母含 degraded ✅ |
| SlidingWindowMetricsStore.getAverageElapsed | SlidingWindowMetricsStoreTest | 纯 success ✅ / 含 degraded ✅ / 含 failure(0ms) ✅ |
| SlidingWindowMetricsStore.buildDegradationContext | SlidingWindowMetricsStoreTest | 无窗口零值 ✅ / 正常统计 + serializedTimestamp 范围校验 ✅ / lastFailureTime 无 FAILURE=0 ✅ / 多 FAILURE 取 max 精确校验 ✅ |
| 惰性淘汰 | SlidingWindowMetricsStoreTest | 窗口内事件保留 ✅ / maxEvents 裁剪至上限 ✅ |
| 并发安全 | SlidingWindowMetricsStoreTest | join(5000) 超时 + isAlive 检查 ✅ / 2000 事件总数 + 失败率 0.45~0.55 验证 ✅ |

## 覆盖缺口说明

- **getEffectiveFailureRate 公式验证**：已通过混合事件用例覆盖，分子仅含 failureCount，分母含三类事件，与设计修订后的 OOD §3.5 公式一致。
- **JSON 序列化/反序列化**：因涉及 Jackson ObjectMapper 集成测试，且在 DegradationContext 设计上 @JsonIgnoreProperties 已保证旧 JSON 兼容，未纳入单元测试范围。
- **DegradationStrategy.getOrder()**：为 default 方法返回常量 0，不涉及业务逻辑，无独立测试必要。
- **惰性淘汰移除旧事件**：因依赖系统时钟，引入 sleep 会导致测试不稳定，暂不添加。

## 修订说明（v1 r1）

| 审查意见 | 修改措施 |
|---------|---------|
| [一般] 并发测试 join() 无超时 + 缺少数据完整性断言 | `join(5000)` 加超时 + `assertFalse(isAlive)` 检查线程正常结束；新增 `buildDegradationContext` 验证 2000 事件总数，`getFailureRate` 断言落在 [0.45, 0.55] |
| [轻微] lastFailureTime 多 FAILURE 仅断言 `>= first`，强度不足 | 捕获第二次 recordFailure 前后的时间戳，断言 `lastFailureTime` 落于该时间区间且严格大于第一次 |
| [轻微] maxEvents 仅断言 `avg > 0` | 替换为 `buildDegradationContext`，断言 `invocationCount == 10000`（= maxEventsPerCapability） |
| [轻微] serializedTimestamp 仅断言 `> 0` 属平凡真条件 | 捕获 `buildDegradationContext` 调用前后时间戳，断言 serializedTimestamp 落于该区间 |

## 汇总

| 模块 | 测试文件 | 用例数 |
|------|---------|-------|
| ai-api | DegradationReasonTest | 21 |
| ai-api | DegradationContextTest | 18 |
| ai-impl | SlidingWindowMetricsStoreTest | 19 |
| **合计** | | **58** |
