package com.aimedical.modules.labtest.dto;

import com.aimedical.modules.labtest.entity.AbnormalFlag;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 检验趋势图 DTO。
 */
@Data
public class LabTestTrendDTO {

    private String itemName;
    private String unit;
    private List<TrendPoint> points;

    @Data
    public static class TrendPoint {

        private LocalDateTime testDate;
        private String result;
        private AbnormalFlag abnormalFlag;
    }
}
