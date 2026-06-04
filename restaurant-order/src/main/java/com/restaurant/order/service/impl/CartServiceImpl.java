package com.restaurant.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.order.entity.OrderCart;
import com.restaurant.order.mapper.OrderCartMapper;
import com.restaurant.order.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final OrderCartMapper orderCartMapper;

    @Override
    public List<OrderCart> listByUserId(Long userId) {
        LambdaQueryWrapper<OrderCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderCart::getUserId, userId)
                .orderByDesc(OrderCart::getCreateTime);
        return orderCartMapper.selectList(wrapper);
    }

    @Override
    public void add(OrderCart cart) {
        if (cart.getUserId() == null) {
            log.warn("Cart item has null userId, cannot add");
            return;
        }
        if (cart.getQuantity() == null || cart.getQuantity() <= 0) {
            cart.setQuantity(1);
        }
        cart.setCreateTime(LocalDateTime.now());
        cart.setUpdateTime(LocalDateTime.now());
        orderCartMapper.insert(cart);
        log.info("Cart item added: userId={}, dishId={}, quantity={}", cart.getUserId(), cart.getDishId(), cart.getQuantity());
    }

    @Override
    public void updateQuantity(Long id, Integer quantity, Long userId) {
        OrderCart cart = orderCartMapper.selectById(id);
        if (cart == null) {
            throw new RuntimeException("购物车项不存在");
        }
        if (!cart.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此购物车项");
        }
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("数量必须大于0");
        }
        cart.setQuantity(quantity);
        cart.setUpdateTime(LocalDateTime.now());
        orderCartMapper.updateById(cart);
        log.info("Cart item quantity updated: id={}, quantity={}", id, quantity);
    }

    @Override
    public void delete(Long id, Long userId) {
        OrderCart cart = orderCartMapper.selectById(id);
        if (cart == null) {
            log.warn("Cart item not found: id={}", id);
            return;
        }
        if (!cart.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此购物车项");
        }
        orderCartMapper.deleteById(id);
        log.info("Cart item deleted: id={}, userId={}", id, userId);
    }

    @Override
    public void clear(Long userId) {
        LambdaQueryWrapper<OrderCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderCart::getUserId, userId);
        orderCartMapper.delete(wrapper);
        log.info("Cart cleared for userId={}", userId);
    }
}
