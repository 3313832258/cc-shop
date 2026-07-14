# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CC-Shop 是一个微服务电商全栈项目（简历/学习项目）。技术栈：Spring Cloud Alibaba 后端 + Nuxt 3 前端。目标是在 4 天内端到端跑通主链路。

当前进度（见根目录 `plan.md`）：Day 1 已完成（用户/商品/网关），Day 2~4 待实现（购物车、订单、支付、秒杀、Admin 等）。

---

## 技术栈

- **后端**：Spring Boot 3.2.5 + Spring Cloud 2023.0.1 + Spring Cloud Alibaba 2023.0.1.0
  - Java 17, Maven 多模块
  - ORM: MyBatis-Plus 3.5.5（逻辑删除字段 `deleted`）
  - DB: MySQL 8.0
  - Cache: Redis 7（密码 `ccshop`）
  - MQ: RabbitMQ 3.13（用户 `ccshop` / 密码 `ccshop`）
  - 注册/配置: Nacos 2.2.3
  - 分布式事务: Seata 1.7.0（file 模式）
  - 限流: Sentinel（内存规则）
  - 文档: Knife4j 4.5.0（OpenAPI 3）
  - 工具: Hutool, JJWT 0.12.5

- **前端**：Nuxt 4 + Vue 3 + TypeScript + Pinia
  - UI 为纯 CSS（无 UI 框架）
  - 基础请求库：`$fetch`，API 基地址在 `nuxt.config.ts` 的 `runtimeConfig.public.apiBase`

---

## 仓库结构

```
cc-shop/
├── cc-shop-cloud/          # 后端（Spring Cloud 微服务）
│   ├── pom.xml             # 父 POM，管理依赖版本和阿里云 Maven 仓库
│   ├── cc-common/          # 公共模块（Result、JwtUtil、FeignConfig、枚举、全局异常、UserContext）
│   ├── cc-gateway/         # 网关（端口 8080），Spring Cloud Gateway + JWT 鉴权 + Sentinel + CORS
│   ├── cc-user/            # 用户服务（端口 8081）：注册/登录/信息/地址/收藏/消息
│   ├── cc-product/         # 商品服务（端口 8082）：商品/SKU/分类/品牌/搜索/缓存
│   └── docker/
│       ├── infra.yml        # Docker Compose：MySQL + Redis + RabbitMQ + Nacos + Seata
│       └── mysql/init/      # 初始化 SQL（V1__init_schema.sql, V2__init_data.sql）
└── cc-shop-web/             # 前端（Nuxt 3 SPA）
    ├── nuxt.config.ts
    ├── package.json
    ├── pages/               # 路由页面
    ├── components/          # Vue 组件（放根目录，Nuxt 才能自动导入）
    ├── composables/         # useApi.ts, useToast.ts
    ├── stores/              # Pinia stores（auth.ts）
    ├── middleware/          # 路由中间件（auth.ts）
    ├── plugins/             # auth.client.ts（从 localStorage 恢复 token）
    └── assets/css/main.css  # 全局样式变量
```

---

## 常用命令

### 后端（cc-shop-cloud/）

```bash
# 全量编译（含子模块）
mvn clean install

# 运行单个服务（在对应模块目录下）
mvn spring-boot:run

# 打包
mvn clean package
```

### 前端（cc-shop-web/）

```bash
# 安装依赖
npm install

# 开发服务器（http://localhost:3000）
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview
```

### 基础设施（Docker）

```bash
cd cc-shop-cloud

# 一键启动 MySQL / Redis / RabbitMQ / Nacos / Seata
docker compose -f docker/infra.yml up -d

# 查看日志
docker compose -f docker/infra.yml logs -f

# 停止
docker compose -f docker/infra.yml down
```

---

## 后端架构要点

### 微服务与网关路由

网关（`cc-gateway`，端口 8080）通过 Nacos 做服务发现，路由规则如下（均在 `application.yml` 的 `spring.cloud.gateway.routes` 中配置）：

| 路径前缀 | 目标服务 | 说明 |
|---------|---------|------|
| `/api/user/**` | `cc-user` | 用户服务（端口 8081） |
| `/api/product/**` | `cc-product` | 商品服务（端口 8082） |
| `/api/cart/**` | `cc-cart` | （待实现） |
| `/api/order/**` | `cc-order` | （待实现） |
| `/api/payment/**` | `cc-payment` | （待实现） |
| `/api/promotion/**` | `cc-promotion` | （待实现，含优惠券/秒杀） |
| `/api/logistics/**` | `cc-logistics` | （待实现） |

### 统一返回结构

所有 Controller 返回 `Result<T>`（位于 `cc-common`）：

```java
Result.success(data)
Result.fail(message)
Result.fail(code, message)
```

结构：
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1234567890
}
```

### 数据库与 ORM

- MyBatis-Plus 配置在 `application.yml` 中：
  - `map-underscore-to-camel-case: true`
  - 逻辑删除字段：`deleted`（1=已删除，0=未删除）
  - 主键策略：`id-type: auto`
- 表统一在 `cc_shop` 库下，初始化 SQL 在 `docker/mysql/init/` 中
- `cc-user` 和 `cc-product` 均直连同一个 MySQL 实例

### 环境变量与默认值

各服务通过环境变量注入外部依赖地址，均有本地默认值（127.0.0.1）：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `NACOS_ADDR` | `127.0.0.1:8848` | Nacos 地址 |
| `MYSQL_HOST` | `127.0.0.1` | MySQL 主机 |
| `MYSQL_PORT` | `3306` | MySQL 端口 |
| `MYSQL_USER` / `MYSQL_PASSWORD` | `root` / `root` | MySQL 凭据 |
| `REDIS_HOST` / `REDIS_PORT` | `127.0.0.1` / `6379` | Redis 地址 |
| `REDIS_PASSWORD` | `ccshop` | Redis 密码 |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `127.0.0.1` / `5672` | RabbitMQ 地址 |
| `RABBITMQ_USER` / `RABBITMQ_PASSWORD` | `ccshop` / `ccshop` | RabbitMQ 凭据 |

---

## 前端架构要点

### API 请求

全局 API 封装在 `composables/useApi.ts`：

- 自动携带 JWT（`Authorization: Bearer <token>`）
- 401 时自动退出登录并跳转到 `/user/login`
- 接口报错自动 toast 提示
- 前端通过 `runtimeConfig.public.apiBase` 指向网关地址（默认 `http://127.0.0.1:8080`）

### 状态管理

- Pinia store `stores/auth.ts`：管理登录态（token、userId、username），持久化到 `localStorage`
- 插件 `plugins/auth.client.ts`：客户端启动时从 `localStorage` 恢复 auth 状态

### 路由与权限

- `middleware/auth.ts`：未登录用户拦截受保护路由，重定向到 `/user/login`
- 当前已实现页面：`/`, `/product/list`, `/product/:id`, `/cart`, `/favorites`, `/message`, `/user/login`, `/user/register`, `/user/profile`

### 组件自动导入

Nuxt 默认自动导入 `components/` 下的组件。注意：**组件必须放在 `components/` 根目录**，子目录（如 `components/layout/`）会导致组件名需要带前缀，引发运行时解析失败。当前已修复。

---

## 开发流程

1. 启动基础设施：`docker compose -f docker/infra.yml up -d`
2. 启动后端服务（按顺序）：Gateway → User → Product（以及后续服务）
3. 启动前端：`npm run dev`
4. 前端访问 http://localhost:3000，API 走 http://localhost:8080

---

## 关键约束与注意事项

- 不要在代码中硬写死依赖地址（MySQL/Redis/Nacos 等），应通过 `application.yml` 的环境变量读取
- 不要在代码或提交中写入 API Key、Token、密码等敏感信息
- 组件统一放在 `components/` 根目录，避免 Nuxt 自动导入前缀问题
- 后端新增微服务时，务必在 Gateway 的 `application.yml` 中配置对应路由
- `plan.md` 是当前项目进度的唯一权威参考，不确定范围时优先查看该文件

<!-- superpowers-zh:begin (do not edit between these markers) -->
# Superpowers-ZH 中文增强版

本项目已安装 superpowers-zh 技能框架（20 个 skills）。

## 核心规则

1. **收到任务时，先检查是否有匹配的 skill** — 哪怕只有 1% 的可能性也要检查
2. **设计先于编码** — 收到功能需求时，先用 brainstorming skill 做需求分析
3. **测试先于实现** — 写代码前先写测试（TDD）
4. **验证先于完成** — 声称完成前必须运行验证命令

## 可用 Skills

Skills 位于 `.claude/skills/` 目录，每个 skill 有独立的 `SKILL.md` 文件。

- **brainstorming**: 在任何创造性工作之前必须使用此技能——创建功能、构建组件、添加功能或修改行为。在实现之前先探索用户意图、需求和设计。
- **chinese-code-review**: 中文 review 沟通参考——话术模板、分级标注（必须修复/建议修改/仅供参考）、国内团队常见反模式应对。仅在用户显式 /chinese-code-review 时调用，不要根据上下文自动触发。
- **chinese-commit-conventions**: 中文 commit 与 changelog 配置参考——Conventional Commits 中文适配、commitlint/husky/commitizen 中文模板、conventional-changelog 中文配置。仅在用户显式 /chinese-commit-conventions 时调用，不要根据上下文自动触发。
- **chinese-documentation**: 中文文档排版参考——中英文空格、全半角标点、术语保留、链接格式、中文文案排版指北约定。仅在用户显式 /chinese-documentation 时调用，不要根据上下文自动触发。
- **chinese-git-workflow**: 国内 Git 平台配置参考——Gitee、Coding.net、极狐 GitLab、CNB 的 SSH/HTTPS/凭据/CI 接入差异与镜像同步配置。仅在用户显式 /chinese-git-workflow 时调用，不要根据上下文自动触发。
- **dispatching-parallel-agents**: 当面对 2 个以上可以独立进行、无共享状态或顺序依赖的任务时使用
- **executing-plans**: 当你有一份书面实现计划需要在单独的会话中执行，并设有审查检查点时使用
- **finishing-a-development-branch**: 当实现完成、所有测试通过、需要决定如何集成工作时使用——通过提供合并、PR 或清理等结构化选项来引导开发工作的收尾
- **mcp-builder**: MCP 服务器构建方法论 — 系统化构建生产级 MCP 工具，让 AI 助手连接外部能力
- **receiving-code-review**: 收到代码审查反馈后、实施建议之前使用，尤其当反馈不明确或技术上有疑问时——需要技术严谨性和验证，而非敷衍附和或盲目执行
- **requesting-code-review**: 完成任务、实现重要功能或合并前使用，用于验证工作成果是否符合要求
- **subagent-driven-development**: 当在当前会话中执行包含独立任务的实现计划时使用
- **systematic-debugging**: 遇到任何 bug、测试失败或异常行为时使用，在提出修复方案之前执行
- **test-driven-development**: 在实现任何功能或修复 bug 时使用，在编写实现代码之前
- **using-git-worktrees**: 当需要开始与当前工作区隔离的功能开发，或在执行实现计划之前使用——通过原生工具或 git worktree 回退机制确保隔离工作区存在
- **using-superpowers**: 在开始任何对话时使用——确立如何查找和使用技能，要求在任何响应（包括澄清性问题）之前调用 Skill 工具
- **verification-before-completion**: 在宣称工作完成、已修复或测试通过之前使用，在提交或创建 PR 之前——必须运行验证命令并确认输出后才能声称成功；始终用证据支撑断言
- **workflow-runner**: 在 Claude Code / OpenClaw / Cursor 中直接运行 agency-orchestrator YAML 工作流——无需 API key，使用当前会话的 LLM 作为执行引擎。当用户提供 .yaml 工作流文件或要求多角色协作完成任务时触发。
- **writing-plans**: 当你有规格说明或需求用于多步骤任务时使用，在动手写代码之前
- **writing-skills**: 当创建新技能、编辑现有技能或在部署前验证技能是否有效时使用

## 如何使用

当任务匹配某个 skill 时，使用 `Skill` 工具加载对应 skill 并严格遵循其流程。绝不要用 Read 工具读取 SKILL.md 文件。

如果你认为哪怕只有 1% 的可能性某个 skill 适用于你正在做的事情，你必须调用该 skill 检查。
<!-- superpowers-zh:end -->
