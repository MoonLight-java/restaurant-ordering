package com.restaurant.common.enums;

import lombok.Getter;

@Getter
public enum OrderStatusEnum {
    PENDING(0, "待支付"),
    CONFIRMED(1, "已确认"),
    PREPARING(2, "制作中"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderStatusEnum fromCode(int code) {
        for (OrderStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}
