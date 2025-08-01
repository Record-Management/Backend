-- HabitLog Database Initialization Script
-- This script is executed when MySQL container starts for the first time

CREATE DATABASE IF NOT EXISTS habitlog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE habitlog_db;

-- Grant privileges to habitlog user
GRANT ALL PRIVILEGES ON habitlog_db.* TO 'habitlog'@'%';
FLUSH PRIVILEGES;

-- Set timezone
SET time_zone = '+09:00';
