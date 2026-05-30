package com.recordmanagement.habitlog.domain.notification.application.dto;

import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 히스토리 응답 DTO (Redoc 형태)
 * 
 * 프론트엔드 요청사항:
 * - id, type, title, message, sentAt, isRead 형태
 * - 피그마 명세에 맞는 title, message
 *
 * @author 전우선
 * @since 2025.11.18
 * @version 2.0.0
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "알림 히스토리 응답")
public class NotificationHistoryResponse {

    @Schema(description = "알림 고유 식별자", example = "notification-123")
    private final String id;

    @Schema(description = "알림 타입", example = "HABIT_REMINDER")
    private final String type;

    @Schema(description = "알림 제목", example = "습관 기록")
    private final String title;

    @Schema(description = "알림 메시지", example = "아직 '습관 기록'을 작성하지 않았어요. 기록이 쌓일수록 습관이 되고, 어느새 습관이 자연스러워질 거예요.")
    private final String message;

    @Schema(description = "알림 발송 시간", example = "2025-11-17T19:00:00")
    private final LocalDateTime sentAt;

    @Schema(description = "읽음 여부", example = "false")
    private final boolean isRead;

    /**
     * 도메인 모델로부터 응답 DTO 생성 (피그마 명세 기반)
     *
     * @param history 알림 히스토리 도메인 모델
     * @return 응답 DTO
     */
    public static NotificationHistoryResponse from(NotificationHistory history) {
        // 일정 알림의 경우 도메인 모델의 message를 사용 (일정명)
        if (history.getType() == com.recordmanagement.habitlog.domain.notification.domain.model.NotificationType.SCHEDULE_REMINDER) {
            return new NotificationHistoryResponse(
                history.getId().getValue(),
                history.getType().name(),
                "일정 기록",
                history.getMessage(), // 일정명을 그대로 사용
                history.getSentAt(),
                history.isRead()
            );
        }

        // 다른 알림 타입들은 기존처럼 고정 메시지 사용
        TitleMessage titleMessage = getTitleMessage(history.getType());

        return new NotificationHistoryResponse(
            history.getId().getValue(),
            history.getType().name(),
            titleMessage.title(),
            titleMessage.message(),
            history.getSentAt(),
            history.isRead()
        );
    }

    /**
     * 알림 타입에 따른 제목과 메시지 매핑 (피그마 명세 기반)
     */
    private static TitleMessage getTitleMessage(com.recordmanagement.habitlog.domain.notification.domain.model.NotificationType type) {
        return switch (type) {
            case DAILY_RECORD_REMINDER -> new TitleMessage(
                "하루 기록",
                "아직 '하루 기록'을 작성하지 않았어요. 하루의 작은 순간이 쌓이면 큰 변화가 돼요."
            );
            case EXERCISE_REMINDER -> new TitleMessage(
                "운동 기록", 
                "아직 '운동 기록'을 작성하지 않았어요. 기록이 쌓일수록 습관이 되고, 어느새 운동이 자연스러워질 거예요."
            );
            case HABIT_REMINDER -> new TitleMessage(
                "습관 기록",
                "아직 '습관 기록'을 작성하지 않았어요. 꾸준히 쌓이는 하루가 큰 변화를 만들 수 있어요."
            );
            case HABIT_TIME_BASED_REMINDER -> new TitleMessage(
                "습관 실천 알림",
                "습관을 실천할 시간이에요! 오늘도 꾸준히 이어가볼까요?"
            );
            case GOAL_SETTING_REMINDER -> new TitleMessage(
                "목표 설정",
                "아직 목표를 설정하지 않으셨어요! 지금부터 새로운 목표를 만들어볼까요?"
            );
            case SCHEDULE_REMINDER -> new TitleMessage(
                "일정 기록",
                "" // 일정 알림은 from() 메서드에서 별도 처리되므로 이 코드는 실행되지 않음
            );
            case SYSTEM_ANNOUNCEMENT -> new TitleMessage(
                "HabitLog",
                "시스템 공지사항"
            );
            case TEST -> new TitleMessage(
                "HabitLog",
                "테스트 알림 발송 완료"
            );
        };
    }

    /**
     * 제목과 메시지를 담는 레코드
     */
    private record TitleMessage(String title, String message) {}
}