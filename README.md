# 疫苗配送管理系统

面向区域疾控中心、第三方冷链车队、接种门诊的疫苗冷链配送全流程管理系统。

## 原始需求

> 搭建一个给区域疾控中心、第三方冷链车队、接种门诊共同使用的疫苗配送管理系统，页面部分可以用 Vue3 组织成批次、配送、在途、验收几个工作区，服务端用 Spring Boot 承接批次状态、温控记录和库存流水。疾控中心维护疫苗批号、厂家、规格、有效期、储存温区、批签发文件和可配送数量；仓库人员出库时绑定冷链箱、温度探头、封签编号、车辆、司机和预计到达时间；车队在运输途中上报温度、定位、停留点、开箱记录和探头状态；门诊收货时核对批号、数量、封签、温度曲线和到货时间，验收通过后写入门诊库存。探头离线时要标记温控证据缺口，温度曲线缺失时暂停门诊入库，门诊拒收时生成回库任务，数量不一致时要求仓库和门诊分别确认实发、实收数量，不能把这些情况都处理成同一个失败状态。

## 技术栈

- **前端**：Vue 3 + TypeScript + Vite + Tailwind CSS + Vue Router + Lucide Icons
- **后端**：Spring Boot 3.4 + Spring Data JPA + H2 内存数据库 + Lombok
- **部署**：Docker + Docker Compose + Nginx 反向代理

## 功能模块

### 批次管理（疾控中心）
- 维护疫苗批号、厂家、规格、有效期、储存温区、批签发文件
- 管理可配送数量和批次状态

### 配送管理（仓库）
- 创建配送单，绑定冷链箱、温度探头、封签编号
- 绑定车辆、司机和预计到达时间
- 自动关联批次信息

### 在途监控（车队）
- 实时查看温度曲线和探头状态
- 上报温度、定位、停留点、开箱记录
- 温控证据缺口标记（探头离线时自动创建）

### 验收管理（接种门诊）
- 核对批号、数量、封签、温度曲线
- 四种异常场景差异化处理：
  - **温控证据缺口**（探头离线）：标记缺口，暂停入库，确认后恢复
  - **温度曲线缺失**：暂停入库，验证曲线后恢复
  - **门诊拒收**：生成独立回库任务
  - **数量不一致**：仓库和门诊分别确认实发/实收数量
- 验收通过后写入门诊库存

## 目录结构

```
wl-253/
├── src/                          # Vue3 前端源码
│   ├── components/               # 通用组件
│   ├── composables/              # API 组合式函数
│   ├── data/                     # Mock 数据
│   ├── pages/                    # 页面组件
│   ├── router/                   # 路由配置
│   ├── types/                    # TypeScript 类型定义
│   ├── App.vue
│   ├── main.ts
│   └── style.css
├── server/                       # Spring Boot 后端
│   ├── src/main/java/com/vaccine/
│   │   ├── config/               # 配置类（CORS、数据初始化）
│   │   ├── controller/           # REST 控制器
│   │   ├── dto/                  # 数据传输对象
│   │   ├── entity/               # JPA 实体
│   │   ├── enums/                # 枚举类型
│   │   ├── exception/            # 异常处理
│   │   ├── repository/           # 数据仓库
│   │   └── service/              # 业务服务
│   ├── Dockerfile
│   └── .dockerignore
├── Dockerfile                    # 前端 Docker 构建
├── docker-compose.yml            # 一键启动编排
├── nginx.conf                    # Nginx 反向代理配置
├── .dockerignore
├── index.html
├── package.json
├── vite.config.ts
├── tailwind.config.js
└── postcss.config.js
```

## 启动方式

### 前置要求

- **Docker 启动**：Docker 20.10+、Docker Compose V2
- **本地开发**：Node.js 18+、JDK 17+、Maven 3.8+

### Docker 一键启动（推荐）

#### 1. 构建并启动

```bash
docker compose up --build
```

后台运行：

```bash
docker compose up --build -d
```

#### 2. 访问

- 前端页面：http://localhost:3000
- 后端 API：http://localhost:8080/api/batches
- H2 控制台：http://localhost:8080/h2-console（JDBC URL: `jdbc:h2:mem:vaccinedb`，用户名: `sa`，密码: 空）

#### 3. 停止

```bash
docker compose down
```

### 本地开发启动

#### 1. 安装前端依赖

```bash
npm install
```

#### 2. 启动后端

```bash
cd server
mvn spring-boot:run
```

#### 3. 启动前端

```bash
npm run dev
```

访问地址：http://localhost:3000

> 前端会自动检测后端是否可用。后端运行时使用真实 API，后端不可用时自动使用 Mock 数据。

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/batches | 获取所有批次 |
| POST | /api/batches | 创建批次 |
| GET | /api/distribution-orders | 获取配送单列表 |
| POST | /api/distribution-orders | 创建配送单 |
| GET | /api/transit-reports | 获取在途报告 |
| POST | /api/transit-reports | 上报在途数据 |
| GET | /api/evidence-gaps | 获取温控证据缺口 |
| GET | /api/acceptance-records | 获取验收记录 |
| PUT | /api/acceptance-records/{id}/accept | 验收通过 |
| PUT | /api/acceptance-records/{id}/reject | 拒收（生成回库任务） |
| POST | /api/acceptance-records/{id}/confirm-quantity | 确认数量（仓库/门诊分别） |
| PUT | /api/acceptance-records/{id}/acknowledge-gap | 确认温控证据缺口 |
| PUT | /api/acceptance-records/{id}/resume-inbound | 恢复入库 |
| GET | /api/return-tasks | 获取回库任务 |
| GET | /api/clinic-inventory | 获取门诊库存 |
