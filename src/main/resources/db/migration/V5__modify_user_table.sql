-- 修改用户表：删除姓名、手机号、邮箱、工号字段，添加用户昵称
ALTER TABLE sys_user
DROP COLUMN real_name,
DROP COLUMN email,
DROP COLUMN phone,
DROP COLUMN employee_id,
ADD COLUMN nickname VARCHAR(100) COMMENT '用户昵称' AFTER username;

-- 将现有表的employee_id字段改为created_by（用户名）
ALTER TABLE code_file_oper CHANGE COLUMN employee_id created_by VARCHAR(50) COMMENT '创建用户';
ALTER TABLE config_pull_datasource CHANGE COLUMN employee_id created_by VARCHAR(50) COMMENT '创建用户';
ALTER TABLE job_execution_log CHANGE COLUMN employee_id created_by VARCHAR(50) COMMENT '创建用户';
ALTER TABLE log_oss_record CHANGE COLUMN employee_id created_by VARCHAR(50) COMMENT '创建用户';