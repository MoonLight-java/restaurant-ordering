package com.restaurant.order.dto;

import com.restaurant.order.entity.OrderDetail;
import com.restaurant.order.entity.OrderMain;
import lombok.Data;

import java.util.List;

@Data
public class OrderDetailDTO {

    /** 订单主信息 */
    private OrderMain order;

    /** 订单明细列表 */
    private List<OrderDetail> items;

    /** 支付状态描述 */
    private String paymentStatus;
}
