# 任务指令（v14）

## 动作
NEW

## 任务描述
在 `ai-api/dto/base/` 包新增 4 个类型：`Phase4ServiceMeta`（元数据值对象）、`Phase4ServiceMetaCapable`（可选接口）、`Phase4BusinessException`（抽象异常基类）、`CallContext`（不可变业务上下文值对象）；确认 `AiRequestBase` 已有骨架满足当前需求，无需修改。

### 涉及文件
| # | 文件路径 | 操作 | 说明 |
|---|---------|------|------|
| 1 | `ai-api/src/main/java/.../api/dto/base/Phase4ServiceMeta.java` | **新建** | 元数据值对象，含 modelId/promptVersion/retryCount 三个字段 |
| 2 | `ai-api/src/main/java/.../api/dto/base/Phase4ServiceMetaCapable.java` | **新建** | 可选接口，定义 `Phase4ServiceMeta getServiceMeta()` 方法 |
| 3 | `ai-api/src/main/java/.../api/dto/base/Phase4BusinessException.java` | **新建** | 抽象异常基类，继承 RuntimeException，薄适配器 instanceof 检测用 |
| 4 | `ai-api/src/main/java/.../api/dto/base/CallContext.java` | **新建** | 不可变业务上下文值对象，聚合 9 个业务上下文字段 |
| 5 | `ai-api/src/test/java/.../api/dto/base/Phase4ServiceMetaTest.java` | **新建** | 测试构造/序列化/equals-hashCode |
| 6 | `ai-api/src/test/java/.../api/dto/base/Phase4ServiceMetaCapableTest.java` | **新建** | 测试匿名实现类的 getServiceMeta() 返回 |
| 7 | `ai-api/src/test/java/.../api/dto/base/Phase4BusinessExceptionTest.java` | **新建** | 测试异常构造/继承链/message |
| 8 | `ai-api/src/test/java/.../api/dto/base/CallContextTest.java` | **新建** | 测试构造/不可变性/withOutputSummary/withPromptVersion/序列化 |

基路径：`AIMedical/backend/modules/ai/ai-api/src/`

### 类型详细要求

#### 1. Phase4ServiceMeta
- **包路径**：`com.aimedical.modules.ai.api.dto.base`
- **形态**：class (简单值对象)
- **字段**：
  - `private final String modelId` — 模型标识，服务未记录时为 null
  - `private final Integer promptVersion` — Prompt 版本号，无 Prompt 版本概念时为 null
  - `private final int retryCount` — 内部重试次数，无重试机制时为 0
- **构造器**：全参构造器 `public Phase4ServiceMeta(String modelId, Integer promptVersion, int retryCount)`
- **public getter** 三个：getModelId() / getPromptVersion() / getRetryCount()
- **Jackson 支持**：全参构造器标注 `@JsonProperty` 或使用 @JsonCreator；或使用 @Data/@Value（若项目已有 lombok 配置，但当前 ai-api 模块无 lombok，使用手写构造器 + getter）
- **equals/hashCode/toString**：标准 Object 方法，按所有字段实现

#### 2. Phase4ServiceMetaCapable
- **包路径**：`com.aimedical.modules.ai.api.dto.base`
- **形态**：interface
- **方法**：`Phase4ServiceMeta getServiceMeta();`
- **用途**：Phase 4 响应 DTO 可选实现此接口，底座通过 `instanceof` 检测后安全调用

#### 3. Phase4BusinessException
- **包路径**：`com.aimedical.modules.ai.api.dto.base`
- **形态**：abstract class
- **继承**：`extends RuntimeException`
- **构造器**：
  - `protected Phase4BusinessException(String message)`
  - `protected Phase4BusinessException(String message, Throwable cause)`
- **用途**：6 个 Phase 4 模块的业务异常类未来统一继承此类，薄适配器 catch 块通过 `instanceof Phase4BusinessException` 匹配业务异常

#### 4. CallContext
- **包路径**：`com.aimedical.modules.ai.api.dto.base`
- **形态**：class (不可变值对象)
- **字段**（全部 `private final`）：
  | 字段 | 类型 | 可空 | 说明 |
  |------|------|------|------|
  | departmentId | String | 否 | 科室标识 |
  | callerRole | String | 否 | 调用方角色 |
  | callerId | String | 否 | 调用方标识 |
  | visitId | String | 否 | 就诊标识 |
  | patientId | String | 否 | 患者标识 |
  | sessionId | String | 否 | 会话标识 |
  | inputSummary | String | 否 | 输入摘要 |
  | outputSummary | String | 是 | 输出摘要（降级路径由 fallback 填充，成功路径由管线填充） |
  | promptVersion | Integer | 是 | Prompt 版本号（A/B 实验使用） |
- **构造器**：全参构造器 `public CallContext(String departmentId, String callerRole, String callerId, String visitId, String patientId, String sessionId, String inputSummary, String outputSummary, Integer promptVersion)`，参数顺序同上表
- **public getter**：全部 9 个字段
- **复制方法**：
  - `public CallContext withOutputSummary(String outputSummary)` — 返回新实例，仅 outputSummary 不同
  - `public CallContext withPromptVersion(Integer promptVersion)` — 返回新实例，仅 promptVersion 不同
- **Jackson 支持**：全参构造器标注 @JsonProperty，或提供无参构造器 + setter（不可变对象推荐 @JsonCreator 模式）
- **equals/hashCode/toString**：标准 Object 方法，按所有字段实现

## 选择理由
Batch5 P0 优先级高于 Batch4 P2。Task 16 是 Task 17（6 项薄适配器 CapabilityExecutor）的直接前置依赖。这些类型均在 ai-api 模块内，零外部代码依赖，独立可测。AiRequestBase 已作为抽象骨架存在（含 getDepartmentId/getVisitId/getPatientId/getSessionId 默认方法），满足当前阶段需求。

## 任务上下文

### 设计文档对照
| 设计文档 § | 类型 | 关键约束 |
|-----------|------|---------|
| §3.1 Phase 4 响应级元数据契约 | Phase4ServiceMeta | modelId/promptVersion/retryCount 三字段；值对象 |
| §3.1 Phase 4 响应级元数据契约 | Phase4ServiceMetaCapable | 可选接口；底座通过 instanceof 检测后安全调用 |
| §3.1 Phase 4 模块异常契约 | Phase4BusinessException | 抽象基类 extends RuntimeException；Phase 4 模块统一继承 |
| §3.1 构造器参数数量说明 | CallContext | 9 字段不可变对象；withOutputSummary/withPromptVersion 复制方法 |

### 已有代码上下文
- `ai-api/dto/base/AiRequestBase.java` — 抽象骨架已存在，提供 getDepartmentId/getVisitId/getPatientId/getSessionId 默认方法（返回 null），满足当前 Phase 4 薄适配器骨架需求
- ai-api 模块已有其他 DTO 类型（如 `DegradationContext`、`DegradationReason`）可参考其包结构和编码风格

### 测试规划
| 测试类 | 测试方法 | 覆盖场景 |
|--------|---------|---------|
| Phase4ServiceMetaTest | shouldConstructWithAllFields | 全参构造器正确赋值 |
| | shouldSupportNullFields | modelId 和 promptVersion 为 null |
| | shouldSupportJacksonSerialization | JSON 序列化/反序列化往返 |
| Phase4ServiceMetaCapableTest | shouldReturnServiceMetaFromAnonymousImpl | 匿名实现返回正确元数据 |
| Phase4BusinessExceptionTest | shouldConstructWithMessage | message 构造器 |
| | shouldConstructWithMessageAndCause | message+cause 构造器 |
| | shouldBeInstanceOfRuntimeException | instanceof 检测 |
| CallContextTest | shouldConstructWithAllFields | 全参构造器 9 字段均正确赋值 |
| | shouldBeImmutable | 反射验证字段均为 final |
| | shouldCreateCopyWithOutputSummary | withOutputSummary 返回新实例，原实例不变 |
| | shouldCreateCopyWithPromptVersion | withPromptVersion 返回新实例，原实例不变 |
| | shouldSupportNullOutputSummaryAndPromptVersion | outputSummary/promptVersion 可 null |
| | shouldSupportJacksonSerialization | JSON 序列化/反序列化往返 |

## 已有代码上下文

ai-api 模块基路径：`AIMedical/backend/modules/ai/ai-api/src/main/java/com/aimedical/modules/ai/api/dto/base/`

已有类型：
- `AiRequestBase.java` — 抽象基类，4 个 getter 默认返回 null
- ai-api 模块 test 目录已有测试惯例（参考 `ai-api/src/test/java/.../api/` 下 AiResultTest、DegradationReasonTest 等）
