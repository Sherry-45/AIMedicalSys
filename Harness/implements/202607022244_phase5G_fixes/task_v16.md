# 任务指令（v16）

## 动作
NEW

## 任务描述
执行 `mvn clean test -pl modules/ai/ai-impl -am`，从 `AIMedical/backend/` 目录运行 Maven 清理 + 全量重新编译所有依赖包，消除 28 个 NoSuchMethodError 二进制兼容性错误。

**不需要修改任何源文件。**

## 选择理由
R15 验证失败的 28 error（547 pass / 0 fail / 28 error）全部为同一根因的 NoSuchMethodError：
- `DegradationContext.Builder.invocationCount(Integer)` — 因 R11 中 T30 将 `invocationCount/failureCount` 从 `Integer` 改为 `int`，Builder 方法签名从 `invocationCount(Integer)` 变为 `invocationCount(int)`
- 源码（`DegradationContext.java`、`SlidingWindowMetricsStore.java` 等）已正确更新，但增量编译认为这些文件无变化而跳过，故运行时的类加载仍使用旧二进制中的 `invocationCount(Integer)` 签名
- `mvn clean test` 的 `clean` 阶段删除所有 target/ 目录，触发全量重新编译，确保所有引用方使用新签名生成的字节码

这是 Phase 5G 修复的最后一步。28 个错误清除后，所有 64 项问题修复应全部通过验证。

## 任务上下文
- **根因**：R11 中 T30 将 `DegradationContext.invocationCount/failureCount` 从 `Integer` 改为 `int`，对应 `Builder.invocationCount(int)` / `getInvocationCount()` 返回 `int`。测试类中引用该变更的代码（`CircuitBreakerDegradationStrategyTest` 16 个、`TimeoutDegradationStrategyTest` 6 个、`SlidingWindowMetricsStoreTest` 5 个、`AbstractCapabilityExecutorTest` 1 个）在增量编译时未被重新编译，仍然指向旧的 `invocationCount(Integer)` 方法签名。
- **修复方法**：`mvn clean` 删除所有已编译 class 文件，`test` 阶段全量重新编译并运行测试。

## 已有代码上下文
- `AIMedical/backend/` — Maven 聚合模块父工程目录
- `modules/ai/ai-impl` — ai-impl 模块（包含所有需要重新编译的测试类）
- `-am`（also-make）标志让 Maven 同时编译所有依赖模块（ai-api、common 等）

## 执行命令
```bash
cd AIMedical/backend && mvn clean test -pl modules/ai/ai-impl -am
```
