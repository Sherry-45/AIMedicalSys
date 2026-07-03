# 实现报告（v15）

## 概述

完成 R15 全部 4 个子任务：AiRouterPropertiesTest 修复（A）、DefaultCredentialProvider 语义注释（B-T49）、PrescriptionLocalRuleFallback 健壮性（C-T64/T65）、JsonStructuredOutputParser 专用异常（D-T66）。Phase 5G 全部 64 项问题修复完毕。

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `router/AiRouterPropertiesTest.java` | 6处 `.getMessage()` → `.getFormattedMessage()`；1处 `List.of` → `Collections.singletonList`；新增 `import java.util.Collections` |
| 修改 | `client/DefaultCredentialProvider.java` | 添加语义注释 |
| 修改 | `fallback/PrescriptionLocalRuleFallback.java` | T64：`fallback()` try-catch 包裹 + CHECK_SKIPPED；T65：checkAllergy drugName 回退；新增 Logger 字段及 import |
| 新建 | `parser/StructuredOutputParseException.java` | 专用异常类 extends RuntimeException |
| 修改 | `parser/JsonStructuredOutputParser.java` | RuntimeException → StructuredOutputParseException |
| 修改 | `parser/JsonStructuredOutputParserTest.java` | 2处 `assertThrows` 类型更换 |

## 编译验证

`mvn compile test-compile -q` 通过，无错误。

## 设计偏差说明

| 设计规格 | 偏差原因 | 实际处理 |
|---------|---------|---------|
| `PrescriptionLocalRuleFallback.fallback()` 中使用 `log.warn` | 设计未声明 Logger 字段，该类原无 Logger | 新增 `private static final Logger log` 字段及 `org.slf4j` import |
