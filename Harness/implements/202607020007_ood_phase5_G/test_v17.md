# 测试报告（v17）

## 概述

根据详细设计 v17 的行为契约对 PromptTemplate JPA 实体、DatabasePromptTemplateManager 缓存/事件/兜底/并发/promptVersion 分支编写单元测试。测试覆盖正常路径、边界条件、错误路径、状态交互。

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 保留 | `src/test/java/com/aimedical/modules/ai/impl/template/PromptTemplateTest.java` | 实体契约测试（8 条用例） |
| 修改 | `src/test/java/com/aimedical/modules/ai/impl/template/DatabasePromptTemplateManagerTest.java` | 新增 2 条用例：全局失效事件 + DB 异常返回 null |
| 保留 | `src/test/java/com/aimedical/modules/ai/impl/template/PromptTemplateTestConfig.java` | @SpringBootApplication 配置 |

## 测试覆盖分析

### PromptTemplateTest（8 条）

| 测试方法 | 契约维度 | 覆盖内容 |
|---------|---------|---------|
| shouldConstructAndGetProperties | 正常路径 | 全参构造器 + getter |
| shouldDefaultStatusToDraft | 正常路径 | 无参构造器 status 默认 DRAFT |
| shouldSetPropertiesViaSetters | 正常路径 | setter 设置字段 |
| equalsAndHashCodeShouldBeBasedOnId | 正常路径 | equals/hashCode 基于 id |
| equalsShouldReturnFalseForDifferentIds | 边界条件 | id 不同则不等 |
| uniqueConstraintShouldPreventDuplicateTriple | 错误路径 | (capabilityId, departmentId, version) 唯一约束 |
| nullableDepartmentIdShouldBeAllowed | 边界条件 | departmentId 可为 null |
| shouldPersistAndRetrieveEnumAsString | 状态交互 | EnumType.STRING 持久化 |

### DatabasePromptTemplateManagerTest（15 条）

| 测试方法 | 契约维度 | 覆盖内容 | 设计依据 |
|---------|---------|---------|---------|
| shouldReturnCachedContentOnCacheHit | 正常路径 | 缓存命中，不查询 DB | 缓存设计 |
| shouldQueryDbOnCacheMiss | 正常路径 | 缓存未命中 → DB → 填充缓存 → 替换占位符 | 缓存设计 |
| warmupShouldPrepopulateCache | 正常路径 | @PostConstruct 缓存预填充 | warmup() |
| eventShouldInvalidateCacheEntry | 状态交互 | 事件失效（departmentId 非 null） | onTemplateChanged() |
| **eventWithNullDepartmentIdShouldInvalidateAllAndRewarm** | **状态交互** | **事件失效（departmentId=null → invalidateAll + warmup）** | **onTemplateChanged() 分支** |
| fallbackShouldReturnConfiguredValue | 正常路径 | 有配置时返回配置值 | getFallbackPrompt() |
| fallbackShouldReturnDefaultWhenNoConfig | 正常路径 | 无配置时返回通用兜底 | getFallbackPrompt() |
| concurrentRenderShouldNotThrow | 边界条件 | 多线程并发不抛异常 | 并发安全 |
| nullVariableValueShouldRetainPlaceholder | 边界条件 | null value 保留占位符 + WARN 日志 | replacePlaceholders() |
| exactVersionActiveShouldReturnVersionContent | 正常路径 | 精确版本 ACTIVE，不走缓存 | promptVersion 分支 |
| nonActiveVersionShouldFallbackToActive | 状态交互 | 版本非 ACTIVE → WARN + 回退到 ACTIVE 模板 | promptVersion 分支 |
| versionNotFoundInDepartmentShouldFallbackToGlobal | 状态交互 | 科室级未找到 → 查全局模板 | promptVersion 分支 |
| invalidVersionStringShouldFallbackToActive | 错误路径 | 版本解析异常 → WARN + 回退到 ACTIVE 模板 | promptVersion 分支 |
| allQueriesReturnNullShouldReturnNull | 边界条件 | 全部路径无结果 → 返回 null | promptVersion 分支 |
| **dbExceptionShouldReturnNull** | **错误路径** | **DB 异常 → catch 后 WARN 日志 + 返回 null** | **错误处理** |

**加粗**：本轮新增测试。

## 设计偏差说明

- 无偏差。所有测试基于详细设计的行为契约编写，未引入实现细节依赖。
- 新增 2 条测试覆盖了原始设计遗漏的分支（departmentId=null 全量失效 + DB 异常错误路径）。

## 测试命令

```bash
# 运行全部 ai-impl 模块测试
mvn test -pl AIMedical/backend/modules/ai/ai-impl

# 仅运行模板相关测试
mvn test -pl AIMedical/backend/modules/ai/ai-impl -Dtest="*PromptTemplate*,*DatabasePromptTemplateManager*"
```
