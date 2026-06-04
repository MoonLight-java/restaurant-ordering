package com.restaurant.common.enums;

import lombok.Getter;

@Getter
public enum PaymentStatusEnum {
    UNPAID(0, "未支付"),
    PAID(1, "已支付"),
    REFUNDING(2, "退款中"),
    REFUNDED(3, "已退款");

    private final int code;
    private final String desc;

    PaymentStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
