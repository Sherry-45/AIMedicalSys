# 任务指令（v13）

## 动作
RETRY + NEW

## 任务描述
### RETRY（R12）：修复 DiscussionConclusionCapabilityExecutorTest.java 编译错误
- 删除 `DiscussionConclusionCapabilityExecutorTest.java:439` 多余的右花括号 `}`（文件末尾双重关闭，行 438 已有关闭类的 `}`）

### NEW（R13）：模板管理——T62 + T54 + T55
- **T62**: `PromptTemplateManager.render()` 签名 `String promptVersion` → `Integer promptVersion`，同步下溯至 `DatabasePromptTemplateManager`、`AbstractCapabilityExecutor` 3 个方法签名、8 个子类字段类型
- **T54**: `DatabasePromptTemplateManager` 缓存键包含 `promptVersion`，`resolveExactVersion()` 先查缓存再查 DB
- **T55**: `DatabasePromptTemplateManager.warmup()` 缓存键包含版本号

## 选择理由
R12 失败仅 1 行多余 `}`，修复代价接近零。R13 三项全部集中于 `template/` 包的两个源文件（`PromptTemplateManager` 接口 + `DatabasePromptTemplateManager` 实现），从接口类型变更到底层缓存修复形成完整依赖链，合并一轮避免分段修改造成的中间编译失败。T62 类型变更后 T54/T55 的缓存键自然使用 `Integer` 版本号。

## 任务上下文

### R12 RETRY 上下文
- 文件：`ai-impl/src/test/.../orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java`
- 位置：末尾 line 438 已有关闭类的 `}`，line 439 多余 `}` 导致 `需要 class/interface/enum/record` 编译错误
- 修复：仅删除 line 439
- 该测试已传递 `Executors.newSingleThreadExecutor()` 作为 `transcriptSummaryExecutor` 构造参数（对应 `new DiscussionConclusionCapabilityExecutor(...)` 调用末尾），无其他编译问题

### T62 上下文
- **接口变更**：
  - `PromptTemplateManager.render(String capabilityId, String departmentId, Map<String, Object> variables, String promptVersion)` → `Integer promptVersion`
  - `DatabasePromptTemplateManager.render()` 同步变更
  - `DatabasePromptTemplateManager.resolveExactVersion()` 参数 `String promptVersion` → `Integer promptVersion`，移除 `Integer.parseInt()` 解析过程
- **AbstractCapabilityExecutor 方法签名变更**：
  - `executeStandardPipeline(..., String promptVersion, String sentinelReason)` → `Integer promptVersion`
  - `doDegrade(..., String promptVersion, String modelId, String sentinelReason)` → `Integer promptVersion`
  - `handleSuccess(..., String promptVersion, ...)` → `Integer promptVersion`
  - **注意**：`AiCallRecord` 的公开构造器 `AiCallRecord(String, String, String, ...)` 第三个参数是 `String promptVersion`（内部 parse 为 Integer）。由于上游变为 Integer，AbstractCapabilityExecutor 中 2 处构造调用（line 268 `doDegrade` 内、line 509 `handleSuccess` 内）必须将 `promptVersion` 从 Integer 转为 String：`promptVersion != null ? String.valueOf(promptVersion) : null`
- **8 个子类字段变更**（7 底座 + DiscussionConclusion）：
  - `private String promptVersion;` → `private Integer promptVersion;`
  - `@Value("${ai.prompt.version.XXX:XXX}")` → `@Value("${ai.prompt.version.XXX:0}")`，默认值统一为 `0`（0 版不存在 → resolveExactVersion 返回 null → fallback 到 active）
- **测试适配**：
  - `DatabasePromptTemplateManagerTest`：`render()` 第 4 参 `"2"` → `2`（Integer 字面量）
  - 测试 `invalidVersionStringShouldFallbackToActive`：因 Integer 参数无法传非数字字符串，改为 `nullVersionShouldFallbackToActive`，传 `null` 验证走 active 模板
  - Mockito `when(mockTemplate.render(any(), any(), any(), any()))` 不受影响——`any()` 匹配任意类型
- **AbstractCapabilityExecutorTest.java 同步修改（23 处 executeStandardPipeline 调用 + 2 处 doDegrade 覆盖）**：
  - 所有 23 处 `executeStandardPipeline(..., Map.of(), "v1", null)` 的倒数第二参数 `"v1"` → `1`（Integer 字面量）
  - 第 1 处 `doDegrade` 覆盖（line 340-349）：方法签名 `String promptVersion` → `Integer promptVersion`，并在 `super.doDegrade(..., promptVersion, ...)` 中同步传递 Integer
  - 第 2 处 `doDegrade` 覆盖（line 1530-1537）：方法签名 `String promptVersion` → `Integer promptVersion`，并在 `AiResult.degraded(degradeReason)` 返回语句中同步

### T54 上下文
- **buildCacheKey 改造**：新增重载 `buildCacheKey(capabilityId, departmentId, promptVersion)`：
  ```java
  private static String buildCacheKey(String capabilityId, String departmentId, Integer promptVersion) {
      String base = capabilityId + ":" + (departmentId != null ? departmentId : "");
      if (promptVersion != null) {
          return base + ":v" + promptVersion;
      }
      return base;
  }
  ```
  现有 `buildCacheKey(capabilityId, departmentId)` 保留（内部调用三参版传 null）保持向后兼容。
- **resolveExactVersion 缓存化**：
  ```java
  private PromptTemplate resolveExactVersion(String capabilityId, String departmentId, Integer promptVersion) {
      String key = buildCacheKey(capabilityId, departmentId, promptVersion);
      PromptTemplate cached = cache.getIfPresent(key);
      if (cached != null) return cached;
      // ... 现有 DB 查询逻辑 ...
      // 查得后：cache.put(key, result);
  }
  ```
- `render()` 中 `promptVersion != null` 分支不变，`resolveExactVersion` 内部缓存化后自动受益

### T55 上下文
- **warmup 双键缓存**：
  ```java
  for (PromptTemplate pt : activeTemplates) {
      String key = buildCacheKey(pt.getCapabilityId(), pt.getDepartmentId(), null);
      cache.put(key, pt);
      if (pt.getVersion() != null) {
          String versionKey = buildCacheKey(pt.getCapabilityId(), pt.getDepartmentId(), pt.getVersion());
          cache.put(versionKey, pt);
      }
  }
  ```
  - 预热后 `render(..., 2)` 直接命中版本缓存，不触发 `resolveExactVersion` DB 查询
  - 预热后 `render(..., null)` 命中 active 缓存，行为不变
- **onTemplateChanged 更新**：department 级 invalidation 保持针对 active 键（不逐个清理 version 键）；version 键由 5 分钟 TTL 自动过期。全库 invalidation 时 `cache.invalidateAll()` + `warmup()` 自然重建双键缓存

## 预期文件变更清单
| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `ai-impl/src/test/.../orchestrator/impl/DiscussionConclusionCapabilityExecutorTest.java` | R12 RETRY：删行 439 |
| 修改 | `ai-impl/src/main/.../template/PromptTemplateManager.java` | T62：`String`→`Integer` |
| 修改 | `ai-impl/src/main/.../template/DatabasePromptTemplateManager.java` | T62+T54+T55 |
| 修改 | `ai-impl/src/main/.../orchestrator/AbstractCapabilityExecutor.java` | T62：3 方法签名 + 2 处 AiCallRecord 构造 |
| 修改 | 7 底座 Executor 各 1 行 | T62：字段类型+@Value |
| 修改 | `.../impl/DiscussionConclusionCapabilityExecutor.java` | T62：字段类型+@Value |
| 修改 | `ai-impl/src/test/.../template/DatabasePromptTemplateManagerTest.java` | T62：`"2"`→`2`, `"not-a-number"`→`null` 测试 |
| 修改 | `ai-impl/src/test/.../orchestrator/AbstractCapabilityExecutorTest.java` | T62：23 处 `"v1"`→`1` + 2 处 `String promptVersion`→`Integer` |

## 修订说明（v13 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| T62(promptVersion String→Integer) 变更通过 AbstractCapabilityExecutor 方法签名传播，影响 AbstractCapabilityExecutorTest.java 中 23 处 executeStandardPipeline() 调用和 2 处 doDegrade() 覆盖。需将 AbstractCapabilityExecutorTest.java 加入修改清单，所有 "v1" 改为 1，String promptVersion 改为 Integer。 | 任务上下文中新增 AbstractCapabilityExecutorTest.java 同步修改说明（23 处 `"v1"`→`1` + 2 处 `String promptVersion`→`Integer`）；预期文件变更清单新增 `AbstractCapabilityExecutorTest.java` 行 |
