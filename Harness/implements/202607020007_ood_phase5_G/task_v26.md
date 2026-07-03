# 任务指令（v26）

## 动作
RETRY

## 任务描述
修复 Task 18（AiPlatformConfig + AiPlatformEnvironmentPostProcessor）v25 验证暴露的 13 失败 + 6 错误。**生产代码 2 处修改 + 测试代码 7 处修改**。

## 失败根因

### 1. AiPlatformConfigTest.initShouldSucceedWithValidConfiguration:54-55
`thinAdapterPerCapabilityConfigRef` 和 `parseTimeoutConfigRef` 仅存储 `getPerCapability()` 的 per-capability 值，不包含默认值。测试第 54-55 行错误地期望 `get("cap1")` 返回默认超时（PT30S / PT5S）。**修正方向：调整测试期望以匹配实际行为。**

### 2. AbstractCapabilityExecutor.resolveTimeout():525 NPE
`thinAdapterPerCapabilityConfig.get()` 在字段为 null 时抛出 NPE。薄适配器测试构造器第 8 参传 `null`，导致 `resolveThinAdapterTimeout()` 在第 173 行 `thinAdapterPerCapabilityConfig.get()` 直接 NPE。**修正方向：生产代码添加 null 安全检查 + 测试代码传空 AtomicReference 而非 null。**

## 选择理由
同为 Task 18 RETRY，第二次失败。本次修复同时覆盖生产代码 null 安全和测试期望错误，彻底解决 v25 所有 19 个失败/错误。

## 任务上下文

### AiPlatformConfigTest.java（修改，1 文件）
**测试方法**: `initShouldSucceedWithValidConfiguration`
- **Line 54**: `assertEquals(Duration.ofSeconds(30), config.thinAdapterPerCapabilityConfig().get().get("cap1"));`
  → 改为 `assertEquals(0, config.thinAdapterPerCapabilityConfig().get().size());`（"cap1" 不在 per-cap 映射中，验证映射为空）
- **Line 55**: `assertEquals(Duration.ofSeconds(5), config.parseTimeoutConfig().get().get("cap1"));`
  → 改为 `assertNull(config.parseTimeoutConfig().get().get("cap1"));`（"cap1" 不在 per-cap 映射中，验证不存在）
- **Line 56**: `assertEquals(Duration.ofSeconds(5), config.parseTimeoutDefault().get());`（不变，此断言正确）
- **Line 53**: `assertEquals(Duration.ofSeconds(40), config.capabilityTimeoutConfig().get().get("cap1"));`（不变，此断言正确）

### AbstractCapabilityExecutor.java（生产代码，修改 1 文件）
**方法**: `resolveTimeout(String capabilityId)`（行 520-533）
**当前代码**:
```java
protected Duration resolveTimeout(String capabilityId) {
    Map<String, Duration> timeoutConfig = capabilityTimeoutConfig.get();           // 行 521
    if (timeoutConfig != null && timeoutConfig.containsKey(capabilityId)) {
        return timeoutConfig.get(capabilityId);
    }
    Map<String, Duration> thinConfig = thinAdapterPerCapabilityConfig.get();       // 行 525 — NPE 风险
    if (thinConfig != null && thinConfig.containsKey(capabilityId)) {
        return thinConfig.get(capabilityId);
    }
    ...
}
```
**修改**: 行 525 前添加 null 检查：
```java
Map<String, Duration> thinConfig = thinAdapterPerCapabilityConfig != null
    ? thinAdapterPerCapabilityConfig.get() : null;
```

### DiagnosisCapabilityExecutor.java（生产代码，修改 1 文件）
**方法**: `resolveThinAdapterTimeout(String capabilityId)`（行 172-178）
**当前代码**:
```java
private long resolveThinAdapterTimeout(String capabilityId) {
    Map<String, Duration> config = thinAdapterPerCapabilityConfig.get();  // 行 173 — NPE 风险
    if (config != null && config.containsKey(capabilityId)) {
```
**修改**: 行 173 前添加 null 检查：
```java
private long resolveThinAdapterTimeout(String capabilityId) {
    if (thinAdapterPerCapabilityConfig == null) {
        return thinAdapterTimeout.toMillis();
    }
    Map<String, Duration> config = thinAdapterPerCapabilityConfig.get();
```

### 6 个薄适配器测试文件（修改，6 文件）
每个文件的 `createExecutor()` 方法中第 8 参数（`thinAdapterPerCapabilityConfig`）从 `null` 改为 `new AtomicReference<>(new ConcurrentHashMap<>())`。

受影响文件：
1. **DiagnosisCapabilityExecutorTest.java** — line 100: `null` → `new AtomicReference<>(new ConcurrentHashMap<>())`
2. **AnalysisReportForInspectionCapabilityExecutorTest.java** — line 99: 同上
3. **AnalysisReportForLabTestCapabilityExecutorTest.java** — line 99: 同上
4. **ImageAnalysisCapabilityExecutorTest.java** — line 99: 同上
5. **RecommendExaminationCapabilityExecutorTest.java** — line 99: 同上
6. **RecommendExecutionOrderCapabilityExecutorTest.java** — line 99: 同上

每个测试文件还需要检查 import 中是否已包含 `java.util.concurrent.ConcurrentHashMap`。如果当前文件没有该 import，需要补充：
```java
import java.util.concurrent.ConcurrentHashMap;
```
（AtomicReference 的 import 在 v25 已添加）

## 验证标准
- `mvn test -pl modules/ai/ai-impl -am` 全部 test-compile 通过
- AiPlatformConfigTest.`initShouldSucceedWithValidConfiguration` 通过
- 6 个薄适配器测试文件全部 30 测试方法通过（各 5 个：shouldDegradeWhenDtoIsEmpty、shouldDegradeWhenServiceIsNull、shouldSucceedWithValidDelegation、shouldDegradeOnTimeout、shouldReturnFailureOnPhase4BusinessException）
- 总测试通过数 >= 516（common 225 + ai-api 200 + ai-impl >= 91）

## 涉及文件清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `orchestrator/AbstractCapabilityExecutor.java` | `resolveTimeout()` 行 525 前添加 `thinAdapterPerCapabilityConfig == null` 检查 |
| 修改 | `orchestrator/impl/DiagnosisCapabilityExecutor.java` | `resolveThinAdapterTimeout()` 行 173 前添加 `thinAdapterPerCapabilityConfig == null` 检查 |
| 修改 | `config/AiPlatformConfigTest.java` | Lines 54-55 断言修正 |
| 修改 | `orchestrator/impl/DiagnosisCapabilityExecutorTest.java` | `createExecutor()` 第 8 参 null → `new AtomicReference<>()` |
| 修改 | `orchestrator/impl/AnalysisReportForInspectionCapabilityExecutorTest.java` | 同上 |
| 修改 | `orchestrator/impl/AnalysisReportForLabTestCapabilityExecutorTest.java` | 同上 |
| 修改 | `orchestrator/impl/ImageAnalysisCapabilityExecutorTest.java` | 同上 |
| 修改 | `orchestrator/impl/RecommendExaminationCapabilityExecutorTest.java` | 同上 |
| 修改 | `orchestrator/impl/RecommendExecutionOrderCapabilityExecutorTest.java` | 同上 |
