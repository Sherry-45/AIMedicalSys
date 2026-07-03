# 设计审查报告（v16 r1）

## 审查结果
REJECTED

## 发现

- **[严重]** `DefaultModelRouter.java` 中 import 语句引用 `com.aimedical.modules.ai.impl.config.AiRouterProperties`（L121），但 `AiRouterProperties` 设计在 `com.aimedical.modules.ai.impl.router` 包下（L190），且文件规划中 `AiRouterProperties.java` 位于 `router/` 目录。包路径不一致将导致编译错误。

- **[一般]** `@ConfigurationProperties` 与 `ModelRoute` 不可变特性不兼容：`AiRouterProperties.routes` 类型为 `Map<String, List<ModelRoute>>`，但 `ModelRoute` 仅有全参构造器 + `of()` 工厂方法，没有无参构造器和 setter。Spring Boot 无法通过 `@ConfigurationProperties` 的 setter 绑定机制将 YAML 自动反序列化为 `ModelRoute` 实例。此限制虽在 Task 18 中被吸收替换，但设计应明确标注该限制（建议添加 `@TODO` 注释），避免后期运维时排查困难。

- **[轻微]** `ModelRoute` 构造器未说明 `parameters` 参数的防御性拷贝策略。`getParameters()` 被设计为返回 `unmodifiableMap`，但如果构造器不拷贝原始 Map，持有外部引用的调用方仍可修改内部状态。建议在构造器设计中明确 `this.parameters = new HashMap<>(parameters)`。

- **[轻微]** `@EventListener(RouteConfigChangedEvent.class)` 引用了 `RouteConfigChangedEvent` 类型，但设计未定义该类型也未将其列入依赖。若方法体/注解处于注释状态则无影响，但设计应注明该事件类将在后续任务引入。

## 修改要求（仅 REJECTED 时）

### [严重] AiRouterProperties 包路径不一致
- **问题**：DefaultModelRouter L121 import 路径为 `config.AiRouterProperties`，但 AiRouterProperties 设计在 `router` 包
- **期望修正**：将 `DefaultModelRouter.java` 中的 import 改为 `import com.aimedical.modules.ai.impl.router.AiRouterProperties;`（因同包可省略 import），确保编译通过

### [一般] @ConfigurationProperties 与 ModelRoute 绑定不兼容
- **问题**：`Map<String, List<ModelRoute>>` 无法通过 Spring Boot setter 绑定自动反序列化，因 ModelRoute 无无参构造器 + setter
- **期望修正**：在 `AiRouterProperties.java` 中添加 `@TODO Phase5:` 注释说明该存根的绑定限制，注明后续由 AiPlatformConfig（Task 18）替换；或为 `ModelRoute` 增加无参构造器 + setter（但会破坏不可变性，不推荐）
