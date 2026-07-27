# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CC-Shop 是一个微服务电商全栈项目（简历/学习项目）。技术栈：Spring Cloud Alibaba 后端 + Nuxt 3 前端。

当前进度（见 `plan.md`）：**阶段 1~4 已完成**（用户/商品/购物车/优惠券/下单/支付/物流/售后/秒杀/Admin），阶段 5 待实现（完善+部署）。

---

## 技术栈

- **后端**：Spring Boot 3.2.5 + Spring Cloud 2023.0.1 + Spring Cloud Alibaba 2023.0.1.0
  - Java 17, Maven 多模块
  - ORM: MyBatis-Plus 3.5.5（逻辑删除字段 `deleted`，`map-underscore-to-camel-case: true`，`id-type: auto`）
  - DB: MySQL 8.0，库名 `cc_shop`
  - Cache: Redis 7（密码 `ccshop`）
  - MQ: RabbitMQ 3.13（用户/密码均为 `ccshop`）
  - 注册/配置: Nacos 2.2.3
  - 分布式事务: Seata 1.7.0（file 模式，实际已降级为本地事务+MQ补偿）
  - 限流: Sentinel（内存规则）
  - 文档: Knife4j 4.5.0（OpenAPI 3）
  - 工具: Hutool 5.8.27, JJWT 0.12.5
- **前端**：Nuxt 3 + Vue 3 + TypeScript + Pinia，纯 CSS 无 UI 框架
  - API 基地址：`nuxt.config.ts` 中 `runtimeConfig.public.apiBase` 默认 `http://127.0.0.1:8080`

---

## 仓库结构

```
cc-shop/
├── cc-shop-cloud/              # 后端（Spring Cloud 微服务）
│   ├── pom.xml                 # 父 POM，7 个子模块
│   ├── cc-common/              # 公共模块：Result、JwtUtil、FeignConfig、枚举、WebMvcConfig、UserContext
│   ├── cc-gateway/             # 网关 8080：路由 + JWT 鉴权 + Sentinel + CORS
│   ├── cc-user/                # 用户服务 8081：注册/登录/信息/地址/收藏/消息/验证码/登录日志
│   ├── cc-product/             # 商品服务 8082：商品/SKU/分类/品牌/搜索/库存
│   ├── cc-trade/               # 交易服务 8083：购物车/订单/支付/物流/售后/MQ消费者
│   ├── cc-promotion/           # 促销服务 8085：优惠券/秒杀/MQ消费者
│   ├── cc-shop-admin/          # 管理后台 8086：Dashboard/商品管理/订单管理/售后审批/优惠券管理/客户管理
│   ├── cc-ai/                  # AI 智能客服 8087：通义千问 LLM + RAG 商品问答 + SSE 流式
│   └── docker/
│       ├── infra.yml            # Docker Compose：MySQL + Redis + RabbitMQ + Nacos + Seata
│       └── mysql/init/          # 初始化 SQL
├── cc-shop-web/                 # 前端（Nuxt 3 SPA，用户端）
│   ├── pages/                   # 路由页面
│   ├── components/              # Vue 组件（必须放根目录，子目录会导致前缀问题）
│   ├── composables/             # useApi.ts, useToast.ts
│   ├── stores/                  # Pinia auth store
│   ├── middleware/               # auth.ts（路由守卫）
│   └── assets/css/main.css      # 全局 CSS 变量
└── cc-shop-admin/               # 前端（Nuxt 3 SPA，管理后台）
    ├── pages/                   # 管理页面
    └── ...
```

---

## 常用命令

### 基础设施

```bash
cd cc-shop-cloud
docker compose -f docker/infra.yml up -d      # 启动 MySQL/Redis/RabbitMQ/Nacos/Seata
docker compose -f docker/infra.yml down       # 停止
docker compose -f docker/infra.yml logs -f    # 查看日志
# MySQL 端口映射：容器 3306 → 宿主机 3307
```

### 后端

```bash
cd cc-shop-cloud
mvn clean install -DskipTests            # 全量编译

# 启动单个服务（在对应模块目录下），需要设置 JAVA_HOME 和 MYSQL_PORT：
export JAVA_HOME="/c/Users/wxt/.jdks/graalvm-jdk-17.0.12"
export MYSQL_PORT=3307
mvn spring-boot:run

# 快捷启动全部 6 个服务（每个在独立终端）
cd cc-gateway && mvn spring-boot:run     # → :8080
cd cc-user && mvn spring-boot:run        # → :8081
cd cc-product && mvn spring-boot:run     # → :8082
cd cc-trade && mvn spring-boot:run       # → :8083
cd cc-promotion && mvn spring-boot:run   # → :8085
cd cc-shop-admin && mvn spring-boot:run  # → :8086
cd cc-ai && mvn spring-boot:run          # → :8087
```

### 前端

```bash
# 用户端
cd cc-shop-web
npm install && npm run dev               # → http://localhost:3000

# 管理后台
cd cc-shop-admin
npm install && npm run dev               # → http://localhost:3001
```

### Windows 进程管理

```bash
# 查找端口占用
netstat -ano | grep LISTENING | grep ":8080"

# 杀进程（Git Bash 的 kill 不管用，用 tskill）
tskill <PID>
```

---

## 微服务架构

### 网关路由（`cc-gateway` → Nacos 服务发现）

| 路径前缀 | 目标服务 | 端口 |
|---------|---------|------|
| `/api/user/**` | `cc-user` | 8081 |
| `/api/product/**` | `cc-product` | 8082 |
| `/api/trade/**` | `cc-trade` | 8083 |
| `/api/promotion/**` | `cc-promotion` | 8085 |
| `/api/admin/**` | `cc-admin` | 8086 |
| `/api/ai/**` | `cc-ai` | 8087 |

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

- Gateway 白名单路径（无需 JWT）：`/api/user/auth/login`、`/api/user/auth/register`、`/api/product/list`、`/api/product/detail` 等
- 直接调用下游服务端口（如 `localhost:8081`）不走 Gateway，需要手动传 `X-User-Id` Header，否则 `UserContext.getUserId()` 返回 null

### 统一返回结构

```java
// cc-common: Result<T>
Result.success(data)           // {code:200, message:"success", data:..., timestamp:...}
Result.fail(message)           // {code:500, message:"...", data:null}
Result.fail(code, message)
```

---

## 数据库与 ORM

- MyBatis-Plus 配置：`map-underscore-to-camel-case: true`，逻辑删除 `deleted`，主键 `id-type: auto`
- 表统一在 `cc_shop` 库，初始化 SQL 在 `docker/mysql/init/`
- **`order` 是 MySQL 保留字**：Entity 必须使用 `@TableName("\`order\`")`，手写 SQL 也用反引号
- Docker MySQL 宿主机端口是 **3307**，默认配置是 3306，启动时必须 `export MYSQL_PORT=3307`

---

## Redis 数据模型

| Key 模式 | 类型 | 说明 |
|---------|------|------|
| `cart:{userId}` | Hash | field=skuId, value=JSON(CartItemVO) |
| `coupon:remaining:{couponId}` | String | 优惠券剩余数，领取时 DECR |
| `flash:stock:{itemId}` | String | 秒杀库存，Lua 脚本原子扣减 |
| `flash:limit:{userId}:{itemId}` | String | 秒杀限购，防重复购买 |

---

## RabbitMQ 架构

### 队列

| 队列 | 类型 | 用途 |
|------|------|------|
| `coupon.use` | Durable | 下单后异步核销券（cc-trade → cc-promotion） |
| `order.delay` | Durable + TTL 30min | 下单后 30min 超时（DLX → `order.timeout`） |
| `order.timeout` | Durable | 超时订单取消（cc-trade 消费） |
| `payment.success` | Durable | 支付成功通知（物流/状态更新） |

### 关键配置

- **必须配置 `Jackson2JsonMessageConverter`**：跨服务 MQ 消息用 JSON 格式，默认的 Java 序列化不支持跨服务 Map 反序列化
- cc-trade 和 cc-promotion 的 `RabbitMQConfig` 中都需要声明 `MessageConverter` bean
- DLX 模式：`order.delay` 消息 TTL 过期 → 投递到 `order.dlx` exchange → 路由到 `order.timeout` 队列
- MQ 发送在事务提交后执行，用 `TransactionSynchronizationManager.registerSynchronization(→ afterCommit())`

---

## 枚举值速查

```java
// cc-common: CouponType
FIXED(0, "满减")    // coupon.value = 固定金额
PERCENT(1, "折扣")   // coupon.value = 折扣率（0.85 即 8.5 折），折扣金额 = totalAmount * (1 - value)

// cc-common: CouponStatus
AVAILABLE(0), USED(1), EXPIRED(2)

// cc-common: OrderStatus
PENDING_PAYMENT(0), PENDING_SHIPMENT(1), SHIPPED(2), COMPLETED(3), CANCELLED(4), REFUNDED(5)

// cc-common: PaymentStatus
PENDING(0), SUCCESS(1), FAILED(2), REFUNDED(3)

// cc-common: AftersaleStatus
PENDING(0), APPROVED(1), REJECTED(2), COMPLETED(3)

// cc-common: AftersaleType
REFUND(0), EXCHANGE(1), RETURN_REFUND(2)
```

---

## 前端架构要点

- **auth store** (`stores/auth.ts`)：token/userId/username 持久化到 `localStorage`（key: `cc_token`、`cc_uid`、`cc_username`）
- **auth middleware** (`middleware/auth.ts`)：未登录跳转 `/user/login`，在需要保护的页面加 `definePageMeta({ middleware: 'auth' })`
- **useApi** (`composables/useApi.ts`)：自动带 Bearer token、401 自动登出、错误自动 toast。`post(url, body, query)` 第三个参数可选 query string
- **组件位置**：必须在 `components/` 根目录，子目录会导致命名前缀问题
- **导航**：Nuxt 3 使用 `navigateTo()` 而非 `router.push()`
- 登录/注册 API：`/api/user/auth/login`，`/api/user/auth/register`（前缀含 `auth`，在 Gateway 白名单中）

### 当前页面（用户端 cc-shop-web）

| 路径 | 文件 |
|------|------|
| `/` | `pages/index.vue` |
| `/product/list`, `/product/:id` | `pages/product/list.vue`, `pages/product/[id].vue` |
| `/cart` | `pages/cart.vue` |
| `/coupon` | `pages/coupon.vue` |
| `/favorites`, `/message` | `pages/favorites.vue`, `pages/message.vue` |
| `/user/login`, `/user/register`, `/user/profile`, `/user/address` | `pages/user/` |
| `/user/forgot-password` | `pages/user/forgot-password.vue` |
| `/order/checkout`, `/order/pay`, `/order/list`, `/order/:id` | `pages/order/` |
| `/order/logistics`, `/order/aftersale`, `/order/review` | `pages/order/` |
| `/promotion/flash` | `pages/promotion/flash.vue` |

### 当前页面（管理后台 cc-shop-admin）

| 路径 | 文件 |
|------|------|
| `/login` | `pages/login.vue` |
| `/` | `pages/index.vue` (Dashboard) |
| `/product` | `pages/product/index.vue` |
| `/order` | `pages/order/index.vue` |
| `/aftersale` | `pages/aftersale/index.vue` |
| `/coupon` | `pages/coupon/index.vue` |
| `/customer` | `pages/customer/index.vue` |
| `/merchant` | `pages/merchant/index.vue` |

---

## 测试账号

| 用户名 | 密码 |
|--------|------|
| testuser1 | 123456 |
| testuser3 | 123456 |

---

## 关键约束与注意事项

- JAVA_HOME 环境变量在 Windows 上有递归引用问题，用绝对路径 `/c/Users/wxt/.jdks/graalvm-jdk-17.0.12`
- Docker MySQL 映射到宿主 **3307**，所有后端服务启动需 `MYSQL_PORT=3307`
- 不用 Seata AT，用本地事务 + MQ 最终一致性补偿
- 跨服务 MQ 消息必须配 `Jackson2JsonMessageConverter`，不能用默认 Java 序列化
- 新增微服务需在父 POM `modules` 和 Gateway `application.yml` 路由中同步注册
- `plan.md` 是项目进度的权威参考
- 对外 Feign 接口放在 `/promotion/internal/**` 路径，不经过 Gateway
- Admin 后台独立 Nuxt 项目，端口 3001，后端 8086
