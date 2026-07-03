# 计划审查报告（v1 r1）

## 审查结果
REJECTED

## 发现

### 1. **[一般] DegradationContext 字段类型不匹配**
- **文件**: task_v1.md §3 "ai-api: DegradationContext 扩展"
- **问题**: 任务将 `invocationCount` 和 `failureCount` 声明为原始类型 `int`（默认值 0），但设计文档 `Docs/06_ood_phase5_G.md` §3.8 `DegradationContext` 扩展字段表（第 2699~2706 行）及相关分析（第 2711 行）明确要求使用包装类型 `Integer`
- **为什么是问题**: 设计文档用 §3.8 整节（第 2708~2716 行）论证了使用 `Integer` 而非 `int` 的必要性——Jackson 反序列化旧 JSON（不含这些字段的序列化数据）时，原始 `int` 会被设置为 0，下游降级策略（如 `CircuitBreakerDegradationStrategy`）可能将 "0 次调用" 误判为 "有 0 次调用" 而非 "数据不存在"；而 `Integer` 默认为 null，策略可通过 `failureCount == null` 区分。`isInitialized()`/`postDeserializationValidate()` 方法的守卫逻辑也依赖包装类型的 null 检测来判定 DegradationContext 是否已初始化。
- **修正方向**: 将 `invocationCount` 和 `failureCount` 的类型从 `int` 改为 `Integer`，默认值从 `0` 改为 `null`，保持与设计文档一致。

### 2. **[轻微] DegradationReason 枚举值不完整**
- **文件**: task_v1.md §1 "ai-api: DegradationReason 枚举"
- **问题**: 任务列出 6 个枚举值，但设计文档 §3.8 DegradationReason 枚举常量表（第 2733~2743 行）定义了 8 个值。缺失：`NO_AVAILABLE_ROUTE`（模型路由无可⽤端点）、`PARSE_FAILURE`（LLM 输出解析失败）、`INTERNAL_ERROR`（不可预知异常）。同时任务新增了设计文档中未定义的 `THIN_ADAPTER_DELEGATE_ERROR`。
- **为什么是问题**: `NO_AVAILABLE_ROUTE` 在 §3.8 CircuitBreakerDegradationStrategy 伪代码中被引用（第 2665 行），`PARSE_FAILURE` 在 StructuredOutputParser 路径中使用（第 2738 行）。虽然这些值在后序批次才需要，但若当前枚举设计未预留扩展空间，后序添加将产生二进制不兼容变更。`THIN_ADAPTER_DELEGATE_ERROR` 作为额外改进是可接受的，但需确认是否与 `INFRASTRUCTURE_ERROR` 的语义重叠。
- **修正方向**: 建议补充完整 8 个设计文档已定义的枚举值，确保后序批次实现时枚举已就位。

### 3. **[轻微] WindowedEvent.EventType 命名差异**
- **文件**: task_v1.md §4 "SlidingWindowMetricsStore"
- **问题**: 任务使用 `SUCCESS` 作为 EventType 枚举值，设计文档 §3.8 第 2534 行定义为 `NORMAL_SUCCESS`
- **为什么是问题**: 命名不一致虽不影响功能，但增加后续阅读理解负担。
- **修正方向**: 将 `SUCCESS` 改为 `NORMAL_SUCCESS` 与设计文档一致。
