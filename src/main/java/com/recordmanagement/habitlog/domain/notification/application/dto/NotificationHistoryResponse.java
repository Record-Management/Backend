package com.recordmanagement.habitlog.domain.notification.application.dto;

import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 히스토리 응답 DTO (간소화 버전)
 * 
 * 프론트엔드 요청사항:
 * - 메인 기록 타입
 * - 알림 설명 (가운데 부분)  
 * - 알림 발송 시간
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "알림 히스토리 간소화 응답")
public class NotificationHistoryResponse {

    @Schema(description = "메인 기록 타입", example = "EXERCISE")
    private final String mainRecordType;

    @Schema(description = "알림 설명", example = "꾸준한 운동 기록으로 건강한 습관을 만들어보세요!")
    private final String description;

    @Schema(description = "알림 발송 시간 [년, 월, 일, 시, 분, 초]", example = "[2025, 10, 23, 20, 0, 0]")
    private final LocalDateTime sentAt;

    /**
     * 도메인 모델로부터 간소화된 응답 DTO 생성
     *
     * @param history 알림 히스토리 도메인 모델
     * @return 간소화된 응답 DTO
     */
    public static NotificationHistoryResponse from(NotificationHistory history) {
        // 알림 타입에 따른 메인 기록 타입 매핑
        String mainRecordType = mapNotificationTypeToRecordType(history.getType());
        
        // 알림 내용(message)을 설명으로 사용
        String description = history.getMessage();
        
        return new NotificationHistoryResponse(
            mainRecordType,
            description,
            history.getSentAt()
        );
    }

    /**
     * 알림 타입을 메인 기록 타입으로 매핑
     *
     * @param notificationType 알림 타입
     * @return 메인 기록 타입 문자열
     */
    private static String mapNotificationTypeToRecordType(com.recordmanagement.habitlog.domain.notification.domain.model.NotificationType notificationType) {
        return switch (notificationType) {
            case DAILY_RECORD_REMINDER -> "DAILY";
            case EXERCISE_REMINDER -> "EXERCISE";  
            case HABIT_REMINDER -> "HABIT";
            case SYSTEM_ANNOUNCEMENT, TEST -> "SYSTEM";
        };
    }
}