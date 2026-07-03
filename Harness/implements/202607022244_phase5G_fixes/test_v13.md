# 测试报告（v13）

## 概述

验证 R12 RETRY（修复 DiscussionConclusionCapabilityExecutorTest.java 末尾多余 `}`）及 T62/T54/T55 三项模板管理修复：promptVersion 类型 String→Integer 下溯全部调用链；DatabasePromptTemplateManager 缓存键包含版本号并在 resolveExactVersion 中先查缓存（T54）；warmup 同时缓存 version 键（T55）。补齐 3 项缺失测试覆盖 T54 和 T55。

## 验证结果

| 测试文件 | 状态 | 说明 |
|---------|------|------|
| `orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java` | ✓ 已修复 | 删除 line 439 多余 `}`，文件末尾仅保留单个类关闭 `}` |
| `template/PromptTemplateManagerTest.java` | ✓ 已适配 | `render(..., "2")` → `render(..., 2)`（3 处） |
| `template/DatabasePromptTemplateManagerTest.java` | ✓ 已适配 + 补充 | `"2"`→`2`（3 处）；`invalidVersionStringShouldFallbackToActive` → `nullVersionShouldFallbackToActive`；新增 T54/T55 覆盖（3 测试） |
| `orchestrator/AbstractCapabilityExecutorTest.java` | ✓ 已适配 | 23 处 `"v1"`→`1`，2 处 doDegrade 签名 `String`→`Integer` |

## 新增测试明细

### DatabasePromptTemplateManagerTest（T55）

**`warmupShouldCreateVersionCacheKeys`** — 验证 warmup 为 version > 0 的模板创建版本缓存键。`activeDept1.version=1`，warmup 后以 `promptVersion=1` 调用 render 应命中缓存，不触发 DB 查询。

**`warmupWithDefaultVersionZeroShouldNotCreateVersionKey`** — 验证 version=0（默认值）的模板不创建版本缓存键。warmup 后 base 键可用，但版本键不存在。

### DatabasePromptTemplateManagerTest（T54）

**`exactVersionShouldHitCacheOnSecondCall`** — 验证 `resolveExactVersion` 在首次 DB 查询后将结果写入缓存，第二次调用命中缓存。DB 仅调用一次。

## 行为契约覆盖

| 组件 | 契约 | 覆盖状态 |
|------|------|---------|
| DiscussionConclusionCapabilityExecutorTest | 无多余语法错误 `}` | ✓ |
| PromptTemplateManager.render() | 第 4 参数类型 `Integer`，null 表示 active | ✓ |
| DatabasePromptTemplateManager.resolveExactVersion() | 先查缓存后查 DB；查得后写入缓存（仅 ACTIVE） | ✓（新增 T54 测试） |
| DatabasePromptTemplateManager.warmup() | 每个 active 模板写入两个缓存键：active 键 + version 键 | ✓（新增 T55 测试） |
| AbstractCapabilityExecutor | AiCallRecord 构造时 `Integer → String` 转换；方法签名 `Integer` | ✓ |
