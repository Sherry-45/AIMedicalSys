package com.aimedical.modules.inventory;

import com.aimedical.common.exception.ErrorCode;

/**
 * 库存模块错误码。
 *
 * <p>覆盖药库基础管理（D1）与调拨管理（D2）的业务异常场景。
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
public enum InventoryErrorCode implements ErrorCode {

    DRUG_NOT_FOUND("DRUG_NOT_FOUND", "药品目录不存在"),
    DRUG_CODE_DUPLICATE("DRUG_CODE_DUPLICATE", "药品编码已存在"),
    DRUG_DISABLED("DRUG_DISABLED", "药品已停用"),
    STOCK_NOT_FOUND("STOCK_NOT_FOUND", "库存批次不存在"),
    STOCK_INSUFFICIENT("STOCK_INSUFFICIENT", "库存数量不足"),
    STOCK_BATCH_NOT_FOUND("STOCK_BATCH_NOT_FOUND", "该药品批次库存不存在"),
    STOCKTAKING_NOT_FOUND("STOCKTAKING_NOT_FOUND", "盘点单不存在"),
    STOCKTAKING_INVALID_STATE("STOCKTAKING_INVALID_STATE", "盘点单状态不允许该操作"),
    STOCKTAKING_ITEM_EMPTY("STOCKTAKING_ITEM_EMPTY", "盘点单至少需要包含一个明细"),
    STOCKTAKING_ACTUAL_EMPTY("STOCKTAKING_ACTUAL_EMPTY", "提交实际数量时明细不能为空"),
    TRANSFER_NOT_FOUND("TRANSFER_NOT_FOUND", "调拨单不存在"),
    TRANSFER_INVALID_STATE("TRANSFER_INVALID_STATE", "调拨单状态不允许该操作"),
    TRANSFER_ITEM_EMPTY("TRANSFER_ITEM_EMPTY", "调拨单至少需要包含一个明细"),
    TRANSFER_DEPT_SAME("TRANSFER_DEPT_SAME", "调出与调入部门不能相同"),
    TRANSFER_APPROVE_REJECT_REASON_REQUIRED("TRANSFER_APPROVE_REJECT_REASON_REQUIRED", "驳回时必须填写驳回原因");

    private final String code;
    private final String message;

    InventoryErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
