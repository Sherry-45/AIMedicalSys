# 测试审查报告（v19 r4）

## 审查结果
REJECTED

## 发现

- **[一般]** `LoggingMetricsCollectorTest.java` — 缺少对 `record()` 方法上 `@Async` 注解的验证。详细设计（detail_v19.md:305）中"行为契约"明确要求 `record()` 标记 `@Async` 异步执行，但测试套件未通过反射验证该注解存在。删除 `@Async` 后所有测试仍可通过。

- **[一般]** `LoggingMetricsCollectorTest.java:64`（shouldHandleRepositoryExceptionGracefully）— 未验证 `repository.save()` 抛出异常时 `log.warn()` 被调用。详细设计（detail_v19.md:282）规定"catch Exception → log.warn('Failed to persist AiCallLogEntity', e)"，属于行为契约，当前仅验证了异常不传播。

- **[轻微]** `LoggingMetricsCollectorTest.java:79`（shouldSetCallTimeToNow）— 仅断言 `callTime` 非 null，未验证其接近 `LocalDateTime.now()`。设计契约要求"callTime 固定使用 LocalDateTime.now()"（detail_v19.md:307），应当断言与实际时间的偏差在合理范围内（如 < 1s）。

## 修改要求

### 1. `LoggingMetricsCollectorTest.java` — 缺少 @Async 注解验证

**位置**：类末尾或新增测试方法

**问题**：行为契约要求 `record()` 方法标记 `@Async`，但无测试覆盖。若后续重构误删该注解，异步语义丢失，测试仍绿。

**期望修正**：新增反射测试验证 `@Async` 注解存在：
```java
@Test
void recordMethodShouldBeAnnotatedWithAsync() throws Exception {
    java.lang.reflect.Method method = LoggingMetricsCollector.class.getMethod("record", AiCallRecord.class);
    assertNotNull(method.getAnnotation(org.springframework.scheduling.annotation.Async.class));
}
```

### 2. `LoggingMetricsCollectorTest.java:64` — shouldHandleRepositoryExceptionGracefully 缺少日志断言

**位置**：`shouldHandleRepositoryExceptionGracefully` 方法

**问题**：设计契约要求异常时输出 `log.warn("Failed to persist AiCallLogEntity", e)`，但测试仅验证了 `assertDoesNotThrow`。日志行为是重要的可观测契约。

**期望修正**：引入测试日志 Appender（如 `ch.qos.logback:logback-classic` 的 `ListAppender`）捕获 Logger 输出，验证 WARN 级别日志包含预期消息。示例：
```java
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

// 在 setUp 或测试方法内：
Logger logbackLogger = (Logger) LoggerFactory.getLogger(LoggingMetricsCollector.class);
ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
listAppender.start();
logbackLogger.addAppender(listAppender);

// 执行后断言：
assertEquals(1, listAppender.list.size());
assertTrue(listAppender.list.get(0).getMessage().contains("Failed to persist AiCallLogEntity"));
```

### 3. `LoggingMetricsCollectorTest.java:79` — shouldSetCallTimeToNow 断言偏弱

**位置**：`shouldSetCallTimeToNow` 方法

**问题**：仅断言 `callTime` 非 null，而设计要求该值固定使用 `LocalDateTime.now()`。当前断言允许任意非 null 值通过。

**期望修正**：在调用 `record()` 前后记录时间戳，断言 `callTime` 落在此区间：
```java
LocalDateTime before = LocalDateTime.now();
collector.record(record);
LocalDateTime after = LocalDateTime.now();
verify(repository).save(entityCaptor.capture());
LocalDateTime callTime = entityCaptor.getValue().getCallTime();
assertTrue(callTime.isEqual(before) || callTime.isAfter(before));
assertTrue(callTime.isEqual(after) || callTime.isBefore(after));
```
