# 实现报告（v14）

## 概述
在 `ai-api/dto/base/` 包新增 4 个类型（Phase4ServiceMeta、Phase4ServiceMetaCapable、Phase4BusinessException、CallContext）及对应的 4 个测试类。

## 文件变更清单
| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新建 | `dto/base/Phase4ServiceMeta.java` | 元数据值对象，含 modelId/promptVersion/retryCount 三字段 |
| 新建 | `dto/base/Phase4ServiceMetaCapable.java` | 可选接口，定义 `getServiceMeta()` 方法 |
| 新建 | `dto/base/Phase4BusinessException.java` | 抽象异常基类，extends RuntimeException |
| 新建 | `dto/base/CallContext.java` | 不可变业务上下文值对象，聚合 9 个业务上下文字段 |
| 新建 | `dto/base/Phase4ServiceMetaTest.java` | 测试构造/序列化/equals-hashCode |
| 新建 | `dto/base/Phase4ServiceMetaCapableTest.java` | 测试匿名实现类的 getServiceMeta() 返回 |
| 新建 | `dto/base/Phase4BusinessExceptionTest.java` | 测试异常构造/继承链/message |
| 新建 | `dto/base/CallContextTest.java` | 测试构造/不可变性/withOutputSummary/withPromptVersion/序列化 |
| 修改 | `pom.xml` | 新增 jackson-databind 测试依赖 |

## 编译验证
通过。`mvn test -Dtest="Phase4ServiceMetaTest,Phase4ServiceMetaCapableTest,Phase4BusinessExceptionTest,CallContextTest"` 共计 13 个测试全部通过。

## 设计偏差说明
| 设计规格 | 偏差原因 | 实际处理 |
|---------|---------|---------|
| Jackson 测试使用 `Jackson2ObjectMapperBuilder` + `JacksonConfig.customizer()` | `Jackson2ObjectMapperBuilder` 位于 spring-web 中，而 ai-api 模块的 test 作用域未包含该依赖；添加 spring-web 会引入大量无关依赖。设计未声明该依赖，按「不自行决策」原则不修改 ai-api 的依赖范围（仅 jackson-databind 除外） | 测试中改用 `new ObjectMapper()` + `PropertyNamingStrategies.SNAKE_CASE`，等效模拟生产环境的 SNAKE_CASE 命名策略 |
| 未声明 `jackson-databind` 测试依赖 | ai-api 未传递依赖 jackson-databind，但测试代码（ObjectMapper、PropertyNamingStrategies）需要该包 | 在 pom.xml 中新增 `<artifactId>jackson-databind</artifactId><scope>test</scope>` |
