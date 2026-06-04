// [TODO] RabbitMQ 暂未安装，待 RabbitMQ 环境就绪后取消注释整个文件
//package com.restaurant.payment.mq;
//
//import com.restaurant.common.enums.PaymentStatusEnum;
//import com.restaurant.payment.entity.PaymentRecord;
//import com.restaurant.payment.mapper.PaymentRecordMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//
//@Slf4j
//@Component
//public class OrderCreatedConsumer {
//
//    @Resource
//    private PaymentRecordMapper paymentRecordMapper;
//
//    @RabbitListener(queues = "order.created.queue")
//    public void handleOrderCreated(OrderMessage message) {
//        if (message == null || message.getOrderId() == null) {
//            log.warn("Received invalid OrderMessage: {}", message);
//            return;
//        }
//
//        log.info("Received order created event: orderId={}, orderNo={}, payAmount={}",
//                message.getOrderId(), message.getOrderNo(), message.getPayAmount());
//
//        try {
//            // Check if payment record already exists for this order (idempotency)
//            PaymentRecord existing = paymentRecordMapper.selectOne(
//                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentRecord>()
//                            .eq(PaymentRecord::getOrderId, message.getOrderId())
//            );
//
//            if (existing != null) {
//                log.warn("Payment record already exists for orderId={}, skipping", message.getOrderId());
//                return;
//            }
//
//            PaymentRecord record = new PaymentRecord();
//            record.setOrderId(message.getOrderId());
//            record.setOrderNo(message.getOrderNo());
//            record.setUserId(message.getUserId());
//            record.setPayAmount(message.getPayAmount());
//            record.setPayMethod(0); // Default to wechat pay
//            record.setPayStatus(PaymentStatusEnum.UNPAID.getCode());
//
//            int inserted = paymentRecordMapper.insert(record);
//            if (inserted > 0) {
//                log.info("Payment record created: paymentId={}, orderId={}", record.getId(), message.getOrderId());
//            } else {
//                log.error("Failed to insert payment record for orderId={}", message.getOrderId());
//            }
//        } catch (Exception e) {
//            log.error("Error processing order created event for orderId={}: {}", message.getOrderId(), e.getMessage(), e);
//            // Re-throw to trigger MQ retry
//            throw new RuntimeException("Failed to process order created event", e);
//        }
//    }
//}
