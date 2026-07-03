# 实现报告（v8）

## 概述

按详细设计 v8 修复 7 项问题（T15/T16/T17/T25/T51/T52/T59），涉及 4 个源文件修改。

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `ai-impl/.../metrics/AiCallRecord.java` | 扩容至 23 字段；promptVersion String→Integer；degradeReason→degradationReason；构造器 private + 3 静态工厂方法；sentinelReason 处理 |
| 修改 | `ai-impl/.../metrics/LoggingMetricsCollector.java` | @Async 绑定 executor；7 处硬编码改为从 record 读取；移除 parsePromptVersion |
| 修改 | `ai-impl/.../metrics/SlidingWindowMetricsStore.java` | windowSeconds→AtomicLong + 参数校验；4 读取方法快照复制 |
| 修改 | `ai-impl/.../metrics/ModelEndpointHealthManager.java` | UNAVAILABLE 探测成功→DEGRADED |

## 编译验证

**编译失败**。构造器改为 private 后，以下文件中的 `new AiCallRecord(...)` 调用无法编译（共 31 处调用点）：

**生产代码**（10 个文件）：
- `thinadapter/DiagnosisCapabilityExecutor.java:109,127`
- `thinadapter/AnalysisReportForInspectionCapabilityExecutor.java:109,127`
- `thinadapter/AnalysisReportForLabTestCapabilityExecutor.java:109,127`
- `thinadapter/RecommendExecutionOrderCapabilityExecutor.java:109,127`
- `thinadapter/RecommendExaminationCapabilityExecutor.java:109,127`
- `thinadapter/ImageAnalysisCapabilityExecutor.java:109,127`
- `orchestrator/AiOrchestrator.java:159`
- `orchestrator/AbstractCapabilityExecutor.java:267,511`

**测试代码**（3 个文件）：
- `metrics/AiCallRecordTest.java:11,38,54`
- `metrics/LoggingMetricsCollectorTest.java:42,87,108,131,162,177,192,207,224,253`
- `orchestrator/AbstractCapabilityExecutorTest.java:512`
- `orchestrator/AiOrchestratorTest.java:143`

## 修订说明（v8 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| 构造器 private 导致 ~31 处调用点编译失败 | 新增 public 旧版 15 参构造器（capabilityId, modelId, promptVersion, userId, departmentId, sessionId, visitId, patientId, callerRole, callerId, elapsedMs, degraded, degradationReason, promptTokens, completionTokens），委托到 private 全参构造器，新字段填默认值（callTime=LocalDateTime.now(), retryCount=0, 其余新字段为 null）。旧调用方无需修改即可继续使用 |
| 调用方缺少 factory 方法参数无法迁移 | 保留旧版构造器 public 作为兼容层，调用方可逐步按需迁移到 success()/failure()/degraded() 工厂方法，无需立即全部改造 |
| 5 处 getDegradeReason() 需改为 getDegradationReason() | AiCallRecordTest.java(3 处)、AbstractCapabilityExecutorTest.java(1 处)、AiOrchestratorTest.java(1 处) 全部已修正 |

## 修订说明（v8 r3）
| 审查意见 | 修改措施 |
|---------|---------|
| LoggingMetricsCollector.java:30 entity.setCapabilityName(record.getCapabilityId()) 错误赋值 | 改为 entity.setCapabilityName(record.getCapabilityName()) |
