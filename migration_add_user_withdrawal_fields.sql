-- Migration script for adding withdrawal-related fields to users table
-- Execute this script to support soft delete with 7-day retention policy

USE habitlog_db;

-- Add new columns for soft delete functionality
ALTER TABLE users 
ADD COLUMN deleted_at DATETIME NULL COMMENT '회원 탈퇴 요청 시간',
ADD COLUMN deletion_scheduled_at DATETIME NULL COMMENT '실제 삭제 예정 시간 (탈퇴일 + 7일)',
ADD COLUMN withdrawal_reason VARCHAR(500) NULL COMMENT '회원 탈퇴 사유';

-- Add indexes for performance
CREATE INDEX idx_users_deleted_at ON users(deleted_at);
CREATE INDEX idx_users_deletion_scheduled_at ON users(deletion_scheduled_at);

-- Add index for finding expired withdrawn users (used by cleanup scheduler)
CREATE INDEX idx_users_expired_withdrawn ON users(deletion_scheduled_at, deleted_at);

COMMIT;

-- Verify the changes
DESCRIBE users;