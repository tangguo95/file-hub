# File Hub

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.3.1-brightgreen?logo=spring" alt="Spring Boot">
  <img src="https://img.shields.io/badge/JDK-21-orange?logo=java" alt="JDK 21">
  <img src="https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql" alt="MySQL">
  <img src="https://img.shields.io/badge/license-MIT-green" alt="License">
</p>

<p align="center">
  <img src="https://img.shields.io/github/stars/tangguo95/file-hub?style=flat-square" alt="Stars">
  <img src="https://img.shields.io/github/commit-activity/m/tangguo95/file-hub?style=flat-square" alt="Commits">
  <img src="https://img.shields.io/github/last-commit/tangguo95/file-hub?style=flat-square" alt="Last Commit">
</p>

<p align="center">
  <b>🇨🇳 中文</b> | <a href="#features">特性</a> | <a href="#quick-start">快速开始</a> | <a href="#screenshots">截图</a>
</p>

---

`File Hub` 是一个基于 `Spring Boot 3.3.1`、`JDK 21`、`Thymeleaf` 的单体开源文件任务平台，专注于文件传输任务的自动化管理。

## ✨ 特性

- 📅 **内置单机定时调度器** - 基于数据库配置驱动，无需外部调度平台
- 📤📥 **文件上传/下载任务管理** - 支持 SFTP/FTP/本地文件传输
- 🗄️ **动态数据源管理** - 支持 MySQL/Oracle 多数据源配置
- 📋 **任务执行日志** - 完整的执行历史与状态追踪
- 🎨 **Web 管理界面** - 基于 Thymeleaf 的响应式管理后台
- 🔄 **数据库版本管理** - 集成 Flyway 自动迁移

## 🚀 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+
- MySQL 8.0+

### 1. 克隆项目

```bash
git clone https://github.com/tangguo95/file-hub.git
cd file-hub
```

### 2. 配置数据库

```bash
# 创建数据库
mysql -uroot -p -e "CREATE DATABASE file_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 复制配置文件
cp src/main/resources/application-example.yml src/main/resources/application.yml

# 编辑 application.yml 填写数据库连接信息
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

### 4. 访问管理台

| 功能 | 地址 |
|------|------|
| 定时任务 | http://localhost:8080/admin/jobs |
| 文件任务 | http://localhost:8080/admin/files |
| 动态数据源 | http://localhost:8080/admin/datasources |

## 🛠️ 技术栈

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.3.1-6DB33F?logo=spring&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/JDK-21-007396?logo=java&logoColor=white" alt="JDK">
  <img src="https://img.shields.io/badge/MyBatis-3.0-000000?logo=mybatis" alt="MyBatis">
  <img src="https://img.shields.io/badge/Thymeleaf-3.1-005F0F?logo=thymeleaf" alt="Thymeleaf">
  <img src="https://img.shields.io/badge/Flyway-9.0-CC0200?logo=flyway" alt="Flyway">
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white" alt="MySQL">
</p>

## 📸 截图

<p align="center">
  <img src="screenshots/main.png" alt="管理后台" width="80%">
</p>

## 📁 项目结构

```
file-hub/
├── src/main/java/com/tydic/filehub/
│   ├── business/          # 业务逻辑层
│   ├── controller/        # 控制器层
│   ├── mapper/            # MyBatis Mapper
│   ├── scheduler/         # 定时调度器
│   ├── utils/             # 工具类
│   └── dto/               # 数据传输对象
├── src/main/resources/
│   ├── mapper/            # MyBatis XML
│   ├── db/migration/      # Flyway 迁移脚本
│   ├── static/            # 静态资源
│   └── templates/         # Thymeleaf 模板
├── docs/                  # 文档
└── README.md
```

## 📝 说明

- ✅ 已移除 Dockerfile、K8s 和 XXL-Job 依赖，简化部署
- ✅ 调度器为内置单机版，采用数据库配置驱动
- ✅ 开源主线以 MySQL 为主，动态数据源可额外配置 Oracle/MySQL
- ✅ 支持 SFTP/FTP/本地文件传输协议

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

[MIT](LICENSE) © tangguo95

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/tangguo95">tangguo95</a>
</p>
