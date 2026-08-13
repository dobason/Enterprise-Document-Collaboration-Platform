-- V2: Thêm cột lưu Step Functions task token cho workflow phê duyệt
ALTER TABLE documents ADD COLUMN IF NOT EXISTS task_token VARCHAR(1024) NULL;
