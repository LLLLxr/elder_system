# 用户模块API测试指南

本指南提供了user中所有用户相关API的详细测试方法和示例。

## 基础信息

- **服务地址**: http://localhost:8084/user-service
- **H2控制台**: http://localhost:8084/user-service/h2-console
- **测试数据库**: H2内存数据库

## 测试用户凭证

| 用户名 | 密码 | 角色 | 描述 |
|--------|------|------|------|
| admin | admin123 | ADMIN | 系统管理员，拥有所有权限 |
| user | user123 | USER | 普通用户，拥有基本权限 |
| elder | elder123 | ELDER | 老人用户，拥有老人相关权限 |

---

## 1. 用户认证相关API

### 1.1 用户登录
**接口地址**: `POST /api/auth/login`  
**接口描述**: 用户登录获取JWT令牌  
**权限要求**: 无需登录  
**请求参数**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```
**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "roles": [
        {
          "id": 1,
          "roleName": "超级管理员",
          "roleCode": "ADMIN"
        }
      ]
    }
  }
}
```

### 1.2 刷新令牌
**接口地址**: `POST /api/auth/refresh`  
**接口描述**: 使用刷新令牌获取新的访问令牌  
**权限要求**: 无需登录（但需要有效的刷新令牌）  
**请求头**: `Authorization: Bearer <refresh_token>`  
**响应示例**:
```json
{
  "code": 200,
  "message": "刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

---

## 2. 用户管理API

### 2.1 用户注册
**接口地址**: `POST /api/users/register`  
**接口描述**: 新用户注册  
**权限要求**: 无需登录  
**请求参数**:
```json
{
  "username": "testuser",
  "password": "password123",
  "realName": "测试用户",
  "phone": "13800138000",
  "email": "testuser@example.com"
}
```
**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 4,
    "username": "testuser",
    "realName": "测试用户",
    "phone": "13800138000",
    "email": "testuser@example.com",
    "status": 1,
    "createTime": "2026-04-11T16:47:40",
    "updateTime": "2026-04-11T16:47:40"
  }
}
```

### 2.2 分页查询用户列表
**接口地址**: `GET /api/users`  
**接口描述**: 分页查询用户列表  
**权限要求**: `user:query`  
**请求头**: `Authorization: Bearer <token>`  
**查询参数**:
```
current: 页码（默认1）
size: 每页大小（默认10）
username: 用户名（可选）
realName: 真实姓名（可选）
phone: 手机号（可选）
status: 状态（可选）
```
**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "content": [
      {
        "id": 1,
        "username": "admin",
        "realName": "系统管理员",
        "phone": "13800138000",
        "email": "admin@example.com",
        "status": 1,
        "createTime": "2026-04-11T16:47:40",
        "updateTime": "2026-04-11T16:47:40",
        "roles": [
          {
            "id": 1,
            "roleName": "超级管理员",
            "roleCode": "ADMIN"
          }
        ]
      }
    ],
    "pageable": {
      "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
      },
      "offset": 0,
      "pageNumber": 0,
      "pageSize": 10,
      "unpaged": false,
      "paged": true
    },
    "totalElements": 3,
    "totalPages": 1,
    "last": true,
    "first": true,
    "numberOfElements": 3,
    "size": 10,
    "number": 0
  }
}
```

### 2.3 获取用户详情
**接口地址**: `GET /api/users/{userId}`  
**接口描述**: 获取指定用户详情  
**权限要求**: `user:query`  
**请求头**: `Authorization: Bearer <token>`  
**路径参数**: `userId` - 用户ID  
**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "phone": "13800138000",
    "email": "admin@example.com",
    "status": 1,
    "createTime": "2026-04-11T16:47:40",
    "updateTime": "2026-04-11T16:47:40",
    "roles": [
      {
        "id": 1,
        "roleName": "超级管理员",
        "roleCode": "ADMIN"
      }
    ]
  }
}
```

### 2.4 创建用户
**接口地址**: `POST /api/users`  
**接口描述**: 创建新用户  
**权限要求**: `user:add`  
**请求头**: `Authorization: Bearer <token>`  
**请求参数**:
```json
{
  "username": "newuser",
  "password": "password123",
  "realName": "新用户",
  "phone": "13800138003",
  "email": "newuser@example.com",
  "status": 1
}
```
**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 5,
    "username": "newuser",
    "realName": "新用户",
    "phone": "13800138003",
    "email": "newuser@example.com",
    "status": 1,
    "createTime": "2026-04-11T16:47:40",
    "updateTime": "2026-04-11T16:47:40"
  }
}
```

### 2.5 更新用户
**接口地址**: `PUT /api/users/{userId}`  
**接口描述**: 更新指定用户信息  
**权限要求**: `user:edit`  
**请求头**: `Authorization: Bearer <token>`  
**路径参数**: `userId` - 用户ID  
**请求参数**:
```json
{
  "realName": "更新的用户名",
  "phone": "13800138004",
  "email": "updated@example.com",
  "status": 1
}
```
**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "更新的用户名",
    "phone": "13800138004",
    "email": "updated@example.com",
    "status": 1,
    "createTime": "2026-04-11T16:47:40",
    "updateTime": "2026-04-11T16:47:41"
  }
}
```

### 2.6 删除用户
**接口地址**: `DELETE /api/users/{userId}`  
**接口描述**: 删除指定用户  
**权限要求**: `user:delete`  
**请求头**: `Authorization: Bearer <token>`  
**路径参数**: `userId` - 用户ID  
**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 2.7 更新用户状态
**接口地址**: `PUT /api/users/{userId}/status`  
**接口描述**: 更新指定用户状态  
**权限要求**: `user:edit`  
**请求头**: `Authorization: Bearer <token>`  
**路径参数**: `userId` - 用户ID  
**查询参数**: `status` - 状态（0-禁用，1-启用）  
**响应示例**:
```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": null
}
```

### 2.8 重置用户密码
**接口地址**: `PUT /api/users/{userId}/password/reset`  
**接口描述**: 重置指定用户密码  
**权限要求**: `user:edit`  
**请求头**: `Authorization: Bearer <token>`  
**路径参数**: `userId` - 用户ID  
**查询参数**: `newPassword` - 新密码  
**响应示例**:
```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null
}
```

---

## 3. 个人信息管理API

### 3.1 获取当前登录用户信息
**接口地址**: `GET /api/users/me`  
**接口描述**: 获取当前登录用户信息  
**权限要求**: 需要登录  
**请求头**: `Authorization: Bearer <token>`  
**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "phone": "13800138000",
    "email": "admin@example.com",
    "status": 1,
    "createTime": "2026-04-11T16:47:40",
    "updateTime": "2026-04-11T16:47:40",
    "roles": [
      {
        "id": 1,
        "roleName": "超级管理员",
        "roleCode": "ADMIN"
      }
    ]
  }
}
```

### 3.2 更新当前登录用户信息
**接口地址**: `PUT /api/users/me`  
**接口描述**: 更新当前登录用户信息  
**权限要求**: 需要登录  
**请求头**: `Authorization: Bearer <token>`  
**请求参数**:
```json
{
  "realName": "更新的姓名",
  "phone": "13800138005",
  "email": "newemail@example.com"
}
```
**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "更新的姓名",
    "phone": "13800138005",
    "email": "newemail@example.com",
    "status": 1,
    "createTime": "2026-04-11T16:47:40",
    "updateTime": "2026-04-11T16:47:41"
  }
}
```

### 3.3 修改当前登录用户密码
**接口地址**: `PUT /api/users/me/password`  
**接口描述**: 修改当前登录用户密码  
**权限要求**: 需要登录  
**请求头**: `Authorization: Bearer <token>`  
**请求参数**:
```json
{
  "oldPassword": "admin123",
  "newPassword": "newpassword123"
}
```
**响应示例**:
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

---

## 4. 数据验证API

### 4.1 检查用户名是否存在
**接口地址**: `GET /api/users/check-username`  
**接口描述**: 检查用户名是否已存在  
**权限要求**: 无需登录  
**查询参数**: `username` - 用户名  
**响应示例**:
```json
{
  "code": 200,
  "message": "检查完成",
  "data": true
}
```

### 4.2 检查邮箱是否存在
**接口地址**: `GET /api/users/check-email`  
**接口描述**: 检查邮箱是否已存在  
**权限要求**: 无需登录  
**查询参数**: `email` - 邮箱地址  
**响应示例**:
```json
{
  "code": 200,
  "message": "检查完成",
  "data": false
}
```

### 4.3 检查手机号是否存在
**接口地址**: `GET /api/users/check-phone`  
**接口描述**: 检查手机号是否已存在  
**权限要求**: 无需登录  
**查询参数**: `phone` - 手机号  
**响应示例**:
```json
{
  "code": 200,
  "message": "检查完成",
  "data": false
}
```

### 4.4 检查身份证号是否存在
**接口地址**: `GET /api/users/check-idcard`  
**接口描述**: 检查身份证号是否已存在  
**权限要求**: 无需登录  
**查询参数**: `idCardNo` - 身份证号  
**响应示例**:
```json
{
  "code": 200,
  "message": "检查完成",
  "data": false
}
```

---

## 5. API测试示例

### 5.1 使用curl测试

#### 5.1.1 登录获取令牌
```bash
curl -X POST http://localhost:8084/user-service/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

#### 5.1.2 查询用户列表
```bash
# 首先从登录响应中获取token
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

curl -X GET "http://localhost:8084/user-service/api/users?current=1&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

#### 5.1.3 创建用户
```bash
curl -X POST http://localhost:8084/user-service/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "realName": "测试用户",
    "phone": "13800138006",
    "email": "testuser@example.com"
  }'
```

### 5.2 使用Postman测试

1. **创建新的Collection**，命名为"User Service API"
2. **设置环境变量**：
   - `base_url`: `http://localhost:8084/user-service`
   - `token`: 从登录响应中获取
3. **添加请求**：
   - 登录请求：`POST {{base_url}}/api/auth/login`
   - 其他请求：添加Authorization头，类型为Bearer Token，值为`{{token}}`

---

## 6. 错误码说明

| 错误码 | 错误描述 | 说明 |
|--------|----------|------|
| 200 | 成功 | 请求成功 |
| 400 | 请求参数错误 | 参数校验失败 |
| 401 | 未授权 | 需要登录或令牌无效 |
| 403 | 禁止访问 | 权限不足 |
| 404 | 资源不存在 | 用户不存在 |
| 409 | 资源冲突 | 用户名/邮箱/手机号已存在 |
| 500 | 服务器内部错误 | 系统异常 |

---

## 7. 常见问题

### 7.1 令牌过期
当收到401错误时，需要重新登录获取新的令牌。

### 7.2 权限不足
当收到403错误时，请确认当前用户是否具有相应的权限。

### 7.3 参数校验失败
当收到400错误时，请检查请求参数是否符合要求。

### 7.4 资源冲突
当收到409错误时，说明用户名、邮箱或手机号已存在。

---

## 8. 数据库操作

### 8.1 查看用户数据
1. 访问H2控制台：http://localhost:8084/user-service/h2-console
2. 输入连接信息：
   - JDBC URL: `jdbc:h2:mem:testdb`
   - User Name: `sa`
   - Password: 留空
3. 点击"Connect"按钮
4. 执行SQL查询：
   ```sql
   SELECT * FROM sys_user;
   SELECT * FROM role;
   SELECT * FROM permission;
   SELECT * FROM user_role;
   SELECT * FROM role_permission;
   ```

### 8.2 清理测试数据
```sql
-- 清理用户角色关系
DELETE FROM user_role WHERE user_id > 3;

-- 清理用户
DELETE FROM sys_user WHERE id > 3;
```

---

## 9. 性能测试建议

### 9.1 并发测试
使用JMeter或类似工具进行并发测试，建议：
- 登录接口：100并发用户
- 查询接口：200并发用户
- 创建用户：50并发用户

### 9.2 监控指标
- 响应时间
- 吞吐量
- 错误率
- 资源使用率（CPU、内存、数据库连接）