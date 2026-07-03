# 设计审查报告（v10 r2）

## 审查结果
REJECTED

## 发现

### **[严重] NORMAL 状态下查询顺序与任务描述冲突**

设计状态转换表（detail_v10.md:107）在 NORMAL 状态下先查 `credentialStore` 再查缓存——`credentialStore` 正常返回则直接返回凭据，`credentialStore` 无数据才回退到缓存并触发 CACHE_ONLY 转换。但 task_v10.md:72 明确要求：

> **NORMAL**：查缓存 → 命中返回 | 未命中查 `credentialStore` → 找到则缓存后返回，未找到则递增 `consecutiveFailures`；若连续失败≥5 次，切至 BACKOFF

两种顺序导致根本不同的状态机行为：
- 任务要求：缓存命中则短路，不查询 `credentialStore`，不触发 CACHE_ONLY
- 设计要求：每次 NORMAL 都查 `credentialStore`，未命中才走缓存从而触发 CACHE_ONLY

**期望修正**：将 NORMAL 行"触发条件"改为"查缓存命中 → NORMAL，返回凭据，重置失败计数器"；"缓存未命中 → 查 credentialStore → 找到则缓存后返回"；仅当 credentialStore 无数据且缓存存在旧凭据时才进入 CACHE_ONLY。或者确认设计为权威且独立于任务简化描述，则在设计中明确标注该顺序差异并解释理由。

### **[一般] CACHE_ONLY 状态下失败计数器递增机制缺失**

状态表行 112-113：CACHE_ONLY 状态下缓存命中/未命中均未说明是否递增 `consecutiveFailures`，但行 113 又声明"连续失败≥5"可触发 CACHE_ONLY→BACKOFF 转换。若无递增机制，CACHE_ONLY 一旦进入将永远无法到达 BACKOFF。

**期望修正**：明确 CACHE_ONLY 状态下缓存未命中时是否递增失败计数器，或写明 CACHE_ONLY→BACKOFF 转换仅发生在进入 CACHE_ONLY 前已在 NORMAL 累计到≥4 次失败、进入 CACHE_ONLY 后第 1 次缓存未命中推至 5 次的场景。

### **[一般] expireAfterUpdate 策略导致凭据更新时不重新计算 TTL**

设计(detail_v10.md:100)：`expireAfterUpdate` 返回当前 duration 不变。但 `registerCredential()` 调用 `cache.put(endpointId, credential)` 会触发 Caffeine 的更新回调——此时旧凭据的剩余 TTL 与新凭据的 `expiresAt` 无关。若新旧凭据 `expiresAt` 不同，将保留旧 TTL 导致新凭据提前过期或超期滞留。

**期望修正**：`expireAfterUpdate` 应根据新凭据的 `expiresAt`（OAUTH2 提前 60 秒）动态计算 TTL，与 `expireAfterCreate` 使用相同的计算逻辑。

### **[一般] CredentialProviderState 类型定位模糊**

设计(detail_v10.md:61)表述为"内嵌 CredentialProviderState 枚举"，但 task_v10.md:28-32 明确展示为顶层枚举（位于 `CredentialProvider` 接口同文件但非内部类型）。在 Java 中，嵌套于接口意味着 `CredentialProvider.CredentialProviderState` 的访问路径，顶层包私有枚举则直接为 `CredentialProviderState`。两者对外部使用者的引用方式不同。

**期望修正**：明确指明 `CredentialProviderState` 是 `CredentialProvider` 接口的内部嵌套类型还是同文件包私有顶层枚举，并确保整篇设计一致。

### **[轻微] BACKOFF 恢复 NORMAL 后 consecutiveFailures 未复位**

状态表行 115：BACKOFF 到期后自动恢复为 NORMAL，但 `consecutiveFailures` 仍保持≥5 的计数。NORMAL 状态下首次查询若再次失败，计数器增至 6（或保持 5），立即重新触发 BACKOFF。系统可能在单个失败后持续陷入 BACKOFF 循环，无法真正恢复。

**期望修正**：在 BACKOFF→NORMAL 转换时复位 `consecutiveFailures = 0`，或在进入 BACKOFF 时复位计数器。
