-- 운동 기록 테이블 생성
CREATE TABLE exercise_records (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    record_date DATE NOT NULL,
    exercise_type VARCHAR(50) NOT NULL,
    calories INTEGER,
    duration_minutes INTEGER,
    weight DECIMAL(5,2),
    steps INTEGER,
    memo TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    -- 외래키 제약조건
    CONSTRAINT fk_exercise_records_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- 유니크 제약조건 (하루에 하나의 운동 기록만)
    CONSTRAINT uk_exercise_records_user_date UNIQUE (user_id, record_date)
);

-- 인덱스 생성
CREATE INDEX idx_exercise_records_user_id ON exercise_records(user_id);
CREATE INDEX idx_exercise_records_record_date ON exercise_records(record_date);
CREATE INDEX idx_exercise_records_user_id_date ON exercise_records(user_id, record_date);