package com.recordmanagement.habitlog.domain.notification.application.strategy;

import com.recordmanagement.habitlog.domain.notification.application.dto.NotificationMessage;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import org.springframework.stereotype.Component;

/**
 * 습관 기록 알림 메시지 전략 구현체
 * 
 * OCP 적용: NotificationMessageStrategy 인터페이스 구현
 * - 습관 기록 특화 알림 메시지 생성 로직 캡슐화
 * - 다른 기록 타입과 독립적으로 관리
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0 (OCP 적용)
 */
@Component
public class HabitNotificationStrategy implements NotificationMessageStrategy {

    @Override
    public NotificationMessage createDailyRecordReminderMessage() {
        return new NotificationMessage(
            "오늘 습관 기록을 확인해보세요",
            "목표 달성을 위해 오늘의 습관을 체크해주세요!"
        );
    }

    @Override
    public RecordType getSupportedType() {
        return RecordType.HABIT;
    }
}