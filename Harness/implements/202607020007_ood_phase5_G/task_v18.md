# 任务指令（v18）

## 动作
NEW

## 任务描述
在 ai-impl/experiment/ 包实现 A/B 实验管理层（8 个类型（含 1 个事件类）+ 2 个测试文件）。

**预期文件清单：**

| # | 文件路径 | 操作 | 职责 |
|--|---------|------|------|
| 1 | `experiment/ExperimentManager.java` | 新建 | 实验管理接口，定义 `assign(String capabilityId, String userId, String sessionId) → ExperimentAssignment` |
| 2 | `experiment/ExperimentAssignment.java` | 新建 | 实验分组结果不可变值对象，提供全参构造器 + `createDefault()` + `createErrorFallback()` 工厂方法 |
| 3 | `experiment/ExperimentStatus.java` | 新建 | 枚举 DRAFT / ACTIVE / PAUSED / COMPLETED |
| 4 | `experiment/Experiment.java` | 新建 | JPA @Entity，含 id/capabilityId/groups(OneToMany)/status/startTime/endTime 字段 |
| 5 | `experiment/ExperimentGroup.java` | 新建 | JPA @Entity，含 id/experiment(ManyToOne, fetch=LAZY)/groupId/percentage/targetModelId/targetPromptVersion 字段 |
| 6 | `experiment/ExperimentRepository.java` | 新建 | Spring Data JPA Repository + 查询方法 |
| 7 | `experiment/ExperimentChangedEvent.java` | 新建 | ApplicationEvent 子类，含 capabilityId/experimentId/changeType/changedAt |
| 8 | `experiment/HashBucketExperimentManager.java` | 新建 | @Service 实现：Caffeine 缓存、assign() 哈希分桶、@PostConstruct 预热、@EventListener 缓存失效 |
| 9 | `experiment/ExperimentAssignmentTest.java` | 新建 | 值对象契约测试（构造/createDefault/createErrorFallback/不可变性） |
| 10 | `experiment/HashBucketExperimentManagerTest.java` | 新建 | 分桶/缓存/预热/事件/并发/边界条件测试 |

全部文件包路径：`com.aimedical.modules.ai.impl.experiment`。

## 选择理由
Batch4 P2 次序第三项。ExperimentManager 是底座管线实验分流组件——被全部 7 项底座能力 CapabilityExecutor 的 `doExecuteInternal()` 在管线步骤 2（实验分流）中引用。所有前置依赖均已就绪：JPA（Task 12 已引入 spring-boot-starter-data-jpa + h2）、Caffeine（已有依赖）。Task 13 是第 3 个依赖 JPA 的类型，与 PromptTemplate 使用相同的 JPA 基础设施。

## 任务上下文

### 设计文档摘录（06_ood_phase5_G.md）

**ExperimentManager 接口**（§3.4）：
```java
public interface ExperimentManager {
    ExperimentAssignment assign(String capabilityId, String userId, String sessionId);
}
```

**ExperimentAssignment**（§3.4）：
- 不可变值对象，字段：`experimentId`(String), `groupId`(String), `targetModelId`(String), `targetPromptVersion`(Integer)
- 全参构造器：`ExperimentAssignment(String experimentId, String groupId, String targetModelId, Integer targetPromptVersion)`
- 无参默认工厂：`createDefault()` → `groupId="default"`, 其余字段 null
- 异常降级工厂：`createErrorFallback()` → `groupId="experiment-error"`, 其余字段 null

**ExperimentStatus**（§3.4）：
```java
DRAFT → ACTIVE → PAUSED → COMPLETED
```

**Experiment JPA Entity**（§3.4）：
- 字段：`id`(Long, PK), `capabilityId`(String, VARCHAR(50), not null), `groups`(List<ExperimentGroup>, @OneToMany(cascade = ALL, orphanRemoval = true, fetch = FetchType.EAGER)), `status`(ExperimentStatus, @Enumerated STRING), `startTime`(LocalDateTime), `endTime`(LocalDateTime, nullable)
- `@Table(name = "ai_experiment")`
- equals/hashCode based on id

**ExperimentGroup JPA Entity**（§3.4）：
- 字段：`id`(Long, PK, AUTO), `experiment`(Experiment, @ManyToOne(fetch = FetchType.LAZY)), `groupId`(String, VARCHAR(50), not null), `percentage`(int, 0-1000), `targetModelId`(String, VARCHAR(50), nullable), `targetPromptVersion`(Integer, nullable)
- `@Table(name = "ai_experiment_group")`

**ExperimentChangedEvent**（§3.4）：
```java
class ExperimentChangedEvent extends ApplicationEvent {
    String capabilityId;      // null=影响全部能力
    Long experimentId;        // null=影响该能力下全部实验
    ChangeType changeType;    // CREATED / UPDATED / DELETED / STATUS_CHANGED
    Instant changedAt;
}
```
- 与 TemplateChangedEvent 相同模式，内嵌 `ChangeType` 枚举

**HashBucketExperimentManager**（§3.4）：
- 内部维护 `Caffeine<CacheKey, List<Experiment>>` 缓存（expireAfterWrite=5 分钟, maximumSize=100）
- `assign()` 实现：Math.floorMod(userId/sessionId.hashCode(), 1000) → [0,1000) 区间 → 遍历分组累加 percentage → 首个 cumulativePercentage > hash 的分组
- 未命中任何实验 → `createDefault()`
- @PostConstruct 预热：查询全部 ACTIVE 实验（含 ExperimentGroup），预填充缓存
- @EventListener 接收 ExperimentChangedEvent：清除对应 capabilityId 的缓存条目（capabilityId=null 清空全部）
- groupId 不可变语义：由管理端保证，实现侧不做校验（同 Template 模式）
- 流量分配精度：千分比整数累加 + 整数比较，避免浮点精度问题
- 同一 capability 同时最多一个 ACTIVE 实验（管理端保证）；若因异常出现多个，assign() 取按 startTime 降序排序后的第一条作为有效实验，确保选择确定性

**ExperimentRepository**（§3.4）：
```java
public interface ExperimentRepository extends JpaRepository<Experiment, Long> {
    List<Experiment> findByStatus(ExperimentStatus status);
    List<Experiment> findByCapabilityIdAndStatus(String capabilityId, ExperimentStatus status);
    List<Experiment> findByStatusAndEndTimeBetween(ExperimentStatus status, LocalDateTime start, LocalDateTime end);
    List<Experiment> findByCapabilityIdOrderByEndTimeDesc(String capabilityId);
}
```

### 已有代码上下文
- **JPA 基础设施**：Task 12 已引入 spring-boot-starter-data-jpa(compile) + h2(test) 依赖，可直接使用 `@Entity`, `@Repository` 等 JPA 注解
- **Caffeine**：已有依赖 `com.github.ben-manes.caffeine:caffeine`，Template 实现中已使用 `Caffeine.newBuilder().expireAfterWrite(...).maximumSize(...).build()`
- **TemplateChangedEvent 模式**：可作为 ExperimentChangedEvent 参照模板（相同结构：ApplicationEvent 子类 + ChangeType 内嵌枚举 + capabilityId 字段 + 无 source 语义约束）
- **DatabasePromptTemplateManager 模式**：可作为 HashBucketExperimentManager 参照（相同模式：@Service + Caffeine 缓存 + @PostConstruct 预热 + @EventListener 失效）
- **pom.xml 无需修改**：spring-boot-starter-data-jpa + h2 已在 Task 12 添加，当前 11 个依赖满足 Task 13 编译需求
- **AiImplPomCleanDependencyTest 无需修改**：断言总数仍为 11

### 测试规划

**ExperimentAssignmentTest（6 条）**：

| 测试方法 | 契约维度 |
|---------|---------|
| shouldConstructWithAllFields | 全参构造器 + getter |
| shouldCreateDefaultAssignment | createDefault() 返回 groupId="default" |
| shouldCreateErrorFallbackAssignment | createErrorFallback() 返回 groupId="experiment-error" |
| shouldBeImmutable | 不可变性验证（class 为 final，无 setter） |
| shouldSupportNullTargetFields | targetModelId/targetPromptVersion 可为 null |
| equalsAndHashCodeShouldNotBeImplemented | 值对象不覆写 equals/hashCode（按引用比较） |

**HashBucketExperimentManagerTest（12 条）**：

| 测试方法 | 契约维度 | 覆盖内容 |
|---------|---------|---------|
| shouldReturnDefaultWhenNoExperiments | 正常路径 | 无实验时返回 createDefault() |
| shouldAssignToBucketByHash | 正常路径 | 哈希分桶落到正确分组 |
| shouldMatchCumulativePercentage | 边界条件 | 50/50 分组均覆盖，hash=499→A, hash=500→B |
| shouldReturnDefaultWhenHashExceedsAllPercentages | 边界条件 | 分组 percentage 和不等于 1000 时超出部分返回 default |
| shouldHandleNegativeHashValue | 边界条件 | hashCode()=-1,-1000,Integer.MIN_VALUE 时 floorMod 落于 [0,1000) 区间 |
| cacheShouldExpireAfterWrite | 状态交互 | Caffeine expireAfterWrite 5min 后重新查 DB |
| warmupShouldPrepopulateCache | 状态交互 | @PostConstruct 预热填充缓存 |
| eventShouldInvalidateCacheEntry | 状态交互 | ExperimentChangedEvent → 清除对应 capabilityId 缓存 |
| eventWithNullCapabilityIdShouldInvalidateAll | 状态交互 | capabilityId=null → 清空全部缓存 |
| assignShouldNotThrowOnNullUserIdOrSessionId | 错误路径 | userId/sessionId 为 null 时使用 "" 作为哈希输入 |
| concurrentAssignShouldNotThrow | 并发安全 | 多线程并发 assign() 不抛异常 |
| dbExceptionShouldReturnDefault | 错误路径 | DB 异常 → 日志 WARN → 返回 createDefault()；触发方式：先在 @PostConstruct 前 mock Repository 抛异常使 cache miss 后查询 DB 失败，或预热后 @EventListener 清空缓存 → cache miss → DB 查询失败 |
| sessionIdNullButUserIdNonNullShouldInvokeHash | 边界条件 | sessionId=null 时仅用 userId 做哈希分桶 |

---

## 修订说明（v18 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] 路线表 row 13 "涉及文件"列不完整（缺少 ExperimentStatus.java、ExperimentChangedEvent.java、ExperimentAssignmentTest.java、HashBucketExperimentManagerTest.java） | plan.md 路线表 row 13 "涉及文件"列补齐为 10 个文件；R21 NEW 标题同步补齐 ExperimentStatus + ExperimentChangedEvent |
| [一般] Experiment.groups @OneToMany LAZY 加载策略未明确，存在 LazyInitializationException 运行时风险 | Experiment.groups 获取策略明确为 `FetchType.EAGER`；任务上下文 Experiment JPA Entity 定义中补充 fetch = FetchType.EAGER |
| [轻微] "8 个类型 + 1 个事件类" 计数描述可优化 | 任务描述改为"8 个类型（含 1 个事件类）+ 2 个测试文件"保证计数准确 |

## 修订说明（v18 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] HashBucketExperimentManager.assign() 对同一 capabilityId 存在多个 ACTIVE 实验时的选择策略未定义 | 补充约定：同一 capability 同时最多一个 ACTIVE 实验（管理端保证）；若出现多个则按 startTime 降序取第一条，确保确定性。已在「任务上下文」HashBucketExperimentManager 小节添加该策略 |
| [轻微] ExperimentGroup.@ManyToOne experiment 未指定 fetch 策略，默认 EAGER 与 Experiment.groups EAGER 形成双向 EAGER | 已修改为 `@ManyToOne(fetch = FetchType.LAZY)`，避免不必要的联表查询。已在预期文件清单 Item 5 及「任务上下文」ExperimentGroup 字段定义中同步更新 |

## 修订说明（v18 r3）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] 路线表 row 13 "涉及文件"列仍不完整（缺少 ExperimentStatus.java、ExperimentChangedEvent.java、ExperimentAssignmentTest.java、HashBucketExperimentManagerTest.java） | 路线表 row 13 "涉及文件"列已补齐为 10 个文件（此前 R22 r1 修订已更新） |
| [一般] hash 计算可能产生负值，违反 [0,1000) 区间约定 — `hashCode() % 1000` 在负数时结果不在 [0,1000) 内 | hash 计算改为 `Math.floorMod(hashCode(), 1000)`，结果恒为非负；测试规划新增 `shouldHandleNegativeHashValue` 覆盖 hash=-1,-1000,Integer.MIN_VALUE 边界 |
| [轻微] dbExceptionShouldReturnDefault 测试的执行路径未明确（预热后缓存命中无法触发 DB 异常） | 测试规划中补充触发方式说明：在 @PostConstruct 前 mock Repository 抛异常，或预热后 @EventListener 清空缓存制造 cache miss |

## 修订说明（v18 r4）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] ExperimentRepository 缺少 `findByStatus(ExperimentStatus)` 查询方法。HashBucketExperimentManager.warmup() 需要"查询全部 ACTIVE 实验（含 ExperimentGroup），预填充缓存"，但当前 3 个查询方法均需 capabilityId 或时间范围作为过滤条件，无法直接返回全部 ACTIVE 实验 | ExperimentRepository 新增 `List<Experiment> findByStatus(ExperimentStatus status);` 方法，与 PromptTemplateRepository.findByStatus(TemplateStatus) 模式一致，用于 warmup() 查询全部 ACTIVE 实验 |
