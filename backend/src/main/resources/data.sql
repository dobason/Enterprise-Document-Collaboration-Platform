-- ============================================================
-- EDMS Seed Data — mô phỏng doanh nghiệp thực tế
-- Role model: ADMIN (1 duy nhất) | MANAGER (trưởng phòng) | USER (nhân viên)
-- Mật khẩu mặc định mọi user: "Password123!" (BCrypt)
-- ============================================================

-- 1. DEPARTMENTS (6 phòng ban theo tổ chức thực tế)
INSERT INTO departments (id, code, name) VALUES
('dpt1', 'HR', 'Human Resources'),
('dpt2', 'FIN', 'Finance'),
('dpt3', 'LEG', 'Legal'),
('dpt4', 'MKT', 'Marketing'),
('dpt5', 'IT', 'IT Support'),
('dpt6', 'ENG', 'Engineering');

-- 2. USERS
-- ADMIN (1 tài khoản duy nhất)
INSERT INTO users (id, cognito_sub, email, password, name, role, department, department_id) VALUES
('u-admin', 'sub-admin-0001', 'admin@edms.vn', '$2a$10$5wRYAsOt0WIa6WkKsDDtd.JIU2ZCcZLTksJBUB2WEe.05chYIUUlW', 'Huynh Phuc Hung', 'ADMIN', 'Management', NULL);

-- MANAGERS (trưởng phòng — 1 người/phòng)
INSERT INTO users (id, cognito_sub, email, password, name, role, department, department_id) VALUES
('u-hr-mgr', 'sub-mgr-hr-001', 'hr.manager@edms.vn', '$2a$10$5wRYAsOt0WIa6WkKsDDtd.JIU2ZCcZLTksJBUB2WEe.05chYIUUlW', 'Nguyen Thi Mai', 'MANAGER', 'Human Resources', 'dpt1'),
('u-fin-mgr', 'sub-mgr-fin-001', 'fin.manager@edms.vn', '$2a$10$5wRYAsOt0WIa6WkKsDDtd.JIU2ZCcZLTksJBUB2WEe.05chYIUUlW', 'Tran Quoc Bao', 'MANAGER', 'Finance', 'dpt2'),
('u-leg-mgr', 'sub-mgr-leg-001', 'leg.manager@edms.vn', '$2a$10$5wRYAsOt0WIa6WkKsDDtd.JIU2ZCcZLTksJBUB2WEe.05chYIUUlW', 'Le Thi Huong', 'MANAGER', 'Legal', 'dpt3'),
('u-mkt-mgr', 'sub-mgr-mkt-001', 'mkt.manager@edms.vn', '$2a$10$5wRYAsOt0WIa6WkKsDDtd.JIU2ZCcZLTksJBUB2WEe.05chYIUUlW', 'Pham Van Long', 'MANAGER', 'Marketing', 'dpt4'),
('u-it-mgr', 'sub-mgr-it-001', 'it.manager@edms.vn', '$2a$10$5wRYAsOt0WIa6WkKsDDtd.JIU2ZCcZLTksJBUB2WEe.05chYIUUlW', 'Doan Minh Tri', 'MANAGER', 'IT Support', 'dpt5'),
('u-eng-mgr', 'sub-mgr-eng-001', 'eng.manager@edms.vn', '$2a$10$5wRYAsOt0WIa6WkKsDDtd.JIU2ZCcZLTksJBUB2WEe.05chYIUUlW', 'Hoang Duc Anh', 'MANAGER', 'Engineering', 'dpt6');

-- USERS (nhân viên các phòng ban)
INSERT INTO users (id, cognito_sub, email, password, name, role, department, department_id) VALUES
('u-hr-01', 'sub-hr-0001', 'lan.nguyen@edms.vn', '$2a$10$5wRYAsOt0WIa6WkKsDDtd.JIU2ZCcZLTksJBUB2WEe.05chYIUUlW', 'Nguyen Thi Lan', 'USER', 'Human Resources', 'dpt1'),
('u-hr-02', 'sub-hr-0002', 'hoa.tran@edms.vn', '$2a$10$5wRYAsOt0WIa6WkKsDDtd.JIU2ZCcZLTksJBUB2WEe.05chYIUUlW', 'Tran Thi Hoa', 'USER', 'Human Resources', 'dpt1'),
('u-fin-01', 'sub-fin-0001', 'tuan.le@edms.vn', '$2a$10$5wRYAsOt0WIa6WkKsDDtd.JIU2ZCcZLTksJBUB2WEe.05chYIUUlW', 'Le Minh Tuan', 'USER', 'Finance', 'dpt2'),
('u-fin-02', 'sub-fin-0002', 'thao.pham@edms.vn', '$2a$10$5wRYAsOt0WIa6WkKsDDtd.JIU2ZCcZLTksJBUB2WEe.05chYIUUlW', 'Pham Thi Thao', 'USER', 'Finance', 'dpt2'),
('u-leg-01', 'sub-leg-0001', 'hung.vo@edms.vn', '$2a$10$5wRYAsOt0WIa6WkKsDDtd.JIU2ZCcZLTksJBUB2WEe.05chYIUUlW', 'Vo Quoc Hung', 'USER', 'Legal', 'dpt3'),
('u-mkt-01', 'sub-mkt-0001', 'ngoc.bui@edms.vn', '$2a$10$5wRYAsOt0WIa6WkKsDDtd.JIU2ZCcZLTksJBUB2WEe.05chYIUUlW', 'Bui Thi Ngoc', 'USER', 'Marketing', 'dpt4'),
('u-mkt-02', 'sub-mkt-0002', 'khoa.phan@edms.vn', '$2a$10$5wRYAsOt0WIa6WkKsDDtd.JIU2ZCcZLTksJBUB2WEe.05chYIUUlW', 'Phan Dang Khoa', 'USER', 'Marketing', 'dpt4'),
('u-it-01', 'sub-it-0001', 'hieu.dang@edms.vn', '$2a$10$5wRYAsOt0WIa6WkKsDDtd.JIU2ZCcZLTksJBUB2WEe.05chYIUUlW', 'Dang Trung Hieu', 'USER', 'IT Support', 'dpt5'),
('u-it-02', 'sub-it-0002', 'minh.ly@edms.vn', '$2a$10$5wRYAsOt0WIa6WkKsDDtd.JIU2ZCcZLTksJBUB2WEe.05chYIUUlW', 'Ly Quang Minh', 'USER', 'IT Support', 'dpt5'),
('u-eng-01', 'sub-eng-0001', 'quan.ngo@edms.vn', '$2a$10$5wRYAsOt0WIa6WkKsDDtd.JIU2ZCcZLTksJBUB2WEe.05chYIUUlW', 'Ngo Minh Quan', 'USER', 'Engineering', 'dpt6'),
('u-eng-02', 'sub-eng-0002', 'vy.nguyen@edms.vn', '$2a$10$5wRYAsOt0WIa6WkKsDDtd.JIU2ZCcZLTksJBUB2WEe.05chYIUUlW', 'Nguyen Khanh Vy', 'USER', 'Engineering', 'dpt6');

-- 3. FOLDERS
INSERT INTO folders (id, name, department, department_id, owner_id) VALUES
('f-hr-01', 'Employee Records', 'Human Resources', 'dpt1', 'u-hr-mgr'),
('f-hr-02', 'Recruitment', 'Human Resources', 'dpt1', 'u-hr-mgr'),
('f-fin-01', 'Financial Reports', 'Finance', 'dpt2', 'u-fin-mgr'),
('f-fin-02', 'Budget Planning', 'Finance', 'dpt2', 'u-fin-mgr'),
('f-leg-01', 'Contracts', 'Legal', 'dpt3', 'u-leg-mgr'),
('f-leg-02', 'Legal Policies', 'Legal', 'dpt3', 'u-leg-mgr'),
('f-mkt-01', 'Campaigns', 'Marketing', 'dpt4', 'u-mkt-mgr'),
('f-it-01', 'System Documentation', 'IT Support', 'dpt5', 'u-it-mgr'),
('f-it-02', 'Infrastructure', 'IT Support', 'dpt5', 'u-it-mgr'),
('f-eng-01', 'Product Development', 'Engineering', 'dpt6', 'u-eng-mgr'),
('f-eng-02', 'Architecture', 'Engineering', 'dpt6', 'u-eng-mgr');

-- 4. DOCUMENTS
INSERT INTO documents (id, title, type, status, owner_id, folder_id, department_id, content, current_version_id, file_name, file_type) VALUES
-- HR
('doc-hr-01', 'Employee Handbook 2026', 'Policy', 'APPROVED', 'u-hr-01', 'f-hr-01', 'dpt1', '{"blocks":[{"key":"a1","text":"Employee Handbook 2026 - full guidelines","type":"unstyled"}]}', 'v-hr-01-2', 'employee_handbook_2026.pdf', 'application/pdf'),
('doc-hr-02', 'Recruitment Process Flow', 'Procedure', 'PENDING', 'u-hr-02', 'f-hr-02', 'dpt1', '{"blocks":[{"key":"a2","text":"End-to-end recruitment workflow","type":"unstyled"}]}', 'v-hr-02-1', 'recruitment_flow.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'),
-- Finance
('doc-fin-01', 'Q1 Financial Statement', 'Report', 'APPROVED', 'u-fin-01', 'f-fin-01', 'dpt2', '{"blocks":[{"key":"b1","text":"Consolidated Q1 financial results","type":"unstyled"}]}', 'v-fin-01-1', 'q1_financial_statement.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'),
('doc-fin-02', 'Annual Budget 2026 Draft', 'Budget', 'PENDING', 'u-fin-02', 'f-fin-02', 'dpt2', '{"blocks":[{"key":"b2","text":"2026 annual budget draft v2","type":"unstyled"}]}', 'v-fin-02-2', 'budget_2026_draft.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'),
-- Legal
('doc-leg-01', 'Vendor Agreement - 2026', 'Contract', 'APPROVED', 'u-leg-01', 'f-leg-01', 'dpt3', '{"blocks":[{"key":"c1","text":"Master vendor agreement 2026","type":"unstyled"}]}', 'v-leg-01-1', 'vendor_agreement_2026.pdf', 'application/pdf'),
('doc-leg-02', 'Data Privacy Policy', 'Policy', 'APPROVED', 'u-leg-mgr', 'f-leg-02', 'dpt3', '{"blocks":[{"key":"c2","text":"Company data privacy policy","type":"unstyled"}]}', 'v-leg-02-1', 'data_privacy_policy.pdf', 'application/pdf'),
-- Marketing
('doc-mkt-01', 'Q2 Campaign Strategy', 'Strategy', 'PENDING', 'u-mkt-01', 'f-mkt-01', 'dpt4', '{"blocks":[{"key":"d1","text":"Q2 integrated marketing campaign","type":"unstyled"}]}', 'v-mkt-01-1', 'q2_campaign_strategy.pptx', 'application/vnd.openxmlformats-officedocument.presentationml.presentation'),
-- IT
('doc-it-01', 'System Architecture Diagram', 'Architecture', 'APPROVED', 'u-it-01', 'f-it-01', 'dpt5', '{"blocks":[{"key":"e1","text":"Overall EDMS system architecture","type":"unstyled"}]}', 'v-it-01-1', 'system_architecture.pdf', 'application/pdf'),
('doc-it-02', 'Server Maintenance Runbook', 'Runbook', 'PENDING', 'u-it-02', 'f-it-02', 'dpt5', '{"blocks":[{"key":"e2","text":"Monthly server maintenance checklist","type":"unstyled"}]}', 'v-it-02-1', 'maintenance_runbook.pdf', 'application/pdf'),
-- Engineering
('doc-eng-01', 'Product Roadmap 2026', 'Roadmap', 'APPROVED', 'u-eng-01', 'f-eng-01', 'dpt6', '{"blocks":[{"key":"f1","text":"2026 product roadmap and milestones","type":"unstyled"}]}', 'v-eng-01-2', 'product_roadmap_2026.pdf', 'application/pdf'),
('doc-eng-02', 'API Design Specification', 'Specification', 'PENDING', 'u-eng-02', 'f-eng-02', 'dpt6', '{"blocks":[{"key":"f2","text":"REST API design for EDMS","type":"unstyled"}]}', 'v-eng-02-1', 'api_specification.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document');

-- 5. DOCUMENT VERSIONS
INSERT INTO document_versions (id, document_id, version_number, content, created_by) VALUES
('v-hr-01-1', 'doc-hr-01', 1, '{"blocks":[{"text":"Handbook draft"}]}', 'u-hr-01'),
('v-hr-01-2', 'doc-hr-01', 2, '{"blocks":[{"text":"Employee Handbook 2026 - full guidelines"}]}', 'u-hr-01'),
('v-hr-02-1', 'doc-hr-02', 1, '{"blocks":[{"text":"End-to-end recruitment workflow"}]}', 'u-hr-02'),
('v-fin-01-1', 'doc-fin-01', 1, '{"blocks":[{"text":"Consolidated Q1 financial results"}]}', 'u-fin-01'),
('v-fin-02-1', 'doc-fin-02', 1, '{"blocks":[{"text":"Budget v1"}]}', 'u-fin-02'),
('v-fin-02-2', 'doc-fin-02', 2, '{"blocks":[{"text":"2026 annual budget draft v2"}]}', 'u-fin-02'),
('v-leg-01-1', 'doc-leg-01', 1, '{"blocks":[{"text":"Master vendor agreement 2026"}]}', 'u-leg-01'),
('v-leg-02-1', 'doc-leg-02', 1, '{"blocks":[{"text":"Company data privacy policy"}]}', 'u-leg-mgr'),
('v-mkt-01-1', 'doc-mkt-01', 1, '{"blocks":[{"text":"Q2 integrated marketing campaign"}]}', 'u-mkt-01'),
('v-it-01-1', 'doc-it-01', 1, '{"blocks":[{"text":"Overall EDMS system architecture"}]}', 'u-it-01'),
('v-it-02-1', 'doc-it-02', 1, '{"blocks":[{"text":"Monthly server maintenance checklist"}]}', 'u-it-02'),
('v-eng-01-1', 'doc-eng-01', 1, '{"blocks":[{"text":"Roadmap draft"}]}', 'u-eng-01'),
('v-eng-01-2', 'doc-eng-01', 2, '{"blocks":[{"text":"2026 product roadmap and milestones"}]}', 'u-eng-01'),
('v-eng-02-1', 'doc-eng-02', 1, '{"blocks":[{"text":"REST API design for EDMS"}]}', 'u-eng-02');

-- 6. TAGS
INSERT INTO tags (id, name) VALUES
('t-urgent', 'Urgent'),
('t-conf', 'Confidential'),
('t-final', 'Final'),
('t-draft', 'Draft'),
('t-internal', 'Internal'),
('t-external', 'External');

-- 7. DOCUMENT TAGS
INSERT INTO document_tags (id, document_id, tag_id) VALUES
('dt-01', 'doc-hr-01', 't-final'),
('dt-02', 'doc-fin-01', 't-conf'),
('dt-03', 'doc-leg-01', 't-conf'),
('dt-04', 'doc-leg-01', 't-final'),
('dt-05', 'doc-mkt-01', 't-internal'),
('dt-06', 'doc-eng-01', 't-internal'),
('dt-07', 'doc-it-01', 't-internal');

-- 8. PERMISSIONS (quyền trên tài liệu - OWNER/EDITOR/VIEWER)
-- Mọi doc đều có OWNER permission cho chủ sở hữu (để owner truy cập được)
INSERT INTO permissions (id, document_id, user_id, role) VALUES
('perm-hr-01', 'doc-hr-01', 'u-hr-01', 'OWNER'),
('perm-hr-02', 'doc-hr-01', 'u-hr-02', 'EDITOR'),
('perm-hr-03', 'doc-hr-01', 'u-hr-mgr', 'VIEWER'),
('perm-hr-04', 'doc-hr-02', 'u-hr-02', 'OWNER'),
('perm-fin-01', 'doc-fin-01', 'u-fin-01', 'OWNER'),
('perm-fin-02', 'doc-fin-01', 'u-fin-02', 'EDITOR'),
('perm-fin-03', 'doc-fin-02', 'u-fin-02', 'OWNER'),
('perm-leg-01', 'doc-leg-01', 'u-leg-01', 'OWNER'),
('perm-leg-02', 'doc-leg-01', 'u-leg-mgr', 'VIEWER'),
('perm-leg-03', 'doc-leg-02', 'u-leg-mgr', 'OWNER'),
('perm-mkt-01', 'doc-mkt-01', 'u-mkt-01', 'OWNER'),
('perm-it-01', 'doc-it-01', 'u-it-01', 'OWNER'),
('perm-it-02', 'doc-it-02', 'u-it-02', 'OWNER'),
('perm-eng-01', 'doc-eng-01', 'u-eng-01', 'OWNER'),
('perm-eng-02', 'doc-eng-01', 'u-eng-02', 'EDITOR'),
('perm-eng-03', 'doc-eng-02', 'u-eng-02', 'OWNER');

-- 9. SHARES (link chia sẻ có thời hạn)
INSERT INTO shares (id, document_id, shared_by, shared_with_email, expires_at, token) VALUES
('sh-01', 'doc-eng-01', 'u-eng-01', 'client.partner@external.com', DATE_ADD(NOW(), INTERVAL 7 DAY), 'share-token-eng-01'),
('sh-02', 'doc-leg-01', 'u-leg-01', 'law.firm@external.com', DATE_ADD(NOW(), INTERVAL 3 DAY), 'share-token-leg-01');

-- 10. APPROVAL HISTORIES
INSERT INTO approval_histories (id, document_id, action, from_status, to_status, performed_by, timestamp) VALUES
('ah-hr-01', 'doc-hr-01', 'SUBMIT', 'DRAFT', 'PENDING', 'u-hr-01', NOW() - INTERVAL 3 DAY),
('ah-hr-02', 'doc-hr-01', 'APPROVE', 'PENDING', 'APPROVED', 'u-hr-mgr', NOW() - INTERVAL 2 DAY),
('ah-fin-01', 'doc-fin-01', 'SUBMIT', 'DRAFT', 'PENDING', 'u-fin-01', NOW() - INTERVAL 2 DAY),
('ah-fin-02', 'doc-fin-01', 'APPROVE', 'PENDING', 'APPROVED', 'u-fin-mgr', NOW() - INTERVAL 1 DAY),
('ah-leg-01', 'doc-leg-01', 'SUBMIT', 'DRAFT', 'PENDING', 'u-leg-01', NOW() - INTERVAL 4 DAY),
('ah-leg-02', 'doc-leg-01', 'APPROVE', 'PENDING', 'APPROVED', 'u-leg-mgr', NOW() - INTERVAL 3 DAY),
('ah-eng-01', 'doc-eng-01', 'SUBMIT', 'DRAFT', 'PENDING', 'u-eng-01', NOW() - INTERVAL 2 DAY),
('ah-eng-02', 'doc-eng-01', 'APPROVE', 'PENDING', 'APPROVED', 'u-eng-mgr', NOW() - INTERVAL 1 DAY);
