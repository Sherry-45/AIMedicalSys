package com.aimedical.modules.ai.impl.fallback;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.aimedical.modules.ai.api.dto.prescription.AlertItem;
import com.aimedical.modules.ai.api.dto.prescription.AllergyDetailItem;
import com.aimedical.modules.ai.api.dto.prescription.PatientInfo;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionCheckItem;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionCheckRequest;
import com.aimedical.modules.ai.api.dto.prescription.PrescriptionCheckResponse;

import static org.junit.jupiter.api.Assertions.*;

class PrescriptionLocalRuleFallbackTest {

    private final PrescriptionLocalRuleFallback fallback = new PrescriptionLocalRuleFallback();

    @Test
    void allRulesPassShouldReturnPASS() {
        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(createItem("drug_para", "对乙酰氨基酚", 500)));
        request.setPatientInfo(createPatient(30, null, null, null));

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("PASS", response.getRiskLevel());
        assertTrue(response.getAlerts() == null || response.getAlerts().isEmpty());
    }

    @Test
    void drugInteractionShouldReturnBLOCK() {
        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(
            createItem("drug_cef", "头孢", 500),
            createItem("drug_eth", "酒精", 0)
        ));

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("BLOCK", response.getRiskLevel());
        assertTrue(response.getAlerts().stream().anyMatch(a -> "DRUG_INTERACTION".equals(a.getAlertCode())));
    }

    @Test
    void doseBelowMinShouldReturnWARN() {
        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(createItem("drug_para", "对乙酰氨基酚", 200)));

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("WARN", response.getRiskLevel());
        assertTrue(response.getAlerts().stream().anyMatch(a -> "DOSE_EXCEED".equals(a.getAlertCode())));
    }

    @Test
    void doseAboveMaxShouldReturnWARN() {
        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(createItem("drug_para", "对乙酰氨基酚", 1100)));

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("WARN", response.getRiskLevel());
        assertTrue(response.getAlerts().stream().anyMatch(a -> "DOSE_EXCEED".equals(a.getAlertCode())));
    }

    @Test
    void duplicateDrugShouldReturnWARN() {
        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(
            createItem("drug_para", "对乙酰氨基酚", 500),
            createItem("drug_acet", "泰诺", 500)
        ));

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("WARN", response.getRiskLevel());
        assertTrue(response.getAlerts().stream().anyMatch(a -> "DUPLICATE_DRUG".equals(a.getAlertCode())));
    }

    @Test
    void allergyConflictShouldReturnBLOCK() {
        AllergyDetailItem allergy = new AllergyDetailItem();
        allergy.setAllergen("paracetamol");

        PatientInfo patient = new PatientInfo();
        patient.setAllergyDetails(List.of(allergy));

        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(createItem("drug_para", "对乙酰氨基酚", 500)));
        request.setPatientInfo(patient);

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("BLOCK", response.getRiskLevel());
        assertTrue(response.getAlerts().stream().anyMatch(a -> "ALLERGY_CONFLICT".equals(a.getAlertCode())));
    }

    @Test
    void pediatricCautionShouldReturnWARN() {
        PatientInfo patient = new PatientInfo();
        patient.setAge(10);

        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(createItem("drug_aspirin", "阿司匹林", 100)));
        request.setPatientInfo(patient);

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("WARN", response.getRiskLevel());
        assertTrue(response.getAlerts().stream().anyMatch(a -> "SPECIAL_POP_WARN".equals(a.getAlertCode())
            && "WARN".equals(a.getSeverity())));
    }

    @Test
    void pregnantCautionShouldReturnBLOCK() {
        PatientInfo patient = new PatientInfo();
        patient.setAge(28);
        patient.setComorbidities(List.of("妊娠期高血压"));

        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(createItem("drug_iso", "异烟肼", 300)));
        request.setPatientInfo(patient);

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("BLOCK", response.getRiskLevel());
        assertTrue(response.getAlerts().stream().anyMatch(a -> "SPECIAL_POP_WARN".equals(a.getAlertCode())
            && "BLOCK".equals(a.getSeverity())));
    }

    @Test
    void nullPrescriptionItemsShouldReturnPASS() {
        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(null);

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("PASS", response.getRiskLevel());
    }

    @Test
    void emptyPrescriptionItemsShouldReturnPASS() {
        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of());

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("PASS", response.getRiskLevel());
        assertTrue(response.getAlerts() == null || response.getAlerts().isEmpty());
    }

    @Test
    void nullAgeShouldSkipPediatricCheck() {
        PatientInfo patient = new PatientInfo();
        patient.setAge(null);

        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(createItem("drug_aspirin", "阿司匹林", 100)));
        request.setPatientInfo(patient);

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("PASS", response.getRiskLevel());
        assertTrue(response.getAlerts() == null || response.getAlerts().isEmpty());
    }

    @Test
    void mixedBlockAndWarnShouldReturnBLOCK() {
        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(
            createItem("drug_cef", "头孢", 500),
            createItem("drug_eth", "酒精", 0),
            createItem("drug_para", "对乙酰氨基酚", 1100)
        ));

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("BLOCK", response.getRiskLevel());
        assertTrue(response.getAlerts().stream().anyMatch(a -> "DRUG_INTERACTION".equals(a.getAlertCode())));
        assertTrue(response.getAlerts().stream().anyMatch(a -> "DOSE_EXCEED".equals(a.getAlertCode())));
    }

    @Test
    void unknownDrugIdShouldReturnPASS() {
        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(createItem("drug_unknown", "未知药品", 500)));

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("PASS", response.getRiskLevel());
        assertTrue(response.getAlerts() == null || response.getAlerts().isEmpty());
    }

    @Test
    void nullDrugIdShouldBeSkipped() {
        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(
            createItem(null, "null药品", 500),
            createItem("drug_para", "对乙酰氨基酚", 500)
        ));

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("PASS", response.getRiskLevel());
        assertTrue(response.getAlerts() == null || response.getAlerts().isEmpty());
    }

    @Test
    void zeroDoseShouldBeSkipped() {
        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(createItem("drug_para", "对乙酰氨基酚", 0)));

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("PASS", response.getRiskLevel());
        assertTrue(response.getAlerts() == null || response.getAlerts().isEmpty());
    }

    @Test
    void negativeDoseShouldBeSkipped() {
        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(createItem("drug_para", "对乙酰氨基酚", -100)));

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("PASS", response.getRiskLevel());
        assertTrue(response.getAlerts() == null || response.getAlerts().isEmpty());
    }

    @Test
    void nullAllergyFieldsShouldNotCauseAllergyAlert() {
        PatientInfo patient = new PatientInfo();
        patient.setAge(30);
        patient.setAllergyDetails(null);
        patient.setAllergyHistory(null);

        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(createItem("drug_para", "对乙酰氨基酚", 500)));
        request.setPatientInfo(patient);

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("PASS", response.getRiskLevel());
        assertTrue(response.getAlerts() == null || response.getAlerts().isEmpty());
    }

    @Test
    void fallbackShouldNotModifyRequest() {
        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        List<PrescriptionCheckItem> items = List.of(createItem("drug_para", "对乙酰氨基酚", 500));
        request.setPrescriptionItems(items);
        PatientInfo patient = createPatient(30, null, null, null);
        request.setPatientInfo(patient);

        double doseBefore = items.get(0).getDose();
        String drugIdBefore = items.get(0).getDrugId();
        Integer ageBefore = patient.getAge();

        fallback.fallback(request);

        assertEquals(doseBefore, items.get(0).getDose());
        assertEquals(drugIdBefore, items.get(0).getDrugId());
        assertEquals(ageBefore, patient.getAge());
    }

    @Test
    void nullComorbiditiesShouldSkipPregnancyCheck() {
        PatientInfo patient = new PatientInfo();
        patient.setAge(28);
        patient.setComorbidities(null);

        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(createItem("drug_iso", "异烟肼", 300)));
        request.setPatientInfo(patient);

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("PASS", response.getRiskLevel());
        assertTrue(response.getAlerts() == null || response.getAlerts().isEmpty());
    }

    @Test
    void nullPatientInfoShouldSkipAllergyAndSpecialPop() {
        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(
            createItem("drug_cef", "头孢", 500),
            createItem("drug_eth", "酒精", 0)
        ));
        request.setPatientInfo(null);

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("BLOCK", response.getRiskLevel());
        assertTrue(response.getAlerts().stream().anyMatch(a -> "DRUG_INTERACTION".equals(a.getAlertCode())));
    }

    @Test
    void fallbackExceptionShouldReturnCheckSkipped() {
        PatientInfo patient = new PatientInfo();
        patient.setAge(30);
        List<AllergyDetailItem> badDetails = new ArrayList<>();
        badDetails.add(null);
        patient.setAllergyDetails(badDetails);

        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(createItem("drug_para", "对乙酰氨基酚", 500)));
        request.setPatientInfo(patient);

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("CHECK_SKIPPED", response.getRiskLevel());
        assertTrue(response.getAlerts().isEmpty());
    }

    @Test
    void allergyCheckShouldFallbackToDrugNameWhenDrugIdNotInIngredients() {
        AllergyDetailItem allergy = new AllergyDetailItem();
        allergy.setAllergen("advil");

        PatientInfo patient = new PatientInfo();
        patient.setAllergyDetails(List.of(allergy));

        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(createItem("drug_unknown", "Advil", 500)));
        request.setPatientInfo(patient);

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("BLOCK", response.getRiskLevel());
        assertTrue(response.getAlerts().stream().anyMatch(a -> "ALLERGY_CONFLICT".equals(a.getAlertCode())));
    }

    @Test
    void allergyCheckShouldNotFailWhenDrugNameIsNullAndDrugIdUnknown() {
        AllergyDetailItem allergy = new AllergyDetailItem();
        allergy.setAllergen("penicillin");

        PatientInfo patient = new PatientInfo();
        patient.setAllergyDetails(List.of(allergy));

        PrescriptionCheckRequest request = new PrescriptionCheckRequest();
        request.setPrescriptionItems(List.of(createItem("drug_unknown", null, 500)));
        request.setPatientInfo(patient);

        PrescriptionCheckResponse response = fallback.fallback(request);

        assertTrue(response.isFromFallback());
        assertEquals("PASS", response.getRiskLevel());
    }

    private static PrescriptionCheckItem createItem(String drugId, String drugName, double dose) {
        PrescriptionCheckItem item = new PrescriptionCheckItem();
        item.setDrugId(drugId);
        item.setDrugName(drugName);
        item.setDose(dose);
        return item;
    }

    private static PatientInfo createPatient(Integer age, String allergyHistory,
                                              List<AllergyDetailItem> allergyDetails,
                                              List<String> comorbidities) {
        PatientInfo patient = new PatientInfo();
        patient.setAge(age);
        patient.setAllergyHistory(allergyHistory);
        patient.setAllergyDetails(allergyDetails);
        patient.setComorbidities(comorbidities);
        return patient;
    }
}
