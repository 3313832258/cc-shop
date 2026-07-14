# CC-Shop 电商网站实现方案

## Context

从零搭建一个类京东的电商网站，需满足：高并发（上百流量）、非京东风格 UI、模拟支付、物流追踪。
技术栈：Vue3+Nuxt3 前端 / Spring Cloud 微服务后端 / MySQL+Redis / 沙箱支付 / Nacos 注册+配置中心 / Sentinel 流控 / Seata 分布式事务。

**项目定位**：简历/学习项目，以技术展示为目的。"上百流量"本身用单体 Spring Boot 即可承载，此处选用全套微服务 + Seata/Sentinel/MQ 是为覆盖高并发与分布式场景的实践，而非流量驱动的必要选型。核心功能：用户系统、SKU多规格商品、购物车、下单、沙箱支付、物流追踪、促销+优惠券、收藏、售后退款、消息通知、后台管理系统、用户行为埋点（为后续 ML 推荐做准备）。

---

## 整体架构

```
                    [ Nuxt3 SSR 前端 ]       [ Nuxt3 Admin 后台 ]
                          │                        │
                    [ Spring Cloud Gateway ]  ← 统一入口、路由、限流、鉴权
                          │
     ┌──────┬──────┬──────┼──────┬──────┬──────┬──────┐
     │      │      │      │      │      │      │      │
  cc-user cc-product cc-cart cc-order cc-payment cc-promotion cc-logistics
     │      │      │      │      │      │      │
     └──┬───┴──┬───┘      │      │      │      │
        │      │          │      │      │      │
    [ MySQL ] [ Redis ] [ Seata分布式事务 ] [ Nacos ]
```

### 基础设施组件

| 组件 | 选型 | 说明 |
|------|------|------|
| 注册+配置中心 | **Nacos** | 国内主流，服务注册+动态配置 |
| API 网关 | **Spring Cloud Gateway** | 路由、鉴权、限流、跨域 |
| 流控/熔断 | **Sentinel** | 限流、熔断降级、热点参数限流（秒杀场景） |
| 服务调用 | **OpenFeign** | 声明式 HTTP，拦截器传递 JWT |
| 分布式事务 | **Seata (AT模式)** | 下单跨服务一致性（扣库存+创订单走 AT 全局事务；清购物车在 Redis，AT 无法管，单独补偿/异步清理） |
| 消息队列 | **RabbitMQ** | 异步通知（支付成功通知、物流更新推送）、削峰（秒杀） |

---

## 项目结构

### 前端 `cc-shop-web/`（用户端）
```
cc-shop-web/
├── nuxt.config.ts
├── package.json
├── app.vue
├── pages/
│   ├── index.vue              # 首页
│   ├── product/
│   │   ├── list.vue           # 商品列表/搜索
│   │   ├── [id].vue           # 商品详情（含 SKU 选择）
│   ├── cart.vue               # 购物车
│   ├── order/
│   │   ├── create.vue         # 下单页
│   │   ├── list.vue           # 订单列表
│   │   ├── [id].vue           # 订单详情+物流
│   ├── payment/
│   │   ├── [orderId].vue      # 支付页（沙箱模拟）
│   ├── favorites.vue          # 我的收藏
│   ├── coupon/
│   │   ├── list.vue           # 我的优惠券
│   │   ├── center.vue         # 优惠券领取中心
│   ├── aftersale/
│   │   ├── apply.vue          # 申请售后
│   │   ├── list.vue           # 售后记录
│   ├── message.vue            # 消息中心
│   ├── user/
│   │   ├── login.vue
│   │   ├── register.vue
│   │   ├── profile.vue
│   │   ├── address.vue
├── components/
│   ├── layout/
│   │   ├── AppHeader.vue      # 含搜索+购物车图标+用户头像
│   │   ├── AppFooter.vue
│   ├── product/
│   │   ├── ProductCard.vue
│   │   ├── ProductGallery.vue
│   │   ├── ProductSkuSelector.vue  # SKU 规格选择器
│   │   ├── CategoryNav.vue
│   │   ├── ProductSpecs.vue         # 商品参数表
│   ├── order/
│   │   ├── OrderCard.vue
│   │   ├── LogisticsTimeline.vue
│   │   ├── AftersaleForm.vue
│   ├── cart/
│   │   ├── CartItem.vue
│   │   ├── CartSummary.vue
│   │   ├── CouponSelector.vue       # 下单时选优惠券
│   ├── payment/
│   │   ├── PaymentForm.vue
│   │   ├── PaymentResult.vue
│   ├── promotion/
│   │   ├── FlashSaleBanner.vue      # 限时抢购 Banner
│   │   ├── CouponCard.vue
│   │   ├── CouponCenter.vue
│   ├── common/
│   │   ├── SearchBar.vue
│   │   ├── Pagination.vue
│   │   ├── Rating.vue
│   │   ├── Toast.vue
│   │   ├── MessageBadge.vue         # 消息未读数徽章
│   │   ├── FavoriteButton.vue       # 收藏按钮
├── composables/
│   ├── useAuth.ts
│   ├── useCart.ts
│   ├── useProduct.ts
│   ├── useOrder.ts
│   ├── usePayment.ts
│   ├── useCoupon.ts                 # 优惠券逻辑
│   ├── useFavorites.ts              # 收藏逻辑
│   ├── useMessage.ts                # 消息通知
│   ├── useApi.ts                    # 统一 API（指向 Gateway）
├── stores/
│   ├── auth.ts
│   ├── cart.ts
│   ├── product.ts
│   ├── order.ts
│   ├── message.ts
├── middleware/
│   ├── auth.ts
├── assets/
│   ├── css/
│   │   ├── main.css
│   │   ├── variables.css
│   ├── images/
├── utils/
│   ├── format.ts
│   ├── validators.ts
│   ├── tracker.ts                   # 用户行为埋点工具
├── types/
│   ├── product.ts                   # 含 SKU 类型
│   ├── order.ts
│   ├── user.ts
│   ├── payment.ts
│   ├── logistics.ts
│   ├── coupon.ts
│   ├── aftersale.ts
│   ├── message.ts
│   ├── sku.ts
```

### Admin 后台 `cc-shop-admin/`（独立 Nuxt3 项目）
```
cc-shop-admin/
├── nuxt.config.ts
├── pages/
│   ├── index.vue               # Dashboard（订单/销售额统计）
│   ├── product/
│   │   ├── list.vue            # 商品管理（含 SKU 编辑）
│   │   ├── create.vue          # 新增商品
│   │   ├── [id]/edit.vue       # 编辑商品
│   │   ├── category.vue        # 分类管理
│   │   ├── brand.vue           # 品牌管理
│   ├── order/
│   │   ├── list.vue            # 订单管理
│   │   ├── [id].vue            # 订单详情+发货操作
│   ├── promotion/
│   │   ├── flash-sale.vue      # 限时抢购管理
│   │   ├── coupon.vue          # 优惠券管理
│   ├── user/
│   │   ├── list.vue            # 用户管理
│   ├── aftersale/
│   │   ├── list.vue            # 售后申请处理
│   ├── logistics/
│   │   ├── list.vue            # 物流管理
│   ├── message/
│   │   ├── list.vue            # 系统消息推送
│   ├── login.vue               # Admin 登录
├── components/
│   ├── admin/
│   │   ├── AdminSidebar.vue
│   │   ├── AdminHeader.vue
│   │   ├── StatCard.vue        # 统计卡片
│   │   ├── SkuEditor.vue       # SKU 规格编辑器
│   │   ├── ImageUploader.vue   # 图片上传
│   │   ├── DataTable.vue       # 通用数据表格
├── composables/
│   ├── useAdminApi.ts
│   ├── useDashboard.ts
```

### 后端微服务 `cc-shop-cloud/`
```
cc-shop-cloud/
├── pom.xml                          # 父 POM（统一版本管理）
├── cc-common/                       # 公共模块
│   ├── Result.java, PageResult.java, BusinessException.java
│   ├── JwtUtil.java, FeignConfig.java（传递JWT）
│   ├── enums/ (OrderStatus, PaymentStatus, LogisticsStatus, AftersaleStatus, CouponStatus)
│   ├── tracker/ UserActionEvent.java  # 埋点事件定义
├── cc-gateway/                      # API 网关
│   ├── AuthGlobalFilter.java        # JWT 鉴权
│   ├── RateLimitFilter.java         # Sentinel 限流
│   ├── CorsConfig.java, SentinelConfig.java
│   ├── routes.yml                   # 路由规则
├── cc-user/                         # 用户+地址+收藏+消息服务
│   ├── AuthController, UserController, AddressController
│   ├── FavoriteController, MessageController
│   ├── entity/ User, Address, Favorite, Message, UserActionLog(埋点)
│   ├── service/ AuthService, UserService, FavoriteService, MessageService
│   ├── client/ (Feign 调其他服务)
├── cc-product/                      # 商品+SKU+分类+品牌+参数服务
│   ├── ProductController, CategoryController, BrandController, SearchController
│   ├── entity/ Product, ProductSku, Category, Brand, ProductSpec, ProductImage
│   ├── cache/ ProductCache, HotProductCache
│   ├── client/ UserFeignClient
├── cc-cart/                         # 购物车服务（Redis为主）
│   ├── CartController
│   ├── entity/ (Redis Hash，无MySQL表)
├── cc-order/                        # 订单+售后服务
│   ├── OrderController, AftersaleController
│   ├── entity/ Order, OrderItem, AftersaleRecord
│   ├── task/ OrderTimeoutTask
│   ├── client/ ProductFeignClient, CartFeignClient, UserFeignClient, PaymentFeignClient
├── cc-payment/                      # 支付服务（沙箱）
│   ├── PaymentController
│   ├── entity/ PaymentRecord
│   ├── client/ OrderFeignClient
├── cc-promotion/                    # 促销+优惠券服务
│   ├── FlashSaleController, CouponController
│   ├── entity/ FlashSaleActivity, FlashSaleItem, Coupon, UserCoupon
│   ├── cache/ FlashSaleCache（Redis 热点数据）
│   ├── client/ OrderFeignClient, ProductFeignClient
├── cc-logistics/                    # 物流服务（独立部署）
│   ├── LogisticsController
│   ├── entity/ LogisticsRecord, LogisticsStep
│   ├── task/ LogisticsSimulator
│   ├── client/ OrderFeignClient
├── docker/
│   ├── docker-compose.yml           # 全栈部署（Nacos+MySQL+Redis+RabbitMQ+Seata+各服务）
│   ├── mysql/init/ V1__init_schema.sql, V2__init_data.sql
├── sql/
│   ├── V1__init_schema.sql
│   ├── V2__init_data.sql            # 种子数据（含 SKU 示例）
```

---

## 数据库设计

### 用户域
| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `user` | 用户 | id, username, password_hash, email, phone, avatar, created_at |
| `address` | 收货地址 | id, user_id, province, city, district, detail, phone, is_default |
| `favorite` | 收藏 | id, user_id, product_id, created_at |
| `message` | 消息通知 | id, user_id, type(order/promotion/system), title, content, is_read, created_at |
| `user_action_log` | 行为埋点 | id, user_id, action(view/click/cart/order), target_type, target_id, extra(JSON), created_at |

### 商品域
| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `category` | 分类 | id, name, parent_id, level, sort_order, icon |
| `brand` | 品牌 | id, name, logo, description |
| `product` | 商品主表 | id, name, description, brand_id, category_id, images(JSON), status, created_at |
| `product_sku` | SKU规格 | id, product_id, specs(JSON如{"颜色":"红","尺码":"XL"}), price, original_price, stock, sku_code, image |
| `product_spec` | 商品参数 | id, product_id, spec_name, spec_value (如"屏幕尺寸: 6.1英寸") |
| `product_image` | 商品图片 | id, product_id, url, sort, is_main |
| `product_review` | 评价 | id, user_id, product_id, sku_id, order_id, rating, content, images(JSON), created_at |

### 订单域
| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `order` | 订单 | id, user_id, order_no, total_amount, discount_amount, final_amount, coupon_id, status, address_snapshot(JSON), created_at, paid_at |
| `order_item` | 订单明细 | id, order_id, product_id, sku_id, product_name, sku_specs(JSON), product_image, price, quantity |
| `aftersale_record` | 售后记录 | id, order_id, order_item_id, user_id, type(refund/return_refund), reason, status, amount, created_at |

### 支付域
| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `payment_record` | 支付记录 | id, order_id, payment_no, amount, method, status, paid_at |

### 促销域
| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `flash_sale_activity` | 限时抢购活动 | id, name, start_time, end_time, status |
| `flash_sale_item` | 抢购商品 | id, activity_id, product_id, sku_id, flash_price, total_stock, available_stock, limit_per_user |
| `coupon` | 优惠券模板 | id, name, type(fixed/percent), value, min_order_amount, start_time, end_time, total_count, remaining_count |
| `user_coupon` | 用户优惠券 | id, user_id, coupon_id, status(available/used/expired), used_order_id, created_at |

### 物流域
| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `logistics_record` | 物流记录 | id, order_id, carrier, tracking_no, status, current_location, estimated_delivery |
| `logistics_step` | 物流步骤 | id, logistics_id, description, location, timestamp |

---

## 高并发方案

1. **Gateway + Sentinel**
   - 统一限流：登录5/min/IP，下单10/min/user，搜索30/min/IP
   - **秒杀场景**：热点参数限流（ Sentinel `ParamFlowRule`），抢购接口按商品ID限流
   - 熔断降级：下游故障时返回默认数据

2. **Redis 缓存**
   - 商品详情缓存（TTL 30min）
   - 分类/品牌列表缓存
   - 购物车 Redis 存储
   - **秒杀库存**：`flash_sale_stock:{itemId}` Redis 预扣，Lua脚本保证原子性
   - 订单号 Redis 自增序列

3. **RabbitMQ 异步**
   - 支付成功 → MQ 通知订单服务更新状态
   - 物流更新 → MQ 推送消息给用户
   - 秒杀请求 → MQ 削峰（请求先入队列，异步处理）
   - 埋点日志 → MQ 收集后批量写入
   - **可靠性保证**：消费端幂等（按业务唯一键去重，如 `payment_no`/`order_id+step`）；生产端用本地消息表（outbox）保证业务写库与发消息同事务，防丢消息、防重复消费

4. **Seata 分布式事务**
   - 下单流程（AT模式）：扣库存(product) + 创订单(order) 走全局事务；**清购物车在 Redis，AT 无法纳入**，下单事务提交后异步清理购物车，失败走补偿/重试
   - **防超卖**：AT 只保证一致性不防并发超扣，扣库存用条件更新 `UPDATE product_sku SET stock=stock-n WHERE stock>=n`，依赖 affected rows 判断，秒杀场景改走 Redis 预扣
   - TCC 模式备选：秒杀场景用 TCC 更精确控制

5. **数据库优化**
   - HikariCP 20连接/service
   - MySQL FULLTEXT 搜索（中文须 `WITH PARSER ngram`，否则默认分词器对中文无效）
   - 读写分离（后续可加）

6. **前端优化**
   - Nuxt3 SSR
   - **SSR 下 Token 处理**：JWT 存 HttpOnly Cookie，服务端 `useRequestHeaders` 透传 cookie 给 Gateway；客户端走 cookie 自动携带；避免 localStorage 在 SSR 取不到 token 的问题
   - Pinia stale-while-revalidate
   - 图片懒加载
   - 购物车乐观更新

---

## SKU 规格体系设计

商品 → 多个 SKU，每个 SKU 是最小可购买单元：

```
Product: iPhone 15
  ├── SKU1: 颜色=黑, 尺码=128G  → ¥5999, 库存100
  ├── SKU2: 颜色=黑, 尺码=256G  → ¥6999, 库存50
  ├── SKU3: 颜色=蓝, 尺码=128G  → ¥5999, 库存80
  ├── SKU4: 颜色=蓝, 尺码=256G  → ¥6999, 库存30
```

- `product_sku.specs` 用 JSON 存储：`{"颜色":"黑","存储":"128G"}`
- 前端 `ProductSkuSelector.vue`：规格组合选择器，选中组合定位到具体 SKU
- 下单时传 `sku_id`，订单明细记录 `sku_specs`

---

## 促销+优惠券设计

### 限时抢购（秒杀）
1. Admin 创建抢购活动 + 抢购商品（设置闪购价、库存、限购数量）
2. 开抢前：抢购数据预加载到 Redis（库存、限购计数）
3. 抢购请求：Gateway → Sentinel 热点限流 → MQ 削峰 → Order 服务异步处理
4. Redis Lua 脚本原子操作：检查限购 + 扣库存
5. 成功后 Seata 事务：创订单 + 扣 product 库存 + 扣 flash_sale 库存

### 优惠券
1. 两种类型：固定金额（满100减20）、百分比折扣（满200打8折）
2. 优惠券领取中心：用户领取 → `user_coupon` 状态=available
3. 抢券并发控制：领取为高并发场景，与秒杀同构，`coupon.remaining_count` 用 Redis 预扣 + Lua 原子扣减，DB 异步落库，防超发
4. 下单时选择优惠券 → `CouponSelector.vue`
5. 后端校验：金额门槛、有效期、是否已使用
6. 订单记录 `coupon_id` + `discount_amount`

---

## 沙箱支付设计

1. 选择"支付宝"或"微信支付"
2. 弹出模拟支付界面
3. PaymentService 模拟：3秒延迟、95%成功/5%失败
4. 成功 → MQ 通知订单服务 → 更新状态 PAID → 通知物流创建记录 → 推送消息给用户
5. 失败 → 标记 FAILED，可重新支付
6. 30分钟超时自动取消

---

## 物流追踪设计

1. 支付成功 → MQ 通知 → LogisticsSimulator 创建物流记录
2. 每2分钟模拟步骤：已下单→已发货→运输中→派送中→已签收
3. 每更新一步 → MQ 推送消息给用户
4. 前端 `LogisticsTimeline.vue` 时间线

---

## 售后退款设计

1. 用户在订单详情页申请售后（选退款/退货退款，填原因）
2. `AftersaleRecord` 状态：PENDING → APPROVED/REJECTED → PROCESSING → COMPLETED
3. Admin 后台审批售后申请
4. 退款模拟：审批通过后自动退款（沙箱模式，模拟3秒处理）

---

## 消息通知设计

1. `Message` 表存储用户消息，类型：order（订单状态变更）、promotion（促销通知）、system（系统公告）
2. 触发点：支付成功、发货、物流更新、售后审批、优惠券到期提醒
3. 通过 RabbitMQ 异步推送 → cc-user 服务写入 Message
4. 前端 Header 消息徽章 + 消息中心页面
5. 可扩展 WebSocket 实时推送

---

## 用户行为埋点（为 ML 推荐做准备）

1. `UserActionLog` 记录：浏览(view)、点击(click)、加购(cart)、下单(order)、收藏(favorite)
2. 前端 `tracker.ts`：页面访问、商品点击、搜索关键词自动上报
3. 上报 → Gateway → MQ → cc-user 批量写入
4. 后续 ML 推荐服务可直接读取这些数据做协同过滤
5. **数据边界**：埋点为分析域、数据量大且持续增长，与用户核心域（登录/地址/收藏/消息）混在 `cc-user` 会成热点表。建议 `user_action_log` 单独表 + 定期归档到冷存；如规模进一步增长，拆 `cc-tracker` 独立服务

---

## UI 风格（非京东风格）

- **配色**：深青蓝 `#1a73e8` + 柔灰 `#f5f5f5`，促销用暖橙 `#ff6d00`
- **布局**：卡片式 Grid，不搞密集分类墙
- **首页**：大 Hero Banner + 推荐卡片 + 限时抢购专区 + 优惠券领取入口
- **商品详情**：左图右详情，SKU 选择器 + 参数表 Tab，评价 Tab
- **导航**：简洁顶部栏 + 消息徽章 + 收藏入口
- **Admin**：侧边栏 + 数据表格 + 统计 Dashboard，配色同系但偏深色
- **整体**：Shopify/Zara 简洁质感

```css
:root {
  --primary: #1a73e8;
  --primary-light: #e8f0fe;
  --accent: #ff6d00;
  --bg: #f5f5f5;
  --surface: #ffffff;
  --text: #202124;
  --text-secondary: #5f6368;
  --border: #dadce0;
  --radius: 8px;
  --shadow: 0 1px 3px rgba(0,0,0,0.1);
}
```

---

## 实现步骤

### 阶段 1：基础设施 + 公共模块
1. 父 POM + cc-common（Result, JwtUtil, FeignConfig, enums）
2. Docker 启动 Nacos + MySQL + Redis + RabbitMQ + Seata
3. 数据库建表脚本 + 种子数据（含 SKU 示例）
4. cc-gateway（路由 + JWT鉴权 + Sentinel限流 + CORS）

### 阶段 2：用户服务 + 前端骨架
1. cc-user（注册/登录/信息/地址/收藏/消息/埋点）
2. Nuxt3 前端项目初始化 + 全局样式 + 布局组件
3. 登录/注册/个人信息/地址管理/收藏/消息页面

### 阶段 3：商品服务 + SKU
1. cc-product（商品+SKU+分类+品牌+参数+搜索+缓存）
2. 前端分类导航 + 商品列表 + 详情页（SKU选择器+参数表）
3. 首页（Banner + 推荐 + 分类卡片）

### 阶段 4：购物车 + 优惠券
1. cc-cart（Redis 存储）
2. cc-promotion（优惠券模板+领取+校验）
3. 前端购物车页 + 优惠券领取中心 + 下单选券

### 阶段 5：订单 + 支付 + 物流
1. cc-order（下单+Seata事务+超时取消+售后）
2. cc-payment（沙箱支付 + MQ通知）
3. cc-logistics（物流模拟器）
4. 前端下单页 + 订单列表/详情 + 支付页 + 物流时间线 + 售后申请

### 阶段 6：限时抢购（秒杀）
1. cc-promotion 限时抢购功能（Redis预扣+MQ削峰+Sentinel热点限流）
2. 前端秒杀专区 + 抢购倒计时 + 抢购按钮

### 阶段 7：Admin 后台
1. cc-shop-admin 项目（Dashboard + 商品管理含SKU编辑 + 订单管理 + 售后审批 + 促销管理 + 优惠券管理 + 用户管理 + 消息推送）

### 阶段 8：完善 + 部署
1. 前端 SEO / 错误处理 / Toast / 移动端适配
2. 行为埋点完善
3. Docker Compose 全栈部署
4. Sentinel 规则持久化到 Nacos

---

## 验证方式

1. **功能验证**：走通完整流程（注册→浏览→收藏→加购→选券→下单→支付→看物流→申请售后）
2. **SKU验证**：商品详情页选不同规格，价格/库存/图片联动正确
3. **秒杀验证**：限时抢购场景，Redis原子扣库存、限购生效、MQ削峰正常
4. **优惠券验证**：领取→下单使用→金额门槛校验→已使用不可再用
5. **并发验证**：JMeter 100并发压测商品列表、下单、秒杀接口
6. **分布式事务验证**：下单成功后库存+订单+购物车三者一致；下单失败全部回滚
7. **微服务验证**：Nacos控制台各服务注册成功；停某服务后 Sentinel 熔断生效
8. **Admin验证**：后台管理全流程（新增含SKU商品→创建抢购→审批售后→推送消息）
