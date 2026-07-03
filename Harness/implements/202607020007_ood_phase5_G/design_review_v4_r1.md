# 设计审查报告（v4 r1）

## 审查结果
REJECTED

## 发现

- **[严重]** CircuitBreakerDegradationStrategy NPE：CLOSED→OPEN 转换时 `circuitDataMap[capabilityId]` 未初始化
  当 `shouldDegrade` 首次检测到某能力的失败率 ≥ 阈值时，执行 `circuitDataMap[capabilityId].circuitOpenedAt = System.currentTimeMillis()`。但 `CircuitData` 实例从未预初始化，`ConcurrentHashMap.get()` 返回 null，导致 NullPointerException。
  期望：在初始化 `stateMap` 条目的同时初始化 `circuitDataMap` 条目（或使用 `computeIfAbsent`，或改用合并的数据结构）。

- **[严重]** 全局 `probeLock` 破坏 "每个 capabilityId 独立维护熔断状态" 的设计要求
  单个 `AtomicBoolean probeLock` 对所有能力共享，导致能力 A 在 HALF_OPEN 下进行探测时，能力 B 的 HALF_OPEN 探测被错误阻塞。与 §职责 中明确的 "每个 capabilityId 独立维护熔断状态" 直接矛盾。
  期望：将 `probeLock` 改为按 capabilityId 独立管理（如 `ConcurrentHashMap<String, AtomicBoolean>`，或直接在 `CircuitData` 中增加 `probeLock` 字段）。

- **[一般]** HALF_OPEN 状态下过早转入 CLOSED
  `shouldDegrade` 在 HALF_OPEN 中获取 probeLock 后立即将状态设为 CLOSED（`stateMap[capabilityId] = CLOSED`），再返回 false 放行探测请求。这产生一个竞争窗口：在探测请求实际完成之前，另一线程的 `shouldDegrade` 看到 CLOSED 状态，检查 `getFailureRate()` 发现仍高于阈值，立即重新打开熔断器，使探测失去意义。
  期望：`shouldDegrade` 在 HALF_OPEN 中不应改变状态，仅借 probeLock 决定是否放行；状态变更（CLOSED 或回到 OPEN）全部交由 `recordProbeResult` 处理。

- **[一般]** `stateMap.get(capabilityId)` 不会自动初始化
  设计伪代码注释 "首次自动初始化为 CLOSED"，但 `ConcurrentHashMap.get()` 对于不存在的 key 返回 null，不会自动初始化。若 switch 语句未处理 null，则首次访问任意能力时状态为 null 而非 CLOSED。
  期望：明确使用 `computeIfAbsent`（如 `stateMap.computeIfAbsent(capabilityId, k -> new AtomicReference<>(CLOSED))`）而非 `get`。

- **[轻微]** `probeLock` 异常残留风险
  `probeLock.compareAndSet(false, true)` 成功后，若在 `stateMap.put` 或 return 之前发生异常，probeLock 将永久卡在 true 状态，导致所有能力的 HALF_OPEN 探测永久阻塞。
  期望：考虑 try-finally 保证 probeLock 释放，或至少记录该风险并在编码时注意。

- **[轻微]** `CircuitData` 字段与任务描述不一致
  任务文件要求 `CircuitData` 维护 "失败计数和最近失败时间"，但设计中仅包含 `circuitOpenedAt` 一个字段。虽然当前功能不依赖更多字段，但设计描述与任务文件存在偏差。

## 修改要求（仅 REJECTED 时）

1. **[严重]** 修复 `circuitDataMap` 未初始化导致的 NPE
2. **[严重]** 将全局 `probeLock` 改为 per-capability 管理
3. **[一般]** 修正 HALF_OPEN 状态机的时序逻辑——状态变更统一由 `recordProbeResult` 管理
4. **[一般]** 明确 `stateMap` 使用 `computeIfAbsent` 初始化策略
5. **[轻微]** 处理 `probeLock` 异常残留风险
6. **[轻微]** 补充 `CircuitData` 字段或同步更新任务描述
