-- 온보딩 필드 추가: 생년월일, 목표일수, 알림허용여부
ALTER TABLE users 
ADD COLUMN birth_date DATE COMMENT '생년월일',
ADD COLUMN goal_days INT COMMENT '목표 일수 (10-30일)',
ADD COLUMN notification_enabled BOOLEAN DEFAULT FALSE COMMENT '알림 허용 여부';