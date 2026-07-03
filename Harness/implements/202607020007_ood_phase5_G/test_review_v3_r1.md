# 测试审查报告（v3 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** `AbstractCapabilityExecutorTest.java:308` — `executeShouldSortStrategiesByOrder` 未能验证排序行为。测试设置了 `[lowPriority(order=10, returns false), highPriority(order=0, returns true)]`，无论是否按 getOrder() 排序都会得到"降级"结果，无法区分有序和未排序行为。必须重构为：两个策略均返回 true，但低 order 的策略在列表第二位，验证触发降级的是低 order 策略（而非插入顺序靠前的策略）。

- **[一般]** `test_v3.md` — 测试数量报告不一致。报告称"总用例数：29（原有 22 + 新增 7）"，但实际文件只有 28 个测试方法，且新增用例表格仅列出 6 条（缺少第 7 条）。需要修正报告中的计数。

- **[轻微]** `AbstractCapabilityExecutorTest.java` — 多个行为契约维度未覆盖：resolveTimeout 第四级兜底（全部为 null 回退 Duration.ofSeconds(30)）、SecurityContextHolder.getAuthentication() 为 null 时回退 "SYSTEM"、computeInputSummary 截断逻辑、elapsedInDoExecuteInternal 时序赋值、metricsCollector 非 null 时记录降级指标路径。

- **[轻微]** `AbstractCapabilityExecutorTest.java:136` — `executeShouldHandleTimeout` 未断言降级原因为 DegradationReason.TIMEOUT。

## 修改要求

### 1. 重构 `executeShouldSortStrategiesByOrder`
**位置**: `AbstractCapabilityExecutorTest.java:308~335`
**问题**: 测试无法验证排序是否生效。两个策略的返回值（false + true）组合导致无论是否排序结果都是"降级"。
**期望**: 将两个策略均设为返回 true，高 order 策略（如 order=10）放在列表首位，低 order 策略（order=0）放在次位。排序后低 order 策略应优先触发，测试需断言触发的是低 order 策略（比照 doDegrade 中的 sentinelReason 策略类名）。

### 2. 修正测试报告计数
**位置**: `test_v3.md`
**问题**: 报告称"29（原有 22 + 新增 7）"但文件实际 28 个方法，且表格仅列 6 条新增。
**期望**: 核实现有测试方法数量，同步修正报告总述和表格条目数。
