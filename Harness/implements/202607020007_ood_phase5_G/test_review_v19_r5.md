# 测试审查报告（v19 r5）

## 审查结果
REJECTED

## 发现

- **[一般]** `test_v19.md` 汇总表 — 汇总数据不一致：总结显示 "Total test methods | 27"、"Passed | 26"，但详细列表合计 30 个测试方法（LoggingMetricsCollectorTest 12 + AiCallLogEntityTest 5 + AiCallLogStatsTest 4 + AiCallLogRepositoryTest 4 + AiCallLogStatsRepositoryTest 5 = 30）。Passed 26 与 Total 27 差额 1，与详细列表 30 差额 4，数据不可靠。

- **[一般]** `LoggingMetricsCollectorTest.shouldHandleNullRecordGracefully` — 设计契约第 305 行明确要求 record(null) 应 "WARN 日志 + return"，测试仅验证 `verifyNoInteractions`（不写库），未验证 WARN 日志输出，存在行为覆盖缺口。

- **[轻微]** `LoggingMetricsCollectorTest.shouldNotThrowWhenConcurrentlyCalled` — 仅验证并发调用不抛异常，未验证 save() 被正确调用 5 次或数据完整性。因 record() 已 catch 全部异常，即使存在并发缺陷也能通过。

## 修改要求

1. **test_v19.md 汇总数据**：修正 Total test methods 及 Passed 数量使其与详细列表一致，或说明 30 个方法中仅 27 计入统计的原因。

2. **shouldHandleNullRecordGracefully**：补充对 WARN 日志的验证（如 Mockito verify 确认 log.warn() 被调用，或 LoggerAppender 断言日志内容），确保契约中 "WARN 日志" 要求被覆盖。
