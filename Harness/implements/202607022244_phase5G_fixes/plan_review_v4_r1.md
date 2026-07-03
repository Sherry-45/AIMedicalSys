# 计划审查报告（v4 r1）

## 审查结果
REJECTED

## 发现

### [严重] T21 构造器类型变更范围严重遗漏 — 涉及文件清单不完整

**问题**: T21 要求将 `AbstractCapabilityExecutor` 构造器中 `capabilityTimeoutConfig` / `parseTimeoutConfig` / `parseTimeoutDefault` / `thinAdapterPerCapabilityConfig` 四个参数从 `AtomicReference<Map<String, Duration>>` / `AtomicReference<Duration>` 改为直接类型 `Map<String, Duration>` / `Duration`。但任务"涉及文件清单"仅列出 `AbstractCapabilityExecutor.java` 和 `RequestContextUtils.java` 两个文件。

**为什么是问题**: 代码验证发现以下文件均依赖这些参数类型和字段类型：

1. **7个底座子类构造器**（每个都携带 `AtomicReference<Map<String, Duration>>` 参数并传入 `super()`）：
   - `TriageCapabilityExecutor.java:43-45`
   - `ScheduleCapabilityExecutor.java:43-45`
   - `PrescriptionCheckCapabilityExecutor.java:43-45`
   - `PrescriptionAssistCapabilityExecutor.java:43-45`
   - `MedicalRecordGenCapabilityExecutor.java:43-45`
   - `KbQueryCapabilityExecutor.java:43-45`
   - `DiscussionConclusionCapabilityExecutor.java:71-73`

2. **6个薄适配器子类构造器**（同样携带 AtomicReference 参数并传入 `super()`）：
   - `DiagnosisCapabilityExecutor.java:44-48`
   - `ImageAnalysisCapabilityExecutor.java:44-48`
   - `AnalysisReportForLabTestCapabilityExecutor.java`（未核对但同为 thinadapter 模式）
   - `AnalysisReportForInspectionCapabilityExecutor.java`
   - `RecommendExecutionOrderCapabilityExecutor.java`
   - `RecommendExaminationCapabilityExecutor.java`

3. **父类内部 `.get()` 调用点**: 父类 `AbstractCapabilityExecutor.java` 中至少 4 处 `.get()` 调用需同步修改为直接字段引用（第 393 行 `parseTimeoutConfig.get()`、第 397 行 `parseTimeoutDefault.get()`、第 518 行 `capabilityTimeoutConfig.get()`、第 522-523 行 `thinAdapterPerCapabilityConfig.get()`）。

4. **薄适配器内部调用**: `DiagnosisCapabilityExecutor.resolveThinAdapterTimeout()` 第 155 行 `thinAdapterPerCapabilityConfig.get()` 在字段类型变更后也将编译失败（`Map` 无无参 `.get()` 方法）。

按当前计划实施，R4 会产生至少 15 个编译错误。所有子类构造器参数类型和 `super()` 调用均需同步变更。

### [一般] T26 doDegrade() 参数新增对 checkPreDegradation() 的非私有化假定

**问题**: T26 要求为 `checkPreDegradation()` 增加 `userId` 参数。该方法当前为 `private`（第 203 行），且无子类重写，实际影响可控。但任务描述中未显式说明 `checkPreDegradation()` 是私有方法、不涉及子类，在缺乏代码上下文时可能造成后续环节对该方法签名的误解。建议在任务描述中显式标注方法可见性和重写状态。

### [一般] T28 endpointHealthManager null 检查与 T5 异常分支重构的代码位置交叉

**问题**: `endpointHealthManager` 的 null 检查（第 357-366 行）与 T5 的三叉异常分支重构（第 433-481 行）处于同一方法体但未交叉，技术上可独立实施。但两个变更均涉及 `executeStandardPipeline()` 的大段落重写，建议在任务中标注两个修改的叠加重合区域（如 `doDegrade()` 调用 14 处），以减少后续合并冲突风险。

## 修改要求

### 必须修正（严重）
1. **T21 范围扩展**: 将 13 个子类构造器（7 底座 + 6 薄适配器）的文件路径全部纳入修改清单。每个子类的构造器参数类型从 `AtomicReference<Map<String, Duration>>` 改为 `Map<String, Duration>`，`AtomicReference<Duration>` 改为 `Duration`，`super()` 调用对应调整。
2. **T21 父类内部 `.get()` 调用清理**: 在 AbstractCapabilityExecutor.java 中，将 `resolveTimeout()`、`executeStandardPipeline()` 中的 `.get()` 调用全部替换为直接字段引用。
3. **T21 薄适配器内部同步**: 在 6 个薄适配器中将 `resolveThinAdapterTimeout()` 等方法的 `.get()` 调用同步修改。
4. **修正后涉及文件清单**至少应包括 13 个子类文件 + AbstractCapabilityExecutor.java + RequestContextUtils.java，而非当前所列的 2 个文件。

### 建议修正（一般）
5. 明确标注 `checkPreDegradation()` 为 `private` 方法且无子类重写，消除对 T26 范围的疑问。
6. 标注 T5 和 T28 在 `executeStandardPipeline()` 中的叠加重合区域，降低合并冲突概率。
