# 详细设计（v27）

## 概述

重构 `FallbackAiService`：构造器改为 `ObjectProvider<AiService>` + `@Primary` 模式，移除降级策略管控逻辑，简化 13 个委托方法。本任务为 OOD Phase5_G 最终任务。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `modules/ai/ai-impl/src/main/java/.../fallback/FallbackAiService.java` | 重写 ~70% | 构造器/注解/字段/方法全面重构 |
| `modules/ai/ai-impl/src/test/java/.../fallback/FallbackAiServiceTest.java` | 修改 | 适配新构造器、删除 7 个策略测试 |

## 生产代码修改

### FallbackAiService.java

**类注解变更**：
```
@Service
@Primary  // 新增
public class FallbackAiService implements AiService {
```

**import 变更（汇总）**：
- 新增：`import org.springframework.beans.factory.ObjectProvider;`
- 新增：`import org.springframework.beans.factory.annotation.Value;`
- 新增：`import org.springframework.context.annotation.Primary;`
- 删除：`import java.util.List;`
- 删除：`import java.util.stream.Collectors;`
- 删除：`import com.aimedical.modules.ai.api.degradation.DegradationContext;`
- 删除：`import com.aimedical.modules.ai.api.degradation.DegradationStrategy;`

**字段变更**：
```java
// 删除
private final List<AiService> delegates;
private final List<DegradationStrategy> strategies;

// 替换为
private final AiService delegate;
```

**构造器变更**：
```java
// 原
public FallbackAiService(List<AiService> aiServiceList,
                         List<DegradationStrategy> strategies) {
    this.delegates = aiServiceList.stream()
            .filter(s -> !(s instanceof FallbackAiService))
            .collect(Collectors.toList());
    this.strategies = strategies;
    if (this.delegates.isEmpty()) {
        log.error("No available AiService delegate");
    }
}

// 新
public FallbackAiService(ObjectProvider<AiService> delegateProvider,
                          @Value("${ai.platform.enabled:false}") boolean aiPlatformEnabled) {
    this.delegate = delegateProvider.getIfUnique();
    if (this.delegate == null) {
        log.error("No available AiService delegate");
    }
}
```

**删除的方法**：
- `selectDelegate(DegradationContext context)` — 原行 66-80
- `applyStrategies(AiResult<T> result, DegradationContext context)` — 原行 290-300

**保留并简化 `handleEmptyDelegates()`**（原行 61-64，不变；按 task_v27.md line 45 的"简化日志逻辑"路径执行，移除 AtomicBoolean，始终 log WARN）：
```java
private <T> CompletableFuture<AiResult<T>> handleEmptyDelegates() {
    log.warn("No available AiService delegate");
    return CompletableFuture.completedFuture(AiResult.degraded("No available AiService delegate"));
}
```

**13 个委托方法简化模式**（以 `triage` 为例）：
```java
@Override
public CompletableFuture<AiResult<TriageResponse>> triage(TriageRequest request) {
    if (delegate == null) {
        return handleEmptyDelegates();
    }
    return delegate.triage(request);
}
```

其余 12 个方法（`diagnosis`、`prescriptionCheck`、`generateMedicalRecord`、`analysisReportForInspection`、`analysisReportForLabTest`、`imageAnalysis`、`knowledgeBaseQuery`、`recommendExamination`、`prescriptionAssist`、`recommendExecutionOrder`、`schedule`、`discussionConclusion`）均遵循同一模式，只替换方法名和参数/返回类型。

## 测试代码修改

### FallbackAiServiceTest.java

**import 变更**：
- 新增：`import org.springframework.beans.factory.ObjectProvider;`
- 删除：`import com.aimedical.modules.ai.api.degradation.DegradationContext;`（2 处：行 7 和行 49）
- 删除：`import com.aimedical.modules.ai.api.degradation.DegradationStrategy;`
- 删除：`import java.util.List;`
- 保留（日志测试保留，对应 import 不删除）：`ch.qos.logback.classic.Level`、`ch.qos.logback.classic.Logger`、`ch.qos.logback.classic.spi.ILoggingEvent`、`ch.qos.logback.core.read.ListAppender`、`org.slf4j.LoggerFactory`

**删除 7 个测试方法**（整段删除）：

| # | 方法名 | 行号范围 | 理由 |
|---|--------|---------|------|
| 1 | `shouldDegradeWhenStrategyTriggers` | 77-93 | 策略逻辑已移除 |
| 2 | `shouldReturnOriginalResultWhenNoStrategyDegrades` | 95-111 | 同上 |
| 3 | `shouldExcludeSelfFromDelegates` | 113-125 | ObjectProvider.getIfUnique() 无需自排除 |
| 4 | `selectDelegateShouldPickFirstWhenNoStrategies` | 451-465 | selectDelegate 已移除 |
| 5 | `selectDelegateShouldSkipFirstWhenDegradedByStrategy` | 467-484 | 同上 |
| 6 | `selectDelegateShouldReturnEmptyDelegatesWhenAllSkipped` | 486-501 | 同上 |
| 7 | `selectDelegateShouldUseContextWithServiceNameAndOperationName` | 503-520 | 同上 |

**保留并修改的 29 个测试**：所有构造器调用从 `new FallbackAiService(List.of(...), List.of())` 改为 ObjectProvider mock 模式。

**构造器适配模式**：

有 delegate 的场景：
```java
ObjectProvider<AiService> provider = mock(ObjectProvider.class);
when(provider.getIfUnique()).thenReturn(delegate);
FallbackAiService fallback = new FallbackAiService(provider, false);
```

无 delegate 的场景：
```java
ObjectProvider<AiService> provider = mock(ObjectProvider.class);
when(provider.getIfUnique()).thenReturn(null);
FallbackAiService fallback = new FallbackAiService(provider, false);
```

**逐测试修改映射：**

| 测试方法 | 当前构造调用 | 修改后 | 说明 |
|---------|-------------|--------|------|
| `shouldDelegateToFirstAvailableService` | `new FallbackAiService(List.of(delegate), List.of())` | ObjectProvider mock + `thenReturn(delegate)` | delegate 可用 |
| `shouldReturnFallbackResultWhenNoDelegateAvailable` | `new FallbackAiService(List.of(), List.of())` | ObjectProvider mock + `thenReturn(null)` | 无 delegate |
| `shouldReturnOriginalResultWhenDelegateAlreadyDegraded` | `new FallbackAiService(List.of(delegate), List.of())` | ObjectProvider mock + `thenReturn(delegate)` | delegate 可用 |
| `shouldLogErrorOnConstruction` | `new FallbackAiService(List.of(), List.of())` | ObjectProvider mock + `thenReturn(null)` | 无 delegate，日志验证 |
| `shouldLogWarnOnSubsequentCalls` | `new FallbackAiService(List.of(), List.of())` | ObjectProvider mock + `thenReturn(null)` | 无 delegate，日志验证 |
| 12 个 `xxxShouldDelegateWhenAvailable`（非 triage 方法） | `new FallbackAiService(List.of(delegate), List.of())` | ObjectProvider mock + `thenReturn(delegate)` | 分别对应 12 个能力（triage 已在上组） |
| 12 个 `xxxShouldReturnDegradedWhenNoDelegate`（非 triage 方法） | `new FallbackAiService(List.of(), List.of())` | ObjectProvider mock + `thenReturn(null)` | 分别对应 12 个能力（triage 已在上组） |

## 错误处理

- `delegate == null` 时 `handleEmptyDelegates()` 返回 `AiResult.degraded("No available AiService delegate")` 并记录 WARN 日志
- 构造器中 `delegate == null` 时记录 ERROR 日志（行为不变）
- 无自定义异常抛出

## 行为契约

1. 构造器从 `ObjectProvider.getIfUnique()` 解析唯一 `AiService` delegate；若容器中无或存在多个，`getIfUnique()` 返回 null
2. 所有 13 个委托方法：若 `delegate == null` → 返回降级结果；否则直接委托
3. 日志：构造时 delegate 为空 → ERROR；每次委托调用发现 delegate 为空 → WARN
4. `@Primary` 确保业务模块注入 `AiService` 时优先选择 `FallbackAiService`
5. `aiPlatformEnabled` 参数保留供阶段二使用，当前不参与逻辑

## 依赖关系

- 依赖 `org.springframework.beans.factory.ObjectProvider`（spring-beans 提供，无需额外依赖）
- 依赖 `org.springframework.context.annotation.Primary`（spring-context 提供）
- 依赖 `org.springframework.beans.factory.annotation.Value`（spring-beans 提供）
- 依赖 `com.aimedical.modules.ai.api.AiService` 接口（已有）
- 测试依赖 `org.mockito.MockedStatic`（mock ObjectProvider 接口，标准 Mockito 支持）

## 修订说明（v27 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| 日志测试保留但标记删除日志相关 import，将导致编译失败 | 在 import 变更列表中移除对 `ch.qos.logback.classic.Level`、`ch.qos.logback.classic.Logger`、`ch.qos.logback.classic.spi.ILoggingEvent`、`ch.qos.logback.core.read.ListAppender`、`org.slf4j.LoggerFactory` 这 5 行的删除标记，改为注释说明"保留" |
| 映射表中"13 个 xxxShouldDelegateWhenAvailable"和"13 个 xxxShouldReturnDegradedWhenNoDelegate"计数与 29 个保留测试合计不一致（5 特殊 + 13 + 13 = 31 ≠ 29） | 将两处"13 个"均改为"12 个"，并标注"非 triage 方法（triage 已在上组）"，合计 5 + 12 + 12 = 29 |
| handleEmptyDelegates 行为变更未显式说明（简化 vs 保留 AtomicBoolean 两个选项） | 在 handleEmptyDelegates 说明中补充"按 task_v27.md line 45 的简化日志逻辑路径执行，移除 AtomicBoolean，始终 log WARN" |
