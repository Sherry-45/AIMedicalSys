# 实现报告（v17）

## 概述
实现了 Prompt 模板管理层：TemplateStatus 枚举、PromptTemplate JPA 实体、PromptTemplateRepository、TemplateChangedEvent 事件、DatabasePromptTemplateManager 实现类；修改 PromptTemplateManager 接口签名及所有调用方 mock；更新 pom.xml 依赖与断言测试。

## 文件变更清单
| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新建 | `src/main/java/com/aimedical/modules/ai/impl/template/TemplateStatus.java` | 枚举 DRAFT / ACTIVE / DEPRECATED |
| 新建 | `src/main/java/com/aimedical/modules/ai/impl/template/PromptTemplate.java` | JPA @Entity，含字段、唯一约束、equals/hashCode |
| 新建 | `src/main/java/com/aimedical/modules/ai/impl/template/PromptTemplateRepository.java` | Spring Data JPA Repository |
| 新建 | `src/main/java/com/aimedical/modules/ai/impl/template/TemplateChangedEvent.java` | ApplicationEvent 子类，含内嵌 ChangeType |
| 新建 | `src/main/java/com/aimedical/modules/ai/impl/template/DatabasePromptTemplateManager.java` | @Service 实现类，含 Caffeine 缓存、warmup、事件监听 |
| 新建 | `src/test/java/com/aimedical/modules/ai/impl/template/PromptTemplateTestConfig.java` | @SpringBootApplication 供 @DataJpaTest 发现 |
| 新建 | `src/test/java/com/aimedical/modules/ai/impl/template/PromptTemplateTest.java` | 实体契约测试（构造/getter/equals/唯一约束/nullable/枚举持久化） |
| 新建 | `src/test/java/com/aimedical/modules/ai/impl/template/DatabasePromptTemplateManagerTest.java` | 缓存/预热/事件/兜底/并发/null 占位符 + 5 条 promptVersion 分支测试 |
| 修改 | `src/main/java/com/aimedical/modules/ai/impl/template/PromptTemplateManager.java` | 接口签名从 `render(String, Map)` 改为 `render(String, String, Map, String)` + 新增 `getFallbackPrompt(String)` |
| 修改 | `src/main/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutor.java` | `executeStandardPipeline()` 中 render 调用改为 4 参数，移除 templateKey |
| 修改 | `pom.xml` | 新增 spring-boot-starter-data-jpa + h2(test) |
| 修改 | `src/test/java/com/aimedical/modules/ai/impl/pom/AiImplPomCleanDependencyTest.java` | 方法名改为 totalDependenciesCountShouldBeEleven，断言 9→11 |
| 修改 | `src/test/java/com/aimedical/modules/ai/impl/orchestrator/AbstractCapabilityExecutorTest.java` | 16 处 lambda→Mockito mock |
| 修改 | `src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java` | 3 处 lambda→Mockito mock |
| 修改 | `src/test/java/com/aimedical/modules/ai/impl/orchestrator/impl/TriageCapabilityExecutorTest.java` | 1 处 lambda→Mockito mock |

## 编译验证
mvn compile + mvn test 通过，BUILD SUCCESS，393 tests 全部通过。

## 设计偏差说明
- 设计要求 `@DataJpaTest + @AutoConfigureTestDatabase(replace = NONE) 或使用 h2`：因 ai-impl 为库模块无 @SpringBootConfiguration，需额外新建 `PromptTemplateTestConfig.java` 作为测试配置入口，@DataJpaTest 可自动发现并使用嵌入式 H2。非功能偏差，属测试基础设施必要补充。
- 其余无偏差。
