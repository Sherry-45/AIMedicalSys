# 计划审查报告（v13 r2）

## 审查结果
REJECTED

## 发现

### **[严重]** 现有 DelegatingLlmChatServiceTest.java 未纳入涉及文件，重构后将编译失败

task_v13.md 将 DelegatingLlmChatService 的构造器从 `List<LlmChatService>` 改为 `Map<ClientType, LlmChatService>`（§类型详细要求 5.），并移除 `initDelegates()` 方法，但"全部涉及文件"表（§全部涉及文件）**未列出 `DelegatingLlmChatServiceTest.java`**。

现有 `DelegatingLlmChatServiceTest.java`（212行，11个测试方法）存在以下断裂点：
- 全部 11 处 `new DelegatingLlmChatService(List.of(...))` → 构造器签名变更后编译失败
- 8 处 `delegating.initDelegates()` 直接调用 → 方法移除后编译失败
- `shouldLogErrorForMissingClientTypeInInitDelegates`、`shouldSkipSelfInInitDelegates`、`shouldNotFailOnEmptyServiceList` 完全基于旧行为，重构后语义不再适用

此问题不修正则实施后 test-compile 必然失败。

### **[一般]** 测试规划未覆盖重构后 DelegatingLlmChatService 行为契约

task_v13.md 测试规划（§测试规划）列出 4 个新测试文件（HttpApiLlmChatServiceTest、SpringAiLlmChatServiceTest、HttpApiLlmChatStreamServiceTest、SpringAiLlmChatStreamServiceTest），但未规划重构后 DelegatingLlmChatService 的关键行为测试：
- Map 构造器防御性拷贝（unmodifiableMap + new HashMap<>）
- chat() / structuredChat() 分发逻辑
- ClientType 为 null / 无对应实现时的回退行为
- getClientType() 返回 null

尽管旧测试注销后这些行为不被直接验证，但重构涉及的行为变更应有明确的测试覆盖。

## 修改要求
1. **[严重]** 将 `DelegatingLlmChatServiceTest.java` 列入涉及文件，适配 `Map<ClientType, LlmChatService>` 构造器；移除 `initDelegates` 相关用例（shouldLogErrorForMissingClientTypeInInitDelegates、shouldSkipSelfInInitDelegates、shouldNotFailOnEmptyServiceList）；保留并调整回退/分发/防御性拷贝用例为新构造器匹配的形态
2. **[一般]** 在测试规划中补充重构后 DelegatingLlmChatService 的分发、回退、防御性拷贝、getClientType() 行为测试
