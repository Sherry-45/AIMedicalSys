# 测试审查报告（v19 r2）

## 审查结果
REJECTED

## 发现

- **[一般]** `LoggingMetricsCollectorTest.java:210` — `shouldNotThrowWhenAsyncExecuted` 测试在 MockitoExtension 下直接 `new LoggingMetricsCollector(repository)` 实例化，未经过 Spring AOP 代理，因此 `@Async` 注解完全不生效。测试名和设计规范均声称该测试验证 "@Async 异步执行"，但实际仅验证了多线程直接调用方法不抛异常。虽然 thread-safety 测试本身有附加价值，但未达成其声称的 `@Async` 行为验证目的。

## 修改要求

1. **`LoggingMetricsCollectorTest.java:210` `shouldNotThrowWhenAsyncExecuted`** — 问题：直接 new 目标对象导致 @Async 失效，测试名与设计描述均指向 @Async 异步行为，但实际未验证。修正方向：
   - 方案 A：改为 Spring 集成测试（如 `@SpringBootTest` + 注入代理后的 `LoggingMetricsCollector` Bean）以真正验证 @Async 行为。
   - 方案 B：保留现有 Mockito 测试，但将测试名改为 `shouldNotThrowWhenConcurrentlyCalled`，并同步更新设计文档（detail_v19.md）中对应测试规划的描述，改为"多线程并发调用不抛异常"。
