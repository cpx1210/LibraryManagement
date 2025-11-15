# MySQL 迁移快速指南 ⚡

> 图书馆问题书目管理系统 - 从 PostgreSQL 迁移到 MySQL

---

## 📦 已创建的文件

### 数据库脚本（位于 `library-management-backend/src/main/resources/db/`）

| 文件名 | 说明 |
|--------|------|
| ✅ `mysql_schema.sql` | MySQL 建表脚本（9张表） |
| ✅ `mysql_data_cn.sql` | 中文初始数据（用户、分类、白名单） |
| ✅ `mysql_create_database.bat` | Windows 一键初始化脚本 |

### 配置文件（位于 `library-management-backend/src/main/resources/`）

| 文件名 | 说明 |
|--------|------|
| ✅ `application-mysql.yml` | MySQL 专用配置文件 |

### 文档（位于 `library-management-backend/src/main/resources/db/`）

| 文件名 | 说明 |
|--------|------|
| ✅ `MYSQL_README.md` | MySQL 脚本总览和使用说明 |
| ✅ `MYSQL_MIGRATION_GUIDE.md` | 详细迁移指南（30+ 页） |
| ✅ `MYSQL_CODE_CHANGES.md` | 代码修改清单（5处修改） |

---

## 🚀 三步完成迁移

### 步骤 1️⃣：初始化 MySQL 数据库

```bash
# 进入 db 目录
cd library-management-backend\src\main\resources\db

# 编辑 mysql_create_database.bat，设置数据库密码
# 然后执行
mysql_create_database.bat
```

**或手动执行**：
```bash
mysql -u root -p --default-character-set=utf8mb4 < mysql_schema.sql
mysql -u root -p --default-character-set=utf8mb4 < mysql_data_cn.sql
```

---

### 步骤 2️⃣：修改项目配置

#### A. 修改 `pom.xml`

**位置**: `library-management-backend/pom.xml`

```xml
<!-- 注释掉 PostgreSQL 驱动（第 107-111 行） -->
<!--
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
-->

<!-- 添加 MySQL 驱动 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

#### B. 修改 `application.yml`

**位置**: `library-management-backend/src/main/resources/application.yml`

**方案1（简单）**：修改第 6 行
```yaml
spring:
  profiles:
    active: mysql  # 改为 mysql
```
然后编辑 `application-mysql.yml` 填入数据库密码

**方案2（直接）**：直接修改 `application.yml` 的数据源配置
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/library_management?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

  jpa:
    database-platform: org.hibernate.dialect.MySQL8Dialect
```

---

### 步骤 3️⃣：修改 Mapper 文件（3个文件，5处修改）

#### 文件 1: `SensitiveWordMapper.xml`

**位置**: `src/main/resources/mapper/SensitiveWordMapper.xml`

```xml
<!-- 第 19 行：TRUE → 1 -->
WHERE is_active = 1

<!-- 第 25 行：ILIKE → LIKE -->
(match_type = 1 AND #{text} LIKE CONCAT('%', keyword, '%'))
```

#### 文件 2: `BooklistCheckTaskMapper.xml`

**位置**: `src/main/resources/mapper/BooklistCheckTaskMapper.xml`

```xml
<!-- 第 30 行：ILIKE → LIKE -->
AND task_name LIKE CONCAT('%', #{taskName}, '%')

<!-- 第 47 行：CURRENT_DATE → CURDATE() -->
AND DATE(submit_time) = CURDATE()
```

#### 文件 3: `ProblemBookMapper.xml`

**位置**: `src/main/resources/mapper/ProblemBookMapper.xml`

**完整替换第 20-33 行**：

```xml
WHERE
    -- ISBN 精确匹配（优先级最高）
    isbn = #{isbn}
    OR
    -- 书名模糊匹配
    (#{bookName} IS NOT NULL AND book_name LIKE CONCAT('%', #{bookName}, '%'))
ORDER BY
    -- 优先返回 ISBN 匹配的结果
    CASE WHEN isbn = #{isbn} THEN 0 ELSE 1 END,
    -- 然后按书名匹配度排序
    CASE 
        WHEN book_name = #{bookName} THEN 0
        WHEN book_name LIKE CONCAT(#{bookName}, '%') THEN 1
        WHEN book_name LIKE CONCAT('%', #{bookName}, '%') THEN 2
        ELSE 3
    END,
    LENGTH(book_name)
LIMIT 1
```

---

## ✅ 验证和启动

```bash
# 1. 重新编译
cd library-management-backend
mvn clean compile

# 2. 启动应用
mvn spring-boot:run

# 或使用批处理脚本
build.bat
run.bat
```

**测试登录**：
- 用户名: `admin`
- 密码: `admin123`

---

## 📊 修改总结

| 项目 | 修改内容 | 数量 |
|------|----------|------|
| 依赖 | PostgreSQL → MySQL | 1 处 |
| 配置 | 数据源和 JPA 方言 | 1 处 |
| Mapper | SQL 语法兼容性 | 3 文件 5 处 |
| **总计** | | **5 处** |

---

## 🔄 PostgreSQL vs MySQL 语法对照表

| PostgreSQL | MySQL | 用途 |
|-----------|-------|------|
| `BIGSERIAL` | `BIGINT AUTO_INCREMENT` | 自增主键 |
| `BOOLEAN` (TRUE/FALSE) | `TINYINT(1)` (1/0) | 布尔值 |
| `TIMESTAMP` | `DATETIME` | 时间戳 |
| `ILIKE` | `LIKE` | 模糊搜索 |
| `similarity()` | `LIKE` + `CASE` | 相似度匹配 |
| `CURRENT_DATE` | `CURDATE()` | 当前日期 |
| `GIN` + `gin_trgm_ops` | `FULLTEXT` + `ngram` | 全文索引 |

---

## 📚 详细文档

如需更多信息，请查看：

1. **`MYSQL_README.md`** - MySQL 脚本总览
2. **`MYSQL_MIGRATION_GUIDE.md`** - 完整迁移指南（包含故障排除）
3. **`MYSQL_CODE_CHANGES.md`** - 详细的代码修改说明

---

## ⚠️ 重要提示

1. **备份数据**：迁移前请备份 PostgreSQL 数据
2. **修改密码**：生产环境请立即修改默认密码
3. **测试功能**：迁移后请全面测试各项功能
4. **性能调优**：根据实际情况调整 MySQL 配置

---

## 🐛 常见问题

### 问题 1: 连接失败

```bash
# 检查 MySQL 服务
net start mysql

# 测试连接
mysql -u root -p -e "SELECT 1;"
```

### 问题 2: 中文乱码

确保连接字符串包含：
```
?characterEncoding=utf-8
```

### 问题 3: 搜索不准确

已在 `mysql_schema.sql` 中创建了全文索引，使用 `ngram` 解析器支持中文。

---

## ⏱️ 预计时间

- **数据库初始化**: 2 分钟
- **代码修改**: 15 分钟
- **测试验证**: 10 分钟
- **总计**: 约 30 分钟

---

## 🎯 迁移检查清单

- [ ] 执行 `mysql_create_database.bat` 或手动执行 SQL 脚本
- [ ] 修改 `pom.xml` 添加 MySQL 驱动
- [ ] 修改 `application.yml` 或使用 `application-mysql.yml`
- [ ] 修改 `SensitiveWordMapper.xml`（2处）
- [ ] 修改 `BooklistCheckTaskMapper.xml`（2处）
- [ ] 修改 `ProblemBookMapper.xml`（1处）
- [ ] 执行 `mvn clean compile`
- [ ] 启动应用测试
- [ ] 测试登录功能
- [ ] 测试敏感词管理
- [ ] 测试书目检测
- [ ] 测试搜索功能

---

**完成以上步骤后，你的系统就可以在 MySQL 上运行了！** 🎉

如有问题，请参考详细文档或检查日志输出。

