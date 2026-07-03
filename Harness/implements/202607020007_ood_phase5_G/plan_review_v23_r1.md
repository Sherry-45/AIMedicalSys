# 计划审查报告（v23 r1）

## 审查结果
REJECTED

## 发现

### 1. **[严重]** 路线表 row 21 涉及文件列错误包含 LocalRuleFallback.java

计划路线表 row 21 的"涉及文件"列为 `LocalRuleFallback.java, PrescriptionLocalRuleFallback.java`，任务名称为"LocalRuleFallback + PrescriptionLocalRuleFallback"，暗示 LocalRuleFallback 接口需要修改。

但 task_v23.md §4 明确要求 **"不修改的文件: LocalRuleFallback.java — 接口不变，保持 `<T,R> R fallback(T)` 签名"**。按本计划路线表惯例（如 task 12/15 将修改文件悉数列出），将 LocalRuleFallback.java 列入涉及文件列会误导实施者对其进行不必要的修改。

### 2. **[一般]** 路线表 row 21 缺少测试文件 PrescriptionLocalRuleFallbackTest.java

task_v23.md §2 要求新建 `PrescriptionLocalRuleFallbackTest.java`（9 个测试方法覆盖正常/异常/边界场景），但路线表 row 21 "涉及文件"列未列出该测试文件。

### 3. **[一般]** R27 NEW 上下文依赖描述与 task_v23.md 实际设计不一致

plan.md R27 NEW 上下文声称"直接依赖 Task 20 的 JSON 解析能力做结构化数据解析"。但 task_v23.md 定义的 PrescriptionLocalRuleFallback 基于 hardcode 内存数据（已知配伍禁忌表、剂量范围映射表、成分归属映射表等），完全不涉及 JSON 解析。该描述不准确但不影响实施者理解（因 task_v23.md 已提供完整规则定义）。

## 修改要求

1. **路线表 row 21 修正**：任务名称改为"PrescriptionLocalRuleFallback（新建）"，涉及文件列移除 `LocalRuleFallback.java`，补充 `PrescriptionLocalRuleFallbackTest.java`，确保与 task_v23.md §4 不修改 LocalRuleFallback 的指令一致。
2. **R27 NEW 上下文修正**：移除"直接依赖 Task 20 的 JSON 解析能力做结构化数据解析"描述，改为准确说明——基于 hardcode 内存数据实现 5 项预设规则检查。
