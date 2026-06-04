package com.restaurant.menu.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.restaurant.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("menu_dish_spec_group")
public class MenuDishSpecGroup extends BaseEntity {

    /**
     * 所属菜品ID
     */
    private Long dishId;

    /**
     * 规格组名称
     */
    private String name;

    /**
     * 选择类型: 0=单选, 1=多选
     */
    private Integer selectType;

    /**
     * 是否必选: 0=否, 1=是
     */
    private Integer isRequired;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 规格项列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<MenuDishSpecItem> items;
}
