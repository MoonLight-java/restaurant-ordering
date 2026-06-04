package com.restaurant.menu.service;

import com.restaurant.menu.entity.MenuDishSpecGroup;
import com.restaurant.menu.entity.MenuDishSpecItem;

public interface SpecService {

    /**
     * 新增规格组
     */
    void addGroup(MenuDishSpecGroup group);

    /**
     * 修改规格组
     */
    void updateGroup(MenuDishSpecGroup group);

    /**
     * 删除规格组
     */
    void deleteGroup(Long id);

    /**
     * 新增规格项
     */
    void addItem(MenuDishSpecItem item);

    /**
     * 修改规格项
     */
    void updateItem(MenuDishSpecItem item);

    /**
     * 删除规格项
     */
    void deleteItem(Long id);
}
