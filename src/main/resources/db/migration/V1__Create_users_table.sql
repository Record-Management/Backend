-- Users 테이블 생성 (MySQL, H2 호환)
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    social_type VARCHAR(50) NOT NULL,
    social_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 인덱스 생성 (이미 존재하면 무시)
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_social_login ON users (social_type, social_id);

-- 소셜 로그인 중복 방지를 위한 유니크 제약 (이미 존재하면 무시)
ALTER TABLE users 
ADD CONSTRAINT IF NOT EXISTS uk_users_social_login UNIQUE (social_type, social_id);