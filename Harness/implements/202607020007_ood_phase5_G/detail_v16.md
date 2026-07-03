# 详细设计（v16）

## 概述

在 `ai-impl/router/` 包实现 ModelRouter 路由系统（3 个类型），同步修改 AbstractCapabilityExecutor 适配 ModelRoute 返回类型。

## 文件规划

基路径（源码）：`AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/`
基路径（测试）：`AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/`

### 生产代码（5 个文件）

| # | 文件路径 | 操作 | 职责 |
|---|---------|------|------|
| 1 | `router/ModelRouter.java` | 修改 | 接口返回类型 `Object` → `ModelRoute` |
| 2 | `router/ModelRoute.java` | 新建 | 路由条目值对象（endpointId/clientType/authType/modelId/endpointUrl/weight/timeoutMs/parameters） |
| 3 | `router/DefaultModelRouter.java` | 新建 | @Service 实现，AtomicReference 路由表 @PostConstruct 初始化 + @Scheduled 热刷新 + 权重随机选择 |
| 4 | `router/AiRouterProperties.java` | 新建 | @ConfigurationProperties(prefix="ai.router") 存根类，持有 `Map<String, List<ModelRoute>> routes` |
| 5 | `orchestrator/AbstractCapabilityExecutor.java` | 修改 | routeResult 从 `String.valueOf(routeResult)` 改为 `ModelRoute` 字段访问（executeStandardPipeline 中三处修改） |

### 测试代码（5 个文件）

| # | 文件路径 | 操作 | 主要测试维度 |
|---|---------|------|-------------|
| 6 | `orchestrator/AbstractCapabilityExecutorTest.java` | 修改 | 15 处 ModelRouter lambda mock 从 `(capId, req) -> "model-1"` 改为 `(capId, req) -> ModelRoute.of("model-1")`（1 处返回 null 的不修改） |
| 7 | `orchestrator/DiscussionConclusionCapabilityExecutorTest.java` | 修改 | 3 处 ModelRouter lambda mock 同上适配 |
| 8 | `orchestrator/TriageCapabilityExecutorTest.java` | 修改 | 1 处 ModelRouter lambda mock 同上适配 |
| 9 | `router/ModelRouteTest.java` | 新建 | ModelRoute 值对象契约测试（构造器/getter/of()工厂/parameters 防御性拷贝/equals&hashCode） |
| 10 | `router/DefaultModelRouterTest.java` | 新建 | 路由/权重选择/并发安全/生命周期（@PostConstruct/@Scheduled） |

## 类型定义

### 1. ModelRouter 接口（修改）

**形态**：interface
**包路径**：`com.aimedical.modules.ai.impl.router`

```java
package com.aimedical.modules.ai.impl.router;

public interface ModelRouter {
    ModelRoute route(String capabilityId, Object request);
}
```

**公开接口**：仅 `route(String capabilityId, Object request): ModelRoute`，返回 null 表示无可用路由
**构造方式**：不适用
**类型关系**：无继承/实现

---

### 2. ModelRoute 值对象（新建）

**形态**：class（不可变值对象）
**包路径**：`com.aimedical.modules.ai.impl.router`
**职责**：封装单条模型路由的完整配置元数据

```java
package com.aimedical.modules.ai.impl.router;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.aimedical.modules.ai.impl.client.AuthType;
import com.aimedical.modules.ai.impl.client.ClientType;

public class ModelRoute {

    private final String endpointId;
    private final ClientType clientType;
    private final AuthType authType;
    private final String modelId;
    private final String endpointUrl;
    private final int weight;
    private final long timeoutMs;
    private final Map<String, Object> parameters;

    public ModelRoute(String endpointId, ClientType clientType, AuthType authType,
                      String modelId, String endpointUrl, int weight,
                      long timeoutMs, Map<String, Object> parameters) {
        this.endpointId = endpointId;
        this.clientType = clientType;
        this.authType = authType;
        this.modelId = modelId;
        this.endpointUrl = endpointUrl;
        this.weight = weight;
        this.timeoutMs = timeoutMs;
        this.parameters = parameters != null
            ? Collections.unmodifiableMap(new HashMap<>(parameters))
            : Collections.emptyMap();
    }

    // 便捷工厂：仅需 modelId，其余字段取默认值（测试 mock 最小化）
    public static ModelRoute of(String modelId) {
        return new ModelRoute(null, null, null, modelId, null, 1, 30000L, null);
    }

    public String getEndpointId() { return endpointId; }
    public ClientType getClientType() { return clientType; }
    public AuthType getAuthType() { return authType; }
    public String getModelId() { return modelId; }
    public String getEndpointUrl() { return endpointUrl; }
    public int getWeight() { return weight; }
    public long getTimeoutMs() { return timeoutMs; }
    public Map<String, Object> getParameters() { return parameters; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelRoute that)) return false;
        return weight == that.weight && timeoutMs == that.timeoutMs
            && Objects.equals(endpointId, that.endpointId)
            && clientType == that.clientType
            && authType == that.authType
            && Objects.equals(modelId, that.modelId)
            && Objects.equals(endpointUrl, that.endpointUrl)
            && Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpointId, clientType, authType, modelId, endpointUrl, weight, timeoutMs, parameters);
    }

    @Override
    public String toString() {
        return "ModelRoute{modelId='" + modelId + "', endpointId='" + endpointId + "'}";
    }
}
```

**公开接口**：全参构造器、`of(String modelId)` 静态工厂、全部 getter、`equals/hashCode/toString`
**构造方式**：`new ModelRoute(...)` 或 `ModelRoute.of("model-1")`
**类型关系**：无继承，字段引用 `ClientType`、`AuthType` 枚举（均同模块已有）

**防御性拷贝说明**：构造器中 `parameters` 参数通过 `Collections.unmodifiableMap(new HashMap<>(parameters))` 执行双重防御——先拷贝原始 Map 切断外部引用，再包装为不可变视图，确保 `getParameters()` 返回的 Map 绝对不可变。

---

### 3. DefaultModelRouter（新建）

**形态**：class（@Service 实现）
**包路径**：`com.aimedical.modules.ai.impl.router`
**职责**：基于能力标识的模型路由选择，支持定时刷新与权重随机

```java
package com.aimedical.modules.ai.impl.router;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class DefaultModelRouter implements ModelRouter {

    private static final Logger log = LoggerFactory.getLogger(DefaultModelRouter.class);

    private final AiRouterProperties routerProperties;
    private final AtomicReference<Map<String, ModelRoute[]>> routeTableRef;

    public DefaultModelRouter(AiRouterProperties routerProperties) {
        this.routerProperties = routerProperties;
        this.routeTableRef = new AtomicReference<>(Collections.emptyMap());
    }

    @PostConstruct
    public void init() {
        refreshRouteTable();
    }

    @Scheduled(fixedDelay = 60000)
    public void refreshRouteTable() {
        Map<String, List<ModelRoute>> rawRoutes = routerProperties.getRoutes();
        Map<String, ModelRoute[]> newTable = buildRouteTable(rawRoutes);
        routeTableRef.set(newTable);
        log.debug("路由表已刷新，包含 {} 项能力路由", newTable.size());
    }

    // @TODO Phase5: 后续任务引入 RouteConfigChangedEvent 事件驱动刷新
    // @EventListener(RouteConfigChangedEvent.class)
    // public void onRouteConfigChanged(RouteConfigChangedEvent event) {
    //     refreshRouteTable();
    // }

    @Override
    public ModelRoute route(String capabilityId, Object request) {
        ModelRoute[] routes = routeTableRef.get().get(capabilityId);
        if (routes == null || routes.length == 0) {
            return null;
        }
        if (routes.length == 1) {
            return routes[0];
        }
        return selectWeighted(routes);
    }

    static ModelRoute selectWeighted(ModelRoute[] routes) {
        int totalWeight = 0;
        for (ModelRoute r : routes) {
            totalWeight += r.getWeight();
        }
        if (totalWeight <= 0) {
            return routes[ThreadLocalRandom.current().nextInt(routes.length)];
        }
        int target = ThreadLocalRandom.current().nextInt(totalWeight);
        int cumulative = 0;
        for (ModelRoute r : routes) {
            cumulative += r.getWeight();
            if (target < cumulative) {
                return r;
            }
        }
        return routes[routes.length - 1];
    }

    private Map<String, ModelRoute[]> buildRouteTable(Map<String, List<ModelRoute>> rawRoutes) {
        if (rawRoutes == null || rawRoutes.isEmpty()) {
            return Collections.emptyMap();
        }
        java.util.HashMap<String, ModelRoute[]> table = new java.util.HashMap<>();
        for (Map.Entry<String, List<ModelRoute>> entry : rawRoutes.entrySet()) {
            List<ModelRoute> list = entry.getValue();
            if (list == null || list.isEmpty()) {
                continue;
            }
            table.put(entry.getKey(), list.toArray(new ModelRoute[0]));
        }
        return Collections.unmodifiableMap(table);
    }
}
```

**公开接口**：
- `route(String capabilityId, Object request): ModelRoute` — 路由方法
- `init(): void` — @PostConstruct 初始化加载
- `refreshRouteTable(): void` — @Scheduled 定时刷新
- `selectWeighted(ModelRoute[] routes): ModelRoute` — 静态包级可见，权重随机选择

**构造方式**：通过 Spring @Service + 构造器注入（`AiRouterProperties`）
**类型关系**：实现 `ModelRouter` 接口

---

### 4. AiRouterProperties（新建）

**形态**：class（@ConfigurationProperties 存根）
**包路径**：`com.aimedical.modules.ai.impl.router`
**职责**：绑定 `ai.router` 前缀的 YAML 配置，持有路由配置映射

```java
package com.aimedical.modules.ai.impl.router;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.router")
public class AiRouterProperties {

    // @TODO Phase5: 当前 Map<String, List<ModelRoute>> 的绑定存在限制：
    // ModelRoute 为不可变值对象（无无参构造器 + setter），Spring Boot @ConfigurationProperties
    // 的 setter 绑定机制无法自动将 YAML 反序列化为 ModelRoute 实例。
    // 此存根类将在 Task 18（AiPlatformConfig）中被吸收或委托为更合适的配置加载方式。
    // 当前实现中，路由配置通过编程方式（如 @Bean 工厂方法）提供以保证编译通过。
    private Map<String, List<ModelRoute>> routes;

    public Map<String, List<ModelRoute>> getRoutes() {
        return routes;
    }

    public void setRoutes(Map<String, List<ModelRoute>> routes) {
        this.routes = routes;
    }
}
```

**公开接口**：`getRoutes(): Map<String, List<ModelRoute>>`, `setRoutes(...): void`
**构造方式**：Spring Boot 自动通过 @EnableConfigurationProperties 绑定
**类型关系**：无继承

---

### 5. AbstractCapabilityExecutor 修改

**形态**：abstract class（已有，修改 `executeStandardPipeline` 方法）
**包路径**：`com.aimedical.modules.ai.impl.orchestrator`

**修改点 1（L352）：路由结果类型切换**
```java
// 修改前：
Object routeResult = modelRouter.route(capabilityId, request);

// 修改后：
ModelRoute routeResult = modelRouter.route(capabilityId, request);
```

**修改点 2（L359 + L367 + L375）：端点健康检查 + 模型 ID + LlmChatRequest 构造**
```java
// 修改前：
String healthState = endpointHealthManager.getState(String.valueOf(routeResult));
// ... (L367)
options.setModelId(String.valueOf(routeResult));
// ... (L375)
LlmChatRequest llmChatRequest = new LlmChatRequest(messages, options, ClientType.HTTP_API, null, null);

// 修改后：
String healthState = endpointHealthManager.getState(routeResult.getEndpointId());
// ... (L367)
options.setModelId(routeResult.getModelId());
// ... (L375)
ClientType clientType = routeResult.getClientType() != null ? routeResult.getClientType() : ClientType.HTTP_API;
LlmChatRequest llmChatRequest = new LlmChatRequest(messages, options, clientType, null, null);
```

**修改点 3（L383，超时降级路径中的 routeResult 引用）：**
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

**import 变更**：新增 `import com.aimedical.modules.ai.impl.router.ModelRoute;`（原有 `import ...ModelRouter;` 保留）

---

### 6. 测试文件修改模式

**修改模式（19 处，覆盖 3 个文件）：**

**AbstractCapabilityExecutorTest.java** — 15 处将：
```java
ModelRouter mockRouter = (capId, req) -> "model-1";
```
改为：
```java
ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");
```
另 1 处返回 null 的不修改（line 430: `(capId, req) -> null`）。

**需同步增加 import**：
```java
import com.aimedical.modules.ai.impl.router.ModelRoute;
```

**DiscussionConclusionCapabilityExecutorTest.java** — 3 处（lines 260, 295, 380），同一模式。

**TriageCapabilityExecutorTest.java** — 1 处（line 55），同一模式。

---

### 7. ModelRouteTest（新建）

**包路径**：`com.aimedical.modules.ai.impl.router`
**测试维度**：

| # | 测试方法 | 验证要点 |
|---|---------|---------|
| 1 | `shouldCreateWithAllFields` | 全参构造器正确赋值，getter 返回正确值 |
| 2 | `shouldCreateViaOfFactoryWithMinimalFields` | `of("model-1")` 工厂方法返回 modelId="model-1"，其余默认值 |
| 3 | `shouldDefensivelyCopyParametersInConstructor` | 构造后修改原始 Map 不影响内部状态 |
| 4 | `shouldReturnUnmodifiableParameters` | `getParameters()` 返回的 Map 不可修改 |
| 5 | `shouldImplementEqualsAndHashCode` | 相同字段的实例 equals=true，hashCode 相同；不同字段不等 |
| 6 | `shouldHandleNullParameters` | 传入 null parameters 时返回空 Map 而非 NPE |

---

### 8. DefaultModelRouterTest（新建）

**包路径**：`com.aimedical.modules.ai.impl.router`
**测试维度**：

| # | 测试方法 | 验证要点 |
|---|---------|---------|
| 1 | `shouldReturnNullForUnknownCapability` | 未配置能力返回 null |
| 2 | `shouldReturnRouteForKnownCapability` | 单路由直接返回，不触发权重选择 |
| 3 | `shouldSelectByWeight` | 多路由时权重随机选择（概率统计验证：重复选择 1000 次，各 route 被选次数接近 weight 比例） |
| 4 | `shouldSelectRandomlyWhenAllWeightsZero` | 所有权重为 0 时等概率选择 |
| 5 | `shouldBeThreadSafe` | 多线程并发调用 `route()` 不抛异常 |
| 6 | `shouldRefreshOnPostConstruct` | @PostConstruct 后路由表正确初始化 |
| 7 | `shouldNotCrashOnEmptyProperties` | 配置为空时所有 route 返回 null |

---

### 9. 测试文件中 ModelRouter mock 修改详细清单

**AbstractCapabilityExecutorTest.java** — 15 处需修改的行号：
- L451, L512, L538, L587, L641, L693, L721, L765, L815, L861, L904, L963, L1023, L1076, L1132

L430 保留 `(capId, req) -> null` 不变。

**DiscussionConclusionCapabilityExecutorTest.java** — 3 处需修改的行号：
- L260, L295, L380

**TriageCapabilityExecutorTest.java** — 1 处需修改的行号：
- L55

共 19 处修改，统一模式：`(capId, req) -> "model-1"` → `(capId, req) -> ModelRoute.of("model-1")`

---

### import 变更汇总

| 文件 | 新增 import | 说明 |
|------|------------|------|
| `AbstractCapabilityExecutor.java` | `import com.aimedical.modules.ai.impl.router.ModelRoute;` | 使用 ModelRoute 类型 |
| `AbstractCapabilityExecutorTest.java` | `import com.aimedical.modules.ai.impl.router.ModelRoute;` | mock 工厂方法 |
| `DiscussionConclusionCapabilityExecutorTest.java` | `import com.aimedical.modules.ai.impl.router.ModelRoute;` | mock 工厂方法 |
| `TriageCapabilityExecutorTest.java` | `import com.aimedical.modules.ai.impl.router.ModelRoute;` | mock 工厂方法 |

## 错误处理

- `ModelRouter.route()` 返回 null → 触发 `NO_AVAILABLE_ROUTE` 降级（现有逻辑不变）
- `DefaultModelRouter` 中路由表为空或能力无匹配路由时返回 null
- `ModelRoute.of()` 不抛异常（parameters 为 null 时内部处理为空 Map）
- `selectWeighted` 在 `routes` 为 null/空时不调用（调用前已做 null 检查）

## 行为契约

### ModelRouter 契约
- `route()` 入参 `request` 仅用于路由决策上下文，不被修改
- 返回 null 表示无可用路由，调用方需做降级处理
- 线程安全：DefaultModelRouter 内部通过 `AtomicReference` 保证读路径无锁

### DefaultModelRouter 生命周期
1. 容器启动 → 构造器注入 AiRouterProperties → routeTableRef 初始化为空 Map
2. @PostConstruct init() → 从 properties 首次构建路由表 → CAS 更新
3. @Scheduled 每 60s → refreshRouteTable() → 重建不可变数组后 CAS 替换
4. 读路径 `route()` → routeTableRef.get() 获取最新原子快照 → 不可变数组上查找 → 权重选择

### 权重选择算法
`selectWeighted(ModelRoute[] routes)`:
- 计算总权重 `totalWeight = sum(weight)`
- 若 `totalWeight <= 0`，等概率随机选（`ThreadLocalRandom.current().nextInt(n)`）
- 否则生成 `[0, totalWeight)` 随机数，按累积权重命中选择

### @ConfigurationProperties 绑定限制
`AiRouterProperties.routes` 类型为 `Map<String, List<ModelRoute>>`，但 `ModelRoute` 为不可变值对象（无无参构造器 + setter），Spring Boot 的 setter 绑定机制无法自动将 YAML 反序列化为 `ModelRoute` 实例。此限制标注为 `@TODO Phase5:`，后续由 Task 18（AiPlatformConfig）吸收或替换。

### RouteConfigChangedEvent 事件
`@EventListener(RouteConfigChangedEvent.class)` 在当前任务中处于注释状态（`// @TODO Phase5:`），该事件类型将在后续任务中定义并引入。

## 依赖关系

| 依赖类型 | 所在包 | 用途 |
|---------|--------|------|
| `ClientType` | `com.aimedical.modules.ai.impl.client` | ModelRoute 字段类型 |
| `AuthType` | `com.aimedical.modules.ai.impl.client` | ModelRoute 字段类型 |
| `ModelRouter` | `com.aimedical.modules.ai.impl.router` | 被 DefaultModelRouter 实现 |
| `AiRouterProperties` | `com.aimedical.modules.ai.impl.router` | DefaultModelRouter 构造器注入 |
| `ModelRoute` | `com.aimedical.modules.ai.impl.router` | route() 返回类型，被 AbstractCapabilityExecutor 使用 |

**零外部依赖**：ModelRoute/DefaultModelRouter/AiRouterProperties 仅依赖模块内已有枚举类型（ClientType/AuthType），不引入新外部 JAR。

## 修订说明（v16 r1）

| 审查意见 | 修改措施 |
|---------|---------|
| [严重] DefaultModelRouter import 引用 `config.AiRouterProperties`（L121），但 AiRouterProperties 设计在 `router` 包下 | DefaultModelRouter 中 AiRouterProperties 与自身同包（`com.aimedical.modules.ai.impl.router`），无需 import 语句；修正设计文档中 AiRouterProperties 包路径为 `com.aimedical.modules.ai.impl.router` |
| [一般] @ConfigurationProperties 与 ModelRoute 不可变特性不兼容 | 在 AiRouterProperties.java 中添加 `@TODO Phase5:` 注释说明绑定限制，注明后续由 AiPlatformConfig（Task 18）替换；设计文档中明确标注该限制 |
| [轻微] ModelRoute 构造器未说明 parameters 防御性拷贝 | 在构造器设计中明确 `this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters))`，并在设计文档"防御性拷贝说明"中阐述双重防御策略 |
| [轻微] @EventListener(RouteConfigChangedEvent.class) 引用未定义类型 | 将方法体注释化并标注 `// @TODO Phase5:`，注明事件类型在后续任务引入 |
