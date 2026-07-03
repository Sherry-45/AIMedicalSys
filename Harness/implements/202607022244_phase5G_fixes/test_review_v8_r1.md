# 测试审查报告（v8 r1）

## 审查结果
REJECTED

## 发现

- **[严重]** `AiCallRecordTest.java:23` — 测试 `shouldConstructWithAllFields()` 使用旧版 15 参构造器传入 `promptVersion="v1"`，断言 `assertEquals("v1", record.getPromptVersion())`。v8 将 promptVersion 类型由 `String` 改为 `Integer`，旧版构造器的 `parsePromptVersion("v1")` 抛出 `NumberFormatException` 返回 `null`，因此 `record.getPromptVersion()` 为 `null`，断言 `"v1".equals(null)` 失败。该测试无法通过编译/运行。

## 修改要求（仅 REJECTED 时）

### 严重问题

**文件**: `AiCallRecordTest.java:23`
**问题**: `shouldConstructWithAllFields()` 中 `assertEquals("v1", record.getPromptVersion())` 与 v8 类型变更不兼容。旧版构造器将 String 参数经 `parsePromptVersion()` 转为 Integer，"v1" 无法解析，返回 null，导致断言失败。
**期望修正方向**: 将 promptVersion 测试值改为可解析的整数字符串，例如 `"2"`，并将断言改为 `assertEquals(Integer.valueOf(2), record.getPromptVersion())`。或者删除该断言（由于 promptVersion 类型已变更，旧版构造器无法保留原始 String 值）。
