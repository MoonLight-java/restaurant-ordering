package com.restaurant.menu.service.impl;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.menu.entity.MenuDishSpecGroup;
import com.restaurant.menu.entity.MenuDishSpecItem;
import com.restaurant.menu.mapper.MenuDishSpecGroupMapper;
import com.restaurant.menu.mapper.MenuDishSpecItemMapper;
import com.restaurant.menu.service.SpecService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecServiceImpl implements SpecService {

    private final MenuDishSpecGroupMapper specGroupMapper;
    private final MenuDishSpecItemMapper specItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addGroup(MenuDishSpecGroup group) {
        if (group.getSelectType() == null) {
            group.setSelectType(0);
        }
        if (group.getIsRequired() == null) {
            group.setIsRequired(0);
        }
        if (group.getSortOrder() == null) {
            group.setSortOrder(0);
        }
        int rows = specGroupMapper.insert(group);
        if (rows <= 0) {
            throw new BusinessException("新增规格组失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroup(MenuDishSpecGroup group) {
        MenuDishSpecGroup existing = specGroupMapper.selectById(group.getId());
        if (existing == null) {
            throw new BusinessException("规格组不存在");
        }
        int rows = specGroupMapper.updateById(group);
        if (rows <= 0) {
            throw new BusinessException("更新规格组失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long id) {
        MenuDishSpecGroup existing = specGroupMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("规格组不存在");
        }
        int rows = specGroupMapper.deleteById(id);
        if (rows <= 0) {
            throw new BusinessException("删除规格组失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addItem(MenuDishSpecItem item) {
        if (item.getPriceAdjust() == null) {
            item.setPriceAdjust(java.math.BigDecimal.ZERO);
        }
        if (item.getSortOrder() == null) {
            item.setSortOrder(0);
        }
        int rows = specItemMapper.insert(item);
        if (rows <= 0) {
            throw new BusinessException("新增规格项失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItem(MenuDishSpecItem item) {
        MenuDishSpecItem existing = specItemMapper.selectById(item.getId());
        if (existing == null) {
            throw new BusinessException("规格项不存在");
        }
        int rows = specItemMapper.updateById(item);
        if (rows <= 0) {
            throw new BusinessException("更新规格项失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long id) {
        MenuDishSpecItem existing = specItemMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("规格项不存在");
        }
        int rows = specItemMapper.deleteById(id);
        if (rows <= 0) {
            throw new BusinessException("删除规格项失败");
        }
    }
}
