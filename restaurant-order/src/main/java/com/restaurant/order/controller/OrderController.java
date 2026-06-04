package com.restaurant.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.dto.response.Result;
import com.restaurant.order.dto.CreateOrderDTO;
import com.restaurant.order.dto.OrderDetailDTO;
import com.restaurant.order.entity.OrderMain;
import com.restaurant.order.service.OrderService;
import com.restaurant.order.service.OrderService.CreateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public Result<CreateOrderResult> createOrder(@RequestHeader("X-User-Id") Long userId,
                                         @RequestBody CreateOrderDTO dto) {
        log.info("Create order request: userId={}, cartItemIds={}", userId, dto.getCartItemIds());
        return Result.ok(orderService.createOrder(userId, dto));
    }

    @GetMapping("/list")
    public Result<Page<OrderMain>> pageByUserId(@RequestHeader("X-User-Id") Long userId,
                                        @RequestParam(required = false) Integer status,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        log.info("Query orders for userId={}, status={}, page={}, size={}", userId, status, page, size);
        return Result.ok(orderService.pageByUserId(userId, status, page, size));
    }

    @GetMapping("/detail/{id}")
    public Result<OrderDetailDTO> getOrderDetail(@RequestHeader("X-User-Id") Long userId,
                                         @PathVariable Long id) {
        log.info("Query order detail: orderId={}, userId={}", id, userId);
        return Result.ok(orderService.getOrderDetail(id, userId));
    }

    @PutMapping("/cancel/{id}")
    public Result<?> cancelOrder(@RequestHeader("X-User-Id") Long userId,
                              @PathVariable Long id,
                              @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("cancelReason", "用户取消");
        log.info("Cancel order: orderId={}, userId={}, reason={}", id, userId, reason);
        orderService.cancelOrder(id, userId, reason);
        return Result.ok();
    }

    @GetMapping("/status/{id}")
    public Result<Map<String, Object>> getOrderStatus(@RequestHeader("X-User-Id") Long userId,
                                              @PathVariable Long id) {
        log.info("Query order status: orderId={}, userId={}", id, userId);
        OrderDetailDTO detail = orderService.getOrderDetail(id, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", detail.getOrder().getId());
        result.put("orderNo", detail.getOrder().getOrderNo());
        result.put("status", detail.getOrder().getStatus());
        result.put("paymentStatus", detail.getPaymentStatus());
        return Result.ok(result);
    }
}
