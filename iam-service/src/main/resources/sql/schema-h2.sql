-- H2 Database Schema Initialization

CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    id_card VARCHAR(20),
    id_card_verified INTEGER DEFAULT 0,
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar VARCHAR(255),
    face_verified INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    last_login_time TIMESTAMP,
    delete_flag INTEGER DEFAULT 0,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date_time_utc TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 1
);

CREATE TABLE role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    status INTEGER DEFAULT 1,
    delete_flag INTEGER DEFAULT 0,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date_time_utc TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 1
);

CREATE TABLE permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_name VARCHAR(50) NOT NULL,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    status INTEGER DEFAULT 1,
    delete_flag INTEGER DEFAULT 0,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date_time_utc TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 1
);

CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date_time_utc TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 1,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (role_id) REFERENCES role(id)
);

CREATE TABLE role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date_time_utc TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 1,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES role(id),
    FOREIGN KEY (permission_id) REFERENCES permission(id)
);

CREATE TABLE id_card_verify_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    id_card VARCHAR(20) NOT NULL,
    verify_status INTEGER DEFAULT 0,
    verify_result VARCHAR(1000),
    verify_time TIMESTAMP,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date_time_utc TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

CREATE TABLE face_verify_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    face_image_url VARCHAR(500) NOT NULL,
    verify_status INTEGER DEFAULT 0,
    similarity DECIMAL(5,2),
    verify_result VARCHAR(1000),
    verify_time TIMESTAMP,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date_time_utc TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

CREATE TABLE user_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    operation_type VARCHAR(50) NOT NULL,
    operation_desc VARCHAR(255),
    operation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    request_url VARCHAR(500),
    request_method VARCHAR(10),
    request_params TEXT,
    response_data TEXT,
    exception_info TEXT,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    last_modified_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_date_time_utc TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date_time_utc TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

CREATE SEQUENCE IF NOT EXISTS sys_user_sequence START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS role_sequence START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS permission_sequence START WITH 1 INCREMENT BY 1;
