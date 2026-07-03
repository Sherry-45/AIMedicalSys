# 设计审查报告（v10 r5）

## 审查结果
REJECTED

## 发现

- **[严重] CredentialProviderState 代码块中 `public` 修饰符将导致编译错误**  
  设计明确说明 CredentialProviderState 为 package-private 顶层枚举（置于 CredentialProvider.java 同文件中，无 public 修饰），但代码块中却写为 `public enum CredentialProviderState`。Java 限制每个 .java 文件只能有一个 public 顶层类型，CredentialProvider 已是 public interface，因此第二个 public 顶层类型将导致编译失败。必须改为 `enum CredentialProviderState`（去掉 public）。

- **[一般] EndpointRateLimiter 配置方式与 task_v10 要求不一致**  
  task_v10.md §2 明确要求"通过 `@Value` 直接注入属性值（前缀 `ai.rate-limiting.endpoints.{endpointId}`）"，但设计改用 `org.springframework.core.env.Environment` 运行时懒加载查询。设计未经授权偏离了任务规范，应统一为 @Value 方式，或同步更新 task 文件说明。

- **[轻微] 状态转换表中"NORMAL（抛出）"与 NORMAL 实现逻辑不一致**  
  状态转换表第三行："NORMAL | Vault 查询超时，缓存中无数据 | NORMAL（抛出）"，但 NORMAL 逻辑实现描述仅说"未找到则递增 consecutiveFailures；若连续失败 ≥ 5 次 → 切至 BACKOFF"，未提及抛出异常。两个描述互相矛盾，可能导致实现歧义。建议统一为"递增 consecutiveFailures"或明确异常类型及抛出时机。

## 修改要求（仅 REJECTED 时）

1. **[严重]** CredentialProviderState 代码块去除 `public` 修饰符，确保与"package-private 顶层 enum"描述一致，避免编译失败。
2. **[一般]** EndpointRateLimiter 配置方式回退为 @Value 注入以对齐 task_v10 要求，或同步更新 task 文件说明后再使用 Environment 方式。
3. **[轻微]** 状态转换表中删除"NORMAL（抛出）"表述或补充异常类型及抛出逻辑，使其与 NORMAL 实现描述一致。
