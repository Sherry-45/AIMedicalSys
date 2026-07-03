# 设计审查报告（v8 r6）

## 审查结果
REJECTED

## 发现

### [严重] `AbstractCapabilityExecutor` 中 `log` 字段声明冲突

设计文档第 739–743 行（Logger 新增）要求向 `AbstractCapabilityExecutor` 新增 `private static final Logger log` 字段，但该字段已在现有源代码第 37 行存在。编码时若照此执行将导致重复字段声明编译错误。设计内部亦存在矛盾：变更汇总表第 948 行声明"（无新增字段）"而详细节又说要新增。

**期望修正方向**：移除 "Logger 新增" 这一变更条目（字段已存在，无需操作）。

### [严重] 现有 `AbstractCapabilityExecutorTest.java` 兼容性断裂未处理

设计对 `AbstractCapabilityExecutor` 的 3 处修改会直接破坏现有单元测试，但设计文档未识别或给出应对方案：

1. **`executeStandardPipelineShouldThrowUnsupportedOperation()`**（Test 第 419–422 行）：期待 `UnsupportedOperationException`，但设计将 `executeStandardPipeline()` 替换为实际实现，测试将挂掉。
2. **`refineTimeoutReasonShouldReturnTimeout()`**（Test 第 451–453 行）：期待 `assertEquals(DegradationReason.TIMEOUT, …)`，但设计将返回类型改为 `String`，无法通过编译。
3. **`doDegradeShouldHandleNonNullMetricsCollector()`**（Test 第 556–580 行）：使用 `new AiMetricsCollector() {}` 匿名类，设计向 `AiMetricsCollector` 接口新增 `record()` 抽象方法后该匿名类不再编译。

**期望修正方向**：在设计中补充 `AbstractCapabilityExecutorTest.java` 的修改计划，或至少文档化这 3 项已知的测试断裂。

### [一般] `handleSuccess()` 违反 null-safe 行为契约

设计第 898–910 行的 `handleSuccess()` 直接调用 `metricsCollector.record(...)`，未做 null-safe 检查。设计第 969 行的行为契约明确要求"所有 stub 方法调用前均判断非 null"，且父类 `doDegrade()` 现有代码（源码第 257 行）已遵守此约定对 `metricsCollector` 做 null 检查。`executeStandardPipeline()` 的成功路径也可能因 `metricsCollector` 为 null 而 NPE。

**期望修正方向**：`handleSuccess()` 中对 `metricsCollector.record()` 调用前增加 `if (metricsCollector != null)` 保护，与 `doDegrade()` 保持一致。

### [轻微] routeResult → LlmChatOptions 映射描述不足

设计第 785–789 行仅以伪代码描述"从 routeResult 推断 modelId"和"从 routeResult 类型推断 clientType"，未明确从 routeResult 的哪个属性/方法取值。实际编码时实现者需自行推测，可能引入实现不一致。

**期望修正方向**：明确 modelId 取值方式（`routeResult.toString()` 或 `String.valueOf(routeResult)`），clientType 推断规则（例如 routeResult 含字段 `clientType` 则提取，否则默认 `HTTP_API`）。
