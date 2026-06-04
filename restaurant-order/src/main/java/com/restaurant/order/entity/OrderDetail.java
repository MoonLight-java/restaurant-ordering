package com.restaurant.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_detail")
public class OrderDetail {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 订单ID（关联 order_main.id） */
    private Long orderId;

    /** 订单号 */
    private String orderNo;

    /** 菜品ID */
    private Long dishId;

    /** 菜品名称 */
    private String dishName;

    /** 菜品图片 */
    private String dishImage;

    /** 规格JSON（TEXT字段，存储下单时的规格快照） */
    private String specJson;

    /** 数量 */
    private Integer quantity;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 小计（unitPrice * quantity） */
    private BigDecimal subTotal;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
