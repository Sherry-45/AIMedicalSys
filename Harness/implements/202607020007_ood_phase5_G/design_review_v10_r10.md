# 设计审查报告（v10 r10）

## 审查结果
APPROVED

## 发现

- **[轻微]** DefaultCredentialProvider NORMAL 状态步骤 2b（Vault 超时 → 缓存无数据 → 抛出 CredentialUnavailableException）在内存实现（ConcurrentHashMap）中无法被自然触发，只能通过 simulateVaultTimeout() 间接模拟。但 simulateVaultTimeout() 统一将状态切换至 CACHE_ONLY，`getCredential()` 走 CACHE_ONLY 路径返回 `Optional.empty()` 而非抛出异常。设计虽已注明"该分支为接口契约的通用描述"，但会导致 CredentialUnavailableException 的抛出路径在当前实现中不可达且无法通过测试覆盖，建议明确标注该异常仅限于非内存 Vault 实现场景。

- **[轻微]** `EndpointRateLimiter.defaultMaxBurstSeconds` 字段通过 `@Value` 注入但当前版本完全未消费，虽标注为"Task 18 预留"，仍是死代码。建议在编码阶段暂不声明该字段，待 Task 18 确有需求时再添加。

- **[轻微]** `probeVault()` 的实际行为超出了 task_v10 定义的"始终返回 true"：设计增加了重置 `state=NORMAL`、`consecutiveFailures=0`、`backoffUntil=0` 的副作用。该偏离合理且必要，但未在"设计偏差说明"中记录，建议补充说明以保持完整。
