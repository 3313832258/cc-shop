-- CC-Shop 初始化 Schema
-- 所有表放在 cc_shop 库下，docker-compose 挂载到 /docker-entrypoint-initdb.d 自动执行

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS cc_shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cc_shop;

-- ==================== 用户域 ====================

CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `avatar` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人',
  `province` VARCHAR(50) NOT NULL,
  `city` VARCHAR(50) NOT NULL,
  `district` VARCHAR(50) NOT NULL,
  `detail` VARCHAR(200) NOT NULL COMMENT '详细地址',
  `phone` VARCHAR(20) NOT NULL,
  `is_default` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址';

CREATE TABLE `favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏';

CREATE TABLE `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `type` VARCHAR(20) NOT NULL COMMENT 'order/promotion/system',
  `title` VARCHAR(200) NOT NULL,
  `content` VARCHAR(2000) NOT NULL,
  `is_read` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知';

CREATE TABLE `user_action_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `action` VARCHAR(20) NOT NULL COMMENT 'view/click/cart/order/favorite/search',
  `target_type` VARCHAR(20) NOT NULL,
  `target_id` BIGINT DEFAULT NULL,
  `extra` VARCHAR(1000) DEFAULT NULL COMMENT 'JSON',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`, `created_at`),
  KEY `idx_action` (`action`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为埋点';

-- ==================== 商品域 ====================

CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `parent_id` BIGINT NOT NULL DEFAULT 0,
  `level` INT NOT NULL DEFAULT 1,
  `sort_order` INT NOT NULL DEFAULT 0,
  `icon` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类';

CREATE TABLE `brand` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `logo` VARCHAR(500) DEFAULT NULL,
  `description` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌';

CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL,
  `description` TEXT,
  `brand_id` BIGINT DEFAULT NULL,
  `category_id` BIGINT NOT NULL,
  `images` JSON COMMENT '商品主图列表',
  `status` INT NOT NULL DEFAULT 0 COMMENT '0下架/1上架/2停售',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_brand` (`brand_id`),
  FULLTEXT KEY `ft_name_desc` (`name`, `description`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品主表';

CREATE TABLE `product_sku` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `specs` JSON NOT NULL COMMENT '规格键值对，如{"颜色":"黑","存储":"128G"}',
  `price` DECIMAL(10,2) NOT NULL,
  `original_price` DECIMAL(10,2) DEFAULT NULL,
  `stock` INT NOT NULL DEFAULT 0,
  `sku_code` VARCHAR(50) DEFAULT NULL,
  `image` VARCHAR(500) DEFAULT NULL COMMENT '该 SKU 专属图',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_code` (`sku_code`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU';

CREATE TABLE `product_spec` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `spec_name` VARCHAR(50) NOT NULL COMMENT '参数名',
  `spec_value` VARCHAR(200) NOT NULL COMMENT '参数值',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品参数';

CREATE TABLE `product_image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `url` VARCHAR(500) NOT NULL,
  `sort` INT NOT NULL DEFAULT 0,
  `is_main` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片';

CREATE TABLE `product_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `sku_id` BIGINT DEFAULT NULL,
  `order_id` BIGINT DEFAULT NULL,
  `rating` INT NOT NULL DEFAULT 5,
  `content` VARCHAR(1000) DEFAULT NULL,
  `images` JSON,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价（只读种子数据，Day1 不做提交）';

-- ==================== 订单域 ====================

CREATE TABLE `order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `order_no` VARCHAR(64) NOT NULL,
  `total_amount` DECIMAL(10,2) NOT NULL,
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0,
  `final_amount` DECIMAL(10,2) NOT NULL,
  `coupon_id` BIGINT DEFAULT NULL,
  `status` INT NOT NULL DEFAULT 0 COMMENT '0待付款/1待发货/2已发货/3已完成/4已取消/5已退款',
  `address_snapshot` JSON,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `paid_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

CREATE TABLE `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `sku_id` BIGINT NOT NULL,
  `product_name` VARCHAR(200) NOT NULL,
  `sku_specs` JSON,
  `product_image` VARCHAR(500),
  `price` DECIMAL(10,2) NOT NULL,
  `quantity` INT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

CREATE TABLE `aftersale_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `order_item_id` BIGINT DEFAULT NULL,
  `user_id` BIGINT NOT NULL,
  `type` VARCHAR(20) NOT NULL COMMENT 'refund/return_refund',
  `reason` VARCHAR(500),
  `status` INT NOT NULL DEFAULT 0 COMMENT '0待审/1通过/2拒绝/3处理中/4完成',
  `amount` DECIMAL(10,2) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后记录';

-- ==================== 支付域 ====================

CREATE TABLE `payment_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `payment_no` VARCHAR(64) NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `method` VARCHAR(20) NOT NULL COMMENT 'alipay/wechat',
  `status` INT NOT NULL DEFAULT 0 COMMENT '0待支付/1成功/2失败/3已退款',
  `paid_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录';

-- ==================== 促销域 ====================

CREATE TABLE `flash_sale_activity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL,
  `status` INT NOT NULL DEFAULT 0 COMMENT '0未开始/1进行中/2已结束',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='限时抢购活动';

CREATE TABLE `flash_sale_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `sku_id` BIGINT NOT NULL,
  `flash_price` DECIMAL(10,2) NOT NULL,
  `total_stock` INT NOT NULL,
  `available_stock` INT NOT NULL,
  `limit_per_user` INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_sku` (`activity_id`, `sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抢购商品';

CREATE TABLE `coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `type` INT NOT NULL COMMENT '0满减/1折扣',
  `value` DECIMAL(10,2) NOT NULL COMMENT '金额或折扣率',
  `min_order_amount` DECIMAL(10,2) NOT NULL DEFAULT 0,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL,
  `total_count` INT NOT NULL,
  `remaining_count` INT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板';

CREATE TABLE `user_coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `coupon_id` BIGINT NOT NULL,
  `status` INT NOT NULL DEFAULT 0 COMMENT '0可用/1已用/2过期',
  `used_order_id` BIGINT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券';

-- ==================== 物流域 ====================

CREATE TABLE `logistics_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `carrier` VARCHAR(50) NOT NULL,
  `tracking_no` VARCHAR(64) NOT NULL,
  `status` INT NOT NULL DEFAULT 0,
  `current_location` VARCHAR(200) DEFAULT NULL,
  `estimated_delivery` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流记录';

CREATE TABLE `logistics_step` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `logistics_id` BIGINT NOT NULL,
  `description` VARCHAR(500) NOT NULL,
  `location` VARCHAR(200) DEFAULT NULL,
  `timestamp` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_logistics_id` (`logistics_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流步骤';
