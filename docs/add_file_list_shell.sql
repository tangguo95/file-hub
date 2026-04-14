-- CODE_FILE_OPER 表添加 FILE_LIST_SHELL 字段
-- 用于在 SFTP 服务器上执行 Shell 获取文件列表，实现批量下载（如排除 .MD5 等）
-- 配置后优先于 file_name_ext_sql，输出每行一个文件名

-- Oracle / OceanBase Oracle 模式
-- ALTER TABLE CODE_FILE_OPER ADD FILE_LIST_SHELL CLOB;
-- COMMENT ON COLUMN CODE_FILE_OPER.FILE_LIST_SHELL IS '文件列表Shell：在SFTP服务器执行，每行一个文件名';

-- MySQL / OceanBase MySQL 模式
ALTER TABLE code_file_oper ADD COLUMN file_list_shell TEXT DEFAULT NULL COMMENT '文件列表Shell：在SFTP服务器执行，每行一个文件名。如 ls -1 DWD_D_EVT_KF_GD_CASE_MAIN* 2>/dev/null | grep -v ''\\.MD5''';
