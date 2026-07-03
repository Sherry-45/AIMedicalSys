# 计划审查报告（v18 r1）

## 审查结果
REJECTED

## 发现

### **[一般] 1. Plan road map 表格行13 "涉及文件"列不完整**

Road map row 13 仅列出 6 个文件（`ExperimentManager.java, HashBucketExperimentManager.java, Experiment.java, ExperimentGroup.java, ExperimentRepository.java, ExperimentAssignment.java`），但 task_v18.md 定义的完整实现范围包括 10 个文件。缺少以下 4 个文件：
- `ExperimentStatus.java`（Enum，编译期必须有）
- `ExperimentChangedEvent.java`（事件类，编译期必须有）
- `ExperimentAssignmentTest.java`（测试文件）
- `HashBucketExperimentManagerTest.java`（测试文件）

对比前序 row 12 完整列出 14 个涉及文件（含枚举、事件类、测试、pom 修改等全部文件），row 13 的缩略列法与既有模式不一致。可能导致仅阅读 plan.md 的参与者遗漏关键文件（尤其是编译期必须的 Enum 和 Event 类），影响任务拆分的完整性。

**修正方向**：将 row 13 "涉及文件" 列补齐为 10 个文件，与 task_v18.md §预期文件清单保持一致。

---

### **[一般] 2. Experiment.groups @OneToMany LAZY 加载策略未明确，存在运行时风险**

HashBucketExperimentManager 在 Caffeine 缓存（无 `@Transactional` 上下文）中需要访问 `Experiment.groups` 集合进行分桶判定。标准 JPA `@OneToMany` 默认 `FetchType.LAZY`，在非事务环境中访问 groups 会抛出 `LazyInitializationException`。

task_v18.md 虽提及"查询全部 ACTIVE 实验（含 ExperimentGroup）"，但未指定 fetch 策略。若实现侧不主动处理（如改为 `FetchType.EAGER`、使用 `@EntityGraph` 或 `JOIN FETCH`），将在 `@PostConstruct` 预热或缓存命中时发生运行时异常。

**修正方向**：在计划中明确 `Experiment.groups` 的获取策略——建议采用 `FetchType.EAGER`（因 Experiment 始终与 groups 一起使用）或在 repository 查询方法上标注 `@EntityGraph(attributePaths = "groups")`，确保缓存中的数据已初始化完整对象图。

---

### **[轻微] 3. "8 个类型 + 1 个事件类" 计数描述可优化**

R21 NEW Task 13 描述写"8 个类型 + 事件类 + 2 个测试文件"，但事件类（ExperimentChangedEvent）已包含在"8 个类型"计数内，单独列出造成计数歧义。总文件数 10 = 8 生产文件 + 2 测试文件。

**修正方向**：可改为"8 个类型（含 1 个事件类）+ 2 个测试文件"以消除歧义。

## 修改要求

对上述 **[一般]** 问题 1 和 2 进行修正后重新提交审查。
