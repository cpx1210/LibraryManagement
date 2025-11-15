# MySQL 代码修改清单

本文档列出了从 PostgreSQL 切换到 MySQL 需要修改的所有代码位置。

## 📋 修改清单总览

- [ ] 1. 修改 `pom.xml` - 更换数据库驱动
- [ ] 2. 修改 `application.yml` - 更新数据源配置
- [ ] 3. 修改 `SensitiveWordMapper.xml` - 替换 ILIKE 语法
- [ ] 4. 修改 `BooklistCheckTaskMapper.xml` - 替换 ILIKE 语法
- [ ] 5. 修改 `ProblemBookMapper.xml` - 替换 similarity() 函数

---

## 1. 修改 pom.xml

**文件位置**: `library-management-backend/pom.xml`

**第 107-111 行**，注释掉 PostgreSQL 驱动：

```xml
<!-- 注释掉这部分 -->
<!--
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
-->
```

**在第 111 行后**，添加 MySQL 驱动：

```xml
<!-- 添加 MySQL 驱动 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## 2. 修改 application.yml

**文件位置**: `library-management-backend/src/main/resources/application.yml`

### 方案 A：直接修改 application.yml

**第 5-6 行**，修改 active profile：

```yaml
spring:
  profiles:
    active: mysql  # 改为 mysql
```

**第 8-13 行**，修改数据源配置：

```yaml
  # 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/library_management?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: your_password_here
```

**第 15-23 行**，修改 JPA 配置：

```yaml
  # JPA 配置
  jpa:
    database-platform: org.hibernate.dialect.MySQL8Dialect
    show-sql: true
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        format_sql: true
```

### 方案 B：使用独立配置文件（推荐）

1. 使用已创建的 `application-mysql.yml`
2. 只需修改 `application.yml` 第 6 行：

```yaml
spring:
  profiles:
    active: mysql  # 改为 mysql
```

3. 编辑 `application-mysql.yml`，填入你的数据库密码

---

## 3. 修改 SensitiveWordMapper.xml

**文件位置**: `library-management-backend/src/main/resources/mapper/SensitiveWordMapper.xml`

**第 19 行**，修改 `is_active` 比较：

```xml
<!-- 原代码 -->
WHERE is_active = TRUE

<!-- 改为 -->
WHERE is_active = 1
```

**第 25 行**，替换 `ILIKE` 为 `LIKE`：

```xml
<!-- 原代码 -->
(match_type = 1 AND #{text} ILIKE CONCAT('%', keyword, '%'))

<!-- 改为 -->
(match_type = 1 AND #{text} LIKE CONCAT('%', keyword, '%'))
```

### 完整修改后的 SQL：

```xml
<select id="detectSensitiveWords" resultType="com.library.management.module.sensitiveword.entity.SensitiveWords">
    SELECT
        word_id,
        category_id,
        keyword,
        match_type,
        risk_level,
        detection_type,
        alert_message,
        is_active,
        created_by,
        create_time,
        updated_by,
        update_time
    FROM sensitive_words
    WHERE is_active = 1
      AND (
          -- 精确匹配（match_type = 0）
          (match_type = 0 AND #{text} = keyword)
          OR
          -- 模糊匹配（match_type = 1）
          (match_type = 1 AND #{text} LIKE CONCAT('%', keyword, '%'))
      )
    ORDER BY risk_level DESC, LENGTH(keyword) DESC
</select>
```

---

## 4. 修改 BooklistCheckTaskMapper.xml

**文件位置**: `library-management-backend/src/main/resources/mapper/BooklistCheckTaskMapper.xml`

**第 30 行**，替换 `ILIKE` 为 `LIKE`：

```xml
<!-- 原代码 -->
AND task_name ILIKE CONCAT('%', #{taskName}, '%')

<!-- 改为 -->
AND task_name LIKE CONCAT('%', #{taskName}, '%')
```

**第 47 行**，修改日期函数：

```xml
<!-- 原代码 -->
AND DATE(submit_time) = CURRENT_DATE

<!-- 改为 -->
AND DATE(submit_time) = CURDATE()
```

### 完整修改后的 SQL：

```xml
<!-- 分页查询检测任务列表 -->
<select id="selectTaskPage" resultType="com.library.management.module.detection.entity.BooklistCheckTask">
    SELECT
        task_id,
        task_name,
        task_type,
        submitted_by,
        submit_time,
        start_time,
        end_time,
        original_filename,
        file_path,
        result_file_path,
        status,
        total_books,
        sensitive_hits,
        problem_book_hits,
        non_whitelist_pubs,
        total_problem_books,
        error_message,
        created_time,
        update_time
    FROM booklist_check_task
    <where>
        <if test="taskName != null and taskName != ''">
            AND task_name LIKE CONCAT('%', #{taskName}, '%')
        </if>
        <if test="status != null and status != ''">
            AND status = #{status}
        </if>
        <if test="submittedBy != null">
            AND submitted_by = #{submittedBy}
        </if>
    </where>
    ORDER BY submit_time DESC
</select>

<!-- 获取用户今天提交的任务数量 -->
<select id="countTodayTasksByUser" resultType="int">
    SELECT COUNT(*)
    FROM booklist_check_task
    WHERE submitted_by = #{userId}
      AND DATE(submit_time) = CURDATE()
</select>
```

---

## 5. 修改 ProblemBookMapper.xml

**文件位置**: `library-management-backend/src/main/resources/mapper/ProblemBookMapper.xml`

这是最复杂的修改，因为 PostgreSQL 的 `similarity()` 函数在 MySQL 中不存在。

### 方案 A：简单兼容（推荐）

使用 `LIKE` 和 `CASE` 语句实现基本的相似度匹配：

```xml
<select id="detectProblemBook" resultType="com.library.management.module.problembook.entity.ProblemBook">
    SELECT
        book_id,
        book_name,
        author,
        isbn,
        publisher,
        publish_year,
        problem_type,
        source,
        created_by,
        create_time,
        updated_by,
        update_time
    FROM problem_books
    WHERE
        -- ISBN 精确匹配（优先级最高）
        isbn = #{isbn}
        OR
        -- 书名模糊匹配
        (#{bookName} IS NOT NULL AND book_name LIKE CONCAT('%', #{bookName}, '%'))
    ORDER BY
        -- 优先返回 ISBN 匹配的结果
        CASE WHEN isbn = #{isbn} THEN 0 ELSE 1 END,
        -- 然后按书名匹配度排序（完全匹配 > 前缀匹配 > 包含匹配）
        CASE 
            WHEN book_name = #{bookName} THEN 0
            WHEN book_name LIKE CONCAT(#{bookName}, '%') THEN 1
            WHEN book_name LIKE CONCAT('%', #{bookName}, '%') THEN 2
            ELSE 3
        END,
        -- 最后按书名长度排序（越短越相关）
        LENGTH(book_name)
    LIMIT 1
</select>
```

### 方案 B：全文检索（性能更好，适合大数据量）

使用 MySQL 的 `MATCH AGAINST` 全文检索：

```xml
<select id="detectProblemBook" resultType="com.library.management.module.problembook.entity.ProblemBook">
    SELECT
        book_id,
        book_name,
        author,
        isbn,
        publisher,
        publish_year,
        problem_type,
        source,
        created_by,
        create_time,
        updated_by,
        update_time
    FROM problem_books
    WHERE
        -- ISBN 精确匹配（优先级最高）
        isbn = #{isbn}
        OR
        -- 书名全文检索匹配
        (#{bookName} IS NOT NULL AND MATCH(book_name) AGAINST(#{bookName} IN NATURAL LANGUAGE MODE))
    ORDER BY
        -- 优先返回 ISBN 匹配的结果
        CASE WHEN isbn = #{isbn} THEN 0 ELSE 1 END,
        -- 然后按全文检索相关度排序
        MATCH(book_name) AGAINST(#{bookName} IN NATURAL LANGUAGE MODE) DESC
    LIMIT 1
</select>
```

**注意**：方案 B 需要确保 `book_name` 字段有全文索引（已在 `mysql_schema.sql` 中创建）。

---

## 验证修改

修改完成后，执行以下步骤验证：

### 1. 重新编译

```bash
cd library-management-backend
mvn clean compile
```

### 2. 检查编译错误

如果有错误，检查 XML 语法是否正确。

### 3. 启动应用

```bash
mvn spring-boot:run
```

或使用批处理脚本：

```bash
build.bat
run.bat
```

### 4. 测试功能

- [ ] 登录功能（admin / admin123）
- [ ] 敏感词管理（增删改查）
- [ ] 问题书目管理
- [ ] 书目检测功能
- [ ] 搜索和过滤功能

### 5. 检查日志

查看控制台输出，确认：
- ✅ 数据库连接成功
- ✅ 没有 SQL 语法错误
- ✅ 查询结果正确

---

## 常见错误及解决

### 错误 1: Unknown column 'TRUE' in 'where clause'

**原因**: MySQL 不识别 `TRUE`/`FALSE` 关键字（在某些上下文中）

**解决**: 将 `TRUE` 改为 `1`，`FALSE` 改为 `0`

```xml
<!-- 错误 -->
WHERE is_active = TRUE

<!-- 正确 -->
WHERE is_active = 1
```

### 错误 2: FUNCTION library_management.similarity does not exist

**原因**: MySQL 没有 `similarity()` 函数

**解决**: 使用本文档第 5 节中的方案 A 或方案 B

### 错误 3: You have an error in your SQL syntax near 'ILIKE'

**原因**: MySQL 不支持 `ILIKE`

**解决**: 将 `ILIKE` 改为 `LIKE`

```xml
<!-- 错误 -->
WHERE name ILIKE '%test%'

<!-- 正确 -->
WHERE name LIKE '%test%'
```

**注意**: MySQL 的 `LIKE` 默认是大小写不敏感的（使用 `utf8mb4_unicode_ci` 排序规则时）。

### 错误 4: CURRENT_DATE is not a valid date

**原因**: MySQL 使用 `CURDATE()` 而不是 `CURRENT_DATE`

**解决**: 

```xml
<!-- PostgreSQL -->
WHERE DATE(submit_time) = CURRENT_DATE

<!-- MySQL -->
WHERE DATE(submit_time) = CURDATE()
```

---

## 快速修改脚本

如果你想批量修改，可以使用以下 PowerShell 脚本：

```powershell
# 备份原文件
Copy-Item "src\main\resources\mapper\SensitiveWordMapper.xml" "src\main\resources\mapper\SensitiveWordMapper.xml.bak"
Copy-Item "src\main\resources\mapper\BooklistCheckTaskMapper.xml" "src\main\resources\mapper\BooklistCheckTaskMapper.xml.bak"
Copy-Item "src\main\resources\mapper\ProblemBookMapper.xml" "src\main\resources\mapper\ProblemBookMapper.xml.bak"

# 替换 ILIKE 为 LIKE
Get-ChildItem "src\main\resources\mapper\*.xml" | ForEach-Object {
    (Get-Content $_.FullName) -replace ' ILIKE ', ' LIKE ' | Set-Content $_.FullName
}

# 替换 TRUE 为 1
Get-ChildItem "src\main\resources\mapper\*.xml" | ForEach-Object {
    (Get-Content $_.FullName) -replace '= TRUE', '= 1' | Set-Content $_.FullName
}

# 替换 CURRENT_DATE 为 CURDATE()
Get-ChildItem "src\main\resources\mapper\*.xml" | ForEach-Object {
    (Get-Content $_.FullName) -replace 'CURRENT_DATE', 'CURDATE()' | Set-Content $_.FullName
}

Write-Host "修改完成！请手动检查 ProblemBookMapper.xml 中的 similarity() 函数"
```

---

## 总结

完成以上 5 个修改后，你的应用就可以在 MySQL 上运行了。主要修改点：

1. ✅ 数据库驱动：PostgreSQL → MySQL
2. ✅ 数据源配置：连接字符串和方言
3. ✅ SQL 语法：`ILIKE` → `LIKE`
4. ✅ SQL 语法：`TRUE`/`FALSE` → `1`/`0`
5. ✅ SQL 函数：`similarity()` → `LIKE` 或 `MATCH AGAINST`
6. ✅ SQL 函数：`CURRENT_DATE` → `CURDATE()`

**预计修改时间**: 15-30 分钟

**难度**: ⭐⭐☆☆☆（中等偏易）

---

如有问题，请参考 `MYSQL_MIGRATION_GUIDE.md` 获取更详细的说明。

