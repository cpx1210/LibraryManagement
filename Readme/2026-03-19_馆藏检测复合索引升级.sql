ALTER TABLE collection_books
    ADD INDEX idx_cb_problem_create_barcode (is_problem, create_time DESC, barcode DESC),
    ADD INDEX idx_cb_problem_branch_create_barcode (is_problem, branch_library, create_time DESC, barcode DESC);
