-- 알림 히스토리 테이블 생성 (notification_history)
-- 발송된 알림의 기록을 저장하여 앱 내 알림 센터 기능 제공
-- FCM 푸시 알림과 별도로 알림 목록 조회 및 읽음 상태 관리

CREATE TABLE notification_history (
    id VARCHAR(50) NOT NULL COMMENT '알림 히스토리 고유 식별자',
    user_id VARCHAR(50) NOT NULL COMMENT '사용자 ID (users 테이블 참조)',
    type VARCHAR(50) NOT NULL COMMENT '알림 타입 (DAILY_RECORD_REMINDER, EXERCISE_REMINDER, HABIT_REMINDER, SYSTEM_ANNOUNCEMENT, TEST)',
    title VARCHAR(200) NOT NULL COMMENT '알림 제목',
    message VARCHAR(1000) NOT NULL COMMENT '알림 내용',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT '읽음 여부',
    sent_at TIMESTAMP NOT NULL COMMENT '발송 시간',
    read_at TIMESTAMP NULL COMMENT '읽은 시간',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
    PRIMARY KEY (id),
    INDEX idx_user_id_sent_at (user_id, sent_at DESC),
    INDEX idx_user_id_is_read (user_id, is_read),
    INDEX idx_sent_at (sent_at DESC),
    CONSTRAINT fk_notification_history_user_id 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='알림 히스토리 테이블 (앱 내 알림 센터용)';