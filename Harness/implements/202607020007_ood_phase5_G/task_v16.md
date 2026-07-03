# 任务指令（v16）

## 动作
NEW

## 任务描述
在 ai-impl/router/ 包实现 ModelRouter 路由系统（3 个类型），同步修改 AbstractCapabilityExecutor 适配 ModelRoute 返回类型。

**预期文件清单：**

| # | 文件路径 | 操作 | 职责 |
|---|---------|------|------|
| 1 | `router/ModelRouter.java` | 修改 | 接口返回类型 `Object` → `ModelRoute` |
| 2 | `router/ModelRoute.java` | 新建 | 路由条目值对象（endpointId/clientType/authType/modelId/endpointUrl/weight/timeoutMs/parameters） |
| 3 | `router/DefaultModelRouter.java` | 新建 | @Service 实现，AtomicReference 路由表 @PostConstruct 初始化 + @Scheduled 热刷新 + 权重随机选择 |
| 4 | `orchestrator/AbstractCapabilityExecutor.java` | 修改 | routeResult 从 `String.valueOf(routeResult)` 改为 `ModelRoute` 字段访问（getEndpointId/getModelId/getClientType），含 L352-L387 三处修改 |
| 5 | `router/AiRouterProperties.java` | 新建 | @ConfigurationProperties(prefix="ai.router") 存根类，持有 `Map<String, List<ModelRoute>> routes` |
| 6 | `orchestrator/AbstractCapabilityExecutorTest.java` | 修改 | 15 处 ModelRouter lambda mock 从 `(capId, req) -> "model-1"` 改为 `(capId, req) -> ModelRoute.of("model-1")`（1 处返回 null 的不修改） |
| 7 | `orchestrator/DiscussionConclusionCapabilityExecutorTest.java` | 修改 | 3 处 ModelRouter lambda mock 同上适配 |
| 8 | `orchestrator/TriageCapabilityExecutorTest.java` | 修改 | 1 处 ModelRouter lambda mock 同上适配 |
| 9 | `router/ModelRouteTest.java` | 新建 | ModelRoute 值对象契约测试（构造器/getter/of()工厂/parameters 防御性拷贝/equals&hashCode） |
| 10 | `router/DefaultModelRouterTest.java` | 新建 | 路由/权重选择/并发安全/生命周期（@PostConstruct/@Scheduled） |

## 选择理由
Batch4 P2 首项任务。ModelRouter 是底座管线的路由枢纽——被 AbstractCapabilityExecutor 及全部 7 个完整管线 CapabilityExecutor（Triage/PrescriptionCheck/MedicalRecordGen/PrescriptionAssist/KbQuery/Schedule/DiscussionConclusion）直接依赖。当前 ModelRouter 为存根接口返回 Object，需升级为真实 ModelRoute 值对象 + DefaultModelRouter 实现。零外部依赖（仅依赖已完成的 ClientType/AuthType 枚举）。

## 任务上下文

### 设计文档摘录（06_ood_phase5_G.md）

**ModelRouter 接口**（§3.2.7）：
```
+ route(String capabilityId, Object request): ModelRoute
```
- 根据能力标识和调用上下文返回对应的 ModelRoute
- 返回 null 表示无可用路由（触发 NO_AVAILABLE_ROUTE 降级）

**DefaultModelRouter**（§3.2.7）：
- @Service 实现
- `AtomicReference<Map<String, ModelRoute[]>>` 线程安全路由表
- @PostConstruct 从配置源加载路由表（初次加载）
- @Scheduled(fixedDelay = 60000) 定时刷新
- 支持 RouteConfigChangedEvent 事件驱动刷新（@EventListener）
- 多 route 时按 weight 权重随机选择

**ModelRoute**（§3.2.8）：
| 字段 | 类型 | 说明 |
|------|------|------|
| endpointId | String | 端点唯一标识 |
| clientType | ClientType | HTTP_API / SPRING_AI |
| authType | AuthType | API_KEY / OAUTH2 / NONE |
| modelId | String | 模型标识 |
| endpointUrl | String | 端点 URL |
| weight | int | 权重（默认 1） |
| timeoutMs | long | 超时毫秒（默认 30000） |
| parameters | Map<String, Object> | 额外参数（可空） |

## 已有代码上下文

### ModelRouter.java（当前存根，需修改）
```java
public interface ModelRouter {
    Object route(String capabilityId, Object request);  // 返回类型改为 ModelRoute
}
```

### AbstractCapabilityExecutor.java（需修改 L352-L387，共 3 处）

**修改点 1（L352-L357）：路由结果类型切换**
```java
// 修改前：
Object routeResult = modelRouter.route(capabilityId, request);
if (routeResult == null) {
    return doDegrade(..., NO_AVAILABLE_ROUTE, ...);
}

// 修改后：
ModelRoute routeResult = modelRouter.route(capabilityId, request);
if (routeResult == null) {
    return doDegrade(..., NO_AVAILABLE_ROUTE, ...);
}
```

**修改点 2（L359 + L367-L375）：端点健康检查 + 选项设置 + LlmChatRequest 构造**
```java
// 修改前：
String healthState = endpointHealthManager.getState(String.valueOf(routeResult));
// ...
options.setModelId(String.valueOf(routeResult));
// ...
LlmChatRequest llmChatRequest = new LlmChatRequest(messages, options, ClientType.HTTP_API, null, null);

// 修改后：
String healthState = endpointHealthManager.getState(routeResult.getEndpointId());
// ...
options.setModelId(routeResult.getModelId());
// ...
ClientType clientType = routeResult.getClientType() != null ? routeResult.getClientType() : ClientType.HTTP_API;
LlmChatRequest llmChatRequest = new LlmChatRequest(messages, options, clientType, null, null);
```

**修改点 3（L381-L383，超时降级路径中的 routeResult 引用）：**
```java
// 修改前：
return doDegrade(startTime, DegradationReason.TIMEOUT.getCode(),
    request, capabilityId, departmentId, callerRole, callerId,
    visitId, patientId, sessionId, inputSummary, null, promptVersion, String.valueOf(routeResult), sentinelReason);

// 修改后：
return doDegrade(startTime, DegradationReason.TIMEOUT.getCode(),
    request, capabilityId, departmentId, callerRole, callerId,
    visitId, patientId, sessionId, inputSummary, null, promptVersion, routeResult.getModelId(), sentinelReason);
```

### 测试文件中的 ModelRouter mock（共 19 处需更新）

现有 mock 模式：
```java
ModelRouter mockRouter = (capId, req) -> "model-1";
```
需改为：
```java
ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
```

受影响的 mock 计数（精确）：
- `AbstractCapabilityExecutorTest.java`：15 处返回 `"model-1"`（另有 1 处返回 `null`，无需修改）
- `DiscussionConclusionCapabilityExecutorTest.java`：3 处
- `TriageCapabilityExecutorTest.java`：1 处
- 共计：19 处修改

## 实施要点

1. **ModelRoute.of(String modelId) 便捷工厂**：在 ModelRoute 中提供静态工厂方法，返回所有其他字段为 null/默认值的实例，确保测试 mock 最小化修改
2. **DefaultModelRouter 配置加载（AiRouterProperties 存根）**：本任务新建 `@ConfigurationProperties(prefix = "ai.router")` 存根类，持有 `Map<String, List<ModelRoute>> routes` 字段。DefaultModelRouter 通过 @Autowired 注入该配置，在 @PostConstruct 中构建路由表 Map。此存根后续可由 AiPlatformConfig（Task 18）吸收或委托。
3. **权重随机选择**：`selectWeighted(ModelRoute[] routes)` — 按 weight 比例随机选择，所有 weight 为 0 时等概率
4. **并发安全**：`AtomicReference` + `Collections.unmodifiableMap` + 数组不可变
5. **@Scheduled 刷新**：刷新时本地构造完整副本后 CAS 替换，读路径零锁
6. **向后兼容**：所有现有 CapabilityExecutor 生产代码无需修改（仅 AbstractCapabilityExecutor 基类适配）
7. **clientType null 安全**：AbstractCapabilityExecutor 中使用 `routeResult.getClientType() != null ? routeResult.getClientType() : ClientType.HTTP_API` 作为 LlmChatRequest 构造器的 clientType 参数，确保下游 DelegatingLlmChatService 不会 NPE

## 修订说明（v16 r1）

| 审查意见 | 修改措施 |
|---------|---------|
| 测试文件修改未纳入计划 — 3 个测试文件 ~18+ 处 ModelRouter mock 需从返回 String 改为返回 ModelRoute.of("model-1") | 在涉及文件表中新增 3 个测试文件修改项（rows 5-7），任务描述补充 18+ mock 更新范围；测试文件 mock 全部从 `(capId, req) -> "model-1"` 改为 `(capId, req) -> ModelRoute.of("model-1")` |
| ModelRoute.of(String modelId) 便捷工厂方法未纳入 | 在 ModelRoute 值对象定义中明确标注需提供 `public static ModelRoute of(String modelId)` 便捷工厂方法 |
| 路线表涉及文件列仅列出 3 个文件，未包含 AbstractCapabilityExecutor.java 及 3 个测试文件 | 路线表涉及文件列扩展为 7 个文件（4 生产 + 3 测试） |

## 修订说明（v16 r2）

| 审查意见 | 修改措施 |
|---------|---------|
| [严重] `options.setClientType()` 编译错误 — LlmChatOptions 类无此方法 | 移除 `options.setClientType()` 调用，改为修改 `new LlmChatRequest(messages, options, ClientType.HTTP_API, null, null)` 为 `new LlmChatRequest(messages, options, routeResult.getClientType() != null ? routeResult.getClientType() : ClientType.HTTP_API, null, null)`；同步更新 task_v16.md 代码伪代码 |
| [严重] 新建生产代码缺少测试文件规划（ModelRouteTest、DefaultModelRouterTest） | 在涉及文件表中新增 rows 9-10：`ModelRouteTest.java`（值对象契约）和 `DefaultModelRouterTest.java`（路由/权重/并发/生命周期） |
| [一般] AbstractCapabilityExecutor L383 `String.valueOf(routeResult)` 遗漏 | 在 task_v16.md 新增"修改点 3"，将 L381-L383 超时降级路径中的 `String.valueOf(routeResult)` 改为 `routeResult.getModelId()` |
| [一般] DefaultModelRouter 配置源未明确 | 新增实施要点第 2 条，明确新建 `@ConfigurationProperties(prefix="ai.router")` AiRouterProperties 存根类；涉及文件表新增 row 5 |
| [一般] 测试 mock 计数不精确（~16 → 15） | 将"~16 处"改为"15 处"，补充说明 1 处返回 null 的不修改；总计 19 处 |
| [轻微] routeResult.getClientType() 空值安全未提及 | 新增实施要点第 7 条，明确 null 回退策略：`routeResult.getClientType() != null ? routeResult.getClientType() : ClientType.HTTP_API` |
