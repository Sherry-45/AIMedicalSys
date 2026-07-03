# 代码审查报告（v7 r1）

## 审查结果
REJECTED

## 发现

- **[严重]** `ChatToolDefinition.java:22` — `@JsonProperty("strict")` 在构造器参数上，导致 Jackson 使用参数化构造器反序列化时，JSON 缺失 `strict` 字段会得到 `false` 而非设计要求的默认值 `true`。v7 的修订明确将 `strict` 改为非 final + 初始化值 `true` 以解决此问题，但 `@JsonProperty("strict")` 的存在使 Jackson 仍通过构造器注入该参数，绕过了字段初始化值。默认 Jackson 2.x 配置下 `INFER_CREATOR_FROM_CONSTRUCTOR_PROPERTIES` 为 true，参数化构造器会被推断为 creator。

- **[严重]** `ChatToolDefinition.java:19-27` — 全参构造器第 4 个参数 `@JsonProperty("strict") boolean strict` 的 JSON 绑定与设计规格冲突。设计明确要求 "Jackson：无参构造器 + 字段级 @JsonProperty（仅 name、description、parameters 三个 final 字段）+ strict 字段通过初始化值 true 保证默认"，实现为 strict 添加了构造器参数级 @JsonProperty，与设计背道而驰。

## 修改要求

### 问题 1：ChatToolDefinition.strict 默认值失效

**位置**：`ChatToolDefinition.java:22`（构造器参数标注）

**问题**：`@JsonProperty("strict")` 标注在构造器参数上，使 Jackson 将其视为构造器注入的属性。当 JSON 输入不包含 `strict` 时，boolean 参数获得 JVM 默认值 `false`，而非预期的 `true`。

**为什么是问题**：设计 v7 r1 的修订历史第 1 条就是专门修复此问题——将 `strict` 改为非 final + 初始化 `true`，期望 Jackson 通过无参构造器 + 字段初始化保留默认值。当前实现仍然绑定了 `strict` 到构造器参数，导致修复无效。

**期望的修正方向**：移除 `strict` 的 `@JsonProperty` 构造器参数绑定。有两种可行方案：
- 方案 A：从全参构造器中移除 `strict` 参数，仅保留 setter。Jackson 使用无参构造器创建对象（`strict = true`），再通过 setter 设置 JSON 中存在的值。
- 方案 B：改用字段级 `@JsonProperty`（标注在 `name`、`description`、`parameters` 字段上），构造器不标注 `@JsonProperty`，Jackson 使用无参构造器 + 字段反射注入 final 字段 + setter 设置 `strict`。

推荐方案 A，改动最小且与 "strict 通过初始化值 true 保证默认" 的设计意图最吻合。
