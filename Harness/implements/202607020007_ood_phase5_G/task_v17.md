# 任务指令（v17）

## 动作
NEW

## 任务描述
在 ai-impl/template/ 包实现 Prompt 模板管理层（5 个类型 + 1 个事件类 + 2 个测试文件），同步修改 PromptTemplateManager 接口并更新所有调用方 mock；修改 ai-impl/pom.xml 添加 spring-boot-starter-data-jpa 依赖。

**预期文件清单：**

| # | 文件路径 | 操作 | 职责 |
|--|---------|------|------|
| 1 | `template/PromptTemplateManager.java` | 修改 | 接口签名从 `render(String, Map)` 改为 `render(String capabilityId, String departmentId, Map<String,Object> variables, Integer promptVersion)` + 新增 `getFallbackPrompt(String capabilityId)` |
| 2 | `template/PromptTemplate.java` | 新建 | JPA @Entity，含 id/capabilityId/departmentId/content/version/status 字段 + 状态枚举 |
| 3 | `template/TemplateStatus.java` | 新建 | 枚举 DRAFT / ACTIVE / DEPRECATED |
| 4 | `template/PromptTemplateRepository.java` | 新建 | Spring Data JPA Repository 接口 + findByCapabilityIdAndStatus + 预热查询方法 |
| 5 | `template/DatabasePromptTemplateManager.java` | 新建 | PromptTemplateManager 实现：Caffeine 缓存、@PostConstruct 预热、@EventListener 缓存失效、兜底 Prompt |
| 6 | `template/TemplateChangedEvent.java` | 新建 | ApplicationEvent 子类，含 capabilityId/departmentId/promptVersion/changeType/changedAt |
| 7 | `template/PromptTemplateTest.java` | 新建 | PromptTemplate 值对象/JPA 实体契约测试 |
| 8 | `template/DatabasePromptTemplateManagerTest.java` | 新建 | DatabasePromptTemplateManager 单元测试（缓存/预热/事件/兜底/并发） |
| 9 | `orchestrator/AbstractCapabilityExecutor.java` | 修改 | `executeStandardPipeline()` 中 `render(templateKey, variables)` → `render(capabilityId, departmentId, variables, promptVersion)`，移除 `templateKey` 局部变量 |
| 10 | `ai-impl/pom.xml` | 修改 | 添加 spring-boot-starter-data-jpa（compile）+ h2（test scope）依赖 |
| 11 | `pom/AiImplPomCleanDependencyTest.java` | 修改 | totalDependenciesCountShouldBeEleven 断言从 `assertEquals(9, ...)` 改为 `assertEquals(11, ...)`（新增 data-jpa + h2）；同步重命名方法名以匹配断言值 |
| 12 | `orchestrator/AbstractCapabilityExecutorTest.java` | 修改 | 16 处 PromptTemplateManager lambda→Mockito mock + 新增 `import static org.mockito.ArgumentMatchers.any`、`import static org.mockito.Mockito.*` |
| 13 | `orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java` | 修改 | 3 处 PromptTemplateManager lambda→Mockito mock + 新增 import static |
| 14 | `orchestrator/impl/TriageCapabilityExecutorTest.java` | 修改 | 1 处 PromptTemplateManager lambda→Mockito mock + 新增 import static |

**调用方 mock 更新范围（接口签名变更 + 非函数式接口影响）：**

现有 mock 模式（2 参数 lambda）—— 接口新增 `getFallbackPrompt()` 后不再是函数式接口，lambda 不可用：
```java
PromptTemplateManager mockTemplate = (key, vars) -> "rendered";
```

需改为 Mockito mock 模式：
```java
PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
```

对 line 721 返回 null 的情况：
```java
when(mockTemplate.render(any(), any(), any(), any())).thenReturn(null);
```

3 个测试文件需新增静态导入：
```java
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
```

受影响的文件（共 ~20 处 lambda → Mockito mock）：
- `AbstractCapabilityExecutorTest.java`：16 处（lines 430, 451, 512, 538, 587, 641, 693, 721, 765, 815, 861, 904, 963, 1023, 1076, 1132）
- `DiscussionConclusionCapabilityExecutorTest.java`：3 处（lines 260, 295, 380）
- `TriageCapabilityExecutorTest.java`：1 处（line 55）

## 选择理由
Batch4 P2 次序第二项。PromptTemplateManager 是底座管线的模板渲染组件——被全部 7 项底座能力 CapabilityExecutor 的 `doExecuteInternal()` 在管线步骤 3（模板渲染）中直接引用。当前 PromptTemplateManager 为简单存根接口，需升级为完整实现：
- PromptTemplate JPA 实体支持持久化存储和版本管理
- DatabasePromptTemplateManager 提供 Caffeine 缓存、@PostConstruct 预热、@EventListener 事件驱动缓存失效、兜底 Prompt 等生产级特性
- 本任务也是 Batch4 首个引入 JPA 的组件，新增 spring-boot-starter-data-jpa 依赖后为后续 ExperimentManager/HashBucketExperimentManager（Task 13）铺平道路

## 任务上下文

### 设计文档摘录（06_ood_phase5_G.md）

**PromptTemplateManager 接口**（§3.3）：
```
+ render(String capabilityId, String departmentId, Map<String,Object> variables, Integer promptVersion): String
+ getFallbackPrompt(String capabilityId): String
```
- render()：按能力标识、科室标识、模板变量、可选版本号渲染模板。promptVersion=null 时使用当前 ACTIVE 模板
- getFallbackPrompt()：始终返回非 null 非空字符串，各能力通过 ai.template.fallback.{capabilityId} 配置或 DRAFT 状态记录管理

**DatabasePromptTemplateManager**（§3.3）：
- @EventListener 接收 TemplateChangedEvent 清除缓存（capabilityId + departmentId 维度的 Caffeine 条目）
- @PostConstruct 预热：查询全部 ACTIVE 模板预填充 Caffeine 缓存
- 缓存使用 Caffeine：expireAfterWrite=5 分钟 + maximumSize=1000
- 兜底策略：ai.template.fallback.{capabilityId} YAML → 兜底 Prompt；均不存在时返回通用兜底

**PromptTemplate JPA Entity 字段**（§3.3 UML + §7.3）：

| 字段 | Java 类型 | 数据库类型 | 说明 |
|------|----------|-----------|------|
| id | Long | BIGINT (PK, AUTO) | 主键 |
| capabilityId | String | VARCHAR(50) | 能力标识（非空） |
| departmentId | String | VARCHAR(50) | 科室标识（可空，null=全局模板） |
| content | String | TEXT | 模板内容（含 {{key}} 占位符） |
| version | int | INT | 版本号 |
| status | TemplateStatus | VARCHAR(20) | DRAFT / ACTIVE / DEPRECATED |

唯一约束：同一 `(capabilityId, departmentId, version)` 组合唯一。

**状态模型**：
```
DRAFT → ACTIVE → DEPRECATED
- DRAFT: 草稿，不参与渲染
- ACTIVE: 运行时渲染使用，同 capabilityId+departmentId 同时仅一个 ACTIVE
- DEPRECATED: 废弃，历史保留
```

**TemplateChangedEvent**（§3.3）：
```java
class TemplateChangedEvent extends ApplicationEvent {
    String capabilityId;
    String departmentId;      // null=影响全部科室
    Integer promptVersion;    // null=全部版本
    ChangeType changeType;    // CREATED / UPDATED / DELETED / STATUS_CHANGED
    Instant changedAt;
}
```
`ChangeType` 为 `TemplateChangedEvent` 内嵌枚举（或独立枚举文件，由实现者自选），取值：CREATED / UPDATED / DELETED / STATUS_CHANGED。

**PromptTemplateRepository**：
- `findByCapabilityIdAndStatus(String capabilityId, TemplateStatus status)`：按能力+状态查询
- `findByCapabilityIdAndDepartmentIdAndStatus(String capabilityId, String departmentId, TemplateStatus status)`：按能力+科室+状态查询（用于渲染）
- `findByStatus(TemplateStatus status)`：预热查询全部 ACTIVE 模板

## 已有代码上下文

### PromptTemplateManager.java（当前存根，需修改）
```java
public interface PromptTemplateManager {
    String render(String templateKey, Map<String, Object> variables);
}
```

### AbstractCapabilityExecutor.java 中引用位置
```java
protected final PromptTemplateManager promptTemplateManager;
```
在 executeStandardPipeline() 管线步骤 3 中调用 `promptTemplateManager.render(templateKey, variables)`，当前为 2 参数调用，需配合新签名更新。**本任务需同步修改** `executeStandardPipeline()` 中该调用为 4 参数，并移除 `templateKey` 局部变量。

### 测试文件中 PromptTemplateManager mock（共 ~20 处需更新）
当前：`PromptTemplateManager mockTemplate = (key, vars) -> "rendered"`（lambda，接口仅 1 个抽象方法）
改为：Mockito mock 模式（接口新增 getFallbackPrompt() 后非函数式接口，lambda 不可用）
```java
PromptTemplateManager mockTemplate = mock(PromptTemplateManager.class);
when(mockTemplate.render(any(), any(), any(), any())).thenReturn("rendered");
when(mockTemplate.getFallbackPrompt(any())).thenReturn("fallback");
```

### ai-impl pom.xml 当前依赖（不含 JPA）
需新增：
- `spring-boot-starter-data-jpa`（scope compile）
- `com.h2database:h2`（scope test，版本由父 POM `<h2.version>2.2.224</h2.version>` 管理）

## 实施要点

1. **PromptTemplate JPA 实体**：使用 `@Entity` + `@Table(uniqueConstraints = ...)` + `@Id` + `@GeneratedValue`，字段直接注解。departmentId 可为 null 表示全局模板
2. **TemplateStatus 枚举**：独立文件，非内嵌
3. **DatabasePromptTemplateManager 缓存设计**：
   - Cache Key: `String` 格式为 `capabilityId + ":" + (departmentId ?? "")`
   - Cache Value: `PromptTemplate`（当前 ACTIVE）
   - `@PostConstruct warmup()`：调用 `repository.findByStatus(ACTIVE)`，逐条填充缓存
   - `@EventListener`：接收 TemplateChangedEvent，清除对应 key
   - render() 流程：缓存查询 → 命中直接返回 → 未命中 DB 查询 → 填充缓存 → 占位符替换
   - getFallbackPrompt()：先查 `environment.getProperty("ai.template.fallback." + capabilityId)` → 未配置返回通用兜底
4. **render() 中占位符替换**：遍历 variables.entrySet()，对 content 执行 `replace("{{" + key + "}}", value)`，value 为 null 时保留占位符并 WARN 日志
5. **TemplateChangedEvent**：继承 `ApplicationEvent`，payload 包含全部字段，使用 `@Getter` 或手动 getter
6. **AbstractCapabilityExecutor.executeStandardPipeline() 修改**：将 line 341 `String templateKey = capabilityId + ":" + promptVersion;` 移除，line 344 `promptTemplateManager.render(templateKey, variables)` 改为 `promptTemplateManager.render(capabilityId, departmentId, variables, promptVersion)`（`departmentId` 已是方法参数，可直接使用）
7. **测试 mock 改造**：3 个测试文件新增 `import static org.mockito.ArgumentMatchers.any;` 和 `import static org.mockito.Mockito.mock;` + `import static org.mockito.Mockito.when;`；所有 `PromptTemplateManager mockTemplate = (key, vars) -> ...` lambda 替换为 `mock(PromptTemplateManager.class)` + `when(...).thenReturn(...)` 模式
8. **测试覆盖**：PromptTemplate 值对象契约（构造器/getter/equals/唯一约束）；DatabasePromptTemplateManager 覆盖 8 条路径（缓存命中/未命中/预热/事件失效/兜底配置/通用兜底/并发安全/null variable value）
9. **H2 测试依赖**：在 ai-impl/pom.xml 的 test scope 添加 `com.h2database:h2`（无 version，由父 POM `dependencyManagement` 管理），确保 @DataJpaTest 可启动嵌入式数据库
10. **AiImplPomCleanDependencyTest 同步**：方法名从 `totalDependenciesCountShouldBeEight` 重命名为 `totalDependenciesCountShouldBeEleven`，方法体内 `assertEquals(9, ...)` 改为 `assertEquals(11, ...)`（新增 data-jpa + h2 后总依赖数从 9→11）
11. **路线表 row 12 涉及文件列需包含**：TemplateStatus.java、TemplateChangedEvent.java、PromptTemplateTest.java、DatabasePromptTemplateManagerTest.java、ai-impl/pom.xml、AiImplPomCleanDependencyTest.java

## 修订说明（v17 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] AbstractCapabilityExecutor 生产代码调用未更新，导致编译失败 | 将 `AbstractCapabilityExecutor.java` 纳入涉及文件表（row 10），在 `executeStandardPipeline()` 中将 `render(templateKey, variables)` 改为 `render(capabilityId, departmentId, variables, promptVersion)` 并移除 `templateKey` 局部变量；同步更新"已有代码上下文"说明 |
| [严重] 接口新增 getFallbackPrompt() 后无法使用 Lambda 模拟 | 将 3 个测试文件中 ~20 处 lambda 替换为 Mockito `mock()` + `when().thenReturn()` 模式；补充 `import static org.mockito.*`；分别 stub `render()` 和 `getFallbackPrompt()` |

## 修订说明（v17 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] AiImplPomCleanDependencyTest 断言未同步更新 | `AiImplPomCleanDependencyTest.java:63` 断言从 `assertEquals(9, ...)` 改为 `assertEquals(11, ...)`（新增 spring-boot-starter-data-jpa + h2 后总依赖数 9→11）；将 pom/ 测试文件纳入涉及文件表 row 11 |
| [一般] 路线表 row 12 涉及文件列不完整 | 路线表 row 12 涉及文件列补充 TemplateStatus.java、TemplateChangedEvent.java、PromptTemplateTest.java、DatabasePromptTemplateManagerTest.java、ai-impl/pom.xml、AiImplPomCleanDependencyTest.java；实施要点第 11 条同步更新 |
| [轻微] 未考虑 H2 测试依赖 | 在 ai-impl/pom.xml test scope 添加 `com.h2database:h2`（版本由父 POM `<h2.version>2.2.224</h2.version>` 管理）；实施要点第 9 条补充说明；预期文件表 row 10 补充 h2 |

## 修订说明（v17 r3）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] 预期文件清单表缺失 3 个需修改的测试文件（AbstractCapabilityExecutorTest.java、DiscussionConclusionCapabilityExecutorTest.java、TriageCapabilityExecutorTest.java） | 在预期文件清单表新增 row 12-14，补充 3 个测试文件及其修改范围（共 ~20 处 lambda→Mockito mock）；同步更新实施要点第 7、11 条 |
| [轻微] TemplateChangedEvent 中 ChangeType 枚举未说明来源 | 在 TemplateChangedEvent 代码段后补充说明：ChangeType 为内嵌枚举（或独立文件），含 CREATED/UPDATED/DELETED/STATUS_CHANGED 四个常量 |
| [轻微] AiImplPomCleanDependencyTest 方法名 `totalDependenciesCountShouldBeEight` 与断言值 11 不一致 | row 11 和实施要点第 10 条同步更新：方法名改为 `totalDependenciesCountShouldBeEleven`，消除命名不一致 |
