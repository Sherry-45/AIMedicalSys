# 任务指令（v2）

## 动作
RETRY

## 任务描述
修复 R1 v1 中 6 个遗留测试文件的编译错误——薄适配器移入 `thinadapter` 子包后，`orchestrator/impl/` 下的测试文件仍引用旧包路径且构造参数未更新。

### 失败根因
v1 将 6 个薄适配器从 `orchestrator/impl/` 移至 `thinadapter/`，并将构造参数从 `Object service` 改为 `Phase4ServiceFacade<RQ,RS>`，同时新增 `Executor llmCallExecutor` 参数。但 `src/test/java/.../orchestrator/impl/` 下的 6 个测试文件未更新：
1. 类已不在同包，缺少 import 导致 `cannot find symbol`
2. 构造调用仍使用 `Object service` 而非 `Phase4ServiceFacade<...,...>`
3. 构造调用缺少新增的 `llmCallExecutor` 参数

### 待修复测试文件（共 6 个）
| # | 测试文件路径 |
|---|------------|
| 1 | `ai-impl/src/test/java/.../orchestrator/impl/DiagnosisCapabilityExecutorTest.java` |
| 2 | `ai-impl/src/test/java/.../orchestrator/impl/ImageAnalysisCapabilityExecutorTest.java` |
| 3 | `ai-impl/src/test/java/.../orchestrator/impl/AnalysisReportForLabTestCapabilityExecutorTest.java` |
| 4 | `ai-impl/src/test/java/.../orchestrator/impl/AnalysisReportForInspectionCapabilityExecutorTest.java` |
| 5 | `ai-impl/src/test/java/.../orchestrator/impl/RecommendExecutionOrderCapabilityExecutorTest.java` |
| 6 | `ai-impl/src/test/java/.../orchestrator/impl/RecommendExaminationCapabilityExecutorTest.java` |

### 每项文件需做的修改

以 `DiagnosisCapabilityExecutorTest.java` 为例，其余 5 个同理（替换对应的 DTO 类型和 capabilityId）。

**1. 添加导入**
```java
import com.aimedical.modules.ai.impl.thinadapter.DiagnosisCapabilityExecutor;
import com.aimedical.modules.ai.api.dto.base.Phase4ServiceFacade;
import java.util.concurrent.Executor;
```

**2. 修改 `createExecutor` 签名和实现**（原 line 139）
```java
// 修改前：
private DiagnosisCapabilityExecutor createExecutor(Object service, Duration thinAdapterTimeout) {
    return new DiagnosisCapabilityExecutor(
        service, metricsCollector, metricsStore,
        new AtomicReference<>(Map.of("DIAGNOSIS", Duration.ofSeconds(30))),
        null, new AtomicReference<>(Duration.ofSeconds(5)), thinAdapterTimeout,
        new AtomicReference<>(new ConcurrentHashMap<>()), objectMapper
    );
}

// 修改后：
private DiagnosisCapabilityExecutor createExecutor(
    Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> phase4Service,
    Duration thinAdapterTimeout) {
    return new DiagnosisCapabilityExecutor(
        phase4Service, metricsCollector, metricsStore,
        new AtomicReference<>(Map.of("DIAGNOSIS", Duration.ofSeconds(30))),
        null, new AtomicReference<>(Duration.ofSeconds(5)), thinAdapterTimeout,
        new AtomicReference<>(new ConcurrentHashMap<>()),
        Runnable::run,  // llmCallExecutor: direct execution for tests
        objectMapper
    );
}
```

**3. 修改测试中构建匿名 `Object` 为匿名 `Phase4ServiceFacade`**（原 line 114-125）
```java
// 修改前：
Object service = new Object() {
    @SuppressWarnings("unused")
    public DiagnosisResponse execute(DiagnosisRequest req) {
        return new DiagnosisResponse();
    }
};

// 修改后：
Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> phase4Service = req -> {
    return new DiagnosisResponse();
};
```

**4. 更新所有 `createExecutor(service, ...)` 调用为 `createExecutor(phase4Service, ...)`**
- 将每个测试方法中的 `createExecutor(service, ...)` 改为 `createExecutor(phase4Service, ...)`

### 涉及文件汇总
仅修改 6 个测试文件，不修改任何 main 源文件。

## 选择理由
R1 v1 的 main 源码修改已验证编译通过（`compile` 阶段成功）。仅测试文件残留旧引用导致 `testCompile` 失败。修复 6 个测试文件后 R1 即可通过。

## 任务上下文
- 测试文件使用 `package com.aimedical.modules.ai.impl.orchestrator.impl;`，薄适配器类已移至 `com.aimedical.modules.ai.impl.thinadapter`
- 薄适配器最终构造器签名（10 参数）：`(Phase4ServiceFacade<RQ,RS>, AiMetricsCollector, SlidingWindowMetricsStore, AtomicReference<capabilityTimeoutConfig>, AtomicReference<parseTimeoutConfig>, AtomicReference<parseTimeoutDefault>, Duration thinAdapterTimeout, AtomicReference<thinAdapterPerCapabilityConfig>, Executor llmCallExecutor, ObjectMapper objectMapper)`
- 测试中的 `service.execute(request)` 匿名调用必须在 `Phase4ServiceFacade.execute()` 中实现

## RETRY 说明
- **失败原因**：6 个薄适配器移入 `thinadapter/` 子包后，`src/test/java/.../orchestrator/impl/` 下的 6 个对应测试文件未更新导入和构造调用，导致 `cannot find symbol` 编译错误
- **修正方向**：为每个测试文件添加 `thinadapter` 包导入，将 `Object service` 替换为 `Phase4ServiceFacade<Req,Res>`，补充 `llmCallExecutor` 参数
