CREATE DATABASE IF NOT EXISTS db_restaurant_order DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE db_restaurant_order;

CREATE TABLE order_cart (
    id BIGINT UNSIGNED NOT NULL PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    dish_id BIGINT UNSIGNED NOT NULL COMMENT '菜品ID',
    dish_name VARCHAR(128) NOT NULL DEFAULT '' COMMENT '菜品名称快照',
    dish_image VARCHAR(512) NOT NULL DEFAULT '' COMMENT '菜品图片快照',
    spec_json TEXT COMMENT '规格JSON',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价(含规格加价)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

CREATE TABLE order_main (
    id BIGINT UNSIGNED NOT NULL PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    address_json TEXT COMMENT '地址快照JSON',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
    pay_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=待支付,1=已确认,2=制作中,3=已完成,4=已取消',
    remark VARCHAR(512) DEFAULT '' COMMENT '备注',
    cancel_reason VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    confirm_time DATETIME DEFAULT NULL COMMENT '确认时间',
    complete_time DATETIME DEFAULT NULL COMMENT '完成时间',
    cancel_time DATETIME DEFAULT NULL COMMENT '取消时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

CREATE TABLE order_detail (
    id BIGINT UNSIGNED NOT NULL PRIMARY KEY,
    order_id BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号(冗余)',
    dish_id BIGINT UNSIGNED NOT NULL COMMENT '菜品ID',
    dish_name VARCHAR(128) NOT NULL DEFAULT '' COMMENT '菜品名称快照',
    dish_image VARCHAR(512) NOT NULL DEFAULT '' COMMENT '菜品图片快照',
    spec_json TEXT COMMENT '规格快照JSON',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价',
    sub_total DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '小计',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';
