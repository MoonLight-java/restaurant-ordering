package com.restaurant.payment.controller;

import com.restaurant.common.dto.response.Result;
import com.restaurant.common.exception.BusinessException;
import com.restaurant.payment.entity.PaymentRecord;
import com.restaurant.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    /**
     * 创建支付
     */
    @PostMapping("/create")
    public Result<Map<String, Object>> createPayment(@RequestBody Map<String, Object> body,
                                                      @RequestHeader("X-User-Id") Long userId) {
        Long orderId = getLong(body, "orderId");
        Integer payMethod = getInteger(body, "payMethod");

        if (orderId == null) {
            return Result.fail("订单ID不能为空");
        }
        if (userId == null) {
            return Result.fail("用户ID不能为空");
        }

        try {
            PaymentRecord record = paymentService.createPayment(userId, orderId, payMethod);

            Map<String, Object> data = new HashMap<>();
            data.put("paymentId", record.getId());
            data.put("payAmount", record.getPayAmount());
            return Result.ok(data);
        } catch (BusinessException e) {
            log.warn("Create payment failed: {}", e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 模拟支付
     */
    @PostMapping("/mock-pay/{orderId}")
    public Result<PaymentRecord> mockPay(@PathVariable Long orderId,
                                         @RequestHeader("X-User-Id") Long userId) {
        if (orderId == null) {
            return Result.fail("订单ID不能为空");
        }

        try {
            PaymentRecord record = paymentService.mockPay(orderId, userId);
            return Result.ok(record);
        } catch (BusinessException e) {
            log.warn("Mock pay failed for orderId={}: {}", orderId, e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/status/{orderId}")
    public Result<PaymentRecord> getStatus(@PathVariable Long orderId) {
        if (orderId == null) {
            return Result.fail("订单ID不能为空");
        }

        try {
            PaymentRecord record = paymentService.getStatus(orderId);
            return Result.ok(record);
        } catch (BusinessException e) {
            log.warn("Get payment status failed for orderId={}: {}", orderId, e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    // ====== Helper methods ======

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
