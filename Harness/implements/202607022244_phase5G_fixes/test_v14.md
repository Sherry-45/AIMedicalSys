# 测试报告（v14）

## 测试文件清单

| 文件路径 | 操作 | 说明 |
|---------|------|------|
| `ai-impl/src/test/java/.../router/AiRouterPropertiesTest.java` | 新增 | T48：枚举转换失败告警日志行为契约测试 |
| `ai-impl/src/test/java/.../experiment/HashBucketExperimentManagerTest.java` | 修改 | T56/T57：Mock 方法名更新为 `findByCapabilityIdAndStatusWithGroups` + 新增 loader 排序测试 |

## AiRouterPropertiesTest（新增）

**对应子任务**：C-T48 — 枚举转换失败告警日志

**测试用例**：

| 测试方法 | 契约 | 覆盖维度 |
|---------|------|---------|
| `shouldConvertValidClientType` | 有效 ClientType 字符串正确转换为枚举 | 正常路径 |
| `shouldLogWarningForInvalidClientType` | 无效 clientType 记录 WARN 日志并降级为 HTTP_API | 错误路径 |
| `shouldLogWarningForInvalidAuthType` | 无效 authType 记录 WARN 日志并降级为 NONE | 错误路径 |
| `shouldHandleBothInvalidEnums` | 两个枚举同时无效时各记录一条 WARN | 错误路径组合 |
| `shouldNotLogWarningWhenClientTypeIsNull` | null clientType 不触发日志 | 边界条件 |
| `shouldNotLogWarningWhenAuthTypeIsNull` | null authType 不触发日志 | 边界条件 |
| `shouldHandleNullConfigGracefully` | null ModelRouteConfig 不抛异常、不触发日志 | 边界条件 |
| `shouldHandleEmptyRoutes` | 空路由表返回空 Map | 边界条件 |

## HashBucketExperimentManagerTest（修改）

**对应子任务**：D-T56, D-T57, D-T63

### Mock 方法名更新

所有现有测试用例中 `repository.findByCapabilityIdAndStatus()` 替换为 `repository.findByCapabilityIdAndStatusWithGroups()`，与代码变更同步。

### 新增测试用例

| 测试方法 | 契约 | 覆盖维度 |
|---------|------|---------|
| `loaderShouldSortByStartTimeDescAndMaxSelectsCorrectExperiment` | loader 排序 + max() 正确选取最晚实验 | 正常路径（排序+选择） |

## 行为契约覆盖矩阵

| 组件 | 契约 | 覆盖状态 | 测试文件 |
|------|------|---------|---------|
| ModelRouter.route() | 第二参数变更为 ExperimentAssignment；方法体不使用该参数 | ✅ 已有测试覆盖（DefaultModelRouterTest 传 null） | DefaultModelRouterTest |
| DefaultModelRouter.refreshRouteTable() | 绑定 scheduledTaskExecutor 线程池 | ⚠️ Spring 配置，单元测试无法直接验证 | — |
| **AiRouterProperties.convert()** | **枚举转换失败时打印告警日志** | **✅ 新增（8 用例）** | **AiRouterPropertiesTest** |
| **HashBucketExperimentManager.assign()** | **loader 排序与 warmup 一致（按 startTime 降序）** | **✅ 新增 + 已有 `shouldUseLatestExperimentWhenMultipleActive` 覆盖** | **HashBucketExperimentManagerTest** |
| Experiment.groups | Lazy 加载，通过 JOIN FETCH 保证可访问 | ⚠️ JPA 配置，需集成测试（@DataJpaTest） | — |
| ExperimentRepository.findByCapabilityIdAndStatusWithGroups() | 返回的 Experiment 关联 groups 已初始化 | ⚠️ JPA Repository 方法，需集成测试 | — |
