# 测试审查报告（v10 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** `DefaultCredentialProviderTest.java` — 缓存过期行为（Caffeine Expiry TTL）无测试覆盖。设计规格 § 缓存策略 明确指定：OAUTH2 动态 TTL（`expiresAt - 60s`，最长 5 分钟）、`expireAfterRead` 在 CACHE_ONLY 下延长 30 秒（上限 10 分钟）。这些行为是正确性的核心保障（避免返回过期凭据、确保 Vault 故障期间凭据不被提前驱逐），但无任何测试验证。

- **[一般]** `DefaultCredentialProviderTest.java` — BACKOFF 自动恢复路径（30 秒退避窗口到期后通过 `getCredential()` 自动恢复为 NORMAL）未测试。设计状态转换表第 7 行定义 BACKOFF→NORMAL 自动转换，当前仅测试了 `probeVault()` 和 `simulateVaultRecovery()` 的显式恢复路径，缺少超时到期后 `getCredential()` 触发自动恢复的测试（可通过反射设置 `backoffUntil` 为过去值实现，无需 30 秒等待）。

- **[轻微]** `DefaultCredentialProviderTest.java` — `shouldReturnCachedCredentialInCacheOnlyState` 与 `shouldReturnCachedCredentialInCacheOnly` 测试相同的缓存命中行为（仅端点 ID 不同），属重复测试。

- **[轻微]** `DefaultCredentialProviderTest.java` — 状态机线程安全性未测试。`AtomicReference`/`AtomicInteger`/`volatile` 的并发正确性依赖实现细节，无并发测试验证。

- **[轻微]** `EndpointRateLimiterTest.java` — 通过反射设置 `@Value` 字段（`defaultPermitsPerSecond`、`defaultQueueWaitMillis`），未在构造器中注入默认值，增加了测试脆弱性。但鉴于无 Spring 上下文的单元测试中此为常见做法，属可接受折中。

## 修改要求（仅 REJECTED 时）

### 1. [一般] 缓存过期行为未测试 — `DefaultCredentialProviderTest.java`

**位置**：`DefaultCredentialProviderTest.java`，现有测试均未涉及缓存 TTL 验证。

**问题**：Caffeine `Expiry` 接口的自定义 TTL 逻辑是设计规格明确要求的行为：OAUTH2 凭据的 `expireAfterCreate` 需根据 `expiresAt` 动态计算（提前 60 秒）；`expireAfterRead` 在 CACHE_ONLY 状态下额外延长 30 秒。当前无测试验证这些逻辑的正确性。

**期望方向**：
- 新增 `shouldUseCustomTtlForOauth2Credentials()`：注册 OAUTH2 凭据，验证缓存条目在到达动态 TTL 后被驱逐
- 新增 `shouldExtendTtlOnReadInCacheOnly()`：进入 CACHE_ONLY 后读取凭据，验证 `expireAfterRead` 延长了存活时间
- 可借助 Caffeine 的 `Cache.stats()` 或 `estimatedSize()` 间接验证驱逐行为，或使用 `Caffeine.newBuilder().recordStats().build()` 构造可统计缓存

### 2. [一般] BACKOFF 自动恢复路径未测试 — `DefaultCredentialProviderTest.java`

**位置**：`DefaultCredentialProviderTest.java`，在 `shouldReturnEmptyDuringBackoff` 测试之后。

**问题**：设计状态转换表规定 BACKOFF→NORMAL 自动恢复发生在 30 秒退避窗口到期后，下一次 `getCredential()` 调用时。当前仅测试了 `probeVault()` 和 `simulateVaultRecovery()` 的显式恢复，缺少自动恢复路径的测试覆盖。

**期望方向**：
- 新增 `shouldAutoRecoverFromBackoffAfterTimerExpiry()`：
  1. 触发 5 次 `simulateVaultTimeout()` 进入 BACKOFF
  2. 通过反射将 `backoffUntil` 字段设为过去值（如 `System.nanoTime() - 1`），模拟窗口到期
  3. 调用 `getCredential("ep1")`，验证返回 `Optional.of(...)`（自动恢复已发生）
  4. 验证 `getState()` 返回 NORMAL
