# 任务指令（v12）

## 动作
RETRY + NEW

## 任务描述

### RETRY（R11 遗留）— 下游测试适配 AiResult setter 移除
修复因 T29（AiResult 字段 final + 移除 setter）导致的下游测试 NoSuchMethodError：
- `PrescriptionAuditServiceImplTest.java` — 1 处 `setSuccess(true)` + `setData(null)`
- `MedicalRecordConverterTest.java` — 1 处 `setSuccess(true)` + `setData(null)` + 5 处 `setDegraded(true)` + 5 处 `setData(...)`

### NEW（R12）— 讨论结论执行器修复
- **T24**：`DiscussionConclusionCapabilityExecutor.doExecuteInternal()` 修改了防御性拷贝后的 request 对象
- **T33**：`compressTranscripts()` 嵌套提交到 `llmCallExecutor` 存在线程池死锁风险

## 选择理由
R11 验证失败为下游测试适配问题，修复范围小（2 个测试文件 8 处调用），与 R12 均涉及 `DiscussionConclusionCapabilityExecutor`（T24 在同一文件），合并一轮处理减少轮次。

## 任务上下文

### RETRY — 下游测试 AiResult 适配
**根因**：T29 使 AiResult 5 个字段（success/data/errorCode/degraded/fallbackReason）全部变为 `final`，移除 5 个 setter。下游测试在 R11 中未被修复。

**受影响测试**：

1. `PrescriptionAuditServiceImplTest.java:192-194`：
```java
// 当前（会 NoSuchMethodError）：
AiResult<PrescriptionCheckResponse> aiResult = new AiResult<>();
aiResult.setSuccess(true);
aiResult.setData(null);
// 修复后：
AiResult<PrescriptionCheckResponse> aiResult = AiResult.success(null);
```

2. `MedicalRecordConverterTest.java:124-126`（同上模式）：
```java
AiResult<MedicalRecordGenResponse> aiResult = new AiResult<>();
aiResult.setSuccess(true);
aiResult.setData(null);
→ AiResult.success(null)
```

3. `MedicalRecordConverterTest.java:111-113`：
```java
AiResult<MedicalRecordGenResponse> aiResult = AiResult.failure("MR_GEN_AI_TIMEOUT");
aiResult.setData(aiResp);
aiResult.setDegraded(true);
→ AiResult<MedicalRecordGenResponse> aiResult = new AiResult<>(false, aiResp, "MR_GEN_AI_TIMEOUT", true, null);
```

4. `MedicalRecordConverterTest.java:135-136`：
```java
AiResult<MedicalRecordGenResponse> aiResult = AiResult.failure("MR_GEN_AI_TIMEOUT");
aiResult.setDegraded(true);
→ AiResult.degradedWithErrorCode("MR_GEN_AI_TIMEOUT", null);
```

5. `MedicalRecordConverterTest.java:148-150`（`MR_GEN_AI_INTERRUPTED` + data + degraded）：
→ `new AiResult<>(false, aiResp, "MR_GEN_AI_INTERRUPTED", true, null)`

6. `MedicalRecordConverterTest.java:163-165`（`MR_GEN_AI_EXECUTION_ERROR` + data + degraded）：
→ `new AiResult<>(false, aiResp, "MR_GEN_AI_EXECUTION_ERROR", true, null)`

7. `MedicalRecordConverterTest.java:178-180`（`SOME_UNKNOWN_ERROR` + data + degraded）：
→ `new AiResult<>(false, aiResp, "SOME_UNKNOWN_ERROR", true, null)`

### T24 — 防御性拷贝后修改 request
**位置**：`DiscussionConclusionCapabilityExecutor.java:114,117`
**问题**：`doExecuteInternal()` 在压缩 transcripts 成功后调用 `request.setTranscripts(...)`，修改了 `execute()` 中通过 `objectMapper.convertValue` 创建的防御性拷贝。
**修复方向**：不直接修改 request，创建新 `DiscussionConclusionRequest` 实例装载压缩后的 transcripts，赋值给 request 引用。
- `DiscussionConclusionRequest` 仅含一个字段 `List<DiscussionTranscript> transcripts`
- `extractVariables(request)` 和 `executeStandardPipeline()` 均使用 request 引用
- 拷贝后 `transcriptSummaryElapsedMs` 计时逻辑保持不变

### T33 — 线程池嵌套死锁风险
**位置**：`DiscussionConclusionCapabilityExecutor.java:171,185`
**问题**：`compressTranscripts()` 中使用 `CompletableFuture.supplyAsync(() -> {...}, llmCallExecutor)`。`doExecuteInternal` 本身在 `llmCallExecutor` 线程上运行（`AbstractCapabilityExecutor:149-159`），若池中所有线程都被其他请求占用，则子任务永远无法获取线程 → 线程池死锁。
**修复方向**：
- 注入 `AiPlatformConfig` 中已定义的 `@Bean("transcriptSummaryExecutor") Executor`（core=min(availCPU,4), max=2*core, queue=20, DiscardPolicy）
- 在 `compressTranscripts()` 中将 `llmCallExecutor` 替换为 `transcriptSummaryExecutor`
- 构造器新增 `@Qualifier("transcriptSummaryExecutor") Executor transcriptSummaryExecutor` 参数

## 已有代码上下文

### DiscussionConclusionCapabilityExecutor 当前结构
- 全参构造器（约 20 个参数），通过 `super(...)` 调用父类
- `doExecuteInternal()`：获取 transcripts → 判断是否需要压缩 → 压缩/截断 → 修改 request → 调用 `executeStandardPipeline()`
- `compressTranscripts()`：使用 `llmCallExecutor` 提交压缩任务 → `future.get(timeout)`
- `buildCompressionRequest()`：创建 LlmChatRequest（已适配 T47 全参构造器）

### AiPlatformConfig 中已有线程池定义
```java
@Bean("transcriptSummaryExecutor")
public Executor transcriptSummaryExecutor() {
    int coreSize = Math.min(Runtime.getRuntime().availableProcessors(), 4);
    return new ThreadPoolExecutor(coreSize, 2 * coreSize, 60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(20),
        new ThreadPoolExecutor.DiscardPolicy() {
            @Override public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                log.warn("转录摘要压缩任务被丢弃: queueSize={}, activeCount={}",
                    e.getQueue().size(), e.getActiveCount());
                super.rejectedExecution(r, e);
            }
        });
}
```

### AiResult 当前 API
- 构造器：`AiResult()`（全 null）、`AiResult(boolean success, T data, String errorCode, boolean degraded, String fallbackReason)`
- 工厂方法：`success(T data)`、`failure(String)`、`failure(String,String)`、`degraded(String)`、`degradedWithErrorCode(String errorCode, String fallbackReason)`
- 所有字段 final，无 setter

## RETRY 说明
**失败原因摘要**：R11 实现将 AiResult 5 个字段设为 final 并移除全部 setter，但未适配下游 prescription 和 medical-record 模块中 8 处通过 setSuccess/setDegraded 构造 AiResult 的测试代码。运行时 NoSuchMethodError。

**修正方向**：将所有 `new AiResult<>()` + `setXxx()` 模式替换为构造器或工厂方法单行调用。无需修改生产代码。
