package com.restaurant.user.service;

import com.restaurant.user.entity.UserProfile;

public interface ProfileService {

    UserProfile getByUserId(Long userId);

    void updateProfile(Long userId, UserProfile profile);
}
