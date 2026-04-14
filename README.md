# File Hub

`File Hub` 是一个基于 `Spring Boot 3.3.1`、`JDK 21`、`Thymeleaf` 的单体开源文件任务平台，提供：

- 内置单机定时调度器
- 文件上传/下载任务管理
- 动态数据源管理
- 任务执行日志

## 技术栈

- Spring Boot 3.3.1
- JDK 21
- MyBatis
- Thymeleaf
- Flyway
- MySQL 8

## 启动

1. 准备 MySQL 数据库 `file_hub`
2. 复制 `src/main/resources/application-example.yml` 到 `src/main/resources/application.yml` 并填写连接信息
3. 执行：

```bash
mvn spring-boot:run
```

管理台默认入口：

- `http://localhost:6007/admin/jobs`
- `http://localhost:6007/admin/files`
- `http://localhost:6007/admin/datasources`

## 说明

- 已移除 Dockerfile、K8s 和 XXL-Job 依赖
- 调度器为内置单机版，采用数据库配置驱动
- 开源主线以 MySQL 为主，动态数据源可额外配置 Oracle/MySQL
