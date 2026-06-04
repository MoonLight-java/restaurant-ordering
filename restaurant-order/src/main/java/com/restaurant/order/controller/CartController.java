package com.restaurant.order.controller;

import com.restaurant.common.dto.response.Result;
import com.restaurant.order.entity.OrderCart;
import com.restaurant.order.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/items")
    public Result<List<OrderCart>> listByUserId(@RequestHeader("X-User-Id") Long userId) {
        log.info("Query cart items for userId={}", userId);
        return Result.ok(cartService.listByUserId(userId));
    }

    @PostMapping("/items")
    public Result<?> add(@RequestHeader("X-User-Id") Long userId,
                      @RequestBody OrderCart cart) {
        cart.setUserId(userId);
        cartService.add(cart);
        log.info("Cart item added for userId={}, dishId={}", userId, cart.getDishId());
        return Result.ok();
    }

    @PutMapping("/items/{id}")
    public Result<?> updateQuantity(@RequestHeader("X-User-Id") Long userId,
                                 @PathVariable Long id,
                                 @RequestBody Map<String, Integer> body) {
        Integer quantity = body.get("quantity");
        cartService.updateQuantity(id, quantity, userId);
        log.info("Cart item quantity updated: id={}, quantity={}", id, quantity);
        return Result.ok();
    }

    @DeleteMapping("/items/{id}")
    public Result<?> delete(@RequestHeader("X-User-Id") Long userId,
                         @PathVariable Long id) {
        cartService.delete(id, userId);
        log.info("Cart item deleted: id={}", id);
        return Result.ok();
    }

    @DeleteMapping("/items")
    public Result<?> clear(@RequestHeader("X-User-Id") Long userId) {
        cartService.clear(userId);
        log.info("Cart cleared for userId={}", userId);
        return Result.ok();
    }
}
