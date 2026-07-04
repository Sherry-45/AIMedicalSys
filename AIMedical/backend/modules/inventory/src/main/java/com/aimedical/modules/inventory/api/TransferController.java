package com.aimedical.modules.inventory.api;

import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.auth.CurrentUser;
import com.aimedical.modules.inventory.dto.request.TransferApproveRequest;
import com.aimedical.modules.inventory.dto.request.TransferCreateRequest;
import com.aimedical.modules.inventory.dto.request.TransferQueryRequest;
import com.aimedical.modules.inventory.dto.response.TransferOrderResponse;
import com.aimedical.modules.inventory.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 调拨控制器。
 *
 * <p>调拨管理（D2）- 调拨单全流程管理。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/inventory/transfer")
@PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
public class TransferController {

    private final TransferService transferService;
    private final CurrentUser currentUser;

    public TransferController(TransferService transferService, CurrentUser currentUser) {
        this.transferService = transferService;
        this.currentUser = currentUser;
    }

    /**
     * 创建调拨单。
     */
    @PostMapping
    public Result<TransferOrderResponse> create(@Valid @RequestBody TransferCreateRequest request) {
        Long applicantId = currentUser.getUserId();
        String applicantName = currentUser.getUsername();
        return transferService.create(request, applicantId, applicantName);
    }

    /**
     * 提交审批（DRAFT -> PENDING_APPROVAL）。
     */
    @PostMapping("/{id}/submit")
    public Result<TransferOrderResponse> submit(@PathVariable Long id) {
        return transferService.submit(id);
    }

    /**
     * 审批调拨单（PENDING_APPROVAL -> APPROVED 或 REJECTED）。
     */
    @PostMapping("/{id}/approve")
    public Result<TransferOrderResponse> approve(@PathVariable Long id,
                                                 @Valid @RequestBody TransferApproveRequest request) {
        Long approverId = currentUser.getUserId();
        String approverName = currentUser.getUsername();
        return transferService.approve(id, request, approverId, approverName);
    }

    /**
     * 发货（APPROVED -> IN_TRANSIT），扣减调出方库存。
     */
    @PostMapping("/{id}/ship")
    public Result<TransferOrderResponse> ship(@PathVariable Long id) {
        return transferService.ship(id);
    }

    /**
     * 接收（IN_TRANSIT -> RECEIVED），增加调入方库存。
     */
    @PostMapping("/{id}/receive")
    public Result<TransferOrderResponse> receive(@PathVariable Long id) {
        return transferService.receive(id);
    }

    /**
     * 取消调拨单（DRAFT/PENDING_APPROVAL -> CANCELLED）。
     */
    @PostMapping("/{id}/cancel")
    public Result<TransferOrderResponse> cancel(@PathVariable Long id) {
        return transferService.cancel(id);
    }

    /**
     * 查询调拨单详情。
     */
    @GetMapping("/{id}")
    public Result<TransferOrderResponse> getById(@PathVariable Long id) {
        return transferService.getById(id);
    }

    /**
     * 分页查询调拨单。
     */
    @GetMapping
    public Result<Page<TransferOrderResponse>> query(TransferQueryRequest request) {
        return transferService.query(request);
    }
}
