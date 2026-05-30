package com.recordmanagement.habitlog.domain.notification.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 알림 설정 업데이트 요청 DTO
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "알림 설정 업데이트 요청")
public class UpdateNotificationSettingsRequest {

    @Schema(
        description = "메인 기록 미등록 알림 활성화 여부",
        example = "true",
        nullable = true
    )
    private Boolean dailyRecordNotificationEnabled;

    @Schema(
        description = "운동 기록 미등록 알림 활성화 여부",
        example = "true",
        nullable = true
    )
    private Boolean exerciseNotificationEnabled;

    @Schema(
        description = "습관 기록 미등록 알림 활성화 여부",
        example = "false",
        nullable = true
    )
    private Boolean habitNotificationEnabled;

    @Schema(
        description = "목표 미설정 알림 활성화 여부",
        example = "true",
        nullable = true
    )
    private Boolean goalSettingNotificationEnabled;

    @Schema(
        description = "일정 알림 활성화 여부",
        example = "true",
        nullable = true
    )
    private Boolean scheduleNotificationEnabled;
}