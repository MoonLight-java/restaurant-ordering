package com.restaurant.menu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.dto.response.Result;
import com.restaurant.common.utils.MinioUtil;
import com.restaurant.menu.entity.MenuDish;
import com.restaurant.menu.service.DishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;
    private final MinioUtil minioUtil;

    /**
     * 公开接口：分页查询菜品
     */
    @GetMapping("/api/menu/dishes")
    public Result<Page<MenuDish>> page(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MenuDish> result = dishService.pageByCategory(categoryId, keyword, page, size);
        return Result.ok(result);
    }

    /**
     * 公开接口：查询菜品详情
     */
    @GetMapping("/api/menu/dishes/{id}")
    public Result<MenuDish> getDetail(@PathVariable Long id) {
        MenuDish dish = dishService.getDetail(id);
        return Result.ok(dish);
    }

    /**
     * 管理端：新增菜品
     */
    @PostMapping("/api/admin/dishes")
    public Result<Void> add(@RequestBody MenuDish dish) {
        dishService.add(dish);
        return Result.ok();
    }

    /**
     * 管理端：修改菜品
     */
    @PutMapping("/api/admin/dishes/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody MenuDish dish) {
        dish.setId(id);
        dishService.update(dish);
        return Result.ok();
    }

    /**
     * 管理端：删除菜品
     */
    @DeleteMapping("/api/admin/dishes/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dishService.delete(id);
        return Result.ok();
    }

    /**
     * 管理端：更新菜品状态
     */
    @PutMapping("/api/admin/dishes/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        dishService.updateStatus(id, status);
        return Result.ok();
    }

    /**
     * 管理端：上传菜品图片
     */
    @PostMapping("/api/admin/dishes/{id}/image")
    public Result<Map<String, String>> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }

        try {
            String imageUrl = minioUtil.upload(file, "dishes");
            log.info("Dish image uploaded for dishId={}: {}", id, imageUrl);

            Map<String, String> data = new HashMap<>();
            data.put("url", imageUrl);
            return Result.ok(data);
        } catch (Exception e) {
            log.error("File upload failed for dishId={}", id, e);
            return Result.fail("文件上传失败: " + e.getMessage());
        }
    }
}
