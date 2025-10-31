-- =====================================================
-- 数据库迁移脚本：v1.0.0 -> v1.1.0
-- =====================================================
-- Database: PostgreSQL 15.x
-- Migration Date: 2025-10-30
-- Description: Add new fields for template adaptation
-- IMPORTANT: This script is safe to run multiple times
-- =====================================================

-- Set client encoding to UTF8
SET client_encoding = 'UTF8';

-- Start transaction
BEGIN;

-- =====================================================
-- 1. Update sensitive_words table
-- =====================================================
DO $$
BEGIN
    RAISE NOTICE '开始更新 sensitive_words 表...';

    -- Add detection_type column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'sensitive_words' AND column_name = 'detection_type'
    ) THEN
        ALTER TABLE sensitive_words
        ADD COLUMN detection_type VARCHAR(20) NOT NULL DEFAULT '关键词';

        -- Add check constraint
        ALTER TABLE sensitive_words
        ADD CONSTRAINT check_detection_type CHECK (detection_type IN ('关键词', '书名', '作者'));

        RAISE NOTICE '✓ 已添加 detection_type 字段';
    ELSE
        RAISE NOTICE '○ detection_type 字段已存在，跳过';
    END IF;

    -- Add alert_message column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'sensitive_words' AND column_name = 'alert_message'
    ) THEN
        ALTER TABLE sensitive_words
        ADD COLUMN alert_message VARCHAR(200);

        RAISE NOTICE '✓ 已添加 alert_message 字段';
    ELSE
        RAISE NOTICE '○ alert_message 字段已存在，跳过';
    END IF;

    -- Add index for detection_type
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE tablename = 'sensitive_words' AND indexname = 'idx_sw_detection_type'
    ) THEN
        CREATE INDEX idx_sw_detection_type ON sensitive_words(detection_type);
        RAISE NOTICE '✓ 已创建 idx_sw_detection_type 索引';
    ELSE
        RAISE NOTICE '○ idx_sw_detection_type 索引已存在，跳过';
    END IF;

    RAISE NOTICE '✅ sensitive_words 表更新完成';
END $$;

-- Add comments for new columns
COMMENT ON COLUMN sensitive_words.detection_type IS 'Detection Type: 关键词-Global, 书名-BookName Only, 作者-Author Only';
COMMENT ON COLUMN sensitive_words.alert_message IS 'Alert Message: Warning message when keyword is matched';

-- =====================================================
-- 2. Update booklist_check_detail table
-- =====================================================
DO $$
BEGIN
    RAISE NOTICE '开始更新 booklist_check_detail 表...';

    -- Add book_number column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'book_number'
    ) THEN
        ALTER TABLE booklist_check_detail
        ADD COLUMN book_number VARCHAR(50);
        RAISE NOTICE '✓ 已添加 book_number 字段';
    ELSE
        RAISE NOTICE '○ book_number 字段已存在，跳过';
    END IF;

    -- Add subtitle column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'subtitle'
    ) THEN
        ALTER TABLE booklist_check_detail
        ADD COLUMN subtitle VARCHAR(200);
        RAISE NOTICE '✓ 已添加 subtitle 字段';
    ELSE
        RAISE NOTICE '○ subtitle 字段已存在，跳过';
    END IF;

    -- Add author1 column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'author1'
    ) THEN
        ALTER TABLE booklist_check_detail
        ADD COLUMN author1 VARCHAR(100);

        -- Migrate existing author data to author1
        UPDATE booklist_check_detail SET author1 = author WHERE author IS NOT NULL;

        RAISE NOTICE '✓ 已添加 author1 字段并迁移数据';
    ELSE
        RAISE NOTICE '○ author1 字段已存在，跳过';
    END IF;

    -- Add author2 column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'author2'
    ) THEN
        ALTER TABLE booklist_check_detail
        ADD COLUMN author2 VARCHAR(100);
        RAISE NOTICE '✓ 已添加 author2 字段';
    ELSE
        RAISE NOTICE '○ author2 字段已存在，跳过';
    END IF;

    -- Add publish_location column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'publish_location'
    ) THEN
        ALTER TABLE booklist_check_detail
        ADD COLUMN publish_location VARCHAR(100);
        RAISE NOTICE '✓ 已添加 publish_location 字段';
    ELSE
        RAISE NOTICE '○ publish_location 字段已存在，跳过';
    END IF;

    -- Add publish_date column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'publish_date'
    ) THEN
        ALTER TABLE booklist_check_detail
        ADD COLUMN publish_date VARCHAR(50);
        RAISE NOTICE '✓ 已添加 publish_date 字段';
    ELSE
        RAISE NOTICE '○ publish_date 字段已存在，跳过';
    END IF;

    -- Add target_audience column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'target_audience'
    ) THEN
        ALTER TABLE booklist_check_detail
        ADD COLUMN target_audience VARCHAR(100);
        RAISE NOTICE '✓ 已添加 target_audience 字段';
    ELSE
        RAISE NOTICE '○ target_audience 字段已存在，跳过';
    END IF;

    -- Add content_summary column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'content_summary'
    ) THEN
        ALTER TABLE booklist_check_detail
        ADD COLUMN content_summary TEXT;
        RAISE NOTICE '✓ 已添加 content_summary 字段';
    ELSE
        RAISE NOTICE '○ content_summary 字段已存在，跳过';
    END IF;

    -- Add classification_number column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'classification_number'
    ) THEN
        ALTER TABLE booklist_check_detail
        ADD COLUMN classification_number VARCHAR(50);
        RAISE NOTICE '✓ 已添加 classification_number 字段';
    ELSE
        RAISE NOTICE '○ classification_number 字段已存在，跳过';
    END IF;

    -- Add language column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'language'
    ) THEN
        ALTER TABLE booklist_check_detail
        ADD COLUMN language VARCHAR(50);
        RAISE NOTICE '✓ 已添加 language 字段';
    ELSE
        RAISE NOTICE '○ language 字段已存在，跳过';
    END IF;

    RAISE NOTICE '✅ booklist_check_detail 表更新完成';
END $$;

-- Add comments for new columns
COMMENT ON COLUMN booklist_check_detail.book_number IS 'Book Number';
COMMENT ON COLUMN booklist_check_detail.subtitle IS 'Subtitle';
COMMENT ON COLUMN booklist_check_detail.author1 IS 'Author 1 (First Author)';
COMMENT ON COLUMN booklist_check_detail.author2 IS 'Author 2 (Second Author)';
COMMENT ON COLUMN booklist_check_detail.publish_location IS 'Publish Location';
COMMENT ON COLUMN booklist_check_detail.publish_date IS 'Publish Date';
COMMENT ON COLUMN booklist_check_detail.target_audience IS 'Target Audience';
COMMENT ON COLUMN booklist_check_detail.content_summary IS 'Content Summary';
COMMENT ON COLUMN booklist_check_detail.classification_number IS 'Classification Number';
COMMENT ON COLUMN booklist_check_detail.language IS 'Language';

-- Update table comment
COMMENT ON TABLE booklist_check_detail IS 'Booklist Check Detail Table - Updated for new template (2025-10-30)';

-- =====================================================
-- 3. Verify Migration
-- =====================================================
DO $$
DECLARE
    v_sw_detection_type_exists BOOLEAN;
    v_sw_alert_message_exists BOOLEAN;
    v_bcd_book_number_exists BOOLEAN;
    v_bcd_subtitle_exists BOOLEAN;
    v_bcd_author1_exists BOOLEAN;
    v_bcd_author2_exists BOOLEAN;
    v_bcd_publish_location_exists BOOLEAN;
    v_bcd_publish_date_exists BOOLEAN;
    v_bcd_target_audience_exists BOOLEAN;
    v_bcd_content_summary_exists BOOLEAN;
    v_bcd_classification_number_exists BOOLEAN;
    v_bcd_language_exists BOOLEAN;
    v_all_success BOOLEAN := TRUE;
BEGIN
    RAISE NOTICE '';
    RAISE NOTICE '========================================';
    RAISE NOTICE '开始验证迁移结果...';
    RAISE NOTICE '========================================';

    -- Check sensitive_words table
    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'sensitive_words' AND column_name = 'detection_type'
    ) INTO v_sw_detection_type_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'sensitive_words' AND column_name = 'alert_message'
    ) INTO v_sw_alert_message_exists;

    -- Check booklist_check_detail table
    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'book_number'
    ) INTO v_bcd_book_number_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'subtitle'
    ) INTO v_bcd_subtitle_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'author1'
    ) INTO v_bcd_author1_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'author2'
    ) INTO v_bcd_author2_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'publish_location'
    ) INTO v_bcd_publish_location_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'publish_date'
    ) INTO v_bcd_publish_date_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'target_audience'
    ) INTO v_bcd_target_audience_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'content_summary'
    ) INTO v_bcd_content_summary_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'classification_number'
    ) INTO v_bcd_classification_number_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'booklist_check_detail' AND column_name = 'language'
    ) INTO v_bcd_language_exists;

    -- Print results
    RAISE NOTICE '';
    RAISE NOTICE '【sensitive_words 表】';
    IF v_sw_detection_type_exists THEN
        RAISE NOTICE '  ✓ detection_type 字段存在';
    ELSE
        RAISE NOTICE '  ✗ detection_type 字段不存在';
        v_all_success := FALSE;
    END IF;

    IF v_sw_alert_message_exists THEN
        RAISE NOTICE '  ✓ alert_message 字段存在';
    ELSE
        RAISE NOTICE '  ✗ alert_message 字段不存在';
        v_all_success := FALSE;
    END IF;

    RAISE NOTICE '';
    RAISE NOTICE '【booklist_check_detail 表】';
    IF v_bcd_book_number_exists THEN
        RAISE NOTICE '  ✓ book_number 字段存在';
    ELSE
        RAISE NOTICE '  ✗ book_number 字段不存在';
        v_all_success := FALSE;
    END IF;

    IF v_bcd_subtitle_exists THEN
        RAISE NOTICE '  ✓ subtitle 字段存在';
    ELSE
        RAISE NOTICE '  ✗ subtitle 字段不存在';
        v_all_success := FALSE;
    END IF;

    IF v_bcd_author1_exists THEN
        RAISE NOTICE '  ✓ author1 字段存在';
    ELSE
        RAISE NOTICE '  ✗ author1 字段不存在';
        v_all_success := FALSE;
    END IF;

    IF v_bcd_author2_exists THEN
        RAISE NOTICE '  ✓ author2 字段存在';
    ELSE
        RAISE NOTICE '  ✗ author2 字段不存在';
        v_all_success := FALSE;
    END IF;

    IF v_bcd_publish_location_exists THEN
        RAISE NOTICE '  ✓ publish_location 字段存在';
    ELSE
        RAISE NOTICE '  ✗ publish_location 字段不存在';
        v_all_success := FALSE;
    END IF;

    IF v_bcd_publish_date_exists THEN
        RAISE NOTICE '  ✓ publish_date 字段存在';
    ELSE
        RAISE NOTICE '  ✗ publish_date 字段不存在';
        v_all_success := FALSE;
    END IF;

    IF v_bcd_target_audience_exists THEN
        RAISE NOTICE '  ✓ target_audience 字段存在';
    ELSE
        RAISE NOTICE '  ✗ target_audience 字段不存在';
        v_all_success := FALSE;
    END IF;

    IF v_bcd_content_summary_exists THEN
        RAISE NOTICE '  ✓ content_summary 字段存在';
    ELSE
        RAISE NOTICE '  ✗ content_summary 字段不存在';
        v_all_success := FALSE;
    END IF;

    IF v_bcd_classification_number_exists THEN
        RAISE NOTICE '  ✓ classification_number 字段存在';
    ELSE
        RAISE NOTICE '  ✗ classification_number 字段不存在';
        v_all_success := FALSE;
    END IF;

    IF v_bcd_language_exists THEN
        RAISE NOTICE '  ✓ language 字段存在';
    ELSE
        RAISE NOTICE '  ✗ language 字段不存在';
        v_all_success := FALSE;
    END IF;

    RAISE NOTICE '';
    RAISE NOTICE '========================================';
    IF v_all_success THEN
        RAISE NOTICE '✅ 所有字段验证通过！';
        RAISE NOTICE '数据库已成功升级到 v1.1.0';
    ELSE
        RAISE NOTICE '❌ 部分字段验证失败！';
        RAISE NOTICE '请检查上述错误并重新执行脚本';
        RAISE EXCEPTION '迁移验证失败';
    END IF;
    RAISE NOTICE '========================================';
    RAISE NOTICE '';
END $$;

-- Commit transaction
COMMIT;

-- =====================================================
-- Migration Complete
-- =====================================================
-- Summary:
-- 1. Added detection_type and alert_message to sensitive_words table
-- 2. Added 10 new fields to booklist_check_detail table
-- 3. Created necessary indexes
-- 4. Added column comments
-- 5. Verified all changes
--
-- Database Version: v1.1.0
-- Migration Date: 2025-10-30
-- =====================================================
