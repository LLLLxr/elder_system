# 用户模块

## 模块简介

用户模块是智慧老人系统的核心模块之一，负责用户管理、认证授权、身份验证等功能。

## 功能特点

- 用户管理：用户的增删改查、状态管理
- 认证授权：基于JWT的认证机制，支持角色权限控制
- 身份验证：集成身份证验证和人脸验证功能
- 安全性：密码加密存储，防止暴力破解
- 操作日志：基于AOP的用户操作日志记录
- Token黑名单：基于Redis的JWT令牌黑名单机制（支持安全退出）
- 角色权限管理：完整的RBAC角色权限管理体系
- 数据初始化：启动时自动初始化基础角色、权限和管理员用户

## 技术栈

- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- JWT (JSON Web Token / jjwt 0.12.3)
- MySQL
- Redis（用于Token黑名单和缓存）
- Spring Cloud OpenFeign（服务间调用）
- SpringDoc OpenAPI（API文档）
- Lombok

## 数据库表结构

模块包含以下数据表：

- `user` - 用户表
- `role` - 角色表
- `permission` - 权限表
- `user_role` - 用户角色关联表
- `role_permission` - 角色权限关联表
- `id_card_verify_record` - 身份证验证记录表
- `face_verify_record` - 人脸验证记录表
- `user_operation_log` - 用户操作日志表

## API接口

### 认证相关接口

- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户退出
- `POST /api/auth/refresh` - 刷新令牌
- `GET /api/auth/permissions` - 获取用户权限
- `GET /api/auth/roles` - 获取用户角色
- `GET /api/auth/check-permission` - 检查权限

### 用户管理接口

- `GET /api/users` - 分页查询用户列表
- `GET /api/users/{userId}` - 获取用户详情
- `POST /api/users` - 创建用户
- `PUT /api/users/{userId}` - 更新用户
- `DELETE /api/users/{userId}` - 删除用户
- `PUT /api/users/{userId}/status` - 更新用户状态
- `PUT /api/users/{userId}/password/reset` - 重置用户密码
- `POST /api/users/register` - 用户注册
- `GET /api/users/me` - 获取当前登录用户信息
- `PUT /api/users/me` - 更新当前登录用户信息
- `PUT /api/users/me/password` - 修改当前登录用户密码

### 角色管理接口

- `GET /api/roles` - 查询角色列表
- `GET /api/roles/with-permissions` - 查询所有角色及权限
- `GET /api/roles/user/{userId}` - 查询用户角色
- `GET /api/roles/user/{userId}/permissions` - 查询用户权限
- `POST /api/roles/user/{userId}/assign/{roleId}` - 为用户分配角色
- `DELETE /api/roles/user/{userId}/remove/{roleId}` - 移除用户角色
- `POST /api/roles/{roleId}/permission/{permissionId}` - 为角色分配权限
- `DELETE /api/roles/{roleId}/permission/{permissionId}` - 移除角色权限
- `GET /api/roles/by-permission` - 根据权限编码查询角色

### 权限管理接口

- `GET /api/permissions` - 查询权限列表
- `GET /api/permissions/tree` - 获取权限树
- `GET /api/permissions/code/{permissionCode}` - 根据权限编码查询权限
- `GET /api/permissions/role/{roleId}` - 查询角色的权限列表
- `GET /api/permissions/user/{userId}` - 查询用户的权限列表
- `POST /api/permissions` - 创建权限
- `PUT /api/permissions/{permissionId}` - 更新权限
- `DELETE /api/permissions/{permissionId}` - 删除权限
- `PUT /api/permissions/{permissionId}/status` - 更新权限状态

### 验证接口

- `POST /api/auth/id-card/verify` - 身份证验证
- `POST /api/auth/face/verify` - 人脸验证

### 检查接口

- `GET /api/users/check-username` - 检查用户名是否存在
- `GET /api/users/check-email` - 检查邮箱是否存在
- `GET /api/users/check-phone` - 检查手机号是否存在
- `GET /api/users/check-idcard` - 检查身份证号是否存在

## 启动方式

1. 确保MySQL和Redis服务已启动
2. （可选）手动执行数据库初始化脚本：`src/main/resources/sql/schema.sql` 和 `src/main/resources/sql/data.sql`
   - 注意：应用启动时会通过 `DataInitializer` 自动初始化基础角色、权限和管理员用户
3. 修改配置文件：`src/main/resources/application.yml`
4. 运行主程序：`org.smart_elder_system.user.UserApplication`

## 配置说明

主要配置项：

- `spring.datasource` - 数据库连接配置
- `spring.data.redis` - Redis连接配置（Spring Boot 3.x）
- `jwt.secret` - JWT密钥
- `jwt.expiration` - JWT过期时间（毫秒）
- `app.user.admin` - 默认管理员配置（用户名/密码/邮箱）

## 第三方服务集成

### 身份证验证服务

目前模拟了第三方身份证验证服务，实际项目中应该替换为真实的第三方服务调用。

### 人脸验证服务

目前模拟了第三方人脸验证服务，实际项目中应该替换为真实的第三方服务调用。

## 开发计划

- [x] 添加用户操作日志记录（AOP实现）
- [x] 添加 Redis Token黑名单（安全退出支持）
- [x] 角色权限管理API
- [x] 数据初始化组件
- [x] 添加接口文档（SpringDoc OpenAPI）
- [ ] 集成真实的第三方身份证验证服务
- [ ] 集成真实的人脸验证服务
- [ ] 完善单元测试
- [ ] 性能优化