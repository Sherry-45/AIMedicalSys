package com.aimedical.modules.examination.converter;

import com.aimedical.modules.examination.dto.ExaminationDTO;
import com.aimedical.modules.examination.dto.ExaminationItemDTO;
import com.aimedical.modules.examination.entity.Examination;
import com.aimedical.modules.examination.entity.ExaminationItem;
import com.aimedical.modules.examination.entity.ExaminationStatus;
import com.aimedical.modules.examination.entity.ExaminationType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExaminationConverterTest {

    @Test
    void toDTO_shouldReturnNull_whenExaminationIsNull() {
        ExaminationDTO dto = ExaminationConverter.toDTO(null, Collections.emptyList());
        assertThat(dto).isNull();
    }

    @Test
    void toDTO_shouldReturnNull_whenExaminationIsNullAndItemsNull() {
        ExaminationDTO dto = ExaminationConverter.toDTO(null, null);
        assertThat(dto).isNull();
    }

    @Test
    void toDTO_shouldConvertWithEmptyItems_whenItemsIsNull() {
        Examination examination = buildExamination(1L, ExaminationStatus.PENDING);

        ExaminationDTO dto = ExaminationConverter.toDTO(examination, null);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStatus()).isEqualTo(ExaminationStatus.PENDING);
        assertThat(dto.getItems()).isEmpty();
    }

    @Test
    void toDTO_shouldConvertAllFieldsAndItems() {
        Examination examination = buildExamination(2L, ExaminationStatus.COMPLETED);
        examination.setBodyPart("胸部");
        examination.setClinicalDiagnosis("咳嗽");
        examination.setImpression("未见异常");
        examination.setConclusion("正常");
        examination.setReportedAt(LocalDateTime.now());

        ExaminationItem item1 = buildItem(10L, 2L, "肺窗");
        ExaminationItem item2 = buildItem(11L, 2L, "纵隔窗");

        ExaminationDTO dto = ExaminationConverter.toDTO(examination, Arrays.asList(item1, item2));

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getPatientId()).isEqualTo(100L);
        assertThat(dto.getDoctorId()).isEqualTo(200L);
        assertThat(dto.getExaminationType()).isEqualTo(ExaminationType.CT);
        assertThat(dto.getBodyPart()).isEqualTo("胸部");
        assertThat(dto.getClinicalDiagnosis()).isEqualTo("咳嗽");
        assertThat(dto.getStatus()).isEqualTo(ExaminationStatus.COMPLETED);
        assertThat(dto.getImpression()).isEqualTo("未见异常");
        assertThat(dto.getConclusion()).isEqualTo("正常");
        assertThat(dto.getItems()).hasSize(2);
        assertThat(dto.getItems().get(0).getItemName()).isEqualTo("肺窗");
        assertThat(dto.getItems().get(1).getItemName()).isEqualTo("纵隔窗");
    }

    @Test
    void toItemDTO_shouldReturnNull_whenItemIsNull() {
        ExaminationItemDTO dto = ExaminationConverter.toItemDTO(null);
        assertThat(dto).isNull();
    }

    @Test
    void toItemDTO_shouldConvertAllFields() {
        ExaminationItem item = buildItem(5L, 1L, "病灶");
        item.setFinding("结节");
        item.setMeasurement("5mm");
        item.setAbnormalFlag(true);

        ExaminationItemDTO dto = ExaminationConverter.toItemDTO(item);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getExaminationId()).isEqualTo(1L);
        assertThat(dto.getItemName()).isEqualTo("病灶");
        assertThat(dto.getFinding()).isEqualTo("结节");
        assertThat(dto.getMeasurement()).isEqualTo("5mm");
        assertThat(dto.getAbnormalFlag()).isTrue();
    }

    private Examination buildExamination(Long id, ExaminationStatus status) {
        Examination examination = new Examination();
        examination.setId(id);
        examination.setPatientId(100L);
        examination.setDoctorId(200L);
        examination.setExaminationType(ExaminationType.CT);
        examination.setStatus(status);
        examination.setCreatedAt(LocalDateTime.now());
        examination.setUpdatedAt(LocalDateTime.now());
        return examination;
    }

    private ExaminationItem buildItem(Long id, Long examinationId, String itemName) {
        ExaminationItem item = new ExaminationItem();
        item.setId(id);
        item.setExaminationId(examinationId);
        item.setItemName(itemName);
        item.setAbnormalFlag(false);
        return item;
    }
}
