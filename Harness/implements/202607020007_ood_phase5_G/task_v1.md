# 任务指令（v1）

## 动作
NEW

## 任务描述
实现 OOD Phase5_G 底座核心骨架的基础类型：ai-api 模块的降级基础设施扩展 + ai-impl 模块的滑动窗口指标存储。

### 具体交付物

#### 1. ai-api: DegradationReason 枚举 (新建)
- 文件: `AIMedical/backend/modules/ai/ai-api/src/main/java/com/aimedical/modules/ai/api/degradation/DegradationReason.java`
- 枚举值 (8 个):
  - `NO_AVAILABLE_ROUTE` — 模型路由无可用端点
  - `ENDPOINT_UNAVAILABLE` — 端点不可用
  - `CIRCUIT_BREAKER_OPEN` — 熔断器开路
  - `PARSE_FAILURE` — LLM 输出解析失败
  - `TIMEOUT` — 超时降级
  - `STRATEGY_TRIGGERED` — 降级策略触发（通用）
  - `INTERNAL_ERROR` — 不可预知异常
  - `INFRASTRUCTURE_ERROR` — 基础设施异常
- 每个枚举值含 `code: String` 字段和 `message: String` 字段，实现 `getCode()` / `getMessage()` 方法

#### 2. ai-api: DegradationStrategy 接口扩展 (修改)
- 文件: `AIMedical/backend/modules/ai/ai-api/src/main/java/com/aimedical/modules/ai/api/degradation/DegradationStrategy.java`
- 新增 `default int getOrder() { return 0; }` 方法 (二进制兼容)
- 现有 `shouldDegrade(DegradationContext)` 方法保持不变

#### 3. ai-api: DegradationContext 扩展 (修改)
- 文件: `AIMedical/backend/modules/ai/ai-api/src/main/java/com/aimedical/modules/ai/api/degradation/DegradationContext.java`
- 新增字段（实现 Serializable）:
  - `Integer invocationCount` (默认 null，使用包装类型确保 Jackson 反序列化旧 JSON 时缺失字段为 null 而非 0)
  - `long lastFailureTime` (默认 0)
  - `long elapsedTime` (默认 0)
  - `String requestType` (可空)
  - `Integer failureCount` (默认 null，使用包装类型确保 Jackson 反序列化旧 JSON 时缺失字段为 null 而非 0)
  - `String departmentId` (可空)
  - `long serializedTimestamp` (默认 0)
- 新增方法:
  - `void postDeserializationValidate()` — 反序列化后校验
  - `boolean isFresh()` — 判断上下文是否新鲜
  - `boolean isInitialized()` — 判断是否已初始化
  - `void setDepartmentId(String)` / `String getDepartmentId()`
- 保持无参构造器可用，新增 Builder 模式（静态内部类 Builder）

#### 4. ai-impl: SlidingWindowMetricsStore (新建)
- 文件: `AIMedical/backend/modules/ai/ai-impl/src/main/java/com/aimedical/modules/ai/impl/metrics/SlidingWindowMetricsStore.java`
- 包路径: `com.aimedical.modules.ai.impl.metrics`
- 功能:
  - 内部使用 `ConcurrentHashMap<String, Deque<WindowedEvent>>` 按能力标识维护滑动窗口
  - `void recordSuccess(String capabilityId, long elapsedMs)` — 记录成功调用
  - `void recordDegraded(String capabilityId, long elapsedMs)` — 记录降级调用
  - `void recordFailure(String capabilityId)` — 记录失败调用
  - `double getFailureRate(String capabilityId)` — 获取失败率（失败/(成功+失败+降级)）
  - `double getEffectiveFailureRate(String capabilityId)` — 获取有效失败率（(失败+降级)/(成功+失败+降级)）
  - `double getAverageElapsed(String capabilityId)` — 获取平均耗时
  - `DegradationContext buildDegradationContext(String capabilityId, String requestType)` — 构建降级上下文
- 静态内部类 `WindowedEvent`:
  - `EventType type` (NORMAL_SUCCESS / DEGRADED / FAILURE)
  - `long timestamp` (毫秒时间戳)
  - `long elapsedMs` (耗时毫秒)
- 窗口配置: 默认 60 秒窗口，每能力最多 10000 事件，惰性淘汰（添加时移除窗口外事件）
- 标注 `@Component`，线程安全

## 选择理由
这是 Phase5_G 底座最核心的底层基础设施。DegradationReason 枚举被所有降级路径引用，SlidingWindowMetricsStore 被 AbstractCapabilityExecutor、降级策略实现、AiOrchestrator 共同依赖。无任何前置依赖，可独立开发并验证。

## 任务上下文
- 设计文档 §1.6.2: DegradationReason 标记为 ⊕ 需新增, DegradationStrategy 标记为 △ 需扩展骨架
- 设计文档 §3.8 降级策略: DegradationContext 需扩展字段 + Builder 模式
- 设计文档 §1.8.3: 滑动窗口内存占用估算（每能力最大 10000 事件, 13 能力合计约 9.2 MB）
- 设计文档 §3.8: `getEffectiveFailureRate()` 将降级调用纳入失败率计算（区别于传统仅计算失败/总调用数），避免降级触发后的"自我保护"场景中调用成功率仍然为 0% 导致降级不可逆转

## 已有代码上下文
- `AiService.java` — 13 个 AI 能力方法，位于 ai-api
- `AiResult.java` — 已有成功/失败/降级工厂方法，位于 ai-api
- `DegradationStrategy.java` — 仅有 `shouldDegrade(DegradationContext)` 方法，位于 ai-api/degradation/
- `DegradationContext.java` — 仅有 `serviceName`/`operationName` 字段和 getter/setter，位于 ai-api/degradation/
- `NoOpDegradationStrategy.java` — 已标注 `@ConditionalOnMissingBean`，始终返回 false，位于 ai-impl/degradation/
- `FallbackAiService.java` — 当前装饰器模式，使用 `List<AiService>` + `List<DegradationStrategy>`，位于 ai-impl/fallback/

## 修订说明（v1 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] DegradationContext 中 `invocationCount`/`failureCount` 应为 `Integer` 而非 `int`（设计文档 §3.8 第 2699~2706 行及第 2711 行明确要求） | 将两个字段类型从 `int` 改为 `Integer`，默认值从 `0` 改为 `null`，添加包装类型选择理由说明 |
| [轻微] DegradationReason 枚举值不完整：缺失设计文档已定义的 `NO_AVAILABLE_ROUTE`、`PARSE_FAILURE`、`INTERNAL_ERROR` 三个值 | 补充全部 8 个设计文档枚举常量 |
| [轻微] WindowedEvent.EventType 中 `SUCCESS` 与设计文档 `NORMAL_SUCCESS` 命名不一致 | 将 `SUCCESS` 改为 `NORMAL_SUCCESS` |

## 修订说明（v1 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] THIN_ADAPTER_DELEGATE_ERROR 枚举常量与设计文档 §3.8 DegradationReason 定义不一致（设计文档明确定义 8 个常量，薄适配器委托异常统一使用 `INFRASTRUCTURE_ERROR + ":subType"` 模式） | 移除 `THIN_ADAPTER_DELEGATE_ERROR` 枚举常量，恢复为设计文档定义的 8 个标准常量。薄适配器委托异常统一由 `INFRASTRUCTURE_ERROR` + 冒号拼接细分标识处理（如 `DegradationReason.INFRASTRUCTURE_ERROR + ":Phase4DtoEmpty"`），与设计文档 §1.4 第 104 行及 §4.2 伪代码约定一致 |

## 修订说明（v1 r3）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] Batch 3（P1）遗漏 SpringAiLlmChatService 和 SpringAiLlmChatStreamService 两个必需实现。DelegatingLlmChatService 的 Map&lt;ClientType, LlmChatService&gt; 分发机制要求 ClientType.SPRING_AI 有对应实现（设计文档 §3.2 DelegatingLlmChatService 伪代码显式包含 `delegates.put(ClientType.SPRING_AI, springAi)`），缺少将导致启动期自检失败 | 修订 plan.md 中 road map 表 task 10，补充 SpringAiLlmChatService.java 和 SpringAiLlmChatStreamService.java 为并列实现。此计划级修订不影响当前 Task 1 的交付内容，只修正后续任务的编排完整性 |
