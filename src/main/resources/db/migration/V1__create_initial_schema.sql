-- V1: 초기 스키마 생성

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    social_type ENUM('KAKAO', 'APPLE') NOT NULL,
    social_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE,
    nickname VARCHAR(6),
    main_record_type ENUM('EXERCISE', 'DAILY', 'SCHEDULE', 'HABIT'),
    birth_date DATE,
    goal_days INT,
    notification_enabled BOOLEAN,
    
    INDEX idx_social_id (social_id),
    INDEX idx_social_type (social_type),
    INDEX idx_onboarding_completed (onboarding_completed)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    token VARCHAR(512) NOT NULL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id)
);