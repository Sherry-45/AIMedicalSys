# 设计审查报告（v21 r1）

## 审查结果
REJECTED

## 发现

### **[严重]** — `AbstractCapabilityExecutor` 适配伪代码中 `canProbe` 变量作用域错误导致编译失败

**位置**：§行为契约「AbstractCapabilityExecutor 适配」伪代码行 136-154

**问题描述**：
```java
if (healthState == EndpointHealthState.UNAVAILABLE) {
    boolean canProbe = endpointHealthManager.tryProbe(routeResult.getEndpointId());
    if (!canProbe) {
        return doDegrade(...);
    }
    // canProbe=true: ...
}
...
long totalTimeoutMs = resolveTimeout(capabilityId).toMillis();
if (healthState == EndpointHealthState.UNAVAILABLE && canProbe) {  // ← 编译错误
    totalTimeoutMs = totalTimeoutMs / 2;
}
```
`canProbe` 在 `if (healthState == UNAVAILABLE)` 块内声明，作用域限于该块；第二处 `if` 在块外引用 `canProbe`，Java 编译器将报"cannot find symbol"错误。

设计笔记（line 162）已承认此问题（"注意 `canProbe` 变量需要提升作用域到 if 块外部"），但伪代码未做任何修正。直接按此伪代码编码将产生不可编译的代码。

**期望修正方向**：将 `boolean canProbe` 声明提升到外层作用域（在第一个 if 之前声明并初始化为 false），或将 timeout 减半逻辑移入 UNAVAILABLE 分支内部。

---

### **[一般]** — 自动注册机制缺乏线程安全保障

**位置**：§行为契约「自动注册规则」行 100-101

**问题描述**：
设计仅说 `tryProbe` 和 `recordCallResult` "首次传入新 `endpointId` 时，自动注册（同 `getState`）"，但未明确指定使用 `ConcurrentHashMap.computeIfAbsent` 等原子方法。高并发下两个线程同时首次调用同一个新 `endpointId` 时：
1. 可能各自创建不同的 `EndpointState` 实例
2. 或出现部分初始化的 `EndpointState` 被读取

设计在 §错误处理 中提到 `ConcurrentHashMap` 保证映射查找安全，但未说明自动注册使用 `computeIfAbsent`（而非 get/putIfAbsent 复合操作）。

**期望修正方向**：明确指定自动注册（包括 `getState`、`tryProbe`、`recordCallResult` 中的首次访问）统一使用 `ConcurrentHashMap.computeIfAbsent` 实现原子注册。

---

### **[轻微]** — `cumulativeFailures` 重置规则内部不一致

**位置**：§行为契约「状态转换规则」行 107 与「计数器更新规则」行 120

**问题描述**：
- 状态转换表 CONNECTED→UNAVAILABLE 栏（行 107）："重置 consecutiveSlowCalls, consecutiveSuccesses; **不重置** cumulativeFailures"
- 计数器更新规则（行 120）："cumulativeFailures：仅在 DEGRADED 状态下 success=false 时递增；进入 CONNECTED 或 UNAVAILABLE 时**重置**"

"不重置"与"重置"矛盾。虽然在 CONNECTED→UNAVAILABLE 场景下 `cumulativeFailures` 恒为 0（因它仅在 DEGRADED 状态下递增），实际不影响运行时正确性，但设计文档应消除这一内部矛盾。

**期望修正方向**：统一为"重置 cumulativeFailures"（与计数器规则一致），或删除该行引用（因为 cumulativeFailures 在 CONNECTED 状态下始终为 0）。

## 修改要求

1. **[严重]** 修正 `canProbe` 变量作用域：提升声明到外层或将 timeout 减半移入 if 块
2. **[一般]** 明确自动注册使用 `ConcurrentHashMap.computeIfAbsent`
3. **[轻微]** 统一 `cumulativeFailures` 重置描述
