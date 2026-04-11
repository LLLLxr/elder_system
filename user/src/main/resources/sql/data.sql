-- 用户模块基础数据初始化

-- 插入基础角色数据（注意：role_code 不带 ROLE_ 前缀，Spring Security 会自动添加）
INSERT INTO `role` (`role_name`, `role_code`, `description`, `status`) VALUES
('超级管理员', 'ADMIN', '系统超级管理员，拥有所有权限', 1),
('普通用户', 'USER', '普通注册用户，拥有基本权限', 1),
('老人用户', 'ELDER', '已验证的老人用户，拥有老人相关权限', 1),
('子女用户', 'CHILD', '已验证的老人子女用户，拥有子女相关权限', 1),
('客服人员', 'CUSTOMER_SERVICE', '客服人员，拥有处理客户服务的权限', 1);

-- 插入基础权限数据
INSERT INTO `permission` (`permission_name`, `permission_code`, `description`, `status`) VALUES
('用户管理', 'USER_MANAGE', '用户管理权限', 1),
('用户查看', 'USER_VIEW', '查看用户信息权限', 1),
('用户新增', 'USER_ADD', '新增用户权限', 1),
('用户修改', 'USER_EDIT', '修改用户信息权限', 1),
('用户删除', 'USER_DELETE', '删除用户权限', 1),
('角色管理', 'ROLE_MANAGE', '角色管理权限', 1),
('权限管理', 'PERMISSION_MANAGE', '权限管理权限', 1),
('身份证验证', 'ID_CARD_VERIFY', '身份证验证权限', 1),
('人脸验证', 'FACE_VERIFY', '人脸验证权限', 1),
('老人服务', 'ELDER_SERVICE', '老人服务权限', 1),
('子女服务', 'CHILD_SERVICE', '子女服务权限', 1),
('订单管理', 'ORDER_MANAGE', '订单管理权限', 1),
('订单查看', 'ORDER_VIEW', '查看订单权限', 1),
('订单创建', 'ORDER_ADD', '创建订单权限', 1),
('订单修改', 'ORDER_EDIT', '修改订单权限', 1),
('订单取消', 'ORDER_CANCEL', '取消订单权限', 1),
('商品管理', 'PRODUCT_MANAGE', '商品管理权限', 1),
('商品查看', 'PRODUCT_VIEW', '查看商品权限', 1),
('商品新增', 'PRODUCT_ADD', '新增商品权限', 1),
('商品修改', 'PRODUCT_EDIT', '修改商品权限', 1),
('商品删除', 'PRODUCT_DELETE', '删除商品权限', 1),
('系统管理', 'SYSTEM_MANAGE', '系统管理权限', 1),
('日志查看', 'LOG_VIEW', '查看系统日志权限', 1),
('数据统计', 'DATA_STATISTICS', '数据统计分析权限', 1),
('客服功能', 'CUSTOMER_SERVICE', '客服功能权限', 1),
('客服回复', 'CUSTOMER_REPLY', '客服回复权限', 1);

-- 为超级管理员分配所有权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `role` r CROSS JOIN `permission` p
WHERE r.role_code = 'ADMIN' AND p.delete_flag = 0;

-- 为普通用户分配基本权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `role` r
JOIN `permission` p ON p.permission_code IN ('USER_VIEW', 'ORDER_VIEW', 'PRODUCT_VIEW')
WHERE r.role_code = 'USER';

-- 为老人用户分配老人相关权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `role` r
JOIN `permission` p ON p.permission_code IN ('USER_VIEW', 'ELDER_SERVICE', 'ORDER_VIEW', 'ORDER_ADD', 'ORDER_EDIT', 'ORDER_CANCEL', 'PRODUCT_VIEW')
WHERE r.role_code = 'ELDER';

-- 为子女用户分配子女相关权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `role` r
JOIN `permission` p ON p.permission_code IN ('USER_VIEW', 'CHILD_SERVICE', 'ORDER_VIEW', 'ORDER_ADD', 'ORDER_EDIT', 'ORDER_CANCEL', 'PRODUCT_VIEW')
WHERE r.role_code = 'CHILD';

-- 为客服人员分配客服相关权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `role` r
JOIN `permission` p ON p.permission_code IN ('USER_VIEW', 'CUSTOMER_SERVICE', 'CUSTOMER_REPLY', 'ORDER_VIEW', 'ORDER_EDIT', 'ORDER_CANCEL', 'PRODUCT_VIEW')
WHERE r.role_code = 'CUSTOMER_SERVICE';

-- 插入默认管理员用户 (密码: admin123)
-- 注意: 实际生产环境中应使用更安全的密码
INSERT INTO `user` (`username`, `password`, `real_name`, `status`) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '系统管理员', 1);

-- 为管理员分配超级管理员角色
INSERT INTO `user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `user` u JOIN `role` r ON r.role_code = 'ADMIN'
WHERE u.username = 'admin';

-- 插入测试用户数据
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `email`, `status`) VALUES
('testuser', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '测试用户', '13800138000', 'test@example.com', 1),
('elder001', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '张大爷', '13900139000', 'elder@example.com', 1),
('child001', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '李儿子', '13700137000', 'child@example.com', 1);

-- 为测试用户分配角色
INSERT INTO `user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `user` u JOIN `role` r ON r.role_code = 'USER'
WHERE u.username IN ('testuser', 'elder001', 'child001');

-- 为老人用户分配老人角色
INSERT INTO `user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `user` u JOIN `role` r ON r.role_code = 'ELDER'
WHERE u.username = 'elder001';

-- 为子女用户分配子女角色
INSERT INTO `user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `user` u JOIN `role` r ON r.role_code = 'CHILD'
WHERE u.username = 'child001';