package com.restaurant.menu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.menu.entity.MenuDish;

public interface DishService {

    /**
     * 分页查询菜品列表
     */
    Page<MenuDish> pageByCategory(Long categoryId, String keyword, int page, int size);

    /**
     * 查询菜品详情（含规格组和规格项）
     */
    MenuDish getDetail(Long id);

    /**
     * 新增菜品
     */
    void add(MenuDish dish);

    /**
     * 修改菜品
     */
    void update(MenuDish dish);

    /**
     * 删除菜品
     */
    void delete(Long id);

    /**
     * 更新菜品状态
     */
    void updateStatus(Long id, Integer status);
}
