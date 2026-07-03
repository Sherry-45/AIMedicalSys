# 任务指令（v10）

## 动作
NEW

## 任务描述
在 `ai-impl/client/` 包新增以下 3 个类型（含 1 个内嵌值对象 + 1 个状态枚举），为 LLM 调用层（Task 9/10）提供凭据查询和限流基础设施：

### 1. `CredentialProvider`（接口，`ai-impl/client/CredentialProvider.java`）
凭据查询接口，按 endpointId 从 Vault/配置中心查询认证凭据。含以下成员：

```java
public interface CredentialProvider {
    Optional<Credential> getCredential(String endpointId);
    CredentialProviderState getState();
    boolean isCredentialValid(String endpointId);
    boolean probeVault();

    class Credential {
        AuthType authType;
        String credentialValue;
        Instant expiresAt;
        String vaultPath;
        // 全参构造器 + getters
    }
}

enum CredentialProviderState {
    NORMAL,
    CACHE_ONLY,
    BACKOFF
}
```

**缓存策略**：使用 Caffeine `Expiry` 接口，`expireAfterCreate` 根据 `Credential.expiresAt` 动态计算（OAUTH2 提前 60 秒），最长 5 分钟；`expireAfterRead` 在 CACHE_ONLY 状态额外延长 30 秒（上限为原始 TTL 的 2 倍）。

**状态机**：NORMAL（正常查询 Vault）↔ CACHE_ONLY（Vault 超时，仅用缓存，TTL 延长）↔ BACKOFF（连续失败≥5 次，30 秒退避窗口）。详见设计文档 §3.2 完整状态转换表。

### 1b. `DefaultCredentialProvider`（类，`ai-impl/client/DefaultCredentialProvider.java`）
`CredentialProvider` 接口的默认内存实现，基于 `ConcurrentHashMap<String, Credential>` 存储凭据，含 Caffeine 缓存和完整状态机逻辑。被 `@Component` 标注，通过 `registerCredential()` 方法注入初始凭据。

```java
@Component
public class DefaultCredentialProvider implements CredentialProvider {
    // 核心字段
    private final ConcurrentHashMap<String, Credential> credentialStore;
    private final Cache<String, Credential> cache;       // Caffeine
    private final AtomicInteger consecutiveFailures;
    private final AtomicReference<CredentialProviderState> state;
    private volatile long backoffUntil;                   // BACKOFF 窗口结束时间

    // 构造器：初始化 Caffeine 缓存（Expiry 接口动态 TTL）
    public DefaultCredentialProvider() { ... }

    // 注入/更新凭据
    public void registerCredential(String endpointId, Credential credential);

    // CredentialProvider 接口方法
    @Override public Optional<Credential> getCredential(String endpointId);
    @Override public CredentialProviderState getState();
    @Override public boolean isCredentialValid(String endpointId);
    @Override public boolean probeVault();         // 内存实现始终返回 true

    // 测试辅助方法：模拟 Vault 超时/恢复以驱动状态机
    public void simulateVaultTimeout();
    public void simulateVaultRecovery();
}
```

**状态机实现**（在 `getCredential()` 中判定）：
- **BACKOFF**：若当前时间 < `backoffUntil`，直接返回 `Optional.empty()`；否则自动恢复为 NORMAL 并继续查询
- **CACHE_ONLY**：跳过 `credentialStore` 查询，仅从 Caffeine 缓存返回；`expireAfterRead` 延长 TTL
- **NORMAL**：查缓存 → 命中返回 | 未命中查 `credentialStore` → 找到则缓存后返回，未找到则递增 `consecutiveFailures`；若连续失败≥5 次，切至 BACKOFF 并设置 30 秒窗口

### 2. `EndpointRateLimiter`（类，`ai-impl/client/EndpointRateLimiter.java`）
端点限流器，基于 Guava `RateLimiter` 令牌桶算法，按 endpointId 维度独立限流。

```java
@Component
public class EndpointRateLimiter {
    public boolean tryAcquire(String endpointId); // 尝试获取令牌（默认 100ms 等待超时）
    public boolean tryAcquire(String endpointId, long timeout, TimeUnit unit);
    // 内部使用 ConcurrentHashMap<String, RateLimiter>
}
```

**配置来源**：通过 `@Value` 直接注入属性值，属性前缀 `ai.rate-limiting.endpoints.{endpointId}.{permits-per-second / max-burst-seconds / queue-wait-millis}`。AiPlatformConfig 统一装配推迟至 Task 18。

### 3. `LlmInfrastructureException`（类，`ai-impl/client/exception/LlmInfrastructureException.java`）
LLM 基础设施异常，extends RuntimeException。语义为"HTTP 5xx、连接超时、网络抖动等基础设施故障，直接降级不尝试 chat() 回退"。

```java
public class LlmInfrastructureException extends RuntimeException {
    public LlmInfrastructureException(String message) { ... }
    public LlmInfrastructureException(String message, Throwable cause) { ... }
}
```

## 选择理由
Task 8 是 Batch3 P1 LLM 调用层的基础设施组件，前置依赖（DTO 类型 ClientType/AuthType）均已完成。CredentialProvider 和 EndpointRateLimiter 是 DelegatingLlmChatService 和 HttpApiLlmChatService 的必要依赖，必须先于 Task 9/10 完成。LlmInfrastructureException 为 LlmChatService 接口提供的 throws 声明中基础设施异常分类所需。

## 任务上下文
### 已有类型状态
- `ClientType` 枚举 — 已完成（含 HTTP_API / SPRING_AI）
- `AuthType` 枚举 — 已完成（含 API_KEY / OAUTH2 / NONE）
- `LlmChatService` 接口 — 存根已完成（含 chat() / structuredChat() 方法签名）
- `StructuredOutputNotSupportedException` — 已完成（类似形态的异常类，可参考）

### 设计文档参考
- 设计文档 §3.2 ModelRoute → CredentialProvider：`CredentialProvider` 定义完整接口契约（含状态机、缓存策略、Vault 降级行为）
- 设计文档 §3.2 → EndpointRateLimiter：基于 Guava 令牌桶，`ConcurrentHashMap<String, RateLimiter>` 线程安全
- 设计文档 §3.2 → LlmInfrastructureException：extends RuntimeException，HTTP 5xx/超时/网络故障语义

## 涉及文件
| 操作 | 文件路径 |
|------|---------|
| 新建 | `modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/CredentialProvider.java` |
| 新建 | `modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/DefaultCredentialProvider.java` |
| 新建 | `modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/EndpointRateLimiter.java` |
| 新建 | `modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/client/exception/LlmInfrastructureException.java` |
| 新建 | `modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/client/CredentialProviderTest.java` |
| 新建 | `modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/client/DefaultCredentialProviderTest.java` |
| 新建 | `modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/client/EndpointRateLimiterTest.java` |
| 新建 | `modules/ai/ai-impl/src/test/java/com/aimedical/modules/ai/impl/client/exception/LlmInfrastructureExceptionTest.java` |
| 修改 | `modules/ai/ai-impl/pom.xml` → 添加 caffeine + guava 依赖声明 |

## 验证方式
```bash
mvn test -pl modules/ai/ai-impl -am
```
预期：test-compile 通过，新增测试覆盖 CredentialProvider 状态机转换（NORMAL→CACHE_ONLY→BACKOFF）、缓存正常/过期行为、DefaultCredentialProvider 状态机和缓存完整性、EndpointRateLimiter tryAcquire 成功/超时、LlmInfrastructureException 构造。

---

## 修订说明（v10 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] Caffeine 依赖未声明，编译将失败 | 在 `ai-impl/pom.xml` 中添加 `com.github.ben-manes.caffeine:caffeine` 依赖声明（scope compile）；涉及文件表新增 pom.xml 修改行 |
| [严重] Guava 依赖未声明，编译将失败 | 在 `ai-impl/pom.xml` 中添加 `com.google.guava:guava` 依赖声明（scope compile）；Spring Boot 3.2.5 BOM 管理版本，无需指定 version |
| [一般] EndpointRateLimiter 配置来源（AiPlatformConfig）尚未就绪 | 改为通过 `@Value` 直接注入属性值（前缀 `ai.rate-limiting.endpoints.{endpointId}`），AiPlatformConfig 统一装配推迟至 Task 18 |
| [一般] CredentialProvider 的 Vault 查询抽象缺失 | 新增 `DefaultCredentialProvider` 默认内存实现：基于 `ConcurrentHashMap` 的凭据存储 + Caffeine 缓存 + 完整状态机（NORMAL/CACHE_ONLY/BACKOFF）+ 测试辅助方法（simulateVaultTimeout/simulateVaultRecovery）；新增对应测试文件 |
