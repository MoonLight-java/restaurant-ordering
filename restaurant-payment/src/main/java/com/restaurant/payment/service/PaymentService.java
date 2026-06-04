package com.restaurant.payment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.payment.entity.PaymentRecord;

public interface PaymentService {

    /**
     * 创建支付记录
     *
     * @param userId    用户ID
     * @param orderId   订单ID
     * @param payMethod 支付方式
     * @return 支付记录
     */
    PaymentRecord createPayment(Long userId, Long orderId, Integer payMethod);

    /**
     * 模拟支付
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 更新后的支付记录
     */
    PaymentRecord mockPay(Long orderId, Long userId);

    /**
     * 查询支付状态
     *
     * @param orderId 订单ID
     * @return 支付记录
     */
    PaymentRecord getStatus(Long orderId);

    /**
     * 管理端分页查询支付记录
     *
     * @param orderNo   订单号（可选）
     * @param payStatus 支付状态（可选）
     * @param page      页码
     * @param size      每页大小
     * @return 分页结果
     */
    Page<PaymentRecord> adminPage(String orderNo, Integer payStatus, int page, int size);

    /**
     * 退款
     *
     * @param paymentId 支付记录ID
     */
    void refund(Long paymentId);
}
