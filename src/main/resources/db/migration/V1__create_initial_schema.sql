-- V1: 초기 스키마 생성

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    social_id VARCHAR(255) NOT NULL UNIQUE,
    provider ENUM('KAKAO', 'APPLE') NOT NULL,
    onboarding_completed BOOLEAN DEFAULT FALSE,
    nickname VARCHAR(6),
    main_record_type ENUM('PHOTO', 'TEXT') DEFAULT NULL,
    birth_date DATE DEFAULT NULL,
    goal_days INT DEFAULT NULL,
    notification_enabled BOOLEAN DEFAULT NULL,
    refresh_token TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_social_id (social_id),
    INDEX idx_provider (provider),
    INDEX idx_onboarding_completed (onboarding_completed)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_token (token),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);