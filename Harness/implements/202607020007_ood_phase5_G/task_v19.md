# 任务指令（v19）

## 动作
NEW

## 任务描述
在 `ai-impl/metrics/` 包创建 AI 底座指标采集体系，包括以下类型：

### 生产文件
| # | 文件 | 操作 | 职责 |
|---|------|------|------|
| 1 | `metrics/LoggingMetricsCollector.java` | 新建 | AiMetricsCollector 实现：@Async 异步接收 AiCallRecord，写入 AiCallLogEntity（JPA） |
| 2 | `metrics/AiCallLogEntity.java` | 新建 | AI 调用日志 JPA @Entity，字段与现有 AiCallRecord 对等 |
| 3 | `metrics/AiCallLogRepository.java` | 新建 | Spring Data JPA Repository for AiCallLogEntity |
| 4 | `metrics/AiCallLogStats.java` | 新建 | 月度聚合统计 JPA @Entity |
| 5 | `metrics/AiCallLogStatsRepository.java` | 新建 | Spring Data JPA Repository for AiCallLogStats |
| 6 | `client/AiClientConfig.java` | 修改 | 添加 `@EnableAsync` 使 @Async 生效 |

### 测试文件
| # | 文件 | 操作 | 覆盖内容 |
|---|------|------|---------|
| 6 | `metrics/LoggingMetricsCollectorTest.java` | 新建 | @Async 异步写入验证、AiCallLogEntity 持久化验证、null 安全 |
| 7 | `metrics/AiCallLogEntityTest.java` | 新建 | JPA 实体映射、字段 getter/setter、JPA 注解兼容性 |
| 8 | `metrics/AiCallLogStatsTest.java` | 新建 | 值对象契约、JPA 注解验证 |
| 9 | `pom/AiImplPomCleanDependencyTest.java` | 修改（如需） | 断言同步新增 actuator 依赖计数 |

所有文件包路径：`com.aimedical.modules.ai.impl.metrics`（AiCallLogStats 与 AiCallLogEntity 共享 metrics 子包）。

### 形态约束
- **LoggingMetricsCollector**：class，implements AiMetricsCollector，标注 `@Service`
  - `record(AiCallRecord)` 方法标注 `@Async`（无 qualifier，使用 Spring 默认 `applicationTaskExecutor`），方法体：构建 `AiCallLogEntity` → `repository.save(entity)` 
  - 持有 `AiCallLogRepository` 构造器注入
  - 错误处理：catch 异常后 WARN 日志，不抛给调用方（指标写入失败不应影响主流程）
- **AiCallLogEntity**：@Entity，`@Table(name = "ai_call_log")`，字段与现有 AiCallRecord 对等（映射策略见下节映射表）
  - 字段：id(Long, PK, AUTO)、callTime(LocalDateTime)、capabilityId、capabilityName、visitId、patientId、departmentId、callerRole、callerId、userId、inputSummary、outputSummary、degraded(boolean)、degradationReason、elapsedMs(long)、errorCode、errorMessage、modelId、retryCount(int, default 0)、sessionId、promptVersion(Integer)、promptTokens(Integer)、completionTokens(Integer)、totalTokens(Integer)
  - `@Column(columnDefinition = "DATETIME(3)")` for callTime
  - 全参构造器 + 无参构造器（JPA 要求）+ getter/setter
  - `@Table(indexes = {...})` 声明关键索引（idx_call_time、idx_capability_call_time、idx_degraded_call_time）
- **AiCallLogRepository**：`extends JpaRepository<AiCallLogEntity, Long>`，新增 `countByCallTimeBefore(LocalDateTime cutoff)` 查询方法
- **AiCallLogStats**：@Entity，`@Table(name = "ai_call_log_stats")`
  - 字段：id(Long, PK, AUTO)、capabilityId(String)、statMonth(String,格式"YYYY-MM")、totalCalls(long)、successCount(long)、degradedCount(long)、failureCount(long)、avgElapsedMs(double)、p50ElapsedMs(double)、p95ElapsedMs(double)、p99ElapsedMs(double)
  - `@Table(indexes = @Index(name = "idx_stats_capability_month", columnList = "capabilityId, statMonth DESC"))`
- **AiCallLogStatsRepository**：`extends JpaRepository<AiCallLogStats, Long>`
  - 查询方法：`List<AiCallLogStats> findByCapabilityIdAndStatMonthBetween(String capabilityId, String startMonth, String endMonth)`

### AiCallRecord → AiCallLogEntity 字段映射表

| AiCallLogEntity 字段 | 值来源 | 说明 |
|---------------------|--------|------|
| id | AUTO | JPA 自增主键 |
| callTime | `LocalDateTime.now()` | 记录写入时间，非 AiCallRecord 传入 |
| capabilityId | `record.getCapabilityId()` | 直接复制 |
| capabilityName | `record.getCapabilityId()` | 本阶段 capabilityName 与 capabilityId 一致 |
| visitId | `record.getVisitId()` | 直接复制 |
| patientId | `record.getPatientId()` | 直接复制 |
| departmentId | `record.getDepartmentId()` | 直接复制 |
| callerRole | `record.getCallerRole()` | 直接复制 |
| callerId | `record.getCallerId()` | 直接复制 |
| userId | `record.getUserId()` | 直接复制 |
| inputSummary | `null` | 本阶段暂不采集，后续阶段扩展 |
| outputSummary | `null` | 本阶段暂不采集，后续阶段扩展 |
| degraded | `record.isDegraded()` | 直接复制 |
| degradationReason | `record.getDegradeReason()` | 直接复制 |
| elapsedMs | `record.getElapsedMs()` | 直接复制 |
| errorCode | `null` | 本阶段暂不采集 |
| errorMessage | `null` | 本阶段暂不采集 |
| modelId | `record.getModelId()` | 直接复制 |
| retryCount | `0` | 本阶段暂不采集重试计数 |
| sessionId | `record.getSessionId()` | 直接复制 |
| promptVersion | `record.getPromptVersion()` | 直接复制（AiCallRecord 为 String，存入前需做 null/parse 安全转换，parse 失败则存 null） |
| promptTokens | `record.getPromptTokens()` | 直接复制（int） |
| completionTokens | `record.getCompletionTokens()` | 直接复制（int） |
| totalTokens | `null`（Integer） | 暂不计算 totalTokens = promptTokens + completionTokens，保持后期统一 |

**实现要点**：
- promptVersion 处理：`record.getPromptVersion()` 可能为 null，尝试 `Integer.parseInt()`，抛异常则存 null
- 所有 `null` 字段使用 `Integer`/`String` 可空类型，不设默认非空约束

### 不变约定
- **不修改现有 AiCallRecord**：AiCallRecord 保持现有 15 参数构造器不变，所有现有调用方不受影响
- **不添加 actuator/Micrometer 依赖**：Phase 5 初始阶段仅做 JPA 持久化，Micrometer 集成推迟至后续阶段
- **不使用 @Scheduled 分区清理**：分区清理和 stats 聚合聚合为独立任务（后续阶段），本任务仅提供 AiCallLogStats + AiCallLogStatsRepository 骨架
- pom.xml 无需修改（JPA + h2 已在 Task 12 引入；actuator 暂不引入）
- **@EnableAsync 配置**：在已有 `AiClientConfig.java` 类上添加 `@EnableAsync` 注解，使 `LoggingMetricsCollector.record()` 的 `@Async` 生效；`@Async` 无 qualifier，使用 Spring 默认 `applicationTaskExecutor`

## 选择理由
Batch4 P2 次序末项。AiMetricsCollector 是底座管线指标采集核心组件（被 AbstractCapabilityExecutor 和全部 13 项能力执行器直接依赖），所有前置依赖均已就绪（AiMetricsCollector 存根接口、AiCallRecord 值对象、JPA）。先完成 metrics 体系可解锁后续 Batch6 的 AiPlatformConfig 配置装配。

## 任务上下文

### 已有代码上下文
- **AiMetricsCollector.java**（接口存根）：已有 `void record(AiCallRecord record)` 方法签名
- **AiCallRecord.java**（值对象）：已有 15 字段全参构造器 + getter（含 `getUserId()`），被 AbstractCapabilityExecutor 和 6 项薄适配器调用 2+ 处
- **AiCallRecordTest.java**：3 个测试方法覆盖构造契约
- **JPA 依赖**：spring-boot-starter-data-jpa + h2 test scope 已在 Task 12 添加
- **AiClientConfig.java**（已有 @Configuration）：需要添加 `@EnableAsync` 使 @Async 生效

### 实施要点
1. AiCallLogEntity 字段与 AiCallRecord 字段对等但不要求 1:1 复制——AiCallLogEntity 增加 JPA 必需字段（id、callTime），且字段类型/名称可独立优化（如 degraded 使用 boolean/TINYINT）
2. LoggingMetricsCollector.record() 中构造 AiCallLogEntity 时，`callTime` 取 `LocalDateTime.now()`；`capabilityName` 取值与 capabilityId 相同（capabilityName 映射推迟至工厂方法重构阶段）
3. @Async 无 qualifier，使用 Spring 默认 `applicationTaskExecutor`；专用线程池 `metricsAsyncExecutor` 的命名和配置推迟至 Task 18 AiPlatformConfig 阶段
4. 修改 `AiClientConfig.java`：在类级别添加 `@EnableAsync` 注解，确保 `LoggingMetricsCollector.record()` 上的 `@Async` 真正生效
5. AiCallLogEntity 增加 `userId` 字段（String，可空），值来源 `record.getUserId()`，与 AiCallRecord 字段对等
6. 若 `AiImplPomCleanDependencyTest` 硬编码断言依赖计数，无需修改（不新增外部依赖）

---

## 修订说明（v19 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] @Async("metricsAsyncExecutor") 无对应 Bean 导致启动失败 | 改为 @Async（无 qualifier），使用 Spring 默认 applicationTaskExecutor |
| [一般] AiCallLogEntity 字段与现有 AiCallRecord 差异较大，缺失字段映射策略未定义 | 新增 AiCallRecord → AiCallLogEntity 字段映射表，逐字段标注来源策略（直接复制/null/派生） |

## 修订说明（v19 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] @EnableAsync 缺失导致 @Async 无法生效 | 选择 A：在已有 AiClientConfig.java 类上添加 `@EnableAsync`；生产文件表新增第 6 行 `client/AiClientConfig.java`（修改）；实施要点第 4 条说明修改方式；不变约定补充 @EnableAsync 配置说明 |
| [一般] AiCallLogEntity 遗漏 userId 字段 | AiCallLogEntity 字段列表补充 `userId(String)`；映射表补充 userId 行（值来源 `record.getUserId()`）；实施要点第 5 条说明 |
