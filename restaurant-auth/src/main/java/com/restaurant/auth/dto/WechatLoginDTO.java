package com.restaurant.auth.dto;

import lombok.Data;

@Data
public class WechatLoginDTO {
    private String code;
    private String encryptedData;
    private String iv;
}
