# 代码审查报告（v18 r1）

## 审查结果
REJECTED

## 发现

- **[一般]** `HashBucketExperimentManagerTest.java:115` — `cacheShouldExpireAfterWrite` 测试名称和 throws InterruptedException 言称验证过期行为，但实际仅验证了缓存命中（第二次调未走DB），从未验证 expireAfterWrite 5 分钟后重新查 DB 的设计契约。设计明确要求测试 Caffeine 5 分钟过期后的重新查询行为，当前实现未覆盖此契约点。

- **[轻微]** `HashBucketExperimentManagerTest.java:104` — `shouldHandleNegativeHashValue` 仅验证了一个 floorMod 结果为 0 的字符串不抛异常，未按设计覆盖 hashCode() = -1、-1000、Integer.MIN_VALUE 等具体负值边界。

- **[轻微]** `HashBucketExperimentManagerTest.java:81` — `findSessionIdForHash` 辅助方法在 10000 次无匹配时抛出 RuntimeException，存在脆性（尽管实践中不易触发）。

## 修改要求（仅 REJECTED 时）

### 一般问题

1. **`HashBucketExperimentManagerTest.java` `cacheShouldExpireAfterWrite`**
   - **问题**：测试仅验证了缓存命中，未验证 expireAfterWrite(5, MINUTES) 的时间过期行为。设计要求「Caffeine expireAfterWrite 5min 后重新查 DB」。
   - **为什么是问题**：设计明确将此列为状态交互契约，当前测试未覆盖核心约定，降低了对缓存过期机制的信心。
   - **期望修正方向**：至少两种方案：
     a) 为 `HashBucketExperimentManager` 增加可注入的 `Ticker`（Caffeine 支持 `Caffeine.newBuilder().ticker(...)`），在测试中使用虚拟 Ticker 推进时间，验证过期后重新查 DB；或
     b) 若认为纯单元测试无法验证时间行为，应将测试重命名为 `shouldUseCacheOnSubsequentCalls` 并在报告中说明时间验证由集成测试覆盖，但需修改设计文档中的测试规划以匹配。

### 轻微问题（建议改进，不影响通过性）

2. **`HashBucketExperimentManagerTest.java` `shouldHandleNegativeHashValue`**
   - 建议使用 `String.valueOf((char) 0)` 或其他已知 hashCode 为负的字符串直接测试 -1、-1000、Integer.MIN_VALUE 等边界。

3. **`HashBucketExperimentManagerTest.java` `findSessionIdForHash`**
   - 建议增加兜底或使用确定性字符串生成方式，避免在 JVM 版本差异下可能出现的不稳定性。
