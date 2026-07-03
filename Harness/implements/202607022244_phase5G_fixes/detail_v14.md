# 详细设计（v14）

## 概述

R14 路由与实验管理（含R13 RETRY修复+遗留28错误修复）。四个子任务：A—修复 DatabasePromptTemplateManagerTest 6个 Mockito 原始类型匹配错误；B—全量重编译修复 28 个 NoSuchMethod 遗留错误（无源码变更）；C—路由模块 T41/T42/T48 三项修复；D—实验管理 T56/T57/T63 三项修复。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `template/DatabasePromptTemplateManagerTest.java` | 修改 | A：2行 `any()`→`anyString()/anyInt()` |
| _(无源码变更)_ | 全量重编译 | B：`mvn clean compile test-compile -pl ai-api,ai-impl -am` |
| `router/ModelRouter.java` | 修改 | C-T41：`Object request`→`ExperimentAssignment assignment` |
| `router/DefaultModelRouter.java` | 修改 | C-T41：同上；C-T42：`@Scheduled` 加 `scheduler` |
| `router/AiRouterProperties.java` | 修改 | C-T48：添加 Logger + catch 块 `log.warn` |
| `orchestrator/AbstractCapabilityExecutor.java` | 修改 | C-T41：调用方适配，`null`→`ExperimentAssignment.createDefault()` |
| `orchestrator/AbstractCapabilityExecutorTest.java` | 修改 | C-T41：3处 lambda 参数名 `req`→`assignment` |
| `experiment/HashBucketExperimentManager.java` | 修改 | D-T56：`min().reversed()`→`max()`；D-T57：loader 数据排序 |
| `experiment/Experiment.java` | 修改 | D-T63：`FetchType.EAGER`→`FetchType.LAZY` |
| `experiment/ExperimentRepository.java` | 修改 | D-T63：新增 `@Query JOIN FETCH` 方法 |

## 类型定义

### 子任务A — DatabasePromptTemplateManagerTest Mockito 参数匹配修正

**文件**：`DatabasePromptTemplateManagerTest.java`

**变更**：

| 行号 | 变更前 | 变更后 |
|------|--------|--------|
| 217 | `verify(repository, never()).findByCapabilityIdAndDepartmentIdAndVersion(any(), any(), any())` | `verify(repository, never()).findByCapabilityIdAndDepartmentIdAndVersion(anyString(), any(), anyInt())` |
| 239 | `verify(repository, times(1)).findByCapabilityIdAndDepartmentIdAndVersion(any(), any(), any())` | `verify(repository, times(1)).findByCapabilityIdAndDepartmentIdAndVersion(anyString(), any(), anyInt())` |

**说明**：`findByCapabilityIdAndDepartmentIdAndVersion(String, String, int)` 第三参数为原始类型 `int`，Mockito `any()` 无法匹配原始类型参数（返回默认值 null，导致 `NullPointerException` 或参数不匹配），需使用 `anyInt()`。

**import 影响**：文件已存在 `import static org.mockito.ArgumentMatchers.*;`，`anyString()` 和 `anyInt()` 同属此包，无需新增 import。

### 子任务B — 全量重编译修复 28 个 NoSuchMethod 错误

**说明**：无源码变更。错误由 `DegradationContext.Builder.invocationCount(int)` 签名字节码与旧编译产物中引用的 `invocationCount(Integer)` 签名不匹配导致。执行 `mvn clean compile test-compile -pl ai-api,ai-impl -am` 强制清除旧字节码即可修复。

**验证范围**：
- CircuitBreakerDegradationStrategyTest × 16
- TimeoutDegradationStrategyTest × 6
- SlidingWindowMetricsStoreTest × 5
- AbstractCapabilityExecutorTest × 1

### 子任务C — 路由模块（T41, T42, T48）

#### T41: ModelRouter.route() 签名修正

**ModelRouter.java**（接口）：
```java
// 变更前
ModelRoute route(String capabilityId, Object request);

// 变更后
ModelRoute route(String capabilityId, ExperimentAssignment assignment);
```

**新增 import**：
```java
import com.aimedical.modules.ai.impl.experiment.ExperimentAssignment;
```

**DefaultModelRouter.java**（实现）：
```java
// 变更前
public ModelRoute route(String capabilityId, Object request) {

// 变更后
public ModelRoute route(String capabilityId, ExperimentAssignment assignment) {
```

**AbstractCapabilityExecutorTest.java**（3处 lambda 参数名同步）：
| 行号 | 变更前 | 变更后 |
|------|--------|--------|
| 437 | `ModelRouter mockRouter = (capId, req) -> null;` | `ModelRouter mockRouter = (capId, assignment) -> null;` |
| 460 | `ModelRouter mockRouter = (capId, req) -> ModelRoute.of("model-1");` | `ModelRouter mockRouter = (capId, assignment) -> ModelRoute.of("model-1");` |
| 524 | `ModelRouter mockRouter = (capId, req) -> ModelRoute.of("endpoint1");` | `ModelRouter mockRouter = (capId, assignment) -> ModelRoute.of("endpoint1");` |

**行为契约**：方法体不变——`assignment` 参数在当前实现中未被使用（路由仅凭 `capabilityId` 查表），签名变更仅为接口类型对齐。

#### T42: @Scheduled 指定 scheduler

**DefaultModelRouter.java** line 34：
```java
// 变更前
@Scheduled(fixedDelay = 60000)

// 变更后
@Scheduled(fixedDelay = 60000, scheduler = "scheduledTaskExecutor")
```

**说明**：项目中 `ThreadPoolTaskScheduler` 以 bean 名 `scheduledTaskExecutor` 注册于 `AiPlatformConfig.java:189`。其他 `@Scheduled` 方法（`refreshDegradationStrategies`、`refreshCapabilityTimeoutConfig`、`refreshWindowSeconds`）已使用该 bean 名，保持一致。

#### T48: 枚举转换失败告警日志

**AiRouterProperties.java**：

**新增 Logger 字段**：
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger log = LoggerFactory.getLogger(AiRouterProperties.class);
```

**ClientType catch 块**（lines 47-49）：
```java
// 变更前
} catch (IllegalArgumentException e) {
    clientType = ClientType.HTTP_API;
}

// 变更后
} catch (IllegalArgumentException e) {
    log.warn("Invalid clientType value: '{}', falling back to {}", config.getClientType(), ClientType.HTTP_API);
    clientType = ClientType.HTTP_API;
}
```

**AuthType catch 块**（lines 55-57）：
```java
// 变更前
} catch (IllegalArgumentException e) {
    authType = AuthType.NONE;
}

// 变更后
} catch (IllegalArgumentException e) {
    log.warn("Invalid authType value: '{}', falling back to {}", config.getAuthType(), AuthType.NONE);
    authType = AuthType.NONE;
}
```

### 子任务D — 实验管理（T56, T57, T63）

#### T56: min().reversed() → max()

**HashBucketExperimentManager.java** line 86：
```java
// 变更前
.min(Comparator.comparing(Experiment::getStartTime).reversed())

// 变更后
.max(Comparator.comparing(Experiment::getStartTime))
```

**说明**：`min().reversed()` 语义等价于 `max()`，但反直觉。改为 `max()` 后逻辑直接表达「取最晚开始时间的实验」。

#### T57: loader 数据排序统一

**HashBucketExperimentManager.java** lines 76-77：
```java
// 变更前
List<Experiment> experiments = cache.get(capabilityId, k ->
    repository.findByCapabilityIdAndStatus(k, ACTIVE));

// 变更后
List<Experiment> experiments = cache.get(capabilityId, k ->
    repository.findByCapabilityIdAndStatus(k, ACTIVE).stream()
        .sorted(Comparator.comparing(Experiment::getStartTime).reversed())
        .collect(Collectors.toList()));
```

**说明**：warmup() 已对数据按 `getStartTime().reversed()` 排序（line 50），但 loader 未排序，导致缓存中数据格式不一致。现使 loader 排序逻辑与 warmup 一致。

**import 影响**：文件中已有：
- `import java.util.Comparator;` ✓
- `import java.util.stream.Collectors;` ✓
- `import java.util.List;` ✓

无需新增 import。

#### T63: Experiment N+1 查询修复

**Experiment.java** line 31-32：
```java
// 变更前
@OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL,
           orphanRemoval = true, fetch = FetchType.EAGER)

// 变更后
@OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL,
           orphanRemoval = true, fetch = FetchType.LAZY)
```

**ExperimentRepository.java** — 新增 `@Query` 方法：
```java
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Query("SELECT e FROM Experiment e LEFT JOIN FETCH e.groups WHERE e.capabilityId = :capabilityId AND e.status = :status")
List<Experiment> findByCapabilityIdAndStatusWithGroups(@Param("capabilityId") String capabilityId,
                                                       @Param("status") ExperimentStatus status);
```

**HashBucketExperimentManager.java** — 使用新查询方法（line 77）：
```java
// 变更前
repository.findByCapabilityIdAndStatus(k, ACTIVE)

// 变更后
repository.findByCapabilityIdAndStatusWithGroups(k, ACTIVE)
```

**说明**：原 `FetchType.EAGER` 在查询所有 Experiment 时产生 N+1 查询（每条 Experiment 另发查询加载 groups）。改为 `LAZY` 后，需在查询方法中显式 `JOIN FETCH` 以确保 `assign()` 中 `effective.getGroups()` 不触发额外查询。

## 错误处理

| 场景 | 处理方式 |
|------|---------|
| AiRouterProperties 枚举转换失败 | `log.warn` 记录无效值 + 默认值降级，不抛异常（保持向后兼容） |
| HashBucketExperimentManager.assign() 中 cache loader 异常 | 外层 try-catch 返回 `ExperimentAssignment.createDefault()` 降级（已有） |

## 行为契约

| 组件 | 契约 |
|------|------|
| ModelRouter.route() | 第二个参数类型变更为 `ExperimentAssignment`；方法内部不使用该参数 |
| DefaultModelRouter.refreshRouteTable() | 绑定 `scheduledTaskExecutor` 线程池 |
| AiRouterProperties.convert() | 枚举转换失败时打印告警日志 |
| HashBucketExperimentManager.assign() | loader 排序格式与 warmup 一致（按 startTime 降序） |
| Experiment.groups | Lazy 加载，通过 `JOIN FETCH` 查询保证同一事务内可访问 |
| ExperimentRepository.findByCapabilityIdAndStatusWithGroups() | 返回的 Experiment 关联的 groups 已初始化 |

## 依赖关系

| 类型 | 依赖变更 |
|------|---------|
| ModelRouter.java | 新增 `import com.aimedical.modules.ai.impl.experiment.ExperimentAssignment` |
| AbstractCapabilityExecutor.java | 新增 `import com.aimedical.modules.ai.impl.experiment.ExperimentAssignment;` |
| AiRouterProperties.java | 新增 `import org.slf4j.Logger;` + `import org.slf4j.LoggerFactory;` |
| ExperimentRepository.java | 新增 `import org.springframework.data.jpa.repository.Query;` + `import org.springframework.data.repository.query.Param;` |
| 其他文件 | 无新增/移除 import |

## 验证方法

1. `mvn clean compile test-compile -pl ai-api,ai-impl -am` — 全量编译通过
2. `mvn test -pl ai-impl -Dtest="DatabasePromptTemplateManagerTest"` — 6 个错误归零
3. `mvn test -pl ai-impl -Dtest="CircuitBreakerDegradationStrategyTest,TimeoutDegradationStrategyTest,SlidingWindowMetricsStoreTest,AbstractCapabilityExecutorTest"` — 28 个错误归零
4. `mvn test -pl ai-impl` — 所有测试通过（529+ → 预期 ~563 pass）
