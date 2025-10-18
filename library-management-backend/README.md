# 图书馆问题图书管理系统 - 后端

## 📋 项目简介

基于 **Spring Boot 3.2.x + PostgreSQL 15.x** 的图书馆问题图书管理系统后端服务。

**主要功能**：
- 用户管理与权限控制（JWT认证）
- 敏感词库、问题书目库、出版社白名单管理
- 书单检测（敏感词检测、问题书目检测）
- 已购问题图书管理
- 操作日志记录

---

## 🛠️ 技术栈

- **框架**: Spring Boot 3.2.x
- **数据库**: PostgreSQL 15.x
- **ORM**: MyBatis-Plus 3.5.x
- **安全**: Spring Security + JWT
- **文档**: Knife4j (Swagger 3)
- **构建**: Maven 3.9.x
- **Java**: JDK 17+

---

## 🚀 快速开始

### 前置条件

- ✅ JDK 17 或更高版本
- ✅ Maven 3.6 或更高版本
- ✅ PostgreSQL 15.x（已安装并运行）
- ✅ 数据库表已创建（9张表）

### 步骤 1: 配置数据库连接

修改 `src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/library_management
    username: postgres
    password: 你的密码  # 👈 修改为你的PostgreSQL密码
```

### 步骤 2: 填充初始数据（首次运行）

双击运行：
```
init-data.bat
```

这会向数据库填充：
- 2 个测试用户（admin、testuser）
- 5 个敏感词分类
- 4 个示例敏感词
- 10 个出版社白名单

### 步骤 3: 构建项目（首次运行）

双击运行：
```
build.bat
```

或使用命令行：
```bash
mvn clean install -DskipTests
```

### 步骤 4: 启动应用

双击运行：
```
run.bat
```

或使用命令行：
```bash
mvn spring-boot:run
```

### 步骤 5: 访问应用

- **API 文档**: http://localhost:8080/api/doc.html
- **健康检查**: http://localhost:8080/api

---

## 🔑 默认账号

| 用户名 | 密码 | 角色 | 说明 |
|-------|------|-----|------|
| `admin` | `admin123` | 管理员 | 拥有所有权限 |
| `testuser` | `user123` | 普通用户 | 只读权限 |

⚠️ **生产环境请务必修改默认密码！**

---

## 📁 项目结构

```
library-management-backend/
├── src/main/java/com/library/management/
│   ├── common/                      # 公共模块
│   │   ├── constant/               # 常量定义
│   │   ├── exception/              # 异常处理
│   │   ├── result/                 # 统一返回结果
│   │   ├── utils/                  # 工具类（JWT、Excel、ISBN等）
│   │   └── annotation/             # 自定义注解
│   ├── config/                      # 配置类
│   │   └── JwtAuthenticationFilter.java
│   ├── module/                      # 业务模块
│   │   ├── auth/                   # 认证授权
│   │   ├── user/                   # 用户管理
│   │   ├── sensitiveword/          # 敏感词库
│   │   ├── problembook/            # 问题书目库
│   │   ├── publisher/              # 出版社白名单
│   │   ├── purchased/              # 已购图书
│   │   ├── detection/              # 书单检测
│   │   └── log/                    # 日志管理
│   └── LibraryManagementApplication.java
├── src/main/resources/
│   ├── db/
│   │   └── data.sql                # 初始化数据
│   ├── application.yml              # 主配置文件
│   └── application-dev.yml          # 开发环境配置
├── init-data.bat                    # 数据填充脚本
├── build.bat                        # 构建项目脚本
├── run.bat                          # 启动应用脚本
└── pom.xml
```

---

## 📦 数据库说明

### 数据库信息

- **类型**: PostgreSQL 15.x
- **数据库名**: library_management
- **端口**: 5432
- **用户名**: postgres（默认）

### 数据库表（9张）

| 表名 | 说明 |
|-----|------|
| `sys_user` | 用户信息表 |
| `sensitive_categories` | 敏感词分类表 |
| `sensitive_words` | 敏感词库表 |
| `problem_books` | 问题书目库表 |
| `publisher_whitelist` | 出版社白名单表 |
| `purchased_problem_books` | 已购问题图书库表 |
| `operation_log` | 操作日志表 |
| `booklist_check_task` | 书单检测任务表 |
| `booklist_check_detail` | 书单检测结果明细表 |

### PostgreSQL 扩展

项目使用以下扩展以提升性能：
- **pg_trgm**: 三元组模糊匹配（用于敏感词和书名的高性能模糊搜索）
- **btree_gin**: GIN 索引优化

---

## 🔧 常用命令

### Maven 命令

```bash
# 清理
mvn clean

# 编译
mvn compile

# 安装依赖
mvn install -DskipTests

# 启动应用
mvn spring-boot:run

# 运行测试
mvn test
```

### PostgreSQL 命令

```bash
# 登录数据库
psql -U postgres

# 查看所有数据库
\l

# 连接到数据库
\c library_management

# 查看所有表
\dt

# 查看表结构
\d sys_user

# 查看数据
SELECT * FROM sys_user;

# 退出
\q
```

---

## 🐛 常见问题

### Q1: 启动时报错 "无法连接数据库"

**解决方案**：
1. 检查 PostgreSQL 服务是否启动
   ```bash
   # Windows
   sc query postgresql-x64-15
   
   # Linux
   sudo systemctl status postgresql
   ```
2. 检查 `application-dev.yml` 中的连接信息
3. 确认数据库 `library_management` 已创建

### Q2: 依赖下载慢

**解决方案**：
配置 Maven 阿里云镜像，修改 `settings.xml`：
```xml
<mirror>
  <id>aliyun</id>
  <mirrorOf>central</mirrorOf>
  <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

### Q3: 端口 8080 被占用

**解决方案**：
修改 `application.yml` 中的端口：
```yaml
server:
  port: 8081  # 改为其他端口
```

---

## 📚 项目文档

| 文档 | 说明 |
|-----|------|
| [技术方案与架构设计.md](../技术方案与架构设计.md) | 系统架构和技术选型说明 |
| [功能模块拆分与开发任务清单.md](../功能模块拆分与开发任务清单.md) | 详细的开发任务清单 |
| [项目时间规划与里程碑.md](../项目时间规划与里程碑.md) | 项目进度和时间规划 |

---

## 🧪 开发建议

### 编码规范

- 遵循阿里巴巴 Java 开发手册规范
- RESTful API 设计规范
- 统一异常处理和返回格式
- 关键业务逻辑添加注释
- 所有增删改操作记录日志

### Git 提交规范

```bash
feat: 添加新功能
fix: 修复 bug
docs: 文档更新
style: 代码格式调整
refactor: 重构代码
test: 添加测试
chore: 构建/工具变动
```

---

## 📝 快速命令速查

### 日常开发流程

```bash
# 1. 首次运行：构建项目
build.bat

# 2. 首次运行：填充数据
init-data.bat

# 3. 日常开发：启动应用
run.bat

# 4. 访问 API 文档
浏览器打开：http://localhost:8080/api/doc.html
```

### 数据重置

如果需要重置数据，可以重新运行：
```bash
init-data.bat
```

注意：这会覆盖已有的初始数据（用户账号、敏感词分类等）。

---

## 📄 许可证

本项目仅供学习使用。

---

## 👥 团队协作

新成员加入项目：
1. 确保 PostgreSQL 已安装并创建好数据库表
2. 克隆项目仓库
3. 修改 `application-dev.yml` 中的数据库密码
4. 运行 `init-data.bat` 填充数据
5. 运行 `build.bat` 构建项目
6. 运行 `run.bat` 启动应用
7. 访问 http://localhost:8080/api/doc.html 查看 API

---

**开发愉快！** 🎉

如有问题，请查阅项目文档或联系项目负责人。
