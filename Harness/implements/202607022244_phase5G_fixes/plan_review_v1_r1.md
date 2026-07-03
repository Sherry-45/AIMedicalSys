# 计划审查报告（v1 r1）

## 审查结果
REJECTED

## 发现

### **[严重]** T2 修复方案不可行：Phase 4 Service 接口不存在
计划要求"每个薄适配器应注入具体 Phase 4 Service 接口（例如 DiagnosisService），并直接调用其 `execute()` 方法"以消除反射调用。但经核查，代码库中**不存在任何 Phase 4 Service 接口**：
- `DiagnosisService`、`ImageAnalysisService` 等接口均未定义
- `ai-impl/pom.xml` 的 Maven 依赖不包含任何 Phase 4 模块
- 当前 `diagnosisService` 以 `@Autowired(required = false) Object diagnosisService` 注入，无编译期类型信息
实施 Agent 将无法按此方案推进——没有接口可注入。计划需要在 T2 方向上进行实质性修正（如在 `ai-api` 中创建公共 Phase 4 Service 接口、或采用单点封装方案、或暂缓 T2）。

### **[一般]** T22/T18/T2 对薄适配器构造器的叠加修改存在合并冲突风险
- T22 移除 `AbstractCapabilityExecutor` 的 `Class<T> inputType` 构造参数 → 薄适配器 `super()` 调用链变化
- T18 包移动 + 构造器上 `@Service("CAPABILITY_ID")` 的 capabilityId 可能需要调整
- T2 要求更换服务注入类型（`Object` → 具体接口类型），进一步改变构造器签名
三项改动独立实施可能在薄适配器构造器上产生合并冲突。建议在 R1 详细设计中统一定义最终构造器形态。

## 修改要求
1. **[严重]** 重新设计 T2 方案。建议方向：（A）在 `ai-api` 中定义 `Phase4Service<RQ, RS>` 通用接口，薄适配器直接引用；（B）在 `ai-impl` 内创建单点封装类，将反射限制在一处；（C）暂缓 T2，转为 TODO。更新 `plan.md` 和 `task_v1.md`。

2. **[一般]** 在 R1 详细设计中，统一给出薄适配器在 T22+T18+T2 三项改动后的完整构造器签名，避免逐项独立修改导致不一致。
