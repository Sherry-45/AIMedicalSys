package com.aimedical.modules.labtest.converter;

import com.aimedical.modules.labtest.dto.LabTestDTO;
import com.aimedical.modules.labtest.dto.LabTestItemDTO;
import com.aimedical.modules.labtest.entity.AbnormalFlag;
import com.aimedical.modules.labtest.entity.LabTest;
import com.aimedical.modules.labtest.entity.LabTestItem;
import com.aimedical.modules.labtest.entity.LabTestStatus;
import com.aimedical.modules.labtest.entity.SampleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LabTestConverter 转换器单元测试。
 */
@DisplayName("LabTestConverter 转换器")
class LabTestConverterTest {

    @Test
    @DisplayName("LabTest 实体与明细列表转 DTO 应完整映射所有字段")
    void shouldConvertLabTestAndItemsToDto() {
        LocalDateTime now = LocalDateTime.now();
        LabTest entity = new LabTest();
        entity.setId(1L);
        entity.setPatientId(10L);
        entity.setDoctorId(20L);
        entity.setTestType("血常规");
        entity.setSampleType(SampleType.BLOOD);
        entity.setCollectedAt(now);
        entity.setStatus(LabTestStatus.COMPLETED);
        entity.setReportConclusion("正常");
        entity.setAiInterpretation("AI解读");
        entity.setReportedAt(now);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        LabTestItem item = new LabTestItem();
        item.setId(100L);
        item.setLabTestId(1L);
        item.setItemName("白细胞计数");
        item.setResult("6.5");
        item.setUnit("10^9/L");
        item.setReferenceRange("4-10");
        item.setAbnormalFlag(AbnormalFlag.NORMAL);

        LabTestDTO dto = LabTestConverter.toDTO(entity, List.of(item));

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getPatientId()).isEqualTo(10L);
        assertThat(dto.getDoctorId()).isEqualTo(20L);
        assertThat(dto.getTestType()).isEqualTo("血常规");
        assertThat(dto.getSampleType()).isEqualTo(SampleType.BLOOD);
        assertThat(dto.getCollectedAt()).isEqualTo(now);
        assertThat(dto.getStatus()).isEqualTo(LabTestStatus.COMPLETED);
        assertThat(dto.getReportConclusion()).isEqualTo("正常");
        assertThat(dto.getAiInterpretation()).isEqualTo("AI解读");
        assertThat(dto.getReportedAt()).isEqualTo(now);
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getUpdatedAt()).isEqualTo(now);
        assertThat(dto.getItems()).hasSize(1);
        LabTestItemDTO itemDto = dto.getItems().get(0);
        assertThat(itemDto.getId()).isEqualTo(100L);
        assertThat(itemDto.getLabTestId()).isEqualTo(1L);
        assertThat(itemDto.getItemName()).isEqualTo("白细胞计数");
        assertThat(itemDto.getResult()).isEqualTo("6.5");
        assertThat(itemDto.getUnit()).isEqualTo("10^9/L");
        assertThat(itemDto.getReferenceRange()).isEqualTo("4-10");
        assertThat(itemDto.getAbnormalFlag()).isEqualTo(AbnormalFlag.NORMAL);
    }

    @Test
    @DisplayName("toDTO 当实体为 null 时返回 null")
    void shouldReturnNullWhenEntityNull() {
        assertThat(LabTestConverter.toDTO(null, Collections.emptyList())).isNull();
    }

    @Test
    @DisplayName("toDTO 当明细列表为 null 时 items 映射为空列表")
    void shouldReturnEmptyItemsWhenItemsNull() {
        LabTest entity = new LabTest();
        entity.setId(1L);
        entity.setPatientId(10L);
        entity.setStatus(LabTestStatus.PENDING);

        LabTestDTO dto = LabTestConverter.toDTO(entity, null);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getItems()).isEmpty();
    }

    @Test
    @DisplayName("toItemDTO 应完整映射明细字段")
    void shouldConvertItemToDto() {
        LabTestItem item = new LabTestItem();
        item.setId(2L);
        item.setLabTestId(5L);
        item.setItemName("血红蛋白");
        item.setResult("110");
        item.setUnit("g/L");
        item.setReferenceRange("120-160");
        item.setAbnormalFlag(AbnormalFlag.LOW);

        LabTestItemDTO dto = LabTestConverter.toItemDTO(item);

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getLabTestId()).isEqualTo(5L);
        assertThat(dto.getItemName()).isEqualTo("血红蛋白");
        assertThat(dto.getResult()).isEqualTo("110");
        assertThat(dto.getUnit()).isEqualTo("g/L");
        assertThat(dto.getReferenceRange()).isEqualTo("120-160");
        assertThat(dto.getAbnormalFlag()).isEqualTo(AbnormalFlag.LOW);
    }

    @Test
    @DisplayName("toItemDTO 当入参为 null 时返回 null")
    void shouldReturnNullWhenItemNull() {
        assertThat(LabTestConverter.toItemDTO(null)).isNull();
    }

    @Test
    @DisplayName("toItemDtoList 当入参为 null 时返回空列表")
    void shouldReturnEmptyListWhenItemListNull() {
        assertThat(LabTestConverter.toItemDtoList(null)).isEmpty();
    }

    @Test
    @DisplayName("toItemDtoList 应批量转换明细")
    void shouldConvertItemList() {
        LabTestItem item1 = new LabTestItem();
        item1.setId(1L);
        item1.setItemName("白细胞");
        LabTestItem item2 = new LabTestItem();
        item2.setId(2L);
        item2.setItemName("红细胞");

        List<LabTestItemDTO> result = LabTestConverter.toItemDtoList(List.of(item1, item2));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(LabTestItemDTO::getItemName)
                .containsExactly("白细胞", "红细胞");
    }
}
