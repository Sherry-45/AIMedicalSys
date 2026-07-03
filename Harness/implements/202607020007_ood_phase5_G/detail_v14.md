# 详细设计（v14）

## 概述

在 `ai-api/dto/base/` 包新增 4 个类型，覆盖 Phase 4 元数据契约、可选接口、业务异常基类和不可变业务上下文值对象；确认 `AiRequestBase` 已有骨架满足当前需求，无需修改。

## 文件规划

基路径（源码）：`AIMedical/backend/modules/ai/ai-api/src/main/java/com/aimedical/modules/ai/api/dto/base/`
基路径（测试）：`AIMedical/backend/modules/ai/ai-api/src/test/java/com/aimedical/modules/ai/api/dto/base/`

| # | 文件路径 | 操作 | 职责 |
|---|---------|------|------|
| 1 | `dto/base/Phase4ServiceMeta.java` | **新建** | 元数据值对象，含 modelId/promptVersion/retryCount 三字段 |
| 2 | `dto/base/Phase4ServiceMetaCapable.java` | **新建** | 可选接口，定义 `getServiceMeta()` 方法 |
| 3 | `dto/base/Phase4BusinessException.java` | **新建** | 抽象异常基类，extends RuntimeException |
| 4 | `dto/base/CallContext.java` | **新建** | 不可变业务上下文值对象，聚合 9 个业务上下文字段 |
| 5 | `dto/base/Phase4ServiceMetaTest.java` | **新建** | 测试构造/序列化/equals-hashCode |
| 6 | `dto/base/Phase4ServiceMetaCapableTest.java` | **新建** | 测试匿名实现类的 getServiceMeta() 返回 |
| 7 | `dto/base/Phase4BusinessExceptionTest.java` | **新建** | 测试异常构造/继承链/message |
| 8 | `dto/base/CallContextTest.java` | **新建** | 测试构造/不可变性/withOutputSummary/withPromptVersion/序列化 |

## 类型定义

### 1. Phase4ServiceMeta

**形态**：class（值对象）
**包路径**：`com.aimedical.modules.ai.api.dto.base`
**职责**：Phase 4 服务的元数据值对象，记录模型标识、Prompt 版本和重试次数

```java
package com.aimedical.modules.ai.api.dto.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class Phase4ServiceMeta {

    private final String modelId;
    private final Integer promptVersion;
    private final int retryCount;

    @JsonCreator
    public Phase4ServiceMeta(
            @JsonProperty("model_id") String modelId,
            @JsonProperty("prompt_version") Integer promptVersion,
            @JsonProperty("retry_count") int retryCount) {
        this.modelId = modelId;
        this.promptVersion = promptVersion;
        this.retryCount = retryCount;
    }

    public String getModelId() { return modelId; }
    public Integer getPromptVersion() { return promptVersion; }
    public int getRetryCount() { return retryCount; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Phase4ServiceMeta that)) return false;
        return retryCount == that.retryCount
                && Objects.equals(modelId, that.modelId)
                && Objects.equals(promptVersion, that.promptVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelId, promptVersion, retryCount);
    }

    @Override
    public String toString() {
        return "Phase4ServiceMeta{modelId=" + modelId
                + ", promptVersion=" + promptVersion
                + ", retryCount=" + retryCount + "}";
    }
}
```

**公开接口**：
- `String getModelId()` — 返回模型标识（可能 null）
- `Integer getPromptVersion()` — 返回 Prompt 版本号（可能 null）
- `int getRetryCount()` — 返回内部重试次数（默认 0）
- `equals/hashCode/toString` — 标准 Object 方法，按所有字段实现

**构造方式**：`new Phase4ServiceMeta(modelId, promptVersion, retryCount)`，支持 Jackson `@JsonCreator` 反序列化

**类型关系**：无继承/实现

### 2. Phase4ServiceMetaCapable

**形态**：interface
**包路径**：`com.aimedical.modules.ai.api.dto.base`
**职责**：Phase 4 响应 DTO 可选实现此接口，底座通过 `instanceof` 检测后安全调用

```java
package com.aimedical.modules.ai.api.dto.base;

public interface Phase4ServiceMetaCapable {
    Phase4ServiceMeta getServiceMeta();
}
```

**公开接口**：
- `Phase4ServiceMeta getServiceMeta()` — 实现类返回自身关联的元数据

**构造方式**：无（接口不可实例化）
**类型关系**：由 Phase 4 响应 DTO 实现

### 3. Phase4BusinessException

**形态**：abstract class
**包路径**：`com.aimedical.modules.ai.api.dto.base`
**职责**：Phase 4 模块业务异常统一基类，薄适配器 catch 块通过 `instanceof Phase4BusinessException` 匹配业务异常

```java
package com.aimedical.modules.ai.api.dto.base;

public abstract class Phase4BusinessException extends RuntimeException {

    protected Phase4BusinessException(String message) {
        super(message);
    }

    protected Phase4BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**公开接口**：
- `protected Phase4BusinessException(String message)` — 仅消息构造器
- `protected Phase4BusinessException(String message, Throwable cause)` — 消息+原因构造器

**构造方式**：protected，仅子类可调用
**类型关系**：extends `RuntimeException`；未来 6 个 Phase 4 模块业务异常类继承此类

### 4. CallContext

**形态**：class（不可变值对象）
**包路径**：`com.aimedical.modules.ai.api.dto.base`
**职责**：不可变业务上下文值对象，聚合科室/角色/调用方/就诊/患者/会话/输入输出摘要/Prompt 版本

```java
package com.aimedical.modules.ai.api.dto.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class CallContext {

    private final String departmentId;
    private final String callerRole;
    private final String callerId;
    private final String visitId;
    private final String patientId;
    private final String sessionId;
    private final String inputSummary;
    private final String outputSummary;
    private final Integer promptVersion;

    @JsonCreator
    public CallContext(
            @JsonProperty("department_id") String departmentId,
            @JsonProperty("caller_role") String callerRole,
            @JsonProperty("caller_id") String callerId,
            @JsonProperty("visit_id") String visitId,
            @JsonProperty("patient_id") String patientId,
            @JsonProperty("session_id") String sessionId,
            @JsonProperty("input_summary") String inputSummary,
            @JsonProperty("output_summary") String outputSummary,
            @JsonProperty("prompt_version") Integer promptVersion) {
        this.departmentId = departmentId;
        this.callerRole = callerRole;
        this.callerId = callerId;
        this.visitId = visitId;
        this.patientId = patientId;
        this.sessionId = sessionId;
        this.inputSummary = inputSummary;
        this.outputSummary = outputSummary;
        this.promptVersion = promptVersion;
    }

    public String getDepartmentId() { return departmentId; }
    public String getCallerRole() { return callerRole; }
    public String getCallerId() { return callerId; }
    public String getVisitId() { return visitId; }
    public String getPatientId() { return patientId; }
    public String getSessionId() { return sessionId; }
    public String getInputSummary() { return inputSummary; }
    public String getOutputSummary() { return outputSummary; }
    public Integer getPromptVersion() { return promptVersion; }

    public CallContext withOutputSummary(String outputSummary) {
        return new CallContext(departmentId, callerRole, callerId, visitId,
                patientId, sessionId, inputSummary, outputSummary, promptVersion);
    }

    public CallContext withPromptVersion(Integer promptVersion) {
        return new CallContext(departmentId, callerRole, callerId, visitId,
                patientId, sessionId, inputSummary, outputSummary, promptVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CallContext that)) return false;
        return Objects.equals(departmentId, that.departmentId)
                && Objects.equals(callerRole, that.callerRole)
                && Objects.equals(callerId, that.callerId)
                && Objects.equals(visitId, that.visitId)
                && Objects.equals(patientId, that.patientId)
                && Objects.equals(sessionId, that.sessionId)
                && Objects.equals(inputSummary, that.inputSummary)
                && Objects.equals(outputSummary, that.outputSummary)
                && Objects.equals(promptVersion, that.promptVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(departmentId, callerRole, callerId, visitId,
                patientId, sessionId, inputSummary, outputSummary, promptVersion);
    }

    @Override
    public String toString() {
        return "CallContext{departmentId=" + departmentId
                + ", callerRole=" + callerRole
                + ", callerId=" + callerId
                + ", visitId=" + visitId
                + ", patientId=" + patientId
                + ", sessionId=" + sessionId
                + ", inputSummary=" + inputSummary
                + ", outputSummary=" + outputSummary
                + ", promptVersion=" + promptVersion + "}";
    }
}
```

**公开接口**：
- 9 个 getter 分别返回对应字段
- `CallContext withOutputSummary(String outputSummary)` — 返回新实例，仅 `outputSummary` 不同
- `CallContext withPromptVersion(Integer promptVersion)` — 返回新实例，仅 `promptVersion` 不同
- `equals/hashCode/toString` — 按所有 9 个字段实现

**构造方式**：`new CallContext(departmentId, callerRole, callerId, visitId, patientId, sessionId, inputSummary, outputSummary, promptVersion)`，参数顺序与字段表一致

**类型关系**：无继承/实现

### 5. AiRequestBase（确认无需修改）

**形态**：abstract class（已有）
**包路径**：`com.aimedical.modules.ai.api.dto.base`
**职责**：已有抽象骨架，提供 getDepartmentId/getVisitId/getPatientId/getSessionId 默认方法（返回 null），满足当前 Phase 4 薄适配器骨架需求

**确认**：无需修改。当前 4 个默认返回 null 的 getter 足够 Phase 4 薄适配器使用。后续 Phase 5 需要更多字段时再扩展。

## 错误处理

本任务新增的类型不涉及逻辑异常处理。Phase4BusinessException 作为抽象基类自身不抛出异常，仅供子类继承和 catch 块类型匹配使用。

## 行为契约

### 不可变性
- `Phase4ServiceMeta` 所有字段为 `final`，构造后不可修改
- `CallContext` 所有字段为 `final`，`withOutputSummary`/`withPromptVersion` 返回全新实例，原实例不变

### Jackson 序列化
- 使用 SNAKE_CASE 命名策略（项目已有 `JacksonConfig`），`@JsonProperty` 使用 snake_case 名称与全局策略一致
- 序列化往返示例：Phase4ServiceMeta(modelId="gpt4", promptVersion=2, retryCount=1) ↔ JSON `{"model_id":"gpt4","prompt_version":2,"retry_count":1}`
- 测试中通过 `Jackson2ObjectMapperBuilder` + `customizer()` 构造 `ObjectMapper`，模拟生产环境的 SNAKE_CASE 配置

### equals/hashCode 契约
- `Phase4ServiceMeta`：按 modelId、promptVersion、retryCount 三个字段比较
- `CallContext`：按全部 9 个字段比较
- 均使用 `java.util.Objects.equals` / `Objects.hash` 实现

## 依赖关系

**依赖的已有类型**：
| 类型 | 所在包 | 用途 |
|------|--------|------|
| `RuntimeException` | `java.lang` | Phase4BusinessException 的父类 |
| `com.fasterxml.jackson.annotation.JsonCreator` | 外部依赖 | Phase4ServiceMeta/CallContext 反序列化注解 |
| `com.fasterxml.jackson.annotation.JsonProperty` | 外部依赖 | 构造器参数 JSON 映射注解 |
| `java.util.Objects` | JDK | equals/hashCode 实现 |

**外部依赖**：Jackson 注解（已在 ai-api 模块中通过 spring-boot-starter-web 传递依赖）

**暴露给后续任务的公开接口**：
- `Phase4ServiceMeta` → Task 17 响应 DTO 中使用
- `Phase4ServiceMetaCapable` → Task 17 响应 DTO 可选实现
- `Phase4BusinessException` → Task 17 及后续 Phase 4 模块统一继承
- `CallContext` → Task 17 CapabilityExecutor 管线中使用

## 测试设计

### Phase4ServiceMetaTest

| 测试方法 | 覆盖场景 | 验证要点 |
|---------|---------|---------|
| `shouldConstructWithAllFields` | 全参构造器 | getModelId=getModelId, getPromptVersion=1, getRetryCount=3 |
| `shouldSupportNullFields` | modelId 和 promptVersion 为 null | getModelId=null, getPromptVersion=null, getRetryCount=0 |
| `shouldSupportJacksonSerialization` | JSON 序列化/反序列化往返 | 序列化后反序列化，equals 验证与原对象一致 |

Jackson 测试辅助：使用 `Jackson2ObjectMapperBuilder` 应用 `JacksonConfig.customizer()` 构造 mapper。

### Phase4ServiceMetaCapableTest

| 测试方法 | 覆盖场景 | 验证要点 |
|---------|---------|---------|
| `shouldReturnServiceMetaFromAnonymousImpl` | 匿名实现类 | getServiceMeta() 返回预先构造的 Phase4ServiceMeta 实例，assertSame |

### Phase4BusinessExceptionTest

| 测试方法 | 覆盖场景 | 验证要点 |
|---------|---------|---------|
| `shouldConstructWithMessage` | message 构造器 | getMessage() 返回传入消息 |
| `shouldConstructWithMessageAndCause` | message+cause 构造器 | getMessage() 和 getCause() 均正确 |
| `shouldBeInstanceOfRuntimeException` | instanceof 链 | 匿名子类 assertTrue instanceof RuntimeException |

由于 Phase4BusinessException 为 abstract，测试中使用匿名子类：
```java
Phase4BusinessException ex = new Phase4BusinessException("test") {};
```

### CallContextTest

| 测试方法 | 覆盖场景 | 验证要点 |
|---------|---------|---------|
| `shouldConstructWithAllFields` | 全参构造器 9 字段 | 9 个 getter 均返回构造传入值 |
| `shouldBeImmutable` | 反射验证字段均为 final | 通过 `getDeclaredFields()` 检查所有字段的 `Modifier.isFinal()` |
| `shouldCreateCopyWithOutputSummary` | withOutputSummary 复制 | 返回新实例；原实例 outputSummary 不变；其它字段与原实例相同 |
| `shouldCreateCopyWithPromptVersion` | withPromptVersion 复制 | 返回新实例；原实例 promptVersion 不变；其它字段与原实例相同 |
| `shouldSupportNullOutputSummaryAndPromptVersion` | 可空字段为 null | outputSummary=null, promptVersion=null 时构造/复制均正常 |
| `shouldSupportJacksonSerialization` | JSON 序列化/反序列化往返 | 序列化后反序列化，equals 验证与原对象一致 |

Jackson 测试辅助：同 Phase4ServiceMetaTest，使用 `Jackson2ObjectMapperBuilder` + `customizer()`。
