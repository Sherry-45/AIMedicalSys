package com.aimedical.modules.inventory.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 盘点单响应 DTO。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Data
public class StocktakingResponse {

    private Long id;
    private String stocktakingNo;
    private String stocktakingType;
    private String status;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalItems;
    private Integer surplusItems;
    private Integer lossItems;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<StocktakingItemResponse> items;
}
