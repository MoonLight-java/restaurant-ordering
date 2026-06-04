package com.restaurant.payment.client;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.restaurant.payment.client.dto.OrderInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceClient {

    private final RestTemplate restTemplate;

    private static final String ORDER_SERVICE_URL = "http://localhost:8084/api/order";
    private static final String ORDER_ADMIN_URL = "http://localhost:8084/api/admin";

    /**
     * 获取订单信息（订单号 + 实付金额），用于支付时填充 payment_record
     */
    public OrderInfoDTO getOrderInfo(Long orderId, Long userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", String.valueOf(userId));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    ORDER_SERVICE_URL + "/detail/" + orderId,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            String body = response.getBody();
            if (body == null) {
                return null;
            }

            JSONObject result = JSONUtil.parseObj(body);
            if (result.getInt("code") != 200 || result.get("data") == null) {
                log.warn("Order service returned error for orderId={}: {}", orderId, result.getStr("message"));
                return null;
            }

            JSONObject data = result.getJSONObject("data");
            JSONObject order = data.getJSONObject("order");
            if (order == null) {
                return null;
            }

            OrderInfoDTO dto = new OrderInfoDTO();
            dto.setOrderId(order.getLong("id"));
            dto.setOrderNo(order.getStr("orderNo"));
            BigDecimal payAmount = order.getBigDecimal("payAmount");
            dto.setPayAmount(payAmount);
            dto.setStatus(order.getInt("status"));
            return dto;
        } catch (Exception e) {
            log.error("Failed to fetch order info for orderId={}", orderId, e);
            return null;
        }
    }

    /**
     * 支付成功后回调：将订单状态从 PENDING 更新为 CONFIRMED
     */
    public void updateOrderStatusToConfirmed(Long orderId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = new HashMap<>();
            body.put("orderId", orderId);
            body.put("newStatus", 1); // CONFIRMED

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    ORDER_ADMIN_URL + "/orders/" + orderId + "/status",
                    HttpMethod.PUT,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Order status updated to CONFIRMED after payment: orderId={}", orderId);
            } else {
                log.warn("Failed to update order status after payment: orderId={}, httpStatus={}",
                        orderId, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to call order service status update for orderId={}", orderId, e);
        }
    }
}
