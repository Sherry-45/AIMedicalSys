# 详细设计（v26）

## 概述

修复 Task 18 v25 验证暴露的 13 失败 + 6 错误。**生产代码 2 处修改**（null 安全检查）+ **测试代码 7 处修改**（1 处断言修正 + 6 处构造器参数修正）。根因：`thinAdapterPerCapabilityConfig` 字段为 null 时 `.get()` 抛出 NPE；`AiPlatformConfigTest` 第 54-55 行错误地期望 `getPerCapability()` 的 per-capability 值包含默认 fallback。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `orchestrator/AbstractCapabilityExecutor.java` | 修改 | `resolveTimeout()` 行 525 前添加 `thinAdapterPerCapabilityConfig == null` 检查 |
| `orchestrator/impl/DiagnosisCapabilityExecutor.java` | 修改 | `resolveThinAdapterTimeout()` 行 173 前添加 `thinAdapterPerCapabilityConfig == null` 检查 |
| `config/AiPlatformConfigTest.java` | 修改 | Lines 54-55 断言修正：per-cap 映射不含默认值 |
| `orchestrator/impl/DiagnosisCapabilityExecutorTest.java` | 修改 | `createExecutor()` 第 8 参 null → `new AtomicReference<>(new ConcurrentHashMap<>())`；新增 `ConcurrentHashMap` import |
| `orchestrator/impl/AnalysisReportForInspectionCapabilityExecutorTest.java` | 修改 | 同上 |
| `orchestrator/impl/AnalysisReportForLabTestCapabilityExecutorTest.java` | 修改 | 同上 |
| `orchestrator/impl/ImageAnalysisCapabilityExecutorTest.java` | 修改 | 同上 |
| `orchestrator/impl/RecommendExaminationCapabilityExecutorTest.java` | 修改 | 同上 |
| `orchestrator/impl/RecommendExecutionOrderCapabilityExecutorTest.java` | 修改 | 同上 |

## 生产代码修改

### AbstractCapabilityExecutor.java

**文件**: `ai-impl/src/main/java/.../orchestrator/AbstractCapabilityExecutor.java`

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

**修改后**:
```java
protected Duration resolveTimeout(String capabilityId) {
    Map<String, Duration> timeoutConfig = capabilityTimeoutConfig.get();
    if (timeoutConfig != null && timeoutConfig.containsKey(capabilityId)) {
        return timeoutConfig.get(capabilityId);
    }
    Map<String, Duration> thinConfig = thinAdapterPerCapabilityConfig != null
        ? thinAdapterPerCapabilityConfig.get() : null;
    if (thinConfig != null && thinConfig.containsKey(capabilityId)) {
        return thinConfig.get(capabilityId);
    }
    ...
}
```

### DiagnosisCapabilityExecutor.java

**文件**: `ai-impl/src/main/java/.../orchestrator/impl/DiagnosisCapabilityExecutor.java`

**方法**: `resolveThinAdapterTimeout(String capabilityId)`（行 172-178）

**当前代码**:
```java
private long resolveThinAdapterTimeout(String capabilityId) {
    Map<String, Duration> config = thinAdapterPerCapabilityConfig.get();  // 行 173 — NPE 风险
    if (config != null && config.containsKey(capabilityId)) {
        return config.get(capabilityId).toMillis();
    }
    return thinAdapterTimeout.toMillis();
}
```

**修改后**:
```java
private long resolveThinAdapterTimeout(String capabilityId) {
    if (thinAdapterPerCapabilityConfig == null) {
        return thinAdapterTimeout.toMillis();
    }
    Map<String, Duration> config = thinAdapterPerCapabilityConfig.get();
    if (config != null && config.containsKey(capabilityId)) {
        return config.get(capabilityId).toMillis();
    }
    return thinAdapterTimeout.toMillis();
}
```

## 测试代码修改

### AiPlatformConfigTest.java

**文件**: `ai-impl/src/test/java/.../config/AiPlatformConfigTest.java`

**方法**: `initShouldSucceedWithValidConfiguration`（行 42-57）

| 行号 | 当前代码 | 修改后 |
|------|---------|--------|
| 54 | `assertEquals(Duration.ofSeconds(30), config.thinAdapterPerCapabilityConfig().get().get("cap1"));` | `assertEquals(0, config.thinAdapterPerCapabilityConfig().get().size());` |
| 55 | `assertEquals(Duration.ofSeconds(5), config.parseTimeoutConfig().get().get("cap1"));` | `assertNull(config.parseTimeoutConfig().get().get("cap1"));` |

**不变的行**:
- 行 53: `assertEquals(Duration.ofSeconds(40), config.capabilityTimeoutConfig().get().get("cap1"));`（不变）
- 行 56: `assertEquals(Duration.ofSeconds(5), config.parseTimeoutDefault().get());`（不变）

### 6 个薄适配器测试文件

**变更模式**：每个文件的 `createExecutor()` 方法中第 8 参数（`thinAdapterPerCapabilityConfig`）从 `null` 改为 `new AtomicReference<>(new ConcurrentHashMap<>())`。

以 `DiagnosisCapabilityExecutorTest.java`（行 95-101）为例：

**当前代码**:
```java
private DiagnosisCapabilityExecutor createExecutor(Object service, Duration thinAdapterTimeout) {
    return new DiagnosisCapabilityExecutor(
        service, metricsCollector, metricsStore,
        new AtomicReference<>(Map.of("DIAGNOSIS", Duration.ofSeconds(30))),
        null, new AtomicReference<>(Duration.ofSeconds(5)), thinAdapterTimeout,
        null, objectMapper
    );
}
```

**修改后**:
```java
private DiagnosisCapabilityExecutor createExecutor(Object service, Duration thinAdapterTimeout) {
    return new DiagnosisCapabilityExecutor(
        service, metricsCollector, metricsStore,
        new AtomicReference<>(Map.of("DIAGNOSIS", Duration.ofSeconds(30))),
        null, new AtomicReference<>(Duration.ofSeconds(5)), thinAdapterTimeout,
        new AtomicReference<>(new ConcurrentHashMap<>()), objectMapper
    );
}
```

**各文件对应的 capability name**（第 4 参数所用）：

| 文件 | capability name |
|------|----------------|
| DiagnosisCapabilityExecutorTest.java | `DIAGNOSIS` |
| AnalysisReportForInspectionCapabilityExecutorTest.java | `ANALYSIS_REPORT_INSPECTION` |
| AnalysisReportForLabTestCapabilityExecutorTest.java | `ANALYSIS_REPORT_LABTEST` |
| ImageAnalysisCapabilityExecutorTest.java | `IMAGE_ANALYSIS` |
| RecommendExaminationCapabilityExecutorTest.java | `RECOMMEND_EXAM` |
| RecommendExecutionOrderCapabilityExecutorTest.java | `RECOMMEND_EXEC_ORDER` |

**import 变更**：每个文件需要新增 `import java.util.concurrent.ConcurrentHashMap;`。当前所有 6 个文件均无此 import（已有 `AtomicReference` import）。

## 错误处理

- `thinAdapterPerCapabilityConfig` 为 null 时的安全降级：`resolveTimeout()` 返回 `Duration.ofSeconds(30)`（fallback）；`resolveThinAdapterTimeout()` 返回 `thinAdapterTimeout.toMillis()`。
- 所有修改不引入新异常路径。

## import 汇总

| 文件 | 需新增的 import |
|------|----------------|
| `AbstractCapabilityExecutor.java` | 无需变更 |
| `DiagnosisCapabilityExecutor.java` | 无需变更 |
| `AiPlatformConfigTest.java` | 无需变更 |
| `DiagnosisCapabilityExecutorTest.java` | `import java.util.concurrent.ConcurrentHashMap;` |
| `AnalysisReportForInspectionCapabilityExecutorTest.java` | 同上 |
| `AnalysisReportForLabTestCapabilityExecutorTest.java` | 同上 |
| `ImageAnalysisCapabilityExecutorTest.java` | 同上 |
| `RecommendExaminationCapabilityExecutorTest.java` | 同上 |
| `RecommendExecutionOrderCapabilityExecutorTest.java` | 同上 |

## 行为契约

1. `resolveTimeout()` 在 `thinAdapterPerCapabilityConfig` 为 null 时跳过 thin-adapter 配置查找，回退到 `thinAdapterTimeout` 或默认 30 秒
2. `resolveThinAdapterTimeout()` 在 `thinAdapterPerCapabilityConfig` 为 null 时直接返回 `thinAdapterTimeout.toMillis()`
3. `AiPlatformConfigTest.initShouldSucceedWithValidConfiguration` 第 54-55 行验证 per-capability 映射不包含未显式配置的 key
4. 6 个薄适配器测试的 `createExecutor()` 第 8 参传空 `AtomicReference<Map>` 而非 `null`，消除 NPE 风险
5. 运行 `mvn test -pl modules/ai/ai-impl -am` 预期全部 test-compile 通过且总通过数 >= 516（common 225 + ai-api 200 + ai-impl >= 91）

## 依赖关系

- `AbstractCapabilityExecutor.resolveTimeout()` 依赖 `thinAdapterPerCapabilityConfig` 字段（`AtomicReference<Map<String, Duration>>` 类型）的 null 安全访问
- `DiagnosisCapabilityExecutor.resolveThinAdapterTimeout()` 依赖 `thinAdapterTimeout` 字段（`Duration` 类型）作为 null 回退值
- 测试文件依赖 `java.util.concurrent.ConcurrentHashMap`（新增 import）
