-- H2 Database Test Data Initialization

-- Insert test roles
INSERT INTO role (id, role_name, role_code, description, status, create_time, update_time) VALUES
(1, '超级管理员', 'ADMIN', '系统超级管理员，拥有所有权限', 1, NOW(), NOW()),
(2, '普通用户', 'USER', '普通注册用户，拥有基本权限', 1, NOW(), NOW()),
(3, '老人用户', 'ELDER', '已验证的老人用户，拥有老人相关权限', 1, NOW(), NOW()),
(4, '子女用户', 'CHILD', '已验证的老人子女用户，拥有子女相关权限', 1, NOW(), NOW()),
(5, '客服人员', 'CUSTOMER_SERVICE', '客服人员，拥有处理客户服务的权限', 1, NOW(), NOW());

-- Insert test permissions
INSERT INTO permission (id, permission_name, permission_code, description, status, create_time, update_time) VALUES
(1, '用户管理', 'USER_MANAGE', '用户管理权限', 1, NOW(), NOW()),
(2, '用户查看', 'USER_VIEW', '查看用户信息权限', 1, NOW(), NOW()),
(3, '用户新增', 'USER_ADD', '新增用户权限', 1, NOW(), NOW()),
(4, '用户修改', 'USER_EDIT', '修改用户信息权限', 1, NOW(), NOW()),
(5, '用户删除', 'USER_DELETE', '删除用户权限', 1, NOW(), NOW()),
(6, '角色管理', 'ROLE_MANAGE', '角色管理权限', 1, NOW(), NOW()),
(7, '权限管理', 'PERMISSION_MANAGE', '权限管理权限', 1, NOW(), NOW()),
(8, '身份证验证', 'ID_CARD_VERIFY', '身份证验证权限', 1, NOW(), NOW()),
(9, '人脸验证', 'FACE_VERIFY', '人脸验证权限', 1, NOW(), NOW()),
(10, '老人服务', 'ELDER_SERVICE', '老人服务权限', 1, NOW(), NOW()),
(11, '子女服务', 'CHILD_SERVICE', '子女服务权限', 1, NOW(), NOW()),
(12, '订单管理', 'ORDER_MANAGE', '订单管理权限', 1, NOW(), NOW()),
(13, '订单查看', 'ORDER_VIEW', '查看订单权限', 1, NOW(), NOW()),
(14, '订单创建', 'ORDER_ADD', '创建订单权限', 1, NOW(), NOW()),
(15, '订单修改', 'ORDER_EDIT', '修改订单权限', 1, NOW(), NOW()),
(16, '订单取消', 'ORDER_CANCEL', '取消订单权限', 1, NOW(), NOW());

-- Assign all permissions to ADMIN role
INSERT INTO role_permission (role_id, permission_id, create_time, update_time)
SELECT r.id, p.id, NOW(), NOW()
FROM role r CROSS JOIN permission p
WHERE r.role_code = 'ADMIN';

-- Assign basic permissions to USER role
INSERT INTO role_permission (role_id, permission_id, create_time, update_time)
SELECT r.id, p.id, NOW(), NOW()
FROM role r
JOIN permission p ON p.permission_code IN ('USER_VIEW', 'ORDER_VIEW')
WHERE r.role_code = 'USER';

-- Insert test users
INSERT INTO sys_user (id, username, password, real_name, phone, email, status, id_card_verified, face_verified, delete_flag, create_time, update_time) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTHepUw4uO6O26lH3xAeQ0BhUvKK', '系统管理员', '13800138000', 'admin@example.com', 1, 1, 1, 0, NOW(), NOW()),
(2, 'user', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTHepUw4uO6O26lH3xAeQ0BhUvKK', '测试用户', '13800138001', 'user@example.com', 1, 0, 0, 0, NOW(), NOW()),
(3, 'elder', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTHepUw4uO6O26lH3xAeQ0BhUvKK', '老人用户', '13800138002', 'elder@example.com', 1, 1, 0, 0, NOW(), NOW());

-- Assign roles to test users
INSERT INTO user_role (user_id, role_id, create_time, update_time) VALUES
(1, 1, NOW(), NOW()),  -- admin -> ADMIN
(2, 2, NOW(), NOW()),  -- user -> USER
(3, 3, NOW(), NOW());  -- elder -> ELDER