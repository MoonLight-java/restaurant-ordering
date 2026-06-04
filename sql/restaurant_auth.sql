CREATE DATABASE IF NOT EXISTS db_restaurant_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE db_restaurant_auth;

CREATE TABLE auth_user (
    id BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(64) DEFAULT NULL COMMENT '用户名(管理员登录)',
    password_hash VARCHAR(128) DEFAULT NULL COMMENT 'BCrypt密码哈希',
    openid VARCHAR(64) DEFAULT NULL COMMENT '微信openid',
    unionid VARCHAR(64) DEFAULT NULL COMMENT '微信unionid',
    session_key VARCHAR(64) DEFAULT NULL COMMENT '微信session_key',
    nickname VARCHAR(64) DEFAULT '' COMMENT '昵称',
    avatar VARCHAR(512) DEFAULT '' COMMENT '头像URL',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    role TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0=USER, 1=ADMIN, 2=SUPER_ADMIN',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_openid (openid),
    UNIQUE KEY uk_username (username),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认证用户表';

-- Default admin user (admin/admin123) is auto-created by AuthServiceImpl @PostConstruct on first startup
-- No seed data needed here
