package com.recordmanagement.habitlog.domain.notification.application.dto;

import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import lombok.Value;

/**
 * 알림 설정 업데이트 명령 DTO
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Value
public class NotificationSettingsCommand {
    UserId userId;
    Boolean dailyRecordNotificationEnabled;
    Boolean exerciseNotificationEnabled;
    Boolean habitNotificationEnabled;
    Boolean goalSettingNotificationEnabled;
}