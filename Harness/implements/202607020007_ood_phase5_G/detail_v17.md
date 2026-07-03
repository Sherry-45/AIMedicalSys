# 详细设计（v17）

## 概述

实现 Prompt 模板管理层：包括 JPA 实体、Repository、模板缓存管理器和事件通知；同步修改 PromptTemplateManager 接口签名及所有调用方 mock；新增 spring-boot-starter-data-jpa + h2 测试依赖。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `template/PromptTemplateManager.java` | 修改 | 接口签名从 `render(String, Map)` 改为 `render(String, String, Map, String)` + 新增 `getFallbackPrompt(String)` |
| `template/TemplateStatus.java` | 新建 | 枚举 DRAFT / ACTIVE / DEPRECATED |
| `template/PromptTemplate.java` | 新建 | JPA @Entity，含字段 + 唯一约束 |
| `template/PromptTemplateRepository.java` | 新建 | Spring Data JPA Repository |
| `template/TemplateChangedEvent.java` | 新建 | ApplicationEvent 子类 |
| `template/DatabasePromptTemplateManager.java` | 新建 | PromptTemplateManager 实现 |
| `template/PromptTemplateTest.java` | 新建 | 实体契约测试 |
| `template/DatabasePromptTemplateManagerTest.java` | 新建 | 缓存/预热/事件/兜底/并发测试 |
| `orchestrator/AbstractCapabilityExecutor.java` | 修改 | `executeStandardPipeline()` 中的 render 调用改为 4 参数，移除 templateKey |
| `ai-impl/pom.xml` | 修改 | 添加 spring-boot-starter-data-jpa + h2 |
| `pom/AiImplPomCleanDependencyTest.java` | 修改 | 断言从 9→11，方法名改为 totalDependenciesCountShouldBeEleven |
| `orchestrator/AbstractCapabilityExecutorTest.java` | 修改 | 16 处 lambda→Mockito mock |
| `orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java` | 修改 | 3 处 lambda→Mockito mock |
| `orchestrator/impl/TriageCapabilityExecutorTest.java` | 修改 | 1 处 lambda→Mockito mock |

所有文件包路径：`com.aimedical.modules.ai.impl.template`（template 目录下），测试文件对应到 `src/test/java` 相同包路径。

## 类型定义

### PromptTemplateManager（接口）
**形态**：interface
**包路径**：`com.aimedical.modules.ai.impl.template`
**职责**：模板渲染与兜底管理

```java
public interface PromptTemplateManager {
    String render(String capabilityId, String departmentId, Map<String, Object> variables, String promptVersion);
    String getFallbackPrompt(String capabilityId);
}
```

**说明**：promptVersion 为 null 时使用当前 ACTIVE 模板；非 null 时按具体版本查找（实现层 parse 为 int）。departmentId 可为 null 表示全局模板。

---

### TemplateStatus（枚举）
**形态**：enum
**包路径**：`com.aimedical.modules.ai.impl.template`
**职责**：模板生命周期状态

```java
public enum TemplateStatus {
    DRAFT,
    ACTIVE,
    DEPRECATED
}
```

状态转换：`DRAFT → ACTIVE → DEPRECATED`
- DRAFT：草稿，不参与渲染
- ACTIVE：运行时使用，同（capabilityId, departmentId）同时仅一个 ACTIVE
- DEPRECATED：废弃，历史保留

---

### PromptTemplate（JPA Entity）
**形态**：@Entity
**包路径**：`com.aimedical.modules.ai.impl.template`
**职责**：模板持久化记录

```java
@Entity
@Table(name = "ai_prompt_template",
       uniqueConstraints = @UniqueConstraint(columnNames = {"capabilityId", "departmentId", "version"}))
public class PromptTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String capabilityId;

    @Column(length = 50)
    private String departmentId;    // null = 全局模板

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;         // 含 {{key}} 占位符

    @Column(nullable = false)
    private int version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TemplateStatus status = TemplateStatus.DRAFT;
}
```

**公开接口**：构造器（全参/无参）、getter/setter、equals/hashCode（基于 id）
**构造方式**：new 后 setter 赋值；new 时 status 默认 DRAFT
**类型关系**：无继承/实现

---

### PromptTemplateRepository
**形态**：interface（Spring Data JPA Repository）
**包路径**：`com.aimedical.modules.ai.impl.template`
**职责**：PromptTemplate 数据库访问

```java
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {
    List<PromptTemplate> findByCapabilityIdAndStatus(String capabilityId, TemplateStatus status);
    List<PromptTemplate> findByCapabilityIdAndDepartmentIdAndStatus(
        String capabilityId, String departmentId, TemplateStatus status);
    Optional<PromptTemplate> findByCapabilityIdAndDepartmentIdAndVersion(
        String capabilityId, String departmentId, int version);
    List<PromptTemplate> findByStatus(TemplateStatus status);
}
```

`findByCapabilityIdAndDepartmentIdAndVersion()` 返回 `Optional` 以表达"该版本不存在"的语义（与 List 返回的"无结果"区分）。注意第三个参数为 `int version`，`PromptTemplate` 实体中的 `version` 字段类型为 `int`（数据库列 `INT`）。

---

### TemplateChangedEvent
**形态**：class（extends ApplicationEvent）
**包路径**：`com.aimedical.modules.ai.impl.template`
**职责**：模板变更事件，驱动缓存失效

```java
public class TemplateChangedEvent extends ApplicationEvent {
    private final String capabilityId;
    private final String departmentId;      // null = 影响全部科室
    private final Integer promptVersion;    // null = 全部版本
    private final ChangeType changeType;    // CREATED / UPDATED / DELETED / STATUS_CHANGED
    private final Instant changedAt;

    public enum ChangeType {
        CREATED, UPDATED, DELETED, STATUS_CHANGED
    }

    public TemplateChangedEvent(Object source, String capabilityId, String departmentId,
                                 Integer promptVersion, ChangeType changeType, Instant changedAt);
    // + getter 方法
}
```

**构造方式**：new TemplateChangedEvent(source, capabilityId, departmentId, promptVersion, changeType, changedAt)
**类型关系**：继承 ApplicationEvent；ChangeType 为内嵌枚举

---

### DatabasePromptTemplateManager
**形态**：class（implements PromptTemplateManager）
**包路径**：`com.aimedical.modules.ai.impl.template`
**职责**：基于 JPA + Caffeine 缓存的模板管理器

```java
@Service
public class DatabasePromptTemplateManager implements PromptTemplateManager {

    private final PromptTemplateRepository repository;
    private final Environment environment;
    private final Cache<String, PromptTemplate> cache;

    public DatabasePromptTemplateManager(PromptTemplateRepository repository, Environment environment) {
        this.repository = repository;
        this.environment = environment;
        this.cache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();
    }

    @PostConstruct
    public void warmup();

    @EventListener
    public void onTemplateChanged(TemplateChangedEvent event);

    @Override
    public String render(String capabilityId, String departmentId,
                         Map<String, Object> variables, String promptVersion);

    @Override
    public String getFallbackPrompt(String capabilityId);
}
```

**公开接口**：render（替换占位符后返回 String）、getFallbackPrompt（返回兜底）
**构造方式**：Spring 自动装配（@Service + 构造器注入）
**类型关系**：实现 PromptTemplateManager

**render() 内部流程**：
1. 若 `promptVersion != null`：
   a. 解析 promptVersion 为 int（`Integer.parseInt(promptVersion)`），异常时 WARN 日志 + 回退到 ACTIVE 策略（goto 2）
   b. DB 查询 `repository.findByCapabilityIdAndDepartmentIdAndVersion(capabilityId, departmentId, parsedVersion)`，无科室级 → 再查 `repository.findByCapabilityIdAndDepartmentIdAndVersion(capabilityId, null, parsedVersion)`（全局模板）
   c. 找到且 status == ACTIVE → 使用此模板（goto 4 替换占位符）
   d. 找到但 status != ACTIVE → WARN 日志（指定版本已废弃/非 ACTIVE），回退到 ACTIVE 策略（goto 2）
   e. 未找到 → 回退到 ACTIVE 策略（goto 2）
   f. 注：指定版本查询不经缓存
2. promptVersion == null（或从 1 回退）：ACTIVE 模板检索
   a. 缓存 key = capabilityId + ":" + (departmentId != null ? departmentId : "")
   b. 从 Caffeine 缓存按 key 查询 PromptTemplate
   c. 命中 → 取 content，goto 4
   d. 未命中 → DB 查询 `repository.findByCapabilityIdAndDepartmentIdAndStatus(capabilityId, departmentId, ACTIVE)`
   e. 未找到 → 查 `repository.findByCapabilityIdAndDepartmentIdAndStatus(capabilityId, null, ACTIVE)`（全局模板兜底，departmentId IS NULL）
   f. 填充缓存
3. 模板无结果 → 返回 null
4. 遍历 variables.entrySet()，对 content 执行 `replace("{{" + key + "}}", value)`，value 为 null 时保留占位符并 WARN 日志
5. 返回渲染后的字符串

**缓存设计说明**：cache key 为 `(capabilityId, departmentId)` 二维，仅缓存 ACTIVE 模板。当 `promptVersion != null`（即 A/B 实验指定版本号）时绕过缓存直接查询 DB——因为版本指定查询频率远低于 ACTIVE 模板查询，且 A/B 实验分组结果不跨请求共享缓存价值有限，引入三维缓存反而使缓存失效逻辑复杂化（onTemplateChanged 需同时清除 active 条目和所有版本条目）。

**getFallbackPrompt() 内部流程**：
1. 查 `environment.getProperty("ai.template.fallback." + capabilityId)`
2. 有配置 → 返回配置值
3. 无配置 → 返回通用兜底 "You are a helpful medical AI assistant. Reply concisely."

**warmup() 流程**：
1. `repository.findByStatus(ACTIVE)` 查询全部 ACTIVE 模板
2. 逐条写入缓存，key = capabilityId + ":" + (departmentId ?? "")

**onTemplateChanged() 流程**：
1. 从 event 获取 capabilityId 和 departmentId
2. departmentId != null → 清除单条缓存 key `capabilityId + ":" + departmentId`
3. departmentId == null → `cache.invalidateAll()` 清除全部缓存，然后调用 `warmup()` 重建
4. 注：因 Caffeine 不支持按前缀遍历，departmentId=null 时无法选择性清除匹配能力标识的所有科室条目，故采用全量 invalidateAll() + warmup()

---

## 错误处理

- render()：variables 中 value 为 null 时保留占位符、WARN 日志、不抛异常
- render()：所有 DB 查询路径（科室级 + 全局级 departmentId IS NULL）均无结果 → 返回 null（上层 pipeline 有 null 兜底逻辑）
- getFallbackPrompt()：始终返回非 null 非空字符串
- DatabasePromptTemplateManager 内部异常（DB 访问等）：catch 后 WARN 日志 + 返回 null/getFallbackPrompt
- 无自定义异常类型，复用 RuntimeException 传播

## 行为契约

- render() 前置条件：capabilityId 非 null
- render() 中 promptVersion 行为：
  - promptVersion == null 或回退 → 按 ACTIVE 状态查找，使用缓存
  - promptVersion != null → 按精确版本查找（不经缓存），仅 ACTIVE 状态的指定版本可用；DEPRECATED 或无此版本时 WARN 日志并回退到 ACTIVE 策略
- render() 后置条件：返回 String（可能为 null，表示无可用 ACTIVE 模板）
- getFallbackPrompt() 后置条件：返回非 null 非空字符串
- warmup() 在 @PostConstruct 阶段自动执行一次
- onTemplateChanged() 在 ApplicationEvent 发布后同步执行
- 调用方（AbstractCapabilityExecutor）：当 render 返回 null 时使用通用兜底 prompt

## 依赖关系

### AbstractCapabilityExecutor 调用变更

**修改位置**：`AbstractCapabilityExecutor.java:341-344`

原代码：
```java
String templateKey = capabilityId + ":" + promptVersion;
String renderedPrompt;
try {
    renderedPrompt = promptTemplateManager.render(templateKey, variables);
} catch (Exception e) {
    ...
}
```

修改后：
```java
String renderedPrompt;
try {
    renderedPrompt = promptTemplateManager.render(capabilityId, departmentId, variables, promptVersion);
} catch (Exception e) {
    ...
}
```

移除 `String templateKey = capabilityId + ":" + promptVersion;` 局部变量声明。

### pom.xml 依赖变更

**ai-impl/pom.xml** 新增：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

说明：spring-boot-starter-data-jpa 无 version（由 Spring Boot BOM 管理）；h2 无 version（由父 POM dependencyManagement `<h2.version>2.2.224</h2.version>` 管理），scope=test。

### AiImplPomCleanDependencyTest 断言变更

- 方法名：`totalDependenciesCountShouldBeEight` → `totalDependenciesCountShouldBeEleven`
- 断言：`assertEquals(9, count.intValue())` → `assertEquals(11, count.intValue())`
- 依赖数 9→11：新增 spring-boot-starter-data-jpa + h2

### 测试 mock 改造（3 个文件）

**新增静态导入（每个文件各 1 组）**：
```java
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
```

**lambda → Mockito mock 替换**：

所有形如 `PromptTemplateManager mockTemplate = (key, vars) -> "rendered";` 均替换为：
```java
PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
```

render 返回 null 的情况（AbstractCapabilityExecutorTest.java:721）：
```java
PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
when(mockTemplate.render(any(), any(), any(), any())).thenReturn(null);
when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
```

**影响范围**：
- `AbstractCapabilityExecutorTest.java`：16 处替换（lines 430, 451, 512, 538, 587, 641, 693, 721, 765, 815, 861, 904, 963, 1023, 1076, 1132）
- `DiscussionConclusionCapabilityExecutorTest.java`：3 处替换（lines 260, 295, 380）
- `TriageCapabilityExecutorTest.java`：1 处替换（line 55）

> **行号提示**：以上行号来源于 v16 设计时的代码快照。v16 已在相同文件中执行了 ModelRouter lambda 替换（1 行→1 行，不影响 PromptTemplateManager 行号），但 PromptTemplateManager lambda → Mockito mock 替换涉及 1 行→3 行的展开，后续行号会偏移。实际替换位置请以代码中 `(key, vars) ->` 模式搜索为准。

### 新增测试文件

**PromptTemplateTest.java**：@DataJpaTest + @AutoConfigureTestDatabase(replace = NONE) 或使用 h2
- 构造器/getter/equals/hashCode 契约
- 唯一约束验证（同一 (capabilityId, departmentId, version) 不可重复插入）
- 字段 nullable 约束验证
- 枚举持久化验证（EnumType.STRING）

**DatabasePromptTemplateManagerTest.java**：使用 @MockBean + @InjectMocks 或纯单元测试
**基础路径（8 条）**：
- 缓存命中：render 返回缓存内容，不查询 DB
- 缓存未命中：查询 DB → 填充缓存 → 替换占位符返回
- warmup：@PostConstruct 后缓存预填充
- 事件失效：发布 TemplateChangedEvent 后对应缓存条目清除
- 兜底配置：environment 有配置时返回配置值
- 通用兜底：environment 无配置时返回固定字符串
- 并发安全：多线程并发 render 不抛异常
- null variable value：占位符保留 + WARN 日志

**promptVersion 分支（5 条）**：
- 精确版本 ACTIVE：promptVersion != null，精确版本找到且 status=ACTIVE → 使用该版本 content，不走缓存
- 版本非 ACTIVE：promptVersion != null，版本存在但 status!=ACTIVE → WARN 日志 + 回退到 ACTIVE 模板检索
- 版本在科室级不存在：promptVersion != null，科室级未找到 → 查全局模板级（departmentId=null），找到则使用
- 版本解析异常：promptVersion != null，Integer.parseInt 抛出 NumberFormatException → WARN 日志 + 回退到 ACTIVE 模板检索
- 全部无结果：render 的所有查询路径均无结果 → 返回 null

## 设计说明

1. **PromptTemplateManager.render() promptVersion 使用 String 而非 Integer**：与现有 7 个 CapabilityExecutor 调用方（`private String promptVersion`）保持类型一致，避免级联修改。null 表示"使用 ACTIVE 模板"，实现层内部按需 parse 为 int。
2. **departmentId 为 null 的逻辑**：在缓存 key 中表示为空字符串 `""`，DB 查询 `departmentId IS NULL`。
3. **Caffeine 无法按前缀遍历条目**：departmentId=null 的缓存失效事件（影响全部科室）触发全量 invalidateAll() + warmup()。departmentId 非 null 时精确清除单条缓存。
4. **render() 返回 null 的含义**：DB 中无对应 ACTIVE 模板，调用方（pipeline）继续使用通用兜底。
5. **测试中 Mockito mock 的 render 参数**：使用 `any()` 匹配全部 4 个参数，因为测试不关心具体参数值。
6. **promptVersion 指定查询不缓存**：当 promptVersion != null（A/B 实验指定版本号）时绕过缓存直接查询 DB。理由：(1) 版本指定查询频率远低于 ACTIVE 模板查询（仅实验分组命中且首次调用时触发）；(2) 避免缓存 DEPRECATED 版本导致误用已废弃模板——通过仅按需查询保证每次读取都是当前最新状态；(3) 引入三维缓存使缓存 key 空间膨胀（版本数 × 科室数），且 onTemplateChanged 事件需同时清除 active 条目和所有版本条目，复杂度高收益有限。

## 修订说明（v17 r1）

| 审查意见 | 修改措施 |
|---------|---------|
| [严重] `DatabasePromptTemplateManager.render()` 内部流程完全忽略 `promptVersion` 参数 | render() 流程新增 promptVersion != null 分支：按 capabilityId + departmentId + version 精确查询 DB；找到且 status=ACTIVE 则使用，否则 WARN 日志后回退到 ACTIVE 模板检索策略 |
| [严重] `PromptTemplateRepository` 缺少 `promptVersion` 维度的查询方法 | Repository 新增 `Optional<PromptTemplate> findByCapabilityIdAndDepartmentIdAndVersion(String capabilityId, String departmentId, int version)` 方法 |
| [一般] 缓存 key 不含版本维度，非 ACTIVE 版本无法命中缓存 | 评估后决定版本指定查询绕过缓存直接查 DB（见设计说明第 6 条），未引入三维缓存 key，保持缓存逻辑简洁 |

## 修订说明（v17 r2）

| 审查意见 | 修改措施 |
|---------|---------|
| [严重] render() 全局模板兜底查询未限定 departmentId IS NULL——`findByCapabilityIdAndStatus` 会返回所有科室的 ACTIVE 模板而非仅全局模板 | 步骤 2e 查询方法从 `repository.findByCapabilityIdAndStatus(capabilityId, ACTIVE)` 改为 `repository.findByCapabilityIdAndDepartmentIdAndStatus(capabilityId, null, ACTIVE)`——Spring Data JPA 传入 null 自动生成 `departmentId IS NULL` |
| [一般] PromptTemplate 实体 content 和 status 字段缺少 nullable = false——null content 导致 render() NPE，null status 导致 ACTIVE 比较 NPE | `content` 添加 `@Column(nullable = false, ...)`；`status` 添加 `@Column(nullable = false, ...)` + 字段初始化 `= TemplateStatus.DRAFT` |
| [一般] 测试场景未覆盖 promptVersion 分支的 4 条关键路径 + 边界路径 | 为 DatabasePromptTemplateManagerTest 新增 5 条 promptVersion 分支测试用例：精确版本 ACTIVE、版本非 ACTIVE 回退、科室级不存在转查全局、版本解析异常回退、全部无结果返回 null |
| [轻微] onTemplateChanged() 步骤 3（选择性清除）与步骤 4（invalidateAll + warmup）自相矛盾 | 统一描述：departmentId=null 时全量 invalidateAll() + warmup()；departmentId 非 null 时精确清除单条 key；在缓存设计说明中同步更新 |
| [轻微] 测试文件 mock 行号可能因 v16 修改而偏移 | 在受影响范围后添加行号提示声明"行号仅供参考，实际位置以 `(key, vars) ->` 模式搜索为准" |
