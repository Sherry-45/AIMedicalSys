# 设计审查报告（v21 r3）

## 审查结果
APPROVED

## 发现

- **[轻微]** `tryProbe` 与 `recordCallResult(success=false)` 均更新 `lastProbeTime`。`tryProbe` 返回 true 时已设 `lastProbeTime = now`，若探测调用持续数秒后失败，`recordCallResult` 再次设 `lastProbeTime = now+delta`，使 30 秒探测窗口从调用结束时重新计时。行为正确且有合理意图（避免失败后立即重探），但此双重更新策略未在设计文档中显式说明，读者可能误以为冗余。

- **[轻微]** DEGRADED 状态下缺少告警日志。OOD §3.2 描述 DEGRADED 为"仍尝试调用但上报告警"，设计允许 DEGRADED 正常进入 LLM 调用，但未安排在 `healthState == DEGRADED` 时输出 WARN 日志。不影响状态机正确性，但降低运维可观测性。

- **[轻微]** `slowCallThresholdMs` 字段名与 task_v21.md 第 37 行 `lastSlowCallThresholdMs` 不一致。设计已记录选择理由（`last` 前缀仅适用于时间戳字段），但任务文件未同步，可能造成编码参考时的短暂困惑。

## 修改要求（仅 REJECTED 时）
（无）
