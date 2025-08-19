-- 일상 기록 테이블에 위치 정보 컬럼 추가
ALTER TABLE daily_records ADD COLUMN location_name VARCHAR(255);
ALTER TABLE daily_records ADD COLUMN latitude DECIMAL(10,8);
ALTER TABLE daily_records ADD COLUMN longitude DECIMAL(11,8);