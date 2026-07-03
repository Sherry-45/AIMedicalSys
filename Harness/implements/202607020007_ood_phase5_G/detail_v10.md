# 详细设计（v10）

## 概述

为 `ai-impl/client/` 包新增 4 个类型——`CredentialProvider` 接口（含内嵌 `Credential` 值对象 + `CredentialProviderState` 枚举）、`DefaultCredentialProvider` 默认内存实现、`EndpointRateLimiter` 端点限流器、`LlmInfrastructureException` 基础设施异常——为 LLM 调用层提供凭据查询和限流基础设施。同步修改 `ai-impl/pom.xml` 添加 Caffeine 和 Guava 依赖。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/CredentialProvider.java` | 新建 | 凭据查询接口，含内嵌值对象 Credential 和状态枚举 CredentialProviderState |
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/DefaultCredentialProvider.java` | 新建 | CredentialProvider 接口的默认内存实现，基于 ConcurrentHashMap + Caffeine 缓存 + 完整状态机 |
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/EndpointRateLimiter.java` | 新建 | 端点限流器，基于 Guava RateLimiter 令牌桶算法 |
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/exception/LlmInfrastructureException.java` | 新建 | LLM 基础设施异常，extends RuntimeException |
| `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/exception/CredentialUnavailableException.java` | 新建 | 凭据不可用异常，NORMAL 状态下 Vault 超时且缓存无数据时抛出 |
| `AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/client/CredentialProviderTest.java` | 新建 | CredentialProvider 接口契约测试 |
| `AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/client/DefaultCredentialProviderTest.java` | 新建 | DefaultCredentialProvider 状态机、缓存、凭据管理测试 |
| `AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/client/EndpointRateLimiterTest.java` | 新建 | EndpointRateLimiter tryAcquire 成功/超时测试 |
| `AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/client/exception/LlmInfrastructureExceptionTest.java` | 新建 | LlmInfrastructureException 构造测试 |
| `AIMedical/backend/modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/client/exception/CredentialUnavailableExceptionTest.java` | 新建 | CredentialUnavailableException 构造测试 |
| `AIMedical/backend/modules/ai/ai-impl/pom.xml` | 修改 | 添加 caffeine + guava 依赖 |

## 类型定义

### `CredentialProvider`（接口）

**形态**：interface
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：凭据查询接口，按 endpointId 从 Vault/配置中心查询认证凭据

```java
public interface CredentialProvider {
    Optional<Credential> getCredential(String endpointId);
    CredentialProviderState getState();
    boolean isCredentialValid(String endpointId);
    boolean probeVault();
}
```

**公开接口**：
- `Optional<Credential> getCredential(String endpointId)` — 根据端点标识查询认证凭据，endpointId 不存在时返回 empty
- `CredentialProviderState getState()` — 返回 Vault 连接当前状态
- `boolean isCredentialValid(String endpointId)` — 验证指定端点凭据的有效性
- `boolean probeVault()` — 触发一次主动探测，强制查询 Vault 以尝试恢复 NORMAL 状态

**内嵌类型**：

#### `CredentialProvider.Credential`（值对象）

**形态**：public static class
**包路径**：`com.aimedical.modules.ai.impl.client.CredentialProvider`
**职责**：凭据值对象，封装认证方式与凭证值

```java
public static class Credential {
    private final AuthType authType;
    private final String credentialValue;
    private final Instant expiresAt;
    private final String vaultPath;

    public Credential(AuthType authType, String credentialValue, Instant expiresAt, String vaultPath);
    // getters
}
```

- 全参构造器，所有字段 final
- authType: API_KEY / OAUTH2 / NONE
- credentialValue: API Key 明文 / OAuth2 Access Token / null (NONE 时)
- expiresAt: 凭证过期时间（OAUTH2 场景），可为 null
- vaultPath: Vault 中的凭据路径，用于审计追踪，可为 null

**构造方式**：`new CredentialProvider.Credential(AuthType.API_KEY, "sk-xxx", null, "/v1/secret/ai/ep1")`

#### `CredentialProviderState`（枚举）

**形态**：package-private 顶层 enum（置于 `CredentialProvider.java` 同文件中，无 `public` 修饰）
**包路径**：`com.aimedical.modules.ai.impl.client`（定义于 `CredentialProvider.java` 中）
**职责**：凭据提供者状态枚举

```java
enum CredentialProviderState {
    NORMAL,     // Vault 连接正常，缓存 TTL=5 分钟
    CACHE_ONLY, // Vault 不可达，使用缓存数据，TTL 延长 30 秒
    BACKOFF     // 连续失败超 5 次，30 秒退避窗口内不查询 Vault
}
```

### `DefaultCredentialProvider`（类）

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：CredentialProvider 接口的默认内存实现，基于 ConcurrentHashMap + Caffeine 缓存 + 完整状态机

```java
@Component
public class DefaultCredentialProvider implements CredentialProvider {
    private final ConcurrentHashMap<String, Credential> credentialStore;
    private final Cache<String, Credential> cache;
    private final AtomicInteger consecutiveFailures;
    private final AtomicReference<CredentialProviderState> state;
    private volatile long backoffUntil;

    public DefaultCredentialProvider();

    public void registerCredential(String endpointId, Credential credential);

    @Override public Optional<Credential> getCredential(String endpointId);
    @Override public CredentialProviderState getState();
    @Override public boolean isCredentialValid(String endpointId);
    @Override public boolean probeVault();

    // 测试辅助方法
    public void simulateVaultTimeout();
    public void simulateVaultRecovery();
}
```

**公开接口**：
- `registerCredential(String endpointId, Credential credential)` — 注入/更新凭据到 credentialStore 和 cache
- `getCredential(String endpointId)` — 按状态机逻辑查询凭据
- `getState()` — 返回当前状态
- `isCredentialValid(String endpointId)` — 检查凭据是否存在且未过期
- `probeVault()` — 若非 NORMAL，则原子重置 `state=NORMAL`、`consecutiveFailures=0`、`backoffUntil=0`，返回 true；若已是 NORMAL，直接返回 true
- `simulateVaultTimeout()` — 模拟 Vault 超时（该方法同时承载"模拟 Vault 超时"和"强制进入 CACHE_ONLY"双重语义；注意：NORMAL 状态下调用前须确保该 endpointId 的凭据已缓存，否则进入 CACHE_ONLY 后缓存无数据，后续 `getCredential()` 将返回 `Optional.empty()`，与状态转换表 precondition 一致）：
  1. 递增 `consecutiveFailures`
  2. 当前为 NORMAL → 切换至 CACHE_ONLY
  3. 当前为 CACHE_ONLY 且 `consecutiveFailures` ≥ 5 → 切换至 BACKOFF，设置 `backoffUntil = System.nanoTime() + 30_000_000_000L`
  4. 其他情况（BACKOFF 或 CACHE_ONLY 且 < 5）→ 保持当前状态
- `simulateVaultRecovery()` — 模拟 Vault 恢复：原子重置 `state=NORMAL`，`consecutiveFailures=0`，`backoffUntil=0`

**构造方式**：`new DefaultCredentialProvider()`，@Component 自动注册

**字段说明**：
- `credentialStore`: `ConcurrentHashMap<String, Credential>` — 模拟 Vault 的内存凭据存储
- `cache`: `Cache<String, Credential>` — Caffeine 缓存，使用 Expiry 接口动态 TTL
- `consecutiveFailures`: `AtomicInteger` — Vault 连续不可达计数
- `state`: `AtomicReference<CredentialProviderState>` — 当前状态，初始 NORMAL
- `backoffUntil`: `volatile long` — BACKOFF 窗口结束时间戳（System.nanoTime() 基准）

**缓存策略**（Caffeine Expiry 接口 — 注意：Expiry 三个方法返回值单位为**纳秒 (nanoseconds)**，而非毫秒或秒）：
- `expireAfterCreate`: OAUTH2 根据 `expiresAt` 动态计算（提前 60 秒 = 60_000_000_000L ns），最长 5 分钟（5 × 60 × 1_000_000_000L = 300_000_000_000L ns）；其他类型固定 5 分钟
- `expireAfterUpdate`: 返回 `currentDuration`，不修改过期时间
- `expireAfterRead`: CACHE_ONLY 状态时额外延长 30 秒（30_000_000_000L ns），上限为原始 TTL 的 2 倍（即最长 10 分钟 = 600_000_000_000L ns）；其他状态返回 `currentDuration`

**状态机实现**（`getCredential()` 中判定）：
- BACKOFF: `System.nanoTime() < backoffUntil`（统一使用 `System.nanoTime()` 时钟源进行比较）→ 返回 `Optional.empty()`；否则使用 `compareAndSet(CredentialProviderState.BACKOFF, CredentialProviderState.NORMAL)` 原子恢复 NORMAL，避免并发覆盖其他线程的状态变更（如 CACHE_ONLY）
- CACHE_ONLY:
  1. 从 Caffeine 缓存查询凭据
  2. 缓存命中 → 返回 `Optional.of(credential)`，`expireAfterRead` 延长 TTL（+30 秒，上限 10 分钟）
  3. 缓存未命中 → 返回 `Optional.empty()`
  4. 不查询 `credentialStore`（Vault）；状态转换 CACHE_ONLY→NORMAL 和 CACHE_ONLY→BACKOFF 由 `probeVault()` 或 `simulateVaultTimeout()` / `simulateVaultRecovery()` 驱动，不在 `getCredential()` 内自动触发
- NORMAL:
  1. 查询 `credentialStore`（模拟 Vault）：
     a. **Vault 返回正常结果** → 写入 Caffeine 缓存，重置 `consecutiveFailures=0`，返回凭据（保持 NORMAL）
     b. **Vault 查询超时**（该分支为接口契约的通用描述；在内存实现中通过 `simulateVaultTimeout()` 模拟触发）→ 递增 `consecutiveFailures` → 查询 Caffeine 缓存是否有该 `endpointId` 的旧凭据：
        - 缓存中有旧凭据（Vault 不可达但可回退至缓存）→ 写入 Caffeine 缓存刷新 TTL，切换至 **CACHE_ONLY**，WARN 日志，返回旧凭据
        - 缓存中无旧凭据（从未成功查询过）→ 保持 **NORMAL**，抛出 `CredentialUnavailableException`
     c. **Vault 返回空**（`endpointId` 未在 `credentialStore` 中注册，非超时场景）→ 递增 `consecutiveFailures`；若 ≥ 5 次 → 切换至 **BACKOFF** 并设置 30 秒窗口；否则返回 `Optional.empty()`

**类型关系**：implements `CredentialProvider`

### `EndpointRateLimiter`（类）

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.client`
**职责**：端点限流器，基于 Guava RateLimiter 令牌桶算法，按 endpointId 维度独立限流

```java
@Component
public class EndpointRateLimiter {
    private final ConcurrentHashMap<String, RateLimiter> limiters;
    private final Environment environment;

    // 默认配置（属性前缀 ai.rate-limiting.endpoints.default）
    @Value("${ai.rate-limiting.endpoints.default.permits-per-second:10}")
    private double defaultPermitsPerSecond;

    @Value("${ai.rate-limiting.endpoints.default.max-burst-seconds:1}") // 为 Task 18 预留，当前版本未使用（Guava SmoothBursty 默认 maxBurstSeconds=1.0）
    private double defaultMaxBurstSeconds;

    @Value("${ai.rate-limiting.endpoints.default.queue-wait-millis:100}")
    private long defaultQueueWaitMillis;

    public EndpointRateLimiter(Environment environment);

    public boolean tryAcquire(String endpointId);
    public boolean tryAcquire(String endpointId, long timeout, TimeUnit unit);
}
```

**公开接口**：
- `tryAcquire(String endpointId)` — 尝试获取令牌，使用 endpointId 对应的 `queue-wait-millis`（默认 100ms）等待超时
- `tryAcquire(String endpointId, long timeout, TimeUnit unit)` — 尝试获取令牌，指定超时

**构造方式**：`new EndpointRateLimiter(environment)`，@Component 自动注册

**配置来源**：三维度配置属性统一使用前缀 `ai.rate-limiting.endpoints.{endpointId}.{permits-per-second / max-burst-seconds / queue-wait-millis}`。`endpointId=default` 提供全局默认值；各 endpoint 独立配置通过 `Environment` 运行时查询动态解析（形如 `getProperty("ai.rate-limiting.endpoints." + endpointId + ".permits-per-second", Double.class, defaultPermitsPerSecond)`）。AiPlatformConfig 统一装配推迟至 Task 18。

**内部实现**：
- `ConcurrentHashMap<String, RateLimiter>` 存储各端点限流器实例
- `tryAcquire(endpointId)`: 从 `limiters` 获取或创建 `RateLimiter`；创建时使用 `permits-per-second` 调用 `RateLimiter.create(permitsPerSecond)`；获取令牌使用 `tryAcquire(queueWaitMillis, TimeUnit.MILLISECONDS)`
- `tryAcquire(endpointId, timeout, unit)`: 同上，使用指定超时
- 三维度配置全部通过 `@Value` 注入默认值，per-endpoint 重载通过 `Environment.getProperty("ai.rate-limiting.endpoints." + endpointId + ".permits-per-second", Double.class, defaultPermitsPerSecond)` 运行时动态查询：
  - `permits-per-second` → `RateLimiter.create(double)` 创建令牌桶速率
  - `max-burst-seconds` → 当前通过 Guava `SmoothBursty` 的默认 `maxBurstSeconds=1.0` 隐含实现，Task 18 可引入定制 `RateLimiter` 子类显式控制突发窗口
  - `queue-wait-millis` → 作为 `tryAcquire()` 无参重载的等待超时参数

**类型关系**：独立类，无实现/继承

### `LlmInfrastructureException`（类）

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.client.exception`
**职责**：LLM 基础设施异常，语义为 "HTTP 5xx、连接超时、网络抖动等基础设施故障，直接降级不尝试 chat() 回退"

```java
public class LlmInfrastructureException extends RuntimeException {
    public LlmInfrastructureException(String message);
    public LlmInfrastructureException(String message, Throwable cause);
}
```

**构造方式**：`new LlmInfrastructureException("Connection timeout")` / `new LlmInfrastructureException("HTTP 502", cause)`

**类型关系**：extends `RuntimeException`

### `CredentialUnavailableException`（类）

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.client.exception`
**职责**：凭据不可用异常，NORMAL 状态下 Vault 查询超时且缓存中无该 endpointId 凭据时抛出；语义为"凭据暂时不可用，调用方不应重试（避免放大 Vault 压力）"

```java
public class CredentialUnavailableException extends RuntimeException {
    public CredentialUnavailableException(String message);
    public CredentialUnavailableException(String message, Throwable cause);
}
```

**构造方式**：`new CredentialUnavailableException("Credential unavailable for endpoint ep1")` / `new CredentialUnavailableException("Vault timeout", cause)`

**类型关系**：extends `RuntimeException`

## 错误处理

| 类型 | 异常/返回 | 传播策略 |
|------|----------|---------|
| 凭据不存在 | `getCredential()` 返回 `Optional.empty()` | 调用方自行处理 |
| 凭据无效 | `isCredentialValid()` 返回 false | 调用方自行判断 |
| BACKOFF 状态查询 | `getCredential()` 返回 `Optional.empty()` | 调用方走降级路径 |
| 限流超时 | `tryAcquire()` 返回 false | 调用方决定降级或重试 |
| 基础设施故障 | `LlmInfrastructureException` (RuntimeException) | 透传至 CapabilityExecutor 降级路径 |

## 行为契约

**CredentialProvider 状态机转换**：
| 起始状态 | 触发条件 | 目标状态 | 说明 |
|---------|---------|---------|------|
| NORMAL | Vault 查询超时，Caffeine 缓存中有该 endpointId 的旧凭据 | CACHE_ONLY | 旧凭据 TTL 延长 30 秒，WARN 日志 |
| NORMAL | Vault 查询超时，且缓存中无该 endpointId 的凭据 | NORMAL | 抛出 `CredentialUnavailableException`，不改变状态 |
| NORMAL | Vault 返回正常结果 | NORMAL | 写回缓存，重置连续失败计数器 |
| NORMAL | Vault 返回空（endpointId 未注册），连续失败 ≥ 5 | BACKOFF | 启动 30 秒退避窗口 |
| CACHE_ONLY | Vault 查询超时，连续失败 < 5 | CACHE_ONLY | 仍使用缓存，WARN 日志递增失败计数 |
| CACHE_ONLY | Vault 连续不可达 ≥ 5 | BACKOFF | 启动 30 秒退避窗口 |
| CACHE_ONLY | Vault 返回正常结果 | NORMAL | 重置连续失败计数器，恢复正常查询 |
| BACKOFF | 30 秒退避窗口到期 | NORMAL | 窗口期结束后触发一次探测查询 |
| BACKOFF | 窗口期内再次调用 | BACKOFF | 直接返回 Optional.empty()，不查询 Vault |

**DefaultCredentialProvider 方法调用顺序**：
1. 构造器 → 初始化 credentialStore、cache、state=NORMAL、consecutiveFailures=0
2. `registerCredential()` 可在任何时间调用注入/更新凭据
3. `getCredential()` 按当前状态执行不同查询路径
4. `simulateVaultTimeout()` / `simulateVaultRecovery()` 仅测试场景使用

**EndpointRateLimiter 线程安全**：RateLimiter 本身线程安全，ConcurrentHashMap 保证创建/访问原子性。

## 依赖关系

**依赖的已有类型**：
- `AuthType` (`com.aimedical.modules.ai.impl.client.AuthType`) — Credential 值对象引用
- `StructuredOutputNotSupportedException` (`com.aimedical.modules.ai.impl.client.exception`) — 参考其实现模式构建 LlmInfrastructureException

**外部依赖**（pom.xml 新增）：
- `com.github.ben-manes.caffeine:caffeine` — scope compile，DefaultCredentialProvider 使用 Caffeine Expiry 接口
- `com.google.guava:guava:32.1.3-jre` — scope compile，EndpointRateLimiter 使用 Guava RateLimiter（父 POM `backend/pom.xml` 的 dependencyManagement 未管理 Guava，必须显式指定 version）

**暴露给后续任务的公开接口**：
- `CredentialProvider` 接口 → HttpApiLlmChatService / SpringAiLlmChatService 通过构造器注入
- `EndpointRateLimiter` → HttpApiLlmChatService / SpringAiLlmChatService 通过构造器注入
- `LlmInfrastructureException` → LlmChatService 接口 throws 声明

## 设计偏差说明

### NORMAL 状态下查询顺序：vault-first（设计）vs cache-first（task_v10）

**偏离描述**：task_v10 line 73 指定 NORMAL 状态为"查缓存 → 命中返回 | 未命中查 credentialStore"（缓存优先），而本设计（§ NORMAL 实现）采用 vault-first 顺序（先查 credentialStore，再按结果决定是否使用缓存）。

**理由**：
1. 状态机正确性：若缓存优先且命中，`getCredential()` 将无法感知 Vault 不可达（因未实际查询 credentialStore），导致无法触发 NORMAL→CACHE_ONLY 或 CACHE_ONLY→BACKOFF 状态转换
2. vault-first 确保每次 NORMAL 状态都实际查询 credentialStore 以判断 Vault 可达性，是状态机正确运转的前提
3. 该偏离不影响 task_v10 定义的验收测试，且与 OOD 设计文档 §3.2 一致

**影响范围**：仅 `DefaultCredentialProvider.getCredential()` 在 NORMAL 状态下的内部查询顺序；对外接口行为与 task_v10 定义一致。

## 修订说明（v10 r4）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] EndpointRateLimiter 配置机制矛盾（@Value 与懒加载冲突） | 删除 @Value 描述，统一采用 `Environment` 运行时查询；构造器增加 `Environment environment` 参数 |
| [一般] Caffeine Expiry 时间单位未明确纳秒 | 在缓存策略小节首行显式标注 Expiry 返回值单位为纳秒，所有时间值（5 分钟、30 秒、60 秒、10 分钟）均附换算示例（如 5 × 60 × 1_000_000_000L ns） |
| [轻微] CredentialProviderState 放置策略模糊 | 明确指定为 package-private 顶层枚举置于 `CredentialProvider.java` 同文件中 |
| [轻微] backoffUntil 时钟源同一性未显式声明 | BACKOFF 判断条件中明确为 `System.nanoTime() < backoffUntil`，注明统一使用 `System.nanoTime()` 时钟源 |

## 修订说明（v10 r5）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] CredentialProviderState 代码块中 `public` 修饰符将导致编译错误 | 代码块 `public enum` 改为 `enum`（去掉 public），与"package-private 顶层 enum"描述一致 |
| [一般] EndpointRateLimiter 配置方式与 task_v10 要求不一致 | 回退为 `@Value` 注入方式：新增 `@Value("${ai.rate-limiting.default.permits-per-second:10}") defaultPermitsPerSecond` 和 `@Value("${ai.rate-limiting.default.max-burst-seconds:1}") defaultMaxBurstSeconds` 字段，移除 `Environment` 依赖和构造器参数；配置描述改为 @Value 注入默认参数，各端点独立属性由 AiPlatformConfig（Task 18）统一装配 |
| [轻微] 状态转换表中"NORMAL（抛出）"与 NORMAL 实现逻辑不一致 | 状态转换表第三行 `NORMAL（抛出）` 改为 `CACHE_ONLY`：Vault 查询超时统一导致状态迁移至 CACHE_ONLY，与 NORMAL 实现描述一致（递增 failures，不抛出异常） |

## 修订说明（v10 r6）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] Guava 依赖缺少版本号，编译将直接失败 | Guava 依赖声明加上 `<version>32.1.3-jre</version>`；父 POM（`backend/pom.xml`）的 dependencyManagement 未管理 Guava，必须显式指定 |
| [一般] EndpointRateLimiter.defaultMaxBurstSeconds 为死代码 | 移除 `defaultMaxBurstSeconds` 字段及其 `@Value` 注解决明；实现仅使用 `RateLimiter.create(defaultPermitsPerSecond)`，该字段未被消费 |
| [轻微] 配置属性前缀与 task_v10 存在偏差 | 保留 `ai.rate-limiting.default.permits-per-second` 作为全局默认值（当前任务合理选择），在描述中注明各端点独立配置前缀 `ai.rate-limiting.endpoints.{endpointId}` 推迟至 Task 18 |
| [轻微] BACKOFF 状态恢复未指定原子操作 | 明确使用 `compareAndSet(CredentialProviderState.BACKOFF, CredentialProviderState.NORMAL)` 进行原子状态恢复，防止并发覆盖 |

## 修订说明（v10 r8）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] DefaultCredentialProvider 状态机实现描述与状态转换表严重矛盾 — NORMAL 状态实现中未出现 CACHE_ONLY 转换路径 | 重写 NORMAL 状态实现描述：细分为 Vault 返回正常结果（→NORMAL）、Vault 查询超时且 credentialStore 中有数据（→CACHE_ONLY）、Vault 查询超时且 credentialStore 中无数据（→NORMAL+抛出 CredentialUnavailableException）、Vault 返回空（→递增 failures，≥5 次→BACKOFF）四条分支，与状态转换表完全对应 |
| [一般] EndpointRateLimiter 配置机制与 task_v10 不一致 — 使用 default 前缀而非 endpoints.{endpointId} 前缀，max-burst-seconds 和 queue-wait-millis 缺失 | 配置前缀统一为 `ai.rate-limiting.endpoints.{endpointId}.{permits-per-second / max-burst-seconds / queue-wait-millis}`；使用 @Value 注入 `endpoints.default.*` 三字段作为默认值；+Environment 运行时查询 per-endpoint 独立配置；max-burst-seconds 描述映射至 Guava RateLimiter 内置突发能力 |
| [一般] 状态转换表第 2 行（NORMAL \| Vault 查询超时缓存无数据→CACHE_ONLY）与 OOD 设计文档 §3.2 不符 | 状态转换表第 2 行修正为：NORMAL \| Vault 查询超时且 credentialStore 中无该 endpointId 凭据 → NORMAL（抛出 CredentialUnavailableException），与 OOD 文档 1991 行一致；状态转换表新增"说明"列以提升可读性 |
| [一般] BACKOFF 状态转换表第 7 行与实现描述矛盾（返回缓存数据 vs Optional.empty） | 状态转换表第 7 行"直接返回缓存数据"统一为"直接返回 Optional.empty()"，与 task_v10 line 71 一致 |
| [轻微] NORMAL 状态 vault-first 偏离 task_v10 cache-first 顺序未记录 | 新增"设计偏差说明"小节（§ 依赖关系之后），解释 vault-first 的理由（状态机正确性前提）及影响范围 |
| [轻微] NORMAL 实现引用未定义的 vaultTimeout 标志位 | 删除 `vaultTimeout` 标志位引用，改为"该分支为接口契约的通用描述；在内存实现中通过 `simulateVaultTimeout()` 模拟触发" |
| [轻微] simulateVaultTimeout 无条件 NORMAL→CACHE_ONLY 忽略缓存条件 | 在 `simulateVaultTimeout()` 描述中补充 precondition 说明：NORMAL 状态下调用前须确保凭据已缓存，否则进入 CACHE_ONLY 后因缓存无数据将返回 Optional.empty() |
| [轻微] probeVault() 实现未触发状态恢复 | `probeVault()` 改为：若非 NORMAL，则原子重置 state=NORMAL、consecutiveFailures=0、backoffUntil=0，返回 true |

## 修订说明（v10 r9）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] DefaultCredentialProvider 状态机实现描述与状态转换表严重矛盾 — CACHE_ONLY 实现描述（仅"跳过credentialStore，从缓存返回"）缺少 CACHE_ONLY→BACKOFF 转换路径，验收测试 NORMAL→CACHE_ONLY→BACKOFF 无法通过 | CACHE_ONLY 描述扩展为完整的 4 步骤（查缓存→命中返回→未命中返回empty→不查询credentialStore，转换由probeVault/simulate驱动）；simulateVaultTimeout 细化为状态感知行为（NORMAL→CACHE_ONLY、CACHE_ONLY且≥5→BACKOFF）；simulateVaultRecovery 指定原子重置三项字段；NORMAL 步骤 2b 增加"递增 consecutiveFailures"使失败计数与状态机一致 |
| [一般] EndpointRateLimiter 配置机制与 task_v10 不一致 — max-burst-seconds 和 queue-wait-millis 被视为"完全忽略" | 内部实现重写为三维度显式描述：permits-per-second 用于 RateLimiter.create、max-burst-seconds 映射至 Guava SmoothBursty 默认行为（标注 Task 18 可定制）、queue-wait-millis 作为 tryAcquire 默认超时；配置描述明确使用 @Value 注入三字段默认值 + Environment 逐维度运行时查询 |
| [一般] 状态转换表第 2 行触发条件与 OOD 不符（使用 credentialStore 而非缓存） | 状态转换表第 1/2 行触发条件改为"Caffeine 缓存中有/无该 endpointId 的凭据"，与 OOD §3.2 行 1990-1991 一致；NORMAL 实现改为 vault-first 顺序（先查 credentialStore，超时后查缓存是否有旧凭据回退），消除缓存优先与 CACHE_ONLY 条件之间的逻辑矛盾 |

## 修订说明（v10 r11）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] `CredentialUnavailableException` 未纳入文件规划 — NORMAL 实现步骤 2b 和状态转换表第 2 行引用了不存在的类型 | 新增 `CredentialUnavailableException` 类型定义（extends RuntimeException，String 和 String+Throwable 双构造器）；文件规划中新增该异常文件和对应测试文件；错误处理表隐含其传播策略 |
| [一般] NORMAL→BACKOFF 转换路径未纳入状态转换表 | 状态转换表新增第 4 行：`NORMAL \| Vault 返回空（endpointId 未注册），连续失败 ≥ 5 \| BACKOFF \| 启动 30 秒退避窗口` |
| [轻微] `defaultMaxBurstSeconds` 字段声明但未消费 | 在 `@Value` 声明行追加注释"为 Task 18 预留，当前版本未使用"；该字段为 Guava 默认行为参考，待 Task 18 引入定制 `RateLimiter` 子类时正式启用 |
