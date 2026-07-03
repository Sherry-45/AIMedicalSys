# 计划审查报告（v15 r2）

## 审查结果
REJECTED

## 发现

### **[严重]** 薄适配器传递 `null llmCallExecutor` 导致 `CompletableFuture.supplyAsync` NPE

`AbstractCapabilityExecutor.execute()` 第 156 行调用 `CompletableFuture.supplyAsync(..., llmCallExecutor)`。JDK 的 `supplyAsync(Supplier, Executor)` 在 executor 为 null 时立即抛出 `NullPointerException`（`screenExecutor()` 方法硬校验）。

计划的参数映射表（#16）明确将 `llmCallExecutor` 标记为 `null`，且薄适配器构造器不包含任何 `Executor` 形参，直接向 `super()` 传递 `null`。由于 `execute()` 是 `final` 方法不可覆盖，所有 6 个薄适配器在首次调用 `execute()` 时均会 NPE，完全无法运行。

**修正方向（三选一）：**
1. **修改 AbstractCapabilityExecutor**：在 `execute()` 中为 `llmCallExecutor == null` 时使用 `ForkJoinPool.commonPool()` 作为默认值。
2. **薄适配器提供默认线程池**：在构造器中创建 `Executors.newSingleThreadExecutor()` 并传入 `super()`，同时添加 `@PreDestroy` 关闭资源。
3. **重构继承关系**：薄适配器不继承 AbstractCapabilityExecutor，改为直接实现 CapabilityExecutor 接口，绕过 `final execute()` 模板方法。

推荐方案 1（改动最小，不影响现有执行器）。方案需在 plan/task 中明确指定。

### **[一般]** 测试策略中 `mock(Object.class)` 反射桩不可用

计划测试策略建议 `mock(Object.class, withSettings().defaultAnswer(Mockito.RETURNS_DEFAULTS))`，然后通过 `service.getClass().getMethod("execute", request.getClass())` 设置反射桩。但 Mockito 代理类的 `getClass()` 返回 CGLIB/ByteBuddy 代理类，其上 `getMethod("execute", ...)` 会抛出 `NoSuchMethodException`——代理类不声明 `execute` 方法。该方案在实现阶段会造成 test-compile 失败或运行期错误。

计划虽提及匿名类替代方案（`new Object() { public Response execute(Request req) { ... } }`），但 mock 路径是误导性描述，实现者可能优先尝试 mock 方案，浪费调试时间。

**修正方向：** 从测试策略中移除 `mock(Object.class)` 方案，仅保留匿名类方案，并补充示例代码防止实现歧义。

### **[轻微]** `@Autowired(required = false) Object` 注入在 Phase 4 服务上线后可能失效

当 Phase 4 模块后续添加真实 `@Service` 后，Spring 上下文中会出现多个类型为 `Object` 的 bean（因为一切 Java 对象都是 Object 子类），导致 `@Autowired(required = false) Object` 无法唯一匹配目标 Phase 4 服务，Spring 可能注入错误的 bean 或抛出 `NoUniqueBeanDefinitionException`。

当前底座切流初期 Phase 4 服务不存在（注入 null），该设计可短期接受，但需在代码和计划中明确标记为临时方案，并关联 Phase 5 重构 TODO（届时应改为具体接口类型或 `@Qualifier` 限定）。

**修正方向：** 在 `doExecuteInternal` javadoc 或类注释中添加 `@TODO Phase5:` 标记，提示后续使用实际服务接口类型替换 `Object`。

## 修改要求

| 严重程度 | 问题 | 期望修正方向 |
|---------|------|------------|
| 严重 | llmCallExecutor=null 导致 `supplyAsync` NPE | 修改 `AbstractCapabilityExecutor.execute()`：`llmCallExecutor == null` 时使用 `ForkJoinPool.commonPool()`；或薄适配器构造器中创建默认线程池传入。推荐方案 1。 |
| 一般 | mock(Object.class) 反射桩不可用 | 移除测试策略中的 mock 方案，仅保留匿名类方式，补充可编译的示例代码。 |
| 轻微 | `@Autowired Object` 临时方案的后续风险 | 添加 `@TODO Phase5:` 注释标记重构时机，不影响当前实现但需记录。 |
