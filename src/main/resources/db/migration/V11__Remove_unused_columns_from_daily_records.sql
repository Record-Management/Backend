-- 일상 기록 테이블에서 사용하지 않는 컬럼들 제거
ALTER TABLE daily_records DROP COLUMN audio_url;
ALTER TABLE daily_records DROP COLUMN location_name;
ALTER TABLE daily_records DROP COLUMN latitude;
ALTER TABLE daily_records DROP COLUMN longitude;