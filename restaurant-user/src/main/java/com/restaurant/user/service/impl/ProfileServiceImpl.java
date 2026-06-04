package com.restaurant.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.restaurant.common.exception.BusinessException;
import com.restaurant.user.entity.UserProfile;
import com.restaurant.user.mapper.UserProfileMapper;
import com.restaurant.user.service.ProfileService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Resource
    private UserProfileMapper userProfileMapper;

    @Override
    public UserProfile getByUserId(Long userId) {
        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
        if (profile == null) {
            // 首次访问自动创建空白资料，避免"用户资料不存在"错误
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setNickname("微信用户");
            profile.setAvatar("");
            userProfileMapper.insert(profile);
        }
        return profile;
    }

    @Override
    public void updateProfile(Long userId, UserProfile profile) {
        UserProfile existing = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
        if (existing == null) {
            // 如果还不存在，先创建再更新
            existing = new UserProfile();
            existing.setUserId(userId);
            existing.setNickname(profile.getNickname() != null ? profile.getNickname() : "微信用户");
            existing.setAvatar(profile.getAvatar() != null ? profile.getAvatar() : "");
            existing.setPhone(profile.getPhone());
            existing.setGender(profile.getGender());
            existing.setBirthday(profile.getBirthday());
            userProfileMapper.insert(existing);
            return;
        }

        LambdaUpdateWrapper<UserProfile> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserProfile::getUserId, userId);

        if (profile.getNickname() != null) {
            wrapper.set(UserProfile::getNickname, profile.getNickname());
        }
        if (profile.getAvatar() != null) {
            wrapper.set(UserProfile::getAvatar, profile.getAvatar());
        }
        if (profile.getPhone() != null) {
            wrapper.set(UserProfile::getPhone, profile.getPhone());
        }
        if (profile.getGender() != null) {
            wrapper.set(UserProfile::getGender, profile.getGender());
        }
        if (profile.getBirthday() != null) {
            wrapper.set(UserProfile::getBirthday, profile.getBirthday());
        }

        userProfileMapper.update(null, wrapper);
    }
}
