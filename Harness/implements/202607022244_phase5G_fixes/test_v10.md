# 测试报告（v10）

## 变更汇总

| 测试文件 | 操作 | 对应设计 |
|---------|------|---------|
| `LlmChatRequestTest.java` | 无变更 | RETRY：6 参构造器移除 @JsonProperty，现有测试应全部通过 |
| `AiPlatformConfigTest.java` | 修改 | T8 + T9 + T43 |
| `AiPlatformEnvironmentPostProcessorTest.java` | 修改 | T10 |
| `FallbackAiServiceTest.java` | 修改 | T45 |

## AiPlatformConfigTest.java 变更

### T8 — 新增类级 @ConditionalOnProperty 验证
- `classShouldBeAnnotatedWithConditionalOnProperty()`: 验证 `@ConditionalOnProperty(name="ai.platform.enabled", havingValue="true", matchIfMissing=false)` 存在于类上

### T9 — 新增 ObjectProvider / @ConditionalOnClass 验证
- `delegatingLlmChatServiceShouldUseObjectProvider()`: 验证 `delegatingLlmChatService` 参数为 `ObjectProvider<HttpApiLlmChatService>` 和 `ObjectProvider<SpringAiLlmChatService>`，当 provider 返回 null 时仍正常创建
- `delegatingLlmChatServiceShouldRegisterAvailableDelegates()`: 验证 provider 有值时构建正常
- `httpApiLlmChatServiceShouldUseObjectProvider()`: 验证 `httpApiLlmChatService` 参数改为 `ObjectProvider<CredentialProvider>` 和 `ObjectProvider<EndpointRateLimiter>`
- `springAiLlmChatServiceShouldBeAnnotatedWithConditionalOnClass()`: 验证 `springAiLlmChatService()` 方法有 `@ConditionalOnClass(name="org.springframework.ai.chat.ChatModel")`

### T43 — 更新 refreshCapabilityTimeoutConfig 测试
- `refreshCapabilityTimeoutConfigShouldRefreshFromBean()`: 替换旧测试。新方法调用 `applicationContext.getBean(AiExecutionProperties.class)` 而非 `Binder.get(env)`

### 导入清理
- 移除未使用的 `MapPropertySource`, `StandardEnvironment`, `ClientType` 导入

## AiPlatformEnvironmentPostProcessorTest.java 变更

### T10 — 新增日志验证
- `shouldLogWarningWhenForwardingMockEnabled()`: 验证 `postProcessEnvironment` 在设置 `ai.mock.enabled` 前输出 WARN 级别日志

## FallbackAiServiceTest.java 变更

### T45 — fail-fast 行为变更
- 全部 `new FallbackAiService(provider, false)` 改为 `new FallbackAiService(provider)`（移除 `aiPlatformEnabled` 参数）
- `shouldThrowWhenNoDelegateAvailable()`: 替换全部 14 个"无 delegate 时降级"测试，验证构造器抛出 `IllegalStateException`
- 移除 `shouldLogErrorOnConstruction()` 和 `shouldLogWarnOnSubsequentCalls()`（构造器已 fail-fast，不再可能到达这些路径）
- 保留全部"正向委托"测试

## 未变更

- `LlmChatRequestTest.java`: 6 参构造器移除 `@JsonProperty` 后，Jackson 反序列化路径为 5 参构造器，现有测试 `shouldDeserializeFromJson` 和 `shouldRoundTripThroughJson` 应照常通过
