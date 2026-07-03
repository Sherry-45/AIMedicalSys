# 设计审查报告（v3 r2）

## 审查结果
REJECTED

## 发现

### [一般] doExtractDepartmentId 等方法的行为描述自相矛盾

**位置**：§字段提取方法默认行为 表格及下方说明（行 311-324）

**问题**：表格中 `doExtractDepartmentId`/`doExtractVisitId`/`doExtractPatientId`/`doExtractSessionId` 的默认实现列写为"若 `request instanceof AiRequestBase` 则调用 `((AiRequestBase) request).getDepartmentId()`"，但 AiRequestBase 定义为空 abstract class（仅有 protected 无参构造器），**不存在** `getDepartmentId()` 等方法。如果实现者按表格字面编码，将产生编译错误。

下方说明虽澄清"默认实现暂返回 null"，但表格与说明直接矛盾。设计文档应保证可独立指导实现，不应依赖实现者自行调和矛盾信息。

**修正方向**：表格默认实现列应直接写"return null"，并在"后续批次"备注中说明待 AiRequestBase 补齐 getter 后改为 instanceof + cast 模式。

---

### [一般] 多数字段声明后无使用场景

**位置**：§AbstractCapabilityExecutor 字段（行 200-216）

**问题**：`thinAdapterPerCapabilityConfig`、`parseTimeoutConfig`、`parseTimeoutDefault` 三个字段被声明为 `protected final` 并在构造器中赋值，但整个设计中没有任何流程说明它们被何时/何处使用。这导致：
- 实现者不清楚这些字段是否应被某个方法引用
- 构造器参数膨胀但实际无用，增加维护负担
- `execute()` 超时查找逻辑（行 252-253）跳过了 `thinAdapterPerCapabilityConfig`，直接回到 `thinAdapterTimeout`，与字段存在的意图矛盾

**修正方向**：
- 要么在 `execute()` 超时查找链路中加入 `thinAdapterPerCapabilityConfig`，形成"capabilityTimeoutConfig → thinAdapterPerCapabilityConfig → thinAdapterTimeout"三级回退
- 要么在 `executeStandardPipeline()` 的预期用途中说明这些字段的归属，或移除不必要的字段定义

---

### [一般] execute() 降级预检流程对 metricsStore / degradationStrategyMapRef 不做 null 保护

**位置**：§模板方法 execute() 伪代码流程 步骤 4（行 250）

**问题**：构造器注明"各参数均可为 null"，但步骤 4 直接调用 `metricsStore.buildDegradationContext(...)` 和 `degradationStrategyMapRef.get()`。若任一为 null，执行时直接 NPE，且该路径发生在 supplyAsync 之前（容器线程），会直接传播为未捕获异常，违背了"doDegrade 不抛出异常"的设计目标。

**修正方向**：
- 明确记录 `metricsStore` 和 `degradationStrategyMapRef` 为 `@NonNull`（与"前 5 个可为 null"区分开）
- 或补充 null 判断：若 metricsStore == null 则跳过降级预检直接走正常路径

---

### [一般] exceptionally 分支仅描述 TimeoutException 处理，遗漏其他异常

**位置**：§模板方法 execute() 伪代码步骤 7（行 254）

**问题**：`CompletableFuture.exceptionally()` 会捕获所有异常，但设计只描述了 `TimeoutException` 的处理路径。当 `doExecuteInternal()` 抛出业务 `RuntimeException` 时（经过 `supplyAsync` 包装为 `CompletionException`），`exceptionally` 同样会触发，但设计未规定该场景的行为——是同样调用 `doDegrade()` 还是重新抛出？`isKnownPhase4BusinessException()` 方法存在但未在 exceptionally 中被引用。

**修正方向**：
- 补充 exceptionally 分支的完整处理逻辑：区分 TimeoutException → doDegrade；非 TimeoutException → 检查 isKnownPhase4BusinessException → 调用 doDegrade 或 记录日志后重新抛出
- 如果意图是让非超时异常沿 CompletableFuture 向上传播，也应明确说明

---

### [一般] orTimeout 参数的 thinAdapterTimeout 可能为 null 导致 NPE

**位置**：§模板方法 execute() 伪代码步骤 6（行 252-253）

**问题**：超时查找逻辑为"从 capabilityTimeoutConfig 中按 capabilityId 查找，不存在时使用 thinAdapterTimeout"。但 `thinAdapterTimeout` 类型为 `Duration`，构造器允许任意参数为 null。若 capabilityTimeoutConfig 中无匹配项且 thinAdapterTimeout 为 null，则 `orTimeout(null)` 抛出 NPE。

**修正方向**：
- 约束 thinAdapterTimeout 为 @NonNull 并文档化最低必须提供
- 或在查找失败时使用一个默认值（如 `Duration.ofSeconds(30)`）兜底

---

### [轻微] CapabilityExecutor Javadoc 中 @implNote / @apiNote 用法与任务要求不符

**位置**：§CapabilityExecutor 接口定义（行 143-145）

**问题**：任务要求"使用 @implNote 声明 request DTO 约定为只读对象，使用 @apiNote 声明不可变 DTO 与防御性拷贝兼容性说明"，但设计将两者合并写在 @apiNote 中，未使用 @implNote。按 Java 惯用约定，@implNote 用于面向实现者的注解，更符合作者本意。

**修正方向**：拆分 Javadoc 注释，读对象约定使用 `@implNote`，防御性拷贝说明使用 `@apiNote`。

---

### [轻微] elapsedInDoExecuteInternal 字段生命周期未说明

**位置**：§字段列表（行 207）

**问题**：`volatile long elapsedInDoExecuteInternal` 声明为字段，但设计中没有任何地方说明它由谁设置、何时设置、在何处使用。仅 §行为契约 提及"用于超时诊断"，但模板方法 execute() 流程和 doExecuteInternal() 抽象方法签名中均未涉及。实现者无从得知该字段的赋值时机。

**修正方向**：说明该字段由模板方法 execute() 在调用 doExecuteInternal 前后维护（或在 supplyAsync lambda 中），或由子类 doExecuteInternal 负责设置。

---

### [轻微] execute() 中 buildDegradationContext 的参数 requestType 来源不明确

**位置**：§模板方法 execute() 伪代码步骤 4（行 250）

**问题**：`metricsStore.buildDegradationContext(capabilityId, requestType)` 中的 `requestType` 变量未定义来源。应从 `inputType.getSimpleName()` 或 `request.getClass().getSimpleName()` 派生。

**修正方向**：明确 requestType 的取值来源，例如 `request.getClass().getSimpleName()`。

## 修改要求

以上 5 项 **[一般]** 问题须全部修正后方可通过。修正方向已附在每条发现中。修正后请标记修订说明并提交下一轮审查。
