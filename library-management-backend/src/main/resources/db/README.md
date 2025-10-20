# 数据库初始化说明

本目录包含图书馆问题图书管理系统的数据库初始化脚本。

## 📁 文件说明

### 1. schema.sql - 数据库建表脚本
- **用途**: 创建所有数据表结构
- **执行顺序**: 第一个执行
- **包含内容**:
  - 9张数据表的完整DDL语句
  - 所有表的索引定义
  - 外键约束定义
  - 字段注释说明

### 2. data.sql - 初始化数据脚本
- **用途**: 插入系统初始数据
- **执行顺序**: 第二个执行（在schema.sql之后）
- **包含内容**:
  - 默认管理员账号
  - 测试用户账号
  - 敏感词分类
  - 示例敏感词数据
  - 示例出版社白名单

### 3. creat_database.bat - Windows批处理脚本
- **用途**: Windows系统下自动执行数据库初始化
- **功能**: 自动执行schema.sql和data.sql

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

## 🚀 初始化步骤

### 方法一：使用 psql 命令行工具（推荐）

#### 步骤1：创建数据库
```bash
# 连接到 PostgreSQL
psql -U postgres

# 创建数据库
CREATE DATABASE library_management;

# 退出 psql
\q
```

#### 步骤2：执行建表脚本
```bash
# 执行建表脚本
psql -U postgres -d library_management -f schema.sql
```

#### 步骤3：执行初始化数据脚本
```bash
# 执行数据初始化脚本
psql -U postgres -d library_management -f data.sql
```

### 方法二：使用图形化工具（如 DBeaver、pgAdmin）

1. 打开数据库连接工具
2. 创建新数据库 `library_management`
3. 选择数据库
4. 依次执行以下脚本：
   - 先执行 `schema.sql`
   - 再执行 `data.sql`

### 方法三：使用 Windows 批处理脚本

```bash
# 在 Windows 命令行中执行
cd src/main/resources/db
creat_database.bat
```

**注意**: 需要修改批处理脚本中的数据库连接信息。

## 🔐 默认账号信息

### 管理员账号
- **用户名**: `admin`
- **密码**: `admin123`
- **角色**: 管理员（admin）
- **权限**: 所有功能

### 测试账号
- **用户名**: `testuser`
- **密码**: `user123`
- **角色**: 普通用户（user）
- **权限**: 只读权限

⚠️ **重要**: 生产环境部署时，请务必修改默认密码！

## 📝 注意事项

### 1. PostgreSQL 版本要求
- **最低版本**: PostgreSQL 15.x
- **推荐版本**: PostgreSQL 15.x 或更高

### 2. 字符集设置
```sql
-- 创建数据库时指定字符集
CREATE DATABASE library_management
    WITH ENCODING='UTF8'
    LC_COLLATE='zh_CN.UTF-8'
    LC_CTYPE='zh_CN.UTF-8'
    TEMPLATE=template0;
```

### 3. 扩展依赖
脚本会自动安装以下扩展：
- `pg_trgm`: 用于模糊匹配加速
- `btree_gin`: 用于组合索引优化

如果安装失败，请确保具有超级用户权限。

### 4. 执行顺序
必须按照以下顺序执行脚本：
```
schema.sql → data.sql
```

### 5. 重复执行
- `schema.sql` 使用了 `DROP TABLE IF EXISTS`，可以重复执行
- `data.sql` 第一次执行成功后，重复执行会因为唯一约束失败

### 6. 数据备份
在生产环境执行前，请务必：
1. 备份现有数据
2. 在测试环境验证脚本
3. 确认无误后再在生产环境执行

## 🔍 验证数据库初始化

执行以下 SQL 验证初始化是否成功：

```sql
-- 1. 查看所有表
SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;

-- 2. 查看表记录数
SELECT
    'sys_user' as table_name, COUNT(*) as count FROM sys_user
UNION ALL
SELECT 'sensitive_categories', COUNT(*) FROM sensitive_categories
UNION ALL
SELECT 'sensitive_words', COUNT(*) FROM sensitive_words
UNION ALL
SELECT 'problem_books', COUNT(*) FROM problem_books
UNION ALL
SELECT 'publisher_whitelist', COUNT(*) FROM publisher_whitelist
UNION ALL
SELECT 'purchased_problem_books', COUNT(*) FROM purchased_problem_books
UNION ALL
SELECT 'operation_log', COUNT(*) FROM operation_log
UNION ALL
SELECT 'booklist_check_task', COUNT(*) FROM booklist_check_task
UNION ALL
SELECT 'booklist_check_detail', COUNT(*) FROM booklist_check_detail;

-- 3. 验证管理员账号
SELECT username, real_name, role FROM sys_user WHERE username = 'admin';
```

**期望结果**：
- 应该看到 9 张表
- sys_user 表至少有 2 条记录
- 管理员账号存在且角色为 admin

## 📖 相关文档

- [技术方案与架构设计.md](../../../../../技术方案与架构设计.md) - 查看详细的数据库设计说明
- [Spring Boot 配置文件](../application-dev.yml) - 配置数据库连接信息

## 🆘 常见问题

### Q1: 提示 "permission denied to create extension"
**解决方案**: 使用超级用户（postgres）执行脚本，或者手动创建扩展：
```sql
-- 使用超级用户连接
psql -U postgres -d library_management

-- 手动创建扩展
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS btree_gin;
```

### Q2: 外键约束错误
**解决方案**: 确保按顺序执行脚本，先执行 schema.sql，再执行 data.sql

### Q3: 字符编码问题
**解决方案**:
- Windows: 使用 `chcp 65001` 切换到 UTF-8 编码
- Linux/Mac: 确保终端使用 UTF-8 编码

### Q4: 密码验证失败
**解决方案**:
- data.sql 中的密码已经过 BCrypt 加密
- 使用明文密码 `admin123` 或 `user123` 登录即可

## 📧 技术支持

如遇到问题，请：
1. 查看错误日志
2. 检查 PostgreSQL 版本
3. 验证数据库连接配置
4. 提交 Issue 到项目仓库

---

**最后更新**: 2025-10-18
**维护者**: LibraryManagement 开发团队
