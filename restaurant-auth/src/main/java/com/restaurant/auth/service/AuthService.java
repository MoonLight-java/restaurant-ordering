package com.restaurant.auth.service;

import com.restaurant.auth.dto.LoginResultDTO;

public interface AuthService {
    LoginResultDTO loginByWechat(String code, String encryptedData, String iv);
    LoginResultDTO loginByPassword(String username, String password);
    void logout(String token);
    LoginResultDTO getCurrentUser(Long userId);
    LoginResultDTO adminLogin(String username, String password);
}
