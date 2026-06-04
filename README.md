# Restaurant Ordering — 餐厅点餐系统

微服务架构的餐厅点餐系统，包含后台管理和微信小程序端。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7, Spring Cloud 2021, Spring Cloud Alibaba 2021 |
| 注册/配置中心 | Nacos |
| 网关 | Spring Cloud Gateway |
| 数据库 | MySQL 8 |
| ORM | MyBatis-Plus 3.5 |
| 缓存 | Redis |
| 消息队列 | RabbitMQ |
| 对象存储 | MinIO |
| 认证 | JWT + 微信小程序登录 |
| API 文档 | Knife4j |
| 工具库 | Hutool |
| 管理前端 | Vue 3.4, Vite 5, Element Plus, Pinia, TypeScript |
| 小程序端 | 微信原生小程序 |

## 项目结构

```
restaurant-ordering/
├── restaurant-common/       # 公共模块（工具类、异常处理、枚举、DTO）
├── restaurant-gateway/      # API 网关（端口 8080）
├── restaurant-auth/         # 认证服务（端口 8081）— 登录、JWT、微信授权
├── restaurant-user/         # 用户服务（端口 8082）— 个人信息、地址管理
├── restaurant-menu/         # 菜单服务（端口 8083）— 分类、菜品、规格
├── restaurant-order/        # 订单服务（端口 8084）— 购物车、订单、结算
├── restaurant-payment/      # 支付服务（端口 8085）— 支付处理
├── frontend/
│   ├── admin-frontend/      # 后台管理界面（Vue 3）
│   └── miniprogram-frontend/  # 微信小程序
└── pom.xml                  # 父 POM（聚合模块）
```

## 环境要求

| 依赖 | 版本 | 说明 |
|------|------|------|
| JDK | 1.8+ | |
| Maven | 3.6+ | |
| MySQL | 8.0 | 数据持久化 |
| Redis | 5+ | 缓存 / Token 存储 |
| RabbitMQ | 3.9+ | 订单消息异步处理 |
| Nacos | 2.x | 服务注册与配置中心（地址需与 `bootstrap.yml` 一致） |
| MinIO | 最新版 | 图片/文件存储（可选，不上传图片可暂时不装） |
| Node.js | 18+ | 管理后台前端构建 |
| 微信开发者工具 | 最新版 | 小程序开发与调试 |

## 快速启动

### 1. 启动基础设施

确保 MySQL、Redis、Nacos、RabbitMQ、MinIO 已启动并可访问。

### 2. 初始化数据库

创建以下数据库（编码 utf8mb4）：

```
db_restaurant_auth
db_restaurant_user
db_restaurant_menu
db_restaurant_order
db_restaurant_payment
```

启动各服务后 MyBatis-Plus 会自动建表。

### 3. 配置环境变量

所有敏感配置已通过 `${ENV_VAR}` 占位符引用，启动前需设置以下环境变量：

| 环境变量 | 说明 | 示例 |
|----------|------|------|
| `DB_PASSWORD` | MySQL 密码 | `your_db_password` |
| `REDIS_PASSWORD` | Redis 密码 | `your_redis_password` |
| `NACOS_PASSWORD` | Nacos 密码 | `your_nacos_password` |
| `RABBITMQ_PASSWORD` | RabbitMQ 密码 | `your_rabbitmq_password` |
| `WECHAT_APP_ID` | 微信小程序 AppID | `your_app_id` |
| `WECHAT_APP_SECRET` | 微信小程序 AppSecret | `your_app_secret` |
| `MINIO_ACCESS_KEY` | MinIO Access Key | `your_minio_access_key` |
| `MINIO_SECRET_KEY` | MinIO Secret Key | `your_minio_secret_key` |
| `JWT_SECRET` | JWT 签名密钥 | `your_jwt_secret` |

以下变量有默认值（适用于本地开发），可按需覆盖：

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `DB_USERNAME` | `root` | MySQL 用户名 |
| `REDIS_HOST` | `127.0.0.1` | Redis 地址 |
| `RABBITMQ_HOST` | `127.0.0.1` | RabbitMQ 地址 |
| `RABBITMQ_USERNAME` | `admin` | RabbitMQ 用户名 |
| `NACOS_USERNAME` | `nacos` | Nacos 用户名 |
| `MINIO_ENDPOINT` | `http://127.0.0.1:9000` | MinIO 地址 |

Nacos、Redis、RabbitMQ、MinIO 的地址如果非本机，需同时修改各模块 `application.yml` 中的 host 地址。

### 4. 启动后端服务

按顺序启动（推荐）：

```bash
# 先在父 pom 目录编译整个项目
mvn clean install -DskipTests

# 依次启动各模块（或直接在 IDE 中运行 Application 类）
# 1. Nacos 必须优先启动
# 2. 然后按任意顺序启动业务模块
cd restaurant-gateway && mvn spring-boot:run
cd restaurant-auth && mvn spring-boot:run
cd restaurant-user && mvn spring-boot:run
cd restaurant-menu && mvn spring-boot:run
cd restaurant-order && mvn spring-boot:run
cd restaurant-payment && mvn spring-boot:run
```

### 5. 启动管理后台

```bash
cd frontend/admin-frontend
npm install
npm run dev
```

默认在 `http://localhost:5173` 打开，已配置 `/api` 代理到网关 8080。

### 6. 启动微信小程序

用微信开发者工具打开 `frontend/miniprogram-frontend/` 目录，修改 `app.js` 中 `globalData.baseUrl` 为实际后端地址。

## 微服务端口

| 服务 | 端口 |
|------|------|
| Gateway | 8080 |
| Auth | 8081 |
| User | 8082 |
| Menu | 8083 |
| Order | 8084 |
| Payment | 8085 |

## 注意事项

- 生产部署时需将环境变量配置到服务器或 CI/CD 系统中，**不要**将真实凭据写入配置文件
- Nacos 需要在各服务启动前就绪，否则服务注册会失败
- 微信小程序需要 HTTPS 域名，开发阶段可在开发者工具中关闭域名校验
- 管理后台默认管理员账号：`admin`，首次启动后需通过数据库设置密码
