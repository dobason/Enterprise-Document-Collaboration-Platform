-- Initial Seed Data for EDMS Testing

-- 1. Departments
INSERT INTO departments (id, code, name) VALUES 
('dpt1', 'ENG', 'Engineering'),
('dpt2', 'HR', 'HR'),
('dpt3', 'MGMT', 'Management');

-- 2. Users (Passwords: "Password123!" BCrypt hashed as $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVym5p.Qe48b3M0cT4nC7.1O or matched)
INSERT INTO users (id, cognito_sub, email, password, name, role, department, department_id, avatar) VALUES 
('u1', 'sub-owner-123', 'owner@edms.vn', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVym5p.Qe48b3M0cT4nC7.1O', 'Nguyen Van A', 'OWNER', 'Engineering', 'dpt1', NULL),
('u2', 'sub-editor-456', 'editor@edms.vn', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVym5p.Qe48b3M0cT4nC7.1O', 'Tran Thi B', 'EDITOR', 'Engineering', 'dpt1', NULL),
('u3', 'sub-manager-789', 'manager@edms.vn', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVym5p.Qe48b3M0cT4nC7.1O', 'Le Van C', 'MANAGER', 'Engineering', 'dpt1', NULL),
('u4', 'sub-viewer-012', 'viewer@edms.vn', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVym5p.Qe48b3M0cT4nC7.1O', 'Pham Van D', 'VIEWER', 'HR', 'dpt2', NULL),
('admin', 'sub-admin-999', 'admin@edms.vn', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVym5p.Qe48b3M0cT4nC7.1O', 'Admin User', 'ADMIN', 'Management', 'dpt3', NULL);

-- 3. Folders
INSERT INTO folders (id, name, department, department_id, owner_id) VALUES 
('f1', 'Contracts', 'Engineering', 'dpt1', 'u1'),
('f2', 'HR Documents', 'HR', 'dpt2', 'u4'),
('f3', 'Engineering Reports', 'Engineering', 'dpt1', 'u1');

-- 4. Documents
INSERT INTO documents (id, title, type, status, owner_id, folder_id, department_id, content, current_version_id, file_name, file_type, s3_key) VALUES 
('d1', 'Q1 Engineering Report', 'Report', 'APPROVED', 'u1', 'f3', 'dpt1', '{"blocks":[{"key":"abc","text":"Q1 Report content","type":"unstyled"}]}', 'v3', 'report_q1.pdf', 'application/pdf', 'uploads/report_q1.pdf'),
('d2', 'HR Policy 2025', 'Policy', 'APPROVED', 'u4', 'f2', 'dpt2', '{"blocks":[{"key":"def","text":"HR Policy details","type":"unstyled"}]}', 'v1', 'policy_2025.pdf', 'application/pdf', 'uploads/policy_2025.pdf'),
('d3', 'Software Architecture Proposal', 'Report', 'PENDING', 'u1', 'f3', 'dpt1', '{"blocks":[{"key":"ghi","text":"Architecture proposal draft","type":"unstyled"}]}', 'v1', 'arch_prop.pdf', 'application/pdf', 'uploads/arch_prop.pdf');

-- 5. Document Versions
INSERT INTO document_versions (id, document_id, version_number, content, s3_key, created_by) VALUES 
('v1', 'd1', 1, '{"blocks":[{"text":"Draft v1"}]}', 'uploads/d1_v1.pdf', 'u1'),
('v2', 'd1', 2, '{"blocks":[{"text":"Draft v2"}]}', 'uploads/d1_v2.pdf', 'u1'),
('v3', 'd1', 3, '{"blocks":[{"text":"Q1 Report content"}]}', 'uploads/report_q1.pdf', 'u4');

-- 6. Tags
INSERT INTO tags (id, name) VALUES 
('t1', 'Urgent'),
('t2', 'Confidential'),
('t4', 'Final');

-- 7. Document Tags
INSERT INTO document_tags (id, document_id, tag_id) VALUES 
('dt1', 'd1', 't1'),
('dt2', 'd1', 't4'),
('dt3', 'd2', 't2');

-- 8. Permissions
INSERT INTO permissions (id, document_id, user_id, role) VALUES 
('p1', 'd1', 'u1', 'OWNER'),
('p2', 'd1', 'u2', 'EDITOR');

-- 9. OCR Results
INSERT INTO ocr_results (id, document_id, status, text, extracted_at) VALUES 
('ocr1', 'd1', 'completed', 'Q1 Engineering Report extracted text from document', CURRENT_TIMESTAMP);
