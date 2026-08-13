-- V2: Thêm cột lưu Step Functions task token cho workflow phê duyệt
-- Aurora MySQL không hỗ trợ ADD COLUMN IF NOT EXISTS nên dùng procedure kiểm tra tồn tại
SET @col_exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'documents'
      AND COLUMN_NAME = 'task_token'
);

SET @sql := IF(@col_exists = 0,
    'ALTER TABLE documents ADD COLUMN task_token VARCHAR(1024) NULL',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
