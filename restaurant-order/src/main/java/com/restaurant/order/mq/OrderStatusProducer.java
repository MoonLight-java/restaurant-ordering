package com.restaurant.order.mq;

import com.restaurant.common.dto.mq.OrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.order.exchange:order.exchange}")
    private String orderExchange;

    /**
     * 发送订单创建消息
     */
    public void sendOrderCreated(OrderMessage message) {
        log.info("Sending order created message: orderNo={}", message.getOrderNo());
        rabbitTemplate.convertAndSend(orderExchange, "order.created", message);
        log.info("Order created message sent successfully: orderNo={}", message.getOrderNo());
    }

    /**
     * 发送订单状态变更消息
     */
    public void sendOrderStatusChanged(OrderMessage message) {
        log.info("Sending order status changed message: orderNo={}, newStatus={}", message.getOrderNo(), message.getStatus());
        rabbitTemplate.convertAndSend(orderExchange, "order.status.changed", message);
        log.info("Order status changed message sent successfully: orderNo={}", message.getOrderNo());
    }
}
