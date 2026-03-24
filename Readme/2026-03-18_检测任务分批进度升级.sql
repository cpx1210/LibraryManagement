ALTER TABLE booklist_check_task
    ADD COLUMN processed_books INT DEFAULT 0 COMMENT '已处理书目数' AFTER total_books,
    ADD COLUMN current_batch INT DEFAULT 0 COMMENT '当前批次' AFTER processed_books,
    ADD COLUMN total_batches INT DEFAULT 0 COMMENT '总批次数' AFTER current_batch;
