package com.restaurant.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
@RequestMapping("/api/admin")
public class AdminPaymentController {

    @Resource
    private PaymentService paymentService;

    /**
     * 管理端分页查询支付记录
     */
    @GetMapping("/payments")
    public Result<Map<String, Object>> adminPage(@RequestParam(required = false) String orderNo,
                                                  @RequestParam(required = false) Integer payStatus,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        try {
            Page<PaymentRecord> pageResult = paymentService.adminPage(orderNo, payStatus, page, size);

            Map<String, Object> data = new HashMap<>();
            data.put("records", pageResult.getRecords());
            data.put("total", pageResult.getTotal());
            data.put("page", pageResult.getCurrent());
            data.put("size", pageResult.getSize());
            data.put("pages", pageResult.getPages());

            return Result.ok(data);
        } catch (Exception e) {
            log.error("Admin query payments failed: {}", e.getMessage(), e);
            return Result.fail("查询支付记录失败: " + e.getMessage());
        }
    }

    /**
     * 退款
     */
    @PostMapping("/payments/{id}/refund")
    public Result<String> refund(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (id == null) {
            return Result.fail("支付记录ID不能为空");
        }

        try {
            paymentService.refund(id);
            return Result.ok("退款成功");
        } catch (BusinessException e) {
            log.warn("Refund failed for paymentId={}: {}", id, e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Refund error for paymentId={}: {}", id, e.getMessage(), e);
            return Result.fail("退款失败: " + e.getMessage());
        }
    }
}
