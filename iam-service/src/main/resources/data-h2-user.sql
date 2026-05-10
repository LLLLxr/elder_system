-- H2 Database Test Data Initialization

-- Insert test roles
INSERT INTO role (id, role_name, role_code, description, status, created_date_time_utc, last_modified_date_time_utc, version) VALUES
(1, '超级管理员', 'ADMIN', '系统超级管理员，拥有所有权限', 1, NOW(), NOW(), 1),
(2, '普通用户', 'USER', '普通注册用户，拥有基本权限', 1, NOW(), NOW(), 1),
(3, '老人用户', 'ELDER', '已验证的老人用户，拥有老人相关权限', 1, NOW(), NOW(), 1),
(4, '子女用户', 'CHILD', '已验证的老人子女用户，拥有子女相关权限', 1, NOW(), NOW(), 1),
(5, '客服人员', 'CUSTOMER_SERVICE', '客服人员，拥有处理客户服务的权限', 1, NOW(), NOW(), 1),
(6, '护士', 'NURSE', '护士，拥有健康体检表录入与查看权限', 1, NOW(), NOW(), 1),
(7, '责任医生', 'DOCTOR', '责任医生，拥有健康体检表录入与查看权限', 1, NOW(), NOW(), 1),
(8, '家属用户', 'FAMILY', '家属端预约参观与查看我的预约权限', 1, NOW(), NOW(), 1),
(9, '护理员', 'CAREGIVER', '护理员资质申请与查看状态权限', 1, NOW(), NOW(), 1),
(10, '医护人员', 'MEDICAL_STAFF', '医护审核家属预约与护理员资质权限', 1, NOW(), NOW(), 1);

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
(27, '旅程申请撤回', 'journey:withdraw', '服务旅程申请撤回权限', 1, 'system', 'system', NOW(), NOW(), 1),
(28, '旅程待办列表', 'journey:task:list', '查看服务旅程待办列表权限', 1, 'system', 'system', NOW(), NOW(), 1),
(29, '旅程日志列表', 'journey:log:list', '查看服务旅程日志列表权限', 1, 'system', 'system', NOW(), NOW(), 1),
(30, '家属预约时段查看', 'admission:family-visit-slot:read', '查看家属预约参观时段权限', 1, 'system', 'system', NOW(), NOW(), 1),
(31, '家属预约提交', 'admission:family-visit-reservation:create', '提交家属预约参观权限', 1, 'system', 'system', NOW(), NOW(), 1),
(32, '家属预约我的列表', 'admission:family-visit-reservation:my:list', '查看我的家属预约参观记录权限', 1, 'system', 'system', NOW(), NOW(), 1),
(33, '家属预约审核列表', 'admission:family-visit-reservation:list', '查看家属预约参观审核列表权限', 1, 'system', 'system', NOW(), NOW(), 1),
(34, '家属预约详情', 'admission:family-visit-reservation:detail', '查看家属预约参观详情权限', 1, 'system', 'system', NOW(), NOW(), 1),
(35, '家属预约审核通过', 'admission:family-visit-reservation:approve', '审核通过家属预约参观权限', 1, 'system', 'system', NOW(), NOW(), 1),
(36, '家属预约审核驳回', 'admission:family-visit-reservation:reject', '审核驳回家属预约参观权限', 1, 'system', 'system', NOW(), NOW(), 1),
(37, '护理员资质申请提交', 'quality:caregiver-qualification:create', '提交护理员资质申请权限', 1, 'system', 'system', NOW(), NOW(), 1),
(38, '护理员资质我的列表', 'quality:caregiver-qualification:my:list', '查看我的护理员资质申请记录权限', 1, 'system', 'system', NOW(), NOW(), 1),
(39, '护理员资质审核列表', 'quality:caregiver-qualification:list', '查看护理员资质审核列表权限', 1, 'system', 'system', NOW(), NOW(), 1),
(40, '护理员资质详情', 'quality:caregiver-qualification:detail', '查看护理员资质申请详情权限', 1, 'system', 'system', NOW(), NOW(), 1),
(41, '护理员资质审核通过', 'quality:caregiver-qualification:approve', '审核通过护理员资质申请权限', 1, 'system', 'system', NOW(), NOW(), 1),
(42, '护理员资质审核驳回', 'quality:caregiver-qualification:reject', '审核驳回护理员资质申请权限', 1, 'system', 'system', NOW(), NOW(), 1),
(43, '护理员我的任务列表', 'care-delivery:daily-task:list', '查看护理员每日任务列表权限', 1, 'system', 'system', NOW(), NOW(), 1),
(44, '护理员打卡提交', 'care-delivery:daily-task:check-in', '提交护理员打卡权限', 1, 'system', 'system', NOW(), NOW(), 1),
(45, '护理员打卡我的列表', 'care-delivery:check-in:my:list', '查看护理员我的打卡记录权限', 1, 'system', 'system', NOW(), NOW(), 1),
(46, '护士护理记录列表', 'care-delivery:nurse-care-record:list', '查看护士护理记录列表权限', 1, 'system', 'system', NOW(), NOW(), 1),
(47, '护士护理记录详情', 'care-delivery:nurse-care-record:read', '查看护士护理记录详情权限', 1, 'system', 'system', NOW(), NOW(), 1),
(48, '护士护理记录创建', 'care-delivery:nurse-care-record:create', '创建护士护理记录权限', 1, 'system', 'system', NOW(), NOW(), 1),
(49, '护士护理记录更新', 'care-delivery:nurse-care-record:update', '更新护士护理记录权限', 1, 'system', 'system', NOW(), NOW(), 1),
(50, '家属服务清单列表', 'care-delivery:family-service-plan:list', '查看家属服务清单权限', 1, 'system', 'system', NOW(), NOW(), 1),
(51, '家属打卡记录列表', 'care-delivery:family-check-in:list', '查看家属打卡记录权限', 1, 'system', 'system', NOW(), NOW(), 1),
(52, '家属护理记录列表', 'care-delivery:family-nurse-care-record:list', '查看家属护理记录权限', 1, 'system', 'system', NOW(), NOW(), 1),
(53, '医生查房记录列表', 'health:doctor-round-record:list', '查看医生查房记录列表权限', 1, 'system', 'system', NOW(), NOW(), 1),
(54, '医生查房记录详情', 'health:doctor-round-record:read', '查看医生查房记录详情权限', 1, 'system', 'system', NOW(), NOW(), 1),
(55, '医生查房记录创建', 'health:doctor-round-record:create', '创建医生查房记录权限', 1, 'system', 'system', NOW(), NOW(), 1),
(56, '医生查房记录更新', 'health:doctor-round-record:update', '更新医生查房记录权限', 1, 'system', 'system', NOW(), NOW(), 1),
(57, '家属查房记录列表', 'health:family-doctor-round-record:list', '查看家属查房记录权限', 1, 'system', 'system', NOW(), NOW(), 1),
(58, '老人绑定申请提交', 'elder-binding:request:create', '提交老人绑定申请权限', 1, 'system', 'system', NOW(), NOW(), 1),
(59, '老人绑定申请我的列表', 'elder-binding:request:my:list', '查看我的老人绑定申请权限', 1, 'system', 'system', NOW(), NOW(), 1),
(60, '老人绑定申请列表', 'elder-binding:request:list', '查看老人绑定申请列表权限', 1, 'system', 'system', NOW(), NOW(), 1),
(61, '老人绑定申请详情', 'elder-binding:request:detail', '查看老人绑定申请详情权限', 1, 'system', 'system', NOW(), NOW(), 1),
(62, '老人绑定申请审核通过', 'elder-binding:request:approve', '审核通过老人绑定申请权限', 1, 'system', 'system', NOW(), NOW(), 1),
(63, '老人绑定申请审核驳回', 'elder-binding:request:reject', '审核驳回老人绑定申请权限', 1, 'system', 'system', NOW(), NOW(), 1),
(64, '老人绑定列表', 'elder-binding:list', '查看老人绑定列表权限', 1, 'system', 'system', NOW(), NOW(), 1),
(65, '老人本人绑定', 'elder-binding:self:bind', '老人本人绑定自己的老人档案权限', 1, 'system', 'system', NOW(), NOW(), 1);

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

-- Assign health permissions to NURSE and DOCTOR roles
INSERT INTO role_permission (role_id, permission_id, created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version)
SELECT r.id, p.id, 'system', 'system', NOW(), NOW(), 1
FROM role r
JOIN permission p ON p.permission_code IN (
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

-- Assign family visit permissions to FAMILY role
INSERT INTO role_permission (role_id, permission_id, created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version)
SELECT r.id, p.id, 'system', 'system', NOW(), NOW(), 1
FROM role r
JOIN permission p ON p.permission_code IN (
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

-- Assign qualification permissions to CAREGIVER role
INSERT INTO role_permission (role_id, permission_id, created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version)
SELECT r.id, p.id, 'system', 'system', NOW(), NOW(), 1
FROM role r
JOIN permission p ON p.permission_code IN (
    'quality:caregiver-qualification:create',
    'quality:caregiver-qualification:my:list',
    'care-delivery:daily-task:list',
    'care-delivery:daily-task:check-in',
    'care-delivery:check-in:my:list'
)
WHERE r.role_code = 'CAREGIVER';

-- Assign review permissions to MEDICAL_STAFF role
INSERT INTO role_permission (role_id, permission_id, created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version)
SELECT r.id, p.id, 'system', 'system', NOW(), NOW(), 1
FROM role r
JOIN permission p ON p.permission_code IN (
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

-- Insert test users
INSERT INTO sys_user (id, username, password, real_name, phone, email, status, id_card_verified, face_verified, delete_flag, created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version) VALUES
(1, 'admin', '$2a$10$TMW5dWBpRbGmEuQOb24m2OnvNZSA8D2AtVW4kKd7x.EUV9UTPtZbW', '系统管理员', '13800138000', 'admin@example.com', 1, 1, 1, 0, 'system', 'system', NOW(), NOW(), 1),
(2, 'user', '$2a$10$TMW5dWBpRbGmEuQOb24m2OnvNZSA8D2AtVW4kKd7x.EUV9UTPtZbW', '测试用户', '13800138001', 'user@example.com', 1, 0, 0, 0, 'system', 'system', NOW(), NOW(), 1),
(3, 'elder', '$2a$10$TMW5dWBpRbGmEuQOb24m2OnvNZSA8D2AtVW4kKd7x.EUV9UTPtZbW', '老人用户', '13800138002', 'elder@example.com', 1, 1, 0, 0, 'system', 'system', NOW(), NOW(), 1),
(4, 'family1', '$2a$10$TMW5dWBpRbGmEuQOb24m2OnvNZSA8D2AtVW4kKd7x.EUV9UTPtZbW', '家属用户', '13800138004', 'family1@example.com', 1, 1, 0, 0, 'system', 'system', NOW(), NOW(), 1),
(5, 'caregiver1', '$2a$10$TMW5dWBpRbGmEuQOb24m2OnvNZSA8D2AtVW4kKd7x.EUV9UTPtZbW', '护理员甲', '13800138005', 'caregiver1@example.com', 1, 1, 0, 0, 'system', 'system', NOW(), NOW(), 1),
(6, 'medic1', '$2a$10$TMW5dWBpRbGmEuQOb24m2OnvNZSA8D2AtVW4kKd7x.EUV9UTPtZbW', '医护人员甲', '13800138006', 'medic1@example.com', 1, 1, 0, 0, 'system', 'system', NOW(), NOW(), 1);

-- Assign roles to test users
INSERT INTO user_role (user_id, role_id, created_by, last_modified_by, created_date_time_utc, last_modified_date_time_utc, version) VALUES
(1, 1, 'system', 'system', NOW(), NOW(), 1),  -- admin -> ADMIN
(2, 2, 'system', 'system', NOW(), NOW(), 1),  -- user -> USER
(3, 3, 'system', 'system', NOW(), NOW(), 1),  -- elder -> ELDER
(4, 8, 'system', 'system', NOW(), NOW(), 1),  -- family1 -> FAMILY
(5, 9, 'system', 'system', NOW(), NOW(), 1),  -- caregiver1 -> CAREGIVER
(6, 10, 'system', 'system', NOW(), NOW(), 1); -- medic1 -> MEDICAL_STAFF