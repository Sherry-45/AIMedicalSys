# 测试审查报告（v18 r1）

## 审查结果
APPROVED

## 发现

- **[轻微]** `test_v18.md` — 测试报告文件内容为空（仅标题），无法反映测试执行结果。不影响测试代码质量，但应按流程补充。
- **[轻微]** `code_v18.md` — 声称 `HashBucketExperimentManagerTest` 有 13 条测试，实际测试文件包含 17 条。多出的 4 个测试（`shouldUseLatestExperimentWhenMultipleActive`、`warmupExceptionShouldNotBlockStartup`、`shouldReturnDefaultWhenExperimentHasNullGroups`、`shouldReturnDefaultWhenExperimentHasEmptyGroups`）覆盖了设计契约中的重要边界和异常场景，是正向贡献，但报告计数应与代码一致。
- **[轻微]** `ExperimentAssignmentTest.java:56-60` — `equalsAndHashCodeShouldNotBeImplemented` 仅验证了 `equals()` 为引用比较（`assertFalse(ea1.equals(ea2))`），未显式验证 `hashCode()` 也未在注释中说明不覆写 `equals` 时 `hashCode` 自然也不覆写。建议补充 `assertNotEquals(ea1.hashCode(), ea2.hashCode())` 或内联注释阐明。
- **[轻微]** `HashBucketExperimentManagerTest.java:106-123` — `shouldHandleNegativeHashValue` 中包含对 `Math.floorMod(-1, 1000)`、`Math.floorMod(-1000, 1000)`、`Math.floorMod(Integer.MIN_VALUE, 1000)` 的直接断言。这些是 JDK API 行为，非被测方法（`HashBucketExperimentManager.assign()`）的内部实现，属于过度测试。建议移除这三行，依赖 `"zzzzzzzz"` 的 `assign()` 调用已能验证负哈希分支。另外 `assign()` 的断言仅 `assertNotNull(result.getGroupId())`，未验证哈希映射的具体目标分组，可考虑添加确定性断言。
- **[轻微]** `HashBucketExperimentManagerTest.java:224-230` — `dbExceptionShouldReturnDefault` 验证了异常时返回 `createDefault()`，但设计契约（detail_v18.md:286）约定了 "catch 后 WARN 日志"，该测试未验证日志行为（未 mock Logger 或使用日志 Appender 断言）。建议补充日志验证。
- **[轻微]** `HashBucketExperimentManagerTest.java:201-222` — `concurrentAssignShouldNotThrow` 仅验证并发下无异常，未验证结果的正确性（如无数据竞争、缓存一致性）。建议增加对返回结果非空、分组落在预期范围内的断言。
