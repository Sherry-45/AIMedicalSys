# 实现报告（v14）

## 概述

实现 R14 路由与实验管理修复，涵盖四个子任务：A—Mockito 原始类型匹配修复；B—全量重编译（无源码变更）；C—路由模块 T41/T42/T48 三项修复；D—实验管理 T56/T57/T63 三项修复。

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `ai-impl/src/test/java/.../template/DatabasePromptTemplateManagerTest.java` | A：2处 `any()`→`anyString()/anyInt()` |
| 修改 | `ai-impl/src/main/java/.../router/ModelRouter.java` | C-T41：参数类型 `Object request`→`ExperimentAssignment assignment` |
| 修改 | `ai-impl/src/main/java/.../router/DefaultModelRouter.java` | C-T41：同上；C-T42：`@Scheduled` 加 `scheduler="scheduledTaskExecutor"` |
| 修改 | `ai-impl/src/main/java/.../router/AiRouterProperties.java` | C-T48：添加 Logger + catch 块 `log.warn` |
| 修改 | `ai-impl/src/test/java/.../orchestrator/AbstractCapabilityExecutorTest.java` | C-T41：3处 lambda 参数名 `req`→`assignment` |
| 修改 | `ai-impl/src/main/java/.../orchestrator/AbstractCapabilityExecutor.java` | 设计遗漏：`route()` 调用第二参数由 `null`→`ExperimentAssignment.createDefault()` |
| 修改 | `ai-impl/src/main/java/.../experiment/HashBucketExperimentManager.java` | D-T56：`min().reversed()`→`max()`；D-T57：loader 排序；D-T63：使用 `findByCapabilityIdAndStatusWithGroups` |
| 修改 | `ai-impl/src/main/java/.../experiment/Experiment.java` | D-T63：`FetchType.EAGER`→`FetchType.LAZY` |
| 修改 | `ai-impl/src/main/java/.../experiment/ExperimentRepository.java` | D-T63：新增 `@Query JOIN FETCH` 方法 |

## 编译验证

`mvn clean compile test-compile -pl ai-api,ai-impl -am` — **BUILD SUCCESS**

## 设计偏差说明

| 设计规格 | 偏差原因 | 实际处理 |
|---------|---------|---------|
| 设计未提及 `AbstractCapabilityExecutor.java` 的调用方适配 | 设计遗漏：`ModelRouter.route()` 签名为 `route(String, ExperimentAssignment)` 后，原调用 `modelRouter.route(capabilityId, request)` 中的 `request`（类型 `T`）无法赋值给 `ExperimentAssignment` | 从 `null` 改为 `ExperimentAssignment.createDefault()` |

## 修订说明（v14 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| `AbstractCapabilityExecutor.java` 中 `route(capabilityId, null)` 存在空指针风险，应替换为 `ExperimentAssignment.createDefault()` | ① 源码：新增 import + 替换 `null`→`ExperimentAssignment.createDefault()`；② 设计文档 detail_v14.md：补入文件变更和依赖关系；③ 实现报告：更新条目和偏差说明 |
