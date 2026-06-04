package com.restaurant.common.enums;

import lombok.Getter;

@Getter
public enum UserRoleEnum {
    USER(0, "普通用户"),
    ADMIN(1, "管理员"),
    SUPER_ADMIN(2, "超级管理员");

    private final int code;
    private final String desc;

    UserRoleEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
