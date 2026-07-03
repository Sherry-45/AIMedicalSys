# 详细设计（v19）

## 概述

在 `ai-impl/metrics/` 包创建 AI 底座指标采集体系：LoggingMetricsCollector（@Service + @Async 异步 JPA 写入）、AiCallLogEntity（JPA @Entity）、AiCallLogRepository、AiCallLogStats（月度聚合骨架）、AiCallLogStatsRepository，并修改 AiClientConfig 添加 @EnableAsync。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `metrics/LoggingMetricsCollector.java` | 新建 | AiMetricsCollector 实现：@Async 异步接收 AiCallRecord，写入 AiCallLogEntity |
| `metrics/AiCallLogEntity.java` | 新建 | AI 调用日志 JPA @Entity，字段与 AiCallRecord 对等 |
| `metrics/AiCallLogRepository.java` | 新建 | Spring Data JPA Repository for AiCallLogEntity |
| `metrics/AiCallLogStats.java` | 新建 | 月度聚合统计 JPA @Entity（骨架） |
| `metrics/AiCallLogStatsRepository.java` | 新建 | Spring Data JPA Repository for AiCallLogStats |
| `client/AiClientConfig.java` | 修改 | 添加 `@EnableAsync` 使 @Async 生效 |

全部主代码包路径：`com.aimedical.modules.ai.impl.metrics`（AiCallLogStats 与 AiCallLogEntity 共享 metrics 子包）；AiClientConfig 包路径 `com.aimedical.modules.ai.impl.client`。

## 类型定义

### AiCallLogEntity（JPA Entity）
**形态**：@Entity
**包路径**：`com.aimedical.modules.ai.impl.metrics`
**职责**：AI 调用日志的 JPA 持久化实体，与 AiCallRecord 字段对等

```java
@Entity
@Table(name = "ai_call_log", indexes = {
    @Index(name = "idx_call_time", columnList = "callTime"),
    @Index(name = "idx_capability_call_time", columnList = "capabilityId, callTime DESC"),
    @Index(name = "idx_degraded_call_time", columnList = "degraded, callTime DESC")
})
public class AiCallLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "DATETIME(3)")
    private LocalDateTime callTime;

    @Column(length = 50)
    private String capabilityId;

    @Column(length = 50)
    private String capabilityName;

    @Column(length = 50)
    private String visitId;

    @Column(length = 50)
    private String patientId;

    @Column(length = 50)
    private String departmentId;

    @Column(length = 20)
    private String callerRole;

    @Column(length = 50)
    private String callerId;

    @Column(length = 50)
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String inputSummary;

    @Column(columnDefinition = "TEXT")
    private String outputSummary;

    @Column
    private boolean degraded;

    @Column(length = 255)
    private String degradationReason;

    @Column
    private long elapsedMs;

    @Column(length = 50)
    private String errorCode;

    @Column(length = 500)
    private String errorMessage;

    @Column(length = 50)
    private String modelId;

    @Column
    private int retryCount;

    @Column(length = 50)
    private String sessionId;

    @Column
    private Integer promptVersion;

    @Column
    private int promptTokens;

    @Column
    private int completionTokens;

    @Column
    private Integer totalTokens;

    // 无参构造器（JPA 必需）
    public AiCallLogEntity() {}

    // 全参构造器（按字段顺序）
    public AiCallLogEntity(Long id, LocalDateTime callTime,
                           String capabilityId, String capabilityName,
                           String visitId, String patientId,
                           String departmentId, String callerRole, String callerId,
                           String userId,
                           String inputSummary, String outputSummary,
                           boolean degraded, String degradationReason,
                           long elapsedMs,
                           String errorCode, String errorMessage,
                           String modelId, int retryCount,
                           String sessionId, Integer promptVersion,
                           int promptTokens, int completionTokens, Integer totalTokens) {}

    // getter/setter 方法
}
```

**公开接口**：全参构造器 + 无参构造器 + 全部字段 getter/setter
**构造方式**：new 后 setter 赋值（或全参构造器）
**类型关系**：无继承/实现

---

### AiCallLogRepository
**形态**：interface（Spring Data JPA Repository）
**包路径**：`com.aimedical.modules.ai.impl.metrics`
**职责**：AiCallLogEntity 数据库访问

```java
public interface AiCallLogRepository extends JpaRepository<AiCallLogEntity, Long> {
    long countByCallTimeBefore(LocalDateTime cutoff);
}
```

---

### AiCallLogStats（JPA Entity）
**形态**：@Entity
**包路径**：`com.aimedical.modules.ai.impl.metrics`
**职责**：月度聚合统计值对象骨架

```java
@Entity
@Table(name = "ai_call_log_stats", indexes = {
    @Index(name = "idx_stats_capability_month",
           columnList = "capabilityId, statMonth DESC")
})
public class AiCallLogStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String capabilityId;

    @Column(length = 7)
    private String statMonth;   // 格式 "YYYY-MM"

    @Column
    private long totalCalls;

    @Column
    private long successCount;

    @Column
    private long degradedCount;

    @Column
    private long failureCount;

    @Column
    private double avgElapsedMs;

    @Column
    private double p50ElapsedMs;

    @Column
    private double p95ElapsedMs;

    @Column
    private double p99ElapsedMs;

    // 无参构造器（JPA 必需）
    public AiCallLogStats() {}

    // 全参构造器
    public AiCallLogStats(Long id, String capabilityId, String statMonth,
                          long totalCalls, long successCount, long degradedCount, long failureCount,
                          double avgElapsedMs, double p50ElapsedMs, double p95ElapsedMs, double p99ElapsedMs) {}

    // getter/setter 方法
}
```

**公开接口**：全参构造器 + 无参构造器 + 全部字段 getter/setter
**构造方式**：new 后 setter 赋值
**类型关系**：无继承/实现

---

### AiCallLogStatsRepository
**形态**：interface（Spring Data JPA Repository）
**包路径**：`com.aimedical.modules.ai.impl.metrics`
**职责**：AiCallLogStats 数据库访问

```java
public interface AiCallLogStatsRepository extends JpaRepository<AiCallLogStats, Long> {
    List<AiCallLogStats> findByCapabilityIdAndStatMonthBetween(String capabilityId, String startMonth, String endMonth);
}
```

---

### LoggingMetricsCollector
**形态**：class（implements AiMetricsCollector）
**包路径**：`com.aimedical.modules.ai.impl.metrics`
**职责**：异步 JPA 写入的 AiMetricsCollector 实现

```java
@Service
public class LoggingMetricsCollector implements AiMetricsCollector {

    private static final Logger log = LoggerFactory.getLogger(LoggingMetricsCollector.class);

    private final AiCallLogRepository repository;

    public LoggingMetricsCollector(AiCallLogRepository repository) {}

    @Override
    @Async
    public void record(AiCallRecord record) {
        // 1. 构建 AiCallLogEntity
        // 2. repository.save(entity)
        // 3. catch Exception → log.warn()
    }
}
```

**公开接口**：record(AiCallRecord) — 标记 @Async 异步执行
**构造方式**：Spring 自动装配（@Service + 构造器注入）
**类型关系**：实现 AiMetricsCollector

**record() 内部流程**：
1. 构造 AiCallLogEntity：
   - `callTime = LocalDateTime.now()`
   - `capabilityId = record.getCapabilityId()`
   - `capabilityName = record.getCapabilityId()`（本阶段与 capabilityId 一致）
   - `visitId = record.getVisitId()`
   - `patientId = record.getPatientId()`
   - `departmentId = record.getDepartmentId()`
   - `callerRole = record.getCallerRole()`
   - `callerId = record.getCallerId()`
   - `userId = record.getUserId()`
   - `inputSummary = null`
   - `outputSummary = null`
   - `degraded = record.isDegraded()`
   - `degradationReason = record.getDegradeReason()`
   - `elapsedMs = record.getElapsedMs()`
   - `errorCode = null`
   - `errorMessage = null`
   - `modelId = record.getModelId()`
   - `retryCount = 0`
   - `sessionId = record.getSessionId()`
   - `promptVersion = parsePromptVersion(record.getPromptVersion())`（String → Integer，null/parse 失败存 null）
   - `promptTokens = record.getPromptTokens()`
   - `completionTokens = record.getCompletionTokens()`
   - `totalTokens = null`
2. `repository.save(entity)`
3. 全部异常：catch Exception → `log.warn("Failed to persist AiCallLogEntity", e)`，不抛给调用方

## promptVersion 安全转换

```java
private static Integer parsePromptVersion(String version) {
    if (version == null) return null;
    try {
        return Integer.parseInt(version);
    } catch (NumberFormatException e) {
        return null;
    }
}
```

## 错误处理

- LoggingMetricsCollector.record() 内 catch Exception 后 WARN 日志，不抛异常给调用方
- AiCallLogEntity 的可空字段（inputSummary, outputSummary, errorCode, errorMessage, totalTokens, promptVersion）允许 null，不设 NOT NULL 约束
- 无自定义异常类型

## 行为契约

- record() 前置条件：AiCallRecord 参数可为 null（此时 WARN 日志 + return，不写库）
- record() 后置条件：无（不保证写入成功，异常不传播）
- record() 中 callTime 固定使用 `LocalDateTime.now()`，不使用 AiCallRecord 传入时间
- @Async 方法无 qualifier，使用 Spring 默认 `applicationTaskExecutor`
- AiCallLogEntity 的 id 由 JPA IDENTITY 自增生成，不手动赋值
- AiCallLogStats 仅为骨架，本阶段不包含聚合计算逻辑

## 依赖关系

### 依赖已有类型
- **AiMetricsCollector**（interface）：`metrics/AiMetricsCollector.java`，LoggingMetricsCollector 实现该接口
- **AiCallRecord**（value object）：`metrics/AiCallRecord.java`，record(AiCallRecord) 输入参数
- **AiClientConfig**：`client/AiClientConfig.java`，添加 `@EnableAsync` 注解

### 依赖已有依赖
- **spring-boot-starter-data-jpa**：已在 pom.xml（Task 12 引入），所有 @Entity 和 Repository 可用
- **h2**：test scope，测试时使用
- **SLF4J**：spring-boot-starter 提供

### 无需修改的文件
- **pom.xml**：不新增依赖（JPA + h2 已存在，actuator 暂不引入）
- **AiCallRecord.java**：不修改（保持现有 15 参数构造器）
- **AiImplPomCleanDependencyTest**：不修改（不新增外部依赖）

## 修改计划：AiClientConfig.java

**操作**：在类级别添加 `@EnableAsync` 注解

```java
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AiClientConfig {
    // 现有 Bean 定义保持不变
}
```

`@EnableAsync` 使 `LoggingMetricsCollector.record()` 上的 `@Async` 生效，使用 Spring 默认 `applicationTaskExecutor`。

## 测试规划

### LoggingMetricsCollectorTest（@ExtendWith(MockitoExtension.class)）

| 测试方法 | 契约维度 | 覆盖内容 |
|---------|---------|---------|
| shouldSaveEntityWhenRecordCalled | 正常路径 | 验证 record() 调用 repository.save() 且传入正确实体 |
| shouldHandleNullRecordGracefully | 错误路径 | record(null) 不抛异常 |
| shouldHandleRepositoryExceptionGracefully | 错误路径 | repository.save() 抛异常时被 catch，不传播 |
| shouldSetCallTimeToNow | 字段映射 | callTime 为 LocalDateTime.now() 非 null |
| shouldMapFieldsCorrectly | 字段映射 | 验证 capabilityId, modelId, elapsedMs 等字段正确映射 |
| shouldParsePromptVersionSafely | 字段映射 | null → null, "2" → 2, "invalid" → null |
| shouldSetCapabilityNameSameAsCapabilityId | 字段映射 | capabilityName == capabilityId |
| shouldNotThrowWhenConcurrentlyCalled | 并发安全 | 多线程并发调用 record() 不抛异常 |

### AiCallLogEntityTest

| 测试方法 | 契约维度 | 覆盖内容 |
|---------|---------|---------|
| shouldConstructWithNoArgsConstructor | 正常路径 | JPA 无参构造器不抛异常 |
| shouldConstructWithAllArgsConstructor | 正常路径 | 全参构造器+getter 返回正确值 |
| shouldSupportSetters | 正常路径 | setter 写入后 getter 返回正确值 |
| shouldHandleNullOptionalFields | 边界条件 | 可空字段可设为 null |
| jpaAnnotationsShouldBePresent | 编译检查 | @Entity, @Table, @Id, @GeneratedValue, @Column, @Index 声明正确 |

### AiCallLogStatsTest

| 测试方法 | 契约维度 | 覆盖内容 |
|---------|---------|---------|
| shouldConstructWithNoArgsConstructor | 正常路径 | 无参构造器不抛异常 |
| shouldConstructWithAllArgsConstructor | 正常路径 | 全参构造器 |
| shouldSupportSetters | 正常路径 | setter/getter |
| jpaAnnotationsShouldBePresent | 编译检查 | @Entity, @Table, @Id, @Index 声明正确 |
