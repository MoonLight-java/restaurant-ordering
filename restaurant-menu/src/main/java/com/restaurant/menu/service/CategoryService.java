package com.restaurant.menu.service;

import com.restaurant.menu.entity.MenuCategory;

import java.util.List;

public interface CategoryService {

    /**
     * 查询可见分类列表
     */
    List<MenuCategory> listVisible();

    /**
     * 查询全部分类列表
     */
    List<MenuCategory> listAll();

    /**
     * 新增分类
     */
    void add(MenuCategory category);

    /**
     * 修改分类
     */
    void update(MenuCategory category);

    /**
     * 删除分类
     */
    void delete(Long id);
}
