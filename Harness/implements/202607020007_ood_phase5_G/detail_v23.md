# 详细设计（v23）

## 概述

实现 `PrescriptionLocalRuleFallback`，作为 `LocalRuleFallback<PrescriptionCheckRequest, PrescriptionCheckResponse>` 接口的处方审核本地规则降级实现。当 AI 处方审核底座管线 LLM 调用失败或触发降级时，由 `PrescriptionCheckCapabilityExecutor.doDegrade()` 调用此 Fallback 执行 5 项硬编码本地规则检查，返回降级后的审核结果。

## 文件规划

| 文件路径 | 操作 | 职责 |
|---------|------|------|
| `ai-impl/src/main/java/com/aimedical/modules/ai/impl/fallback/PrescriptionLocalRuleFallback.java` | 新建 | `LocalRuleFallback` 的处方审核本地规则实现 |
| `ai-impl/src/test/java/com/aimedical/modules/ai/impl/fallback/PrescriptionLocalRuleFallbackTest.java` | 新建 | 9 个测试覆盖正常/异常/边界场景 |

## 类型定义

### PrescriptionLocalRuleFallback

**形态**：class
**包路径**：`com.aimedical.modules.ai.impl.fallback`
**职责**：基于 5 项硬编码本地规则对处方进行安全审核，当 AI 管线不可用时作为降级方案执行

```java
package com.aimedical.modules.ai.impl.fallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.aimedical.modules.ai.api.dto.prescription.AlertItem;
import com.aimedical.modules.ai.api.dto.prescription.AllergyDetailItem;
import com.aimedical.modules.ai.api.dto.prescription.PatientInfo;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionCheckItem;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionCheckRequest;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionCheckResponse;

@Service
public class PrescriptionLocalRuleFallback
    implements LocalRuleFallback<PrescriptionCheckRequest, PrescriptionCheckResponse> {

    // 配伍禁忌表: Set of "drugId1:drugId2" (小写排序后)
    private static final Set<String> DRUG_INTERACTIONS = new HashSet<>();

    // 安全剂量映射: drugId -> DoseRange
    private static final Map<String, DoseRange> SAFE_DOSE_LIMITS = new HashMap<>();

    // 成分归属映射: drugId -> ingredient (药品ID→活性成分)
    private static final Map<String, String> DRUG_INGREDIENTS = new HashMap<>();

    // 儿童慎用药品列表: Set of drugId
    private static final Set<String> PEDIATRIC_CAUTION_DRUGS = new HashSet<>();

    // 孕妇慎用药品列表: Set of drugId
    private static final Set<String> PREGNANT_CAUTION_DRUGS = new HashSet<>();

    static {
        initInteractionData();
        initDoseData();
        initIngredientData();
        initSpecialPopData();
    }

    private static void initInteractionData() {
        // hardcode 最小样本数据: "头孢类+酒精类" 示例
        DRUG_INTERACTIONS.add("drug_cef:drug_eth");
    }

    private static void initDoseData() {
        // hardcode 常见药品剂量阈值 (单次剂量安全范围, 单位 mg)
        SAFE_DOSE_LIMITS.put("drug_para", new DoseRange(300, 1000));
        SAFE_DOSE_LIMITS.put("drug_ibu", new DoseRange(200, 800));
        SAFE_DOSE_LIMITS.put("drug_amox", new DoseRange(250, 1000));
    }

    private static void initIngredientData() {
        // drugId → ingredient
        DRUG_INGREDIENTS.put("drug_para", "paracetamol");
        DRUG_INGREDIENTS.put("drug_acet", "paracetamol");
        DRUG_INGREDIENTS.put("drug_ibu", "ibuprofen");
        DRUG_INGREDIENTS.put("drug_advil", "ibuprofen");
    }

    private static void initSpecialPopData() {
        PEDIATRIC_CAUTION_DRUGS.add("drug_aspirin");
        PREGNANT_CAUTION_DRUGS.add("drug_iso");
        PREGNANT_CAUTION_DRUGS.add("drug_warfarin");
    }

    public PrescriptionLocalRuleFallback() {
    }

    @Override
    public PrescriptionCheckResponse fallback(PrescriptionCheckRequest request) {
        PrescriptionCheckResponse result = new PrescriptionCheckResponse();
        result.setFromFallback(true);
        List<AlertItem> alerts = new ArrayList<>();

        List<PrescriptionCheckItem> items = request.getPrescriptionItems();
        if (items != null && !items.isEmpty()) {
            // 1. 配伍禁忌检查
            checkDrugInteractions(items, alerts);
            // 2. 剂量范围检查
            checkDoseRange(items, alerts);
            // 3. 重复用药检查
            checkDuplicateDrugs(items, alerts);
        }

        PatientInfo patient = request.getPatientInfo();
        // 4. 过敏史检查 (patientInfo 非 null 时执行)
        if (patient != null && items != null && !items.isEmpty()) {
            checkAllergy(patient, items, alerts);
        }

        // 5. 儿童/孕妇用药警示 (patientInfo 非 null 时执行)
        if (patient != null && items != null && !items.isEmpty()) {
            checkSpecialPopulation(patient, items, alerts);
        }

        // 白名单模式: 确定风险等级
        if (hasBlockAlert(alerts)) {
            result.setRiskLevel("BLOCK");
        } else if (hasWarnAlert(alerts)) {
            result.setRiskLevel("WARN");
        } else {
            result.setRiskLevel("PASS");
        }
        result.setAlerts(alerts);
        return result;
    }

    private void checkDrugInteractions(List<PrescriptionCheckItem> items, List<AlertItem> alerts) {
        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                String id1 = items.get(i).getDrugId();
                String id2 = items.get(j).getDrugId();
                String name1 = items.get(i).getDrugName();
                String name2 = items.get(j).getDrugName();
                String pair = toInteractionKey(id1, id2);
                if (DRUG_INTERACTIONS.contains(pair)) {
                    AlertItem alert = new AlertItem();
                    alert.setAlertCode("DRUG_INTERACTION");
                    alert.setSeverity("BLOCK");
                    alert.setAlertMessage("药品" + name1 + "与" + name2 + "存在配伍禁忌");
                    alerts.add(alert);
                }
            }
        }
    }

    private void checkDoseRange(List<PrescriptionCheckItem> items, List<AlertItem> alerts) {
        for (PrescriptionCheckItem item : items) {
            DoseRange range = SAFE_DOSE_LIMITS.get(item.getDrugId());
            if (range != null && item.getDose() > 0) {
                if (item.getDose() < range.minDose || item.getDose() > range.maxDose) {
                    AlertItem alert = new AlertItem();
                    alert.setAlertCode("DOSE_EXCEED");
                    alert.setSeverity("WARN");
                    alert.setAlertMessage("药品" + item.getDrugName() + "单次剂量" + item.getDose()
                        + "mg超出推荐范围" + range.minDose + "-" + range.maxDose + "mg");
                    alerts.add(alert);
                }
            }
        }
    }

    private void checkDuplicateDrugs(List<PrescriptionCheckItem> items, List<AlertItem> alerts) {
        // ingredient -> first drug name found
        Map<String, String> seenIngredients = new HashMap<>();
        for (PrescriptionCheckItem item : items) {
            String ingredient = DRUG_INGREDIENTS.get(item.getDrugId());
            if (ingredient == null) {
                continue;
            }
            String existing = seenIngredients.get(ingredient);
            if (existing != null) {
                AlertItem alert = new AlertItem();
                alert.setAlertCode("DUPLICATE_DRUG");
                alert.setSeverity("WARN");
                alert.setAlertMessage("药品" + item.getDrugName() + "与" + existing + "成分相同");
                alerts.add(alert);
            } else {
                seenIngredients.put(ingredient, item.getDrugName());
            }
        }
    }

    private void checkAllergy(PatientInfo patient, List<PrescriptionCheckItem> items, List<AlertItem> alerts) {
        // 从 allergyDetails 中收集过敏原
        Set<String> allergens = new HashSet<>();
        if (patient.getAllergyDetails() != null) {
            for (AllergyDetailItem detail : patient.getAllergyDetails()) {
                if (detail.getAllergen() != null) {
                    allergens.add(detail.getAllergen().toLowerCase());
                }
            }
        }
        if (patient.getAllergyHistory() != null) {
            for (String item : patient.getAllergyHistory().split(",")) {
                String trimmed = item.trim().toLowerCase();
                if (!trimmed.isEmpty()) {
                    allergens.add(trimmed);
                }
            }
        }

        if (allergens.isEmpty()) {
            return;
        }

        for (PrescriptionCheckItem item : items) {
            String ingredient = DRUG_INGREDIENTS.get(item.getDrugId());
            if (ingredient != null && allergens.contains(ingredient.toLowerCase())) {
                AlertItem alert = new AlertItem();
                alert.setAlertCode("ALLERGY_CONFLICT");
                alert.setSeverity("BLOCK");
                alert.setAlertMessage("药品" + item.getDrugName() + "含有您已知过敏成分" + ingredient);
                alerts.add(alert);
            }
        }
    }

    private void checkSpecialPopulation(PatientInfo patient, List<PrescriptionCheckItem> items, List<AlertItem> alerts) {
        boolean isPediatric = patient.getAge() != null && patient.getAge() < 18;
        boolean isPregnant = false;
        if (patient.getComorbidities() != null) {
            for (String comorbidity : patient.getComorbidities()) {
                if (comorbidity != null && (comorbidity.contains("妊娠") || comorbidity.contains("怀孕")
                    || comorbidity.contains("pregnancy"))) {
                    isPregnant = true;
                    break;
                }
            }
        }

        for (PrescriptionCheckItem item : items) {
            if (isPediatric && PEDIATRIC_CAUTION_DRUGS.contains(item.getDrugId())) {
                AlertItem alert = new AlertItem();
                alert.setAlertCode("SPECIAL_POP_WARN");
                alert.setSeverity("WARN");
                alert.setAlertMessage("药品" + item.getDrugName() + "在儿童中慎用");
                alerts.add(alert);
            }
            if (isPregnant && PREGNANT_CAUTION_DRUGS.contains(item.getDrugId())) {
                AlertItem alert = new AlertItem();
                alert.setAlertCode("SPECIAL_POP_WARN");
                alert.setSeverity("BLOCK");
                alert.setAlertMessage("药品" + item.getDrugName() + "在孕妇中禁用");
                alerts.add(alert);
            }
        }
    }

    private static String toInteractionKey(String id1, String id2) {
        if (id1 == null || id2 == null) {
            return "";
        }
        String a = id1.toLowerCase();
        String b = id2.toLowerCase();
        return a.compareTo(b) <= 0 ? a + ":" + b : b + ":" + a;
    }

    private static boolean hasBlockAlert(List<AlertItem> alerts) {
        for (AlertItem a : alerts) {
            if ("BLOCK".equals(a.getSeverity())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasWarnAlert(List<AlertItem> alerts) {
        for (AlertItem a : alerts) {
            if ("WARN".equals(a.getSeverity())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 安全剂量范围内部类
     */
    private static class DoseRange {
        final double minDose;
        final double maxDose;

        DoseRange(double minDose, double maxDose) {
            this.minDose = minDose;
            this.maxDose = maxDose;
        }
    }
}
```

**公开接口**：
- `PrescriptionLocalRuleFallback()` — 无参构造器，所有规则数据通过 static initializer 硬编码初始化
- `PrescriptionCheckResponse fallback(PrescriptionCheckRequest request)` — 执行 5 项本地规则检查，返回审核结果

**构造方式**：`new PrescriptionLocalRuleFallback()`（Spring `@Service` 自动管理单例）
**类型关系**：实现 `LocalRuleFallback<PrescriptionCheckRequest, PrescriptionCheckResponse>` 接口

## ErrorItem 辅助类型

**形态**：无独立类型，5 项检查均复用已有的 `AlertItem`（`com.aimedical.modules.ai.api.dto.prescription.AlertItem`）通过无参构造器 + setter 创建。

**AlertItem 创建方式**：
```java
AlertItem alert = new AlertItem();
alert.setAlertCode("DRUG_INTERACTION");
alert.setSeverity("BLOCK");
alert.setAlertMessage("药品X与Y存在配伍禁忌");
alerts.add(alert);
```

## DoseRange 内部类

**形态**：`private static` inner class
**包路径**：`PrescriptionLocalRuleFallback` 内部
**字段**：
- `double minDose` — 单次最小安全剂量（mg）
- `double maxDose` — 单次最大安全剂量（mg）

## 错误处理

| 条件 | 处理方式 |
|------|---------|
| `prescriptionItems` 为 null 或空 | 跳过检查项 1-3，仅返回 `fromFallback=true` + `riskLevel="PASS"` |
| `patientInfo` 为 null | 跳过检查项 4（过敏史）和 5（特殊人群），1-3 正常执行 |
| `patientInfo.age` 为 null | `isPediatric` 为 false，不触发儿童用药检查 |
| `patientInfo.allergyDetails` 或 `allergyHistory` 为 null/空 | 过敏检查无过敏原，不产生 alert |
| `patientInfo.comorbidities` 为 null | 妊娠判断为 false，不触发孕妇检查 |
| 药品 ID 不在硬编码表中 | 该规则静默跳过（白名单模式—默认通过） |
| 所有规则均不产生 alert | `riskLevel="PASS"` |
| 单个 `drugId` 为 null | `toInteractionKey` 返回空串，不匹配任何禁忌项；`DRUG_INGREDIENTS.get(null)` 返回 null，静默跳过 |

## 行为契约

1. **白名单安全策略**：任何检查项若因数据缺失无法执行（如药品 ID 不在表中），静默跳过该检查项，不产生 alert，不影响风险等级判定
2. **风险等级优先级**：BLOCK > WARN > PASS。只要有一个 BLOCK 等级 alert，整体等级即为 BLOCK；无 BLOCK 但存在至少一个 WARN，则等级为 WARN；全部通过则为 PASS
3. **`fromFallback`**：所有返回结果的 `isFromFallback()` 均为 `true`
4. **副作用**：无。每次调用独立创建 `PrescriptionCheckResponse` 和 `List<AlertItem>`，不修改入参 `request`
5. **线程安全**：该类无 mutable 共享状态，所有硬编码数据为 `static final`，一次调用产生的 `alerts` 列表不会逃逸

## 剂量检查简化说明

当前 `checkDoseRange` 仅检查单次剂量（`item.getDose()`），未计算日剂量（dose × frequency）。原因：
- `frequency` 字段为字符串（如 "tid"、"bid"、"q8h"），解析为数值倍数需要额外映射逻辑，属于非功能增强
- 当前 hardcode 样本均为单剂量阈值，覆盖常见药品的简单超标检测
- 若后续需要日剂量检查，可扩展 `DoseRange` 增加 `maxDailyDose` 字段并添加 `parseFrequencyMultiplier()` 方法

## 依赖关系

### 依赖的已有类型
- `LocalRuleFallback<T,R>` — 实现的接口（同包，`ai-impl/fallback/`）
- `PrescriptionCheckRequest` — 入参 DTO（`ai-api/dto/prescription/`）
- `PrescriptionCheckResponse` — 返回值 DTO（`ai-api/dto/prescription/`）
- `PrescriptionCheckItem` — 处方条目 DTO（`ai-api/dto/prescription/`）
- `PatientInfo` — 患者信息 DTO（`ai-api/dto/prescription/`）
- `AllergyDetailItem` — 过敏详情 DTO（`ai-api/dto/prescription/`）
- `AlertItem` — 告警条目 DTO（`ai-api/dto/prescription/`）
- `org.springframework.stereotype.Service` — Spring `@Service` 注解

### 不修改的已有文件
- `LocalRuleFallback.java` — 接口不变
- `PrescriptionCheckCapabilityExecutor.java` — 构造器第 11 参数 `LocalRuleFallback<PrescriptionCheckRequest, PrescriptionCheckResponse>`，Spring 自动装配本实现
- `AbstractCapabilityExecutor.java` — `doDegrade()` 中 `localRuleFallback.fallback(request)` 调用，零修改
