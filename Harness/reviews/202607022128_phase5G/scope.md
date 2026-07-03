# 审查范围界定

## 审查目标

对 Phase 5 包 G（AI 进阶底座）的完整实现代码进行审议式代码审查，验证代码是否忠实实现 `Docs/06_ood_phase5_G.md` 中的架构级 OOD 设计方案。

## 审查依据

- **设计文档**：`Docs/06_ood_phase5_G.md`（v30，Phase 5 包 G 架构级 OOD 设计方案）
- **源分支**：`harness/implements/202607020007_ood_phase5_G`
- **目标分支**：`develop`
- **变更规模**：485 文件，+65,542 行 / -316 行

## 审查重点

### 1. 设计一致性（P0）
- 代码实现是否与 OOD 设计方案中的核心抽象（§1.3）、类图（§2.3）、目录结构（§2.1）一致
- 13 项 CapabilityExecutor 实现是否符合设计文档定义的管线行为
- AiOrchestrator 路由委托模式是否正确实现
- AbstractCapabilityExecutor 模板方法模式是否正确封装降级预检、端到端超时、指标采集等公共逻辑
- 薄适配器 vs 底座完整管线的实现差异是否与设计一致

### 2. 正确性（P0）
- 降级策略链（DegradationStrategy + SlidingWindowMetricsStore）的并发安全性
- CompletableFuture 超时（orTimeout）与线程池交互是否正确
- 指标采集（AiCallRecord/AiCallLogEntity）字段完整性
- LlmChatService 双实现（HTTP_API/SPRING_AI）的 DelegatingLlmChatService 分发逻辑
- Bean 装配（AiPlatformConfig）的条件注解与互斥逻辑

### 3. 设计合理性（P1）
- 依赖方向是否遵守 ai-api ← ai-impl 单向约束
- 薄适配器对 Phase 4 的 Maven provided 依赖是否正确声明
- 运行时配置热加载（AtomicReference 全量替换）的线程安全模型
- JPA Entity / Repository 设计是否合理

### 4. 异常处理（P1）
- StructuredOutputNotSupportedException vs LlmInfrastructureException 的双 catch 分支
- Phase 4 BusinessException 的两阶段判定（instanceof + 包路径回退）
- 降级路径是否完整覆盖所有异常场景

### 5. 可读性/可维护性（P2）
- 命名规范一致性
- 注释与文档对应关系
- 代码结构清晰度

## 审查排除范围

- `Harness/` 目录下的审议过程记录文件（plan/task/detail/verify/review 等）
- `test-output.txt` 等临时文件
- `%TEMP%` 目录下的临时文件
- 测试代码本身的正确性（仅关注生产代码；测试覆盖度作为参考指标）

## 审查背景

Phase 5 包 G 是 AI 进阶底座的完整实现，替代原 MockAiService 的扁平实现为多层管线架构。核心变更包括：
- 新增 AiOrchestrator 统一编排路由层
- 13 项 CapabilityExecutor（7 底座 + 6 薄适配器）
- LlmChatService 双实现 + DelegatingLlmChatService 分发
- PromptTemplateManager / ExperimentManager / ModelRouter 基础服务
- SlidingWindowMetricsStore / CircuitBreakerDegradationStrategy 降级体系
- AiPlatformConfig Bean 装配与配置热加载
