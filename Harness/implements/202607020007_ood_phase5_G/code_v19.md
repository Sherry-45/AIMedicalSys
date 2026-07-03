# 实现报告（v19）

## 概述

在 `ai-impl/metrics/` 包创建了 AI 底座指标采集体系的 5 个新文件，并修改了 `client/AiClientConfig.java` 添加 `@EnableAsync`。

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新建 | `metrics/AiCallLogEntity.java` | AI 调用日志 JPA @Entity，字段与 AiCallRecord 对等 |
| 新建 | `metrics/AiCallLogRepository.java` | Spring Data JPA Repository，含 countByCallTimeBefore 方法 |
| 新建 | `metrics/AiCallLogStats.java` | 月度聚合统计 JPA @Entity（骨架） |
| 新建 | `metrics/AiCallLogStatsRepository.java` | Spring Data JPA Repository，含 findByCapabilityIdAndStatMonthBetween 方法 |
| 新建 | `metrics/LoggingMetricsCollector.java` | @Service 实现 AiMetricsCollector，@Async 异步写入 AiCallLogEntity |
| 修改 | `client/AiClientConfig.java` | 类级别添加 @EnableAsync |

## 编译验证

`mvn compile -q` 通过，无错误。

## 设计偏差说明

无偏差。
