-- 创建用户模块数据库表结构

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `id_card` VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
    `id_card_verified` TINYINT(1) DEFAULT 0 COMMENT '身份证是否已验证 0:未验证 1:已验证',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `face_verified` TINYINT(1) DEFAULT 0 COMMENT '人脸是否已验证 0:未验证 1:已验证',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
    `created_by` VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '审计创建人',
    `last_modified_by` VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '审计更新人',
    `created_date_time_utc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审计创建时间',
    `last_modified_date_time_utc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '审计更新时间',
    `version` BIGINT NOT NULL DEFAULT 1 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_username` (`username`),
    UNIQUE KEY `idx_phone` (`phone`),
    UNIQUE KEY `idx_email` (`email`),
    UNIQUE KEY `idx_id_card` (`id_card`),
    KEY `idx_status` (`status`),
    KEY `idx_id_card_verified` (`id_card_verified`),
    KEY `idx_face_verified` (`face_verified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
    `created_by` VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '审计创建人',
    `last_modified_by` VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '审计更新人',
    `created_date_time_utc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审计创建时间',
    `last_modified_date_time_utc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '审计更新时间',
    `version` BIGINT NOT NULL DEFAULT 1 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_role_code` (`role_code`),
    UNIQUE KEY `idx_role_name` (`role_name`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '权限描述',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
    `created_by` VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '审计创建人',
    `last_modified_by` VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '审计更新人',
    `created_date_time_utc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审计创建时间',
    `last_modified_date_time_utc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '审计更新时间',
    `version` BIGINT NOT NULL DEFAULT 1 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_permission_code` (`permission_code`),
    UNIQUE KEY `idx_permission_name` (`permission_name`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

CREATE TABLE IF NOT EXISTS `user_role` (
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `created_by` VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '审计创建人',
    `last_modified_by` VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '审计更新人',
    `created_date_time_utc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审计创建时间',
    `last_modified_date_time_utc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '审计更新时间',
    `version` BIGINT NOT NULL DEFAULT 1 COMMENT '乐观锁版本',
    PRIMARY KEY (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`),
    CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
    CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS `role_permission` (
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `created_by` VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '审计创建人',
    `last_modified_by` VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '审计更新人',
    `created_date_time_utc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审计创建时间',
    `last_modified_date_time_utc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '审计更新时间',
    `version` BIGINT NOT NULL DEFAULT 1 COMMENT '乐观锁版本',
    PRIMARY KEY (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`),
    CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
    CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

CREATE TABLE IF NOT EXISTS `id_card_verify_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `id_card` VARCHAR(20) NOT NULL COMMENT '身份证号',
    `verify_status` TINYINT(1) DEFAULT 0 COMMENT '验证状态 0:验证中 1:验证成功 2:验证失败',
    `verify_result` VARCHAR(1000) DEFAULT NULL COMMENT '验证结果',
    `verify_time` DATETIME DEFAULT NULL COMMENT '验证时间',
    `created_by` VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '审计创建人',
    `last_modified_by` VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '审计更新人',
    `created_date_time_utc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审计创建时间',
    `last_modified_date_time_utc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '审计更新时间',
    `version` BIGINT NOT NULL DEFAULT 1 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_verify_status` (`verify_status`),
    KEY `idx_id_card` (`id_card`),
    CONSTRAINT `fk_id_card_verify_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='身份证验证记录表';

CREATE TABLE IF NOT EXISTS `face_verify_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `face_image_url` VARCHAR(500) NOT NULL COMMENT '人脸图片URL',
    `verify_status` TINYINT(1) DEFAULT 0 COMMENT '验证状态 0:验证中 1:验证成功 2:验证失败',
    `similarity` DECIMAL(5,2) DEFAULT NULL COMMENT '相似度',
    `verify_result` VARCHAR(1000) DEFAULT NULL COMMENT '验证结果',
    `verify_time` DATETIME DEFAULT NULL COMMENT '验证时间',
    `created_by` VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '审计创建人',
    `last_modified_by` VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '审计更新人',
    `created_date_time_utc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审计创建时间',
    `last_modified_date_time_utc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '审计更新时间',
    `version` BIGINT NOT NULL DEFAULT 1 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_verify_status` (`verify_status`),
    CONSTRAINT `fk_face_verify_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人脸验证记录表';

CREATE TABLE IF NOT EXISTS `user_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `operation_desc` VARCHAR(255) DEFAULT NULL COMMENT '操作描述',
    `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
    `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
    `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法',
    `request_params` TEXT DEFAULT NULL COMMENT '请求参数',
    `response_data` TEXT DEFAULT NULL COMMENT '响应数据',
    `exception_info` TEXT DEFAULT NULL COMMENT '异常信息',
    `created_by` VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '审计创建人',
    `last_modified_by` VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '审计更新人',
    `created_date_time_utc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审计创建时间',
    `last_modified_date_time_utc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '审计更新时间',
    `version` BIGINT NOT NULL DEFAULT 1 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_operation_time` (`operation_time`),
    CONSTRAINT `fk_operation_log_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户操作日志表';
