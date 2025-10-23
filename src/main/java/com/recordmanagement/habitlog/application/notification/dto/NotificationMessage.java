package com.recordmanagement.habitlog.application.notification.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 알림 메시지 DTO
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public class NotificationMessage {

    private final String title;
    private final String body;
}