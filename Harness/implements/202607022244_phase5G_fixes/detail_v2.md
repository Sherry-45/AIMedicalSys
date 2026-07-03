# 详细设计（v2）

## 概述

修复 v1 验证失败的 6 个编译错误：薄适配器移入 `thinadapter` 子包后，`orchestrator/impl/` 下的测试文件仍引用旧包路径、旧 `Object service` 构造参数、缺少 `Executor llmCallExecutor` 参数。仅修改 6 个测试文件，不涉及 main 源码。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-impl/src/test/java/.../orchestrator/impl/DiagnosisCapabilityExecutorTest.java` | 修改 | 适配 DiagnosisCapabilityExecutor 新包路径和构造签名 |
| `ai-impl/src/test/java/.../orchestrator/impl/ImageAnalysisCapabilityExecutorTest.java` | 修改 | 同上 |
| `ai-impl/src/test/java/.../orchestrator/impl/AnalysisReportForLabTestCapabilityExecutorTest.java` | 修改 | 同上 |
| `ai-impl/src/test/java/.../orchestrator/impl/AnalysisReportForInspectionCapabilityExecutorTest.java` | 修改 | 同上 |
| `ai-impl/src/test/java/.../orchestrator/impl/RecommendExecutionOrderCapabilityExecutorTest.java` | 修改 | 同上 |
| `ai-impl/src/test/java/.../orchestrator/impl/RecommendExaminationCapabilityExecutorTest.java` | 修改 | 同上 |

## 类型定义

无新建类型。仅修改现有测试文件中的构造调用和类型引用。

### 全局修改模式（6 个文件一致）

**修改 1：添加导入声明**

在文件头部（`package` 之后、`import static` 之前）新增 3 行 import：

```java
import com.aimedical.modules.ai.impl.thinadapter.{TestedClassName};
import com.aimedical.modules.ai.api.dto.base.Phase4ServiceFacade;
import java.util.concurrent.Executor;
```

其中 `{TestedClassName}` 为对应薄适配器类名（如 `DiagnosisCapabilityExecutor`）。

**修改 2：替换 `Object service` 为 `Phase4ServiceFacade<Req,Res> phase4Service`**

文件中所有 `Object service` 声明替换为带类型参数的 `Phase4ServiceFacade`：

- `createExecutor(Object service, Duration thinAdapterTimeout)` → `createExecutor(Phase4ServiceFacade<ReqType, ResType> phase4Service, Duration thinAdapterTimeout)`
- 所有 `createExecutor(service, ...)` 调用 → `createExecutor(phase4Service, ...)`
- 匿名 Object 构造（`new Object() { ... }`）→ Lambda 实现 Phase4ServiceFacade

**修改 3：更新构造器调用——补充 `llmCallExecutor` 参数**

薄适配器构造器第 9 个参数为 `Executor llmCallExecutor`。测试中使用 `Runnable::run` 实现直接执行的简单线程策略。

### 各文件差异（DTO 类型映射）

| 测试文件 | 薄适配器类 | ReqType | ResType |
|---------|-----------|---------|---------|
| DiagnosisCapabilityExecutorTest | DiagnosisCapabilityExecutor | DiagnosisRequest | DiagnosisResponse |
| ImageAnalysisCapabilityExecutorTest | ImageAnalysisCapabilityExecutor | ImageAnalysisRequest | ImageAnalysisResponse |
| AnalysisReportForLabTestCapabilityExecutorTest | AnalysisReportForLabTestCapabilityExecutor | LabTestReportRequest | LabTestReportResponse |
| AnalysisReportForInspectionCapabilityExecutorTest | AnalysisReportForInspectionCapabilityExecutor | InspectionReportRequest | InspectionReportResponse |
| RecommendExecutionOrderCapabilityExecutorTest | RecommendExecutionOrderCapabilityExecutor | ExecutionOrderRequest | ExecutionOrderResponse |
| RecommendExaminationCapabilityExecutorTest | RecommendExaminationCapabilityExecutor | ExaminationRecommendRequest | ExaminationRecommendResponse |

### DiagnosisCapabilityExecutorTest 详细变更

（其余 5 个文件同理，替换对应的 DTO 类型和 capabilityId）

**a) 新增导入**（第 7 行附近，与已有 import 顺序保持一致）

```java
import com.aimedical.modules.ai.impl.thinadapter.DiagnosisCapabilityExecutor;
import com.aimedical.modules.ai.api.dto.base.Phase4ServiceFacade;
import java.util.concurrent.Executor;
```

**b) 修改 `createExecutor` 签名和实现**（原 line 139-146）

```java
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

**c) 替换 `shouldSucceedWithValidDelegation` 中匿名 Object 为 Phase4ServiceFacade Lambda**（原 line 48-53）

```java
Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> phase4Service = req -> {
    return new DiagnosisResponse();
};
DiagnosisCapabilityExecutor executor = createExecutor(phase4Service, Duration.ofSeconds(30));
```

**d) 替换 `shouldDegradeOnTimeout` 中匿名 Object**（原 line 65-71）

```java
Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> phase4Service = req -> {
    Thread.sleep(5000);
    return new DiagnosisResponse();
};
DiagnosisCapabilityExecutor executor = createExecutor(phase4Service, Duration.ofMillis(50));
```

**e) 替换 `shouldReturnFailureOnPhase4BusinessException` 中匿名 Object**（原 line 82-87）

```java
Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> phase4Service = req -> {
    throw new Phase4BusinessException("业务异常") {};
};
DiagnosisCapabilityExecutor executor = createExecutor(phase4Service, Duration.ofSeconds(30));
```

**f) 更新 `shouldSucceedWhenThinAdapterPerCapabilityConfigIsNull` inline 构造**（原 line 97-109）

参数变更：第 1 个参数从 `service` 改为 `phase4Service`，第 8-9 个参数间插入 `llmCallExecutor`（`Runnable::run`）：

```java
Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> phase4Service = req -> {
    return new DiagnosisResponse();
};
DiagnosisCapabilityExecutor executor = new DiagnosisCapabilityExecutor(
    phase4Service, metricsCollector, metricsStore,
    new AtomicReference<>(Map.of("DIAGNOSIS", Duration.ofSeconds(30))),
    null, new AtomicReference<>(Duration.ofSeconds(5)), Duration.ofSeconds(30),
    null, Runnable::run, objectMapper
);
```

**g) 更新 `shouldDegradeOnTimeoutWhenThinAdapterPerCapabilityConfigIsNull` inline 构造**（原 line 118-131）

同理替换参数：

```java
Phase4ServiceFacade<DiagnosisRequest, DiagnosisResponse> phase4Service = req -> {
    Thread.sleep(5000);
    return new DiagnosisResponse();
};
DiagnosisCapabilityExecutor executor = new DiagnosisCapabilityExecutor(
    phase4Service, metricsCollector, metricsStore,
    new AtomicReference<>(Map.of("DIAGNOSIS", Duration.ofSeconds(30))),
    null, new AtomicReference<>(Duration.ofSeconds(5)), Duration.ofMillis(50),
    null, Runnable::run, objectMapper
);
```

**h) 更新 `doExecuteInternal` 调用中 `createExecutor(service, ...)` 为 `createExecutor(phase4Service, ...)`**

- line 28: `createExecutor(null, Duration.ofSeconds(30))` → 保持（null 合法，Phase4ServiceFacade 为 null 时薄适配器构造不报错，doExecuteInternal 内部处理）
- line 38: 同上
- line 54: `createExecutor(service, ...)` → `createExecutor(phase4Service, ...)`（配合上方变量名变更）
- line 72: 同上
- line 88: 同上

## 错误处理

无需新增错误处理逻辑。测试中使用 `Phase4BusinessException` 的匿名子类（`new Phase4BusinessException() {}`）作为 lambda 内的 throw 语句，行为与现有测试一致。

## 行为契约

1. 所有 6 个测试文件保持原包路径 `com.aimedical.modules.ai.impl.orchestrator.impl` 不变
2. 薄适配器类通过 `import com.aimedical.modules.ai.impl.thinadapter.*` 导入，无需移动测试文件
3. `Phase4ServiceFacade<Req,Res>` 是 `@FunctionalInterface` 语义（单方法接口），可直接 lambda 实现
4. `Runnable::run` 方法引用等价于 `command -> command.run()`，供测试中直接执行 CompletableFuture.supplyAsync 的任务体，不引入异步线程

## 依赖关系

- 测试文件依赖 `ai-api` 模块 DTO 类型（已有）
- 测试文件依赖 `ai-impl` 模块的 `thinadapter` 子包类（新增）
- 测试文件依赖 `java.util.concurrent.Executor` 类型（新增）

## 修订说明（v2 R1）

| 审查意见 | 修改措施 |
|---------|---------|
| v1 验证 FAILED：6 个测试文件编译错误——薄适配器包移动后测试文件引用旧包路径、旧构造参数 | 新增 6 个测试文件的详细设计：补全 `thinadapter` 导入、替换 `Object service` 为 `Phase4ServiceFacade<Req,Res>`、补充 `Executor llmCallExecutor` 构造参数 |
