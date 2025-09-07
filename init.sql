-- HabitLog Database Initialization Script
-- This script is executed when MySQL container starts for the first time

CREATE DATABASE IF NOT EXISTS habitlog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE habitlog_db;

-- Grant privileges to habitlog user
GRANT ALL PRIVILEGES ON habitlog_db.* TO 'habitlog'@'%';
FLUSH PRIVILEGES;

-- Set timezone
SET time_zone = '+09:00';

-- Records table
CREATE TABLE IF NOT EXISTS records (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    type ENUM('EXERCISE', 'DAILY', 'SCHEDULE', 'HABIT') NOT NULL,
    emotion VARCHAR(10),
    content TEXT NOT NULL,
    image_urls TEXT,
    record_date DATE NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_user_id_record_date (user_id, record_date),
    INDEX idx_user_id_type (user_id, type),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
