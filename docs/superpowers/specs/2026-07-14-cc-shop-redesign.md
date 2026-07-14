# CC-Shop 重新设计方案

> 2026-07-14
> 目标：一次性跑通主链路，减少 token 消耗，最大限度降低出错概率

---

## 架构变更

### 现状

Day 1 已完成：`cc-gateway`、`cc-user`、`cc-product`、docker-compose 基础设施、前端骨架

### 微服务缩减

| 原计划 | 新计划 | 说明 |
|--------|--------|------|
| `cc-cart` (8083) | → `cc-trade` (8083) | 购物车 + 订单 + 支付 + 物流 合并 |
| `cc-order` (8084) | ↳ | |
| `cc-payment` (8085) | ↳ | |
| `cc-logistics` (8086) | ↳ | |
| `cc-promotion` (8085) | `cc-promotion` (8084) | 优惠券 + 秒杀，独立 |
| `cc-shop-admin` (8087) | `cc-shop-admin` (8085) | Admin 后台，最后阶段 |

**5 个新服务 → 2 个新服务 + 1 个 Admin 服务**

### 技术栈变更

| 项 | 原计划 | 新计划 | 原因 |
|----|--------|--------|------|
| Seata AT | 全局事务 | ❌ 砍掉。本地事务 + MQ 补偿 | trade 合并后大部分操作在单服务内，跨服务的优惠券扣减用 MQ 最终一致性即可 |
| 购物车 | Redis Hash | ✅ 保留 | 轻量，无 DB 依赖 |
| Redis Lua 秒杀 | 原子扣库存 | ✅ 保留 | 秒杀核心亮点 |
| Sentinel 网关限流 | 路由规则 | ✅ 保留 | 已有配置，低风险 |
| RabbitMQ | 削峰/通知 | ✅ 保留 | 支付通知、秒杀削峰、订单超时、优惠券补偿 |
| 支付沙箱 | mock 3s 延迟 | ✅ 保留 | 原方案已够用 |

---

## 阶段划分（垂直切片）

### 阶段 1：购物车 + 优惠券

**后端**
- `cc-trade`：CartController + CartService，Redis Hash 存储
- `cc-promotion`：CouponController + CouponService，DB + Redis 预扣
- 两个新模块的 POM 配置、application.yml、Nacos 注册

**前端**
- `pages/cart.vue`：完整购物车（列表/数量/选择/合计）
- `pages/coupon.vue`：领券中心 + 我的券
- `components/CouponCard.vue`

**验证**：加购→领券→Redis 数据正确

### 阶段 2：下单 + 支付

**后端**
- `cc-trade`：OrderController + OrderService，下单流程（条件更新防超卖）
- `cc-trade`：PaymentController + PaymentService，mock 沙箱
- MQ：订单超时取消（延迟消息）、支付成功通知

**前端**
- `pages/order/checkout.vue`：下单页（地址+券+清单）
- `pages/order/list.vue`：订单列表
- `pages/order/[id].vue`：订单详情
- `pages/order/pay.vue`：支付页

**验证**：加购→下单→支付→库存/券状态更新

### 阶段 3：物流 + 售后 + 秒杀

**后端**
- `cc-trade`：LogisticsSimulator + LogisticsController，物流模拟器
- `cc-trade`：AftersaleController + AftersaleService，售后
- `cc-promotion`：FlashSaleController + FlashSaleService，Redis Lua + MQ 削峰

**前端**
- `pages/order/logistics.vue`：物流时间线
- `pages/order/aftersale.vue`：售后申请页
- `pages/promotion/flash.vue`：秒杀专区
- `components/SeckillCard.vue`：倒计时组件

**验证**：秒杀抢购→物流推进→售后申请

### 阶段 4：Admin 后台

**后端**
- `cc-shop-admin`：商品管理（含 SKU 编辑）、订单管理、售后审批、促销管理
- 用户/消息/物流做最简只读列表

**前端**
- 独立 Nuxt 项目 `cc-shop-admin`，`/admin` 路由
- Dashboard + 商品管理 + 订单管理 + 售后审批 + 促销管理

**验证**：Admin 登录 → 管理商品/订单/售后

### 阶段 5：完善 + 部署 + 验证

- 行为埋点（`tracker.ts` + MQ 批量写入 `user_action_log`）
- 前端错误处理 / Toast / 移动端兜底
- Docker Compose 全栈一键起
- 跑通全部验证清单

---

## 关键风险与应对

| 风险 | 应对 |
|------|------|
| 阶段 2 订单超时取消调不通 | 降级为每分钟定时任务扫描超时订单，不用 MQ 延迟消息 |
| 阶段 3 秒杀 Redis Lua 脚本写错 | 降级为 `DECR` + Java 校验，Lua 可后续优化 |
| 阶段 4 Admin 工作量大 | 砍用户/消息/物流管理页，保商品+订单+售后 |
| 前端活动/秒杀等尚未实现 | 目前阶段 1-3 堆叠，已在阶段计划中解决 |
| Docker 全栈起 Nacos 版本兼容 | 沿用 Day 1 已验证的配置，不升级版本 |