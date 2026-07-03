package com.aimedical.modules.window.api;

import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.auth.CurrentUser;
import com.aimedical.modules.window.dto.PaymentRecordResponse;
import com.aimedical.modules.window.dto.ReconcileRequest;
import com.aimedical.modules.window.dto.ReconcileResponse;
import com.aimedical.modules.window.service.ReconciliationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 线下窗口对账控制器。
 *
 * <p>窗口工作人员（ADMIN 角色）对已支付缴费记录进行批量日终对账。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/window/reconciliation")
@PreAuthorize("hasRole('ADMIN')")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;
    private final CurrentUser currentUser;

    public ReconciliationController(ReconciliationService reconciliationService, CurrentUser currentUser) {
        this.reconciliationService = reconciliationService;
        this.currentUser = currentUser;
    }

    /**
     * 批量对账（PAID -> RECONCILED）。
     */
    @PostMapping("/reconcile")
    public Result<ReconcileResponse> reconcile(@Valid @RequestBody ReconcileRequest request) {
        return reconciliationService.reconcile(request, currentUser.getUserId(), currentUser.getUsername());
    }

    /**
     * 按对账批次号查询该批次全部缴费记录。
     */
    @GetMapping("/batch/{batchNo}")
    public Result<List<PaymentRecordResponse>> listByBatchNo(@PathVariable String batchNo) {
        return reconciliationService.listByBatchNo(batchNo);
    }
}
