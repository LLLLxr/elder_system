-- H2 Database Test Data Initialization

-- Insert test roles
INSERT INTO role (id, role_name, role_code, description, status, created_date_time_utc, last_modified_date_time_utc, version) VALUES
(1, '超级管理员', 'ADMIN', '系统超级管理员，拥有所有权限', 1, NOW(), NOW(), 1),
(2, '普通用户', 'USER', '普通注册用户，拥有基本权限', 1, NOW(), NOW(), 1),
(3, '老人用户', 'ELDER', '已验证的老人用户，拥有老人相关权限', 1, NOW(), NOW(), 1),
(4, '子女用户', 'CHILD', '已验证的老人子女用户，拥有子女相关权限', 1, NOW(), NOW(), 1),
(5, '客服人员', 'CUSTOMER_SERVICE', '客服人员，拥有处理客户服务的权限', 1, NOW(), NOW(), 1);

-- Insert test permissions
INSERT INTO permission (id, permission_name, permission_code, description, status, created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version) VALUES
(1, '用户管理', 'USER_MANAGE', '用户管理权限', 1, 'system', 'system', NOW(), NOW(), 1),
(2, '用户查看', 'USER_VIEW', '查看用户信息权限', 1, 'system', 'system', NOW(), NOW(), 1),
(3, '用户新增', 'USER_ADD', '新增用户权限', 1, 'system', 'system', NOW(), NOW(), 1),
(4, '用户修改', 'USER_EDIT', '修改用户信息权限', 1, 'system', 'system', NOW(), NOW(), 1),
(5, '用户删除', 'USER_DELETE', '删除用户权限', 1, 'system', 'system', NOW(), NOW(), 1),
(6, '角色管理', 'ROLE_MANAGE', '角色管理权限', 1, 'system', 'system', NOW(), NOW(), 1),
(7, '权限管理', 'PERMISSION_MANAGE', '权限管理权限', 1, 'system', 'system', NOW(), NOW(), 1),
(8, '身份证验证', 'ID_CARD_VERIFY', '身份证验证权限', 1, 'system', 'system', NOW(), NOW(), 1),
(9, '人脸验证', 'FACE_VERIFY', '人脸验证权限', 1, 'system', 'system', NOW(), NOW(), 1),
(10, '老人服务', 'ELDER_SERVICE', '老人服务权限', 1, 'system', 'system', NOW(), NOW(), 1),
(11, '子女服务', 'CHILD_SERVICE', '子女服务权限', 1, 'system', 'system', NOW(), NOW(), 1),
(12, '订单管理', 'ORDER_MANAGE', '订单管理权限', 1, 'system', 'system', NOW(), NOW(), 1),
(13, '订单查看', 'ORDER_VIEW', '查看订单权限', 1, 'system', 'system', NOW(), NOW(), 1),
(14, '订单创建', 'ORDER_ADD', '创建订单权限', 1, 'system', 'system', NOW(), NOW(), 1),
(15, '订单修改', 'ORDER_EDIT', '修改订单权限', 1, 'system', 'system', NOW(), NOW(), 1),
(16, '订单取消', 'ORDER_CANCEL', '取消订单权限', 1, 'system', 'system', NOW(), NOW(), 1),
(17, '旅程需求评估通过', 'journey:assessment:approve', '服务旅程需求评估通过权限', 1, 'system', 'system', NOW(), NOW(), 1),
(18, '旅程需求评估驳回', 'journey:assessment:reject', '服务旅程需求评估驳回权限', 1, 'system', 'system', NOW(), NOW(), 1),
(19, '旅程退回需求评估', 'journey:return:assessment', '服务旅程退回需求评估权限', 1, 'system', 'system', NOW(), NOW(), 1),
(20, '旅程健康评估通过', 'journey:health:approve', '服务旅程健康评估通过权限', 1, 'system', 'system', NOW(), NOW(), 1),
(21, '旅程健康评估驳回', 'journey:health:reject', '服务旅程健康评估驳回权限', 1, 'system', 'system', NOW(), NOW(), 1),
(22, '旅程退回健康评估', 'journey:return:health', '服务旅程退回健康评估权限', 1, 'system', 'system', NOW(), NOW(), 1),
(23, '旅程协议签署', 'journey:agreement:sign', '服务旅程协议签署权限', 1, 'system', 'system', NOW(), NOW(), 1),
(24, '旅程评价改进', 'journey:review:improve', '服务旅程评价改进权限', 1, 'system', 'system', NOW(), NOW(), 1),
(25, '旅程评价续约', 'journey:review:renew', '服务旅程评价续约权限', 1, 'system', 'system', NOW(), NOW(), 1),
(26, '旅程评价终止', 'journey:review:terminate', '服务旅程评价终止权限', 1, 'system', 'system', NOW(), NOW(), 1),
(27, '旅程申请撤回', 'journey:withdraw', '服务旅程申请撤回权限', 1, 'system', 'system', NOW(), NOW(), 1);

-- Assign all permissions to ADMIN role
INSERT INTO role_permission (role_id, permission_id, created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version)
SELECT r.id, p.id, 'system', 'system', NOW(), NOW(), 1
FROM role r CROSS JOIN permission p
WHERE r.role_code = 'ADMIN';

-- Assign basic permissions to USER role
INSERT INTO role_permission (role_id, permission_id, created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version)
SELECT r.id, p.id, 'system', 'system', NOW(), NOW(), 1
FROM role r
JOIN permission p ON p.permission_code IN ('USER_VIEW', 'ORDER_VIEW', 'journey:withdraw')
WHERE r.role_code = 'USER';

-- Assign journey permissions to CUSTOMER_SERVICE role
INSERT INTO role_permission (role_id, permission_id, created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version)
SELECT r.id, p.id, 'system', 'system', NOW(), NOW(), 1
FROM role r
JOIN permission p ON p.permission_code IN (
    'journey:assessment:approve',
    'journey:assessment:reject',
    'journey:return:assessment',
    'journey:health:approve',
    'journey:health:reject',
    'journey:return:health',
    'journey:agreement:sign',
    'journey:review:improve',
    'journey:review:renew',
    'journey:review:terminate',
    'journey:withdraw'
)
WHERE r.role_code = 'CUSTOMER_SERVICE';

-- Assign withdraw permission to ELDER and CHILD roles
INSERT INTO role_permission (role_id, permission_id, created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version)
SELECT r.id, p.id, 'system', 'system', NOW(), NOW(), 1
FROM role r
JOIN permission p ON p.permission_code = 'journey:withdraw'
WHERE r.role_code IN ('ELDER', 'CHILD');

-- Insert test users
INSERT INTO sys_user (id, username, password, real_name, phone, email, status, id_card_verified, face_verified, delete_flag, created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version) VALUES
(1, 'admin', '$2a$10$TMW5dWBpRbGmEuQOb24m2OnvNZSA8D2AtVW4kKd7x.EUV9UTPtZbW', '系统管理员', '13800138000', 'admin@example.com', 1, 1, 1, 0, 'system', 'system', NOW(), NOW(), 1),
(2, 'user', '$2a$10$TMW5dWBpRbGmEuQOb24m2OnvNZSA8D2AtVW4kKd7x.EUV9UTPtZbW', '测试用户', '13800138001', 'user@example.com', 1, 0, 0, 0, 'system', 'system', NOW(), NOW(), 1),
(3, 'elder', '$2a$10$TMW5dWBpRbGmEuQOb24m2OnvNZSA8D2AtVW4kKd7x.EUV9UTPtZbW', '老人用户', '13800138002', 'elder@example.com', 1, 1, 0, 0, 'system', 'system', NOW(), NOW(), 1);

-- Assign roles to test users
INSERT INTO user_role (user_id, role_id, created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version) VALUES
(1, 1, 'system', 'system', NOW(), NOW(), 1),  -- admin -> ADMIN
(2, 2, 'system', 'system', NOW(), NOW(), 1),  -- user -> USER
(3, 3, 'system', 'system', NOW(), NOW(), 1);  -- elder -> ELDER