-- 일정 기록 테이블 생성
CREATE TABLE schedule_records (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    schedule_type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    start_time TIME,
    end_time TIME,
    memo TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    -- 외래키 제약조건
    CONSTRAINT fk_schedule_records_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX idx_schedule_records_user_id ON schedule_records(user_id);
CREATE INDEX idx_schedule_records_start_date ON schedule_records(start_date);
CREATE INDEX idx_schedule_records_user_date ON schedule_records(user_id, start_date);