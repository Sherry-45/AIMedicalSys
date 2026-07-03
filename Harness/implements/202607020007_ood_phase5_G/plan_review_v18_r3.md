# 计划审查报告（v18 r3）

## 审查结果
REJECTED

## 发现

### **[一般] 1. 路线表 row 13 "涉及文件"列仍不完整**

plan.md 路线表 row 13 的"涉及文件"列仅列出 6 个文件（`ExperimentManager.java, HashBucketExperimentManager.java, Experiment.java, ExperimentGroup.java, ExperimentRepository.java, ExperimentAssignment.java`），但 task_v18.md §预期文件清单定义的完整实现范围包括 10 个文件。缺少：
- `ExperimentStatus.java`（枚举，编译期必须有）
- `ExperimentChangedEvent.java`（事件类，编译期必须有）
- `ExperimentAssignmentTest.java`（测试文件）
- `HashBucketExperimentManagerTest.java`（测试文件）

对比前序 row 12 完整列出 14 个涉及文件（含枚举、事件类、测试文件等全部文件），row 13 的缩略列法与既有模式不一致。R22 r1 修订虽声明已补齐，但实际路线表未更新。仅阅读路线表的参与者会遗漏关键文件。

**修正方向**：将 row 13 "涉及文件"列扩充为 10 个文件，与 task_v18.md §预期文件清单完全一致。

---

### **[一般] 2. hash 计算可能产生负值，违反 [0,1000) 区间约定**

task_v18.md HashBucketExperimentManager 小节定义：
```
assign() 实现：userId/sessionId.hashCode() % 1000 → [0,1000) 区间
```

Java 的 `String.hashCode()` 可返回负数（int 范围），而 `%` 运算符保留符号。当 hashCode 为负时，`hashCode() % 1000` 结果为 `[-999, -1]`，不落在 `[0,1000)` 区间内。`Math.abs(hashCode())` 对 `Integer.MIN_VALUE` 也会失效。

这将导致分桶逻辑无法命中任何分组（所有负 hash 均走 default 路径），且测试中硬编码的正 hash 值无法覆盖该场景。

**修正方向**：将 hash 计算改为 `Math.floorMod(hashCode(), 1000)`（结果恒为非负且均匀分布），或 `(hashCode() & Integer.MAX_VALUE) % 1000`。同步在测试中补充负 hashCode 的边界用例（如 `hashCode() = -1, -1000, Integer.MIN_VALUE` 时仍落在 [0,1000) 区间）。

---

### **[轻微] 3. dbExceptionShouldReturnDefault 测试的执行路径需明确**

测试规划中 `dbExceptionShouldReturnDefault` 的意图是"DB 异常 → 返回 createDefault()"。但 HashBucketExperimentManager 的 @PostConstruct 预热已在启动时填充缓存，此后 `assign()` 读取缓存而非 DB。若要触发 DB 异常路径，测试需确保缓存未命中（如等待 expireAfterWrite=5min 过期，或直接 mock Repository）。

当前计划未明确该测试如何进入 DB 异常路径，可能在实际实现时发现无法正常触发。

**修正方向**：在测试规划中说明触发方式——建议在预热前 mock Repository 抛异常，或在预热后先通过 Event 清空缓存再触发 cache miss。

## 修改要求

对上述 **[一般]** 问题 1 和 2 进行修正后重新提交审查。
