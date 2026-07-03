# 计划审查报告（v16 r2）

## 审查结果
REJECTED

## 发现

### **[严重] `options.setClientType()` 编译错误 — LlmChatOptions 类无此方法**

task_v16.md 第 85 行和 plan.md 第 398 行描述 AbstractCapabilityExecutor 修改内容包含 `options.setClientType(routeResult.getClientType())`，但 `LlmChatOptions.java:49` 不存在 `setClientType(ClientType)` 方法。当前 `clientType` 通过 `LlmChatRequest` 构造器第 3 参数硬编码为 `ClientType.HTTP_API`（`AbstractCapabilityExecutor.java:375`）。

**修正方向**：移除 `options.setClientType()` 调用，改为修改 `new LlmChatRequest(messages, options, ClientType.HTTP_API, null, null)` 为 `new LlmChatRequest(messages, options, routeResult.getClientType(), null, null)`。同时需处理 `routeResult.getClientType()` 可能为 null 时的回退（如 `routeResult.getClientType() != null ? routeResult.getClientType() : ClientType.HTTP_API`）。

### **[严重] 新建生产代码缺少测试文件规划**

任务新建 `ModelRoute.java`（值对象）和 `DefaultModelRouter.java`（@Service 实现），但涉及文件表和任务描述中完全未提及对应的测试文件（如 `ModelRouteTest.java`、`DefaultModelRouterTest.java`）。具体缺失覆盖：

- **ModelRoute**：构造器/getter/`of()` 工厂方法/equals & hashCode（如有）/`Map<String, Object> parameters` 防御性拷贝
- **DefaultModelRouter**：`route()` 返回正确路由/返回 null（无可用路由）/权重随机选择（`selectWeighted`）/所有 weight=0 时等概率/`AtomicReference` 并发安全/@PostConstruct 初始加载/@Scheduled 刷新/空路由表/`getClientType()`/`getEndpointId()` 字段正确性

计划未说明测试策略如何覆盖上述契约。此前多轮审查（如 v3 r1、v8 r1、v11 r1）均要求为新建类型补充测试规划，本任务不应例外。

### **[一般] `AbstractCapabilityExecutor.java` 第 383 行 routeResult 使用遗漏**

task_v16.md 仅描述 L352-L375 范围的代码修改，但 `executeStandardPipeline` 中第 381-384 行剩余超时检查路径也存在 `String.valueOf(routeResult)` 作为 `doDegrade()` 的 `modelId` 参数，未标注需改为 `routeResult.getModelId()`：

```java
if (remainingMs <= 0) {
    return doDegrade(startTime, DegradationReason.TIMEOUT.getCode(),
        request, capabilityId, departmentId, callerRole, callerId,
        visitId, patientId, sessionId, inputSummary, null, promptVersion,
        String.valueOf(routeResult), sentinelReason);  // ← 遗漏
}
```

### **[一般] DefaultModelRouter 配置源未明确**

设计文档指出 DefaultModelRouter 依赖 `AiRouterProperties` YAML 配置，但该类不存在（当前位 Task 18 Batch6 P3 范围，晚于本任务）。plan.md 和 task_v16.md 仅模糊描述"接收 `List<ModelRoute>` 通过 @Autowired 注入（或通过 @Value 配置）"，未明确具体配置加载方案（如在本任务创建 `AiRouterProperties` 存根，或使用 `@Value` 逐字段绑定 + SpEL）。配置源不确定可能导致实现阶段发现阻塞。

**修正方向**：明确 DefaultModelRouter 的配置加载方式——建议在本任务创建最小 `@ConfigurationProperties(prefix = "ai.router")` 存根类（持有 `Map<String, List<ModelRoute>> routes`），后续 `AiPlatformConfig` 可吸收或委托。此存根的设计模式与本项目既有 `@ConfigurationProperties` 实践一致。

### **[一般] 测试 mock 计数不精确**

plan.md 第 397 行标注 `AbstractCapabilityExecutorTest` 为"~16 处 ModelRouter mock 需修改"，但实际该文件中 1 处 mock 返回 `null`（第 430 行，用于 `NO_AVAILABLE_ROUTE` 测试路径，保留 `(capId, req) -> null` 无需修改），仅 15 处返回 `"model-1"` 需要改为 `ModelRoute.of("model-1")`。总数：15（AbstractCapabilityExecutorTest）+ 3（DiscussionConclusionCapabilityExecutorTest）+ 1（TriageCapabilityExecutorTest）= 19 处修改。计数偏差虽不影响正确性，但有误导性。

### **[轻微] routeResult.getClientType() 空值安全未提及**

`DefaultModelRouter` 返回的 `ModelRoute` 的 `clientType` 字段可能为 null（`ModelRoute.of(String modelId)` 工厂方法将其置为 null，来自 YAML 配置也可能缺少 `client-type`）。AbstractCapabilityExecutor 将其传入 `LlmChatRequest` 构造器作为 `clientType` 参数后，下游 `DelegatingLlmChatService` 可能 NPE。计划未提及此空值处理策略。

## 修改要求

1. **[严重]** 修复 `options.setClientType()` 编译错误：改为修改 `LlmChatRequest` 构造器参数，补充 null 安全回退；同步更新 task_v16.md 代码伪代码和 plan.md 描述。

2. **[严重]** 补充新建类型的测试规划：`ModelRouteTest`（值对象契约 + of() 工厂）和 `DefaultModelRouterTest`（路由/权重/并发/生命周期），扩展涉及文件表。

3. **[一般]** 在 task_v16.md 涉及文件表中标注 `AbstractCapabilityExecutor.java` 第 383 行的 `String.valueOf(routeResult)` → `routeResult.getModelId()` 修改。

4. **[一般]** 明确 DefaultModelRouter 配置加载方案，建议在本任务创建最小 `AiRouterProperties` 存根类，或明确使用 `@Value` SpEL 绑定的具体方案，更新 plan.md 描述。

5. **[一般]** 更正 AbstractCapabilityExecutorTest mock 修改计数（15 处而非 ~16 处）。

6. **[轻微]** 补充 `routeResult.getClientType()` null 安全策略说明。
