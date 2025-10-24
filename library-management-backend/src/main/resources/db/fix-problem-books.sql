-- 修复 problem_books 表结构
-- 添加缺失的 publish_year 字段

-- 检查并添加 publish_year 列（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'problem_books'
          AND column_name = 'publish_year'
    ) THEN
        ALTER TABLE problem_books ADD COLUMN publish_year VARCHAR(4);
        RAISE NOTICE 'Column publish_year added to problem_books table';
    ELSE
        RAISE NOTICE 'Column publish_year already exists in problem_books table';
    END IF;
END $$;

-- 确认表结构
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'problem_books'
ORDER BY ordinal_position;
