package com.restaurant.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.exception.BusinessException;
import com.restaurant.menu.entity.MenuDish;
import com.restaurant.menu.entity.MenuDishSpecGroup;
import com.restaurant.menu.entity.MenuDishSpecItem;
import com.restaurant.menu.mapper.MenuDishMapper;
import com.restaurant.menu.mapper.MenuDishSpecGroupMapper;
import com.restaurant.menu.mapper.MenuDishSpecItemMapper;
import com.restaurant.menu.service.DishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final MenuDishMapper dishMapper;
    private final MenuDishSpecGroupMapper specGroupMapper;
    private final MenuDishSpecItemMapper specItemMapper;

    @Override
    public Page<MenuDish> pageByCategory(Long categoryId, String keyword, int page, int size) {
        Page<MenuDish> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<MenuDish> wrapper = new LambdaQueryWrapper<>();

        if (categoryId != null) {
            wrapper.eq(MenuDish::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(MenuDish::getName, keyword);
        }
        wrapper.eq(MenuDish::getStatus, 1)
               .orderByAsc(MenuDish::getSortOrder);

        return dishMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public MenuDish getDetail(Long id) {
        MenuDish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }

        // Query spec groups
        LambdaQueryWrapper<MenuDishSpecGroup> groupWrapper = new LambdaQueryWrapper<>();
        groupWrapper.eq(MenuDishSpecGroup::getDishId, id)
                    .orderByAsc(MenuDishSpecGroup::getSortOrder);
        List<MenuDishSpecGroup> groups = specGroupMapper.selectList(groupWrapper);

        // For each group, query items
        for (MenuDishSpecGroup group : groups) {
            LambdaQueryWrapper<MenuDishSpecItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(MenuDishSpecItem::getGroupId, group.getId())
                       .orderByAsc(MenuDishSpecItem::getSortOrder);
            List<MenuDishSpecItem> items = specItemMapper.selectList(itemWrapper);
            group.setItems(items);
        }

        dish.setSpecGroups(groups);
        return dish;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(MenuDish dish) {
        if (dish.getStatus() == null) {
            dish.setStatus(1);
        }
        if (dish.getSortOrder() == null) {
            dish.setSortOrder(0);
        }
        if (dish.getSalesCount() == null) {
            dish.setSalesCount(0);
        }
        int rows = dishMapper.insert(dish);
        if (rows <= 0) {
            throw new BusinessException("新增菜品失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MenuDish dish) {
        MenuDish existing = dishMapper.selectById(dish.getId());
        if (existing == null) {
            throw new BusinessException("菜品不存在");
        }
        int rows = dishMapper.updateById(dish);
        if (rows <= 0) {
            throw new BusinessException("更新菜品失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        MenuDish existing = dishMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("菜品不存在");
        }
        int rows = dishMapper.deleteById(id);
        if (rows <= 0) {
            throw new BusinessException("删除菜品失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        MenuDish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        dish.setStatus(status);
        dishMapper.updateById(dish);
    }
}
