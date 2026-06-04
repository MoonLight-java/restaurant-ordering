package com.restaurant.common.enums;

import lombok.Getter;

@Getter
public enum DishStatusEnum {
    OFF_SALE(0, "下架"),
    ON_SALE(1, "上架");

    private final int code;
    private final String desc;

    DishStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
