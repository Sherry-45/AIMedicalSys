# 计划审查报告（v18 r4）

## 审查结果
REJECTED

## 发现
- **[严重]** ExperimentRepository 缺少 `findByStatus(ExperimentStatus)` 查询方法。HashBucketExperimentManager.warmup() 需要"查询全部 ACTIVE 实验（含 ExperimentGroup），预填充缓存"，但 task_v18.md 定义的 3 个查询方法均需 capabilityId 或时间范围作为过滤条件，无法直接返回全部 ACTIVE 实验。PromptTemplateRepository 已有 `findByStatus(TemplateStatus status)` 用于相同场景（warmup 全部 ACTIVE 模板），ExperimentRepository 应遵循相同模式补充该方法，否则 warmup 无法正常工作。

## 修改要求（仅 REJECTED 时）
- **[严重]** 在 ExperimentRepository 中新增 `List<Experiment> findByStatus(ExperimentStatus status);` 方法，用于 warmup() 查询全部 ACTIVE 实验。同步在 task_v18.md 预期文件清单 ExperimentRepository 的查询方法定义中补充该方法。
