-- Refresh Tokens 테이블 생성
CREATE TABLE refresh_tokens (
    token VARCHAR(512) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    INDEX idx_refresh_tokens_user_id (user_id),
    INDEX idx_refresh_tokens_expires_at (expires_at),
    UNIQUE KEY uk_refresh_tokens_user_id (user_id)
);