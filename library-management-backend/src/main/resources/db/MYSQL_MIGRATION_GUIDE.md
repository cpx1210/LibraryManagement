# MySQL 迁移指南

本指南帮助你将图书馆问题书目管理系统从 PostgreSQL 迁移到 MySQL。

## 📋 目录

1. [为什么需要迁移](#为什么需要迁移)
2. [快速开始](#快速开始)
3. [详细步骤](#详细步骤)
4. [代码修改说明](#代码修改说明)
5. [数据迁移](#数据迁移)
6. [常见问题](#常见问题)

---

## 为什么需要迁移

如果你的服务器环境满足以下任一条件，建议迁移到 MySQL：

- ✅ 服务器只开放了 MySQL 端口（3306），未开放 PostgreSQL 端口（5432）
- ✅ 生产环境已有 MySQL 基础设施，不想额外维护 PostgreSQL
- ✅ 团队更熟悉 MySQL 的运维和优化

---

## 快速开始

### 前置要求

- MySQL 8.0 或更高版本
- Java 21
- Maven 3.6+

### 三步完成迁移

```bash
# 1. 初始化 MySQL 数据库
cd library-management-backend\src\main\resources\db
mysql_create_database.bat

# 2. 修改 pom.xml（添加 MySQL 驱动）
# 见下文详细步骤

# 3. 修改配置文件，启动应用
# 见下文详细步骤
```

---

## 详细步骤

### 步骤 1：安装和配置 MySQL

#### 1.1 安装 MySQL 8.0+

- **Windows**: 下载 [MySQL Installer](https://dev.mysql.com/downloads/installer/)
- **Linux**: 
  ```bash
  sudo apt update
  sudo apt install mysql-server-8.0
  ```

#### 1.2 创建数据库用户（可选）

```sql
-- 以 root 身份登录 MySQL
mysql -u root -p

-- 创建专用数据库用户
CREATE USER 'library_user'@'localhost' IDENTIFIED BY 'secure_password';
CREATE USER 'library_user'@'%' IDENTIFIED BY 'secure_password';

-- 授予权限
GRANT ALL PRIVILEGES ON library_management.* TO 'library_user'@'localhost';
GRANT ALL PRIVILEGES ON library_management.* TO 'library_user'@'%';

FLUSH PRIVILEGES;
```

### 步骤 2：初始化数据库

#### 2.1 修改批处理脚本配置

编辑 `src/main/resources/db/mysql_create_database.bat`：

```batch
set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_PASSWORD=your_password
```

#### 2.2 执行初始化脚本

```bash
cd src\main\resources\db
mysql_create_database.bat
```

脚本会自动：
- ✅ 创建 `library_management` 数据库
- ✅ 创建所有 9 张表
- ✅ 插入初始数据（管理员账号、测试用户、敏感词分类、出版社白名单）

#### 2.3 手动执行（可选）

如果批处理脚本失败，可以手动执行：

```bash
# 创建数据库和表
mysql -u root -p --default-character-set=utf8mb4 < mysql_schema.sql

# 插入初始数据
mysql -u root -p --default-character-set=utf8mb4 < mysql_data_cn.sql
```

### 步骤 3：修改项目依赖

编辑 `pom.xml`，替换数据库驱动：

```xml
<!-- 注释掉或删除 PostgreSQL 驱动 -->
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

### 步骤 4：修改应用配置

#### 方案 A：修改现有配置文件

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/library_management?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: your_password

  jpa:
    database-platform: org.hibernate.dialect.MySQL8Dialect
    show-sql: true
    hibernate:
      ddl-auto: none
```

#### 方案 B：使用独立的 MySQL 配置（推荐）

1. 使用已创建的 `application-mysql.yml`
2. 修改 `application.yml` 中的 active profile：

```yaml
spring:
  profiles:
    active: mysql  # 改为 mysql
```

3. 编辑 `application-mysql.yml` 填入你的数据库密码

### 步骤 5：修改 Mapper XML 文件

需要修改 3 个 Mapper 文件中的 PostgreSQL 特有语法。

#### 5.1 SensitiveWordMapper.xml

**位置**: `src/main/resources/mapper/SensitiveWordMapper.xml`

**修改内容**:

```xml
<!-- 原 PostgreSQL 语法 -->
<if test="text != null and text != ''">
    (match_type = 1 AND #{text} ILIKE CONCAT('%', keyword, '%'))
</if>

<!-- 改为 MySQL 语法 -->
<if test="text != null and text != ''">
    (match_type = 1 AND #{text} LIKE CONCAT('%', keyword, '%'))
</if>
```

#### 5.2 BooklistCheckTaskMapper.xml

**位置**: `src/main/resources/mapper/BooklistCheckTaskMapper.xml`

**修改内容**:

```xml
<!-- 原 PostgreSQL 语法 -->
<if test="taskName != null and taskName != ''">
    AND task_name ILIKE CONCAT('%', #{taskName}, '%')
</if>

<!-- 改为 MySQL 语法 -->
<if test="taskName != null and taskName != ''">
    AND task_name LIKE CONCAT('%', #{taskName}, '%')
</if>
```

#### 5.3 ProblemBookMapper.xml

**位置**: `src/main/resources/mapper/ProblemBookMapper.xml`

**修改内容**:

```xml
<!-- 原 PostgreSQL 语法（使用 similarity 函数） -->
<if test="bookName != null and bookName != ''">
    (#{bookName} IS NOT NULL AND similarity(book_name, #{bookName}) > 0.6)
</if>
ORDER BY similarity(book_name, #{bookName}) DESC

<!-- 改为 MySQL 语法（方案1：简单兼容） -->
<if test="bookName != null and bookName != ''">
    (#{bookName} IS NOT NULL AND book_name LIKE CONCAT('%', #{bookName}, '%'))
</if>
ORDER BY 
    CASE 
        WHEN book_name = #{bookName} THEN 0
        WHEN book_name LIKE CONCAT(#{bookName}, '%') THEN 1
        WHEN book_name LIKE CONCAT('%', #{bookName}, '%') THEN 2
        ELSE 3
    END

<!-- 改为 MySQL 语法（方案2：全文检索，性能更好） -->
<if test="bookName != null and bookName != ''">
    MATCH(book_name) AGAINST(#{bookName} IN NATURAL LANGUAGE MODE)
</if>
ORDER BY 
    MATCH(book_name) AGAINST(#{bookName} IN NATURAL LANGUAGE MODE) DESC
```

**推荐使用方案1**（简单兼容），如果数据量大且性能不足，再考虑方案2。

### 步骤 6：重新编译和启动

```bash
# 清理并重新编译
mvn clean compile

# 启动应用
mvn spring-boot:run

# 或使用提供的批处理脚本
build.bat
run.bat
```

---

## 代码修改说明

### PostgreSQL vs MySQL 主要差异

| 特性 | PostgreSQL | MySQL 8.0 |
|------|-----------|-----------|
| 自增主键 | `BIGSERIAL` | `BIGINT AUTO_INCREMENT` |
| 布尔类型 | `BOOLEAN` (true/false) | `TINYINT(1)` (1/0) |
| 时间戳 | `TIMESTAMP` | `DATETIME` |
| 大小写不敏感搜索 | `ILIKE` | `LIKE` (需配置 collation) |
| 相似度搜索 | `similarity()` (pg_trgm) | `FULLTEXT` + `MATCH AGAINST` |
| 全文索引 | `GIN` + `gin_trgm_ops` | `FULLTEXT` + `WITH PARSER ngram` |
| 数组类型 | 支持 | 不支持（用 JSON 替代） |
| JSON 操作 | 强大的 JSONB | 支持但功能较弱 |

### 已处理的兼容性问题

✅ **数据类型转换**
- `BIGSERIAL` → `BIGINT AUTO_INCREMENT`
- `BOOLEAN` → `TINYINT(1)`
- `TIMESTAMP` → `DATETIME`

✅ **索引优化**
- PostgreSQL GIN 索引 → MySQL FULLTEXT 索引（使用 ngram 解析器支持中文）
- 保留了所有必要的 B-tree 索引

✅ **约束处理**
- `CHECK` 约束在 MySQL 8.0+ 中支持
- 外键约束完全兼容

✅ **字符集和排序规则**
- 使用 `utf8mb4` 字符集（支持完整 Unicode，包括 emoji）
- 使用 `utf8mb4_unicode_ci` 排序规则（大小写不敏感）

---

## 数据迁移

### 从 PostgreSQL 迁移现有数据

如果你已经在 PostgreSQL 中有数据，需要迁移到 MySQL：

#### 方法 1：使用 CSV 导出导入

**1. 从 PostgreSQL 导出**

```sql
-- 导出用户表
COPY sys_user TO '/tmp/sys_user.csv' WITH CSV HEADER;

-- 导出敏感词表
COPY sensitive_words TO '/tmp/sensitive_words.csv' WITH CSV HEADER;

-- 其他表类似...
```

**2. 导入到 MySQL**

```sql
-- 导入用户表
LOAD DATA LOCAL INFILE '/tmp/sys_user.csv'
INTO TABLE sys_user
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;

-- 其他表类似...
```

**注意事项**：
- 布尔值需要转换：`true` → `1`, `false` → `0`
- 时间戳格式可能需要调整
- 自增主键可能需要重置序列

#### 方法 2：使用数据迁移工具

推荐工具：
- **pgloader**: 专门用于 PostgreSQL 到 MySQL 的迁移
- **DBeaver**: 图形化工具，支持跨数据库数据传输
- **Navicat**: 商业工具，提供数据传输功能

#### 方法 3：使用 Spring Boot 代码迁移

编写一个临时的迁移程序：
1. 同时连接 PostgreSQL 和 MySQL
2. 从 PostgreSQL 读取数据
3. 转换后写入 MySQL

---

## 常见问题

### Q1: 启动时报错 "Unknown database 'library_management'"

**解决方案**：
```bash
# 确保数据库已创建
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS library_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### Q2: 中文显示乱码

**解决方案**：
1. 检查数据库字符集：
   ```sql
   SHOW CREATE DATABASE library_management;
   ```
   应该显示 `CHARACTER SET utf8mb4`

2. 检查连接字符集：
   ```yaml
   url: jdbc:mysql://localhost:3306/library_management?characterEncoding=utf-8
   ```

3. 检查 MySQL 配置文件 `my.ini` 或 `my.cnf`：
   ```ini
   [client]
   default-character-set=utf8mb4

   [mysql]
   default-character-set=utf8mb4

   [mysqld]
   character-set-server=utf8mb4
   collation-server=utf8mb4_unicode_ci
   ```

### Q3: 搜索功能不准确

**问题**：模糊搜索效果不如 PostgreSQL

**解决方案**：
1. 确保使用了 FULLTEXT 索引（已在 schema 中创建）
2. 对于中文搜索，使用 ngram 解析器（已配置）
3. 调整搜索语法：
   ```xml
   <!-- 使用全文搜索 -->
   MATCH(book_name) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE)
   ```

### Q4: 性能比 PostgreSQL 慢

**优化建议**：

1. **启用查询缓存**（MySQL 8.0 已移除，使用应用层缓存）
2. **优化索引**：
   ```sql
   -- 分析表
   ANALYZE TABLE sensitive_words;
   
   -- 查看索引使用情况
   EXPLAIN SELECT * FROM sensitive_words WHERE keyword LIKE '%test%';
   ```

3. **调整 MySQL 配置**：
   ```ini
   [mysqld]
   innodb_buffer_pool_size=1G
   innodb_log_file_size=256M
   max_connections=200
   ```

### Q5: 无法连接到 MySQL

**检查清单**：
- ✅ MySQL 服务是否运行：`net start mysql` (Windows) 或 `systemctl status mysql` (Linux)
- ✅ 端口是否开放：`netstat -an | findstr 3306`
- ✅ 防火墙是否放行
- ✅ 用户名密码是否正确
- ✅ 用户是否有远程访问权限

### Q6: 全文索引不生效

**解决方案**：

1. 检查 ngram 分词器配置：
   ```sql
   -- 查看 ngram token size（默认为 2）
   SHOW VARIABLES LIKE 'ngram_token_size';
   ```

2. 重建全文索引：
   ```sql
   ALTER TABLE sensitive_words DROP INDEX idx_sw_keyword_fulltext;
   CREATE FULLTEXT INDEX idx_sw_keyword_fulltext ON sensitive_words(keyword) WITH PARSER ngram;
   ```

3. 测试全文搜索：
   ```sql
   SELECT * FROM sensitive_words 
   WHERE MATCH(keyword) AGAINST('测试' IN NATURAL LANGUAGE MODE);
   ```

---

## 性能对比

| 操作 | PostgreSQL | MySQL 8.0 | 备注 |
|------|-----------|-----------|------|
| 简单查询 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 性能相当 |
| 模糊搜索 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | PG 的 pg_trgm 更强 |
| 全文搜索 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | 中文搜索都需要额外配置 |
| 写入性能 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | MySQL InnoDB 略优 |
| 并发读取 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | PG MVCC 更好 |

---

## 回滚到 PostgreSQL

如果迁移后发现问题，可以快速回滚：

1. 修改 `application.yml` 的 profile 为 `dev`（PostgreSQL）
2. 恢复 `pom.xml` 中的 PostgreSQL 驱动
3. 恢复 Mapper XML 文件中的 `ILIKE` 和 `similarity()` 语法
4. 重新编译和启动

---

## 技术支持

如有问题，请检查：
1. 本文档的常见问题部分
2. MySQL 官方文档：https://dev.mysql.com/doc/
3. Spring Boot 数据库配置文档

---

## 更新日志

- **2025-11-06**: 初始版本，支持从 PostgreSQL 迁移到 MySQL 8.0
- 包含完整的建表脚本、数据初始化脚本和迁移指南

---

**祝迁移顺利！** 🎉

