# 代码审查报告（v18 r3）

## 审查结果
REJECTED

## 发现
- **[一般]** `HashBucketExperimentManager.java:110` — 设计文档规定异常路径返回 `createDefault()`（groupId="default"），但代码返回 `createErrorFallback()`（groupId="experiment-error"），两者不一致

## 修改要求（仅 REJECTED 时）
### 问题1：异常路径返回值偏离设计
- **位置**：`HashBucketExperimentManager.java` 第 110 行
- **问题**：设计文档 `detail_v18.md` 第 262 行（assign 流程步骤 8）及第 287 行（错误处理节）均明确规定所有 DB/异常路径 catch 后返回 `createDefault()`。但代码中 catch 块返回的是 `createErrorFallback()`。
- **为什么是问题**：下游管线可能依赖 `groupId="default"` 作为 "无实验命中" 的标识；返回 `groupId="experiment-error"` 将导致下游无法识别该路径，且与设计契约不符。测试 `dbExceptionShouldReturnDefault`（第 228-229 行）也追随了错误实现，验证了 `createErrorFallback()`，构成系统性的设计偏离。
- **期望的修正方向**：二者择一：
  a) 将 `HashBucketExperimentManager.java:110` 改为 `return ExperimentAssignment.createDefault();`，并相应修正测试 `dbExceptionShouldReturnDefault` 的断言为 `assertEquals("default", result.getGroupId())`；**或**
  b) 更新详细设计文档，将异常路径的返回值规范正式改为 `createErrorFallback()`，以反映当前实现意图。
