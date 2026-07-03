package com.aimedical.modules.ai.impl.fallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.aimedical.modules.ai.api.dto.prescription.AlertItem;
import com.aimedical.modules.ai.api.dto.prescription.AllergyDetailItem;
import com.aimedical.modules.ai.api.dto.prescription.PatientInfo;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionCheckItem;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionCheckRequest;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionCheckResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(name = "ai.platform.enabled", havingValue = "true")
@Service
public class PrescriptionLocalRuleFallback
    implements LocalRuleFallback<PrescriptionCheckRequest, PrescriptionCheckResponse> {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionLocalRuleFallback.class);

    private static final Set<String> DRUG_INTERACTIONS = new HashSet<>();

    private static final Map<String, DoseRange> SAFE_DOSE_LIMITS = new HashMap<>();

    private static final Map<String, String> DRUG_INGREDIENTS = new HashMap<>();

    private static final Set<String> PEDIATRIC_CAUTION_DRUGS = new HashSet<>();

    private static final Set<String> PREGNANT_CAUTION_DRUGS = new HashSet<>();

    static {
        initInteractionData();
        initDoseData();
        initIngredientData();
        initSpecialPopData();
    }

    private static void initInteractionData() {
        DRUG_INTERACTIONS.add("drug_cef:drug_eth");
    }

    private static void initDoseData() {
        SAFE_DOSE_LIMITS.put("drug_para", new DoseRange(300, 1000));
        SAFE_DOSE_LIMITS.put("drug_ibu", new DoseRange(200, 800));
        SAFE_DOSE_LIMITS.put("drug_amox", new DoseRange(250, 1000));
    }

    private static void initIngredientData() {
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
        try {
            List<AlertItem> alerts = new ArrayList<>();

            List<PrescriptionCheckItem> items = request.getPrescriptionItems();
            if (items != null && !items.isEmpty()) {
                checkDrugInteractions(items, alerts);
                checkDoseRange(items, alerts);
                checkDuplicateDrugs(items, alerts);
            }

            PatientInfo patient = request.getPatientInfo();
            if (patient != null && items != null && !items.isEmpty()) {
                checkAllergy(patient, items, alerts);
            }

            if (patient != null && items != null && !items.isEmpty()) {
                checkSpecialPopulation(patient, items, alerts);
            }

            if (hasBlockAlert(alerts)) {
                result.setRiskLevel("BLOCK");
            } else if (hasWarnAlert(alerts)) {
                result.setRiskLevel("WARN");
            } else {
                result.setRiskLevel("PASS");
            }
            result.setAlerts(alerts);
        } catch (Exception e) {
            log.warn("Prescription local rule fallback failed, marking as CHECK_SKIPPED", e);
            result.setRiskLevel("CHECK_SKIPPED");
            result.setAlerts(Collections.emptyList());
        }
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
            if (ingredient == null && item.getDrugName() != null) {
                ingredient = item.getDrugName().toLowerCase();
            }
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

    private static class DoseRange {
        final double minDose;
        final double maxDose;

        DoseRange(double minDose, double maxDose) {
            this.minDose = minDose;
            this.maxDose = maxDose;
        }
    }
}
