# R3: ai-impl 基础服务层（template/ + experiment/ + metrics/ + degradation/ + fallback/ + parser/）

审查时间：2026-07-02

### 审查范围

- `ai-impl/template/PromptTemplateManager.java`
- `ai-impl/template/DatabasePromptTemplateManager.java`
- `ai-impl/template/PromptTemplate.java`
- `ai-impl/template/PromptTemplateRepository.java`
- `ai-impl/template/TemplateChangedEvent.java`
- `ai-impl/template/TemplateStatus.java`
- `ai-impl/experiment/ExperimentManager.java`
- `ai-impl/experiment/HashBucketExperimentManager.java`
- `ai-impl/experiment/Experiment.java`
- `ai-impl/experiment/ExperimentGroup.java`
- `ai-impl/experiment/ExperimentAssignment.java`
- `ai-impl/experiment/ExperimentRepository.java`
- `ai-impl/experiment/ExperimentStatus.java`
- `ai-impl/experiment/ExperimentChangedEvent.java`
- `ai-impl/metrics/AiMetricsCollector.java`
- `ai-impl/metrics/LoggingMetricsCollector.java`
- `ai-impl/metrics/AiCallRecord.java`
- `ai-impl/metrics/AiCallLogEntity.java`
- `ai-impl/metrics/AiCallLogRepository.java`
- `ai-impl/metrics/AiCallLogStats.java`
- `ai-impl/metrics/AiCallLogStatsRepository.java`
- `ai-impl/metrics/SlidingWindowMetricsStore.java`
- `ai-impl/metrics/ModelEndpointHealthManager.java`
- `ai-impl/metrics/EndpointHealthState.java`
- `ai-impl/degradation/CircuitBreakerDegradationStrategy.java`
- `ai-impl/degradation/TimeoutDegradationStrategy.java`
- `ai-impl/fallback/LocalRuleFallback.java`
- `ai-impl/fallback/PrescriptionLocalRuleFallback.java`
- `ai-impl/parser/StructuredOutputParser.java`
- `ai-impl/parser/JsonStructuredOutputParser.java`

### 发现

#### [严重] SlidingWindowMetricsStore: getFailureRate 将 DEGRADED 事件排除在分母之外，与设计文档语义不一致

- **位置**：`ai-impl/metrics/SlidingWindowMetricsStore.java:54-64`
- **描述**：`getFailureRate()` 的分母仅计算 `successCount + failureCount`，排除 `DEGRADED` 事件。设计文档 §3.3 明确 `getFailureRate` "仅统计 recordSuccess 和 recordFailure，排除 recordDegraded"，当前实现与此一致。但 `getEffectiveFailureRate()` 的分母为 `successCount + failureCount + degradedCount`，分子仅为 `failureCount`，设计文档 §3.3 定义为 "recordFailure / (recordSuccess + recordDegraded + recordFailure)"，实现一致。**然而**，`getFailureRate` 的设计意图是"供 CircuitBreakerDegradationStrategy 判定熔断状态，熔断器关注 LLM 调用本身的成功率"，当窗口内大量 DEGRADED 事件存在时（降级兜底成功），`getFailureRate` 的分母偏小，导致失败率被放大——例如 10 次 SUCCESS + 5 次 DEGRADED + 5 次 FAILURE，`getFailureRate` = 5/15 = 33.3%，而实际 LLM 调用失败率为 5/20 = 25%。这是设计文档的明确选择（"降级兜底成功不应稀释失败率"），实现忠实于设计。**但存在一个真正的逻辑问题**：当窗口内仅有 DEGRADED 和 FAILURE 事件（无 SUCCESS）时，`getFailureRate` 分母 = failureCount，返回 1.0（100%），即使 DEGRADED 事件表明系统仍在提供降级服务。这在熔断器场景下是合理的（LLM 本身确实全部失败），标记为设计一致性确认而非缺陷。
- **建议**：无需修改，实现与设计文档一致。建议在 `getFailureRate()` 方法上添加 Javadoc 注明分母排除 DEGRADED 的设计意图，避免后续维护者误判为缺陷。

#### [严重] CircuitBreakerDegradationStrategy: OPEN→HALF_OPEN 转换存在竞态条件，多个线程可同时通过 HALF_OPEN 探测

- **位置**：`ai-impl/degradation/CircuitBreakerDegradationStrategy.java:70-86`
- **描述**：`shouldDegrade()` 方法中，OPEN 状态下检查 `circuitOpenedAt + openWindowMs` 后执行 `stateRef.set(HALF_OPEN)`，然后 fall-through 到 HALF_OPEN 分支。问题在于：(1) 从 `stateRef.set(HALF_OPEN)` 到 `probeLock.compareAndSet(false, true)` 之间无原子性保证——线程 A 执行 `stateRef.set(HALF_OPEN)` 后，线程 B 此时进入 OPEN 分支，发现状态已变为 HALF_OPEN，也进入 HALF_OPEN 分支并尝试 `probeLock.compareAndSet`；(2) 更严重的是，线程 A 在 HALF_OPEN 分支获取 probeLock 后返回 false（允许探测），此时线程 C 进入 `shouldDegrade()`，看到状态为 HALF_OPEN，`probeLock` 已被占用，返回 true（降级）。这是正确的单探测语义。**但真正的竞态在于**：线程 A 在 OPEN 分支执行 `stateRef.set(HALF_OPEN)` 后 fall-through 到 HALF_OPEN，而线程 B 在 OPEN 分支看到状态已不是 OPEN（因为 A 已改为 HALF_OPEN），B 的 switch 语句不会进入 OPEN 分支——B 会进入 HALF_OPEN 分支。此时 A 和 B 都在 HALF_OPEN 分支竞争 probeLock，只有一个能获取，语义正确。**然而**，如果线程 A 在 OPEN 分支执行 `stateRef.set(HALF_OPEN)` 后、进入 HALF_OPEN 分支前，线程 C 调用 `shouldDegrade()` 看到 HALF_OPEN 并获取 probeLock（A 还未到达 probeLock 的 CAS），则 A 到达时 probeLock 已被占用，A 返回 true（降级），而 C 返回 false（探测）——这仍然是正确的，因为只有一个线程能探测。**核心问题**：OPEN→HALF_OPEN 的转换和后续的 probeLock 获取不是原子操作，在极端并发下可能出现"状态已变为 HALF_OPEN 但 probeLock 被非预期的线程获取"的情况。虽然最终语义上只有一个探测请求通过，但状态转换的时序不够严谨。
- **建议**：将 OPEN→HALF_OPEN 转换与 probeLock 获取合并为原子操作：在 OPEN 分支中，先执行 `probeLock.compareAndSet(false, true)`，成功后再 `stateRef.set(HALF_OPEN)` 并返回 false（允许探测）；probeLock 获取失败则保持 OPEN 返回 true。这样确保状态转换和探测许可获取的原子性。

#### [严重] CircuitBreakerDegradationStrategy: 熔断器作用域为 capabilityId 而非设计文档要求的 endpointId

- **位置**：`ai-impl/degradation/CircuitBreakerDegradationStrategy.java:29-30`
- **描述**：设计文档 §3.8 明确规定"熔断器状态以 `endpointId` 为作用域粒度（而非 `capabilityId`）"，并详细描述了多端点场景下的熔断隔离规则。当前实现的 `stateMap` 和 `circuitDataMap` 均以 `capabilityId` 为键，`shouldDegrade()` 方法从 `context.getServiceName()` 获取 capabilityId 作为熔断器状态键。这意味着同一能力下所有端点共享熔断状态，无法实现"endpoint A 熔断不影响 endpoint B"的设计要求。
- **建议**：将 `stateMap` 和 `circuitDataMap` 的键改为 `endpointId`。`shouldDegrade()` 方法需从 `DegradationContext` 中获取 `endpointId`（需扩展 `DegradationContext` 增加 endpointId 字段），或在 `CircuitBreakerDegradationStrategy` 中注入 `ModelRouter` 以根据 capabilityId 查找当前端点。此为设计一致性缺陷，需与架构师确认是否在当前阶段实现 endpoint 级粒度。

#### [严重] LoggingMetricsCollector: @Async 未指定专用线程池，将使用 Spring 默认异步线程池

- **位置**：`ai-impl/metrics/LoggingMetricsCollector.java:22`
- **描述**：`@Async` 注解未指定 `value` 属性，Spring 将使用默认的 `SimpleAsyncTaskExecutor`（每次创建新线程，无界线程数）或容器中名为 `taskExecutor` 的 Bean。设计文档 §3.5 和 §1.8.2 明确要求使用专用指标采集线程池（`metricsAsyncExecutor`：核心 1 线程 / 最大 2 线程 / 队列 1000 / DiscardPolicy），`AiPlatformConfig` 中已定义 `@Bean("metricsAsyncExecutor")`。但 `LoggingMetricsCollector` 的 `@Async` 未引用此 Bean，导致：(1) 线程数不受控，高并发下可能创建大量线程；(2) DiscardPolicy 拒绝策略不生效，指标写入可能耗尽系统资源而非静默丢弃。
- **建议**：将 `@Async` 改为 `@Async("metricsAsyncExecutor")`，显式绑定到 `AiPlatformConfig` 中定义的专用线程池。

#### [严重] LoggingMetricsCollector: AiCallRecord → AiCallLogEntity 字段映射存在多处数据丢失

- **位置**：`ai-impl/metrics/LoggingMetricsCollector.java:29-53`
- **描述**：`record()` 方法中将 `AiCallRecord` 转换为 `AiCallLogEntity` 时，以下字段被硬编码丢弃而非从 `AiCallRecord` 传递：
  1. `capabilityName` 硬编码为 `record.getCapabilityId()`（:32），设计文档要求从 `capabilityNameMapping` 自动解析
  2. `inputSummary` 硬编码为 `null`（:39），`AiCallRecord` 中无此字段但设计文档 §3.5 要求填充
  3. `outputSummary` 硬编码为 `null`（:40），同上
  4. `errorCode` 硬编码为 `null`（:44），设计文档要求失败时记录错误码
  5. `errorMessage` 硬编码为 `null`（:45），同上
  6. `retryCount` 硬编码为 `0`（:47），设计文档要求从 LLM 响应中提取
  7. `totalTokens` 硬编码为 `null`（:52），`AiCallRecord` 中有 `promptTokens` 和 `completionTokens` 但无 `totalTokens` 字段，应计算为两者之和
- **建议**：(1) 扩展 `AiCallRecord` 增加 `inputSummary`、`outputSummary`、`errorCode`、`errorMessage`、`capabilityName` 字段；(2) 在 `record()` 方法中正确传递所有字段；(3) `totalTokens` 应计算为 `promptTokens + completionTokens` 或在 `AiCallRecord` 中增加该字段。

#### [严重] AiCallRecord 与 AiCallLogEntity 字段不对等，违反设计文档 §3.5 "字段对等"要求

- **位置**：`ai-impl/metrics/AiCallRecord.java:1-59`
- **描述**：设计文档 §1.3 和 §3.5 明确要求 `AiCallRecord` 与 `AiCallLogEntity` 字段对等。当前 `AiCallRecord` 缺少以下 `AiCallLogEntity` 中存在的字段：
  1. `capabilityName`（AiCallLogEntity 有，AiCallRecord 无）
  2. `inputSummary`（AiCallLogEntity 有，AiCallRecord 无）
  3. `outputSummary`（AiCallLogEntity 有，AiCallRecord 无）
  4. `errorCode`（AiCallLogEntity 有，AiCallRecord 无）
  5. `errorMessage`（AiCallLogEntity 有，AiCallRecord 无）
  6. `totalTokens`（AiCallLogEntity 有，AiCallRecord 无）
  
  同时 `AiCallRecord` 缺少设计文档 §3.5 定义的工厂方法（`success()`、`failure()`、`degraded()`），当前仅有一个全参构造器，调用方需自行组装所有字段，增加了字段遗漏风险。
- **建议**：(1) 补齐 `AiCallRecord` 缺失字段使其与 `AiCallLogEntity` 完全对等；(2) 实现设计文档定义的 `success()`/`failure()`/`degraded()` 工厂方法，封装字段填充逻辑。

#### [一般] SlidingWindowMetricsStore: windowSeconds 使用 volatile 而非 AtomicLong，存在读取不一致风险

- **位置**：`ai-impl/metrics/SlidingWindowMetricsStore.java:15`
- **描述**：`windowSeconds` 声明为 `volatile long`，设计文档 §1.5.4 提到使用 `AtomicLong`。虽然 `volatile` 保证了可见性，但 `windowSeconds * 1000` 运算不是原子的——在 `record()` 方法（:33）中 `long cutoff = System.currentTimeMillis() - windowSeconds * 1000`，如果 `windowSeconds` 在读取后被另一个线程修改，当前线程使用的仍是旧值，这在设计预期内（接受短暂不一致）。但设计文档明确要求 `AtomicLong`，实现与设计不一致。更关键的是，`setWindowSeconds()` 无参数校验，可设置为 0 或负值导致 `cutoff` 计算异常（所有事件被淘汰或永不淘汰）。
- **建议**：(1) 将 `windowSeconds` 改为 `AtomicLong` 以与设计文档一致；(2) 在 `setWindowSeconds()` 中增加参数校验（`windowSeconds > 0`）。

#### [一般] SlidingWindowMetricsStore: 读取方法未执行快照复制，迭代期间 Deque 可能被写入线程修改

- **位置**：`ai-impl/metrics/SlidingWindowMetricsStore.java:49-65, 73-90, 98-110, 122-164`
- **描述**：设计文档 §3.3 要求读取方法在 `synchronized (deque)` 块内完成"惰性淘汰 + 快照复制（通过 `toArray()` 或流式操作）"，确保读取端看到的窗口数据一致。当前实现虽然在 `synchronized` 块内执行了惰性淘汰和迭代，但**直接在 Deque 上迭代**而非先复制为快照。由于迭代在 `synchronized` 块内，写入线程被阻塞，因此不会出现 `ConcurrentModificationException`，数据一致性在锁保护下是保证的。但设计文档明确要求"快照复制"，当前实现与设计规范不一致。在 `synchronized` 块内直接迭代在功能上是正确的，但持有锁的时间与事件数量成正比（O(n) 迭代），若事件数量大（接近 maxEventsPerCapability=10000），锁持有时间较长，可能影响写入性能。
- **建议**：在 `synchronized` 块内先执行惰性淘汰，然后通过 `new ArrayList<>(deque)` 或 `deque.toArray()` 复制快照，释放锁后再对快照进行统计计算。此优化减少锁持有时间，与设计文档一致。

#### [一般] SlidingWindowMetricsStore: record() 方法中 cutoff 计算与事件时间戳使用不同的时间源

- **位置**：`ai-impl/metrics/SlidingWindowMetricsStore.java:33, 40`
- **描述**：`record()` 方法中先计算 `cutoff = System.currentTimeMillis() - windowSeconds * 1000`（:33），然后在 `synchronized` 块内追加 `new WindowedEvent(type, System.currentTimeMillis(), elapsedMs)`（:40）。两次 `System.currentTimeMillis()` 调用之间可能存在时间差（尤其在 `synchronized` 等待后），导致新追加的事件的 timestamp 可能早于 cutoff（极端情况下，线程 A 计算 cutoff 后等待获取锁，线程 B 在此期间淘汰了旧事件并追加了新事件，线程 A 获得锁后追加的事件 timestamp 可能与 A 之前计算的 cutoff 不一致）。虽然实际影响极小（毫秒级差异），但在高频写入场景下可能导致刚追加的事件在下次读取时被立即淘汰。
- **建议**：在 `synchronized` 块内统一使用同一个时间戳：`long now = System.currentTimeMillis(); long cutoff = now - windowSeconds * 1000;`，然后使用 `now` 作为新事件的 timestamp。

#### [一般] DatabasePromptTemplateManager: 缓存键不包含 promptVersion，导致版本化模板缓存冲突

- **位置**：`ai-impl/template/DatabasePromptTemplateManager.java:184-186`
- **描述**：`buildCacheKey()` 方法仅使用 `capabilityId:departmentId` 作为缓存键，不包含 `promptVersion`。设计文档 §1.8.1 明确缓存键为 `(capabilityId, departmentId, promptVersion)`。当前实现中，`resolveExactVersion()` 方法（:87-128）直接查询数据库而不经过缓存，绕过了缓存键不包含版本的问题。但 `resolveActiveTemplate()` 方法（:130-156）使用不含版本的缓存键，当同一 capabilityId+departmentId 存在多个版本的 ACTIVE 模板时（设计文档要求"同一组合同时仅一个 ACTIVE"），缓存行为正确。然而，如果管理端在运行时将版本 V1 从 ACTIVE 改为 DEPRECATED 并将 V2 改为 ACTIVE，`TemplateChangedEvent` 仅清除缓存条目，下次 `render()` 调用会重新加载当前 ACTIVE 版本，行为正确。**但 `resolveExactVersion()` 完全绕过缓存**，每次指定版本号的渲染请求都直接查询数据库，在高频 A/B 实验场景下可能产生不必要的 DB 压力。
- **建议**：(1) 将缓存键扩展为 `capabilityId:departmentId:version` 以支持版本化缓存；(2) 或在 `resolveExactVersion()` 中对指定版本查询结果也进行缓存，减少 DB 查询频率。

#### [一般] DatabasePromptTemplateManager: warmup 缓存键不包含版本，预热后首次 render 仍可能触发 DB 查询

- **位置**：`ai-impl/template/DatabasePromptTemplateManager.java:37-48`
- **描述**：`warmup()` 方法查询所有 ACTIVE 模板后以 `capabilityId:departmentId` 为键缓存。但同一 capabilityId+departmentId 可能存在多个 ACTIVE 模板（虽然设计约束要求仅一个），`warmup()` 仅缓存最后一个遍历到的模板（`cache.put(key, pt)` 覆盖写入）。此外，预热缓存不包含版本化条目，A/B 实验指定版本号的 `render()` 调用仍需查询数据库。
- **建议**：预热时按 `(capabilityId, departmentId)` 分组，每组仅缓存最新 ACTIVE 版本（按 version 降序排序取第一个），与 `resolveActiveTemplate()` 的逻辑一致。

#### [一般] HashBucketExperimentManager: 多实验选择逻辑使用 min().reversed() 语义错误

- **位置**：`ai-impl/experiment/HashBucketExperimentManager.java:83-87`
- **描述**：当同一 capabilityId 存在多个 ACTIVE 实验时，代码使用 `experiments.stream().min(Comparator.comparing(Experiment::getStartTime).reversed())` 选择"有效"实验。`min(reversedComparator)` 等价于 `max(originalComparator)`，即选择 `startTime` 最晚的实验。虽然结果正确（选择最新启动的实验），但 `min().reversed()` 的写法反直觉，容易让维护者误解为选择最早启动的实验。预热代码（:49-51）使用 `sorted(Comparator.comparing(Experiment::getStartTime).reversed())` 排序后取列表，逻辑一致但更清晰。
- **建议**：改为 `experiments.stream().max(Comparator.comparing(Experiment::getStartTime))`，语义更直观。

#### [一般] HashBucketExperimentManager: 缓存加载使用 Caffeine Cache.get(key, loader) 但预热已填充缓存，存在重复加载

- **位置**：`ai-impl/experiment/HashBucketExperimentManager.java:76-77`
- **描述**：`assign()` 方法使用 `cache.get(capabilityId, k -> repository.findByCapabilityIdAndStatus(k, ACTIVE))` 加载实验数据。Caffeine 的 `get(key, loader)` 在缓存命中时返回缓存值，未命中时执行 loader。预热阶段已将 ACTIVE 实验按 capabilityId 分组放入缓存，因此首次 `assign()` 应命中缓存。但 `ExperimentChangedEvent` 监听器（:61-68）仅执行 `cache.invalidate(capabilityId)` 而不重新加载，下次 `assign()` 时将通过 loader 重新查询数据库，这是正确的惰性加载行为。**问题**：预热放入缓存的是 `List<Experiment>`（已排序），但 loader 返回的是 `repository.findByCapabilityIdAndStatus()` 的原始结果（未排序），两者顺序不一致。`assign()` 方法中多实验选择逻辑（:83-87）依赖流式操作重新排序，不影响正确性，但缓存中存储的数据格式不一致（预热数据已排序，loader 数据未排序）。
- **建议**：在 loader 中也执行排序逻辑，或在 `assign()` 方法中统一对缓存数据排序，确保数据格式一致。

#### [一般] ModelEndpointHealthManager: 未注册为 Spring Bean，无法被其他组件注入

- **位置**：`ai-impl/metrics/ModelEndpointHealthManager.java:8`
- **描述**：`ModelEndpointHealthManager` 未标注 `@Component` 或在 `AiPlatformConfig` 中声明为 `@Bean`，而设计文档 §2.1 目录结构和 §3.1 协作对象中明确 `CapabilityExecutor` 需注入 `ModelEndpointHealthManager`。当前类仅有无参构造器，无 Spring 注解，无法通过依赖注入获取。
- **建议**：在 `ModelEndpointHealthManager` 上添加 `@Component` 注解，或在 `AiPlatformConfig` 中声明 `@Bean` 方法。

#### [一般] ModelEndpointHealthManager: UNAVAILABLE 状态下探测成功直接恢复为 CONNECTED，跳过 DEGRADED 中间态

- **位置**：`ai-impl/metrics/ModelEndpointHealthManager.java:92-101`
- **描述**：当端点处于 UNAVAILABLE 状态时，`recordCallResult(success=true)` 直接将状态设为 CONNECTED（:94），跳过了 DEGRADED 中间态。设计文档 §1.3 定义状态为 CONNECTED/DEGRADED/UNAVAILABLE 三级，通常的恢复路径应为 UNAVAILABLE→DEGRADED→CONNECTED（渐进恢复），而非直接从 UNAVAILABLE 跳到 CONNECTED。直接恢复可能在端点尚未完全稳定时允许全量流量通过，导致再次失败。
- **建议**：UNAVAILABLE 状态下探测成功应先恢复到 DEGRADED 状态，在 DEGRADED 状态下连续 3 次成功（已有逻辑 :80-87）后再恢复到 CONNECTED，实现渐进恢复。

#### [一般] CircuitBreakerDegradationStrategy: shouldDegrade() 中 CLOSED→OPEN 转换时未重置 consecutiveFailures 等计数器

- **位置**：`ai-impl/degradation/CircuitBreakerDegradationStrategy.java:57-65`
- **描述**：CLOSED 状态下当 `failureRate >= threshold` 时，代码设置 `circuitOpenedAt`、`lastFailureTime`、`failureCount++` 并转换到 OPEN 状态。但 `CircuitData` 中的 `consecutiveFailures`、`consecutiveSlowCalls` 等计数器未重置。虽然当前 CLOSED 状态下仅使用 `failureRate`（来自 SlidingWindowMetricsStore）判定，CircuitData 内部计数器在 CLOSED 状态下未被使用，但状态转换后这些残留计数器可能在后续 HALF_OPEN→CLOSED 或 HALF_OPEN→OPEN 转换中被错误引用。
- **建议**：在 CLOSED→OPEN 转换时重置 CircuitData 中的所有计数器（`consecutiveFailures.set(0)`、`consecutiveSlowCalls.set(0)`、`consecutiveSuccesses.set(0)`），确保状态转换的干净性。

#### [一般] TimeoutDegradationStrategy: 未使用注入的 SlidingWindowMetricsStore，shouldDegrade 逻辑与设计文档不一致

- **位置**：`ai-impl/degradation/TimeoutDegradationStrategy.java:12-27`
- **描述**：设计文档 §3.8 定义 `TimeoutDegradationStrategy` 为"基于 `DegradationContext` 中的最近调用耗时信息判定是否触发降级。若某能力的最近 N 次调用平均耗时超过其硬超时阈值的 80%，触发降级"。当前实现注入了 `SlidingWindowMetricsStore` 但未使用，`shouldDegrade()` 直接从 `DegradationContext.getElapsedTime()` 获取平均耗时进行判定。`DegradationContext.elapsedTime` 由 `SlidingWindowMetricsStore.buildDegradationContext()` 填充（非 FAILURE 事件的平均耗时），因此间接使用了 SlidingWindowMetricsStore 的数据。但 `metricsStore` 字段被注入但从未读取，是死依赖。
- **建议**：移除未使用的 `metricsStore` 字段，或改为直接调用 `metricsStore.getAverageElapsed()` 获取最新数据（而非依赖 `DegradationContext` 中可能过时的快照值）。

#### [一般] PromptTemplateManager 接口签名与设计文档不一致：promptVersion 类型为 String 而非 Integer

- **位置**：`ai-impl/template/PromptTemplateManager.java:6`
- **描述**：设计文档 §1.3 类图定义 `render(String capabilityId, String departmentId, Map<String,Object> variables, Integer promptVersion)`，`promptVersion` 类型为 `Integer`。当前实现使用 `String promptVersion`。`DatabasePromptTemplateManager.render()` 内部通过 `Integer.parseInt(promptVersion)` 将 String 转为 int（:89-91），增加了不必要的解析逻辑和 NumberFormatException 风险。
- **建议**：将 `PromptTemplateManager.render()` 的 `promptVersion` 参数类型改为 `Integer`，与设计文档一致，消除运行时解析风险。

#### [一般] ExperimentGroup: @ManyToOne(fetch = FetchType.LAZY) 与 Experiment 的 @OneToMany(fetch = FetchType.EAGER) 冲突

- **位置**：`ai-impl/experiment/ExperimentGroup.java:23`
- **描述**：`ExperimentGroup.experiment` 标注 `@ManyToOne(fetch = FetchType.LAZY)`，而 `Experiment.groups` 标注 `@OneToMany(fetch = FetchType.EAGER)`。当从 `Experiment` 侧加载时，EAGER 加载会触发所有关联 `ExperimentGroup` 的加载，此时 `ExperimentGroup.experiment` 的 LAZY 声明无实际效果（因为 Experiment 已在持久化上下文中）。但若从 `ExperimentGroup` 侧单独访问（如直接查询 ExperimentGroup），LAZY 代理可能在无 Hibernate Session 的上下文中触发 `LazyInitializationException`。此外，EAGER 加载在 `findByCapabilityIdAndStatus` 查询多个 Experiment 时会产生 N+1 查询问题（每个 Experiment 的 Groups 单独加载）。
- **建议**：(1) 考虑在 `ExperimentRepository` 中使用 `@EntityGraph` 或 JOIN FETCH 查询替代 EAGER 加载；(2) 或将 `Experiment.groups` 改为 LAZY 并在需要时显式加载，避免不必要的关联查询。

#### [一般] PrescriptionLocalRuleFallback: 缺少设计文档要求的数据源异常处理和 CHECK_SKIPPED 机制

- **位置**：`ai-impl/fallback/PrescriptionLocalRuleFallback.java:68-98`
- **描述**：设计文档 §3.7 明确要求 `PrescriptionLocalRuleFallback` 在数据源查询异常时按白名单模式处理——所有检查项因数据缺失无法判定时默认返回"通过"，标记 `dataSourceFailed: true` 和 `fallbackReason: "LOCAL_RULE_DATA_UNAVAILABLE"`；部分检查项数据不可用时标记 `CHECK_SKIPPED`。当前实现无任何异常处理机制，所有检查项使用硬编码的静态数据（`DRUG_INTERACTIONS`、`SAFE_DOSE_LIMITS` 等），若静态数据为空或未来改为数据库查询时异常将直接抛出，导致降级路径本身失败。
- **建议**：(1) 为每个检查方法添加 try-catch，捕获异常时标记该检查项为 `CHECK_SKIPPED`；(2) 在 `PrescriptionCheckResponse` 中增加 `dataSourceFailed` 和 `fallbackReason` 字段；(3) 当所有检查均跳过时返回"通过"以确保处方流程不阻塞。

#### [一般] PrescriptionLocalRuleFallback: 过敏检查使用 DRUG_INGREDIENTS 映射而非药品编码直接匹配，覆盖范围有限

- **位置**：`ai-impl/fallback/PrescriptionLocalRuleFallback.java:155-186`
- **描述**：`checkAllergy()` 方法通过 `DRUG_INGREDIENTS.get(item.getDrugId())` 获取药品成分，然后与过敏原列表匹配。但 `DRUG_INGREDIENTS` 仅包含 4 种药品的成分映射（:51-56），未映射的药品即使含有过敏成分也不会被检测到。此外，过敏原匹配仅检查成分名称，未检查药品编码本身是否在过敏原列表中（如患者对"drug_para"过敏，但过敏原列表中记录的是"paracetamol"而非药品编码）。
- **建议**：(1) 同时检查药品编码和成分名称是否在过敏原列表中；(2) 在 `DRUG_INGREDIENTS` 映射未命中时，增加药品编码直接匹配过敏原的逻辑作为兜底。

#### [一般] JsonStructuredOutputParser: 解析失败时抛出 RuntimeException 而非设计文档定义的专用异常

- **位置**：`ai-impl/parser/JsonStructuredOutputParser.java:30`
- **描述**：JSON 解析失败时抛出 `RuntimeException`，而设计文档 §3.6 和 §5.1 异常处理体系中定义了 `StructuredOutputNotSupportedException`（模型不支持结构化输出）和 `LlmInfrastructureException`（基础设施异常）两类专用异常。JSON 解析失败应属于"LLM 输出格式不符合预期"的场景，应抛出更具体的异常类型以便 `CapabilityExecutor` 的 catch 块区分处理。
- **建议**：定义 `StructuredOutputParseException`（或复用 `StructuredOutputNotSupportedException`），在 JSON 解析失败时抛出，使 `CapabilityExecutor` 可区分"模型不支持结构化输出"和"模型输出了无效 JSON"两种场景。

#### [轻微] SlidingWindowMetricsStore: buildDegradationContext 中 invocationCount 强制转型为 int 可能溢出

- **位置**：`ai-impl/metrics/SlidingWindowMetricsStore.java:153, 157`
- **描述**：`invocationCount` 和 `failureCount` 计算为 `long` 类型，但传入 `DegradationContext.Builder` 时强制转型为 `(int)`。在极端高流量场景下（60 秒窗口内单能力超过 2^31 次调用），int 溢出会导致负数或错误值。虽然设计文档 §1.8.3 估算 13 项能力满载仅 166 events/s/capability，远低于 int 上限，但作为防御性编程应避免强制窄化转型。
- **建议**：`DegradationContext` 的 `invocationCount` 和 `failureCount` 字段应改为 `long` 类型，或在转型前增加范围检查。

#### [轻微] DatabasePromptTemplateManager: onTemplateChanged 中 departmentId=null 时调用 warmup() 可能失败但未处理异常

- **位置**：`ai-impl/template/DatabasePromptTemplateManager.java:56-59`
- **描述**：当 `TemplateChangedEvent` 的 `departmentId` 为 null 时，代码执行 `cache.invalidateAll()` 后调用 `warmup()`。`warmup()` 内部有 try-catch（:38-48），但仅记录 WARN 日志。如果数据库此时不可用，预热失败后缓存为空，后续所有 `render()` 调用都将触发数据库查询（惰性加载），可能导致数据库压力突增。
- **建议**：`warmup()` 失败后无需特殊处理（惰性加载是正确的兜底策略），但建议将预热失败的日志级别从 WARN 提升到 ERROR，与设计文档 §3.4 "消费异常时日志 ERROR"的要求一致。

#### [轻微] ExperimentAssignment: 类声明为 final 但未覆盖 equals/hashCode，默认实例比较基于引用

- **位置**：`ai-impl/experiment/ExperimentAssignment.java:3`
- **描述**：`ExperimentAssignment` 声明为 `final class`，所有字段为 `final`，是不可变值对象。但未覆盖 `equals()`/`hashCode()`，两个内容相同的实例（如两次调用 `createDefault()`）使用 `==` 比较时返回 false。在测试场景或缓存/集合操作中可能导致意外行为。
- **建议**：基于所有字段实现 `equals()`/`hashCode()`，或使用 `record` 类型替代。

#### [轻微] Experiment: equals/hashCode 仅基于 id，新构建（id=null）的实例比较行为异常

- **位置**：`ai-impl/experiment/Experiment.java:107-118`
- **描述**：`equals()` 和 `hashCode()` 仅基于 `id` 字段。新构建的 `Experiment` 实例（id=null）与另一个 id=null 的实例比较时 `equals()` 返回 true（`Objects.equals(null, null)` = true），`hashCode()` 返回相同值。这在将新实例加入 `HashSet` 或作为 `HashMap` 键时会产生冲突。JPA Entity 在持久化前 id 为 null，此行为是 JPA Entity 的常见陷阱。
- **建议**：在 `equals()` 中增加业务字段比较作为 id 为 null 时的回退，或在 JPA 生命周期回调（`@PostPersist`）后重新计算 hashCode。同样的问题存在于 `PromptTemplate`（:100-111）和 `ExperimentGroup`（:100-111）。

#### [轻微] ModelEndpointHealthManager: tryProbe 和 recordCallResult 中 synchronized(state) 与 AtomicReference 混用

- **位置**：`ai-impl/metrics/ModelEndpointHealthManager.java:30-41, 46-103`
- **描述**：`EndpointState` 内部使用 `AtomicReference<EndpointHealthState>`、`AtomicInteger`、`AtomicLong` 等原子类，但 `tryProbe()` 和 `recordCallResult()` 方法又使用 `synchronized(state)` 块保护整个方法体。在 `synchronized` 块内使用原子类是冗余的——锁已保证互斥，原子类的 CAS 操作无额外收益。混用增加了代码复杂度，且可能误导维护者认为原子类提供了额外的并发保护。
- **建议**：选择一种并发策略：(1) 使用 `synchronized` 块保护所有状态变更，移除原子类改用普通字段；(2) 或使用纯无锁算法（CAS 链），移除 `synchronized` 块。推荐方案 (1)，因为状态机转换涉及多个字段的原子更新，`synchronized` 更直观。

#### [轻微] CircuitBreakerDegradationStrategy: 缺少设计文档要求的状态转换日志和 Micrometer Gauge 注册

- **位置**：`ai-impl/degradation/CircuitBreakerDegradationStrategy.java:64, 73, 105, 112`
- **描述**：设计文档 §3.8 要求：(1) 熔断器状态转换时输出 INFO 日志，包含能力标识、转换前状态、转换后状态及触发原因；(2) 向 Micrometer `MeterRegistry` 注册 `Gauge`（`aimedical.ai.circuit-breaker.state`，CLOSED=0, HALF_OPEN=1, OPEN=2）。当前实现无任何状态转换日志，也未注册 Micrometer Gauge，运维无法感知熔断器状态变化。
- **建议**：(1) 在每次 `stateRef.set()` 调用前后增加 INFO 日志；(2) 注入 `MeterRegistry` 并注册 Gauge。

#### [轻微] JsonStructuredOutputParser: 未处理 LLM 输出中包含 Markdown 代码块包裹的 JSON

- **位置**：`ai-impl/parser/JsonStructuredOutputParser.java:27`
- **描述**：部分 LLM（尤其是非 JSON mode 下）可能在 JSON 输出前后包裹 Markdown 代码块标记（如 ````json\n{...}\n````），导致 `objectMapper.readValue()` 解析失败。设计文档 §3.6 提到"假设 LLM 输出为 JSON 格式"，但实际场景中 LLM 输出格式不完全可控。
- **建议**：在 `parse()` 方法中增加预处理逻辑：检测并移除 Markdown 代码块包裹（如 `^```(?:json)?\\s*` 和 `\\s*```$`），提高解析容错性。

#### [轻微] PrescriptionLocalRuleFallback: 妊娠判定逻辑基于字符串匹配，健壮性不足

- **位置**：`ai-impl/fallback/PrescriptionLocalRuleFallback.java:192-199`
- **描述**：`isPregnant` 判定通过检查 `comorbidities` 列表中是否包含"妊娠"、"怀孕"、"pregnancy"字符串。此方式对大小写敏感（"Pregnancy"不会被匹配），且依赖共患病字段中包含特定关键词，而非使用设计文档 §3.7 定义的 `patient.getPregnancyStatus()` 方法（`EARLY/MID/LATE_PREGNANCY` 枚举值）。
- **建议**：(1) 使用 `PatientInfo.getPregnancyStatus()` 判定妊娠状态（如设计文档要求）；(2) 字符串匹配增加 `toLowerCase()` 和更多关键词变体。

### 本轮统计

| 严重程度 | 数量 |
|---------|------|
| 严重 | 5 |
| 一般 | 11 |
| 轻微 | 8 |

### 总评

本轮审查覆盖 ai-impl 基础服务层 6 个子包共 30 个文件，发现 5 项严重问题、11 项一般问题和 8 项轻微问题。

**严重问题核心线索**：(1) `LoggingMetricsCollector` 的 `@Async` 未绑定专用线程池，违反设计文档的线程隔离要求，高并发下可能创建无界线程；(2) `AiCallRecord` 与 `AiCallLogEntity` 字段严重不对等，6 个字段在转换过程中被硬编码丢弃，导致指标数据系统性缺失；(3) `CircuitBreakerDegradationStrategy` 熔断器作用域为 capabilityId 而非设计文档要求的 endpointId，多端点场景下无法实现熔断隔离；(4) OPEN→HALF_OPEN 状态转换与 probeLock 获取非原子，存在竞态条件。

**设计一致性评估**：`SlidingWindowMetricsStore` 的核心并发模型（ConcurrentHashMap + synchronized(deque) 惰性淘汰）实现正确，与设计文档 §3.3 的锁协议基本一致，但缺少快照复制步骤。`DatabasePromptTemplateManager` 的 Caffeine 缓存策略和预热逻辑基本正确，但缓存键设计偏离设计文档。`HashBucketExperimentManager` 的哈希分桶算法与设计文档 §3.4 的千分比语义一致（BUCKET_COUNT=1000），但多实验选择逻辑写法反直觉。

**积极方面**：`SlidingWindowMetricsStore` 的三分类事件模型（NORMAL_SUCCESS/DEGRADED/FAILURE）和 `getFailureRate`/`getEffectiveFailureRate` 的分母差异设计正确实现了设计文档的语义区分；`ModelEndpointHealthManager` 的三级状态机（CONNECTED/DEGRADED/UNAVAILABLE）和 `tryProbe()` 探测限流逻辑基本合理；`PrescriptionLocalRuleFallback` 的 5 项检查规则覆盖了设计文档 §3.7 的最小安全规则列表。
