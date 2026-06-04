package com.restaurant.payment.mq;

import com.restaurant.common.dto.mq.OrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class PaymentResultProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送支付完成事件
     *
     * @param message 订单消息
     */
    public void sendPaymentCompleted(OrderMessage message) {
        if (message == null) {
            log.warn("Cannot send null OrderMessage");
            return;
        }

        log.info("Sending payment completed event: orderId={}, orderNo={}",
                message.getOrderId(), message.getOrderNo());

        try {
            rabbitTemplate.convertAndSend("order.exchange", "payment.completed", message);
            log.info("Payment completed event sent successfully: orderId={}", message.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send payment completed event for orderId={}: {}",
                    message.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("Failed to send payment completed event", e);
        }
    }
}
