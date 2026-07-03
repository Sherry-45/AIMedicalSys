# 测试审查报告（v7 r2）

## 审查结果
REJECTED

## 发现

- **[一般]** `ChatToolDefinitionTest.java:71-79` — `shouldRoundTripThroughJson` 构造了含非 null `parameters`（JsonNode）的 `ChatToolDefinition` 并执行序列化/反序列化往返，但仅对 `getName()`、`getDescription()`、`isStrict()` 进行了断言，遗漏了对 `getParameters()` 的验证。`parameters` 是设计契约中定义的 4 个字段之一，且作为最复杂的 `JsonNode` 类型，其数据完整性在往返测试中未被覆盖可能导致 Jackson 处理缺陷无法被捕获。应补充 `assertEquals(original.getParameters(), restored.getParameters())`。

- **[轻微]** `StructuredChatResultTest.java:61-70` — `shouldRoundTripThroughJson` 对嵌套 `LlmChatUsage` 仅验证了 `promptTokens`，未验证 `completionTokens` 和 `totalTokens` 的往返一致性。建议补充完整断言。

- **[轻微]** 多个序列化测试（`LlmChatMessageTest`、`LlmChatOptionsTest`、`ChatToolDefinitionTest` 等）使用 `assertTrue(json.contains(...))` 子串匹配而非精确 JSON 断言。当前测试值简单，风险极低，但长期维护中可能产生假阳性匹配。建议使用 Jackson 的 `JsonNode.equals()` 或 `ObjectMapper.readTree()` 做结构化比较。

## 修改要求（仅 REJECTED 时）

### 问题 1（一般）：ChatToolDefinitionTest.shouldRoundTripThroughJson——缺少 getParameters() 断言

- **位置**：`ChatToolDefinitionTest.java:71-79`，`shouldRoundTripThroughJson` 方法
- **问题**：方法构造了含 `JsonNode` 类型 `parameters` 的 `ChatToolDefinition` 实例，经过 Jackson 序列化/反序列化后，仅验证 name、description、strict 三字段，未验证 `parameters` 字段的往返一致性
- **为什么是问题**：`parameters` 是设计契约 `ChatToolDefinition` 的四字段之一，且为最复杂的嵌套结构类型（`JsonNode`）。缺少该字段断言意味着 Jackson 在处理过程中可能丢失、损坏或改变 `parameters` 的内容而测试无法感知。`shouldDeserializeFromJson` 仅用了 `assertNotNull` 不足以替代结构一致性验证
- **期望的修正方向**：在 `assertEquals(original.getName(), restored.getName());` 行后补充 `assertEquals(original.getParameters(), restored.getParameters());`
