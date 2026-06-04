package com.restaurant.menu.controller;

import com.restaurant.common.dto.response.Result;
import com.restaurant.menu.entity.MenuCategory;
import com.restaurant.menu.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 公开接口：查询可见分类列表
     */
    @GetMapping("/api/menu/categories")
    public Result<List<MenuCategory>> listVisible() {
        List<MenuCategory> list = categoryService.listVisible();
        return Result.ok(list);
    }

    /**
     * 管理端：查询全部分类
     */
    @GetMapping("/api/admin/categories")
    public Result<List<MenuCategory>> listAll() {
        List<MenuCategory> list = categoryService.listAll();
        return Result.ok(list);
    }

    /**
     * 管理端：新增分类
     */
    @PostMapping("/api/admin/categories")
    public Result<Void> add(@RequestBody MenuCategory category) {
        categoryService.add(category);
        return Result.ok();
    }

    /**
     * 管理端：修改分类
     */
    @PutMapping("/api/admin/categories/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody MenuCategory category) {
        category.setId(id);
        categoryService.update(category);
        return Result.ok();
    }

    /**
     * 管理端：删除分类
     */
    @DeleteMapping("/api/admin/categories/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }
}
