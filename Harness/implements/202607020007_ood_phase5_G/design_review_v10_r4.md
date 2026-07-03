# 设计审查报告（v10 r4）

## 审查结果
REJECTED

## 发现

### **[一般] EndpointRateLimiter 动态配置机制存在矛盾**

设计第 170 行和第 176 行同时提到两套不同的配置读取方式：`@Value` 静态注入属性值（前缀 `ai.rate-limiting.endpoints.{endpointId}`）和"懒加载：首次调用时从环境属性读取"。问题在于：

- `@Value` 注解在 Spring bean 创建时完成注入，无法处理动态的 endpointId（endpointId 在运行时才确定，不是编译时已知的固定属性键名）。
- "懒加载从环境属性读取"暗示使用 `Environment.getProperty()` 运行时查询，但这与 `@Value` 方案矛盾。

期望的修正方向：删除 `@Value` 描述，明确使用 `Environment` 注入（`org.springframework.core.env.Environment`），在 `tryAcquire()` 首次调用时通过 `environment.getProperty("ai.rate-limiting.endpoints." + endpointId + ".permits-per-second", Double.class)` 读取各端点的限流配置。

### **[一般] Caffeine Expiry 接口时间单位未明确**

设计第 135-137 行描述了 Expiry 接口的 TTL 行为，使用"5 分钟""30 秒""60 秒""10 分钟"等自然时间单位。但 Caffeine 的 `Expiry` 接口三个方法（`expireAfterCreate` / `expireAfterUpdate` / `expireAfterRead`）**返回值单位为纳秒 (nanoseconds)**，而非毫秒或秒。

若实现者误以为返回毫秒（如返回 `300_000` 代表 5 分钟），实际过期时间为 300 微秒，与预期差 **1,000,000 倍**，导致缓存几乎立即过期。

期望的修正方向：在缓存策略小节中显式注明 Expiry 方法返回值格式为纳秒，并给出示例换算值（如"5 分钟 = 5 × 60 × 1_000_000_000L ns"）。

### **[轻微] CredentialProviderState 枚举放置策略可进一步明确**

设计第 75 行描述 `CredentialProviderState` "与 CredentialProvider 同文件/独立文件均可，设计建议放在同文件"。但 Java 规范限制一个 `.java` 文件最多只能有一个 `public` 顶层类型。若将 `CredentialProviderState` 放在 `CredentialProvider.java` 中，则必须声明为 package-private（无 public 修饰），这与任务代码片段一致。建议明确指定为 package-private 顶层枚举置于同一文件，消除选择空间，避免实现不一致。

### **[轻微] backoffUntil 时间基准同一性可显式说明**

设计第 132 行注明 `backoffUntil` 使用 `System.nanoTime()` 基准，但第 140 行状态机描述中仅说"当前时间 < backoffUntil"，未显式说明"当前时间"也应使用 `System.nanoTime()` 获取。若实现者误用 `System.currentTimeMillis()` 与 `System.nanoTime()` 进行比较，将导致 `backoffUntil` 窗口完全失效（不同时钟源，量级和基准不一致）。建议显式说明比较时统一使用 `System.nanoTime()`。

## 修改要求（REJECTED）

| 严重程度 | 问题 | 修正方向 |
|---------|------|---------|
| 一般 | EndpointRateLimiter 配置机制矛盾 | 删除 @Value 描述，统一采用 Environment 运行时查询 |
| 一般 | Caffeine Expiry 时间单位未明确纳秒 | 显式标注 Expiry 返回值单位为纳秒，附换算示例 |
| 轻微 | CredentialProviderState 放置策略模糊 | 明确指定为 package-private 顶层枚举置于同一文件 |
| 轻微 | backoffUntil 时钟源同一性未显式声明 | 显式说明当前时间使用 System.nanoTime() 获取 |
