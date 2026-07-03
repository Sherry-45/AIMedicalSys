# 详细设计（v12）

## 概述

RETRY（R11）：修复 2 个下游测试文件（PrescriptionAuditServiceImplTest、MedicalRecordConverterTest）中 AiResult setter 调用 → 构造器/工厂方法替换（共 8 处）。NEW（R12）：修复 DiscussionConclusionCapabilityExecutor 2 个缺陷 T24（防御性拷贝后修改 request）和 T33（线程池嵌套死锁）。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `prescription/.../PrescriptionAuditServiceImplTest.java` | 修改 | RETRY：替换 1 处 setter 调用 |
| `medical-record/.../MedicalRecordConverterTest.java` | 修改 | RETRY：替换 7 处 setter 调用 |
| `ai-impl/.../impl/DiscussionConclusionCapabilityExecutor.java` | 修改 | T24+T33 |

## RETRY — 下游测试适配

### PrescriptionAuditServiceImplTest

**位置**：Line 192-195（方法 `auditShouldHandleAiResultDataNull`）

**变更前**：
```java
AiResult<PrescriptionCheckResponse> aiResult = new AiResult<>();
aiResult.setSuccess(true);
aiResult.setData(null);
```

**变更后**：
```java
AiResult<PrescriptionCheckResponse> aiResult = new AiResult<>(true, null, null, false, null);
```

**说明**：`AiResult.success(null)` 因 `Objects.requireNonNull(data)` 会抛出 NPE，故使用全参构造器而非 `success()` 工厂方法。

### MedicalRecordConverterTest

**位置 1**：Line 124-126（方法 `toRecordGenerateResponseShouldReturnSuccessFalseWhenSuccessWithNullData`）

**变更前**：
```java
AiResult<MedicalRecordGenResponse> aiResult = new AiResult<>();
aiResult.setSuccess(true);
aiResult.setData(null);
```

**变更后**：
```java
AiResult<MedicalRecordGenResponse> aiResult = new AiResult<>(true, null, null, false, null);
```

**位置 2**：Line 111-113（方法 `toRecordGenerateResponseShouldSetTimeoutErrorCode`）

**变更前**：
```java
AiResult<MedicalRecordGenResponse> aiResult = AiResult.failure("MR_GEN_AI_TIMEOUT");
aiResult.setData(aiResp);
aiResult.setDegraded(true);
```

**变更后**：
```java
AiResult<MedicalRecordGenResponse> aiResult = new AiResult<>(false, aiResp, "MR_GEN_AI_TIMEOUT", true, null);
```

**位置 3**：Line 135-136（方法 `toRecordGenerateResponseShouldReturnSuccessTrueWhenTimeoutEvenWithNullData`）

**变更前**：
```java
AiResult<MedicalRecordGenResponse> aiResult = AiResult.failure("MR_GEN_AI_TIMEOUT");
aiResult.setDegraded(true);
```

**变更后**：
```java
AiResult<MedicalRecordGenResponse> aiResult = AiResult.degradedWithErrorCode("MR_GEN_AI_TIMEOUT", null);
```

**位置 4**：Line 148-150（方法 `toRecordGenerateResponseShouldSetInterruptedErrorCode`）

**变更前**：
```java
AiResult<MedicalRecordGenResponse> aiResult = AiResult.failure("MR_GEN_AI_INTERRUPTED");
aiResult.setData(aiResp);
aiResult.setDegraded(true);
```

**变更后**：
```java
AiResult<MedicalRecordGenResponse> aiResult = new AiResult<>(false, aiResp, "MR_GEN_AI_INTERRUPTED", true, null);
```

**位置 5**：Line 163-165（方法 `toRecordGenerateResponseShouldSetExecutionErrorCode`）

**变更前**：
```java
AiResult<MedicalRecordGenResponse> aiResult = AiResult.failure("MR_GEN_AI_EXECUTION_ERROR");
aiResult.setData(aiResp);
aiResult.setDegraded(true);
```

**变更后**：
```java
AiResult<MedicalRecordGenResponse> aiResult = new AiResult<>(false, aiResp, "MR_GEN_AI_EXECUTION_ERROR", true, null);
```

**位置 6**：Line 178-180（方法 `toRecordGenerateResponseShouldIgnoreUnknownErrorCode`）

**变更前**：
```java
AiResult<MedicalRecordGenResponse> aiResult = AiResult.failure("SOME_UNKNOWN_ERROR");
aiResult.setData(aiResp);
aiResult.setDegraded(true);
```

**变更后**：
```java
AiResult<MedicalRecordGenResponse> aiResult = new AiResult<>(false, aiResp, "SOME_UNKNOWN_ERROR", true, null);
```

## T24 — 防御性拷贝后修改 request

**位置**：`DiscussionConclusionCapabilityExecutor.java` Line 114, 117

**问题**：`doExecuteInternal()` 的 `request` 参数来自 `execute()` 中 `objectMapper.convertValue` 创建的防御性拷贝。直接调用 `request.setTranscripts(...)` 修改了拷贝后的对象，违反防御性拷贝契约。

**变更方案**：

**Line 108-114（压缩成功分支）**：
```java
// 变更前：
request.setTranscripts(List.of(compressedTranscript));
// 变更后：
DiscussionConclusionRequest newRequest = new DiscussionConclusionRequest();
newRequest.setTranscripts(List.of(compressedTranscript));
request = newRequest;
```

**Line 116-117（压缩失败→截断分支）**：
```java
// 变更前：
request.setTranscripts(truncateTranscripts(transcripts, 2000));
// 变更后：
DiscussionConclusionRequest newRequest = new DiscussionConclusionRequest();
newRequest.setTranscripts(truncateTranscripts(transcripts, 2000));
request = newRequest;
```

**影响**：`extractVariables(request)`（Line 123）和 `executeStandardPipeline(... request ...)`（Line 124）均使用局部变量 `request`，重新赋值后自动使用新实例。`transcriptSummaryElapsedMs` 计时逻辑（Line 119）无影响。

## T33 — 线程池嵌套死锁风险

**位置**：`DiscussionConclusionCapabilityExecutor.java` Line 185

**问题**：`compressTranscripts()` 中 `CompletableFuture.supplyAsync(() -> {...}, llmCallExecutor)`。`doExecuteInternal` 本身在 `llmCallExecutor` 线程上运行（AbstractCapabilityExecutor:149-159），若池中所有线程均被占用，子任务永远无法获取线程 → 死锁。

**变更方案**：

**新增字段**：
```java
private final Executor transcriptSummaryExecutor;
```

**构造器参数新增**（末尾追加，已存在 ~20 个参数后）：
```java
@Qualifier("transcriptSummaryExecutor") Executor transcriptSummaryExecutor
```

**构造体新增**（末尾追加）：
```java
this.transcriptSummaryExecutor = transcriptSummaryExecutor;
```

**Line 185 变更**：
```java
// 变更前：
}, llmCallExecutor);
// 变更后：
}, transcriptSummaryExecutor);
```

**新增导入**：
```java
import org.springframework.beans.factory.annotation.Qualifier;
```

## 错误处理

| 文件 | 变更 |
|------|------|
| DiscussionConclusionCapabilityExecutor | `transcriptSummaryExecutor` 注入失败时同其他依赖注入（Spring 启动期报错）。`DiscardPolicy` 拒绝策略通过日志告警，线程池满时丢弃新任务而非阻塞 |
| 测试文件 | 无新错误路径；仅替换 setter 调用为构造器/工厂方法 |

## 行为契约

| 组件 | 契约 |
|------|------|
| DiscussionConclusionCapabilityExecutor | `compressTranscripts()` 使用独立线程池 `transcriptSummaryExecutor`，不占用 `llmCallExecutor` 线程。`doExecuteInternal` 不再修改传入的 request 对象，而是创建新实例 |
| PrescriptionAuditServiceImplTest | 使用全参构造器替换 `new AiResult<>()` + `setXxx()` 链 |
| MedicalRecordConverterTest | 使用全参构造器或 `degradedWithErrorCode()` 工厂方法替换 `failure()` + `setXxx()` 链 |

## 依赖关系

| 类型 | 依赖变更 |
|------|---------|
| DiscussionConclusionCapabilityExecutor | 新增字段 `Executor transcriptSummaryExecutor`。新增构造器参数 `@Qualifier("transcriptSummaryExecutor") Executor`。新增导入 `org.springframework.beans.factory.annotation.Qualifier` |
| 测试文件 | 无新增/移除依赖 |
