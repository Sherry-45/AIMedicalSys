# 详细设计（v22）

## 概述

实现 `JsonStructuredOutputParser`，作为 `StructuredOutputParser` 接口的默认 JSON 实现，基于 Jackson `ObjectMapper` 完成 LLM 结构化输出解析。新增对应单元测试覆盖正常解析、边界条件和异常场景。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/parser/JsonStructuredOutputParser.java` | 新建 | `StructuredOutputParser` 的 Jackson JSON 实现 |
| `ai-impl/src/test/java/com/aimedical/modules/ai/impl/parser/JsonStructuredOutputParserTest.java` | 新建 | 8 个测试覆盖正常/异常/边界场景 |

## 类型定义

### JsonStructuredOutputParser

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.parser`
**职责**：将 LLM 原始文本输出（假设为 JSON 格式）解析为指定 Java DTO

```java
package com.aimedical.modules.ai.impl.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class JsonStructuredOutputParser implements StructuredOutputParser {

    private static final Logger log = LoggerFactory.getLogger(JsonStructuredOutputParser.class);

    private final ObjectMapper objectMapper;

    public JsonStructuredOutputParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T parse(String rawContent, Class<T> targetClass) {
        if (rawContent == null || rawContent.isBlank()) {
            throw new IllegalArgumentException("rawContent must not be null or blank");
        }
        Objects.requireNonNull(targetClass, "targetClass must not be null");
        try {
            return objectMapper.readValue(rawContent, targetClass);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON content to {}", targetClass.getSimpleName(), e);
            throw new RuntimeException("Failed to parse JSON content to " + targetClass.getSimpleName(), e);
        }
    }
}
```

**公开接口**：
- `JsonStructuredOutputParser(ObjectMapper objectMapper)` — 构造器注入 Jackson ObjectMapper
- `<T> T parse(String rawContent, Class<T> targetClass)` — 解析 JSON 字符串到目标类型

**构造方式**：`new JsonStructuredOutputParser(objectMapper)`
**类型关系**：实现 `StructuredOutputParser` 接口

## 错误处理

| 异常条件 | 异常类型 | 说明 |
|---------|---------|------|
| `rawContent` 为 null 或空白字符串 | `IllegalArgumentException` | 前置校验，立即抛出 |
| `targetClass` 为 null | `NullPointerException` | 通过 `Objects.requireNonNull` 抛出 |
| JSON 格式错误 | `RuntimeException`（包裹 `JsonProcessingException`） | 日志 WARN 记录异常堆栈后包装抛出 |
| JSON 类型不匹配（如 JSON 数组 → String） | `RuntimeException`（包裹 `JsonProcessingException`） | Jackson 抛出 `MismatchedInputException`，同 JSON 格式错误路径 |

## 行为契约

- `parse` 方法无副作用，幂等
- 构造器不校验 `objectMapper` 是否 null（允许空 ObjectMapper 便于极端场景测试，调用 `parse` 时会 NPE）
- `ObjectMapper.readValue` 内部对 `targetClass` 使用 Jackson 标准反序列化（包括 getter/setter、字段注解、`@JsonCreator` 等）
- 对 `String.class` 目标，Jackson 自动将 JSON 字符串字面量（如 `"\"hello\""`）反序列化为 String `hello`

## 依赖关系

### 依赖的已有类型
- `StructuredOutputParser` — 实现的接口（包同级，无需额外引入）
- `com.fasterxml.jackson.databind.ObjectMapper` — 通过 `spring-boot-starter-web` transitive 引入
- `com.fasterxml.jackson.core.JsonProcessingException` — Jackson 反序列化异常基类
- `org.slf4j.Logger` / `org.slf4j.LoggerFactory` — 项目统一日志门面

### 暴露给后续任务
- 被 `AbstractCapabilityExecutor.executeStandardPipeline`（步骤 6）通过 `structuredOutputParser.parse(rawContent, outputType)` 调用
- 通过 `StructuredOutputParser` 接口引用，不直接依赖具体实现类
