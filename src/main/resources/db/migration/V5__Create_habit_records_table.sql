-- 습관 기록 테이블 생성
CREATE TABLE habit_records (
    id VARCHAR(255) PRIMARY KEY,
    habit_id VARCHAR(255) NOT NULL,
    record_date DATE NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    actual_value VARCHAR(100),
    memo TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    -- 외래키 제약조건
    CONSTRAINT fk_habit_records_habit_id FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE,
    
    -- 유니크 제약조건 (하나의 습관에 하루에 하나의 기록만)
    CONSTRAINT uk_habit_records_habit_date UNIQUE (habit_id, record_date)
);

-- 인덱스 생성
CREATE INDEX idx_habit_records_habit_id ON habit_records(habit_id);
CREATE INDEX idx_habit_records_record_date ON habit_records(record_date);
CREATE INDEX idx_habit_records_habit_id_date ON habit_records(habit_id, record_date);