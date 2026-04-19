-- CODE_FILE_OPER 表增加 5 个字段
-- 1) file_list_shell 开关  2) 上传/下载前 Groovy  3) 前 Groovy 开关
-- 4) 上传/下载成功后 Groovy  5) 后 Groovy 开关

ALTER TABLE code_file_oper ADD COLUMN file_list_shell_enable INT DEFAULT 1 NOT NULL COMMENT '是否执行 FILE_LIST_SHELL：0 否 1 是（为 1 且 FILE_LIST_SHELL 非空时走 Shell 列文件）';
ALTER TABLE code_file_oper ADD COLUMN file_oper_groovy_before TEXT COMMENT 'SFTP 上传/下载前执行的 Groovy 脚本';
ALTER TABLE code_file_oper ADD COLUMN file_oper_groovy_before_enable INT DEFAULT 0 NOT NULL COMMENT '是否执行前置 Groovy：0 否 1 是';
ALTER TABLE code_file_oper ADD COLUMN file_oper_groovy_after TEXT COMMENT 'SFTP 上传/下载成功后执行的 Groovy 脚本';
ALTER TABLE code_file_oper ADD COLUMN file_oper_groovy_after_enable INT DEFAULT 0 NOT NULL COMMENT '是否执行后置 Groovy：0 否 1 是';
