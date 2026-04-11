# H2数据库测试指南

本指南帮助您使用H2内存数据库快速测试user。

## 什么是H2数据库？

H2是一个开源的内存数据库，非常适合快速测试和原型开发。它不需要安装，完全在内存中运行，应用程序关闭后数据会被清除。

## 先决条件

确保已安装：
- JDK 21或更高版本
- Maven 3.6或更高版本

## 快速开始

### 1. 使用测试配置运行应用程序

在项目根目录执行以下命令：

```bash
# 使用test配置运行应用程序
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

或者直接在IDE中运行，并设置活动配置文件为`test`。

### 2. 验证应用程序启动

应用程序启动后，您应该看到类似以下的日志：

```
2026-04-11 15:30:00 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer : Tomcat started on port(s): 8084 (http)
2026-04-11 15:30:00 [main] INFO  o.s.b.a.Application : Started UserApplication in 5.234 seconds (JVM running for 6.789)
```

### 3. 访问H2控制台

1. 打开浏览器，访问：http://localhost:8084/user-service/h2-console
2. 使用以下登录信息：
   - JDBC URL: jdbc:h2:mem:testdb
   - User Name: sa
   - Password: (留空)
3. 点击"Connect"按钮

### 4. 查看测试数据

连接成功后，您可以看到以下表：
- `USER` - 用户表
- `ROLE` - 角色表
- `PERMISSION` - 权限表
- `USER_ROLE` - 用户角色关联表
- `ROLE_PERMISSION` - 角色权限关联表

### 5. 测试数据

数据库中预置了以下测试数据：

#### 测试用户
| 用户名 | 密码 | 角色 | 描述 |
|--------|------|------|------|
| admin | password | ADMIN | 系统管理员，拥有所有权限 |
| user | password | USER | 普通用户，拥有基本权限 |
| elder | password | ELDER | 老人用户 |

#### 测试角色
- ADMIN: 超级管理员
- USER: 普通用户
- ELDER: 老人用户
- CHILD: 子女用户
- CUSTOMER_SERVICE: 客服人员

#### 测试权限
预置了16种权限，包括用户管理、订单管理、商品管理等。

## API测试

### 1. 获取访问令牌

首先，使用测试用户登录获取JWT令牌：

```bash
curl -X POST http://localhost:8083/user-service/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'
```

响应示例：
```json
{
  "status": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

### 2. 测试用户API

使用获取的令牌访问用户API：

```bash
curl -X GET http://localhost:8083/user-service/api/users \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 3. 查看API文档

访问Swagger UI文档：http://localhost:8083/user-service/swagger-ui.html

## 故障排除

### 1. 数据库连接问题

如果出现数据库连接错误，请检查：
- 应用程序是否使用了`test`配置文件
- H2依赖是否正确添加到pom.xml

### 2. 初始化脚本问题

如果数据没有正确初始化，请：
- 检查`schema-h2.sql`和`data-h2.sql`文件是否存在
- 确认SQL语句语法正确

### 3. 端口冲突

如果8083端口被占用，可以：
- 修改`application-test.yml`中的端口设置
- 或者停止占用8083端口的程序

### 4. 权限问题

如果遇到权限错误，请：
- 使用admin用户登录（拥有所有权限）
- 确保JWT令牌正确传递

## 停止应用程序

按`Ctrl+C`停止运行中的应用程序。由于H2是内存数据库，所有数据都会被清除。

## 下一步

H2数据库测试通过后，您可以：

1. **配置MySQL数据库**：切换到开发或生产环境配置，连接真实的MySQL数据库
2. **编写测试用例**：使用JUnit和Spring Boot Test编写自动化测试
3. **部署到测试环境**：将应用程序部署到测试服务器进行进一步测试

## 相关配置文件

- `application-test.yml` - H2测试配置
- `schema-h2.sql` - H2数据库结构
- `data-h2.sql` - H2测试数据
- `pom.xml` - Maven依赖配置

如有问题，请参考Spring Boot官方文档或H2数据库官方文档。