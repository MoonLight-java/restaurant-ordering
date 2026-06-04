package com.restaurant.auth.controller;

import com.restaurant.auth.dto.LoginResultDTO;
import com.restaurant.auth.dto.WechatLoginDTO;
import com.restaurant.auth.service.AuthService;
import com.restaurant.common.dto.response.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/login/wechat")
    public Result<LoginResultDTO> wechatLogin(@RequestBody WechatLoginDTO dto) {
        LoginResultDTO result = authService.loginByWechat(dto.getCode(), dto.getEncryptedData(), dto.getIv());
        return Result.ok(result);
    }

    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        return Result.ok();
    }

    @GetMapping("/current")
    public Result<LoginResultDTO> current(@RequestHeader("X-User-Id") Long userId) {
        LoginResultDTO result = authService.getCurrentUser(userId);
        return Result.ok(result);
    }
}
