package com.restaurant.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.restaurant.common.exception.BusinessException;
import com.restaurant.user.entity.UserAddress;
import com.restaurant.user.mapper.UserAddressMapper;
import com.restaurant.user.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Resource
    private UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddress> listByUserId(Long userId) {
        return userAddressMapper.selectList(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, userId)
                        .orderByDesc(UserAddress::getIsDefault)
                        .orderByDesc(UserAddress::getCreateTime));
    }

    @Override
    public void add(UserAddress addr) {
        userAddressMapper.insert(addr);
    }

    @Override
    public void update(UserAddress addr) {
        UserAddress existing = userAddressMapper.selectById(addr.getId());
        if (existing == null) {
            throw new BusinessException("地址不存在");
        }
        if (!existing.getUserId().equals(addr.getUserId())) {
            throw new BusinessException("无权修改此地址");
        }
        userAddressMapper.updateById(addr);
    }

    @Override
    public void delete(Long id, Long userId) {
        UserAddress existing = userAddressMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("地址不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此地址");
        }
        userAddressMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id, Long userId) {
        UserAddress existing = userAddressMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("地址不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此地址");
        }

        // Clear all default addresses for this user
        LambdaUpdateWrapper<UserAddress> clearWrapper = new LambdaUpdateWrapper<>();
        clearWrapper.eq(UserAddress::getUserId, userId)
                .set(UserAddress::getIsDefault, 0);
        userAddressMapper.update(null, clearWrapper);

        // Set this address as default
        UserAddress update = new UserAddress();
        update.setId(id);
        update.setIsDefault(1);
        userAddressMapper.updateById(update);
    }
}
