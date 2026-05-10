-- 用户模块基础数据初始化

-- 插入基础角色数据（注意：role_code 不带 ROLE_ 前缀，Spring Security 会自动添加）
INSERT INTO `role` (`role_name`, `role_code`, `description`, `status`) VALUES
('超级管理员', 'ADMIN', '系统超级管理员，拥有所有权限', 1),
('普通用户', 'USER', '普通注册用户，拥有基本权限', 1),
('老人用户', 'ELDER', '已验证的老人用户，拥有老人相关权限', 1),
('子女用户', 'CHILD', '已验证的老人子女用户，拥有子女相关权限', 1),
('客服人员', 'CUSTOMER_SERVICE', '客服人员，拥有处理客户服务的权限', 1),
('护士', 'NURSE', '护士，拥有健康体检表录入与查看权限', 1),
('责任医生', 'DOCTOR', '责任医生，拥有健康体检表录入与查看权限', 1),
('家属用户', 'FAMILY', '家属端预约参观与查看我的预约权限', 1),
('护理员', 'CAREGIVER', '护理员资质申请与查看状态权限', 1),
('医护人员', 'MEDICAL_STAFF', '医护审核家属预约与护理员资质权限', 1);

-- 插入基础权限数据
INSERT INTO `permission` (`permission_name`, `permission_code`, `description`, `status`, `created_by`, `last_modified_by`) VALUES
('用户管理', 'USER_MANAGE', '用户管理权限', 1, 'system', 'system'),
('用户查看', 'USER_VIEW', '查看用户信息权限', 1, 'system', 'system'),
('用户新增', 'USER_ADD', '新增用户权限', 1, 'system', 'system'),
('用户修改', 'USER_EDIT', '修改用户信息权限', 1, 'system', 'system'),
('用户删除', 'USER_DELETE', '删除用户权限', 1, 'system', 'system'),
('角色管理', 'ROLE_MANAGE', '角色管理权限', 1, 'system', 'system'),
('权限管理', 'PERMISSION_MANAGE', '权限管理权限', 1, 'system', 'system'),
('身份证验证', 'ID_CARD_VERIFY', '身份证验证权限', 1, 'system', 'system'),
('人脸验证', 'FACE_VERIFY', '人脸验证权限', 1, 'system', 'system'),
('老人服务', 'ELDER_SERVICE', '老人服务权限', 1, 'system', 'system'),
('子女服务', 'CHILD_SERVICE', '子女服务权限', 1, 'system', 'system'),
('订单管理', 'ORDER_MANAGE', '订单管理权限', 1, 'system', 'system'),
('订单查看', 'ORDER_VIEW', '查看订单权限', 1, 'system', 'system'),
('订单创建', 'ORDER_ADD', '创建订单权限', 1, 'system', 'system'),
('订单修改', 'ORDER_EDIT', '修改订单权限', 1, 'system', 'system'),
('订单取消', 'ORDER_CANCEL', '取消订单权限', 1, 'system', 'system'),
('商品管理', 'PRODUCT_MANAGE', '商品管理权限', 1, 'system', 'system'),
('商品查看', 'PRODUCT_VIEW', '查看商品权限', 1, 'system', 'system'),
('商品新增', 'PRODUCT_ADD', '新增商品权限', 1, 'system', 'system'),
('商品修改', 'PRODUCT_EDIT', '修改商品权限', 1, 'system', 'system'),
('商品删除', 'PRODUCT_DELETE', '删除商品权限', 1, 'system', 'system'),
('系统管理', 'SYSTEM_MANAGE', '系统管理权限', 1, 'system', 'system'),
('日志查看', 'LOG_VIEW', '查看系统日志权限', 1, 'system', 'system'),
('数据统计', 'DATA_STATISTICS', '数据统计分析权限', 1, 'system', 'system'),
('客服功能', 'CUSTOMER_SERVICE', '客服功能权限', 1, 'system', 'system'),
('客服回复', 'CUSTOMER_REPLY', '客服回复权限', 1, 'system', 'system'),
('旅程需求评估通过', 'journey:assessment:approve', '服务旅程需求评估通过权限', 1, 'system', 'system'),
('旅程需求评估驳回', 'journey:assessment:reject', '服务旅程需求评估驳回权限', 1, 'system', 'system'),
('旅程退回需求评估', 'journey:return:assessment', '服务旅程退回需求评估权限', 1, 'system', 'system'),
('旅程健康评估通过', 'journey:health:approve', '服务旅程健康评估通过权限', 1, 'system', 'system'),
('旅程健康评估驳回', 'journey:health:reject', '服务旅程健康评估驳回权限', 1, 'system', 'system'),
('旅程退回健康评估', 'journey:return:health', '服务旅程退回健康评估权限', 1, 'system', 'system'),
('旅程协议签署', 'journey:agreement:sign', '服务旅程协议签署权限', 1, 'system', 'system'),
('旅程评价改进', 'journey:review:improve', '服务旅程评价改进权限', 1, 'system', 'system'),
('旅程评价续约', 'journey:review:renew', '服务旅程评价续约权限', 1, 'system', 'system'),
('旅程评价终止', 'journey:review:terminate', '服务旅程评价终止权限', 1, 'system', 'system'),
('旅程申请撤回', 'journey:withdraw', '服务旅程申请撤回权限', 1, 'system', 'system'),
('旅程待办列表', 'journey:task:list', '查看服务旅程待办列表权限', 1, 'system', 'system'),
('旅程日志列表', 'journey:log:list', '查看服务旅程日志列表权限', 1, 'system', 'system'),
('健康体检表创建', 'health:check-form:create', '健康体检表创建权限', 1, 'system', 'system'),
('健康体检表查看', 'health:check-form:read', '健康体检表查看权限', 1, 'system', 'system'),
('健康体检表列表', 'health:check-form:list', '健康体检表列表权限', 1, 'system', 'system'),
('家属预约时段查看', 'admission:family-visit-slot:read', '查看家属预约参观时段权限', 1, 'system', 'system'),
('家属预约提交', 'admission:family-visit-reservation:create', '提交家属预约参观权限', 1, 'system', 'system'),
('家属预约我的列表', 'admission:family-visit-reservation:my:list', '查看我的家属预约参观记录权限', 1, 'system', 'system'),
('家属预约审核列表', 'admission:family-visit-reservation:list', '查看家属预约参观审核列表权限', 1, 'system', 'system'),
('家属预约详情', 'admission:family-visit-reservation:detail', '查看家属预约参观详情权限', 1, 'system', 'system'),
('家属预约审核通过', 'admission:family-visit-reservation:approve', '审核通过家属预约参观权限', 1, 'system', 'system'),
('家属预约审核驳回', 'admission:family-visit-reservation:reject', '审核驳回家属预约参观权限', 1, 'system', 'system'),
('护理员资质申请提交', 'quality:caregiver-qualification:create', '提交护理员资质申请权限', 1, 'system', 'system'),
('护理员资质我的列表', 'quality:caregiver-qualification:my:list', '查看我的护理员资质申请记录权限', 1, 'system', 'system'),
('护理员资质审核列表', 'quality:caregiver-qualification:list', '查看护理员资质审核列表权限', 1, 'system', 'system'),
('护理员资质详情', 'quality:caregiver-qualification:detail', '查看护理员资质申请详情权限', 1, 'system', 'system'),
('护理员资质审核通过', 'quality:caregiver-qualification:approve', '审核通过护理员资质申请权限', 1, 'system', 'system'),
('护理员资质审核驳回', 'quality:caregiver-qualification:reject', '审核驳回护理员资质申请权限', 1, 'system', 'system'),
('护理员我的任务列表', 'care-delivery:daily-task:list', '查看护理员每日任务列表权限', 1, 'system', 'system'),
('护理员打卡提交', 'care-delivery:daily-task:check-in', '提交护理员打卡权限', 1, 'system', 'system'),
('护理员打卡我的列表', 'care-delivery:check-in:my:list', '查看护理员我的打卡记录权限', 1, 'system', 'system'),
('护士护理记录列表', 'care-delivery:nurse-care-record:list', '查看护士护理记录列表权限', 1, 'system', 'system'),
('护士护理记录详情', 'care-delivery:nurse-care-record:read', '查看护士护理记录详情权限', 1, 'system', 'system'),
('护士护理记录创建', 'care-delivery:nurse-care-record:create', '创建护士护理记录权限', 1, 'system', 'system'),
('护士护理记录更新', 'care-delivery:nurse-care-record:update', '更新护士护理记录权限', 1, 'system', 'system'),
('家属服务清单列表', 'care-delivery:family-service-plan:list', '查看家属服务清单权限', 1, 'system', 'system'),
('家属打卡记录列表', 'care-delivery:family-check-in:list', '查看家属打卡记录权限', 1, 'system', 'system'),
('家属护理记录列表', 'care-delivery:family-nurse-care-record:list', '查看家属护理记录权限', 1, 'system', 'system'),
('医生查房记录列表', 'health:doctor-round-record:list', '查看医生查房记录列表权限', 1, 'system', 'system'),
('医生查房记录详情', 'health:doctor-round-record:read', '查看医生查房记录详情权限', 1, 'system', 'system'),
('医生查房记录创建', 'health:doctor-round-record:create', '创建医生查房记录权限', 1, 'system', 'system'),
('医生查房记录更新', 'health:doctor-round-record:update', '更新医生查房记录权限', 1, 'system', 'system'),
('家属查房记录列表', 'health:family-doctor-round-record:list', '查看家属查房记录权限', 1, 'system', 'system'),
('老人绑定申请提交', 'elder-binding:request:create', '提交老人绑定申请权限', 1, 'system', 'system'),
('老人绑定申请我的列表', 'elder-binding:request:my:list', '查看我的老人绑定申请权限', 1, 'system', 'system'),
('老人绑定申请列表', 'elder-binding:request:list', '查看老人绑定申请列表权限', 1, 'system', 'system'),
('老人绑定申请详情', 'elder-binding:request:detail', '查看老人绑定申请详情权限', 1, 'system', 'system'),
('老人绑定申请审核通过', 'elder-binding:request:approve', '审核通过老人绑定申请权限', 1, 'system', 'system'),
('老人绑定申请审核驳回', 'elder-binding:request:reject', '审核驳回老人绑定申请权限', 1, 'system', 'system'),
('老人绑定列表', 'elder-binding:list', '查看老人绑定列表权限', 1, 'system', 'system'),
('老人本人绑定', 'elder-binding:self:bind', '老人本人绑定自己的老人档案权限', 1, 'system', 'system');

-- 为超级管理员分配所有权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `created_by`, `last_modified_by`)
SELECT r.id, p.id, 'system', 'system'
FROM `role` r CROSS JOIN `permission` p
WHERE r.role_code = 'ADMIN' AND p.delete_flag = 0;

-- 为普通用户分配基本权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `created_by`, `last_modified_by`)
SELECT r.id, p.id, 'system', 'system'
FROM `role` r
JOIN `permission` p ON p.permission_code IN ('USER_VIEW', 'ORDER_VIEW', 'PRODUCT_VIEW')
WHERE r.role_code = 'USER';

-- 为老人用户分配老人相关权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `created_by`, `last_modified_by`)
SELECT r.id, p.id, 'system', 'system'
FROM `role` r
JOIN `permission` p ON p.permission_code IN ('USER_VIEW', 'ELDER_SERVICE', 'ORDER_VIEW', 'ORDER_ADD', 'ORDER_EDIT', 'ORDER_CANCEL', 'PRODUCT_VIEW')
WHERE r.role_code = 'ELDER';

-- 为子女用户分配子女相关权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `created_by`, `last_modified_by`)
SELECT r.id, p.id, 'system', 'system'
FROM `role` r
JOIN `permission` p ON p.permission_code IN ('USER_VIEW', 'CHILD_SERVICE', 'ORDER_VIEW', 'ORDER_ADD', 'ORDER_EDIT', 'ORDER_CANCEL', 'PRODUCT_VIEW')
WHERE r.role_code = 'CHILD';

-- 为客服人员分配客服相关权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `created_by`, `last_modified_by`)
SELECT r.id, p.id, 'system', 'system'
FROM `role` r
JOIN `permission` p ON p.permission_code IN (
    'USER_VIEW',
    'CUSTOMER_SERVICE',
    'CUSTOMER_REPLY',
    'ORDER_VIEW',
    'ORDER_EDIT',
    'ORDER_CANCEL',
    'PRODUCT_VIEW',
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

-- 为普通/老人/子女用户分配可撤回申请权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `created_by`, `last_modified_by`)
SELECT r.id, p.id, 'system', 'system'
FROM `role` r
JOIN `permission` p ON p.permission_code = 'journey:withdraw'
WHERE r.role_code IN ('USER', 'ELDER', 'CHILD');

-- 为护士和责任医生分配健康体检表权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `created_by`, `last_modified_by`)
SELECT r.id, p.id, 'system', 'system'
FROM `role` r
JOIN `permission` p ON p.permission_code IN (
    'health:check-form:create',
    'health:check-form:read',
    'health:check-form:list',
    'care-delivery:nurse-care-record:list',
    'care-delivery:nurse-care-record:read',
    'care-delivery:nurse-care-record:create',
    'care-delivery:nurse-care-record:update',
    'health:doctor-round-record:list',
    'health:doctor-round-record:read',
    'health:doctor-round-record:create',
    'health:doctor-round-record:update'
)
WHERE r.role_code IN ('NURSE', 'DOCTOR');

-- 为家属用户分配预约参观权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `created_by`, `last_modified_by`)
SELECT r.id, p.id, 'system', 'system'
FROM `role` r
JOIN `permission` p ON p.permission_code IN (
    'admission:family-visit-slot:read',
    'admission:family-visit-reservation:create',
    'admission:family-visit-reservation:my:list',
    'care-delivery:family-service-plan:list',
    'care-delivery:family-check-in:list',
    'care-delivery:family-nurse-care-record:list',
    'health:family-doctor-round-record:list',
    'elder-binding:request:create',
    'elder-binding:request:my:list',
    'elder-binding:self:bind'
)
WHERE r.role_code = 'FAMILY';

-- 为护理员分配资质申请权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `created_by`, `last_modified_by`)
SELECT r.id, p.id, 'system', 'system'
FROM `role` r
JOIN `permission` p ON p.permission_code IN (
    'quality:caregiver-qualification:create',
    'quality:caregiver-qualification:my:list',
    'care-delivery:daily-task:list',
    'care-delivery:daily-task:check-in',
    'care-delivery:check-in:my:list'
)
WHERE r.role_code = 'CAREGIVER';

-- 为医护人员分配预约与资质审核权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `created_by`, `last_modified_by`)
SELECT r.id, p.id, 'system', 'system'
FROM `role` r
JOIN `permission` p ON p.permission_code IN (
    'admission:family-visit-reservation:list',
    'admission:family-visit-reservation:detail',
    'admission:family-visit-reservation:approve',
    'admission:family-visit-reservation:reject',
    'quality:caregiver-qualification:list',
    'quality:caregiver-qualification:detail',
    'quality:caregiver-qualification:approve',
    'quality:caregiver-qualification:reject',
    'journey:task:list',
    'journey:log:list',
    'care-delivery:nurse-care-record:list',
    'care-delivery:nurse-care-record:read',
    'care-delivery:nurse-care-record:create',
    'care-delivery:nurse-care-record:update',
    'health:doctor-round-record:list',
    'health:doctor-round-record:read',
    'health:doctor-round-record:create',
    'health:doctor-round-record:update',
    'elder-binding:request:list',
    'elder-binding:request:detail',
    'elder-binding:request:approve',
    'elder-binding:request:reject'
)
WHERE r.role_code = 'MEDICAL_STAFF';

-- 插入默认管理员用户 (密码: admin123)
-- 注意: 实际生产环境中应使用更安全的密码
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `status`, `created_by`, `last_modified_by`) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '系统管理员', 1, 'system', 'system');

-- 为管理员分配超级管理员角色
INSERT INTO `user_role` (`user_id`, `role_id`, `created_by`, `last_modified_by`)
SELECT u.id, r.id, 'system', 'system'
FROM `sys_user` u JOIN `role` r ON r.role_code = 'ADMIN'
WHERE u.username = 'admin';

-- 插入测试用户数据
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`, `status`, `created_by`, `last_modified_by`) VALUES
('testuser', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '测试用户', '13800138000', 'test@example.com', 1, 'system', 'system'),
('elder001', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '张大爷', '13900139000', 'elder@example.com', 1, 'system', 'system'),
('child001', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '李儿子', '13700137000', 'child@example.com', 1, 'system', 'system');

-- 为测试用户分配角色
INSERT INTO `user_role` (`user_id`, `role_id`, `created_by`, `last_modified_by`)
SELECT u.id, r.id, 'system', 'system'
FROM `sys_user` u JOIN `role` r ON r.role_code = 'USER'
WHERE u.username IN ('testuser', 'elder001', 'child001');

-- 为老人用户分配老人角色
INSERT INTO `user_role` (`user_id`, `role_id`, `created_by`, `last_modified_by`)
SELECT u.id, r.id, 'system', 'system'
FROM `sys_user` u JOIN `role` r ON r.role_code = 'ELDER'
WHERE u.username = 'elder001';

-- 为子女用户分配子女角色
INSERT INTO `user_role` (`user_id`, `role_id`, `created_by`, `last_modified_by`)
SELECT u.id, r.id, 'system', 'system'
FROM `sys_user` u JOIN `role` r ON r.role_code = 'CHILD'
WHERE u.username = 'child001';