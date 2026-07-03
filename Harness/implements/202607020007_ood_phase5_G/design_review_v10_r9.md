# 设计审查报告（v10 r9）

## 审查结果
REJECTED

## 发现

### **[一般]** `CredentialUnavailableException` 未纳入文件规划

NORMAL 状态步骤 2b 及状态转换表（§ 行为契约 第 2 行）明确抛出 `CredentialUnavailableException`，该异常也在 OOD 设计文档 §3.2（行 1991）及 §3.2 凭据查询时序中作为标准异常引用。但该类当前不存在于代码库中，且 § 文件规划中**既未作为已有类型列出，也未作为新建文件纳入**。实现阶段将因引用不存在类型而编译失败。

**修正方向**：在 § 文件规划中新增：
- 新建 `exception/CredentialUnavailableException.java`，模式参考同包的 `StructuredOutputNotSupportedException`（extends RuntimeException，String 和 String+Throwable 双构造器）
- 新建对应测试文件 `exception/CredentialUnavailableExceptionTest.java`

### **[一般]** NORMAL→BACKOFF 转换路径未纳入状态转换表

§ 行为契约状态转换表仅列出 NORMAL 状态的 3 条转换（→CACHE_ONLY、→NORMAL×2），但 NORMAL 实现步骤 2c 定义了 Vault 返回空（endpointId 未注册）且连续失败 ≥ 5 次时，从 NORMAL **直接转换至 BACKOFF**。该转换路径未出现在状态转换表中（表中仅 CACHE_ONLY→BACKOFF），也未出现在"CredentialProvider 状态机转换"表格的行中。该缺失可能导致实现遗漏该转换路径。

**修正方向**：在状态转换表中新增一行：
`| NORMAL | Vault 返回空（endpointId 未注册），连续失败 ≥ 5 | BACKOFF | 启动 30 秒退避窗口 |`

### **[轻微]** `defaultMaxBurstSeconds` 字段声明但未消费

`EndpointRateLimiter` 中 `@Value("${ai.rate-limiting.endpoints.default.max-burst-seconds:1}") private double defaultMaxBurstSeconds` 已声明，但 `RateLimiter.create()` 仅使用 `defaultPermitsPerSecond`，该字段未被任何实际代码消费，属于死代码且无对应配置来源消费方。

**修正方向**：在描述中明确标记为"为 Task 18 预留，当前版本未使用"并在 `@Value` 声明处附带注释；或暂不声明，待 Task 18 引入定制 `RateLimiter` 子类时一并添加。
