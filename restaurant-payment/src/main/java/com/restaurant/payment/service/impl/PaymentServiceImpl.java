package com.restaurant.payment.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.enums.PaymentStatusEnum;
import com.restaurant.common.exception.BusinessException;
import com.restaurant.payment.client.OrderServiceClient;
import com.restaurant.payment.client.dto.OrderInfoDTO;
import com.restaurant.payment.entity.PaymentRecord;
import com.restaurant.payment.mapper.PaymentRecordMapper;
import com.restaurant.common.dto.mq.OrderMessage;
import com.restaurant.payment.mq.PaymentResultProducer;
import com.restaurant.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Resource
    private PaymentRecordMapper paymentRecordMapper;

    @Resource
    private OrderServiceClient orderServiceClient;

    @Resource
    private PaymentResultProducer paymentResultProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentRecord createPayment(Long userId, Long orderId, Integer payMethod) {
        if (userId == null || orderId == null) {
            throw new BusinessException("用户ID和订单ID不能为空");
        }

        // Check if payment already exists for this order
        PaymentRecord existing = paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getOrderId, orderId)
        );

        if (existing != null) {
            throw new BusinessException("该订单已存在支付记录，请勿重复创建");
        }

        PaymentRecord record = new PaymentRecord();
        record.setOrderId(orderId);
        record.setUserId(userId);
        record.setPayMethod(payMethod != null ? payMethod : PaymentRecord.PAY_METHOD_MOCK);
        record.setPayStatus(PaymentStatusEnum.UNPAID.getCode());

        // 从订单服务获取订单号与实付金额
        OrderInfoDTO orderInfo = orderServiceClient.getOrderInfo(orderId, userId);
        if (orderInfo != null) {
            record.setOrderNo(orderInfo.getOrderNo());
            record.setPayAmount(orderInfo.getPayAmount());
        }

        int inserted = paymentRecordMapper.insert(record);
        if (inserted <= 0) {
            throw new BusinessException("创建支付记录失败");
        }

        log.info("Payment record created: paymentId={}, orderId={}, userId={}, payMethod={}",
                record.getId(), orderId, userId, record.getPayMethod());

        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentRecord mockPay(Long orderId, Long userId) {
        if (orderId == null) {
            throw new BusinessException("订单ID不能为空");
        }

        // Find payment record by orderId
        PaymentRecord record = paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getOrderId, orderId)
        );

        if (record == null) {
            throw new BusinessException("未找到该订单的支付记录");
        }

        // Verify that the payment belongs to this user (if userId is provided)
        if (userId != null && !userId.equals(record.getUserId())) {
            throw new BusinessException("无权操作该支付记录");
        }

        // Check current status
        if (record.getPayStatus() != null && record.getPayStatus() == PaymentStatusEnum.PAID.getCode()) {
            throw new BusinessException("该订单已支付，请勿重复支付");
        }

        if (record.getPayStatus() != null && record.getPayStatus() == PaymentStatusEnum.REFUNDED.getCode()) {
            throw new BusinessException("该订单已退款，无法支付");
        }

        // Update payment status to PAID
        record.setPayStatus(PaymentStatusEnum.PAID.getCode());
        record.setPayTime(LocalDateTime.now());
        record.setTransactionId("MOCK-" + IdUtil.simpleUUID());
        record.setPayMethod(PaymentRecord.PAY_METHOD_MOCK);

        int updated = paymentRecordMapper.updateById(record);
        if (updated <= 0) {
            throw new BusinessException("更新支付状态失败");
        }

        log.info("Mock payment completed: paymentId={}, orderId={}, transactionId={}",
                record.getId(), orderId, record.getTransactionId());

        // [TODO] RabbitMQ 已安装，HTTP回调暂注释，改用 MQ 方式通知订单服务
        // orderServiceClient.updateOrderStatusToConfirmed(orderId);

        // Send payment completed event via MQ
        OrderMessage message = new OrderMessage();
        message.setOrderId(record.getOrderId());
        message.setOrderNo(record.getOrderNo());
        message.setUserId(record.getUserId());
        message.setPayAmount(record.getPayAmount());
        message.setTimestamp(LocalDateTime.now());

        try {
            paymentResultProducer.sendPaymentCompleted(message);
        } catch (Exception e) {
            log.error("Failed to send payment completed event for orderId={}: {}", orderId, e.getMessage(), e);
            // Payment is already done; log the error but don't rollback the payment
            // A scheduled task or admin can re-send the event if needed
        }

        return record;
    }

    @Override
    public PaymentRecord getStatus(Long orderId) {
        if (orderId == null) {
            throw new BusinessException("订单ID不能为空");
        }

        PaymentRecord record = paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getOrderId, orderId)
        );

        if (record == null) {
            throw new BusinessException("未找到该订单的支付记录");
        }

        return record;
    }

    @Override
    public Page<PaymentRecord> adminPage(String orderNo, Integer payStatus, int page, int size) {
        LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();

        if (orderNo != null && !orderNo.trim().isEmpty()) {
            queryWrapper.like(PaymentRecord::getOrderNo, orderNo.trim());
        }

        if (payStatus != null) {
            queryWrapper.eq(PaymentRecord::getPayStatus, payStatus);
        }

        queryWrapper.orderByDesc(PaymentRecord::getCreateTime);

        Page<PaymentRecord> pageResult = new Page<>(page, size);
        return paymentRecordMapper.selectPage(pageResult, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long paymentId) {
        if (paymentId == null) {
            throw new BusinessException("支付记录ID不能为空");
        }

        PaymentRecord record = paymentRecordMapper.selectById(paymentId);

        if (record == null) {
            throw new BusinessException("未找到该支付记录");
        }

        // Check if the payment is in a refundable state
        if (record.getPayStatus() == null || record.getPayStatus() != PaymentStatusEnum.PAID.getCode()) {
            throw new BusinessException("只有已支付的订单才能退款");
        }

        // Update status to REFUNDED
        record.setPayStatus(PaymentStatusEnum.REFUNDED.getCode());
        record.setRefundAmount(record.getPayAmount());
        record.setRefundTime(LocalDateTime.now());

        int updated = paymentRecordMapper.updateById(record);
        if (updated <= 0) {
            throw new BusinessException("退款操作失败");
        }

        log.info("Refund completed: paymentId={}, orderId={}, refundAmount={}",
                paymentId, record.getOrderId(), record.getRefundAmount());
    }
}
