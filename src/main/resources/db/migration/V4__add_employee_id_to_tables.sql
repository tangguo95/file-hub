-- 为现有表添加工号字段

ALTER TABLE code_file_oper
ADD COLUMN employee_id VARCHAR(50) COMMENT '工号' AFTER id,
ADD INDEX idx_employee_id (employee_id);

ALTER TABLE config_pull_datasource
ADD COLUMN employee_id VARCHAR(50) COMMENT '工号' AFTER id,
ADD INDEX idx_employee_id (employee_id);

ALTER TABLE job_execution_log
ADD COLUMN employee_id VARCHAR(50) COMMENT '工号' AFTER id,
ADD INDEX idx_employee_id (employee_id);

ALTER TABLE log_oss_record
ADD COLUMN employee_id VARCHAR(50) COMMENT '工号' AFTER id,
ADD INDEX idx_employee_id (employee_id);