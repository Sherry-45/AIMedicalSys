# 计划审查报告（v15 r1）

## 审查结果
REJECTED

## 发现

### **[严重] RequestContextUtils 不存在导致生产代码编译错误**

`task_v15.md` 中 6 个执行器的 `doExtractDepartmentId/doExtractVisitId/doExtractPatientId/doExtractSessionId` 重写方法均调用 `RequestContextUtils.extractFromRequestContext("X-...-ID")`，但 `RequestContextUtils.java` 尚未创建——该类型属于 Task 18（Batch6 P3），在 `plan.md` 实施路线表中状态为 ☐ 未完成。

`plan.md:276` 虽注明"RequestContextUtils 需在 Task 18 才完成，当前测试中模拟提取逻辑"，但仅针对测试层面；生产代码中 6 个执行器直接引用 `RequestContextUtils` 类型，编译必然失败。`task_v15.md` 也未提供替代方案（如暂用 `super.doExtract*()` 回退或内联上下文提取逻辑）。

**修正方向**：两种方案择一——
- **选项 A（推荐）**：在本任务（Task 17）内提前创建 `RequestContextUtils` 类（最小实现，`ThreadLocal` 或 `RequestAttributes` 提取），并在 `doExtract*` 中调用它。这样 Task 18 再扩展即可。
- **选项 B**：临时移除 `RequestContextUtils` 依赖，`doExtract*` 直接返回 `super.doExtract*()`，待 Task 18 再用重写覆盖。

### **[严重] AiCallRecord.success()/failure() 静态工厂方法不存在**

代码模板（`task_v15.md:123-125`）的 **成功路径** 调用：
```java
metricsCollector.record(AiCallRecord.success(capabilityId, LocalDateTime.now(), elapsedMs,
    departmentId, null, 0, null, null, inputSummary, outputSummary,
    visitId, patientId, sessionId, callerRole, callerId, null));
```
**失败路径**（`:140-142`）调用：
```java
metricsCollector.record(AiCallRecord.failure(capabilityId, LocalDateTime.now(), elapsedMs,
    errorCode, cause.getMessage(), departmentId, inputSummary, visitId, patientId,
    sessionId, callerRole, callerId, null));
```

但 `AiCallRecord.java` 仅有 15 参数的构造器（`new AiCallRecord(capabilityId, modelId, promptVersion, userId, departmentId, sessionId, visitId, patientId, callerRole, callerId, elapsedMs, degraded, degradeReason, promptTokens, completionTokens)`），**不存在 `success()`/`failure()` 静态工厂方法**，也不存在接受 `LocalDateTime` 的构造器。生产代码将无法编译。

**修正方向**：将工厂方法调用替换为直接构造 `new AiCallRecord(...)`——参考 `AbstractCapabilityExecutor.doDegrade()` 中已有模式（`line 269-275`），成功路径传 `degraded=false, degradeReason=null, promptTokens=0, completionTokens=0`，失败路径传 `degraded=false` + errorCode/理由 + token 默认值。移除 `LocalDateTime` 参数（`AiCallRecord` 只有 `long elapsedMs`，不存储时间戳值对象）。

### **[一般] Phase 4 服务类型引用存在矛盾**

主模板（`task_v15.md:49` 以及构造器代码块）使用具体类型声明构造器参数：
```java
@Autowired(required = false) DiagnosisService diagnosisService,
```
但：
1. `com.aimedical.modules.diagnosis.service.DiagnosisService` 等 6 个 Phase 4 服务接口均不存在
2. `ai-impl/pom.xml` 中无任何 Phase 4 模块的 Maven 依赖
3. 即使 `required = false`，Java 编译器仍需解析类型引用——未导入且不在 classpath 上的类型会导致编译错误

`task_v15.md:301-303` "编译注意事项"明确要求"生产代码中使用 `@Autowired(required = false) Object diagnosisService` 接收，内部通过反射调用 `execute` 方法"，但主模板与此矛盾。

**修正方向**：统一按"编译注意事项"方案：构造器参数类型改为 `Object`，`doExecuteInternal()` 中在 `if (serviceField == null)` 之后通过反射调用 `execute()` 方法。`task_v15.md` 主模板应同步更新为 `Object` 类型，消除矛盾。

### **[轻微] isDtoEmpty 反射策略脆弱**

`isDtoEmpty()` 使用 `request.getClass().getMethods()` 检查非 Object 方法数是否为零来判定空 DTO。该策略在以下情况失败：
- DTO 类被 IDE 或 Lombok 自动生成 `toString()`/`hashCode()`/`equals()` 导致 `nonObjectMethodCount > 0`
- DTO 增加任何非业务字段的 getter（如 `getMeta()`）即使字段值为 null 也会误判为非空

建议：改为按 package 前缀检测是否属于已知空 DTO package（`com.aimedical.modules.ai.api.dto.*`），或添加空标记接口（如 `EmptyPhase4Dto` marker interface）。

## 修改要求（REJECTED）

### 1. RequestContextUtils 依赖问题（严重）
- **问题**：`doExtract*` 重写调用 `RequestContextUtils.extractFromRequestContext()`，该类型不存在
- **期望**：选选项 A（本任务提前创建带基础实现的 `RequestContextUtils`）或选项 B（临时走 `super.doExtract*()` 回退）

### 2. AiCallRecord.success/failure 工厂方法缺失（严重）
- **问题**：模板调用不存在的静态工厂方法，且参数含 `LocalDateTime`
- **期望**：替换为 `new AiCallRecord(...)`，按已有模式构造（参见 `AbstractCapabilityExecutor:269`），删掉 `LocalDateTime`

### 3. Phase 4 服务类型引用矛盾（一般）
- **问题**：主模板使用具体类型（`DiagnosisService`）但类型不存在且无依赖
- **期望**：统一为 `Object` + 反射的方案，更新主模板消除矛盾

### 4. isDtoEmpty 策略脆弱（轻微）
- **问题**：反射计数策略容易被非业务方法干扰
- **期望**：替换为 package 前缀检测或 marker interface，或明确标注此策略为"已知限制，Phase 4 DTO 非空后须更新"
