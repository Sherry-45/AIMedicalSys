# 设计审查报告（v10 r1）

## 审查结果
REJECTED

## 发现

### **[严重]** BACKOFF 状态机行为自相矛盾

`detail_v10.md` 内部存在一处直接矛盾：

- **状态转换表 §3.2（行 108）**：`BACKOFF | 窗口期内再次调用 | BACKOFF（仅返回缓存）`——声称从缓存返回凭据
- **行为契约 §行为契约（行 159）**：`BACKOFF 状态下若当前时间 < backoffUntil 直接返回 Optional.empty()`

而任务指令（行 71）明确定义 BACKOFF 行为为 `直接返回 Optional.empty()`。状态表表述错误将导致实现者写出错误的 getCredential() 逻辑（返回缓存值而非 empty），这是一个编译可通过但逻辑错误的严重缺陷。

**修正方向**：状态表 BACKOFF 窗口期内目标行为修正为 `BACKOFF（返回 Optional.empty()）`，并确保行为契约、状态表、任务描述三者一致。

### **[一般]** `Credential` 值对象不可变性未通过类型系统保障

设计声明 `Credential` 为"不可变"（行 53），但类图中字段缺少 `private final`（行 46-51），且未显式声明 getter 方法。这种情况下，同包代码可以直接修改字段值，破坏不可变性契约。已有 `StructuredOutputNotSupportedException` 等类型均采用标准 Java 惯用法。

**修正方向**：将字段声明为 `private final`，并提供公开 getter 方法（匹配任务要求的"全参构造器 + getters"）。

### **[一般]** `EndpointRateLimiter` 缺少 `@Value` 配置字段声明

设计（行 133）声明配置来源为 `@Value` 注入属性 `ai.rate-limiting.endpoints.{endpointId}.{permits-per-second / max-burst-seconds / queue-wait-millis}`，但类图（行 118-125）中没有任何对应字段（如 `permitsPerSecond`、`maxBurstSeconds`、`queueWaitMillis`），构造器标注为"无参构造器"。这会掩盖 `@Value` 如何在无参构造器 + `@Component` 场景下完成注入的实现细节。

**修正方向**：在类图中补充 `@Value` 注解的配置字段，明确字段注入方式（或构造器参数注入），并说明首次创建 `RateLimiter` 时如何使用这些配置值。

### **[一般]** `CACHE_ONLY` 状态下缓存未命中的兜底行为未定义

状态表（行 101-105）在 CACHE_ONLY 状态下跳过 `credentialStore` 查询，但未明确当 Caffeine 缓存未命中指定 `endpointId` 时应当返回什么。任务指令（行 72）说"仅从 Caffeine 缓存返回"，但缓存未命中时是否返回 `Optional.empty()`？实现者需要在这个决策点上做出猜测。

**修正方向**：在 CACHE_ONLY 状态的行为描述中补充"缓存未命中则返回 `Optional.empty()`"的明确声明。

### **[轻微]** 非 OAUTH2 凭据类型缺少默认 TTL

缓存策略（行 93）只定义了 OAUTH2 凭据的动态 TTL（expiresAt - 60s，最长 5 分钟），但 `AuthType.API_KEY` 和 `AuthType.NONE` 类型没有 `expiresAt` 语义，未指定默认缓存 TTL。实现者需要猜测合理的默认值（例如 5 分钟上限与 OAUTH2 保持一致）。

**建议**：在 `expireAfterCreate` 描述中补充非 OAUTH2 类型的默认 TTL 值（如"最长 5 分钟"作为统一的兜底值）。

### **[轻微]** `registerCredential()` 的缓存交互未说明

设计（行 81）声明了 `registerCredential(String endpointId, Credential credential)` 方法，但未定义此操作是否同时更新 Caffeine 缓存。若不主动刷新缓存，后续 `getCredential()` 可能读到过期条目直至原 TTL 到期。

**建议**：明确 `registerCredential()` 是否调用 `cache.put(endpointId, credential)` 以同步刷新缓存条目，或在注册后使旧缓存失效。

## 修改要求

1. **BACKOFF 状态表修正**（严重）：行 108"仅返回缓存"改为"返回 Optional.empty()"，与任务指令和行为契约保持一致。同时检查行 105 `CACHE_ONLY` 状态在条件"连续失败 >= 5"下应优先使用缓存值而非"仅返回缓存"的模糊表述——实际上该状态下应尝试从缓存返回（而非返回 empty）。
2. **Credential 字段访问控制**（一般）：`Credential` 类所有字段添加 `private final` 修饰符，补充公开 getter 方法。
3. **EndpointRateLimiter 配置声明**（一般）：在类图中补充 `@Value` 注入的配置字段（permitsPerSecond、maxBurstSeconds 等），并说明构造时如何从配置构建 `RateLimiter`。
4. **CACHE_ONLY 缓存未命中**（一般）：在 CACHE_ONLY 行为描述中补充"缓存未命中时返回 `Optional.empty()`"。
5. **非 OAUTH2 默认 TTL**（轻微）：补充 API_KEY/NONE 类型凭据的默认缓存过期策略。
6. **registerCredential 缓存同步**（轻微）：明确 `registerCredential()` 是否主动刷新缓存。
