# 设计审查报告（v24 r1）

## 审查结果
**REJECTED**

## 发现

### **[严重]** `refreshCapabilityTimeoutConfig()` 运行时刷新机制失效

`refreshCapabilityTimeoutConfig()` 方法体中通过 `applicationContext.getBean(AiExecutionProperties.class)` 获取配置值，但 `AiExecutionProperties` 是一个标准的 `@ConfigurationProperties` 单例 Bean，在标准 Spring Boot 生命周期中，该 Bean 在应用启动时从 Environment 绑定一次后**不会再随 Environment 变更而自动重新绑定**。因此每次 `@Scheduled` 定时触发时，`getBean()` 返回的都是同一个持有初始值的 Bean，`clear+putAll` 仅仅是原地复制同一份数据，**实际从未读取新的配置值**。该定时刷新方法事实上是一个无操作。

与此同时，方法体中获取了 `Environment env` 变量却**从未被使用**（dead code），表明设计意图是通过 Environment 直接重新读取，但未完成正确实现。

**任务规格明确要求**（task_v24.md §1 `@Scheduled 定时刷新`）：`"通过 Environment 重新读取配置值并更新 capabilityTimeoutConfig Map Bean 缓存"`，设计偏离了任务规格。

**期望修正方向**：改用 `Environment.getProperty()` 直接从 Environment 逐项读取超时配置并解析为 `Map<String, Duration>`，或为 `AiExecutionProperties` 添加 `@RefreshScope` 使得 Bean 能随 Environment 刷新而重新绑定。

### **[一般]** 配置缓存的 `clear()+putAll()` 非原子操作引入读取窗口

`capabilityTimeoutConfigCache`、`thinAdapterPerCapabilityConfigCache`、`parseTimeoutConfigCache` 三个缓存在 `refreshCapabilityTimeoutConfig()` 中采用 `clear()` 后 `putAll()` 的模式更新。由于 `ConcurrentHashMap` 仅保证单操作线程安全，`clear()` 与 `putAll()` 组成的复合操作不是原子的。在 `clear()` 执行完毕但 `putAll()` 尚未完成的时间窗口内，并发读取的消费者（如各 `CapabilityExecutor` 通过 `@Qualifier` 注入的同一 Map 引用）调用 `get(capabilityId)` 会得到 `null`，可能导致 `NullPointerException` 或错误的超时行为。

设计 §6（线程安全）声称 `"clear+putAll 模式原子更新引用内容"`，该描述与事实不符。

**期望修正方向**：将缓存字段类型改为 `AtomicReference<Map<String, Duration>>`，在 @Scheduled 方法中构造新的 `ConcurrentHashMap` 后通过 `AtomicReference.set()` 一次性原子替换；@Bean 方法返回 `AtomicReference` 或通过 `get()` 暴露内部 Map。

### **[轻微]** `refreshCapabilityTimeoutConfig()` 中 `Environment env` 变量未被使用

方法体第 333 行获取了 `Environment env = applicationContext.getEnvironment()`，但后续未对该变量进行任何读取操作。这是死代码，应移除或替换为正确的 Environment 读取逻辑（同上一[严重]问题的修正方向）。

### **[轻微]** `@Bean("degradationStrategyMapRef")` 直接暴露 `AtomicReference` 给消费者

`degradationStrategyMapRef()` 返回 `AtomicReference<Map<String, List<DegradationStrategy>>>` 的原始引用，注入该 Bean 的消费者可以调用 `set()` 意外替换整个引用。设计意图是消费者仅通过 `get()` 读取，`set()` 仅由 `AiPlatformConfig` 自身执行。虽然 Spring DI 场景下意外调用的风险较低，但直接暴露可变引用不符合最小权限原则。

**期望修正方向**：保留当前方案（可后续优化），或考虑返回 `AtomicReference` 的不可变视图（但 Java 标准库不直接支持，非强制要求）。

## 修改要求

1. **[严重]** `refreshCapabilityTimeoutConfig()` 必须改为从 `Environment` 直接读取配置值，或对 `AiExecutionProperties` 添加 `@RefreshScope`，确保定时刷新能获取最新的配置变更。同时移除未使用的 `Environment env` 变量。
2. **[一般]** 三个配置缓存的刷新模式必须改为原子替换（使用 `AtomicReference<Map<String, Duration>>`），消除 `clear()+putAll()` 非原子窗口。
3. **[轻微]** 上述 1 和 2 修正后，相应更新测试计划中的 `shouldRefreshCapabilityTimeoutConfigOnScheduledRefresh` 测试方法设计以验证真实刷新行为。
