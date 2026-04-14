create table if not exists code_file_oper (
    id bigint primary key auto_increment,
    file_oper_code varchar(64) not null unique,
    job_name varchar(128),
    file_name varchar(255),
    server_ip varchar(128),
    server_port int,
    server_user_name varchar(255),
    server_password varchar(255),
    file_path varchar(500),
    oper_type int,
    download_deal_type int,
    download_data_update_condition varchar(255),
    file_url varchar(500),
    download_tb_name varchar(255),
    split_label varchar(32),
    create_time timestamp null,
    update_time timestamp null,
    state int default 1,
    file_name_ext_sql text,
    remark text,
    query_sql text,
    insert_column_name text,
    deal_remark text,
    deal_time timestamp null,
    is_delete int default 0,
    data_source int,
    is_upload_oss int default 0,
    datasource_code varchar(64),
    file_list_shell text,
    file_list_shell_enable int default 0,
    file_oper_groovy_before text,
    file_oper_groovy_before_enable int default 0,
    file_oper_groovy_after text,
    file_oper_groovy_after_enable int default 0,
    stop_on_row_error int default 1,
    max_row_errors int,
    skip_header_lines int default 0,
    job_enabled int default 0,
    cron_expression varchar(64),
    concurrent_mode varchar(32) default 'SERIAL',
    last_trigger_time timestamp null,
    last_finish_time timestamp null,
    last_status varchar(32),
    last_message varchar(500)
);

create table if not exists config_pull_datasource (
    id bigint primary key auto_increment,
    datasource_code varchar(64) not null unique,
    data_ip varchar(1000) not null,
    username varchar(255),
    password varchar(255),
    drive_name varchar(255),
    datasource_type int default 1,
    state int default 1,
    create_time timestamp null,
    update_time timestamp null,
    remark text
);

create table if not exists log_oss_record (
    id bigint primary key auto_increment,
    file_url varchar(500),
    create_time timestamp null,
    file_name varchar(255)
);

create table if not exists job_execution_log (
    id bigint primary key auto_increment,
    file_oper_code varchar(64) not null,
    trigger_type varchar(32) not null,
    status varchar(32) not null,
    summary varchar(500),
    error_stack text,
    start_time timestamp null,
    end_time timestamp null,
    create_time timestamp null
);
