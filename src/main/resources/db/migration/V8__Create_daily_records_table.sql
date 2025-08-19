-- 일상 기록 테이블 생성
CREATE TABLE daily_records (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    record_date DATE NOT NULL,
    mood VARCHAR(20) NOT NULL,
    title VARCHAR(255),
    content TEXT,
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    -- 외래키 제약조건
    CONSTRAINT fk_daily_records_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- 유니크 제약조건 (하루에 하나의 일상 기록만)
    CONSTRAINT uk_daily_records_user_date UNIQUE (user_id, record_date)
);

-- 인덱스 생성
CREATE INDEX idx_daily_records_user_id ON daily_records(user_id);
CREATE INDEX idx_daily_records_record_date ON daily_records(record_date);