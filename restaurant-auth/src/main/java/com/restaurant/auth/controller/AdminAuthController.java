package com.restaurant.auth.controller;

import com.restaurant.auth.dto.LoginResultDTO;
import com.restaurant.auth.dto.PasswordLoginDTO;
import com.restaurant.auth.service.AuthService;
import com.restaurant.common.dto.response.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/login")
    public Result<LoginResultDTO> login(@RequestBody PasswordLoginDTO dto) {
        LoginResultDTO result = authService.adminLogin(dto.getUsername(), dto.getPassword());
        return Result.ok(result);
    }

    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        return Result.ok();
    }
}
