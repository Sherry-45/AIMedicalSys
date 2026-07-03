# 计划审查报告（v18 r2）

## 审查结果
REJECTED

## 发现

- **[一般]** HashBucketExperimentManager.assign() 对同一 capabilityId 存在多个 ACTIVE 实验时的选择策略未定义。`findByCapabilityIdAndStatus` 无 ORDER BY，结果顺序不确定，当多个 ACTIVE 实验存在时 assign() 可能返回不一致的分组结果。计划应明确约定：要么假设同一 capability 同时最多一个 ACTIVE 实验（由管理端保证），要么指定选择策略（如按 endTime 降序取最近的一个）。

- **[轻微]** ExperimentGroup 中 `@ManyToOne experiment` 字段未指定 fetch 策略，默认 EAGER 将与 Experiment.groups 的 `fetch = FetchType.EAGER` 形成双向 EAGER。虽 Hibernate 一级缓存可处理循环引用，但会导致不必要的 SQL 联表查询（当加载 Experiment 时 groups 被 EAGER 加载，每个 group 又 EAGER 加载 experiment）。建议明确指定 `@ManyToOne(fetch = FetchType.LAZY)`。
