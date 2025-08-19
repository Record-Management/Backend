-- 습관 테이블 생성
CREATE TABLE habits (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    target_value VARCHAR(100),
    unit VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    -- 외래키 제약조건
    CONSTRAINT fk_habits_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 인덱스 생성
CREATE INDEX idx_habits_user_id ON habits(user_id);
CREATE INDEX idx_habits_user_id_active ON habits(user_id, active);