# 任务指令（v22）

## 动作
NEW

## 任务描述

### 1. JsonStructuredOutputParser 实现（新建）
在 `ai-impl/parser/` 包下新建 `JsonStructuredOutputParser.java`，实现 `StructuredOutputParser` 接口：

**类签名**：`public class JsonStructuredOutputParser implements StructuredOutputParser`

**构造器**：`public JsonStructuredOutputParser(ObjectMapper objectMapper)` — 通过构造器注入 Jackson ObjectMapper

**parse() 方法实现**：
```java
@Override
public <T> T parse(String rawContent, Class<T> targetClass) {
    if (rawContent == null || rawContent.isBlank()) {
        throw new IllegalArgumentException("rawContent must not be null or blank");
    }
    Objects.requireNonNull(targetClass, "targetClass must not be null");
    try {
        return objectMapper.readValue(rawContent, targetClass);
    } catch (JsonProcessingException e) {
        throw new RuntimeException("Failed to parse JSON content to " + targetClass.getSimpleName(), e);
    }
}
```

- null/blank rawContent → `IllegalArgumentException`
- null targetClass → `NullPointerException`（Objects.requireNonNull）
- 格式错误的 JSON → `RuntimeException` 包裹 Jackson `JsonProcessingException`，日志 WARN 记录

**现有接口不变**（`StructuredOutputParser.java` 不修改）：
```java
public interface StructuredOutputParser {
    <T> T parse(String rawContent, Class<T> targetClass);
}
```

### 2. 测试文件
**新建 `JsonStructuredOutputParserTest.java`**（在 `ai-impl/src/test/.../parser/`），覆盖：
1. `shouldParseValidJsonString` — 输入 `"\"hello\""` + `String.class` → 返回 `"hello"`
2. `shouldParseValidJsonObject` — 输入 JSON `{"name":"test"}` + `Map.class` → 返回 Map，key="name"，value="test"
3. `shouldParseToCustomDto` — 定义内部静态 DTO 类（含 name、value 字段），验证字段映射正确
4. `shouldThrowExceptionWhenRawContentIsNull` — null → `IllegalArgumentException`
5. `shouldThrowExceptionWhenRawContentIsBlank` — `""` 或 `"  "` → `IllegalArgumentException`
6. `shouldThrowExceptionWhenTargetClassIsNull` — null targetClass → `NullPointerException`
7. `shouldThrowExceptionWhenMalformedJson` — 非 JSON 字符串 → `RuntimeException`
8. `shouldThrowExceptionWhenTypeMismatch` — JSON 数组 `[1,2,3]` + `String.class` → `RuntimeException`（Jackson 类型不匹配）

### 3. 涉及文件清单

| 操作 | 文件路径 |
|------|---------|
| 新建 | `ai-impl/.../parser/JsonStructuredOutputParser.java` |
| 新建 | `ai-impl/.../parser/JsonStructuredOutputParserTest.java` |

## 选择理由

Batch7 P3 首项。StructuredOutputParser 是 LLM 结构化输出解析的接口（被 AbstractCapabilityExecutor 标准管线引用），JsonStructuredOutputParser 是唯一实现，零外部代码依赖（仅需已有 Jackson + ObjectMapper），可独立测试。底层依赖优先原则，先实现解析层再推动上层回退逻辑（Task 21 LocalRuleFallback 依赖 JSON 解析能力）。

## 任务上下文

### 设计文档摘录（06_ood_phase5_G.md §3.6）

```
StructuredOutputParser — 结构化输出解析契约（interface，归属 ai-impl/parser/）
职责：定义从 LLM 原始文本输出中解析出 Java DTO 的统一协议。
协作对象：
  - 被 CapabilityExecutor 在结果解析步骤中调用
  - JsonStructuredOutputParser（默认实现）：假设 LLM 输出为 JSON 格式，基于 Jackson 反序列化
```

### 已有代码上下文

- `StructuredOutputParser.java` — 现有接口，方法签名为 `<T> T parse(String rawContent, Class<T> targetClass)`（不修改，保持向后兼容）
- `AbstractCapabilityExecutor.java:447` — 管线步骤 6 调用 `structuredOutputParser.parse(rawContent, outputType)`，`rawContent` 为 `LlmChatResponse.getContent()` 的返回值（String），`outputType` 为能力对应的 DTO Class
- `StructuredOutputParserTest.java` — 现有匿名类测试（验证匿名类编译正确性），与新测试互补
- Jackson ObjectMapper — 已有依赖（`ai-impl/pom.xml` 依赖 `spring-boot-starter-json`，Jackson 2.x 已作为 transitive dependency 存在）
