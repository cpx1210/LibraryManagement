# 数据库初始化说明

本目录包含图书馆问题图书管理系统的数据库初始化脚本。

---

## 📁 文件清单

本目录只保留必要的脚本文件，确保从零搭建数据库的简洁性和准确性：

| 文件名 | 说明 | 执行顺序 |
|--------|------|----------|
| **schema.sql** | 数据库建表脚本（包含所有最新表结构） | ① 第一个执行 |
| **data.sql** | 初始化数据脚本（用户、分类、白名单等） | ② 第二个执行 |
| **data_cn.sql** | 中文初始化数据脚本（可选） | ③ 可选执行 |
| **creat_database.bat** | Windows 一键初始化批处理脚本 | - |
| **README.md** | 本说明文档 | - |

---

## 📊 数据表结构

系统共包含 **9张数据表**：

| 序号 | 表名 | 说明 | 记录数估算 |
|------|------|------|-----------|
| 1 | sys_user | 用户信息表 | ~50 |
| 2 | sensitive_categories | 敏感词分类表 | ~10 |
| 3 | sensitive_words | 敏感词库表 | ~2,000 |
| 4 | problem_books | 问题书目库表 | ~10,000 |
| 5 | publisher_whitelist | 出版社白名单表 | ~100 |
| 6 | purchased_problem_books | 已购问题图书库表 | ~1,000 |
| 7 | operation_log | 操作日志表 | 无上限 |
| 8 | booklist_check_task | 书单检测任务表 | 无上限 |
| 9 | booklist_check_detail | 书单检测结果明细表 | 无上限 |

### 🆕 最新更新（2025-10-30）

#### sensitive_words 表新增字段：
- `detection_type` VARCHAR(20) - 检测类型（关键词/书名/作者）
- `alert_message` VARCHAR(200) - 警报信息

#### booklist_check_detail 表新增字段：
- `book_number` VARCHAR(50) - 书号
- `subtitle` VARCHAR(200) - 副题名
- `author1` VARCHAR(100) - 著者1
- `author2` VARCHAR(100) - 著者2
- `publish_location` VARCHAR(100) - 出版地
- `publish_date` VARCHAR(50) - 出版日期
- `target_audience` VARCHAR(100) - 读者对象
- `content_summary` TEXT - 内容简介
- `classification_number` VARCHAR(50) - 分类号
- `language` VARCHAR(50) - 作品语种

---

## 🚀 快速开始

### 方法一：使用 psql 命令行（推荐）

#### 步骤1：创建数据库

```bash
# 连接到 PostgreSQL
psql -U postgres

# 创建数据库（指定UTF-8编码）
CREATE DATABASE library_management
    WITH ENCODING='UTF8'
    LC_COLLATE='zh_CN.UTF-8'
    LC_CTYPE='zh_CN.UTF-8'
    TEMPLATE=template0;

# 退出 psql
\q
```

#### 步骤2：执行建表脚本

```bash
# 切换到 db 目录
cd library-management-backend/src/main/resources/db

# 执行 schema.sql
psql -U postgres -d library_management -f schema.sql
```

#### 步骤3：执行初始化数据脚本

```bash
# 执行 data.sql
psql -U postgres -d library_management -f data.sql
```

#### 步骤4：验证安装

```bash
# 连接数据库
psql -U postgres -d library_management

# 查看所有表
\dt

# 验证数据
SELECT COUNT(*) FROM sys_user;
SELECT COUNT(*) FROM sensitive_categories;

# 退出
\q
```

---

### 方法二：使用图形化工具

支持使用 DBeaver、pgAdmin、Navicat 等工具：

1. 打开数据库连接工具
2. 创建新数据库 `library_management`（UTF-8编码）
3. 打开 SQL 编辑器
4. 依次执行以下脚本：
   - `schema.sql`
   - `data.sql`
5. 验证表结构和数据

---

### 方法三：Windows 一键初始化

```powershell
# 在 Windows 命令行或 PowerShell 中执行
cd library-management-backend\src\main\resources\db
creat_database.bat
```

**注意**: 需要修改 `creat_database.bat` 中的数据库连接信息。

---

## 🔐 默认账号

### 管理员账号
- **用户名**: `admin`
- **密码**: `admin123`
- **角色**: 管理员（admin）
- **权限**: 全部功能

### 测试账号
- **用户名**: `testuser`
- **密码**: `user123`
- **角色**: 普通用户（user）
- **权限**: 只读权限

⚠️ **安全提示**: 生产环境部署时，请务必修改默认密码！

---

## 📋 执行顺序

```
1. 创建数据库
   ↓
2. 执行 schema.sql  ← 创建所有表结构
   ↓
3. 执行 data.sql    ← 插入初始数据
   ↓
4. （可选）执行 data_cn.sql
   ↓
5. 验证安装
```

**重要**: 必须严格按照此顺序执行，否则会因为外键约束导致失败。

---

## ✅ 验证数据库初始化

执行以下 SQL 验证初始化是否成功：

```sql
-- 1. 查看所有表
SELECT tablename FROM pg_tables
WHERE schemaname = 'public'
ORDER BY tablename;

-- 2. 查看表记录数
SELECT
    'sys_user' as table_name, COUNT(*) as count FROM sys_user
UNION ALL SELECT 'sensitive_categories', COUNT(*) FROM sensitive_categories
UNION ALL SELECT 'sensitive_words', COUNT(*) FROM sensitive_words
UNION ALL SELECT 'problem_books', COUNT(*) FROM problem_books
UNION ALL SELECT 'publisher_whitelist', COUNT(*) FROM publisher_whitelist
UNION ALL SELECT 'purchased_problem_books', COUNT(*) FROM purchased_problem_books
UNION ALL SELECT 'operation_log', COUNT(*) FROM operation_log
UNION ALL SELECT 'booklist_check_task', COUNT(*) FROM booklist_check_task
UNION ALL SELECT 'booklist_check_detail', COUNT(*) FROM booklist_check_detail;

-- 3. 验证管理员账号
SELECT username, real_name, role FROM sys_user WHERE username = 'admin';

-- 4. 验证新字段是否存在
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'sensitive_words'
  AND column_name IN ('detection_type', 'alert_message');

SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'booklist_check_detail'
  AND column_name IN ('book_number', 'subtitle', 'author1', 'author2');
```

**期望结果**：
- 应该看到 9 张表
- sys_user 表至少有 2 条记录
- sensitive_words 表包含 detection_type 和 alert_message 字段
- booklist_check_detail 表包含新增的书目字段

---

## 📝 注意事项

### 1. PostgreSQL 版本要求
- **最低版本**: PostgreSQL 15.x
- **推荐版本**: PostgreSQL 15.x 或更高

### 2. 字符集设置
必须使用 UTF-8 编码，确保中文数据正常存储：

```sql
CREATE DATABASE library_management
    WITH ENCODING='UTF8'
    LC_COLLATE='zh_CN.UTF-8'
    LC_CTYPE='zh_CN.UTF-8'
    TEMPLATE=template0;
```

### 3. 扩展依赖
脚本会自动安装以下扩展：
- `pg_trgm` - 用于模糊匹配加速
- `btree_gin` - 用于组合索引优化

如果安装失败，请确保具有超级用户权限。

### 4. 重复执行
- ✅ `schema.sql` 使用了 `DROP TABLE IF EXISTS`，可以重复执行
- ⚠️ `data.sql` 重复执行会因为唯一约束失败（可忽略错误）

### 5. 数据备份
在生产环境执行前，请务必：
1. 备份现有数据
2. 在测试环境验证脚本
3. 确认无误后再在生产环境执行

### 6. 权限要求
- 创建数据库：需要 CREATEDB 权限
- 创建扩展：需要超级用户权限
- 创建表和索引：需要数据库所有者权限

---

## 🔧 常见问题

### Q1: 提示 "permission denied to create extension"

**解决方案**: 使用超级用户（postgres）执行脚本：

```bash
psql -U postgres -d library_management -f schema.sql
```

或手动创建扩展：

```sql
-- 使用超级用户连接
psql -U postgres -d library_management

-- 手动创建扩展
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS btree_gin;
```

### Q2: 外键约束错误

**原因**: 未按顺序执行脚本

**解决方案**: 确保先执行 schema.sql，再执行 data.sql

### Q3: 字符编码问题

**解决方案**:
- Windows: 使用 `chcp 65001` 切换到 UTF-8 编码
- Linux/Mac: 确保终端使用 UTF-8 编码
- 图形工具: 设置连接字符集为 UTF-8

### Q4: 密码验证失败

**原因**: data.sql 中的密码使用 BCrypt 加密

**解决方案**: 使用明文密码登录（系统会自动验证）：
- 管理员：`admin` / `admin123`
- 测试用户：`testuser` / `user123`

### Q5: 表已存在错误

**解决方案**: 脚本使用了 `DROP TABLE IF EXISTS`，正常重新执行 schema.sql 即可

---

## 🔄 更新已有数据库

如果您已经有旧版本的数据库，需要升级到新版本：

### 方式一：完全重建（推荐，数据会丢失）

```bash
# 1. 备份数据（重要！）
pg_dump -U postgres library_management > backup.sql

# 2. 删除旧数据库
psql -U postgres -c "DROP DATABASE IF EXISTS library_management;"

# 3. 按照"快速开始"步骤重新创建
```

### 方式二：仅更新表结构（保留数据）

```bash
# 连接数据库
psql -U postgres -d library_management

# 手动执行以下 SQL
ALTER TABLE sensitive_words
ADD COLUMN IF NOT EXISTS detection_type VARCHAR(20) NOT NULL DEFAULT '关键词'
CHECK (detection_type IN ('关键词', '书名', '作者'));

ALTER TABLE sensitive_words
ADD COLUMN IF NOT EXISTS alert_message VARCHAR(200);

-- 添加更多字段...详见之前创建的 update_for_new_template.sql
```

---

## 📖 相关文档

- [技术方案与架构设计.md](../../../../../技术方案与架构设计.md) - 详细的数据库设计说明
- [新模板适配说明.md](../../../../../新模板适配说明.md) - 最新模板更新说明
- [Spring Boot 配置文件](../application.yml) - 数据库连接配置

---

## 📞 技术支持

如遇到问题，请：

1. 查看错误日志
2. 检查 PostgreSQL 版本
3. 验证数据库连接配置
4. 查看本文档"常见问题"部分
5. 提交 Issue 到项目仓库

---

## 📅 更新日志

### 2025-10-30
- ✨ 添加 `sensitive_words.detection_type` 字段（支持按类型精确检测）
- ✨ 添加 `sensitive_words.alert_message` 字段（支持自定义警报信息）
- ✨ 扩展 `booklist_check_detail` 表，支持新的书目模板（13个字段）
- 📝 更新文档说明
- 🗑️ 清理过时脚本，仅保留必要文件

### 2025-10-27
- 📝 初始版本，包含 9 张表的完整结构

---

**最后更新**: 2025-10-30
**维护者**: LibraryManagement 开发团队
**数据库版本**: v1.1.0
