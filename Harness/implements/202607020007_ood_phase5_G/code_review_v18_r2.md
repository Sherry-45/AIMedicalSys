# 代码审查报告（v18 r2）

## 审查结果
REJECTED

## 发现
- **[一般]** `Experiment.java:30-32` — `@OneToMany` on `groups` 字段缺少 `@OrderBy`（或 `@OrderColumn`）注解。`HashBucketExperimentManager.assign()` 中的累加百分比算法（cumulative > hash）严重依赖 groups 的迭代顺序。没有 JPA 层级的显式排序保证时，Hibernate 在不同部署环境或数据库状态下可能以不确定顺序返回分组，导致哈希分桶结果不正确（同一用户可能因顺序不同落入错误分组）。设计文档未明确要求分组排序，但算法隐含了顺序依赖性，实现层应主动保证。

- **[轻微]** `HashBucketExperimentManager.java:85-88` — 多实验场景下使用 `stream().sorted(...).collect(Collectors.toList()).get(0)` 构建完整排序列表后取首元素，效率较低。推荐用 `stream().min(Comparator.comparing(Experiment::getStartTime))` 替代，O(n) 单趟查找即可。

- **[轻微]** `HashBucketExperimentManagerTest.java:56-64` — `shouldAssignToBucketByHash` 测试仅断言 groupId 非 null 且为二者之一，未验证具体哈希映射的确定性。虽然 `shouldMatchCumulativePercentage` 已覆盖具体映射，但本测试过于松弛，建议明确为确定的 session/group 断言。

## 修改要求（仅 REJECTED 时）

### 一般问题

**位置**：`Experiment.java:30-32`
```java
@OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL,
           orphanRemoval = true, fetch = FetchType.EAGER)
private List<ExperimentGroup> groups;
```
**问题**：groups 列表没有排序保证。`assign()` 的累加百分比算法 `cumulative += group.getPercentage(); if (cumulative > hash)` 完全依赖分组遍历顺序来决定命中哪个分组。若 JPA 以非确定顺序返回 groups，同一 sessionId 可能在不同请求中落入不同的实验分组，违反分桶一致性约定。

**期望修正**：增加 `@OrderBy("id ASC")` 或 `@OrderColumn(name = "group_order")` 来显式保证 groups 的固定迭代顺序。

### 轻微问题（可选修正）

**位置**：`HashBucketExperimentManager.java:85-88`
**期望修正**：替换为 `experiments.stream().min(Comparator.comparing(Experiment::getStartTime))` 以避免全量排序。

**位置**：`HashBucketExperimentManagerTest.java:56-64`
**期望修正**：使用已知哈希值的 session（如单字符字符串）执行具体 group 断言，如 `assertEquals("groupA", result.getGroupId())`。
