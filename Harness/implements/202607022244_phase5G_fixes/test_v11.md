# 测试报告（v11）

## 概述

验证 R10 RETRY（AiPlatformConfigTest 2 处断言修复）+ R11（6 项数据模型/值对象不可变性与设计一致性缺陷修复），确认生产代码变更与现有测试一致，补齐 2 项缺失测试。

## 验证结果

| 测试文件 | 状态 | 说明 |
|---------|------|------|
| `ai-api/.../AiResultTest.java` | ✓ 已适配 | 无 setter 测试，包含 `degradedWithErrorCode` 工厂测试，保留 8 个测试 |
| `ai-api/.../degradation/DegradationContextTest.java` | ✓ 已适配 + 补充 | assertNull→assertEquals(0) 已调整；新增 `shouldCreateBuilderViaStaticFactory` |
| `ai-api/.../dto/base/Phase4BusinessExceptionTest.java` | ✓ 补充 | 新增 `shouldConstructWithMessageAndErrorCode`、`shouldReturnNullErrorCodeWhenConstructedWithoutErrorCode` |
| `ai-impl/.../client/ChatToolDefinitionTest.java` | ✓ 已适配 | 无 setStrict 测试，含 `shouldDefaultToStrictTrueInSerialization` |
| `ai-impl/.../client/LlmChatOptionsTest.java` | ✓ 已适配 | 无 setter 测试，含全参构造/序列化/反序列化测试 |
| `ai-impl/.../config/AiPlatformConfigTest.java` | ✓ 已修复 | assertArrayEquals + getDeclaredMethod 已修正 |
| `ai-impl/.../metrics/SlidingWindowMetricsStoreTest.java` | ✓ 已适配 | assertNull→assertEquals(0) 已调整 |

## 新增测试明细

### DegradationContextTest

**`shouldCreateBuilderViaStaticFactory`** — 验证 `DegradationContext.Builder.builder()` 返回可用 Builder 实例，链式调用后 build() 产生正确对象。

### Phase4BusinessExceptionTest

**`shouldConstructWithMessageAndErrorCode`** — 验证 `(message, errorCode)` 构造器正确设置 message 和 errorCode。
**`shouldReturnNullErrorCodeWhenConstructedWithoutErrorCode`** — 验证未传 errorCode 的构造器返回 null。

## 行为契约覆盖

| 组件 | 契约 | 覆盖状态 |
|------|------|---------|
| AiResult | 字段不可变；无参构造 degraded=false；degradedWithErrorCode → degraded=true | ✓ |
| DegradationContext | int 原始类型；isInitialized 正数检测；Builder.builder() 静态工厂 | ✓ |
| Phase4BusinessException | 3 种构造方式（含 message+errorCode）；getErrorCode() | ✓ |
| ChatToolDefinition | strict 对外只读；Jackson 可反射写入 | ✓ |
| LlmChatOptions | 7 字段不可变；@JsonProperty 全参构造；无参构造全 null | ✓ |
| AiPlatformConfig | @ConditionalOnProperty.name() String[] 用 assertArrayEquals；getDeclaredMethod | ✓ |
