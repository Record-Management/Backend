package com.recordmanagement.habitlog.application.notification.dto;

import com.recordmanagement.habitlog.domain.user.model.UserId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 알림 설정 업데이트 명령 DTO
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public class NotificationSettingsCommand {

    private final UserId userId;
    private final Boolean dailyRecordNotificationEnabled;
    private final Boolean exerciseNotificationEnabled;
    private final Boolean habitNotificationEnabled;
}