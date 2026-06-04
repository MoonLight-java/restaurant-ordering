package com.restaurant.menu.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.restaurant.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("menu_dish")
public class MenuDish extends BaseEntity {

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 菜品名称
     */
    private String name;

    /**
     * 菜品描述
     */
    private String description;

    /**
     * 菜品图片
     */
    private String image;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 状态: 0=下架, 1=上架
     */
    private Integer status;

    /**
     * 销量
     */
    private Integer salesCount;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 规格组列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<MenuDishSpecGroup> specGroups;
}
