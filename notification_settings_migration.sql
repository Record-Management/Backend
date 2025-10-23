-- 알림 설정 테이블 생성 (notification_settings)
-- 사용자별 알림 설정을 별도 테이블로 관리
-- User 엔티티와 분리하여 알림 관련 설정의 독립성 확보

CREATE TABLE notification_settings (
    id VARCHAR(50) NOT NULL COMMENT '알림 설정 고유 식별자',
    user_id VARCHAR(50) NOT NULL COMMENT '사용자 ID (users 테이블 참조)',
    daily_record_notification_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '메인 기록 미등록 알림 활성화 여부',
    exercise_notification_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '운동 기록 알림 활성화 여부',
    habit_notification_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '습관 기록 알림 활성화 여부',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
    PRIMARY KEY (id),
    UNIQUE KEY unique_user_notification_settings (user_id),
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_notification_settings_user_id 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='사용자별 알림 설정 테이블';

-- 기존 사용자들에 대한 기본 알림 설정 생성
-- 모든 기존 사용자에게 기본값(모든 알림 활성화)으로 알림 설정 생성
INSERT INTO notification_settings (id, user_id, daily_record_notification_enabled, exercise_notification_enabled, habit_notification_enabled)
SELECT 
    CONCAT('notification-', UUID()) as id,
    id as user_id,
    TRUE as daily_record_notification_enabled,
    TRUE as exercise_notification_enabled,
    TRUE as habit_notification_enabled
FROM users 
WHERE deleted_at IS NULL  -- 탈퇴하지 않은 사용자만
AND id NOT IN (SELECT user_id FROM notification_settings);  -- 이미 설정이 없는 사용자만

-- 알림 설정 테이블 인덱스 최적화
-- 자주 조회되는 패턴에 맞춘 복합 인덱스 추가
CREATE INDEX idx_notification_user_daily ON notification_settings (user_id, daily_record_notification_enabled);
CREATE INDEX idx_notification_user_exercise ON notification_settings (user_id, exercise_notification_enabled);
CREATE INDEX idx_notification_user_habit ON notification_settings (user_id, habit_notification_enabled);