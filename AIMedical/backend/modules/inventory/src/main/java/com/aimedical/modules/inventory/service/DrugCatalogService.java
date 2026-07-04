package com.aimedical.modules.inventory.service;

import com.aimedical.common.result.Result;
import com.aimedical.modules.inventory.dto.request.DrugCatalogCreateRequest;
import com.aimedical.modules.inventory.dto.request.DrugCatalogQueryRequest;
import com.aimedical.modules.inventory.dto.response.DrugCatalogResponse;
import org.springframework.data.domain.Page;

/**
 * 药品目录服务。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public interface DrugCatalogService {

    Result<DrugCatalogResponse> create(DrugCatalogCreateRequest request);

    Result<DrugCatalogResponse> update(Long id, DrugCatalogCreateRequest request);

    Result<Void> delete(Long id);

    Result<DrugCatalogResponse> getById(Long id);

    Result<DrugCatalogResponse> getByDrugCode(String drugCode);

    Result<Page<DrugCatalogResponse>> query(DrugCatalogQueryRequest request);

    Result<DrugCatalogResponse> toggleEnabled(Long id);
}
