# CC-Shop 实现计划（修订版）

> 目标：一次性跑通主链路，减少 token 消耗，最大限度降低出错概率
> 无时间限制，每阶段验证通过后再进入下一阶段

---

## 总体架构

```
┌─────────────────────────────────────────────────┐
│                    cc-gateway (8080)              │
│          Gateway + JWT 鉴权 + Sentinel 限流        │
└──────┬──────┬──────┬──────┬──────┬──────────────┘
       │      │      │      │      │
  ┌────▼──┐ ┌▼──┐ ┌─▼──┐ ┌─▼────┐ ┌▼──────────┐
  │cc-user│ │cc-│ │cc- │ │cc-   │ │cc-promotion│
  │(8081) │ │pro│ │tra-│ │shop- │ │(8085)      │
  │       │ │du-│ │de  │ │admin │ │            │
  │ 已实现 │ │ct │ │(8083)│ │(8086)│ │ 优惠券     │
  │       │ │(已│ │    │ │      │ │ 秒杀       │
  │       │ │实 │ │购物│ │商品  │ │ Redis Lua  │
  └───────┘ │现)│ │车  │ │管理  │ │ MQ 削峰    │
            │   │ │订单│ │订单  │ │            │
            │   │ │支付│ │售后  │ └────────────┘
            │   │ │物流│ │促销  │
            │   │ │售后│ │Dashboard │
            │   │ └────┘ └──────┘
            └───┘
```

### 砍掉/降级

| 项 | 处理 |
|----|------|
| Seata AT | 砍掉，本地事务 + MQ 最终一致性补偿 |
| 评价提交 | 只读展示种子数据，不做提交（原 Day 1 已定） |
| ML 推荐 | 不做（原 Day 1 已定） |
| WebSocket 实时推送 | 不做，消息用轮询（原 Day 1 已定） |
| 移动端精细适配 | 只保证不崩，不做响应式精修（原 Day 1 已定） |
| 读写分离 | 不做（原 Day 1 已定） |

---

## 阶段 1：购物车 + 优惠券 ✅

### 后端

**cc-trade 新模块**

```
cc-shop-cloud/cc-trade/
├── pom.xml
├── src/main/java/com/ccshop/trade/
│   ├── TradeApplication.java
│   ├── controller/
│   │   └── CartController.java      # 购物车 CRUD
│   ├── service/
│   │   └── CartService.java
│   └── dto/
│       └── CartItemVO.java
└── src/main/resources/
    └── application.yml
```

- 购物车 Redis Hash：`key=cart:{userId}`，`field=skuId`，`value=JSON(CartItemVO)`
- API：`POST /cart/add`、`PUT /cart/update`、`DELETE /cart/remove/{skuId}`、`GET /cart/list`、`PUT /cart/select`
- 从 JWT 取 userId，无 DB 依赖

**cc-promotion 新模块**

```
cc-shop-cloud/cc-promotion/
├── pom.xml
├── src/main/java/com/ccshop/promotion/
│   ├── PromotionApplication.java
│   ├── controller/
│   │   └── CouponController.java    # 优惠券领取/列表
│   ├── service/
│   │   └── CouponService.java
│   ├── entity/
│   │   ├── Coupon.java              # 优惠券模板
│   │   └── UserCoupon.java          # 用户优惠券
│   ├── mapper/
│   │   ├── CouponMapper.java
│   │   └── UserCouponMapper.java
│   └── dto/
│       └── CouponVO.java
└── src/main/resources/
    └── application.yml
```

- 优惠券模板读取 `coupon` 表（种子数据已有 4 张券）
- 领取：`DECR coupon:{id}:remaining` → 成功则插入 user_coupon
- API：`GET /coupon/available`、`POST /coupon/receive/{id}`、`GET /coupon/my`

**公共变更**
- 父 POM 添加 cc-trade、cc-promotion 模块
- Gateway 路由已预配（无需修改）

### 前端

- `pages/cart.vue`：完整购物车（列表、数量增减、删除、勾选/全选、价格合计）
- `pages/coupon.vue`：领券中心（可领列表 + 已领列表）
- `components/CouponCard.vue`：优惠券卡片组件

### 验证清单

- [x] 购物车：加购 → 改数量 → 删商品 → 勾选 → 合计更新
- [x] 优惠券：领券中心显示 4 张券 → 领取 → 我的券出现 → 重复领取提示
- [x] Redis 数据：`cart:{userId}` 和 `coupon:{id}:remaining` 值正确

---

## 阶段 2：下单 + 支付 ✅

### 后端

**cc-trade 新增模块**

```
cc-trade/.../trade/ 新增：
├── controller/
│   ├── OrderController.java
│   └── PaymentController.java
├── service/
│   ├── OrderService.java
│   └── PaymentService.java
├── entity/
│   ├── Order.java
│   ├── OrderItem.java
│   └── PaymentRecord.java
├── mapper/
│   ├── OrderMapper.java
│   ├── OrderItemMapper.java
│   └── PaymentRecordMapper.java
├── dto/
│   ├── PlaceOrderRequest.java
│   └── OrderVO.java
└── mq/
    ├── OrderTimeoutConsumer.java    # 延迟消息消费
    └── PaymentNotifyConsumer.java   # 支付通知消费
```

**下单流程**（`OrderService.placeOrder`，`@Transactional`）：
1. 读取购物车选中项
2. 条件更新扣库存：`UPDATE product_sku SET stock = stock - ? WHERE id = ? AND stock >= ?`
3. MQ 异步扣优惠券（发消息到 `coupon.use` 队列，promotion 消费）
4. 生成订单 + 订单明细
5. 清购物车已购买项
6. 发 MQ 延迟消息（30 分钟超时取消）

**支付流程**（`PaymentService.pay`）：
1. 校验订单状态
2. 生成 payment_record，状态=待支付
3. mock 沙箱：延迟 3 秒，95% 成功 / 5% 失败
4. 成功 → 更新 payment_record + 更新 order 状态为"待发货" + 发 MQ 通知
5. 幂等：payment_no 去重

**MQ 队列**

| 队列 | 用途 | 类型 |
|------|------|------|
| `coupon.use` | 下单后扣减优惠券 | 普通队列 |
| `coupon.use.retry` | 扣券失败补偿 | 重试队列 |
| `order.delay` | 30 分钟超时取消 | 延迟队列（插件/死信） |
| `payment.success` | 支付成功通知 | 普通队列 |

### 前端

- `pages/order/checkout.vue`：下单页（地址选择、商品清单、优惠券选择、提交）
- `pages/order/list.vue`：订单列表（按状态筛选）
- `pages/order/[id].vue`：订单详情
- `pages/order/pay.vue`：支付页（金额、mock 支付按钮、结果）
- `components/AddressSelector.vue`：地址选择器

### 验证清单

- [x] 下单：购物车选中 → 下单页 → 选地址 → 选券 → 提交 → 订单生成
- [x] 库存扣减：条件更新生效，超卖时下单失败
- [x] 优惠券状态：可用→已用
- [x] 支付：点击支付 → 3 秒 → 成功/失败 → 订单状态更新
- [x] 幂等：重复支付请求不重复扣款
- [ ] 超时：30 分钟未支付 → 订单自动取消 → 库存回滚（无法快速验证，DLX 基础设施已就绪）

---

## 阶段 3：物流 + 售后 + 秒杀 ✅

### 后端

**cc-trade 新增**

```
cc-trade/.../trade/ 新增：
├── controller/
│   ├── LogisticsController.java
│   └── AftersaleController.java
├── service/
│   ├── LogisticsSimulator.java
│   ├── LogisticsService.java
│   └── AftersaleService.java
├── entity/
│   ├── LogisticsRecord.java
│   ├── LogisticsStep.java
│   └── AftersaleRecord.java
├── mapper/
│   ├── LogisticsRecordMapper.java
│   ├── LogisticsStepMapper.java
│   └── AftersaleRecordMapper.java
├── dto/
│   ├── LogisticsVO.java
│   ├── AftersaleApplyRequest.java
│   └── AftersaleVO.java
```

- 物流模拟器：支付成功后定时任务生成物流步骤（已揽收→运输中→派送中→已签收）
- 售后：`POST /order/aftersale`、`GET /order/aftersale/{orderId}`

**cc-promotion 新增**

```
cc-promotion/.../promotion/ 新增：
├── controller/
│   └── FlashSaleController.java
├── service/
│   └── FlashSaleService.java
├── entity/
│   ├── FlashSaleActivity.java
│   └── FlashSaleItem.java
├── mapper/
│   ├── FlashSaleActivityMapper.java
│   └── FlashSaleItemMapper.java
├── dto/
│   └── FlashSaleVO.java
├── feign/
│   └── ProductClient.java
```

- 秒杀流程：
  1. 预热：活动开始前库存加载到 Redis `flash:stock:{itemId}`
  2. 抢购：Lua 脚本原子扣库存 + 限购校验 + 活动时间校验
  3. 扣减成功 → MQ 异步落库
  4. Sentinel 热点参数限流

### 前端

- `pages/order/logistics.vue`：物流时间线
- `pages/order/aftersale.vue`：售后申请页
- `pages/promotion/flash.vue`：秒杀专区（活动列表 + 倒计时 + 抢购）
- `components/SeckillCard.vue`：倒计时组件

### 验证清单

- [x] 物流：支付完成后物流时间线逐步推进
- [x] 售后：提交申请 → 状态流转正常
- [x] 秒杀：倒计时 → 抢购 → Lua 原子扣库存 → 限购 → 订单生成
- [ ] Sentinel：抢购接口限流生效（使用现有Gateway全局限流）

---

## 阶段 4：Admin 后台 ✅

### 后端

```
cc-shop-cloud/cc-shop-admin/
├── pom.xml
├── src/main/java/com/ccshop/admin/
│   ├── AdminApplication.java
│   ├── controller/
│   │   ├── DashboardController.java       # 仪表盘统计
│   │   ├── AdminAuthController.java       # 管理员登录
│   │   ├── AdminProductController.java    # 商品 CRUD（含 SKU 编辑）
│   │   ├── AdminOrderController.java      # 订单管理
│   │   ├── AdminAftersaleController.java  # 售后审批
│   │   ├── AdminCouponController.java     # 优惠券管理
│   │   └── AdminCustomerController.java   # 客户管理
│   ├── service/
│   │   ├── DashboardService.java
│   │   ├── AdminProductService.java
│   │   ├── AdminOrderService.java
│   │   ├── AdminAftersaleService.java
│   │   ├── AdminCouponService.java
│   │   └── AdminCustomerService.java
│   └── dto/
│       └── DashboardVO.java
└── src/main/resources/
    └── application.yml
```

- 端口：8086
- Gateway 路由：`/api/admin/**` → `cc-admin`
- 关键：商品管理含 SKU 编辑器（新增商品时批量添加 SKU）
- 售后审批：通过/拒绝 + 更新售后状态

### 前端

独立 Nuxt 项目 `cc-shop-admin/`，路由 `/admin`：
- `pages/login.vue`：管理员登录
- `pages/index.vue`：Dashboard（统计卡片：订单数、用户数、商品数）
- `pages/product/index.vue`：商品管理（列表 + 新增/编辑含 SKU + 下架）
- `pages/order/index.vue`：订单管理（列表 + 详情 + 发货）
- `pages/aftersale/index.vue`：售后审批（列表 + 通过/拒绝）
- `pages/coupon/index.vue`：促销管理（优惠券模板）
- `pages/customer/index.vue`：客户管理（用户列表 + 详情）
- `pages/merchant/index.vue`：商家管理

### 验证清单

- [x] Admin 登录 → Dashboard 加载
- [x] 商品管理：新增商品含 SKU → 编辑 → 下架
- [x] 订单管理：查看订单 → 发货
- [x] 售后审批：通过 → 状态更新 → 退款

---

## 阶段 5：完善 + 部署 + 验证

### 行为埋点 ✅

- ✅ 前端 `tracker.ts`：采集 view/click/cart/order/favorite/search/receive 事件，批量发送（10条或5秒）
- ✅ 后端 `ActionController`：批量接收事件，发送到 MQ
- ✅ 后端 `UserActionConsumer`：消费 MQ 消息，落库 `user_action_log` 表
- ✅ Gateway 白名单：`/api/user/action` 无需登录即可上报
- ✅ 页面埋点：商品详情（view/click/cart/favorite）、商品列表（search）、下单（order）、领券（receive）

### 全栈 Docker Compose ✅

- ✅ 根目录 `docker-compose.yml`：一键启动 11 个容器（4 基础设施 + 6 微服务 + 2 前端）
- ✅ 后端 Dockerfile：6 个微服务独立 Dockerfile（gateway/user/product/trade/promotion/admin）
- ✅ 前端 Dockerfile：用户端 + 管理后台（Nuxt 3 SSR 模式）
- ✅ 环境变量配置：`.env.example` 包含所有必要配置
- ✅ Nuxt 配置：支持 `NUXT_PUBLIC_API_BASE` 环境变量覆盖
- Sentinel 规则持久化到 Nacos（可选，暂不实现）

### 8 项验证清单

- [ ] 注册 → 登录 → 浏览商品 → 选 SKU
- [ ] 加购 → 购物车操作 → 领券
- [ ] 下单 → 支付成功 → 订单状态更新
- [ ] 支付超时 → 订单自动取消 → 库存回滚
- [ ] 物流时间线推进 → 售后申请 → 审批
- [ ] 秒杀抢购 → 原子扣库存 → 限购生效
- [ ] Admin 管理商品/订单/售后
- [ ] Docker 全栈一键起

---

## 关键风险与应对

| 风险 | 概率 | 应对 |
|------|------|------|
| 阶段 2 订单超时 MQ 延迟消息调不通 | 中 | 降级为每分钟定时任务扫描超时订单 |
| 阶段 3 秒杀 Lua 脚本写错 | 低 | 降级为 `DECR` + Java 校验 |
| 阶段 3 Sentinel 热点限流调不通 | 中 | 降级为 Gateway 全局 QPS 限流（已有配置） |
| 阶段 4 Admin 工作量超预期 | 中 | 砍用户/消息/物流管理页，保商品+订单+售后 |
| 优惠券 MQ 补偿出现死循环 | 低 | 重试队列 + 最大重试次数，超过则人工介入 |
| 条件更新扣库存死锁 | 低 | 用 `FOR UPDATE` 行锁，控制事务粒度 |
