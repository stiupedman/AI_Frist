# 大学生家教管理系统

面向大学生教员、家长学员和平台管理员的家教业务管理系统，采用 Spring Cloud、Spring Boot、Vue 2、MySQL、Redis 和 Nacos 构建。

## 已实现功能

- 教员维护个人资料，管理员审核
- 家长发布家教需求
- 已审核教员申请需求
- 家长接受申请，其他申请自动标记为未选中
- 教员完成订单，家长评分评价
- 按科目、年级、区域和预算筛选开放需求
- 家长取消开放需求，教员撤回待处理申请
- 查看教员完成订单数和平均评分
- 管理员查看需求、匹配率、认证教员和热门科目统计
- 教员记录每次上课日期、课时、内容和费用
- 审核、申请、接单、完课、评价和预约消息通知
- 用户提交订单投诉，管理员处理投诉
- 教员上传学生证和资格证审核材料
- 根据科目、预算和发布时间推荐需求
- 家长收藏教员并发起直接预约
- 教员、家长和管理员菜单及按钮权限

## 环境要求

- JDK 17
- Maven 3.9+
- Node.js
- Docker Desktop

## 本地启动

```powershell
# MySQL、Redis、Nacos
docker compose -f docker/docker-compose.yml up -d ruoyi-mysql ruoyi-redis ruoyi-nacos

# 后端构建
mvn clean package -DskipTests

# 前端
cd ruoyi-ui
npm.cmd install --no-package-lock --cache ..\.npm-cache --registry=https://registry.npmmirror.com
npm.cmd run dev
```

在 IDEA 中依次启动：

1. `RuoYiGatewayApplication`
2. `RuoYiAuthApplication`
3. `RuoYiSystemApplication`
4. `RuoYiFileApplication`（上传教员证件时需要）

已有数据库升级时，请先执行一次 `sql/tutoring_features.sql`；新建 Docker 数据库会自动执行该脚本。

## 服务地址

| 服务 | 地址 |
|---|---|
| 前端 | `http://localhost:80` |
| 网关 | `http://localhost:8080` |
| Nacos 控制台 | `http://localhost:8081` |
| MySQL | `localhost:3307` |
| Redis | `localhost:6379` |

## 演示账号

| 身份 | 账号 | 密码 |
|---|---|---|
| 管理员 | `admin` | `admin123` |
| 大学生教员 | `tutor_demo` | `admin123` |
| 家长学员 | `client_demo` | `admin123` |

演示流程：家长发布需求 → 教员申请 → 家长接受 → 教员完成 → 家长评价。

## 验证

```powershell
mvn clean package -DskipTests
cd ruoyi-ui
npm.cmd run build:prod
```

项目基于 MIT 许可的开源代码开发，原始许可声明保留在 [LICENSE](LICENSE) 中。
