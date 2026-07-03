# 任务指令（v14）

## 动作
NEW

## 任务描述
**R14 路由与实验管理（含R13 RETRY修复+遗留28错误修复）**

### 子任务A：RETRY — 修复R13 DatabasePromptTemplateManagerTest 6个错误
修复Mockito `any()` 对原始类型 `int` 参数不匹配导致的6个测试错误。
- **文件**：`ai-impl/src/test/.../template/DatabasePromptTemplateManagerTest.java`
- **修改**：
  - line 217：`verify(repository, never()).findByCapabilityIdAndDepartmentIdAndVersion(any(), any(), any())`
    → `verify(repository, never()).findByCapabilityIdAndDepartmentIdAndVersion(anyString(), any(), anyInt())`
  - line 239：`verify(repository, times(1)).findByCapabilityIdAndDepartmentIdAndVersion(any(), any(), any())`
    → `verify(repository, times(1)).findByCapabilityIdAndDepartmentIdAndVersion(anyString(), any(), anyInt())`

### 子任务B：FIX — 修复R11遗留的28个NoSuchMethod错误
修复 `DegradationContext.Builder.invocationCount(Integer)` 被引用为Integer签名而当前为int签名导致的运行时NoSuchMethod错误。
- **错误分布**：CircuitBreakerDegradationStrategyTest×16 / TimeoutDegradationStrategyTest×6 / SlidingWindowMetricsStoreTest×5 / AbstractCapabilityExecutorTest×1
- **修复步骤**：
  1. 执行 `mvn clean compile test-compile -pl ai-api,ai-impl -am` 强制全量重编译
  2. 运行 `mvn test -pl ai-impl -Dtest="CircuitBreakerDegradationStrategyTest"` 验证修复
  3. 如仍报错，逐一确认：DegradationContext.java:145 Builder签名、SlidingWindowMetricsStore.java:153 调用处、TimeoutDegradationStrategy.java getInvocationCount() 使用
- **注意**：需要确认 `DegradationContext` 的交互版本一致性——确保 `ai-api` 模块的编译输出被 `ai-impl` 正确引用

### 子任务C：NEW — 路由模块（T41, T42, T48）

#### T41: ModelRouter.route() 签名修正
- **接口**：`ModelRouter.java` line 4
  - 当前：`ModelRoute route(String capabilityId, Object request)`
  - 改为：`ModelRoute route(String capabilityId, ExperimentAssignment assignment)`
  - 添加：`import com.aimedical.modules.ai.impl.experiment.ExperimentAssignment;`
- **实现**：`DefaultModelRouter.java` line 49 同步修改签名
- **测试波及**：`AbstractCapabilityExecutorTest.java` 中所有 `(capId, req) ->` 匿名 ModelRouter lambda 改为 `(capId, assignment) ->`，共3处（line 437, 460, 524附近）

#### T42: @Scheduled 指定 scheduler
- **文件**：`DefaultModelRouter.java` line 34
  - 当前：`@Scheduled(fixedDelay = 60000)`
  - 改为：`@Scheduled(fixedDelay = 60000, scheduler = "taskScheduler")`
- 需确认项目中是否存在 `taskScheduler` Bean（`AiPlatformConfig` 或 `SchedulingConfigurer`），如名称不同则使用实际名称

#### T48: 枚举转换失败告警日志
- **文件**：`AiRouterProperties.java` lines 46-49, 54-57
- 在 `catch (IllegalArgumentException e)` 中添加 `log.warn` 告警
- 添加 Logger 字段：`private static final Logger log = LoggerFactory.getLogger(AiRouterProperties.class);`
- 示例日志：
  ```java
  log.warn("Invalid clientType value: '{}', falling back to {}", config.getClientType(), ClientType.HTTP_API);
  ```

### 子任务D：NEW — 实验管理（T56, T57, T63）

#### T56: min().reversed() → max()
- **文件**：`HashBucketExperimentManager.java` line 86
  - 当前：`.min(Comparator.comparing(Experiment::getStartTime).reversed())`
  - 改为：`.max(Comparator.comparing(Experiment::getStartTime))`

#### T57: loader 数据排序统一
- **文件**：`HashBucketExperimentManager.java` lines 76-77
  - 当前：`cache.get(capabilityId, k -> repository.findByCapabilityIdAndStatus(k, ACTIVE))`
  - 改为：`cache.get(capabilityId, k -> repository.findByCapabilityIdAndStatus(k, ACTIVE).stream().sorted(Comparator.comparing(Experiment::getStartTime).reversed()).collect(Collectors.toList()))`
  - 需确保 Collectors 和 Comparator 已导入

#### T63: Experiment N+1 查询修复
- **文件**：`Experiment.java` line 31-32
  - 当前：`@OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)`
  - **推荐方案**：改为 `fetch = FetchType.LAZY`
  - 对应 `HashBucketExperimentManager.assign()` 中需执行 `JOIN FETCH` 查询，可通过 `ExperimentRepository` 添加 `@Query("SELECT e FROM Experiment e LEFT JOIN FETCH e.groups WHERE e.capabilityId = :capabilityId AND e.status = :status")` 方法实现
- 或者**最小变更方案**：仅添加 `@BatchSize(size = 20)` 缓解N+1（但不完全消除）

## 选择理由
R13 验证失败含 34 个错误（6个R13新引入 + 28个R11遗留），必须在本轮全部修复才能推进后续测试。路由与实验管理（T41-T63）涉及 6 个源文件（ModelRouter/DefaultModelRouter/AiRouterProperties/HashBucketExperimentManager/Experiment/ExperimentGroup），功能独立且修改范围明确，合并一轮处理减少上下文切换。

## 任务上下文
### 项目根目录
`C:\Develop\Software\AIMedicalSys`

### 涉及文件清单
| 文件 | 子任务 | 修改类型 |
|------|--------|---------|
| `ai-impl/.../template/DatabasePromptTemplateManagerTest.java` | RETRY(R13) | 2行修改 |
| `ai-impl/.../degradation/CircuitBreakerDegradationStrategyTest.java` | FIX(遗留) | 全量重编译验证 |
| `ai-impl/.../degradation/TimeoutDegradationStrategyTest.java` | FIX(遗留) | 全量重编译验证 |
| `ai-impl/.../metrics/SlidingWindowMetricsStoreTest.java` | FIX(遗留) | 全量重编译验证 |
| `ai-impl/.../orchestrator/AbstractCapabilityExecutorTest.java` | FIX(遗留)+T41 | 全量重编译+3处lambda修改 |
| `ai-api/.../degradation/DegradationContext.java` | FIX(遗留) | 确认签名 |
| `ai-impl/.../router/ModelRouter.java` | T41 | 接口签名修改 |
| `ai-impl/.../router/DefaultModelRouter.java` | T41,T42 | 签名+@Scheduled |
| `ai-impl/.../router/AiRouterProperties.java` | T48 | 添加日志 |
| `ai-impl/.../experiment/HashBucketExperimentManager.java` | T56,T57 | 排序修改 |
| `ai-impl/.../experiment/Experiment.java` | T63 | FetchType修改 |
| `ai-impl/.../experiment/ExperimentGroup.java` | T63 | 确认 |
| `ai-impl/.../experiment/ExperimentRepository.java` | T63 | 可能新增@Query方法 |

### 验证方法
1. 执行 `mvn clean compile test-compile -pl ai-api,ai-impl -am` 确认全量编译通过
2. 执行 `mvn test -pl ai-impl -Dtest="DatabasePromptTemplateManagerTest"` 确认6个错误归零
3. 执行 `mvn test -pl ai-impl -Dtest="CircuitBreakerDegradationStrategyTest,TimeoutDegradationStrategyTest,SlidingWindowMetricsStoreTest,AbstractCapabilityExecutorTest"` 确认28个错误归零
4. 执行 `mvn test -pl ai-impl` 确认所有测试通过（529+ → 预期 ~563 pass）
