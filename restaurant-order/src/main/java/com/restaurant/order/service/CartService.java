package com.restaurant.order.service;

import com.restaurant.order.entity.OrderCart;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {

    /**
     * 查询用户购物车列表
     */
    List<OrderCart> listByUserId(Long userId);

    /**
     * 添加商品到购物车
     */
    void add(OrderCart cart);

    /**
     * 更新购物车项数量
     */
    void updateQuantity(Long id, Integer quantity, Long userId);

    /**
     * 删除购物车单项
     */
    void delete(Long id, Long userId);

    /**
     * 清空用户购物车
     */
    void clear(Long userId);
}
