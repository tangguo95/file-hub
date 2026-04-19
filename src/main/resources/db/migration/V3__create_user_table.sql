-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    employee_id VARCHAR(50) NOT NULL UNIQUE COMMENT '工号',
    real_name VARCHAR(100) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status INT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    create_time TIMESTAMP NULL COMMENT '创建时间',
    update_time TIMESTAMP NULL COMMENT '更新时间',
    last_login_time TIMESTAMP NULL COMMENT '最后登录时间',
    INDEX idx_employee_id (employee_id),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';