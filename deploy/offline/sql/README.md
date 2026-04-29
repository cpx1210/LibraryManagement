# MySQL 数据库脚本说明

本目录包含了图书馆问题书目管理系统的 MySQL 版本数据库脚本。

## 📁 文件列表

### MySQL 专用文件

| 文件名 | 说明 | 用途 |
|--------|------|------|
| `mysql_schema.sql` | MySQL 建表脚本 | 创建数据库和所有表结构 |
| `mysql_data_cn.sql` | MySQL 中文数据脚本 | 插入初始数据（中文版） |
| `mysql_patch_20260424_submitter_fields.sql` | 上传人信息补丁脚本 | 为现有 `booklist_check_task` 表补充上传人字段 |
| `mysql_create_database.bat` | 数据库初始化批处理 | Windows 一键初始化脚本 |
| `MYSQL_MIGRATION_GUIDE.md` | MySQL 迁移指南 | 详细的迁移步骤和说明 |
| `MYSQL_CODE_CHANGES.md` | 代码修改清单 | 需要修改的代码位置 |
| `MYSQL_README.md` | 本文件 | MySQL 脚本总览 |

### PostgreSQL 原有文件（保留）

| 文件名 | 说明 |
|--------|------|
| `schema.sql` | PostgreSQL 建表脚本 |
| `data.sql` | PostgreSQL 英文数据脚本 |
| `data_cn.sql` | PostgreSQL 中文数据脚本 |
| `migrate_to_v1.1.0.sql` | PostgreSQL 升级脚本 |
| `creat_database.bat` | PostgreSQL 初始化脚本 |

---

## 🚀 快速开始

### 方式 1：使用批处理脚本（推荐）

```bash
# 1. 编辑 mysql_create_database.bat，设置数据库连接信息
# 2. 双击运行批处理文件
mysql_create_database.bat
```

### 方式 2：手动执行 SQL 脚本

```bash
# 1. 创建数据库和表
mysql -u root -p --default-character-set=utf8mb4 < mysql_schema.sql

# 2. 插入初始数据
mysql -u root -p --default-character-set=utf8mb4 < mysql_data_cn.sql

# 3. 如果是旧库升级，再执行上传人字段补丁
mysql -u root -p --default-character-set=utf8mb4 library_management < mysql_patch_20260424_submitter_fields.sql
```

---

## 📊 数据库结构

### 数据库信息

- **数据库名**: `library_management`
- **字符集**: `utf8mb4`
- **排序规则**: `utf8mb4_unicode_ci`
- **引擎**: InnoDB

### 表结构（共 9 张表）

| 序号 | 表名 | 说明 | 记录数（初始） |
|------|------|------|----------------|
| 1 | `sys_user` | 用户信息表 | 2 |
| 2 | `sensitive_categories` | 敏感词分类表 | 5 |
| 3 | `sensitive_words` | 敏感词表 | 0 |
| 4 | `problem_books` | 问题图书表 | 0 |
| 5 | `publisher_whitelist` | 出版社白名单表 | 20 |
| 6 | `purchased_problem_books` | 已采购问题图书表 | 0 |
| 7 | `operation_log` | 操作日志表 | 0 |
| 8 | `booklist_check_task` | 书目检测任务表 | 0 |
| 9 | `booklist_check_detail` | 书目检测详情表 | 0 |

---

## 🔑 初始账号

### 管理员账号

- **用户名**: `admin`
- **密码**: `admin123`
- **角色**: 管理员
- **部门**: 系统管理部

### 测试账号

- **用户名**: `testuser`
- **密码**: `user123`
- **角色**: 普通用户
- **部门**: 图书管理部

⚠️ **重要**: 生产环境部署后请立即修改默认密码！

---

## 🔄 从 PostgreSQL 迁移

如果你当前使用的是 PostgreSQL，需要迁移到 MySQL，请按以下步骤操作：

### 步骤概览

1. **初始化 MySQL 数据库**
   ```bash
   mysql_create_database.bat
   ```

2. **修改项目依赖**
   - 编辑 `pom.xml`
   - 替换 PostgreSQL 驱动为 MySQL 驱动

3. **修改配置文件**
   - 编辑 `application.yml`
   - 或使用 `application-mysql.yml`

4. **修改 Mapper 文件**
   - `SensitiveWordMapper.xml`
   - `BooklistCheckTaskMapper.xml`
   - `ProblemBookMapper.xml`

5. **重新编译和启动**
   ```bash
   mvn clean compile
   mvn spring-boot:run
   ```

### 详细说明

请参考以下文档：

- 📖 **完整迁移指南**: `MYSQL_MIGRATION_GUIDE.md`
- 📝 **代码修改清单**: `MYSQL_CODE_CHANGES.md`

---

## 🆚 PostgreSQL vs MySQL 主要差异

### 数据类型

| PostgreSQL | MySQL | 说明 |
|-----------|-------|------|
| `BIGSERIAL` | `BIGINT AUTO_INCREMENT` | 自增主键 |
| `BOOLEAN` | `TINYINT(1)` | 布尔类型 |
| `TIMESTAMP` | `DATETIME` | 时间戳 |
| `TEXT` | `TEXT` | 长文本（相同） |

### SQL 语法

| PostgreSQL | MySQL | 说明 |
|-----------|-------|------|
| `ILIKE` | `LIKE` | 大小写不敏感搜索 |
| `similarity()` | `MATCH AGAINST` | 相似度/全文搜索 |
| `CURRENT_DATE` | `CURDATE()` | 当前日期 |
| `TRUE`/`FALSE` | `1`/`0` | 布尔值 |

### 索引

| PostgreSQL | MySQL | 说明 |
|-----------|-------|------|
| `GIN` + `gin_trgm_ops` | `FULLTEXT` + `ngram` | 全文索引 |
| B-tree（默认） | B-tree（默认） | 普通索引（相同） |

---

## 📝 脚本详细说明

### mysql_schema.sql

**功能**: 创建数据库和所有表结构

**包含内容**:
- 数据库创建（`library_management`）
- 9 张表的创建语句
- 所有索引（包括全文索引）
- 外键约束
- CHECK 约束
- 字段注释

**特点**:
- ✅ 使用 `utf8mb4` 字符集（支持完整 Unicode）
- ✅ 使用 `InnoDB` 引擎（支持事务和外键）
- ✅ 为中文字段创建 `FULLTEXT` 索引（使用 `ngram` 解析器）
- ✅ 包含详细的字段注释

**执行时间**: 约 1-2 秒

### mysql_data_cn.sql

**功能**: 插入初始数据（中文版）

**包含内容**:
- 2 个用户账号（admin + testuser）
- 5 个敏感词分类
- 20 个出版社白名单

**特点**:
- ✅ 密码使用 BCrypt 加密
- ✅ 所有文本使用中文
- ✅ 包含测试数据

**执行时间**: 约 0.5 秒

### mysql_create_database.bat

**功能**: Windows 批处理脚本，一键初始化数据库

**执行流程**:
1. 检查 MySQL 客户端是否可用
2. 测试数据库连接
3. 执行 `mysql_schema.sql`
4. 执行 `mysql_data_cn.sql`
5. 验证表创建成功

**配置项**:
```batch
set MYSQL_HOST=localhost      # MySQL 主机
set MYSQL_PORT=3306           # MySQL 端口
set MYSQL_USER=root           # MySQL 用户名
set MYSQL_PASSWORD=           # MySQL 密码（留空会提示输入）
```

**执行时间**: 约 5-10 秒

---

## 🔍 验证安装

### 1. 检查数据库

```sql
-- 查看数据库
SHOW DATABASES LIKE 'library_management';

-- 切换到数据库
USE library_management;

-- 查看所有表
SHOW TABLES;

-- 应该显示 9 张表
```

### 2. 检查表结构

```sql
-- 查看用户表结构
DESC sys_user;

-- 查看敏感词表结构
DESC sensitive_words;
```

### 3. 检查初始数据

```sql
-- 查看用户数量
SELECT COUNT(*) FROM sys_user;
-- 应该返回 2

-- 查看分类数量
SELECT COUNT(*) FROM sensitive_categories;
-- 应该返回 5

-- 查看白名单数量
SELECT COUNT(*) FROM publisher_whitelist;
-- 应该返回 20
```

### 4. 测试登录

```sql
-- 查看管理员账号
SELECT username, role, real_name, department 
FROM sys_user 
WHERE username = 'admin';
```

---

## 🛠️ 维护和优化

### 定期维护

```sql
-- 分析表（更新统计信息）
ANALYZE TABLE sensitive_words;
ANALYZE TABLE problem_books;

-- 优化表（整理碎片）
OPTIMIZE TABLE sensitive_words;
OPTIMIZE TABLE problem_books;

-- 检查表
CHECK TABLE sensitive_words;
```

### 性能优化

```sql
-- 查看索引使用情况
SHOW INDEX FROM sensitive_words;

-- 查看表大小
SELECT 
    table_name AS '表名',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS '大小(MB)'
FROM information_schema.TABLES
WHERE table_schema = 'library_management'
ORDER BY (data_length + index_length) DESC;

-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query_log';
SHOW VARIABLES LIKE 'long_query_time';
```

### 备份和恢复

```bash
# 备份数据库
mysqldump -u root -p --default-character-set=utf8mb4 library_management > backup.sql

# 恢复数据库
mysql -u root -p --default-character-set=utf8mb4 library_management < backup.sql
```

---

## ❓ 常见问题

### Q1: 执行脚本时出现字符编码错误

**解决方案**:
```bash
# 确保使用 utf8mb4 字符集
mysql -u root -p --default-character-set=utf8mb4 < mysql_schema.sql
```

### Q2: 全文索引不生效

**解决方案**:
```sql
-- 检查 ngram token size
SHOW VARIABLES LIKE 'ngram_token_size';

-- 重建全文索引
ALTER TABLE sensitive_words DROP INDEX idx_sw_keyword_fulltext;
CREATE FULLTEXT INDEX idx_sw_keyword_fulltext ON sensitive_words(keyword) WITH PARSER ngram;
```

### Q3: 外键约束创建失败

**原因**: 可能是引用的表还未创建

**解决方案**: 按顺序执行脚本，不要跳过任何步骤

### Q4: 无法连接到 MySQL

**检查清单**:
- [ ] MySQL 服务是否运行
- [ ] 端口 3306 是否开放
- [ ] 用户名密码是否正确
- [ ] 防火墙是否允许连接

---

## 📚 相关文档

- **PostgreSQL 原版脚本**: `schema.sql`, `data_cn.sql`
- **迁移指南**: `MYSQL_MIGRATION_GUIDE.md`
- **代码修改清单**: `MYSQL_CODE_CHANGES.md`
- **数据库升级指南**: `../../../数据库升级指南.md`

---

## 📞 技术支持

如有问题，请：
1. 查看本文档的常见问题部分
2. 查看 `MYSQL_MIGRATION_GUIDE.md` 获取详细说明
3. 检查 MySQL 官方文档

---

## 📅 更新日志

### 2025-11-06
- ✅ 创建 MySQL 版本数据库脚本
- ✅ 添加中文数据初始化脚本
- ✅ 创建 Windows 批处理初始化脚本
- ✅ 编写完整的迁移指南和代码修改清单
- ✅ 支持 MySQL 8.0+ 的所有特性

---

**祝使用愉快！** 🎉

如有任何问题或建议，欢迎反馈。

