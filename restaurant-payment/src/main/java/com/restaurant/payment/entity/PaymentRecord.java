package com.restaurant.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_record")
public class PaymentRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 订单号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 支付金额 */
    private BigDecimal payAmount;

    /**
     * 支付方式：
     * 0 - 微信支付
     * 1 - 模拟支付
     */
    private Integer payMethod;

    /**
     * 支付状态：
     * 0 - 未支付
     * 1 - 已支付
     * 2 - 退款中
     * 3 - 已退款
     */
    private Integer payStatus;

    /** 第三方交易流水号 */
    private String transactionId;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 退款时间 */
    private LocalDateTime refundTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    // ====== 支付方式常量 ======
    public static final int PAY_METHOD_WECHAT = 0;
    public static final int PAY_METHOD_MOCK = 1;

    // ====== 支付状态常量 ======
    public static final int PAY_STATUS_UNPAID = 0;
    public static final int PAY_STATUS_PAID = 1;
    public static final int PAY_STATUS_REFUNDING = 2;
    public static final int PAY_STATUS_REFUNDED = 3;
}
