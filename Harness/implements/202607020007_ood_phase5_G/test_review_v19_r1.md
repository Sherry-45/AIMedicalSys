# 测试审查报告（v19 r1）

## 审查结果
REJECTED

## 发现

- **[严重]** `test_v19.md` — 测试报告文件仅含标题行，未包含任何测试执行结果（通过/失败/总数），导致测试交付物缺乏可追溯性，无法确认测试是否真正执行
- **[一般]** `LoggingMetricsCollectorTest.java` — 设计测试规划中明确列出的 `shouldNotThrowWhenAsyncExecuted` 用例（设计文档第358行）未在测试代码中实现
- **[一般]** `AiCallLogEntityTest.java` — 设计测试规划中明确列出的 `jpaAnnotationsShouldBePresent` 用例（设计文档第368行）未在测试代码中实现
- **[一般]** `AiCallLogStatsTest.java` — 设计测试规划中明确列出的 `jpaAnnotationsShouldBePresent` 用例（设计文档第377行）未在测试代码中实现
- **[一般]** — `AiCallLogRepository.countByCallTimeBefore(LocalDateTime)` 和 `AiCallLogStatsRepository.findByCapabilityIdAndStatMonthBetween(String,String)` 两个自定义查询方法无任何测试覆盖
- **[轻微]** `LoggingMetricsCollectorTest.java:148` — `shouldParsePromptVersionSafely` 中使用 `reset(repository)` 跨多个数据点共享同一个测试方法，使测试逻辑脆弱、维护困难，建议拆分为三个独立测试方法

## 修改要求

1. **`test_v19.md`** — 必须补充完整的测试执行报告，包含：测试总数、通过数、失败数，以及每个测试方法的执行结果（通过/失败/跳过）和断言结论。
2. **`LoggingMetricsCollectorTest.java`** — 应实现 `shouldNotThrowWhenAsyncExecuted` 测试用例（通过 Spring `@SpringBootTest` 集成测试验证 `@Async` 在多线程下不抛异常），或在此版本中明确说明设计变更原因并更新设计文档。
3. **`AiCallLogEntityTest.java`** — 应实现 `jpaAnnotationsShouldBePresent`，通过反射验证 `@Entity`、`@Table(name="ai_call_log", indexes=...)`、`@Id`、`@GeneratedValue(strategy=IDENTITY)`、`@Column` 及三个 `@Index` 注解存在且参数正确。
4. **`AiCallLogStatsTest.java`** — 应实现 `jpaAnnotationsShouldBePresent`，通过反射验证 `@Entity`、`@Table(name="ai_call_log_stats", indexes=...)`、`@Id`、`@GeneratedValue`、`@Column` 及 `@Index(name="idx_stats_capability_month")` 注解存在且参数正确。
5. **新增测试文件或集成测试** — 应为 `AiCallLogRepository.countByCallTimeBefore` 和 `AiCallLogStatsRepository.findByCapabilityIdAndStatMonthBetween` 补充 `@DataJpaTest` 集成测试，验证自定义查询方法签名正确且可正常生成 SQL。
6. **`LoggingMetricsCollectorTest.java`** — 建议将 `shouldParsePromptVersionSafely` 拆分为 `shouldHandleNullPromptVersion`、`shouldParseValidPromptVersion`、`shouldHandleInvalidPromptVersion` 三个独立测试方法，消除 `reset(repository)` 引发的不确定性。
