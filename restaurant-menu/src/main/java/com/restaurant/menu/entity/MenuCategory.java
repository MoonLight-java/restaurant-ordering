package com.restaurant.menu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.restaurant.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("menu_category")
public class MenuCategory extends BaseEntity {

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类类型: 0=热菜, 1=套餐, 2=饮品
     */
    private Integer type;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态: 0=隐藏, 1=显示
     */
    private Integer status;
}
