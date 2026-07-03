# 任务指令（v27）

## 动作
NEW

## 任务描述
重构 `FallbackAiService` 构造器为 ObjectProvider + @Primary 模式，移除旧降级策略管控逻辑，简化委托调用方式。

## 选择理由
最终剩余任务（Batch6 P3 末项）。Task 18（AiPlatformConfig）已验证通过，ai.platform.enabled 属性绑定装配和 AiPlatformEnvironmentPostProcessor 转发机制已就绪。ObjectProvider 延迟解析依赖的 AiService Bean @ConditionalOnProperty 互斥机制已由 AiPlatformEnvironmentPostProcessor 保证。

## 任务上下文

### 设计依据（Docs/06_ood_phase5_G.md §9.2、§2.3 类图）

**新设计形态**（类图 §2.3）：
```java
@Primary
class FallbackAiService {
    <<decorator>>
    -ObjectProvider~AiService~ delegateProvider
    -AiService delegate  // 由 @PostConstruct 从 delegateProvider.getIfUnique() 解析
    +triage() // 委托给 delegate + 空降级兜底
    +diagnosis() // 同上
    +... // 共 13 个方法
}
```

**迁移要求**（§9.2 阶段一 + 阶段二合并）：
1. **构造器签名变更**：`FallbackAiService(List<AiService>, List<DegradationStrategy>)` → `FallbackAiService(ObjectProvider<AiService> aiServiceProvider, @Value("${ai.platform.enabled:false}") boolean aiPlatformEnabled)`
2. **委托解析**：在构造器中 `this.delegate = aiServiceProvider.getIfUnique()`，不再使用 stream filter 排除自身
3. **@Primary 注解**：类级别标注 `@Primary`，确保业务模块注入 `AiService` 时优先选择
4. **移除策略管理**：删除 `strategies` 字段、`selectDelegate()` 方法、`applyStrategies()` 方法（标记 `@Deprecated` 仅保留声明但不调用，或直接删除）
5. **简化 13 个委托方法**：每个方法统一为 `if (delegate == null) return handleEmptyDelegates(); return delegate.xxx(request);` 模式，不再创建 DegradationContext 或调用 applyStrategies
6. **日志行为保留**：构造器中 delegate == null 时输出 ERROR 日志；handleEmptyDelegates() 输出 WARN 日志

### 生产代码修改要点

**FallbackAiService.java**（重写 ~70%）：
- 类注解追加 `@Primary`
- 新增 `import org.springframework.context.annotation.Primary`、`import org.springframework.beans.factory.ObjectProvider`、`import org.springframework.beans.factory.annotation.Value`
- 移除 `import com.aimedical.modules.ai.api.degradation.DegradationContext;`、`import com.aimedical.modules.ai.api.degradation.DegradationStrategy;`、`import java.util.stream.Collectors;`
- 字段变更：`List<AiService> delegates` → `AiService delegate`（单例）；删除 `List<DegradationStrategy> strategies`
- 构造器：`(ObjectProvider<AiService> delegateProvider, @Value("${ai.platform.enabled:false}") boolean aiPlatformEnabled)` — 在构造体内 `this.delegate = delegateProvider.getIfUnique();`；delegate == null 时 `log.error(...)`；aiPlatformEnabled 暂不用于本任务（Phase 2 使用），保留供未来使用
- 删除 `handleEmptyDelegates()` 中的 `AtomicBoolean firstEmptyDelegateCall` 保留（避免首次 ERROR→WARN 日志变更），或简化日志逻辑
- 删除 `selectDelegate(DegradationContext context)` 方法
- 删除 `applyStrategies(AiResult<T> result, DegradationContext context)` 方法
- 13 个方法全部统一简化：`if (delegate == null) return handleEmptyDelegates(); return delegate.xxx(request);`（移除 DegradationContext 创建和 applyStrategies 调用）

### 测试文件修改要点

**FallbackAiServiceTest.java**（~40 处构造器调用适配 + 删除 7 个策略依赖测试）：

**构造器适配模式**（原有 ~40 处 `new FallbackAiService(List.of(...), List.of(...))` 改为）：
```java
ObjectProvider<AiService> provider = mock(ObjectProvider.class);
when(provider.getIfUnique()).thenReturn(delegate);  // 有 delegate 时
when(provider.getIfUnique()).thenReturn(null);       // 无 delegate 时
FallbackAiService fallback = new FallbackAiService(provider, false);
```

**删除的测试**（7 个，依赖 DegradationStrategy 行为，不再适用）：
1. `shouldDegradeWhenStrategyTriggers` — FallbackAiService 不再处理策略
2. `shouldReturnOriginalResultWhenNoStrategyDegrades` — 同上
3. `shouldExcludeSelfFromDelegates` — ObjectProvider 无需自排除
4. `selectDelegateShouldPickFirstWhenNoStrategies` — selectDelegate 已移除
5. `selectDelegateShouldSkipFirstWhenDegradedByStrategy` — 同上
6. `selectDelegateShouldReturnEmptyDelegatesWhenAllSkipped` — 同上
7. `selectDelegateShouldUseContextWithServiceNameAndOperationName` — 同上

**保留并修改的测试**（~25 个，仅改构造器调用 + import 新增）：
- `shouldDelegateToFirstAvailableService` — ObjectProvider mock 返回 delegate
- `shouldReturnFallbackResultWhenNoDelegateAvailable` — ObjectProvider mock 返回 null
- `shouldReturnOriginalResultWhenDelegateAlreadyDegraded` — ObjectProvider mock 返回 delegate
- `shouldLogErrorOnConstruction` — ObjectProvider mock 返回 null
- `shouldLogWarnOnSubsequentCalls` — ObjectProvider mock 返回 null
- 所有 13 对 `xxxShouldDelegateWhenAvailable` / `xxxShouldReturnDegradedWhenNoDelegate` — 同上

**import 变更**：
- 新增：`import org.springframework.beans.factory.ObjectProvider;`
- 删除：`import com.aimedical.modules.ai.api.degradation.DegradationContext;`、`import com.aimedical.modules.ai.api.degradation.DegradationStrategy;`
- 删除：`import ch.qos.logback.classic.Level;`、`import ch.qos.logback.classic.Logger;`、`import ch.qos.logback.classic.spi.ILoggingEvent;`、`import ch.qos.logback.core.read.ListAppender;`、`import org.slf4j.LoggerFactory;`（如果日志测试已删除则删除，否则保留）

## 已有代码上下文

当前 FallbackAiService.java（301 行）：
- `@Service` 注解，implements AiService（13 方法）
- 构造器 `(List<AiService> aiServiceList, List<DegradationStrategy> strategies)` — stream filter 排除自身
- `selectDelegate(context)` — 遍历 delegates + strategies 选择可用委托
- `applyStrategies(result, context)` — 对非 success 结果应用降级策略
- 每个方法 3 步：空检查 → selectDelegate → delegate 调用 → applyStrategies
- 日志：构造器无空检测日志；handleEmptyDelegates 首次 ERROR 后续 WARN

当前 FallbackAiServiceTest.java（521 行，36 个 @Test）：
- 全部使用 `new FallbackAiService(List.of(...), List.of(...))` 构造
- 含 7 个 DegradationStrategy 相关测试
- 含 2 个日志验证测试（ListAppender）
- 含 2 个 selectDelegate 行为测试
- 含 13 对 per-method success/degraded 测试
- 含 1 个 `shouldExcludeSelfFromDelegates` 测试

## 实施要点

1. 先修改 FallbackAiService.java（生产代码），确保 compile 通过
2. 再修改 FallbackAiServiceTest.java（测试代码），适配新构造器
3. 策略相关测试直接删除（不再适用），日志测试保留但适配新构造方式
4. 运行 `mvn test -pl modules/ai/ai-impl -am -Dtest=FallbackAiServiceTest` 验证通过
5. 运行 `mvn test -pl modules/ai/ai-impl -am` 验证全量通过

## 完成条件
- FallbackAiService 构造器改为 ObjectProvider + @Primary
- selectDelegate / applyStrategies 方法移除
- 13 个委托方法简化
- FallbackAiServiceTest compile 通过 + 所有 retained 测试通过
- 全量 `mvn test` 通过
