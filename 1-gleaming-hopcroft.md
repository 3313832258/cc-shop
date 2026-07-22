# CC-Shop 电商网站实现方案

## Context

从零搭建一个类京东的电商网站，需满足：高并发（上百流量）、非京东风格 UI、模拟支付、物流追踪。
技术栈：Vue3+Nuxt3 前端 / Spring Cloud 微服务后端 / MySQL+Redis / 沙箱支付 / Nacos 注册+配置中心 / Sentinel 流控 / Seata 分布式事务。

**项目定位**：简历/学习项目，以技术展示为目的。"上百流量"本身用单体 Spring Boot 即可承载，此处选用全套微服务 + Seata/Sentinel/MQ 是为覆盖高并发与分布式场景的实践，而非流量驱动的必要选型。核心功能：用户系统、SKU多规格商品、购物车、下单、沙箱支付、物流追踪、促销+优惠券、收藏、售后退款、消息通知、后台管理系统、用户行为埋点（为后续 ML 推荐做准备）。

**当前进度**：阶段 1~4 已完成（用户/商品/购物车/优惠券/下单/支付/物流/售后/秒杀/Admin），阶段 5 待实现（完善+部署）。

---

## 整体架构

```
                    [ Nuxt3 SPA 前端 ]       [ Nuxt3 Admin 后台 ]
                          │                        │
                    [ Spring Cloud Gateway ]  ← 统一入口、路由、限流、鉴权
                          │
     ┌──────┬──────┬──────┼──────┬──────┐
     │      │      │      │      │      │
  cc-user cc-product cc-trade cc-promotion cc-shop-admin
  (8081)   (8082)    (8083)   (8085)      (8086)
     │      │      │      │      │
     └──┬───┴──┬───┘      │      │      │
        │      │          │      │      │
    [ MySQL ] [ Redis ] [ RabbitMQ ] [ Nacos ]
```

### 基础设施组件

| 组件 | 选型 | 说明 |
|------|------|------|
| 注册+配置中心 | **Nacos 2.2.3** | 国内主流，服务注册+动态配置 |
| API 网关 | **Spring Cloud Gateway** | 路由、鉴权、限流、跨域 |
| 流控/熔断 | **Sentinel** | 限流、熔断降级、热点参数限流（秒杀场景） |
| 服务调用 | **OpenFeign** | 声明式 HTTP，拦截器传递 JWT |
| 分布式事务 | **本地事务+MQ补偿** | 已降级 Seata AT，改用本地事务+MQ 最终一致性 |
| 消息队列 | **RabbitMQ 3.13** | 异步通知（支付成功通知、物流更新推送）、削峰（秒杀） |
| ORM | **MyBatis-Plus 3.5.5** | 逻辑删除、自动填充、分页插件 |
| 缓存 | **Redis 7** | 购物车、商品缓存、秒杀库存 |

---

## 项目结构

### 前端 `cc-shop-web/`（用户端，SPA 模式）

```
cc-shop-web/
├── nuxt.config.ts
├── package.json
├── app.vue                        # UApp 包装器
├── app.config.ts                  # Nuxt UI 主题配置
├── vitest.config.ts               # 测试配置
├── test/                          # 测试目录
│   ├── setup.ts                   # 测试环境 setup
│   └── utils.ts                   # 测试工具函数
├── pages/
│   ├── index.vue                  # 首页
│   ├── product/
│   │   ├── list.vue               # 商品列表/搜索
│   │   └── [id].vue               # 商品详情（含 SKU 选择）
│   ├── cart.vue                   # 购物车
│   ├── coupon.vue                 # 优惠券中心
│   ├── favorites.vue              # 我的收藏
│   ├── message.vue                # 消息中心
│   ├── order/
│   │   ├── checkout.vue           # 下单页
│   │   ├── pay.vue                # 支付页
│   │   ├── list.vue               # 订单列表
│   │   ├── [id].vue               # 订单详情
│   │   ├── logistics.vue          # 物流时间线
│   │   ├── aftersale.vue          # 售后申请
│   │   └── review.vue             # 评价
│   ├── promotion/
│   │   └── flash.vue              # 秒杀专区
│   └── user/
│       ├── login.vue
│       ├── register.vue
│       ├── profile.vue
│       ├── address.vue            # 收货地址管理
│       └── forgot-password.vue    # 忘记密码
├── components/                    # 组件直接放根目录（避免前缀问题）
│   ├── AppHeader.vue              # 含搜索+购物车图标+用户头像
│   ├── AppFooter.vue
│   ├── ProductCard.vue
│   ├── ProductSkuSelector.vue     # SKU 规格选择器
│   ├── CouponCard.vue
│   ├── AddressSelector.vue        # 收货地址选择器
│   ├── SeckillCard.vue            # 秒杀倒计时组件
│   └── ToastContainer.vue
├── composables/
│   ├── useApi.ts                  # 统一 API（指向 Gateway）
│   └── useToast.ts                # Toast 兼容层
├── stores/
│   └── auth.ts                    # Pinia 认证 store
├── middleware/
│   └── auth.ts                    # 路由守卫
├── assets/
│   └── css/
│       └── main.css               # Tailwind + Nuxt UI 入口
└── plugins/
    └── auth.client.ts             # 客户端认证初始化
```

### 前端 `cc-shop-admin/`（管理后台，SPA 模式）

```
cc-shop-admin/
├── nuxt.config.ts
├── package.json
├── app.vue
├── pages/
│   ├── login.vue                  # 管理员登录
│   ├── index.vue                  # Dashboard（统计卡片）
│   ├── product/
│   │   └── index.vue              # 商品管理（CRUD + SKU）
│   ├── order/
│   │   └── index.vue              # 订单管理（列表 + 发货）
│   ├── aftersale/
│   │   └── index.vue              # 售后审批（通过/拒绝）
│   ├── coupon/
│   │   └── index.vue              # 优惠券管理
│   ├── customer/
│   │   └── index.vue              # 客户管理
│   └── merchant/
│       └── index.vue              # 商家管理
└── ...
```

### 后端微服务 `cc-shop-cloud/`

```
cc-shop-cloud/
├── pom.xml                          # 父 POM（Spring Boot 3.2.5 + Spring Cloud 2023.0.1）
├── cc-common/                       # 公共模块
│   ├── Result.java                  # 统一返回结构
│   ├── JwtUtil.java                 # JWT 工具
│   ├── FeignConfig.java             # Feign 拦截器传递 JWT
│   ├── UserContext.java             # ThreadLocal 用户上下文
│   ├── WebMvcConfig.java            # MVC 配置
│   ├── tracker/
│   │   └── UserActionEvent.java     # 用户行为埋点事件
│   └── enums/                       # 枚举：OrderStatus, PaymentStatus, CouponType, AftersaleStatus 等
├── cc-gateway/                      # API 网关 (8080)
│   ├── AuthGlobalFilter.java        # JWT 鉴权
│   ├── application.yml              # 路由配置
│   └── CorsConfig.java
├── cc-user/                         # 用户服务 (8081)
│   ├── controller/
│   │   ├── AuthController.java      # 注册/登录
│   │   ├── UserController.java      # 用户信息
│   │   ├── AddressController.java   # 收货地址
│   │   ├── FavoriteController.java  # 收藏
│   │   └── MessageController.java   # 消息
│   ├── entity/
│   ├── service/
│   │   ├── UserService.java
│   │   ├── AddressService.java
│   │   ├── FavoriteService.java
│   │   ├── MessageService.java
│   │   ├── CaptchaService.java      # 验证码
│   │   ├── SmsService.java          # 短信
│   │   ├── LoginLogService.java     # 登录日志
│   │   └── UserActionLogService.java # 用户行为日志
│   └── mapper/
├── cc-product/                      # 商品服务 (8082)
│   ├── controller/
│   │   ├── ProductController.java
│   │   ├── CategoryController.java
│   │   ├── BrandController.java
│   │   └── SearchController.java
│   ├── entity/
│   │   ├── Product.java
│   │   ├── ProductSku.java
│   │   └── Category.java
│   └── service/
├── cc-trade/                        # 交易服务 (8083)
│   ├── controller/
│   │   ├── CartController.java      # 购物车
│   │   ├── OrderController.java     # 订单
│   │   ├── PaymentController.java   # 支付
│   │   ├── LogisticsController.java # 物流
│   │   └── AftersaleController.java # 售后
│   ├── service/
│   │   ├── CartService.java
│   │   ├── OrderService.java
│   │   ├── PaymentService.java
│   │   ├── LogisticsService.java
│   │   ├── LogisticsSimulator.java  # 物流模拟器
│   │   └── AftersaleService.java
│   ├── entity/
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── PaymentRecord.java
│   │   ├── LogisticsRecord.java
│   │   ├── LogisticsStep.java
│   │   └── AftersaleRecord.java
│   ├── mq/
│   │   ├── RabbitMQConfig.java      # MQ 配置
│   │   ├── OrderTimeoutConsumer.java # 订单超时取消
│   │   └── PaymentNotifyConsumer.java # 支付成功通知
│   └── feign/
│       ├── ProductClient.java
│       ├── PromotionClient.java
│       └── UserClient.java
├── cc-promotion/                    # 促销服务 (8085)
│   ├── controller/
│   │   ├── CouponController.java    # 优惠券
│   │   ├── FlashSaleController.java # 秒杀
│   │   └── InternalCouponController.java # 内部 Feign 接口
│   ├── service/
│   │   ├── CouponService.java
│   │   └── FlashSaleService.java
│   ├── entity/
│   │   ├── Coupon.java
│   │   ├── UserCoupon.java
│   │   ├── FlashSaleActivity.java
│   │   └── FlashSaleItem.java
│   ├── mq/
│   │   ├── RabbitMQConfig.java
│   │   └── CouponUseConsumer.java   # 优惠券核销消费者
│   ├── feign/
│   │   └── ProductClient.java
│   └── config/
│       └── RedisDataLoader.java     # Redis 数据预热
├── cc-shop-admin/                   # 管理后台 (8086)
│   ├── AdminApplication.java
│   ├── controller/
│   │   ├── DashboardController.java # 仪表盘统计
│   │   ├── AdminAuthController.java # 管理员登录
│   │   ├── AdminProductController.java # 商品 CRUD
│   │   ├── AdminOrderController.java # 订单管理
│   │   ├── AdminAftersaleController.java # 售后审批
│   │   ├── AdminCouponController.java # 优惠券管理
│   │   └── AdminCustomerController.java # 客户管理
│   ├── service/
│   │   ├── DashboardService.java
│   │   ├── AdminProductService.java
│   │   ├── AdminOrderService.java
│   │   ├── AdminAftersaleService.java
│   │   ├── AdminCouponService.java
│   │   └── AdminCustomerService.java
│   └── dto/
│       └── DashboardVO.java
├── docker/
│   ├── infra.yml                    # Docker Compose：MySQL + Redis + RabbitMQ + Nacos + Seata
│   └── mysql/init/                  # 初始化 SQL
```

---

## 数据库设计

### 用户域
| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `user` | 用户 | id, username, password_hash, email, phone, created_at |
| `address` | 收货地址 | id, user_id, receiver_name, phone, province, city, district, detail, is_default |
| `favorite` | 收藏 | id, user_id, product_id, created_at |
| `message` | 消息通知 | id, user_id, type, title, content, is_read, created_at |

### 商品域
| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `category` | 分类 | id, name, parent_id, level, sort_order |
| `brand` | 品牌 | id, name, logo |
| `product` | 商品主表 | id, name, description, brand_id, category_id, main_image, status |
| `product_sku` | SKU规格 | id, product_id, specs(JSON), price, original_price, stock, sku_code, image |
| `product_spec` | 商品参数 | id, product_id, spec_name, spec_value |
| `product_review` | 评价（只读） | id, user_id, product_id, sku_id, order_id, rating, content, images |

### 订单域
| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `order` | 订单 | id, user_id, order_no, total_amount, discount_amount, final_amount, coupon_id, status, address_snapshot(JSON), created_at, paid_at |
| `order_item` | 订单明细 | id, order_id, product_id, sku_id, product_name, sku_specs(JSON), price, quantity |

### 支付域
| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `payment_record` | 支付记录 | id, order_id, payment_no, amount, method, status, paid_at |

### 物流域
| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `logistics_record` | 物流记录 | id, order_id, logistics_no, company, status |
| `logistics_step` | 物流步骤 | id, record_id, step_no, description, location, step_time |

### 售后域
| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `aftersale_record` | 售后记录 | id, order_id, user_id, type, reason, status, created_at, processed_at |

### 促销域
| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `coupon` | 优惠券模板 | id, name, type, value, min_order_amount, start_time, end_time, total_count, remaining_count |
| `user_coupon` | 用户优惠券 | id, user_id, coupon_id, status, used_order_id, created_at |
| `flash_sale_activity` | 秒杀活动 | id, name, start_time, end_time, status |
| `flash_sale_item` | 秒杀商品 | id, activity_id, product_id, sku_id, flash_price, total_stock, available_stock, limit_per_user |

---

## 技术栈详情

### 后端
- **Spring Boot 3.2.5** + Spring Cloud 2023.0.1 + Spring Cloud Alibaba 2023.0.1.0
- **Java 17** (GraalVM JDK)
- **MyBatis-Plus 3.5.5**：逻辑删除字段 `deleted`，`map-underscore-to-camel-case: true`，`id-type: auto`
- **MySQL 8.0**：库名 `cc_shop`，Docker 映射端口 3307
- **Redis 7**：密码 `ccshop`，用于购物车、商品缓存、秒杀库存
- **RabbitMQ 3.13**：用户/密码均为 `ccshop`
- **Nacos 2.2.3**：注册/配置中心
- **Sentinel**：内存规则限流
- **Knife4j 4.5.0**：OpenAPI 3 文档
- **Hutool 5.8.27**：工具库
- **JJWT 0.12.5**：JWT 生成/解析

### 前端
- **Nuxt 3** + Vue 3 + TypeScript
- **Nuxt UI v4.10.0**：组件库 + Tailwind CSS v4
- **Pinia**：状态管理
- **纯 CSS 无 UI 框架**：使用 Nuxt UI 语义化颜色

### 测试
- **Vitest**：单元测试框架
- **@nuxt/test-utils**：Nuxt 测试工具
- **Vue Test Utils**：Vue 组件测试
- 当前测试：10 个文件，97 个测试全部通过

---

## 微服务架构

### 网关路由

| 路径前缀 | 目标服务 | 端口 |
|---------|---------|------|
| `/api/user/**` | `cc-user` | 8081 |
| `/api/product/**` | `cc-product` | 8082 |
| `/api/trade/**` | `cc-trade` | 8083 |
| `/api/promotion/**` | `cc-promotion` | 8085 |
| `/api/admin/**` | `cc-admin` | 8086 |

Gateway 使用 `StripPrefix=1`，即 `/api/trade/cart/list` → 路由到 cc-trade 时变为 `/cart/list`。

### JWT 鉴权与用户上下文链路

```
1. Client → Gateway: Authorization: Bearer <JWT>
2. Gateway (AuthGlobalFilter): 解析 JWT → 写入 Header:
   - X-User-Id: <userId>
   - X-Username: <username>
3. 下游服务 (UserContextInterceptor): 读取 X-User-Id/X-Username → 写入 ThreadLocal (UserContext)
4. 服务间 Feign 调用 (FeignConfig): 从 ThreadLocal 读取 → 注入 X-User-Id/X-Username Header
```

### RabbitMQ 队列

| 队列 | 类型 | 用途 |
|------|------|------|
| `coupon.use` | Durable | 下单后异步核销券（cc-trade → cc-promotion） |
| `order.delay` | Durable + TTL 30min | 下单后 30min 超时（DLX → `order.timeout`） |
| `order.timeout` | Durable | 超时订单取消（cc-trade 消费） |
| `payment.success` | Durable | 支付成功通知（物流/状态更新） |

---

## 高并发方案

1. **Gateway + Sentinel**
   - 统一限流：登录5/min/IP，下单10/min/user，搜索30/min/IP
   - **秒杀场景**：热点参数限流（ Sentinel `ParamFlowRule`），抢购接口按商品ID限流
   - 熔断降级：下游故障时返回默认数据

2. **Redis 缓存**
   - 商品详情缓存（TTL 30min）
   - 分类/品牌列表缓存
   - 购物车 Redis 存储：`cart:{userId}` Hash，field=skuId, value=JSON
   - **秒杀库存**：`flash:stock:{itemId}` Redis 预扣，Lua脚本保证原子性
   - 优惠券剩余数：`coupon:remaining:{couponId}` String，领取时 DECR

3. **RabbitMQ 异步**
   - 支付成功 → MQ 通知订单服务更新状态
   - 订单超时 → 延迟队列 30min 后取消
   - 优惠券核销 → MQ 异步处理
   - **可靠性保证**：消费端幂等（按业务唯一键去重）；MQ 发送在事务提交后执行

4. **数据库优化**
   - HikariCP 连接池
   - MyBatis-Plus 逻辑删除、分页插件
   - `order` 是 MySQL 保留字，Entity 使用 `@TableName("\`order\`")`

---

## 实现步骤

### 阶段 1：基础设施 + 公共模块 ✅
1. 父 POM + cc-common（Result, JwtUtil, FeignConfig, enums）
2. Docker 启动 Nacos + MySQL + Redis + RabbitMQ + Seata
3. 数据库建表脚本 + 种子数据
4. cc-gateway（路由 + JWT鉴权 + CORS）

### 阶段 2：核心业务服务 ✅
1. cc-user（注册/登录/信息/地址/收藏/消息）
2. cc-product（商品+SKU+分类+品牌+搜索）
3. cc-trade（购物车+订单+支付+MQ消费者）
4. cc-promotion（优惠券+MQ消费者）
5. Nuxt3 前端 + Nuxt UI + 全部页面

### 阶段 3：物流 + 售后 + 秒杀 ✅
1. cc-trade 物流功能（LogisticsSimulator 物流模拟器）
2. cc-trade 售后功能（AftersaleService）
3. cc-promotion 秒杀功能（FlashSaleService + Redis Lua）
4. 前端物流时间线 + 售后申请页 + 秒杀专区

### 阶段 4：Admin 后台 ✅
1. cc-shop-admin 后端（Dashboard + 商品管理 + 订单管理 + 售后审批 + 优惠券管理）
2. cc-shop-admin 前端（独立 Nuxt 项目）
3. Gateway 路由配置

### 阶段 5：完善 + 部署（待实现）
1. 前端 SEO / 错误处理 / 移动端适配
2. 行为埋点完善（UserActionEvent + UserActionLogService）
3. Docker Compose 全栈部署

---

## 验证方式

1. **功能验证**：走通完整流程（注册→浏览→收藏→加购→选券→下单→支付→物流→售后）
2. **SKU验证**：商品详情页选不同规格，价格/库存联动正确
3. **优惠券验证**：领取→下单使用→金额门槛校验→已使用不可再用
4. **秒杀验证**：倒计时→抢购→Lua原子扣库存→限购→订单生成
5. **分布式事务验证**：下单成功后库存+订单+购物车三者一致；下单失败全部回滚
6. **微服务验证**：Nacos控制台各服务注册成功；停某服务后 Sentinel 熔断生效
7. **Admin验证**：管理后台登录→Dashboard→商品管理→订单管理→售后审批
8. **测试验证**：`cd cc-shop-web && npm test` 运行前端单元测试

---

## 常用命令

### 基础设施
```bash
cd cc-shop-cloud
docker compose -f docker/infra.yml up -d      # 启动 MySQL/Redis/RabbitMQ/Nacos/Seata
docker compose -f docker/infra.yml down       # 停止
```

### 后端
```bash
cd cc-shop-cloud
mvn clean install -DskipTests            # 全量编译
export JAVA_HOME="/c/Users/wxt/.jdks/graalvm-jdk-17.0.12"
export MYSQL_PORT=3307
mvn spring-boot:run                      # 启动单个服务
```

### 前端
```bash
# 用户端
cd cc-shop-web
npm install && npm run dev               # → http://localhost:3000
npm test                                 # 运行测试

# 管理后台
cd cc-shop-admin
npm install && npm run dev               # → http://localhost:3001
```

### 测试账号
| 用户名 | 密码 |
|--------|------|
| testuser1 | 123456 |
| testuser3 | 123456 |
