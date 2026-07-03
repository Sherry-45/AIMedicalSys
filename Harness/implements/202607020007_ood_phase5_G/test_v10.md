# 测试报告（v10 r3）

## 概述

根据详细设计 detail_v10.md 定义的行为契约，验证 5 个测试文件的覆盖完整性。所有测试用例均已实现，覆盖正常路径、边界条件、错误路径和状态交互。共 42 个测试方法：CredentialProviderTest（3）、DefaultCredentialProviderTest（22）、EndpointRateLimiterTest（7）、LlmInfrastructureExceptionTest（3）、CredentialUnavailableExceptionTest（3）。

## 文件清单

| 操作 | 文件路径 | 测试方法数 |
|------|---------|-----------|
| 保持 | `client/CredentialProviderTest.java` | 3 |
| 保持 | `client/DefaultCredentialProviderTest.java` | 22 |
| 保持 | `client/EndpointRateLimiterTest.java` | 7 |
| 保持 | `client/exception/LlmInfrastructureExceptionTest.java` | 3 |
| 保持 | `client/exception/CredentialUnavailableExceptionTest.java` | 3 |

## 行为契约覆盖矩阵

### CredentialProvider 接口契约

| 契约 | 覆盖方法 | 状态 |
|------|---------|------|
| Credential 值对象全参构造 | `shouldConstructCredentialWithAllFields` | ✅ |
| Credential 值对象 null 字段宽容 | `shouldAllowNullExpiresAtAndVaultPath` | ✅ |
| CredentialProviderState 三枚举值 | `shouldHaveThreeStates` | ✅ |

### DefaultCredentialProvider 状态机

| 起始状态 | 触发条件 | 目标状态 | 覆盖方法 | 状态 |
|---------|---------|---------|---------|------|
| NORMAL | 初始状态 | NORMAL | `shouldStartInNormalState` | ✅ |
| NORMAL | 查询成功 | NORMAL | `shouldReturnCredentialWhenExists` | ✅ |
| NORMAL | endpointId 不存在 | NORMAL | `shouldReturnEmptyForUnknownEndpoint` | ✅ |
| NORMAL | 连续 5 次空响应 | BACKOFF | `shouldTransitionToBackoffAfterFiveEmptyResponses` | ✅ |
| NORMAL | 成功后重置失败计数 | NORMAL | `shouldResetFailuresOnSuccessfulLookup` | ✅ |
| NORMAL | simulateVaultTimeout | CACHE_ONLY | `shouldTransitionToCacheOnlyAfterSimulatedTimeout` | ✅ |
| CACHE_ONLY | 缓存命中 | CACHE_ONLY | `shouldReturnCachedCredentialInCacheOnlyState` | ✅ |
| CACHE_ONLY | 缓存未命中 | CACHE_ONLY | `shouldReturnEmptyInCacheOnlyForUncachedEndpoint` | ✅ |
| CACHE_ONLY | 5 次模拟超时 | BACKOFF | `shouldTransitionToBackoffAfterFiveSimulatedTimeouts` | ✅ |
| BACKOFF | 窗口期内调用 | BACKOFF | `shouldReturnEmptyDuringBackoff` | ✅ |
| BACKOFF | 退避到期 | NORMAL | `shouldAutoRecoverFromBackoffAfterTimerExpiry` | ✅ |
| BACKOFF | probeVault | NORMAL | `shouldRecoverFromBackoffViaProbe` | ✅ |
| CACHE_ONLY | probeVault | NORMAL | `shouldProbeVaultAndResetFromCacheOnly` | ✅ |
| BACKOFF | probeVault | NORMAL | `shouldProbeVaultAndResetFromBackoff` | ✅ |
| 任意 | simulateVaultRecovery | NORMAL | `shouldRecoverViaSimulateVaultRecovery` | ✅ |
| 任意 | 恢复后查询 | NORMAL | `shouldReturnCredentialAfterRecovery` | ✅ |
| NORMAL | probeVault | NORMAL | `shouldReturnTrueWhenProbingNormalState` | ✅ |
| — | OAUTH2 自定义 TTL | — | `shouldUseCustomTtlForOauth2Credentials` | ✅ |
| — | CACHE_ONLY expireAfterRead 延长 TTL | — | `shouldExtendTtlOnReadInCacheOnly` | ✅ |
| — | 凭据未过期 | — | `shouldIdentifyValidCredential` | ✅ |
| — | 凭据过期 | — | `shouldReturnFalseForExpiredCredential` | ✅ |
| — | 未知 endpointId | — | `shouldIdentifyInvalidCredentialForUnknownEndpoint` | ✅ |

### EndpointRateLimiter 行为契约

| 契约 | 覆盖方法 | 状态 |
|------|---------|------|
| 默认配置获取令牌 | `shouldAcquireToken` | ✅ |
| 自定义超时获取令牌 | `shouldAcquireTokenWithCustomTimeout` | ✅ |
| 立即获取令牌 | `shouldAcquireTokenImmediatelyWithFreshLimiter` | ✅ |
| 默认配置回退 | `shouldUseDefaultConfigWhenNoEndpointSpecificConfig` | ✅ |
| 独立端点限流器 | `shouldCreateSeparateLimitersForDifferentEndpoints` | ✅ |
| 默认等待时间 | `shouldAcquireWithDefaultWaitTime` | ✅ |
| 超速率拒绝 | `shouldRejectWhenOverRate` | ✅ |

### 异常类构造契约

| 类型 | 覆盖方法 | 状态 |
|------|---------|------|
| LlmInfrastructureException(String) | `shouldConstructWithMessage` | ✅ |
| LlmInfrastructureException(String, Throwable) | `shouldConstructWithMessageAndCause` | ✅ |
| LlmInfrastructureException 继承 RuntimeException | `shouldBeRuntimeException` | ✅ |
| CredentialUnavailableException(String) | `shouldConstructWithMessage` | ✅ |
| CredentialUnavailableException(String, Throwable) | `shouldConstructWithMessageAndCause` | ✅ |
| CredentialUnavailableException 继承 RuntimeException | `shouldBeRuntimeException` | ✅ |

## 设计偏差对测试的影响

detail_v10.md §设计偏差说明 记录了实现采用 vault-first 顺序而非 task_v10 的 cache-first 顺序。该偏差不影响测试验证：NORMAL 状态下 `getCredential()` 优先查询 `credentialStore`，成功时写入缓存并 reset 失败计数，失败时递增计数并在 ≥5 次后切换至 BACKOFF。所有测试均基于实际实现行为编写，与 design 一致。

## 硬性约束满足

- 不修改编码 agent 的源码文件 ✅
- 基于行为契约编写，不测实现细节 ✅
- 每个行为契约至少一个正向用例 ✅
- 覆盖维度：正常路径、边界条件、错误路径、状态交互 ✅
- 用例独立，不依赖执行顺序 ✅
- 未创建新版本号文件 ✅

## 修订说明（v10 r3）

| 审查意见 | 修改措施 |
|---------|---------|
| [一般] `shouldExtendTtlOnReadInCacheOnly` 注释中 cap 描述（"2× original TTL = 20ms"）与实现 cap（600s）矛盾 | 移除错误的 cap 注释，更新为准确的描述（extend by 30s, cap 600s） |
| [一般] 测试使用 10ms TTL + 50ms 等待，仅验证自然过期未验证延长 | 改用 100ms TTL + 300ms 等待，验证存活超过原始 TTL 以证明延长生效；Caffeine 3.x 的 `getIfPresent()` 内部会触发 `afterRead` 回调，从而调用 `expireAfterRead` 扩展 TTL |
