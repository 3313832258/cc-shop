-- 优惠券表
CREATE TABLE IF NOT EXISTS coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type INT NOT NULL,
    value DECIMAL(10,2) NOT NULL,
    min_order_amount DECIMAL(10,2) DEFAULT 0,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    total_count INT DEFAULT 0,
    remaining_count INT DEFAULT 0,
    status INT DEFAULT 1,
    deleted INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS user_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    status INT DEFAULT 0,
    used_order_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 插入测试数据
INSERT INTO coupon (name, type, value, min_order_amount, start_time, end_time, total_count, remaining_count, status) VALUES ('满100减10', 0, 10.00, 100.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 100, 100, 1);
INSERT INTO coupon (name, type, value, min_order_amount, start_time, end_time, total_count, remaining_count, status) VALUES ('满200减20', 0, 20.00, 200.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 50, 50, 1);
INSERT INTO coupon (name, type, value, min_order_amount, start_time, end_time, total_count, remaining_count, status) VALUES ('8折优惠券', 1, 0.80, 500.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 30, 30, 1);

INSERT INTO user_coupon (user_id, coupon_id, status) VALUES (1, 1, 0);
INSERT INTO user_coupon (user_id, coupon_id, status) VALUES (1, 2, 1);
