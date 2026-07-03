# 设计审查报告（v10 r6）

## 审查结果
REJECTED

## 发现

### **[严重]** Guava 依赖缺少版本号，编译将直接失败

`com.google.guava:guava` 在 `ai-impl/pom.xml` 中仅声明 `artifactId` 但未指定 `<version>`。修订说明 v10 r2 称 "Spring Boot 3.2.5 BOM 管理版本，无需指定 version"，但实际情况是 Spring Boot BOM **不管理** Guava 版本，项目父 POM（`backend/pom.xml`）也**未管理** Guava 版本。当前项目中没有任何模块使用 Guava，也无任何 BOM 或 dependencyManagement 为其提供版本。添加依赖时不指定 `<version>` 将导致 Maven 编译解析失败。

**期望的修正方向**：在 `pom.xml` 的 guava 依赖声明中显式添加 `<version>`，例如 `32.1.3-jre`。

---

### **[一般]** `EndpointRateLimiter.defaultMaxBurstSeconds` 为死代码

`EndpointRateLimiter` 声明了 `@Value("${ai.rate-limiting.default.max-burst-seconds:1}") private double defaultMaxBurstSeconds;`，该字段在类的所有公开方法中均未被引用。实现描述明确使用 `RateLimiter.create(defaultPermitsPerSecond)`（单参数），未使用 `defaultMaxBurstSeconds`。该配置绑定定义了但未被消费，属于死代码，也会给运维人员造成配置项已生效的误导。

**期望的修正方向**：移除 `defaultMaxBurstSeconds` 字段及对应的 `@Value` 注解决明，不在本任务中引入未用的配置绑定；或在实际实现中消费该字段（如作为 `RateLimiter.create(permitsPerSecond, Duration)` 的 warmupPeriod），或明确标注为待办、预留字段并添加 `@SuppressWarnings("unused")`。

---

### **[轻微]** 配置属性前缀与 task_v10 存在偏差

task_v10 规定配置属性前缀为 `ai.rate-limiting.endpoints.{endpointId}`，但设计中使用 `ai.rate-limiting.default`。虽然将端点级配置推迟至 Task 18（AiPlatformConfig）在架构上更为合理，但与当前任务的规格描述不完全一致，后续需确保 AiPlatformConfig 接管时不会与 `ai.rate-limiting.default` 前缀产生冲突或重复定义。

---

### **[轻微]** BACKOFF 状态恢复未指定原子操作

`DefaultCredentialProvider` 状态机中 BACKOFF 状态恢复至 NORMAL 的描述为"自动恢复 NORMAL 并继续"，未说明 `AtomicReference` 使用的是 `compareAndSet` 还是直接 `set`。在并发场景下，若线程 T1 发现 BACKOFF 窗口到期、将状态 `set(NORMAL)` 的瞬间，恰好另一线程已将状态切为 `CACHE_ONLY`，直接 `set` 会错误地覆盖为 NORMAL。建议明确为 `compareAndSet(CredentialProviderState.BACKOFF, CredentialProviderState.NORMAL)`。
