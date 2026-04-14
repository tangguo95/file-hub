-- 创建本地文件下载测试任务
INSERT INTO code_file_oper (
    file_oper_code, job_name, file_name, server_ip, server_port,
    server_user_name, server_password, file_path, oper_type,
    download_deal_type, split_label, file_format, state,
    is_upload_oss, is_delete, job_enabled, cron_expression,
    concurrent_mode, remark, create_time, update_time
) VALUES (
    'TEST_LOCAL_DOWNLOAD', '本地文件下载测试', 'test_data.csv', 'localhost', 22,
    'testuser', 'testpass', '/uploads', 1,
    1, ',', 'CSV', 1,
    0, 0, 0, '0 */5 * * * ?',
    'SERIAL', '测试本地文件下载功能', NOW(), NOW()
);

-- 创建本地文件上传测试任务
INSERT INTO code_file_oper (
    file_oper_code, job_name, file_name, server_ip, server_port,
    server_user_name, server_password, file_path, oper_type,
    download_deal_type, split_label, file_format, state,
    is_upload_oss, is_delete, job_enabled, cron_expression,
    concurrent_mode, remark, create_time, update_time
) VALUES (
    'TEST_LOCAL_UPLOAD', '本地文件上传测试', 'output.csv', 'localhost', 22,
    'testuser', 'testpass', '/downloads', 2,
    1, ',', 'CSV', 1,
    0, 0, 0, '0 */5 * * * ?',
    'SERIAL', '测试本地文件上传功能', NOW(), NOW()
);
