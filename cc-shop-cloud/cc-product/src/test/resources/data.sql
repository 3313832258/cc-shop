-- 分类表
CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    level INT DEFAULT 1,
    sort_order INT DEFAULT 0,
    icon VARCHAR(255),
    deleted INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 品牌表
CREATE TABLE IF NOT EXISTS brand (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    logo VARCHAR(255),
    description VARCHAR(500),
    deleted INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 商品表
CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    brand_id BIGINT,
    category_id BIGINT,
    main_image VARCHAR(255),
    images VARCHAR(1000),
    status INT DEFAULT 1,
    deleted INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- SKU 表
CREATE TABLE IF NOT EXISTS product_sku (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    specs VARCHAR(500),
    price DECIMAL(10,2) NOT NULL,
    original_price DECIMAL(10,2),
    stock INT DEFAULT 0,
    sku_code VARCHAR(50),
    image VARCHAR(255),
    deleted INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 商品规格表
CREATE TABLE IF NOT EXISTS product_spec (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    spec_name VARCHAR(50) NOT NULL,
    spec_value VARCHAR(500),
    deleted INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 商品评价表
CREATE TABLE IF NOT EXISTS product_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    sku_id BIGINT,
    order_id BIGINT,
    rating INT DEFAULT 5,
    content VARCHAR(500),
    images VARCHAR(1000),
    deleted INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 商品图片表
CREATE TABLE IF NOT EXISTS product_image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    sort_order INT DEFAULT 0,
    deleted INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 插入测试数据
INSERT INTO category (name, parent_id, level, sort_order) VALUES ('手机数码', 0, 1, 1);
INSERT INTO category (name, parent_id, level, sort_order) VALUES ('手机', 1, 2, 1);
INSERT INTO category (name, parent_id, level, sort_order) VALUES ('电脑办公', 0, 1, 2);

INSERT INTO brand (name, description) VALUES ('Apple', '苹果公司');
INSERT INTO brand (name, description) VALUES ('华为', '华为技术有限公司');
INSERT INTO brand (name, description) VALUES ('小米', '小米科技有限责任公司');

INSERT INTO product (name, description, brand_id, category_id, main_image, status) VALUES ('iPhone 15', '苹果最新款手机', 1, 2, 'https://example.com/iphone15.jpg', 1);
INSERT INTO product (name, description, brand_id, category_id, main_image, status) VALUES ('华为 Mate 60', '华为最新款手机', 2, 2, 'https://example.com/mate60.jpg', 1);
INSERT INTO product (name, description, brand_id, category_id, main_image, status) VALUES ('小米14', '小米最新款手机', 3, 2, 'https://example.com/mi14.jpg', 1);

INSERT INTO product_sku (product_id, specs, price, original_price, stock, sku_code) VALUES (1, '{"颜色":"黑色","存储":"128GB"}', 5999.00, 6999.00, 100, 'IPHONE15-BLACK-128');
INSERT INTO product_sku (product_id, specs, price, original_price, stock, sku_code) VALUES (1, '{"颜色":"白色","存储":"256GB"}', 6999.00, 7999.00, 50, 'IPHONE15-WHITE-256');
INSERT INTO product_sku (product_id, specs, price, original_price, stock, sku_code) VALUES (2, '{"颜色":"黑色","存储":"256GB"}', 6999.00, 7999.00, 80, 'MATE60-BLACK-256');
INSERT INTO product_sku (product_id, specs, price, original_price, stock, sku_code) VALUES (3, '{"颜色":"白色","存储":"128GB"}', 3999.00, 4999.00, 200, 'MI14-WHITE-128');
