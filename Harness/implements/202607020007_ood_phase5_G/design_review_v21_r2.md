# 设计审查报告（v21 r2）

## 审查结果
REJECTED

## 发现

### **[一般]** 状态转换表副作用列与计数器更新规则不一致（3处）

设计文档包含两套对状态转换时计数器重置的描述：§行为契约「状态转换规则」表格的「副作用」列，以及「计数器更新规则」小节。两者在多处不一致，直接编码将产生歧义。

1. **CONNECTED→UNAVAILABLE**（行 135）：副作用列声明重置 `consecutiveSlowCalls, consecutiveSuccesses, cumulativeFailures`；但计数器规则声明进入 UNAVAILABLE 时还应重置 `consecutiveFailures`。

2. **DEGRADED→UNAVAILABLE**（行 139）：副作用列声明重置 `consecutiveSlowCalls, consecutiveSuccesses`；但计数器规则声明进入 UNAVAILABLE 时还应重置 `cumulativeFailures` 和 `consecutiveFailures`。

3. **DEGRADED→CONNECTED**（行 138）：副作用列声明重置 `consecutiveSlowCalls, consecutiveFailures, cumulativeFailures`；但计数器规则声明进入 CONNECTED 时应重置 `consecutiveSuccesses`（注：从 DEGRADED 恢复到 CONNECTED 时，`consecutiveSuccesses` 曾在 DEGRADED 状态累计，应当清零）。

期望修正方向：**统一以「计数器更新规则」为权威来源**，修正转换表中各条目的副作用列，确保每次状态转换的计数器重置清单与计数器规则完全一致。

### **[轻微]** 内部字段命名与任务规格不符

任务 `task_v21.md` §2 内部数据结构清单中指定字段名 `lastSlowCallThresholdMs`，但设计 `detail_v21.md` 行 57 使用 `slowCallThresholdMs`（少 `last` 前缀）。虽然这是内部实现细节，但会产生分支间的不必要认知偏差。建议统一为 `slowCallThresholdMs`（或按任务规格统一），并确保 Getter/Setter 命名 `getSlowCallThreshold`/`setSlowCallThreshold` 的对应关系不受影响。

### **[轻微]** DEGRADED 状态下 "慢成功调用" 的计数器行为未显式定义

行 140 的「其他」→ DEGRADED 案例仅描述"对应计数器按规则更新"。当 `DEGRADED + success=true + elapsed >= threshold` 时，`consecutiveSuccesses` 既不递增（因未满足 elapsed < 阈值条件）也不重置，保持旧值。这可能导致：`consecutiveSuccesses` 已在 2（差一次恢复），一次慢调用不改变它，再下一次快调用（即使间隔很长）直接使计数器到 3 并触发恢复。这不是明显违反规范，但存在语义歧义——通常慢调用应打断恢复连续计数。建议显式声明该场景下 `consecutiveSuccesses` 是否应重置为 0。

### **[轻微]** `consecutiveSlowCalls` 计数器重置规则的表述不完整

计数器规则写道"进入其他状态（非 CONNECTED）时重置"，但没有说明 **进入 CONNECTED 时**（从 DEGRADED 或 UNAVAILABLE 恢复）是否应重置。实际转换表在所有进入 CONNECTED 的条目中都包含了 `consecutiveSlowCalls` 重置，这表明设计意图是**在全部状态转换时都重置**。建议将计数器规则表述从"进入其他状态（非 CONNECTED）时重置"改为"状态转换时重置"，或补充"进入 CONNECTED 时也重置"，以消除不一致。

## 修改要求（REJECTED 时）

### 必须修正
- **问题 1（一般）**：转换表副作用列三处与计数器规则矛盾，编码时无法确定以哪套为准。修正方向：以计数器规则为准，补齐转换表中缺少的计数器重置项。

### 建议修正
- **问题 2（轻微）**：字段名与任务规格对齐。
- **问题 3（轻微）**：显式定义 DEGRADED 状态下 `elapsed >= threshold` 时 `consecutiveSuccesses` 的行为。
- **问题 4（轻微）**：完善 `consecutiveSlowCalls` 重置规则的表述，使之覆盖进入 CONNECTED 的场景。
