-- 修复 problem_books 表结构脚本
-- 问题：数据库中缺少 publish_year 字段
-- 解决方案：添加缺失的字段

-- 方式1：如果字段不存在则添加（推荐）
ALTER TABLE problem_books ADD COLUMN IF NOT EXISTS publish_year VARCHAR(4);

-- 查看修复后的表结构
\d problem_books

-- 方式2：如果需要重建整个表（慎用，会删除现有数据！）
/*
DROP TABLE IF EXISTS problem_books CASCADE;

CREATE TABLE problem_books (
    book_id BIGSERIAL PRIMARY KEY,
    book_name VARCHAR(500) NOT NULL,
    author VARCHAR(200),
    isbn VARCHAR(20),
    publisher VARCHAR(200),
    publish_year VARCHAR(4),
    problem_type VARCHAR(100),
    source VARCHAR(500),
    created_by BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    update_time TIMESTAMP,
    CONSTRAINT fk_pb_creator FOREIGN KEY (created_by) REFERENCES sys_user(user_id),
    CONSTRAINT fk_pb_updater FOREIGN KEY (updated_by) REFERENCES sys_user(user_id)
);

CREATE INDEX idx_pb_isbn ON problem_books(isbn);
CREATE INDEX idx_pb_book_name ON problem_books USING GIN (book_name gin_trgm_ops);
CREATE INDEX idx_pb_author ON problem_books(author);
CREATE INDEX idx_pb_publisher ON problem_books(publisher);
*/
