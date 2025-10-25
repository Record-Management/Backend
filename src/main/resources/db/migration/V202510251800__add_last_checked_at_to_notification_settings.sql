-- 알림 설정 테이블에 마지막 확인 시간 컬럼 추가
-- 프론트엔드에서 읽음/안읽음 상태 판단용

ALTER TABLE notification_settings 
ADD COLUMN last_checked_at TIMESTAMP NULL COMMENT '알림 센터 마지막 확인 시간 (읽음 처리용)';

-- 인덱스 추가 (조회 성능 향상용)
CREATE INDEX idx_notification_settings_last_checked_at ON notification_settings(last_checked_at);