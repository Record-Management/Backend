package com.recordmanagement.habitlog.domain.notification.application.strategy;

import com.recordmanagement.habitlog.domain.notification.application.dto.NotificationMessage;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import org.springframework.stereotype.Component;

/**
 * 운동 기록 알림 메시지 전략 구현체
 * 
 * OCP 적용: NotificationMessageStrategy 인터페이스 구현
 * - 운동 기록 특화 알림 메시지 생성 로직 캡슐화
 * - 다른 기록 타입과 독립적으로 관리
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0 (OCP 적용)
 */
@Component
public class ExerciseNotificationStrategy implements NotificationMessageStrategy {

    @Override
    public NotificationMessage createDailyRecordReminderMessage() {
        return new NotificationMessage(
            "오늘 운동 기록을 등록하지 않았어요",
            "꾸준한 운동 기록으로 건강한 습관을 만들어보세요!"
        );
    }

    @Override
    public RecordType getSupportedType() {
        return RecordType.EXERCISE;
    }
}