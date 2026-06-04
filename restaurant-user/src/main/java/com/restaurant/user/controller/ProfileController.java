package com.restaurant.user.controller;

import com.restaurant.common.dto.response.Result;
import com.restaurant.common.utils.MinioUtil;
import com.restaurant.user.entity.UserProfile;
import com.restaurant.user.service.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class ProfileController {

    @Resource
    private ProfileService profileService;

    @Resource
    private MinioUtil minioUtil;

    @GetMapping("/profile")
    public Result<UserProfile> getProfile(@RequestHeader("X-User-Id") Long userId) {
        UserProfile profile = profileService.getByUserId(userId);
        return Result.ok(profile);
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestHeader("X-User-Id") Long userId,
                                   @RequestBody UserProfile profile) {
        profileService.updateProfile(userId, profile);
        return Result.ok();
    }

    @PostMapping("/profile/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestHeader("X-User-Id") Long userId,
                                                     @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }

        try {
            String avatarUrl = minioUtil.upload(file, "avatars");
            log.info("Avatar uploaded for userId={}: {}", userId, avatarUrl);

            UserProfile profile = new UserProfile();
            profile.setAvatar(avatarUrl);
            profileService.updateProfile(userId, profile);

            Map<String, String> data = new HashMap<>();
            data.put("url", avatarUrl);
            return Result.ok(data);
        } catch (Exception e) {
            log.error("Avatar upload failed for userId={}", userId, e);
            return Result.fail("头像上传失败: " + e.getMessage());
        }
    }
}
