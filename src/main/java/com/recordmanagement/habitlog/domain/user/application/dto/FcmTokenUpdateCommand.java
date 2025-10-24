package com.recordmanagement.habitlog.domain.user.application.dto;

import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * FCM 토큰 업데이트 명령 DTO
 * 
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public class FcmTokenUpdateCommand {
    private final UserId userId;
    private final String fcmToken;
}