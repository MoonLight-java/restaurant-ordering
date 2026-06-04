package com.restaurant.order.mq;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.restaurant.common.dto.mq.OrderMessage;
import com.restaurant.order.entity.OrderMain;
import com.restaurant.order.mapper.OrderMainMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStatusConsumer {

    private final OrderMainMapper orderMainMapper;

    /**
     * 监听支付完成消息，将订单状态从 PENDING 更新为 CONFIRMED
     */
    @Transactional
    @RabbitListener(queues = "${rabbitmq.order.queue.payment-completed:payment.completed.queue}")
    public void listenPaymentCompleted(OrderMessage message) {
        log.info("Received payment completed message: orderNo={}", message.getOrderNo());

        if (message.getOrderId() == null) {
            log.warn("Payment completed message has null orderId, skipping");
            return;
        }

        OrderMain order = orderMainMapper.selectById(message.getOrderId());
        if (order == null) {
            log.warn("Order not found: orderId={}", message.getOrderId());
            return;
        }

        if (order.getStatus() != OrderMain.STATUS_PENDING) {
            log.warn("Order {} is not in PENDING status, current status: {}, skipping",
                    message.getOrderNo(), order.getStatus());
            return;
        }

        LambdaUpdateWrapper<OrderMain> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OrderMain::getId, message.getOrderId())
                .eq(OrderMain::getStatus, OrderMain.STATUS_PENDING)
                .set(OrderMain::getStatus, OrderMain.STATUS_CONFIRMED)
                .set(OrderMain::getConfirmTime, LocalDateTime.now());

        int rows = orderMainMapper.update(null, wrapper);
        if (rows > 0) {
            log.info("Order {} status updated from PENDING to CONFIRMED", message.getOrderNo());
        } else {
            log.warn("Order {} status update failed (concurrent modification or already updated)", message.getOrderNo());
        }
    }
}
