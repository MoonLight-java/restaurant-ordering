package com.restaurant.order.service;

import com.restaurant.order.dto.CreateOrderDTO;
import com.restaurant.order.dto.OrderDetailDTO;
import com.restaurant.order.entity.OrderMain;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     */
    CreateOrderResult createOrder(Long userId, CreateOrderDTO dto);

    /**
     * 用户订单分页查询
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<OrderMain> pageByUserId(
            Long userId, Integer status, int page, int size);

    /**
     * 查询订单详情（含明细）
     */
    OrderDetailDTO getOrderDetail(Long orderId, Long userId);

    /**
     * 用户取消订单
     */
    void cancelOrder(Long orderId, Long userId, String reason);

    /**
     * 管理员订单分页查询
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<OrderMain> adminPageOrders(
            Integer status, String orderNo, int page, int size);

    /**
     * 管理员查询订单详情
     */
    OrderDetailDTO adminGetOrderDetail(Long orderId);

    /**
     * 管理员变更订单状态
     */
    void adminChangeStatus(Long orderId, Integer newStatus, String cancelReason);

    /**
     * 创建订单结果
     */
    @Data
    @AllArgsConstructor
    class CreateOrderResult {
        private Long orderId;
        private String orderNo;
        private BigDecimal payAmount;
    }
}
