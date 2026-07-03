# 测试报告（v1）

## 概述
根据详细设计 v1 的 6 个行为契约（T22、T58、T1、T2、T18、T34），编写了 Phase4ServiceFacadeConfig 单元测试及 6 个薄适配器单元测试。覆盖正常路径、边界条件、错误路径及状态交互。

## 测试文件清单

| 文件路径 | 被测类型 | 用例数 |
|---------|---------|--------|
| `ai-impl/.../config/Phase4ServiceFacadeConfigTest.java` | Phase4ServiceFacadeConfig | 6 |
| `ai-impl/.../thinadapter/DiagnosisCapabilityExecutorTest.java` | DiagnosisCapabilityExecutor | 11 |
| `ai-impl/.../thinadapter/ImageAnalysisCapabilityExecutorTest.java` | ImageAnalysisCapabilityExecutor | 6 |
| `ai-impl/.../thinadapter/AnalysisReportForLabTestCapabilityExecutorTest.java` | AnalysisReportForLabTestCapabilityExecutor | 6 |
| `ai-impl/.../thinadapter/AnalysisReportForInspectionCapabilityExecutorTest.java` | AnalysisReportForInspectionCapabilityExecutor | 6 |
| `ai-impl/.../thinadapter/RecommendExecutionOrderCapabilityExecutorTest.java` | RecommendExecutionOrderCapabilityExecutor | 6 |
| `ai-impl/.../thinadapter/RecommendExaminationCapabilityExecutorTest.java` | RecommendExaminationCapabilityExecutor | 6 |

## 契约覆盖

| 契约 | 测试覆盖说明 |
|------|-------------|
| T22: inputType 移除 | AbstractCapabilityExecutorTest（已有）+ 各薄适配器 `getInputType()` 返回正确类型字面量 |
| T58: @Service 注解 | ModelEndpointHealthManagerTest（已有）+ @Service 不影响已有行为 |
| T1: isDtoEmpty() | 各薄适配器：Phase4 包 DTO 触发降级 / ai-api DTO 正常执行 |
| T2: 集中反射调用 | Phase4ServiceFacadeConfigTest：null 服务抛 IllegalStateException、成功委托、InvocationTargetException 展开、checked 异常包装 |
| T18: 包移动 | 隐式覆盖：测试包路径与源包路径一致 |
| T34: llmCallExecutor | 各薄适配器：CompletableFuture.supplyAsync 使用底座专用线程池 |

## 用例说明

### Phase4ServiceFacadeConfigTest（6 用例）
- 正常路径：diagnosisPhase4Service 正确委托、imageAnalysisPhase4Service 正确委托
- 错误路径：null 服务抛 IllegalStateException、RuntimeException 从 InvocationTargetException 展开、checked 异常包装为 RuntimeException、无匹配方法抛 RuntimeException
- 边界条件：通过匿名 Object 模拟 Phase4 服务，验证反射机制

### 薄适配器测试（各 6-11 用例）
- 正常路径：Phase4ServiceFacade 成功返回 → AiResult.success
- 错误路径：IllegalStateException → 降级、Phase4BusinessException → PHASE4_ 错误码、超时 → ThinAdapterTimeout 降级
- 边界条件：isDtoEmpty 对 ai-api DTO 返回 false、对 Phase4 原生 DTO 返回 true、thinAdapterPerCapabilityConfig 为 null 时回退到默认超时
- 状态交互：getCapabilityId/getInputType/getOutputType 返回正确值
