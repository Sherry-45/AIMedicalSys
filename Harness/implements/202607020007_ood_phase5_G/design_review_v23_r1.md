# 设计审查报告（v23 r1）

## 审查结果
REJECTED

## 发现

**[严重] AlertItem 构造器不匹配**
设计代码中多处使用 `new AlertItem("DRUG_INTERACTION", "BLOCK", "消息内容")` 三参构造器创建 AlertItem 实例，但 `AlertItem` 类（`ai-api/.../prescription/AlertItem.java:9`）仅有无参构造器 `AlertItem() {}`，不存在三参构造器。项目中既有代码（如 `PrescriptionDtoTest.java:91`、`PrescriptionAuditServiceImplTest.java:355`）均使用无参构造器 + setter 方式。设计代码将直接导致编译失败。

**[一般] 通配符导入与项目风格不一致**
设计代码使用 `import com.aimedical.modules.ai.api.dto.prescription.*` 和 `import java.util.*` 通配符导入，但项目中既有代码（如 `AbstractCapabilityExecutor.java` 等）均使用显式导入。建议统一为显式导入以保持风格一致。

**[一般] 剂量范围检查未使用 frequency/duration 字段**
`PrescriptionCheckItem` 包含 `frequency`、`duration`、`unit` 字段（`PrescriptionCheckItem.java:8-10`），但剂量范围检查 `checkDoseRange` 仅检查单次剂量 `dose`。task_v23.md §5 要求"检查单次剂量/日剂量是否在安全范围内"。当前实现未计算日剂量（dose × frequency）。建议在设计中明确：若仅做单次剂量检查，补充说明理由；若需覆盖日剂量，需扩展 `DoseRange` 支持单次与日剂量两个阈值或明确说明简化原因。

## 修改要求

### 严重问题
1. **AlertItem 构造器**：修正所有 `new AlertItem(code, severity, message)` 调用为无参构造 + setter 方式——`AlertItem item = new AlertItem(); item.setAlertCode(...); item.setSeverity(...); item.setAlertMessage(...);`

### 一般问题
2. **导入风格**：将通配符导入 `com.aimedical.modules.ai.api.dto.prescription.*` 和 `java.util.*` 替换为显式导入
3. **剂量范围**：明确设计意图——若仅检查单次剂量，建议在设计规格中补充说明理由；若需覆盖日剂量，需扩展 `DoseRange` 结构或补充说明当前简化的合理性
