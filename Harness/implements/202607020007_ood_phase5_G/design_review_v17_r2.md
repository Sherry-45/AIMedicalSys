# 设计审查报告（v17 r2）

## 审查结果
REJECTED

## 发现

### **[严重]** render() 全局模板兜底查询未限定 departmentId IS NULL

`detail_v17.md:205` render() 步骤 2e 的兜底查询：

```
DB 查询 `repository.findByCapabilityIdAndStatus(capabilityId, ACTIVE)`（全局模板兜底）
```

`findByCapabilityIdAndStatus` 不区分模板的 departmentId，会返回该 capabilityId 下 *所有科室* 的 ACTIVE 模板（包括 deptA、deptB 的科室特定模板），而不是仅返回 departmentId IS NULL 的全局模板。

**影响场景**：假设能力 "triage" 已配置 deptA 和 deptB 的科室模板 + 全局兜底模板（departmentId=null），当调用 `render("triage", "deptC", vars, null)` 时：
1. 步骤 2d 查 deptC 的科室模板 → 未找到
2. 步骤 2e 查出 3 条记录，设计未指定选择策略 → 实现者可能取首位，若 deptA 模板排在全局模板之前，则返回了错误的模板

**期望修正**：将查询改为限定 departmentId IS NULL：
- 方案 A：使用已有方法 `repository.findByCapabilityIdAndDepartmentIdAndStatus(capabilityId, null, ACTIVE)`（Spring Data JPA 传入 null 自动生成 `departmentId IS NULL`）
- 方案 B：在 Repository 中新增 `findByCapabilityIdAndDepartmentIdIsNullAndStatus(String capabilityId, TemplateStatus status)` 方法

---

### **[一般]** PromptTemplate 实体 content 和 status 字段缺少 nullable = false

`detail_v17.md:86-95` 字段定义：

```java
@Column(columnDefinition = "TEXT")
private String content;

@Enumerated(EnumType.STRING)
@Column(length = 20)
private TemplateStatus status;
```

- `content`（String）：若数据库中为 null，render() 在执行占位符替换时对 null 调用 `.replace()` 将抛出 NPE
- `status`（引用类型）：若为 null，在 render() 中比较 `status == ACTIVE` 时抛出 NPE；设计虽声明"new 时 status 默认 DRAFT"，但无字段初始化器或构造器保障，new + setter 模式中忘记 setStatus 将产生 null

**期望修正**：
- `content` 添加 `nullable = false`
- `status` 添加 `nullable = false` + 字段初始化 `private TemplateStatus status = TemplateStatus.DRAFT;`

---

### **[一般]** 测试场景未覆盖 promptVersion 分支的关键路径

`detail_v17.md:341-349` 仅列出 8 个测试场景，但 render() 的 `promptVersion != null` 分支有以下 4 条未经测试覆盖的路径：
1. `promptVersion != null` 且精确版本找到且 status=ACTIVE → 使用该版本
2. `promptVersion != null` 且版本存在但 status!=ACTIVE → WARN 日志 + 回退到 ACTIVE
3. `promptVersion != null` 且版本不存在 → 回退到 ACTIVE（含先查科室级→再查全局级）
4. `promptVersion != null` 但 `Integer.parseInt` 抛出异常 → WARN + 回退到 ACTIVE

以及边界路径：
5. render() 所有查询无结果 → 返回 null

**期望修正**：为 DatabasePromptTemplateManagerTest 补充 5 条测试用例覆盖上述路径，确保 A/B 实验按版本查询的代码路径在编码阶段即被验证。

---

### **[轻微]** onTemplateChanged() 操作描述自相矛盾

`detail_v17.md:223-227` 步骤 3 称"遍历全部缓存条目，清除匹配 capabilityId 的条目"，步骤 4 注释称"departmentId=null 时 invalidateAll() 后重新 warmup"。两者是不同实现——前者选择性清除，后者全量清除+重建。步骤 3 的描述与步骤 4 注释相矛盾。

**期望修正**：统一描述，采纳步骤 4 的 invalidateAll() + warmup 方案（因 Caffeine 不支持按前缀遍历）。

---

### **[轻微]** 测试文件 mock 行号可能因 v16 修改而偏移

`detail_v17.md:329-331` 标注的 AbstractCapabilityExecutorTest.java 行号（430, 451, 512, ...）：v16 已在该文件的相同区域执行了 15 处 ModelRouter lambda 替换（1 行→1 行，不影响偏移），但 v17 的 PromptTemplateManager lambda 替换涉及 1 行→3 行的展开，后续行号将依次偏移。

同理 `DiscussionConclusionCapabilityExecutorTest.java` 和 `TriageCapabilityExecutorTest.java` 也已在 v16 中修改（各 3 处和 1 处 ModelRouter lambda 替换）。

**期望修正**：标注"行号仅供参考，实际位置以代码中 `(key, vars) ->` 模式搜索为准"。
