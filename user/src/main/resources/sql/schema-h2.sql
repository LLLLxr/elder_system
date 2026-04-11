-- H2 Database Schema Initialization

-- Create sys_user table
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar VARCHAR(255),
    id_card VARCHAR(20),
    id_card_verified INTEGER DEFAULT 0,
    face_verified INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    last_login_time TIMESTAMP,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    delete_flag INTEGER DEFAULT 0
);

-- Create role table
CREATE TABLE role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    status INTEGER DEFAULT 1,
    delete_flag INTEGER DEFAULT 0,
    create_time TIMESTAMP,
    update_time TIMESTAMP
);

-- Create permission table
CREATE TABLE permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_name VARCHAR(50) NOT NULL,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    status INTEGER DEFAULT 1,
    delete_flag INTEGER DEFAULT 0,
    create_time TIMESTAMP,
    update_time TIMESTAMP
);

-- Create user_role table (many-to-many relationship)
CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (role_id) REFERENCES role(id)
);

-- Create role_permission table (many-to-many relationship)
CREATE TABLE role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES role(id),
    FOREIGN KEY (permission_id) REFERENCES permission(id)
);

-- Create sequences
CREATE SEQUENCE IF NOT EXISTS sys_user_sequence START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS role_sequence START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS permission_sequence START WITH 1 INCREMENT BY 1;