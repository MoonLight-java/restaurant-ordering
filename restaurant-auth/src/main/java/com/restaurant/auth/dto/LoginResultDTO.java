package com.restaurant.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResultDTO {
    private String token;
    private Long userId;
    private String nickname;
    private String avatar;
    private String phone;
    private String role;
}
