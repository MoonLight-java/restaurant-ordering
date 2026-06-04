package com.restaurant.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.utils.RedisUtils;
import com.restaurant.menu.entity.MenuCategory;
import com.restaurant.menu.mapper.MenuCategoryMapper;
import com.restaurant.menu.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final MenuCategoryMapper categoryMapper;
    private final RedisUtils redisUtils;

    private static final String CACHE_KEY = "menu:categories";
    private static final long CACHE_TTL = 30;

    @Override
    public List<MenuCategory> listVisible() {
        // Check Redis cache first
        Object cached = redisUtils.get(CACHE_KEY);
        if (cached instanceof List) {
            @SuppressWarnings("unchecked")
            List<MenuCategory> cachedList = (List<MenuCategory>) cached;
            if (!CollectionUtils.isEmpty(cachedList)) {
                log.debug("Hit cache for visible categories");
                return cachedList;
            }
        }

        // Query from DB
        LambdaQueryWrapper<MenuCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuCategory::getStatus, 1)
                .eq(MenuCategory::getIsDeleted, 0)
                .orderByAsc(MenuCategory::getSortOrder);
        List<MenuCategory> list = categoryMapper.selectList(wrapper);

        if (CollectionUtils.isEmpty(list)) {
            list = Collections.emptyList();
        }

        // Cache with 30 minute TTL
        redisUtils.set(CACHE_KEY, list, CACHE_TTL, TimeUnit.MINUTES);
        log.debug("Cached visible categories, count: {}", list.size());
        return list;
    }

    @Override
    public List<MenuCategory> listAll() {
        LambdaQueryWrapper<MenuCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(MenuCategory::getSortOrder)
                .orderByDesc(MenuCategory::getStatus);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(MenuCategory category) {
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getType() == null) {
            category.setType(0);
        }
        int rows = categoryMapper.insert(category);
        if (rows <= 0) {
            throw new BusinessException("新增分类失败");
        }
        deleteCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MenuCategory category) {
        MenuCategory existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }
        int rows = categoryMapper.updateById(category);
        if (rows <= 0) {
            throw new BusinessException("更新分类失败");
        }
        deleteCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        MenuCategory existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }
        int rows = categoryMapper.deleteById(id);
        if (rows <= 0) {
            throw new BusinessException("删除分类失败");
        }
        deleteCache();
    }

    private void deleteCache() {
        redisUtils.delete(CACHE_KEY);
        log.debug("Deleted cache: {}", CACHE_KEY);
    }
}
