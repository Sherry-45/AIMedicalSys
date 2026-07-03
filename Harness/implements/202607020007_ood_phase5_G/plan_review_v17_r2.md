# 计划审查报告（v17 r2）

## 审查结果
REJECTED

## 发现

### [严重] AiImplPomCleanDependencyTest 断言未同步更新
计划在 ai-impl/pom.xml 添加 `spring-boot-starter-data-jpa` 依赖，但未同步更新 `AiImplPomCleanDependencyTest.totalDependenciesCountShouldBeEight()` 的断言值。当前该测试断言 `assertEquals(9, ...)`（`AiImplPomCleanDependencyTest.java:63`），新增依赖后总计数将变为 10，导致该测试在验证阶段失败。

这是本项目 Task 8（Guava + Caffeine）和 Task 9（reactor-core）都已出现的固定模式——每次 pom.xml 新增外部依赖，都需要同步更新该测试的硬编码断言值。

### [一般] 路线表 row 12 涉及文件列不完整
路线表 row 12 的"涉及文件"列仅列出 8 个文件，遗漏了本任务确需创建/修改的 5 个文件：
- `TemplateStatus.java`（新建枚举）
- `TemplateChangedEvent.java`（新建事件类）
- `PromptTemplateTest.java`（新建测试）
- `DatabasePromptTemplateManagerTest.java`（新建测试）
- `ai-impl/pom.xml`（修改依赖）

虽然计划详细描述段落在文字中涵盖了这些文件，但路线表作为实施过程中的进度跟踪摘要，不完整可能导致阶段遗漏。

### [轻微] 未考虑 H2 测试依赖
计划未在 ai-impl/pom.xml 中添加 h2 作为 test 依赖。父 POM 已管理 h2 版本（`backend/pom.xml:41 — 2.2.224`），大多数含 JPA 实体测试的模块也声明了 h2。当前 PromptTemplateTest 和 DatabasePromptTemplateManagerTest 的具体测试策略（是否使用 @DataJpaTest）不明确，但添加 h2 测试依赖是低风险的安全做法，符合项目惯例。

## 修改要求

1. **AiImplPomCleanDependencyTest 断言更新**: 将 `AiImplPomCleanDependencyTest.java:63` 的 `assertEquals(9, ...)` 改为 `assertEquals(10, ...)`；将该文件纳入涉及文件清单。
2. **路线表补全**: 在 row 12 涉及文件列补充 `TemplateStatus.java`、`TemplateChangedEvent.java`、`PromptTemplateTest.java`、`DatabasePromptTemplateManagerTest.java`、`ai-impl/pom.xml`。
3. **H2 依赖**: 在 ai-impl/pom.xml test scope 中添加 `com.h2database:h2` 依赖声明，防止 JPA 测试因缺少嵌入式数据库而失败。
