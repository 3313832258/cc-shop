-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    total_amount DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    final_amount DECIMAL(10,2) NOT NULL,
    coupon_id BIGINT,
    status INT DEFAULT 0,
    address_snapshot VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 订单明细表
CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    sku_specs VARCHAR(500),
    product_image VARCHAR(255),
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    deleted INT DEFAULT 0
);

-- 支付记录表
CREATE TABLE IF NOT EXISTS payment_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_no VARCHAR(50) NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    method VARCHAR(20),
    status INT DEFAULT 0,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 插入测试数据
INSERT INTO `order` (user_id, order_no, total_amount, discount_amount, final_amount, status, address_snapshot) VALUES (1, 'ORDER20240101001', 5999.00, 0, 5999.00, 0, '{"receiverName":"张三","phone":"13800138000","province":"北京市","city":"北京市","district":"朝阳区","detail":"三里屯路1号"}');
INSERT INTO `order` (user_id, order_no, total_amount, discount_amount, final_amount, status, address_snapshot) VALUES (1, 'ORDER20240101002', 6999.00, 200, 6799.00, 1, '{"receiverName":"张三","phone":"13800138000","province":"北京市","city":"北京市","district":"朝阳区","detail":"三里屯路1号"}');

INSERT INTO order_item (order_id, product_id, sku_id, product_name, sku_specs, price, quantity) VALUES (1, 1, 1, 'iPhone 15', '{"颜色":"黑色","存储":"128GB"}', 5999.00, 1);
INSERT INTO order_item (order_id, product_id, sku_id, product_name, sku_specs, price, quantity) VALUES (2, 2, 3, '华为 Mate 60', '{"颜色":"黑色","存储":"256GB"}', 6999.00, 1);
