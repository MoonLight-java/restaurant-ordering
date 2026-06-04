CREATE DATABASE IF NOT EXISTS db_restaurant_payment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE db_restaurant_payment;

CREATE TABLE payment_record (
    id BIGINT UNSIGNED NOT NULL PRIMARY KEY,
    order_id BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    pay_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '支付金额',
    pay_method TINYINT NOT NULL DEFAULT 0 COMMENT '支付方式: 0=微信支付, 1=模拟支付',
    pay_status TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态: 0=未支付, 1=已支付, 2=退款中, 3=已退款',
    transaction_id VARCHAR(64) DEFAULT NULL COMMENT '第三方交易号',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    refund_amount DECIMAL(10,2) DEFAULT NULL COMMENT '退款金额',
    refund_time DATETIME DEFAULT NULL COMMENT '退款时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order (order_id),
    INDEX idx_order_no (order_no),
    INDEX idx_pay_status (pay_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';
