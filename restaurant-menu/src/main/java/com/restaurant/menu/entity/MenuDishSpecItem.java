package com.restaurant.menu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.restaurant.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("menu_dish_spec_item")
public class MenuDishSpecItem extends BaseEntity {

    /**
     * 所属规格组ID
     */
    private Long groupId;

    /**
     * 所属菜品ID
     */
    private Long dishId;

    /**
     * 规格项名称
     */
    private String name;

    /**
     * 价格调整（增减）
     */
    private BigDecimal priceAdjust;

    /**
     * 排序
     */
    private Integer sortOrder;
}
