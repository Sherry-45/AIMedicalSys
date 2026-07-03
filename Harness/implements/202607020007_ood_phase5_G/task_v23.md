# 任务指令（v23）

## 动作
NEW

## 任务描述

### 1. PrescriptionLocalRuleFallback 实现（新建）
在 `ai-impl/fallback/` 包下新建 `PrescriptionLocalRuleFallback.java`，实现 `LocalRuleFallback<PrescriptionCheckRequest, PrescriptionCheckResponse>` 接口。

**类签名**：`@Service public class PrescriptionLocalRuleFallback` 实现 `LocalRuleFallback<PrescriptionCheckRequest, PrescriptionCheckResponse>`

**构造器**：无参构造器（无外部依赖注入；本地规则数据均为内存计算，不依赖数据库/外部服务）

**fallback() 方法实现**（对应设计文档 §3.7 伪代码）：

```java
@Override
public PrescriptionCheckResponse fallback(PrescriptionCheckRequest request) {
    PrescriptionCheckResponse result = new PrescriptionCheckResponse();
    result.setFromFallback(true);
    // 伪代码骨架，具体规则检查项见下文
    List<AlertItem> alerts = new ArrayList<>();
    boolean dataSourceFailed = false;

    // 1. 配伍禁忌检查
    // 2. 剂量范围检查
    // 3. 重复用药检查
    // 4. 过敏史检查
    // 5. 儿童/孕妇用药警示

    // 白名单模式：数据不可用时默认通过
    if (/* all skipped due to data unavailable */) {
        result.setRiskLevel("PASS");
    } else if (/* any BLOCK alert */) {
        result.setRiskLevel("BLOCK");
    } else if (/* any WARN alert */) {
        result.setRiskLevel("WARN");
    } else {
        result.setRiskLevel("PASS");
    }
    result.setAlerts(alerts);
    return result;
}
```

**5 项本地规则检查实现**：

| 检查项 | 输入数据 | 判定逻辑 | AlertItem 输出 |
|--------|---------|---------|---------------|
| **1. 配伍禁忌检查** | `request.prescriptionItems` 中的药品对 | 遍历所有药品两两组合，检测预定义的已知配伍禁忌表（hardcode 最小样本数据，如 "头孢类+酒精类"） | alertCode="DRUG_INTERACTION", severity="BLOCK", alertMessage="药品X与Y存在配伍禁忌" |
| **2. 剂量范围检查** | 每种药品的 drugId、dose、frequency、duration、unit | 通过预定义的安全剂量映射表（Map<String, DoseRange>，hardcode 常见药品剂量阈值）检查单次剂量/日剂量是否在安全范围内 | alertCode="DOSE_EXCEED", severity="WARN", alertMessage="药品X单次剂量Xmg超出推荐范围Y-Zmg" |
| **3. 重复用药检查** | `request.prescriptionItems` 中的 drugId/drugName | 通过预定义的成分归属映射表（Map<String, String>，drugId → ingredient）检查是否存在成分相同或药理作用相同的药品 | alertCode="DUPLICATE_DRUG", severity="WARN", alertMessage="药品X与Y成分相同" |
| **4. 过敏史检查** | `request.patientInfo.getAllergyHistory()` / `getAllergyDetails()` | 遍历患者过敏史中的过敏原，检查处方药品成分是否匹配 | alertCode="ALLERGY_CONFLICT", severity="BLOCK", alertMessage="药品X含有您已知过敏成分Y" |
| **5. 儿童/孕妇用药警示** | `request.patientInfo.getAge()` / `getComorbidities()` | 若年龄<18 或包含妊娠相关合并症，检查药品是否在儿童/孕妇慎用/禁用列表（hardcode 最小样本数据） | alertCode="SPECIAL_POP_WARN", severity="WARN/BLOCK", alertMessage="药品X在儿童/孕妇中慎用" |

**白名单模式安全策略**：
- 所有检查项基于 hardcode 内存数据，不存在数据源查询异常——因此 5 项检查始终可执行，不触发局部跳过
- 若未来升级为数据库驱动版本，新增的数据源异常路径按白名单模式处理：单检查项失败 → `alerts.add(new AlertItem("CHECK_SKIPPED", "WARN", "规则X跳过：数据源不可用"))`；全部检查项均跳过 → `riskLevel="PASS"` 且通过 `result.isFromFallback()=true` 标识

### 2. 测试文件
**新建 `PrescriptionLocalRuleFallbackTest.java`**（在 `ai-impl/src/test/.../fallback/`），覆盖：
1. `shouldPassWhenNoIssues` — 正常处方（无剂量超标、无重复用药、无过敏冲突）→ riskLevel="PASS"
2. `shouldBlockWhenDrugInteraction` — 处方含配伍禁忌药品对 → riskLevel="BLOCK"，alerts 包含 DRUG_INTERACTION
3. `shouldWarnWhenDoseExceedsLimit` — 处方中某药品剂量超出安全范围 → riskLevel="WARN"，alerts 包含 DOSE_EXCEED
4. `shouldWarnWhenDuplicateDrugs` — 处方含两种成分相同的药品 → riskLevel="WARN"，alerts 包含 DUPLICATE_DRUG
5. `shouldBlockWhenAllergyConflict` — 患者过敏史与处方药品匹配 → riskLevel="BLOCK"，alerts 包含 ALLERGY_CONFLICT
6. `shouldWarnWhenPediatricPatient` — 患者年龄<18 且处方含儿童慎用药 → riskLevel="WARN"（或 BLOCK），alerts 包含 SPECIAL_POP_WARN
7. `shouldSetFromFallbackTrue` — 验证所有返回结果的 `isFromFallback()` 为 true
8. `shouldHandleEmptyPrescriptionItems` — 空处方条目列表 → riskLevel="PASS"，alerts 为空
9. `shouldHandleNullPatientInfo` — patientInfo 为 null → 跳过过敏史和特殊人群检查，其他检查正常执行

### 3. 涉及文件清单

| 操作 | 文件路径 |
|------|---------|
| 新建 | `ai-impl/.../fallback/PrescriptionLocalRuleFallback.java` |
| 新建 | `ai-impl/.../fallback/PrescriptionLocalRuleFallbackTest.java` |

### 4. 不修改的文件
- `LocalRuleFallback.java` — 接口不变，保持 `<T,R> R fallback(T)` 签名
- `PrescriptionCheckCapabilityExecutor.java` — 现有构造器已注入 `LocalRuleFallback<PrescriptionCheckRequest, PrescriptionCheckResponse>`，运行时自动装配此实现
- `AbstractCapabilityExecutor.java` — `doDegrade()` 中 `localRuleFallback.fallback(request)` 已存在，零修改

## 选择理由

Batch7 P3 第二项。Task 20（StructuredOutputParser + JsonStructuredOutputParser）已完成，PrescriptionLocalRuleFallback 是 Batch7 最后一项代码任务。它仅依赖已有的 LocalRuleFallback 接口和 PrescriptionCheckRequest/Response DTO，零外部新增依赖。完成后剩余任务为 Batch6 P3 配置装配类（Tasks 18-19），可在后续独立完成。

## 任务上下文

### 设计文档摘录（06_ood_phase5_G.md §3.7）

```
LocalRuleFallback — 本地规则降级契约（interface，归属 ai-impl/fallback/）
职责：定义 AI 能力降级到本地规则校验时的执行入口。仅特定能力需要实现此接口（当前仅为 3.4.2 AI 处方审核）。

PrescriptionLocalRuleFallback — 处方审核本地规则降级实现（class，归属 ai-impl/fallback/）
职责：当 AI 处方审核底座管线 LLM 调用失败或触发降级时，由 PrescriptionCheckCapabilityExecutor.doDegrade() 调用此 Fallback 执行本地规则校验，返回降级后的审核结果。

最小安全规则列表：5 项（配伍禁忌检查、剂量范围检查、重复用药检查、过敏史检查、儿童/孕妇用药警示）

执行失败时的安全策略：白名单模式——所有检查项因数据缺失无法判定时默认返回"通过"，
同时在返回结果中标记 dataSourceFailed: true 和 fallbackReason: "LOCAL_RULE_DATA_UNAVAILABLE"
```

### 已有代码上下文

- `LocalRuleFallback.java` — 现有接口，签名为 `R fallback(T request)`（不修改）
- `AbstractCapabilityExecutor.java:286-287` — `doDegrade()` 中调用 `localRuleFallback.fallback(request)`，若返回非 null 则包装为 `AiResult.success(fallbackResult)`
- `PrescriptionCheckCapabilityExecutor.java` — 构造器第 11 参数类型为 `LocalRuleFallback<PrescriptionCheckRequest, PrescriptionCheckResponse>`，Spring 自动装配本实现
- `PrescriptionCheckRequest.java` — 含 `prescriptionItems: List<PrescriptionCheckItem>`、`patientInfo: PatientInfo`、`prescriptionId: String`
- `PrescriptionCheckResponse.java` — 含 `riskLevel`、`alerts: List<AlertItem>`、`fromFallback: boolean`
- `AlertItem.java` — 含 `alertCode`、`alertMessage`、`severity`

---

## 修订说明（v23 r1）
| 审查意见 | 修改措施 |
|---------|---------|
| [严重] 路线表 row 21 涉及文件列错误包含 LocalRuleFallback.java | 路线表 row 21 任务名称改为"PrescriptionLocalRuleFallback（新建）"，涉及文件列移除 `LocalRuleFallback.java`，补充 `PrescriptionLocalRuleFallbackTest.java`，确认与 task_v23.md §4 不修改 LocalRuleFallback 的指令一致 |
| [一般] 路线表 row 21 缺少测试文件 PrescriptionLocalRuleFallbackTest.java | 路线表 row 21 涉及文件列补充 `PrescriptionLocalRuleFallbackTest.java` |
| [一般] R27 NEW 上下文依赖描述不准确（声称依赖 Task 20 JSON 解析能力） | R27 NEW 选择理由和上下文修正为"基于 hardcode 内存数据实现 5 项预设规则检查，不涉及 JSON 解析"，替换原"直接依赖 Task 20 的 JSON 解析能力做结构化数据解析"描述 |

## 修订说明（v23 r2）
| 审查意见 | 修改措施 |
|---------|---------|
| [一般] PrescriptionLocalRuleFallback 缺少 Spring 注解声明，导致运行时无法被自动装配 | 类签名添加 `@Service` 注解（遵循项目已有模式），PrescriptionCheckCapabilityExecutor 的 `@Autowired` 构造器即可通过 Spring 容器自动注入此实现；代码逻辑无需其他改动 |
