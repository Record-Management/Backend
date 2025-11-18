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
            "운동 기록",
            "아직 '운동 기록'을 작성하지 않았어요. 기록이 쌓일수록 습관이 되고, 어느새 운동이 자연스러워질 거예요."
        );
    }

    @Override
    public RecordType getSupportedType() {
        return RecordType.EXERCISE;
    }
}