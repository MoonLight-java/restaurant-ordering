package com.restaurant.user.service;

import com.restaurant.user.entity.UserAddress;

import java.util.List;

public interface AddressService {

    List<UserAddress> listByUserId(Long userId);

    void add(UserAddress addr);

    void update(UserAddress addr);

    void delete(Long id, Long userId);

    void setDefault(Long id, Long userId);
}
