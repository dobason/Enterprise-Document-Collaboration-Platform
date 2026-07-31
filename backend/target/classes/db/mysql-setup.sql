-- ============================================================
-- EDMS Local MySQL Setup Script
-- Chạy script này trên MySQL trước khi start backend
-- ============================================================

-- Tạo database (nếu chưa có)
CREATE DATABASE IF NOT EXISTS edms
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE edms;
