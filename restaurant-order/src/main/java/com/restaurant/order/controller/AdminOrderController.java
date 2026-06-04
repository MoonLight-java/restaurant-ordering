package com.restaurant.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.dto.response.Result;
import com.restaurant.order.dto.OrderDetailDTO;
import com.restaurant.order.dto.StatusChangeDTO;
import com.restaurant.order.entity.OrderMain;
import com.restaurant.order.mapper.OrderMainMapper;
import com.restaurant.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final OrderMainMapper orderMainMapper;

    /**
     * 管理员订单分页查询
     */
    @GetMapping("/orders")
    public Result<Page<OrderMain>> adminPageOrders(@RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) String orderNo,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        log.info("Admin query orders: status={}, orderNo={}, page={}, size={}", status, orderNo, page, size);
        return Result.ok(orderService.adminPageOrders(status, orderNo, page, size));
    }

    /**
     * 管理员查询订单详情
     */
    @GetMapping("/orders/{id}")
    public Result<OrderDetailDTO> adminGetOrderDetail(@PathVariable Long id) {
        log.info("Admin query order detail: orderId={}", id);
        return Result.ok(orderService.adminGetOrderDetail(id));
    }

    /**
     * 管理员变更订单状态
     * Body: { orderId, newStatus, cancelReason }
     */
    @PutMapping("/orders/{id}/status")
    public Result<String> adminChangeStatus(@PathVariable Long id,
                                    @RequestBody StatusChangeDTO dto) {
        log.info("Admin change order status: orderId={}, newStatus={}", id, dto.getNewStatus());
        orderService.adminChangeStatus(id, dto.getNewStatus(), dto.getCancelReason());
        return Result.ok("状态更新成功");
    }

    /**
     * 仪表盘统计数据
     */
    @GetMapping("/dashboard/statistics")
    public Result<Map<String, Object>> dashboardStatistics() {
        log.info("Query dashboard statistics");
        int todayOrderCount = orderMainMapper.countTodayOrders();
        int yesterdayOrderCount = orderMainMapper.countYesterdayOrders();
        BigDecimal todayRevenue = orderMainMapper.sumTodayRevenue();
        int pendingOrders = orderMainMapper.countPendingOrders();

        Map<String, Object> result = new HashMap<>();
        result.put("todayOrderCount", todayOrderCount);
        result.put("yesterdayOrderCount", yesterdayOrderCount);
        result.put("todayRevenue", todayRevenue);
        result.put("pendingOrders", pendingOrders);
        return Result.ok(result);
    }
}
