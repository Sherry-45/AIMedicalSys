# 实现报告（v16）

## 概述
实现了 ModelRouter 路由系统 3 个类型（ModelRouter 接口修改、ModelRoute 值对象、DefaultModelRouter @Service 实现、AiRouterProperties 存根），同步修改 AbstractCapabilityExecutor 适配 ModelRoute 返回类型，并更新 3 个测试文件的 mock lambda 及新增 2 个测试文件。

## 文件变更清单
| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `ai-impl/.../router/ModelRouter.java` | 返回类型 `Object` → `ModelRoute` |
| 新建 | `ai-impl/.../router/ModelRoute.java` | 不可变值对象，含全参构造器、`of()` 工厂、防御性拷贝、equals/hashCode |
| 新建 | `ai-impl/.../router/DefaultModelRouter.java` | @Service 实现，AtomicReference 路由表，@PostConstruct + @Scheduled 热刷新，权重随机选择 |
| 新建 | `ai-impl/.../router/AiRouterProperties.java` | @ConfigurationProperties(prefix="ai.router") 存根 |
| 修改 | `ai-impl/.../orchestrator/AbstractCapabilityExecutor.java` | routeResult 类型切换 + 3 处字段访问适配 + clientType 动态选择 |
| 修改 | `ai-impl/.../orchestrator/AbstractCapabilityExecutorTest.java` | 15 处 lambda mock 适配 + 新增 ModelRoute import |
| 修改 | `ai-impl/.../orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java` | 3 处 lambda mock 适配 + 新增 ModelRoute import |
| 修改 | `ai-impl/.../orchestrator/impl/TriageCapabilityExecutorTest.java` | 1 处 lambda mock 适配 + 新增 ModelRoute import |
| 新建 | `ai-impl/.../router/ModelRouteTest.java` | ModelRoute 全字段/工厂/防御性拷贝/不可变/equals/hashCode/null 参数 6 个测试 |
| 新建 | `ai-impl/.../router/DefaultModelRouterTest.java` | 7 个测试：未知能力/null/权重选择/零权重/并发/初始化/空配置 |

## 编译验证
- `mvn compile -pl ai-impl -am` — 编译通过
- `mvn test-compile -pl ai-impl -am` — 测试编译通过

## 设计偏差说明
无偏差。所有实现严格遵循详细设计 v16 的接口签名、类型定义、行为契约和错误处理规范。

## 修订说明（v16 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] `shouldBeThreadSafe` 线程依次串行启动并 join，无并发访问 | 改为先启动所有 50 个线程再统一 join，构造真实并发场景 |
| [轻微] `shouldSelectRandomlyWhenAllWeightsZero` 使用同一 route 引用两次，无法验证等概率 | 拆为 `routeZeroA`/`routeZeroB` 两个不同实例（不同 endpointId/modelId），200 次选取后断言两者均至少选中 1 次 |
