-- CC-Shop 种子数据
-- 含 SKU 示例（iPhone 15 / AirPods Pro）

USE cc_shop;

-- ==================== 测试用户 ====================
-- 密码均为 123456（BCrypt）
INSERT INTO `user` (`id`, `username`, `password_hash`, `role`) VALUES
(1, 'admin',    '$2a$10$FS4zJaLG6eJbqquQz4r4QOgSmATE2Yb6bqJioF32rzjqk1E3zR2ii', 2),
(2, 'testuser1','$2a$10$FS4zJaLG6eJbqquQz4r4QOgSmATE2Yb6bqJioF32rzjqk1E3zR2ii', 2),
(3, 'testuser3','$2a$10$FS4zJaLG6eJbqquQz4r4QOgSmATE2Yb6bqJioF32rzjqk1E3zR2ii', 1)
ON DUPLICATE KEY UPDATE role=VALUES(role);

-- ==================== 分类（两级结构）====================
INSERT INTO `category` (`id`, `name`, `parent_id`, `level`, `sort_order`) VALUES
(1, '手机数码', 0, 1, 1),
(2, '电脑办公', 0, 1, 2),
(3, '家用电器', 0, 1, 3),
(4, '服装鞋包', 0, 1, 4),
(5, '食品饮料', 0, 1, 5),
(6, '图书文娱', 0, 1, 6),
(101, '手机', 1, 2, 1),
(102, '平板', 1, 2, 2),
(103, '智能手表', 1, 2, 3),
(104, '耳机', 1, 2, 4),
(201, '笔记本', 2, 2, 1),
(202, '台式机', 2, 2, 2),
(203, '显示器', 2, 2, 3),
(301, '冰箱', 3, 2, 1),
(302, '洗衣机', 3, 2, 2),
(401, '男装', 4, 2, 1),
(402, '女装', 4, 2, 2),
(403, '运动鞋', 4, 2, 3);

-- ==================== 品牌 ====================
INSERT INTO `brand` (`id`, `name`, `logo`, `description`) VALUES
(1, 'Apple', NULL, '美国消费电子品牌，iPhone/Mac/AirPods 系列'),
(2, 'Xiaomi', NULL, '中国消费电子品牌，性价比高'),
(3, 'Huawei', NULL, '中国通信与消费电子品牌'),
(4, 'Lenovo', NULL, '联想，电脑/智能设备'),
(5, 'Nike', NULL, '美国运动品牌'),
(6, 'Uniqlo', NULL, '日本快时尚品牌'),
(7, 'Haier', NULL, '海尔家电');

-- ==================== 商品 + SKU 示例 ====================
-- 商品1: iPhone 15 (颜色 × 存储)
INSERT INTO `product` (`id`, `name`, `description`, `brand_id`, `category_id`, `images`, `status`) VALUES
(1, 'Apple iPhone 15 智能手机', 'iPhone 15 搭载 A16 仿生芯片，48MP 主摄，灵动岛设计，USB-C 接口，超瓷晶面板。支持 5G、Face ID、MagSafe。', 1, 101, '["https://picsum.photos/seed/iphone15-1/600/600","https://picsum.photos/seed/iphone15-2/600/600","https://picsum.photos/seed/iphone15-3/600/600"]', 1);

INSERT INTO `product_sku` (`id`, `product_id`, `specs`, `price`, `original_price`, `stock`, `sku_code`, `image`) VALUES
(1, 1, '{"颜色": "黑色", "存储": "128GB"}', 5999.00, 6299.00, 100, 'IP15-BK-128', 'https://picsum.photos/seed/iphone15-bk/600/600'),
(2, 1, '{"颜色": "黑色", "存储": "256GB"}', 6999.00, 7299.00, 50,  'IP15-BK-256', 'https://picsum.photos/seed/iphone15-bk/600/600'),
(3, 1, '{"颜色": "蓝色", "存储": "128GB"}', 5999.00, 6299.00, 80,  'IP15-BL-128', 'https://picsum.photos/seed/iphone15-bl/600/600'),
(4, 1, '{"颜色": "蓝色", "存储": "256GB"}', 6999.00, 7299.00, 30,  'IP15-BL-256', 'https://picsum.photos/seed/iphone15-bl/600/600'),
(5, 1, '{"颜色": "粉色", "存储": "128GB"}', 5999.00, 6299.00, 60,  'IP15-PK-128', 'https://picsum.photos/seed/iphone15-pk/600/600'),
(6, 1, '{"颜色": "粉色", "存储": "256GB"}', 6999.00, 7299.00, 25,  'IP15-PK-256', 'https://picsum.photos/seed/iphone15-pk/600/600');

INSERT INTO `product_spec` (`product_id`, `spec_name`, `spec_value`) VALUES
(1, '屏幕尺寸', '6.1 英寸'),
(1, '处理器', 'A16 仿生芯片'),
(1, '前置摄像头', '12MP 原深感'),
(1, '后置摄像头', '48MP + 12MP 双摄'),
(1, '电池容量', '3349 mAh'),
(1, '重量', '171 g'),
(1, '防水', 'IP68');

-- 商品2: AirPods Pro 第二代
INSERT INTO `product` (`id`, `name`, `description`, `brand_id`, `category_id`, `images`, `status`) VALUES
(2, 'Apple AirPods Pro 第二代（USB-C）', '主动降噪升级、自适应音频、个性化空间音频、USB-C 充电盒，续航长达 6 小时。', 1, 104, '["https://picsum.photos/seed/airpods-1/600/600","https://picsum.photos/seed/airpods-2/600/600"]', 1);

INSERT INTO `product_sku` (`id`, `product_id`, `specs`, `price`, `original_price`, `stock`, `sku_code`) VALUES
(7, 2, '{"颜色": "白色"}', 1799.00, 1899.00, 200, 'APP2-WH');

INSERT INTO `product_spec` (`product_id`, `spec_name`, `spec_value`) VALUES
(2, '降噪', '主动降噪 + 通透模式'),
(2, '充电接口', 'USB-C'),
(2, '续航（耳机）', '6 小时'),
(2, '续航（含充电盒）', '30 小时'),
(2, '防水', 'IPX4');

-- 商品3: 小米 14 Pro
INSERT INTO `product` (`id`, `name`, `description`, `brand_id`, `category_id`, `images`, `status`) VALUES
(3, 'Xiaomi 小米 14 Pro 智能手机', '搭载骁龙 8 Gen 3 处理器，徕卡光学镜头，120W 快充，2K LTPO 屏幕，5000mAh 电池。', 2, 101, '["https://picsum.photos/seed/mi14-1/600/600","https://picsum.photos/seed/mi14-2/600/600"]', 1);

INSERT INTO `product_sku` (`id`, `product_id`, `specs`, `price`, `original_price`, `stock`, `sku_code`, `image`) VALUES
(8, 3, '{"颜色": "黑色", "存储": "256GB"}', 4999.00, 5299.00, 150, 'MI14P-BK-256', 'https://picsum.photos/seed/mi14-bk/600/600'),
(9, 3, '{"颜色": "黑色", "存储": "512GB"}', 5499.00, 5799.00, 80,  'MI14P-BK-512', 'https://picsum.photos/seed/mi14-bk/600/600'),
(10, 3, '{"颜色": "白色", "存储": "256GB"}', 4999.00, 5299.00, 100, 'MI14P-WH-256', 'https://picsum.photos/seed/mi14-wh/600/600');

-- 商品4: MacBook Pro M3
INSERT INTO `product` (`id`, `name`, `description`, `brand_id`, `category_id`, `images`, `status`) VALUES
(4, 'Apple MacBook Pro 14 英寸 M3 芯片', 'M3 芯片，Liquid Retina XDR 显示屏，18 小时续航，MagSafe 充电，3 个雷雳 4 端口。', 1, 201, '["https://picsum.photos/seed/mbp14-1/600/600","https://picsum.photos/seed/mbp14-2/600/600"]', 1);

INSERT INTO `product_sku` (`id`, `product_id`, `specs`, `price`, `original_price`, `stock`, `sku_code`, `image`) VALUES
(11, 4, '{"颜色": "深空黑", "内存": "8GB", "存储": "512GB"}', 12999.00, 13999.00, 40, 'MBP14-M3-8-512', 'https://picsum.photos/seed/mbp14-blk/600/600'),
(12, 4, '{"颜色": "深空黑", "内存": "16GB", "存储": "512GB"}', 14999.00, 15999.00, 30, 'MBP14-M3-16-512', 'https://picsum.photos/seed/mbp14-blk/600/600'),
(13, 4, '{"颜色": "银色", "内存": "8GB", "存储": "512GB"}', 12999.00, 13999.00, 35, 'MBP14-M3-8-512-SV', 'https://picsum.photos/seed/mbp14-sv/600/600');

-- 商品5: 华为 MatePad Pro
INSERT INTO `product` (`id`, `name`, `description`, `brand_id`, `category_id`, `images`, `status`) VALUES
(5, 'HUAWEI MatePad Pro 11 英寸平板电脑', 'OLED 屏幕，骁龙 888，M-Pencil 第二代，HarmonyOS 4，多屏协同。', 3, 102, '["https://picsum.photos/seed/matepad-1/600/600"]', 1);

INSERT INTO `product_sku` (`id`, `product_id`, `specs`, `price`, `original_price`, `stock`, `sku_code`) VALUES
(14, 5, '{"颜色": "曜金黑", "存储": "128GB"}', 3299.00, 3499.00, 60, 'MPP11-BK-128'),
(15, 5, '{"颜色": "曜金黑", "存储": "256GB"}', 3799.00, 3999.00, 40, 'MPP11-BK-256');

-- 商品6: Nike Air Force 1
INSERT INTO `product` (`id`, `name`, `description`, `brand_id`, `category_id`, `images`, `status`) VALUES
(6, 'Nike Air Force 1 \'07 经典运动鞋', '传奇 AF1，皮革鞋面，Air 缓震，经典百搭。', 5, 403, '["https://picsum.photos/seed/af1-1/600/600","https://picsum.photos/seed/af1-2/600/600"]', 1);

INSERT INTO `product_sku` (`id`, `product_id`, `specs`, `price`, `original_price`, `stock`, `sku_code`) VALUES
(16, 6, '{"颜色": "白色", "尺码": "40"}', 799.00, 899.00, 50, 'AF1-WH-40'),
(17, 6, '{"颜色": "白色", "尺码": "41"}', 799.00, 899.00, 60, 'AF1-WH-41'),
(18, 6, '{"颜色": "白色", "尺码": "42"}', 799.00, 899.00, 55, 'AF1-WH-42'),
(19, 6, '{"颜色": "白色", "尺码": "43"}', 799.00, 899.00, 40, 'AF1-WH-43'),
(20, 6, '{"颜色": "黑色", "尺码": "42"}', 799.00, 899.00, 30, 'AF1-BK-42');

-- 商品7: 优衣库轻薄羽绒服
INSERT INTO `product` (`id`, `name`, `description`, `brand_id`, `category_id`, `images`, `status`) VALUES
(7, '优衣库 Ultra Light Down 轻薄羽绒服', '90% 白鸭绒填充，仅 200g 轻盈，收纳袋可折叠。', 6, 402, '["https://picsum.photos/seed/uniqlo-1/600/600"]', 1);

INSERT INTO `product_sku` (`id`, `product_id`, `specs`, `price`, `original_price`, `stock`, `sku_code`) VALUES
(21, 7, '{"颜色": "黑色", "尺码": "S"}', 499.00, 599.00, 80, 'ULD-BK-S'),
(22, 7, '{"颜色": "黑色", "尺码": "M"}', 499.00, 599.00, 100, 'ULD-BK-M'),
(23, 7, '{"颜色": "黑色", "尺码": "L"}', 499.00, 599.00, 90, 'ULD-BK-L'),
(24, 7, '{"颜色": "深蓝", "尺码": "M"}', 499.00, 599.00, 70, 'ULD-NB-M');

-- 商品8: 海尔冰箱
INSERT INTO `product` (`id`, `name`, `description`, `brand_id`, `category_id`, `images`, `status`) VALUES
(8, 'Haier 海尔 500L 十字对开门冰箱', '一级能效，双变频，风冷无霜，抗菌净味，智能保鲜。', 7, 301, '["https://picsum.photos/seed/haier-1/600/600"]', 1);

INSERT INTO `product_sku` (`id`, `product_id`, `specs`, `price`, `original_price`, `stock`, `sku_code`) VALUES
(25, 8, '{"颜色": "星蕴银", "容量": "500L"}', 4299.00, 4599.00, 20, 'HR-500-SV');

-- 商品9: 联想 ThinkBook 14
INSERT INTO `product` (`id`, `name`, `description`, `brand_id`, `category_id`, `images`, `status`) VALUES
(9, 'Lenovo ThinkBook 14 2024 款', '13 代酷睿 i5，16GB 内存，512GB SSD，14 英寸 2.8K 高刷屏。', 4, 201, '["https://picsum.photos/seed/tb14-1/600/600"]', 1);

INSERT INTO `product_sku` (`id`, `product_id`, `specs`, `price`, `original_price`, `stock`, `sku_code`) VALUES
(26, 9, '{"颜色": "星际灰", "配置": "i5/16G/512G"}', 4999.00, 5299.00, 40, 'TB14-I5-16-512'),
(27, 9, '{"颜色": "星际灰", "配置": "i7/32G/1TB"}', 6999.00, 7299.00, 25, 'TB14-I7-32-1TB');

-- 商品10: Redmi Note 13
INSERT INTO `product` (`id`, `name`, `description`, `brand_id`, `category_id`, `images`, `status`) VALUES
(10, 'Xiaomi Redmi Note 13 5G 手机', '骁龙 695，6.67 英寸 AMOLED，108MP 三摄，67W 快充，5000mAh 电池。', 2, 101, '["https://picsum.photos/seed/rn13-1/600/600"]', 1);

INSERT INTO `product_sku` (`id`, `product_id`, `specs`, `price`, `original_price`, `stock`, `sku_code`) VALUES
(28, 10, '{"颜色": "黑色", "存储": "128GB"}', 1299.00, 1499.00, 200, 'RN13-BK-128'),
(29, 10, '{"颜色": "黑色", "存储": "256GB"}', 1499.00, 1699.00, 150, 'RN13-BK-256'),
(30, 10, '{"颜色": "绿色", "存储": "256GB"}', 1499.00, 1699.00, 100, 'RN13-GR-256');

-- ==================== 评价种子（只读展示，Day1 不做提交）====================
INSERT INTO `product_review` (`id`, `user_id`, `product_id`, `sku_id`, `order_id`, `rating`, `content`, `images`) VALUES
(1, 999, 1, 1, NULL, 5, '手感很好，拍照清晰，灵动岛交互很有趣', '["https://picsum.photos/seed/r1/300/300"]'),
(2, 998, 1, 2, NULL, 4, '续航一般，但整体体验不错，升级感明显', NULL),
(3, 997, 3, 8, NULL, 5, '徕卡镜头拍照真的很棒，快充速度惊人', '["https://picsum.photos/seed/r3/300/300"]'),
(4, 999, 4, 11, NULL, 5, 'M3 芯片性能强劲，屏幕素质一流', NULL);

-- ==================== 优惠券种子 ====================
INSERT INTO `coupon` (`id`, `name`, `type`, `value`, `min_order_amount`, `start_time`, `end_time`, `total_count`, `remaining_count`) VALUES
(1, '新用户立减 30', 0, 30.00, 100.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 10000, 10000),
(2, '满 500 减 80', 0, 80.00, 500.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 5000, 5000),
(3, '满 2000 打 85 折', 1, 0.85, 2000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 2000, 2000),
(4, '手机数码专属券 满 3000 减 300', 0, 300.00, 3000.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1000, 1000);

-- ==================== 秒杀活动种子 ====================
INSERT INTO `flash_sale_activity` (`id`, `name`, `start_time`, `end_time`, `status`) VALUES
(1, '今日秒杀', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1);

INSERT INTO `flash_sale_item` (`id`, `activity_id`, `product_id`, `sku_id`, `flash_price`, `total_stock`, `available_stock`, `limit_per_user`) VALUES
(1, 1, 10, 28, 999.00, 100, 100, 1),
(2, 1, 2, 7, 1299.00, 50, 50, 1),
(3, 1, 6, 16, 599.00, 80, 80, 2);
