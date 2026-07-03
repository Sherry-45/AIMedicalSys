# 设计审查报告（v10 r8）

## 审查结果
REJECTED

## 发现

### [一般] BACKOFF 状态转换表与实现描述矛盾

状态转换表第 7 行（line 247-248）声明 BACKOFF 窗口期内"直接返回缓存数据，不查询 Vault"，但实现描述（line 144）写的是"返回 `Optional.empty()`"。两处直接矛盾——有缓存的凭据在 BACKOFF 期间到底能否使用？将导致编码时无法确定正确行为。

修正方向：当前实现（line 144）与 task_v10 line 71 一致（"直接返回 Optional.empty()"），应将状态转换表第 7 行与之一致，或统一为返回缓存数据。必须择一，不可两说。

### [轻微] NORMAL 状态实现顺序未记录与 task_v10 的偏离

task_v10 line 73 指定 NORMAL 为"查缓存 → 命中返回 | 未命中查 credentialStore"（缓存优先），而设计（line 150-156）实现为 vault-first（先查 credentialStore）。该偏离有合理理由（vault-first 是状态机正确运转的前提），但设计未在任何"设计偏差说明"中记录此偏离。

修正方向：在文档末尾新增"设计偏差说明"小节，解释 vault-first 的理由及与 task 的关系。

### [轻微] NORMAL 实现引用未定义的 vaultTimeout 标志位

line 153 实现描述中"Vault 查询超时（由 `vaultTimeout` 标志位驱动）"引用了一个在类字段定义（line 95-99）中不存在的 `vaultTimeout`。该字段既不属于类字段、也不在构造器中初始化。

修正方向：删除 `vaultTimeout` 引用，或明确定义该字段并说明其使用方式；说明该分支当前仅通过 `simulateVaultTimeout()` 在测试中触发。

### [轻微] simulateVaultTimeout 无条件 NORMAL→CACHE_ONLY 忽略缓存条件

`simulateVaultTimeout()` 第 2 步（line 123）无条件将 NORMAL 切至 CACHE_ONLY，但状态转换表（line 240）要求该转换仅在"Caffeine 缓存中有该 endpointId 的旧凭据"时发生。

修正方向：补充该方法的测试契约 precondition——调用者必须先确保凭据已缓存，或注明该方法同时承载了"模拟超时"和"强制进入 CACHE_ONLY"的双重语义。

### [轻微] probeVault() 实现未满足接口语义

接口（line 43）语义为"触发一次主动探测，强制查询 Vault 以尝试恢复 NORMAL 状态"，但 `DefaultCredentialProvider` 实现（line 121）仅返回 true，未尝试重置 state 或 consecutiveFailures。在 CACHE_ONLY 或 BACKOFF 状态下调用 `probeVault()` 无法触发恢复。

修正方向：在 `probeVault()` 中添加：若非 NORMAL，则原子重置 `state=NORMAL`、`consecutiveFailures=0`、`backoffUntil=0`，返回 true。在内存实现中，probe 始终成功。
