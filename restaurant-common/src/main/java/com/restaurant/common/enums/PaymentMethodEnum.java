package com.restaurant.common.enums;

import lombok.Getter;

@Getter
public enum PaymentMethodEnum {
    WECHAT_PAY(0, "微信支付"),
    MOCK(1, "模拟支付");

    private final int code;
    private final String desc;

    PaymentMethodEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
