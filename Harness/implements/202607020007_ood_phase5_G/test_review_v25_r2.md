# 测试审查报告（v25 r2）

## 审查结果
REJECTED

## 发现

- **[严重]** `orchestrator/impl/*CapabilityExecutorTest.java`（6 个薄适配器文件）— 12 个断言失败 + 6 个 NPE 错误。实现声称"无偏差"，但这些文件修改后运行时失败率极高。`shouldSucceedWithValidDelegation` 和 `shouldDegradeOnTimeout` 均返回 `false`（期望 `true`），说明 AtomicReference 包装导致测试执行路径偏离预期；`shouldReturnFailureOnPhase4BusinessException` 中 `getErrorCode()` 返回 null 引发 NPE，表明 mock 构造未适配新类型。

- **[严重]** `AbstractCapabilityExecutorTest.java` — 2 个 NPE 错误（行 318、1306）：`capabilityTimeoutConfig` 为 null。设计文档声称"null 作为 AtomicReference 参数类型合法"，但生产代码对 AtomicReference 调用 `.get()` 时 null 导致 NPE。设计假设错误，测试代码未充分验证运行时行为。

- **[严重]** `DefaultModelRouterTest.java` — `shouldNotCrashOnEmptyProperties` NPE：`this.routes` is null（行 105→31）。`ModelRoute` → `ModelRouteConfig` 迁移后，测试未正确处理空路由场景，导致生产代码 `DefaultModelRouter` 构造器运行时崩溃。

- **[严重]** `AiPlatformConfigTest.initShouldSucceedWithValidConfiguration` — 预期 `PT30S` 但得到 `null`。该文件不在 10 个修改文件清单中，说明存在非预期的回归。与"生产代码零变更"声明矛盾，或表明测试环境的配置加载受到本次变更影响。

## 修改要求

1. **6 个薄适配器 Executor 测试文件** — 需要逐个排查 `shouldSucceedWithValidDelegation`、`shouldDegradeOnTimeout`、`shouldReturnFailureOnPhase4BusinessException` 三个测试方法，修复 AtomicReference 包装后 mock 预期与实际行为不匹配的问题，阻止 NPE 传播。

2. **AbstractCapabilityExecutorTest.java** — 行 318 和 1306 及附近的其他构造调用，若有参数传入 `null` 而生产代码会对该参数调用 AtomicReference.get()，则必须改为传入 `new AtomicReference<>(null)` 或非 null 实例，以匹配实际运行时行为。

3. **DefaultModelRouterTest.java** — 检查 `createRouter` 及其调用路径，确保 `shouldNotCrashOnEmptyProperties` 中传入空 map 后 `DefaultModelRouter` 内部 `this.routes` 被正确初始化而非 null。

4. **AiPlatformConfigTest** — 排查 `initShouldSucceedWithValidConfiguration` 获取到 `null` 而非 `PT30S` 的根因。检查是否因 `AiPlatformConfig` 或 `AiPlatformEnvironmentPostProcessor` 的变更间接影响其行为，或测试配置资源缺失。
