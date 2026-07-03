# 计划审查报告（v21 r1）

## 审查结果
REJECTED

## 发现

### **[严重]** 状态模型与任务要求完全不一致
plan.md L603 描述为"基本状态机（UP/DOWN/RECOVERING）"，而 task_v21.md §1 明确要求 EndpointHealthState 枚举常量为 `CONNECTED`/`DEGRADED`/`UNAVAILABLE`。UP/DOWN/RECOVERING 是完全不同的状态模型，按此实现会导致状态转换逻辑全部错误。

### **[严重]** 关键方法签名缺失
plan.md 仅提及"健康探测抽象方法"，但 task_v21.md §2 定义了三个具体方法签名：
- `EndpointHealthState getState(String endpointId)`
- `boolean tryProbe(String endpointId)`
- `void recordCallResult(String endpointId, boolean success, long elapsedMs)`

且这些方法都是具体实现（非抽象），并包含完整的状态转换规则（连续慢调用/连续失败/累积失败阈值等）。计划未反映任何方法细节和转换规则。

### **[严重]** AbstractCapabilityExecutor 适配未纳入计划
task_v21.md §3 要求修改 `AbstractCapabilityExecutor.java` 行 359-363，将 `!"HEALTHY".equals(healthState)` 字符串比较替换为 `EndpointHealthState` 枚举比较，并增加 tryProbe 探测逻辑。plan.md 完全未提及此修改，这会导致编译失败（getState 返回类型从 String 改为 EndpointHealthState 后，现有代码无法编译）。

### **[严重]** 测试文件完全缺失
task_v21.md §4 要求：
- 新建 `EndpointHealthStateTest.java`（3 类覆盖）
- 新建 `ModelEndpointHealthManagerTest.java`（9 个测试场景含并发）
- 修改 `AbstractCapabilityExecutorTest.java`（~16 处 mock 适配）
- 修改 `DiscussionConclusionCapabilityExecutorTest.java`（~3 处）
- 修改 `TriageCapabilityExecutorTest.java`（~1 处）

plan.md 线路表 row 15 虽列出了测试文件，但详细计划部分（L602-605）只字未提测试工作。这违反了 requirement.md "所有代码需包含对应的单元测试"。

### **[一般]** 详细计划部分过于笼统
plan.md L602-605 详细计划仅 3 行描述，缺乏实施要点、文件变更操作类型、状态转换规则说明、线程安全策略等 task_v21.md 已明确的内容。与此前各轮计划的详细程度不一致。

## 修改要求

1. **修正状态模型**：将详细计划中的 UP/DOWN/RECOVERING 改为 CONNECTED/DEGRADED/UNAVAILABLE，与 task_v21.md §1 一致
2. **补充方法定义**：明确 getState()/tryProbe()/recordCallResult() 三个方法签名和完整状态转换规则（引述 task_v21.md §2 状态转换表）
3. **纳入 AbstractCapabilityExecutor 适配**：将 AbstractCapabilityExecutor.java 修改加入涉及文件清单，写明修改内容（行 359-363 String 比较改为枚举比较 + tryProbe 分支）
4. **补充完整测试规划**：明确列出 5 个测试文件的新建和修改范围，以及各测试的场景覆盖
