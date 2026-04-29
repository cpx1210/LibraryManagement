USE library_management;

SET @stmt = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'booklist_check_task'
          AND column_name = 'submitter_name'
    ),
    'SELECT ''submitter_name already exists''',
    'ALTER TABLE booklist_check_task ADD COLUMN submitter_name VARCHAR(100) COMMENT ''上传人姓名'' AFTER submitted_by'
);
PREPARE s1 FROM @stmt;
EXECUTE s1;
DEALLOCATE PREPARE s1;

SET @stmt = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'booklist_check_task'
          AND column_name = 'submitter_department'
    ),
    'SELECT ''submitter_department already exists''',
    'ALTER TABLE booklist_check_task ADD COLUMN submitter_department VARCHAR(100) COMMENT ''上传人部门'' AFTER submitter_name'
);
PREPARE s2 FROM @stmt;
EXECUTE s2;
DEALLOCATE PREPARE s2;

SET @stmt = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'booklist_check_task'
          AND column_name = 'submitter_email'
    ),
    'SELECT ''submitter_email already exists''',
    'ALTER TABLE booklist_check_task ADD COLUMN submitter_email VARCHAR(100) COMMENT ''上传人邮箱'' AFTER submitter_department'
);
PREPARE s3 FROM @stmt;
EXECUTE s3;
DEALLOCATE PREPARE s3;

SET @stmt = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'booklist_check_task'
          AND column_name = 'submitter_employee_no'
    ),
    'SELECT ''submitter_employee_no already exists''',
    'ALTER TABLE booklist_check_task ADD COLUMN submitter_employee_no VARCHAR(50) COMMENT ''上传人工号'' AFTER submitter_email'
);
PREPARE s4 FROM @stmt;
EXECUTE s4;
DEALLOCATE PREPARE s4;

SET @stmt = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'booklist_check_task'
          AND column_name = 'submitter_mobile'
    ),
    'SELECT ''submitter_mobile already exists''',
    'ALTER TABLE booklist_check_task ADD COLUMN submitter_mobile VARCHAR(20) COMMENT ''上传人手机号'' AFTER submitter_employee_no'
);
PREPARE s5 FROM @stmt;
EXECUTE s5;
DEALLOCATE PREPARE s5;
