package com.aimedical.modules.window.api;

import com.aimedical.common.result.Result;
import com.aimedical.modules.commonmodule.auth.CurrentUser;
import com.aimedical.modules.window.dto.PayRequest;
import com.aimedical.modules.window.dto.PaymentCreateRequest;
import com.aimedical.modules.window.dto.PaymentQueryRequest;
import com.aimedical.modules.window.dto.PaymentRecordResponse;
import com.aimedical.modules.window.dto.RefundRequest;
import com.aimedical.modules.window.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 线下窗口缴费控制器。
 *
 * <p>窗口工作人员（ADMIN 角色）进行缴费创建、收款、退费与查询。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/window/payment")
@PreAuthorize("hasRole('ADMIN')")
public class PaymentController {

    private final PaymentService paymentService;
    private final CurrentUser currentUser;

    public PaymentController(PaymentService paymentService, CurrentUser currentUser) {
        this.paymentService = paymentService;
        this.currentUser = currentUser;
    }

    /**
     * 创建缴费记录（状态 PENDING）。
     */
    @PostMapping
    public Result<PaymentRecordResponse> create(@Valid @RequestBody PaymentCreateRequest request) {
        return paymentService.create(request, currentUser.getUserId(), currentUser.getUsername());
    }

    /**
     * 支付（PENDING -> PAID）。
     */
    @PostMapping("/{id}/pay")
    public Result<PaymentRecordResponse> pay(@PathVariable Long id,
                                             @Valid @RequestBody PayRequest request) {
        return paymentService.pay(id, request);
    }

    /**
     * 退费（PAID -> REFUNDED）。
     */
    @PostMapping("/{id}/refund")
    public Result<PaymentRecordResponse> refund(@PathVariable Long id,
                                                @Valid @RequestBody RefundRequest request) {
        return paymentService.refund(id, request);
    }

    /**
     * 查询缴费详情（含明细）。
     */
    @GetMapping("/{id}")
    public Result<PaymentRecordResponse> getById(@PathVariable Long id) {
        return paymentService.getById(id);
    }

    /**
     * 分页查询缴费记录（按条件组合）。
     */
    @GetMapping
    public Result<Page<PaymentRecordResponse>> query(PaymentQueryRequest request) {
        return paymentService.query(request);
    }

    /**
     * 按患者查询缴费记录列表。
     */
    @GetMapping("/patient/{patientId}")
    public Result<List<PaymentRecordResponse>> queryByPatient(@PathVariable Long patientId) {
        return paymentService.queryByPatient(patientId);
    }
}
