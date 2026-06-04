package com.restaurant.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.auth.entity.AuthUser;
import com.restaurant.auth.mapper.AuthUserMapper;
import com.restaurant.common.dto.response.Result;
import com.restaurant.common.exception.BusinessException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    @Resource
    private AuthUserMapper authUserMapper;

    @GetMapping("/users")
    public Result<Map<String, Object>> listUsers(@RequestHeader("X-User-Role") String role,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        if (!"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
            throw new BusinessException(403, "无管理员权限");
        }

        Page<AuthUser> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AuthUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(AuthUser::getUsername, keyword)
                .or()
                .like(AuthUser::getNickname, keyword)
                .or()
                .like(AuthUser::getPhone, keyword));
        }
        wrapper.orderByDesc(AuthUser::getCreateTime);

        Page<AuthUser> resultPage = authUserMapper.selectPage(pageParam, wrapper);

        List<Map<String, Object>> records = resultPage.getRecords().stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("nickname", user.getNickname());
            map.put("avatar", user.getAvatar());
            map.put("phone", user.getPhone());
            map.put("role", user.getRole());
            map.put("status", user.getStatus());
            map.put("lastLoginTime", user.getLastLoginTime());
            map.put("createTime", user.getCreateTime());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("total", resultPage.getTotal());
        result.put("records", records);

        return Result.ok(result);
    }

    @PutMapping("/users/{id}/status")
    public Result<?> updateStatus(@RequestHeader("X-User-Role") String role,
                                   @PathVariable Long id,
                                   @RequestBody Map<String, Integer> body) {
        if (!"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
            throw new BusinessException(403, "无管理员权限");
        }

        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("无效的状态值");
        }

        AuthUser user = authUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setStatus(status);
        authUserMapper.updateById(user);

        return Result.ok();
    }
}
