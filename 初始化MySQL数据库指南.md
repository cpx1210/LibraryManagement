# 🗄️ MySQL 数据库初始化指南

## 方式一：使用MySQL命令行（推荐）

### 步骤1：打开命令提示符（CMD）
按 `Win + R`，输入 `cmd`，回车

### 步骤2：进入MySQL bin目录
```cmd
cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"
```

### 步骤3：登录MySQL
```cmd
mysql -uroot -p
```
输入密码：`583201844Aa*`

### 步骤4：执行建表脚本
在MySQL命令行中执行：
```sql
SOURCE D:/LibraryManagement/library-management-backend/src/main/resources/db/mysql_schema.sql
```

### 步骤5：执行初始数据脚本
```sql
SOURCE D:/LibraryManagement/library-management-backend/src/main/resources/db/mysql_data_cn.sql
```

### 步骤6：验证安装
```sql
USE library_management;
SHOW TABLES;
SELECT username, role FROM users;
```

应该能看到 9 张表和 2 个用户（admin 和 testuser）

---

## 方式二：使用MySQL Workbench（可视化）

1. 打开 MySQL Workbench
2. 连接到本地MySQL服务器（密码：5832）
3. 点击 "File" → "Open SQL Script"
4. 依次打开并执行：
   - `mysql_schema.sql`（建表）
   - `mysql_data_cn.sql`（初始数据）
5. 刷新左侧的 SCHEMAS，查看 `library_management` 数据库

---

## 验证结果

成功后应该看到以下9张表：
- ✅ users（用户表）
- ✅ sensitive_categories（敏感词分类）
- ✅ sensitive_words（敏感词）
- ✅ problem_books（问题书目）
- ✅ publisher_whitelist（出版社白名单）
- ✅ booklist_check_task（检测任务）
- ✅ booklist_check_detail（检测详情）
- ✅ purchased_problem_books（已购问题图书）
- ✅ operation_log（操作日志）

默认用户：
- 管理员：`admin` / `admin123`
- 普通用户：`testuser` / `password123`

---

## 完成后的下一步

数据库初始化完成后，执行：
```cmd
cd D:\LibraryManagement\library-management-backend
.\run.bat
```

后端会在 http://localhost:8080/api 启动

然后启动前端：
```cmd
cd D:\LibraryManagement\library-management-frontend
npm run dev
```

前端会在 http://localhost:5173 启动

---

## 🎉 大功告成！

所有代码已经成功迁移到MySQL，只需要手动执行一次数据库初始化即可！

