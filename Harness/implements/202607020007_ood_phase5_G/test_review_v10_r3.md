# 测试审查报告（v10 r3）

## 审查结果
REJECTED

## 发现

- **[一般]** `DefaultCredentialProviderTest.java` — `shouldExtendTtlOnReadInCacheOnly` 名义上验证 CACHE_ONLY 状态下 expireAfterRead 的 TTL 延长行为，但实现中 `getCredential()` 使用 `cache.getIfPresent()`，Caffeine 的该 API 不会触发 `expireAfterRead` 回调（仅简单检查过期后返回），导致延长逻辑实际未被调用。测试仅验证 entry 在 50ms 后过期——这恰好是原始 10ms TTL 自然过期的结果，与 CACHE_ONLY 延长逻辑无关。测试注释声称 cap 为"2× original TTL = 20ms"，但实现中 expireAfterRead 硬编码 cap 为 600s（10 分钟），注释与实现不符。该测试未覆盖设计契约中 expireAfterRead 在 CACHE_ONLY 下的延长行为，属于测试盲区。

## 修改要求（仅 REJECTED 时）

### [一般] `DefaultCredentialProviderTest.java:193-205`（`shouldExtendTtlOnReadInCacheOnly`）

**问题**：测试声称验证 expireAfterRead TTL 延长，但：
1. `cache.getIfPresent()` 不触发 `expireAfterRead`，延长逻辑实际未被执行
2. 测试注释"2 × original TTL = 20ms"与实现 cap（600s）矛盾
3. 测试仅证明 entry 在 50ms 后不存在（原始 10ms TTL 自然过期），未证明延长生效

**期望**：改为使用能触发 `expireAfterRead` 的缓存读取方式（如 `cache.get(key, k -> null)`），并验证延长后存活时间超过原始 TTL（例如 10ms TTL 的 entry 在 CACHE_ONLY 读后应存活超过 10ms），同时修正注释中的 cap 描述以匹配实现。
