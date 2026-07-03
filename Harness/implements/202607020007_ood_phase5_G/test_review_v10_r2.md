# 测试审查报告（v10 r2）

## 审查结果
REJECTED

## 发现

- **[一般]** `DefaultCredentialProviderTest.java` — `shouldExtendTtlOnReadInCacheOnly` 未验证 `detail_v10.md` §缓存策略中 `expireAfterRead` 的"上限为原始 TTL 的 2 倍"约束。该测试使用约 10ms 原始 TTL 的 OAUTH2 凭据，在 CACHE_ONLY 下读取触发延长，sleep 50ms 后断言凭据仍 present。此断言仅在缺失 2x 上限时成立（当前实现仅使用 10 分钟硬上限）。若实现符合设计条件（2 × 10ms = 20ms cap），50ms sleep 后凭据应已驱逐。测试未能捕获该设计-实现偏差。

- **[一般]** `EndpointRateLimiterTest.java` — 三个测试方法（`shouldAcquireToken`、`shouldAcquireTokenWithCustomTimeout`、`shouldUseDefaultConfigWhenNoEndpointSpecificConfig`）均以相同参数调用 `tryAcquire("ep1", 5000, TimeUnit.MILLISECONDS)` 并断言 `assertTrue`，属重复测试，未增加边际覆盖。

## 修改要求（REJECTED）

1. **`DefaultCredentialProviderTest.java` | `shouldExtendTtlOnReadInCacheOnly`** — 该测试应明确验证 `expireAfterRead` 的 2x 上限约束：
   - 选项 A（验证上限存在）：短 TTL 凭据在 CACHE_ONLY 下读取后，等待超过 2×原始 TTL 的时间后断言凭据被驱逐（`assertFalse`）
   - 选项 B（规避上限）：使用较长原始 TTL（如 5 分钟）的凭据，确保 2x 上限（10 分钟）不影响 30 秒延长的验证，sleep 50ms 后断言 present 的预期仍然成立
   - 无论选择哪种方案，测试方法注释应明确其覆盖 `expireAfterRead` 的具体约束分支

2. **`EndpointRateLimiterTest.java` | 重复测试** — 移除或合并重复方法：
   - 保留 `shouldAcquireToken` 作为基本获取令牌验证
   - `shouldAcquireTokenWithCustomTimeout` 应改为测试真正的超时场景（如超时耗尽后返回 false）
   - `shouldUseDefaultConfigWhenNoEndpointSpecificConfig` 应改为验证 `Environment` 回退到默认值的逻辑（例如通过验证 mock 调用或默认值生效后的限流行为）
