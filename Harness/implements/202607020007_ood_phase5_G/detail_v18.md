# 详细设计（v18）

## 概述

实现 A/B 实验管理层：包括 ExperimentManager 接口、ExperimentAssignment 值对象、ExperimentStatus 枚举、Experiment/ExperimentGroup JPA 实体、ExperimentRepository、ExperimentChangedEvent 事件、HashBucketExperimentManager 实现类，及对应测试。所有文件新建于 `experiment/` 子包。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `experiment/ExperimentManager.java` | 新建 | 实验管理接口，定义 assign() 方法 |
| `experiment/ExperimentAssignment.java` | 新建 | 实验分组结果不可变值对象 |
| `experiment/ExperimentStatus.java` | 新建 | 枚举 DRAFT / ACTIVE / PAUSED / COMPLETED |
| `experiment/Experiment.java` | 新建 | JPA @Entity，实验配置主表 |
| `experiment/ExperimentGroup.java` | 新建 | JPA @Entity，实验分组子表 |
| `experiment/ExperimentRepository.java` | 新建 | Spring Data JPA Repository |
| `experiment/ExperimentChangedEvent.java` | 新建 | ApplicationEvent 子类 |
| `experiment/HashBucketExperimentManager.java` | 新建 | @Service 实现：Caffeine 缓存 + 哈希分桶 |
| `experiment/ExperimentAssignmentTest.java` | 新建 | 值对象契约测试（6 条） |
| `experiment/HashBucketExperimentManagerTest.java` | 新建 | 分桶/缓存/预热/事件/并发/边界测试（13 条） |

全部文件包路径：`com.aimedical.modules.ai.impl.experiment`。

## 类型定义

### ExperimentManager（接口）
**形态**：interface
**包路径**：`com.aimedical.modules.ai.impl.experiment`
**职责**：A/B 实验管理契约，判断当前请求是否命中某个实验

```java
public interface ExperimentManager {
    ExperimentAssignment assign(String capabilityId, String userId, String sessionId);
}
```

**说明**：capabilityId 为能力标识；userId/sessionId 用于哈希分桶，两者均可为 null（输入为空字符串）。

---

### ExperimentAssignment（不可变值对象）
**形态**：class（final，无 setter，无 equals/hashCode 覆写）
**包路径**：`com.aimedical.modules.ai.impl.experiment`
**职责**：实验分组结果，下游管线据此决定模型或 Prompt 版本

```java
public final class ExperimentAssignment {
    private final String experimentId;
    private final String groupId;
    private final String targetModelId;
    private final Integer targetPromptVersion;

    public ExperimentAssignment(String experimentId, String groupId,
                                 String targetModelId, Integer targetPromptVersion);

    public static ExperimentAssignment createDefault();
    // → groupId="default", 其余字段 null

    public static ExperimentAssignment createErrorFallback();
    // → groupId="experiment-error", 其余字段 null

    // getter 方法
    public String getExperimentId();
    public String getGroupId();
    public String getTargetModelId();
    public Integer getTargetPromptVersion();
}
```

**公开接口**：全参构造器 + 2 个静态工厂方法 + 4 个 getter
**构造方式**：`new ExperimentAssignment(...)` 或工厂方法
**类型关系**：无继承/实现
**不可变性保证**：class 为 final，所有字段为 final，不暴露 setter，不覆写 equals/hashCode（引用比较）

---

### ExperimentStatus（枚举）
**形态**：enum
**包路径**：`com.aimedical.modules.ai.impl.experiment`
**职责**：实验生命周期状态

```java
public enum ExperimentStatus {
    DRAFT,
    ACTIVE,
    PAUSED,
    COMPLETED
}
```

状态转换：`DRAFT → ACTIVE → PAUSED → COMPLETED`

---

### Experiment（JPA Entity）
**形态**：@Entity
**包路径**：`com.aimedical.modules.ai.impl.experiment`
**职责**：实验配置持久化记录

```java
@Entity
@Table(name = "ai_experiment")
public class Experiment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String capabilityId;

    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ExperimentGroup> groups;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExperimentStatus status;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;     // nullable

    // 全参/无参构造器、getter/setter
    // equals/hashCode based on id
}
```

**公开接口**：构造器 + getter/setter + equals/hashCode
**构造方式**：new 后 setter 赋值
**类型关系**：无继承/实现；composition → List<ExperimentGroup>

---

### ExperimentGroup（JPA Entity）
**形态**：@Entity
**包路径**：`com.aimedical.modules.ai.impl.experiment`
**职责**：实验分组子实体，定义流量分配比例和目标配置

```java
@Entity
@Table(name = "ai_experiment_group")
public class ExperimentGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private Experiment experiment;

    @Column(nullable = false, length = 50)
    private String groupId;

    @Column(nullable = false)
    private int percentage;             // 千分比 [0, 1000]

    @Column(length = 50)
    private String targetModelId;       // nullable

    @Column
    private Integer targetPromptVersion; // nullable

    // 全参/无参构造器、getter/setter
    // equals/hashCode based on id
}
```

**公开接口**：构造器 + getter/setter + equals/hashCode
**构造方式**：new 后 setter 赋值
**类型关系**：ManyToOne → Experiment

---

### ExperimentRepository
**形态**：interface（Spring Data JPA Repository）
**包路径**：`com.aimedical.modules.ai.impl.experiment`
**职责**：Experiment 数据库访问

```java
public interface ExperimentRepository extends JpaRepository<Experiment, Long> {
    List<Experiment> findByStatus(ExperimentStatus status);
    List<Experiment> findByCapabilityIdAndStatus(String capabilityId, ExperimentStatus status);
    List<Experiment> findByStatusAndEndTimeBetween(ExperimentStatus status,
                                                    LocalDateTime start, LocalDateTime end);
    List<Experiment> findByCapabilityIdOrderByEndTimeDesc(String capabilityId);
}
```

---

### ExperimentChangedEvent
**形态**：class（extends ApplicationEvent）
**包路径**：`com.aimedical.modules.ai.impl.experiment`
**职责**：实验变更事件，驱动缓存失效

```java
public class ExperimentChangedEvent extends ApplicationEvent {

    public enum ChangeType {
        CREATED, UPDATED, DELETED, STATUS_CHANGED
    }

    private final String capabilityId;      // null = 影响全部能力
    private final Long experimentId;        // null = 影响该能力下全部实验
    private final ChangeType changeType;
    private final Instant changedAt;

    public ExperimentChangedEvent(Object source, String capabilityId, Long experimentId,
                                   ChangeType changeType, Instant changedAt);
    // + getter 方法
}
```

**构造方式**：new ExperimentChangedEvent(source, capabilityId, experimentId, changeType, changedAt)
**类型关系**：继承 ApplicationEvent；ChangeType 为内嵌枚举

---

### HashBucketExperimentManager
**形态**：class（implements ExperimentManager）
**包路径**：`com.aimedical.modules.ai.impl.experiment`
**职责**：基于 JPA + Caffeine 缓存的哈希分桶实验管理器

```java
@Service
public class HashBucketExperimentManager implements ExperimentManager {

    private final ExperimentRepository repository;
    private final Cache<String, List<Experiment>> cache;
    private static final int BUCKET_COUNT = 1000;

    public HashBucketExperimentManager(ExperimentRepository repository);

    @PostConstruct
    public void warmup();

    @EventListener
    public void onExperimentChanged(ExperimentChangedEvent event);

    @Override
    public ExperimentAssignment assign(String capabilityId, String userId, String sessionId);
}
```

**公开接口**：assign() 实现实验分流
**构造方式**：Spring 自动装配（@Service + 构造器注入）
**类型关系**：实现 ExperimentManager

**assign() 内部流程**：
1. 计算哈希值：
   a. `String hashInput = sessionId != null ? sessionId : (userId != null ? userId : "");`
   b. `int hash = Math.floorMod(hashInput.hashCode(), BUCKET_COUNT);` → [0, BUCKET_COUNT) 区间
2. 从缓存按 capabilityId 查询实验列表：
   a. 缓存命中 → 取 List<Experiment>
   b. 缓存未命中 → `repository.findByCapabilityIdAndStatus(capabilityId, ACTIVE)` → 填充缓存
3. 实验列表为空 → 返回 `createDefault()`
4. 若列表包含多个实验（异常情况），按 startTime 降序排序取第一条作为有效实验
5. 遍历有效实验的 groups，累加 percentage：
   a. `cumulativePercentage += group.getPercentage()`
   b. 首个 `cumulativePercentage > hash` 的分组 → 命中
6. 命中分组 → 返回 `new ExperimentAssignment(experimentId, groupId, targetModelId, targetPromptVersion)`
7. 未命中任何分组（hash 超出累计值）→ 返回 `createDefault()`
8. 所有 DB/异常路径 catch → WARN 日志 → 返回 `createDefault()`

**缓存设计**：
- `Cache<String, List<Experiment>>`，key = capabilityId，value = 该能力的 ACTIVE 实验列表（含 groups，EAGER 加载）
- Caffeine 配置：`expireAfterWrite = 5 分钟`，`maximumSize = 100`
- 缓存仅存储 status=ACTIVE 的实验

**warmup() 流程**：
1. `repository.findByStatus(ACTIVE)` 查询全部 ACTIVE 实验（含 ExperimentGroup，EAGER 加载策略保证 groups 一并加载）
2. 遍历实验结果，按 capabilityId 分组（`groupingBy(Experiment::getCapabilityId)`）
3. 逐组写入缓存：`cache.put(capabilityId, experiments)`

**onExperimentChanged() 流程**：
1. 从 event 获取 capabilityId
2. capabilityId != null → `cache.invalidate(capabilityId)`
3. capabilityId == null → `cache.invalidateAll()`

---

## 错误处理

- assign() 内部所有异常（DB 访问、null 指针等）：catch 后 WARN 日志，返回 `createDefault()`
- assign() 中 userId/sessionId 为 null：使用 `""` 作为哈希输入，不抛异常
- 无自定义异常类型

## 行为契约

- assign() 前置条件：capabilityId 非 null
- assign() 后置条件：始终返回非 null ExperimentAssignment
- assign() 中 hash 计算使用 `Math.floorMod`，结果恒为 [0, 1000) 非负整数
- 同一 capability 同时最多一个 ACTIVE 实验（管理端保证）；多个时按 startTime 降序取第一条
- groupId 不可变语义由管理端保证，实现侧不做校验
- percentage 值域 [0, 1000]，管理端保证同一实验内各分组之和 ≤ 1000；超出部分导致的 hash 无匹配返回 default
- warmup() 在 @PostConstruct 阶段自动执行一次，异常时 WARN 日志 + 跳过（不阻塞容器启动）
- onExperimentChanged() 在 ApplicationEvent 发布后同步执行

## 依赖关系

### 依赖已有基础设施
- **JPA**：spring-boot-starter-data-jpa 已在 pom.xml（Task 12 引入），可直接使用 @Entity、@Repository
- **Caffeine**：已有依赖 `com.github.ben-manes.caffeine:caffeine`，与 DatabasePromptTemplateManager 使用相同版本
- **ApplicationEvent**：Spring 容器原生支持

### 无需修改的文件
- **pom.xml**：不新增依赖（spring-boot-starter-data-jpa + h2 + caffeine 均已存在）
- **AiImplPomCleanDependencyTest**：断言总数仍为 11，无需修改

### 被依赖关系（当前任务不涉及修改）
- `AbstractCapabilityExecutor.doExecuteInternal()` 管线步骤 2（实验分流）将引用 `ExperimentManager.assign()`——该修改不在本任务范围内，属后续任务

## 测试规划

### ExperimentAssignmentTest（6 条）

| 测试方法 | 契约维度 | 覆盖内容 |
|---------|---------|---------|
| shouldConstructWithAllFields | 正常路径 | 全参构造器 + getter 返回正确值 |
| shouldCreateDefaultAssignment | 正常路径 | createDefault() 返回 groupId="default"，其余字段 null |
| shouldCreateErrorFallbackAssignment | 正常路径 | createErrorFallback() 返回 groupId="experiment-error"，其余字段 null |
| shouldBeImmutable | 边界条件 | class 为 final，无 setter，字段不可修改 |
| shouldSupportNullTargetFields | 边界条件 | targetModelId/targetPromptVersion 可为 null |
| equalsAndHashCodeShouldNotBeImplemented | 边界条件 | 不覆写 equals/hashCode（引用比较） |

### HashBucketExperimentManagerTest（13 条，@ExtendWith(MockitoExtension.class)）

| 测试方法 | 契约维度 | 覆盖内容 |
|---------|---------|---------|
| shouldReturnDefaultWhenNoExperiments | 正常路径 | 无实验时返回 createDefault() |
| shouldAssignToBucketByHash | 正常路径 | 哈希分桶落到正确分组 |
| shouldMatchCumulativePercentage | 边界条件 | 50/50 分组均覆盖，hash=499→A，hash=500→B |
| shouldReturnDefaultWhenHashExceedsAllPercentages | 边界条件 | group percentage 和 < 1000 时超出部分返回 default |
| shouldHandleNegativeHashValue | 边界条件 | hashCode() = -1, -1000, Integer.MIN_VALUE 时 floorMod 落于 [0,1000) |
| cacheShouldExpireAfterWrite | 状态交互 | Caffeine expireAfterWrite 5min 后重新查 DB |
| warmupShouldPrepopulateCache | 状态交互 | @PostConstruct 预热填充缓存 |
| eventShouldInvalidateCacheEntry | 状态交互 | ExperimentChangedEvent → 清除对应 capabilityId 缓存 |
| eventWithNullCapabilityIdShouldInvalidateAll | 状态交互 | capabilityId=null → 清空全部缓存 |
| assignShouldNotThrowOnNullUserIdOrSessionId | 错误路径 | userId/sessionId=null 时使用 "" 作为哈希输入 |
| concurrentAssignShouldNotThrow | 并发安全 | 多线程并发 assign() 不抛异常 |
| dbExceptionShouldReturnDefault | 错误路径 | DB 异常 → 日志 WARN → 返回 createDefault() |
| sessionIdNullButUserIdNonNullShouldInvokeHash | 边界条件 | sessionId=null 时仅用 userId 做哈希分桶 |
