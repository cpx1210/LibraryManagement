# 📚 图书馆问题图书管理系统

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.5.22-4FC08D.svg)](https://vuejs.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

一个基于 Spring Boot + Vue 3 的图书馆问题图书管理系统，用于管理和检测图书馆书单中的敏感词、问题书目等。

## ✨ 功能特性

### 已完成功能 ✅

- **用户管理**
  - 用户登录/登出（JWT 认证）
  - 用户增删改查
  - 密码重置
  - 角色权限管理（管理员/普通用户）

### 计划开发功能 🚧

- **词库管理**
  - 敏感词库管理
  - 问题书目库管理
  - 出版社白名单管理
  - 批量导入/导出

- **书单检测**
  - 书单上传检测
  - 敏感词检测
  - 问题书目检测
  - 白名单校验
  - 检测结果导出（带颜色标注）

- **已购图书管理**
  - 已购问题图书登记
  - 图书状态跟踪
  - 操作台账导出

- **日志管理**
  - 操作日志记录
  - 日志查询导出

## 🛠️ 技术栈

### 后端

- **核心框架**: Spring Boot 3.5.6
- **数据库**: PostgreSQL 15.x
- **ORM 框架**: MyBatis-Plus 3.5.9
- **权限控制**: Spring Security + JWT
- **API 文档**: Knife4j (Swagger 3)
- **数据校验**: Hibernate Validator

### 前端

- **核心框架**: Vue 3.5.22
- **构建工具**: Vite 7.1
- **UI 组件库**: Element Plus 2.11
- **状态管理**: Pinia 3.0
- **路由管理**: Vue Router 4.6
- **HTTP 客户端**: Axios 1.12

## 📦 项目结构

```
LibraryManagement/
├── library-management-backend/     # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/library/management/
│   │   │   │       ├── common/           # 公共模块
│   │   │   │       ├── config/           # 配置类
│   │   │   │       ├── module/           # 业务模块
│   │   │   │       │   ├── auth/         # 认证模块
│   │   │   │       │   └── user/         # 用户管理
│   │   │   │       └── LibraryManagementApplication.java
│   │   │   └── resources/
│   │   │       ├── application.yml       # 配置文件
│   │   │       └── db/                   # 数据库脚本
│   │   └── test/
│   └── pom.xml
│
├── library-management-frontend/    # 前端项目
│   ├── src/
│   │   ├── api/                    # API 接口封装
│   │   ├── assets/                 # 静态资源
│   │   ├── components/             # 公共组件
│   │   ├── router/                 # 路由配置
│   │   ├── stores/                 # 状态管理
│   │   ├── utils/                  # 工具函数
│   │   ├── views/                  # 页面组件
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
│
├── 技术方案与架构设计.md              # 技术文档
├── 功能模块拆分与开发任务清单.md      # 开发任务
├── 项目时间规划与里程碑.md            # 项目计划
└── README.md
```

## 🚀 快速开始

### 环境要求

- **JDK**: 21
- **Node.js**: 18+
- **PostgreSQL**: 15+

### 如何确认当前 JDK 版本（Windows）

- 命令行查看 Java 版本：

```powershell
java -version
```

输出中应包含 `version "17`（或更高但兼容的 17 LTS 版本）。

- 查看 `JAVA_HOME`：

```powershell
echo %JAVA_HOME%
```

若为空，请安装 JDK 17 并配置系统环境变量 `JAVA_HOME` 指向 JDK 根目录，`Path` 中包含 `%JAVA_HOME%\bin`。

- 也可用 Maven Wrapper 验证 JDK：

```powershell
cd library-management-backend
./mvnw -v
```

输出里会显示 `Java version: 17`。

### 一键启动

1) 数据库准备（PostgreSQL）

- 创建数据库 `library_management`
- 在数据库中执行脚本：
  - `library-management-backend/src/main/resources/db/schema.sql`
  - `library-management-backend/src/main/resources/db/data.sql`
- 如果你本机数据库账号密码不同，请修改：
  `library-management-backend/src/main/resources/application-dev.yml` 中的 `spring.datasource.username/password`

2) 启动后端（无需安装 Maven）

```powershell
cd library-management-backend
build.bat   # 首次构建
run.bat     # 启动服务
```

后端访问地址：
- 接口：`http://localhost:8080/api`
- 文档：`http://localhost:8080/api/doc.html`

3) 启动前端

```powershell
cd library-management-frontend
npm install
npm run dev
```

前端访问地址：`http://localhost:5173`

4) 默认账号

- 管理员：`admin / admin123`
- 普通用户：`testuser / password123`

### 后端启动（进阶）

1. **创建数据库**

```bash
cd library-management-backend/src/main/resources/db
# 执行 data.sql 创建表和初始数据
```

2. **配置数据库连接**

编辑 `library-management-backend/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/library_management
    username: your_username
    password: your_password
```

3. **启动后端**（无需安装 Maven）

```bash
cd library-management-backend
./mvnw spring-boot:run
```

后端访问地址：
- API 接口: http://localhost:8080/api
- API 文档: http://localhost:8080/api/doc.html

### 前端启动

1. **安装依赖**

```bash
cd library-management-frontend
npm install
```

2. **启动前端**

```bash
npm run dev
```

前端访问地址: http://localhost:5173

### 默认账号

- **管理员**: admin / admin123
- **普通用户**: testuser / password123

## 📖 API 文档

启动后端后，访问 Swagger UI 查看 API 文档：

http://localhost:8080/api/doc.html

## 🎯 开发进度

- [x] 项目初始化
- [x] 认证授权模块
- [x] 用户管理模块（后端 + 前端）
- [ ] 词库管理模块
- [ ] 书单检测模块
- [ ] 已购图书管理模块
- [ ] 日志管理模块

详细进度请查看 [项目时间规划与里程碑.md](./项目时间规划与里程碑.md)

## 📝 开发说明

### 代码规范

- 后端遵循阿里巴巴 Java 开发手册
- 前端遵循 Vue 官方风格指南
- 使用中文注释
- 所有接口使用 RESTful 规范

### Git 提交规范

```
<type>: <subject>

<body>
```

类型 (type)：
- `feat`: 新功能
- `fix`: 修复 Bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具相关

## 📄 许可证

本项目采用 MIT 许可证

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📧 联系方式

如有问题，请提交 Issue 或联系项目维护者。

---

**开发工具**: 使用 [Claude Code](https://claude.com/claude-code) 辅助开发 🤖
