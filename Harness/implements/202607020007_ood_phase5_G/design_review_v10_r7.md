# 设计审查报告（v10 r7）

## 审查结果
REJECTED

## 发现

- **[严重]** DefaultCredentialProvider 状态机实现描述与状态转换表严重矛盾。状态转换表（第 212-222 行）列出了两条 NORMAL→CACHE_ONLY 的转换路径，但实现描述（第 139-142 行）中 NORMAL 状态的逻辑是"查缓存→查 credentialStore→递增 consecutiveFailures→≥5 次时直接切至 BACKOFF"，全程未出现 CACHE_ONLY 转换。这意味着：
  - 实现者无法确定正确的行为：应遵循转换表进入 CACHE_ONLY？还是遵循实现描述递增失败计数并跳过 CACHE_ONLY 直接进入 BACKOFF？
  - CACHE_ONLY 状态在实现中实际上仅能从 simulateVaultTimeout() 人工置入，不会被 NORMAL 自动转换进入
  - 最终验收测试"验证状态机转换（NORMAL→CACHE_ONLY→BACKOFF）"在实现描述下无法通过
  - 修正方向：在实现描述中明确 NORMAL 状态下 Vault 查询超时的处理逻辑（如何在 NORMAL 内部判定进入 CACHE_ONLY vs 递增失败计数），使实现与状态转换表一致

- **[一般]** EndpointRateLimiter 配置机制与 task_v10 要求不一致。task_v10 第 87 行要求通过 @Value 注入属性前缀 `ai.rate-limiting.endpoints.{endpointId}.{permits-per-second / max-burst-seconds / queue-wait-millis}`，但设计第 173 行使用 `ai.rate-limiting.default.permits-per-second` 全局默认值，并将端点独立配置全部推迟至 Task 18；
  - `max-burst-seconds` 和 `queue-wait-millis` 两个配置维度在设计中被完全忽略，未实现 @Value 注入
  - 修正方向：按 task_v10 要求使用 `endpoints.{endpointId}` 前缀实现 @Value 注入，或将当前简化方案与 Task 18 的交接点描述得更精确（如注明当前全局默认值仅为过渡值，Task 18 会替换为 per-endpoint 配置）

- **[一般]** 状态转换表第 2 行（NORMAL | Vault 查询超时缓存无数据 → CACHE_ONLY）与 OOD 设计文档 §3.2（第 1991 行）不符。OOD 文档规定此情况应保持 NORMAL 并抛出 CredentialUnavailableException，而非进入 CACHE_ONLY。进入 CACHE_ONLY 后若无缓存数据，getCredential() 持续返回空，下次调用不会重试 Vault，降低了恢复速度。修正方向：恢复为 OOD 文档定义的 NORMAL→NORMAL + 异常行为，或在设计文档中显式说明偏离 OOD 的理由。
