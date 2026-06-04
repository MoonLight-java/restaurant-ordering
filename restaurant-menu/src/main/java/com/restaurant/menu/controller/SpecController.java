package com.restaurant.menu.controller;

import com.restaurant.common.dto.response.Result;
import com.restaurant.menu.entity.MenuDishSpecGroup;
import com.restaurant.menu.entity.MenuDishSpecItem;
import com.restaurant.menu.service.SpecService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SpecController {

    private final SpecService specService;

    /**
     * 管理端：新增规格组
     */
    @PostMapping("/api/admin/dishes/{dishId}/specs")
    public Result<Void> addGroup(@PathVariable Long dishId, @RequestBody MenuDishSpecGroup group) {
        group.setDishId(dishId);
        specService.addGroup(group);
        return Result.ok();
    }

    /**
     * 管理端：修改规格组
     */
    @PutMapping("/api/admin/specs/{id}")
    public Result<Void> updateGroup(@PathVariable Long id, @RequestBody MenuDishSpecGroup group) {
        group.setId(id);
        specService.updateGroup(group);
        return Result.ok();
    }

    /**
     * 管理端：删除规格组
     */
    @DeleteMapping("/api/admin/specs/{id}")
    public Result<Void> deleteGroup(@PathVariable Long id) {
        specService.deleteGroup(id);
        return Result.ok();
    }

    /**
     * 管理端：新增规格项
     */
    @PostMapping("/api/admin/specs/{groupId}/items")
    public Result<Void> addItem(@PathVariable Long groupId, @RequestBody MenuDishSpecItem item) {
        item.setGroupId(groupId);
        specService.addItem(item);
        return Result.ok();
    }

    /**
     * 管理端：修改规格项
     */
    @PutMapping("/api/admin/spec-items/{id}")
    public Result<Void> updateItem(@PathVariable Long id, @RequestBody MenuDishSpecItem item) {
        item.setId(id);
        specService.updateItem(item);
        return Result.ok();
    }

    /**
     * 管理端：删除规格项
     */
    @DeleteMapping("/api/admin/spec-items/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        specService.deleteItem(id);
        return Result.ok();
    }
}
