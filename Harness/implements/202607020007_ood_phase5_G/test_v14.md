# 测试报告（v14）

## 测试文件清单

| 文件路径 | 测试方法数 | 覆盖维度 |
|---------|-----------|---------|
| `dto/base/Phase4ServiceMetaTest.java` | 9 | 正常构造 · 空字段 · Jackson序列化 · equals相等性 · 各字段不等性 · 类型不等性 |
| `dto/base/Phase4ServiceMetaCapableTest.java` | 1 | 匿名实现类 getServiceMeta() 返回 |
| `dto/base/Phase4BusinessExceptionTest.java` | 3 | message构造 · message+cause构造 · instanceof继承链 |
| `dto/base/CallContextTest.java` | 6 | 全字段构造 · 不可变性 · withOutputSummary · withPromptVersion · 可空字段 · Jackson序列化 |

总计 19 个测试方法。

## 偏差说明

| 设计规格 | 偏差原因 | 实际处理 |
|---------|---------|---------|
| Jackson 测试使用 `Jackson2ObjectMapperBuilder` + `JacksonConfig.customizer()` | `Jackson2ObjectMapperBuilder` 位于 spring-web，ai-api test scope 未包含该依赖 | 改用 `new ObjectMapper()` + `PropertyNamingStrategies.SNAKE_CASE`，等效模拟生产 SNAKE_CASE 命名策略 |
| Phase4ServiceMetaTest 未覆盖 equals/hashCode 显式测试 | 设计行为契约包含 equals/hashCode，且文件规划描述注明"equals-hashCode" | 新增 5 个显式契约测试：相等性 · 各字段不等性(3) · 类型不等性 |
