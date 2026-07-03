# 测试报告（v26）

## 概述

基于详细设计 v26 的 5 条行为契约编写单元测试，覆盖 `thinAdapterPerCapabilityConfig` 字段为 null 时的安全降级行为。

## 行为契约覆盖

| 契约 | 测试用例 | 文件 |
|------|---------|------|
| 1. `resolveTimeout()` 在 `thinAdapterPerCapabilityConfig` 为 null 时跳过 thin-adapter 配置查找，回退到 `thinAdapterTimeout` 或默认 30 秒 | `resolveTimeoutShouldFallbackToThinAdapterTimeoutWhenFieldIsNull`：field=null, thinAdapterTimeout=15s → 使用 15s 执行成功 | `AbstractCapabilityExecutorTest.java` |
| 1. （续） | `resolveTimeoutShouldFallbackToDefault30sWhenFieldAndThinAdapterTimeoutAreNull`：field=null, thinAdapterTimeout=null → 使用默认 30s 执行成功 | `AbstractCapabilityExecutorTest.java` |
| 1. （续） | `resolveTimeoutShouldUseCapabilityTimeoutWhenThinAdapterPerCapabilityConfigIsNull`：field=null, capabilityTimeoutConfig 有值 → 使用 capabilityTimeoutConfig | `AbstractCapabilityExecutorTest.java` |
| 2. `resolveThinAdapterTimeout()` 在 `thinAdapterPerCapabilityConfig` 为 null 时直接返回 `thinAdapterTimeout.toMillis()` | `shouldSucceedWhenThinAdapterPerCapabilityConfigIsNull`（6 个文件各 1 个）：field=null, service 有效 → 成功委托 | 6 个薄适配器测试文件 |
| 2. （续） | `shouldDegradeOnTimeoutWhenThinAdapterPerCapabilityConfigIsNull`（6 个文件各 1 个）：field=null, service 延迟 → 超时降级含 "ThinAdapterTimeout" | 6 个薄适配器测试文件 |
| 3. per-capability 映射不包含未显式配置的 key | 已有 `initShouldSucceedWithValidConfiguration` 断言 `size()==0` 和 `assertNull` | `AiPlatformConfigTest.java`（v25 已修正，无需新增） |
| 4. 6 个薄适配器测试 `createExecutor()` 第 8 参传空 AtomicReference 而非 null | 已有构造器参数修正 + 新增 null 场景测试 | 6 个薄适配器测试文件 |

## 新增测试用例

### AbstractCapabilityExecutorTest.java（+3 个用例）

1. **`resolveTimeoutShouldFallbackToThinAdapterTimeoutWhenFieldIsNull`**
   - 场景：`thinAdapterPerCapabilityConfig` 字段为 null，`thinAdapterTimeout` 为 15s
   - 预期：执行成功（使用 thinAdapterTimeout=15s 作为超时）

2. **`resolveTimeoutShouldFallbackToDefault30sWhenFieldAndThinAdapterTimeoutAreNull`**
   - 场景：`thinAdapterPerCapabilityConfig` 字段为 null，`thinAdapterTimeout` 也为 null
   - 预期：执行成功（使用默认 30s 作为超时）

3. **`resolveTimeoutShouldUseCapabilityTimeoutWhenThinAdapterPerCapabilityConfigIsNull`**
   - 场景：`thinAdapterPerCapabilityConfig` 字段为 null，`capabilityTimeoutConfig` 有对应 capability 的配置
   - 预期：执行成功（使用 capabilityTimeoutConfig 中的值）

### 6 个薄适配器测试文件（每个 +2 个用例）

每个文件新增：

1. **`shouldSucceedWhenThinAdapterPerCapabilityConfigIsNull`**
   - 场景：`thinAdapterPerCapabilityConfig` 传 null，service 有效
   - 预期：委托成功，返回成功结果（`resolveThinAdapterTimeout` 安全降级到 `thinAdapterTimeout`）

2. **`shouldDegradeOnTimeoutWhenThinAdapterPerCapabilityConfigIsNull`**
   - 场景：`thinAdapterPerCapabilityConfig` 传 null，service 延迟 5s，thinAdapterTimeout=50ms
   - 预期：超时降级，fallback reason 含 "ThinAdapterTimeout"

## 覆盖维度

| 维度 | 说明 |
|------|------|
| 正常路径 | field=null + 有效 service → 委托成功 |
| 边界条件 | field=null + thinAdapterTimeout=null → 默认 30s |
| 错误路径 | field=null + service 超时 → 降级 |
| 状态交互 | field=null 时 `capabilityTimeoutConfig` 仍优先命中 |

## 总新增用例数

3（AbstractCapabilityExecutorTest）+ 6×2（薄适配器）= **15 个**
