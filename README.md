# 大学生家教管理系统

面向大学生教员、家长学员和平台管理员的家教业务管理系统，采用 Spring Cloud、Spring Boot、Vue 2、MySQL、Redis 和 Nacos 构建。

## 已实现功能

- 教员维护个人资料，管理员审核
- 家长发布家教需求
- 已审核教员申请需求
- 家长接受申请，其他申请自动标记为未选中
- 教员完成订单，家长评分评价
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
