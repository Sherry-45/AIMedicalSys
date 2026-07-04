package com.aimedical.modules.window.entity;

import com.aimedical.common.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 缴费记录状态枚举。
 *
 * <p>状态机：
 * <ul>
 *   <li>PENDING(待支付) -> PAID(已支付)：窗口收款</li>
 *   <li>PENDING(待支付) -> CANCELLED(已取消)：退号同步取消待支付缴费</li>
 *   <li>PAID(已支付) -> REFUNDED(已退款)：窗口退费</li>
 *   <li>PAID(已支付) -> RECONCILED(已对账)：日终对账</li>
 * </ul>
 *
 * @author AIMedical Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum PaymentStatus implements BaseEnum {

    PENDING("PENDING", "待支付"),
    PAID("PAID", "已支付"),
    REFUNDED("REFUNDED", "已退款"),
    RECONCILED("RECONCILED", "已对账"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;
}
