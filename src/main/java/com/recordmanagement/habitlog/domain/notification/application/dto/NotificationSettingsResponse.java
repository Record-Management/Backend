package com.recordmanagement.habitlog.domain.notification.application.dto;

import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationSettings;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 알림 설정 응답 DTO
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "알림 설정 응답")
public class NotificationSettingsResponse {

    @Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private final String userId;

    @Schema(description = "메인 기록 미등록 알림 활성화 여부", example = "true")
    private final boolean dailyRecordNotificationEnabled;

    @Schema(description = "운동 기록 알림 활성화 여부", example = "true")
    private final boolean exerciseNotificationEnabled;

    @Schema(description = "습관 기록 알림 활성화 여부", example = "false")
    private final boolean habitNotificationEnabled;

    @Schema(description = "목표 미설정 알림 활성화 여부", example = "true")
    private final boolean noGoalNotificationEnabled;

    /**
     * 도메인 모델로부터 응답 DTO 생성
     *
     * @param settings 알림 설정 도메인 모델
     * @return 응답 DTO
     */
    public static NotificationSettingsResponse from(NotificationSettings settings) {
        return new NotificationSettingsResponse(
            settings.getUserId().getValue(),
            settings.isDailyRecordNotificationEnabled(),
            settings.isExerciseNotificationEnabled(),
            settings.isHabitNotificationEnabled(),
            settings.isNoGoalNotificationEnabled()
        );
    }
}